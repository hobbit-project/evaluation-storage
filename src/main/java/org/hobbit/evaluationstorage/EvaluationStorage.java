/**
 * This file is part of evaluation-storage.
 *
 * evaluation-storage is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * evaluation-storage is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with evaluation-storage.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.hobbit.evaluationstorage;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Iterator;

import org.apache.commons.io.IOUtils;
import org.hobbit.core.Commands;
import org.hobbit.core.components.AbstractEvaluationStorage;
import org.hobbit.core.components.Component;
import org.hobbit.core.data.ResultPair;
import org.hobbit.core.rabbit.RabbitMQUtils;
import org.hobbit.evaluationstorage.data.SerializableResult;
import org.hobbit.evaluationstorage.resultstore.FileResultStoreBasedFacadeDecorator;
import org.hobbit.evaluationstorage.resultstore.ResultStoreFacade;
import org.hobbit.evaluationstorage.resultstore.RiakResultStoreFacade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Evaluation Storage is a component that stores the gold standard results
 * as well as the responses of the benchmarked system during the computation
 * phase. During the evaluation phase it sends this data to the Evaluation
 * Module.
 *
 * @author Ruben Taelman (ruben.taelman@ugent.be)
 */
public class EvaluationStorage extends AbstractEvaluationStorage implements Component {

    private static final int MAX_OBJECT_SIZE = 10 * 1024 * 1024; // 10mb

    private static final Logger LOGGER = LoggerFactory.getLogger(EvaluationStorage.class);

    protected ResultStoreFacade resultStoreFacade;
    protected Exception exception;
    protected String storagePath;
    protected int maxObjectSize = MAX_OBJECT_SIZE;

    public EvaluationStorage() {
        this(null);
    }

    public EvaluationStorage(String storagePath) {
        this.storagePath = storagePath;
    }

    @Override
    public void init() throws Exception {
        LOGGER.info("Initializing evaluation storage");
        super.init();
        // create and init the storage facade(s)
        resultStoreFacade = createStoreFacade();
        resultStoreFacade = new FileResultStoreBasedFacadeDecorator(resultStoreFacade, maxObjectSize);
        if (storagePath != null) {
            ((FileResultStoreBasedFacadeDecorator) resultStoreFacade).setStorageFolder(storagePath);
        }
        this.resultStoreFacade.init();
        LOGGER.info("Initialized evaluation storage - done");
    }

    private ResultStoreFacade createStoreFacade() throws Exception {
        return new RiakResultStoreFacade(new ContainerController() {
            @Override
            public String createContainer(String imageName, String[] envVariables) {
                LOGGER.info("Creating container {}", imageName);
                for (String envVariable : envVariables) {
                    LOGGER.info("Environment variable {}", envVariable);
                }
                return EvaluationStorage.this.createContainer(imageName, envVariables);
            }

            @Override
            public void stopContainer(String containerId) {
                EvaluationStorage.this.stopContainer(containerId);
            }
        });
    }

    @Override
    public void run() throws Exception {
        super.run();
        if (exception != null) {
            throw new IllegalStateException("Got an unexpected exception. Aborting.", exception);
        }
    }

    @Override
    public void close() throws IOException {
        IOUtils.closeQuietly(resultStoreFacade);
        super.close();
    }

    @Override
    public void receiveExpectedResponseData(String s, long l, byte[] bytes) {
        resultStoreFacade.put(ResultType.EXPECTED, s, new SerializableResult(l, bytes));
    }

    @Override
    public void receiveResponseData(String s, long l, byte[] bytes) {
        resultStoreFacade.put(ResultType.ACTUAL, s, new SerializableResult(l, bytes));
    }

    @Override
    protected Iterator<? extends ResultPair> createIterator() {
        return resultStoreFacade.createIterator();
    }

    @Override
    public void receiveCommand(byte command, byte[] data) {
        // If this is the signal that a container stopped (and we have a class
        // that we
        // need to notify)
        if ((command == Commands.DOCKER_CONTAINER_TERMINATED) && (resultStoreFacade != null)) {
            ByteBuffer buffer = ByteBuffer.wrap(data);
            String containerName = RabbitMQUtils.readString(buffer);
            int exitCode = buffer.get();
            try {
                resultStoreFacade.containerStopped(containerName, exitCode);
            } catch (Exception e) {
                exception = e;
                // release the mutex
                super.receiveCommand(Commands.EVAL_STORAGE_TERMINATE, null);
            }
        } else {
            super.receiveCommand(command, data);
        }
    }

    public int getMaxObjectSize() {
        return maxObjectSize;
    }

    public void setMaxObjectSize(int maxObjectSize) {
        this.maxObjectSize = maxObjectSize;
    }
}

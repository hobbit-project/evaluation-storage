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

import org.apache.commons.collections4.IteratorUtils;
import org.apache.commons.io.IOUtils;
import org.hobbit.core.Commands;
import org.hobbit.core.components.AbstractEvaluationStorage;
import org.hobbit.core.data.ResultPair;
import org.hobbit.core.rabbit.RabbitMQUtils;
import org.hobbit.evaluationstorage.resultstore.FileResultStoreFacade;
import org.hobbit.evaluationstorage.resultstore.ResultStoreFacade;
import org.hobbit.evaluationstorage.resultstore.RiakResultStoreFacade;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.*;

/**
 * The Evaluation Storage is a component that stores the gold standard results
 * as well as the responses of the benchmarked system during the computation
 * phase. During the evaluation phase it sends this data to the Evaluation
 * Module.
 *
 * @author Ruben Taelman (ruben.taelman@ugent.be)
 */
public class EvaluationStorage extends AbstractEvaluationStorage {
    protected ResultStoreFacade smallResultStoreFacade;
    protected ResultStoreFacade bigResultStoreFacade;
    protected Exception exception;

    private static final int MAX_OBJECT_SIZE = 100 * 1024; // 100mb

    @Override
    public void init() throws Exception {
        super.init();
        // create and init riak storage for small results
        this.smallResultStoreFacade = new RiakResultStoreFacade(new ContainerController() {
            @Override
            public String createContainer(String imageName, String[] envVariables) {
                return EvaluationStorage.this.createContainer(imageName, envVariables);
            }

            @Override
            public void stopContainer(String containerId) {
                EvaluationStorage.this.stopContainer(containerId);
            }
        });
        this.smallResultStoreFacade.init();
        // create and init file storage for large results
        this.bigResultStoreFacade = new FileResultStoreFacade();
    }

    @Override
    public void run() throws Exception {
        smallResultStoreFacade.run();
        super.run();
        if (exception != null) {
            throw new IllegalStateException("Got an unexpected exception. Aborting.", exception);
        }
    }

    @Override
    public void close() throws IOException {
        IOUtils.closeQuietly(smallResultStoreFacade);
        super.close();
    }

    @Override
    public void receiveExpectedResponseData(String s, long l, byte[] bytes) {
        int actualSize = bytes.length / 1024;
        if (actualSize < MAX_OBJECT_SIZE) {
            smallResultStoreFacade.put(ResultType.EXPECTED, s, new SerializableResult(l, bytes));
        } else {
            bigResultStoreFacade.put(ResultType.EXPECTED, s, new SerializableResult(l, bytes));
        }
    }

    @Override
    public void receiveResponseData(String s, long l, byte[] bytes) {
        int actualSize = bytes.length / 1024;
        if (actualSize < MAX_OBJECT_SIZE) {
            smallResultStoreFacade.put(ResultType.ACTUAL, s, new SerializableResult(l, bytes));
        } else {
            bigResultStoreFacade.put(ResultType.ACTUAL, s, new SerializableResult(l, bytes));
        }
    }

    @Override
    protected Iterator<ResultPair> createIterator() {
        Iterator<ResultPair> si = smallResultStoreFacade.createIterator();
        Iterator<ResultPair> bi = bigResultStoreFacade.createIterator();

        return IteratorUtils.chainedIterator(si, bi);
    }

    @Override
    public void receiveCommand(byte command, byte[] data) {
        // If this is the signal that a container stopped
        if (command == Commands.DOCKER_CONTAINER_TERMINATED) {
            ByteBuffer buffer = ByteBuffer.wrap(data);
            String containerName = RabbitMQUtils.readString(buffer);
            int exitCode = buffer.get();
            try {
                smallResultStoreFacade.containerStopped(containerName, exitCode);
            } catch (Exception e) {
                exception = e;
                // release the mutex
                super.receiveCommand(Commands.EVAL_STORAGE_TERMINATE, null);
            }
        } else {
            super.receiveCommand(command, data);
        }
    }
}

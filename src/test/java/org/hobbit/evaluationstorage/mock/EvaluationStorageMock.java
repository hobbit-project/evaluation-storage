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
package org.hobbit.evaluationstorage.mock;

import org.hobbit.core.Commands;
import org.hobbit.evaluationstorage.EvaluationStorage;
import org.hobbit.evaluationstorage.resultstore.FileResultStoreBasedFacadeDecorator;
import org.hobbit.evaluationstorage.resultstore.ResultStoreFacade;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

/**
 * Mocked Evaluation Storage.
 *
 * @author Ruben Taelman (ruben.taelman@ugent.be)
 */
public class EvaluationStorageMock extends EvaluationStorage {

    private final CountDownLatch startedLatch = new CountDownLatch(1);
    
    @Override
    public void init() throws Exception {
        // create and init the storage facade(s)
        resultStoreFacade = new InMemoryResultStore();
        resultStoreFacade = new FileResultStoreBasedFacadeDecorator(resultStoreFacade, maxObjectSize);
        if (storagePath != null) {
            ((FileResultStoreBasedFacadeDecorator) resultStoreFacade).setStorageFolder(storagePath);
        }
        this.resultStoreFacade.init();
    }
    
    public EvaluationStorageMock() {
        super();
    }

    public EvaluationStorageMock(String storagePath) {
        super(storagePath);
    }

    public ResultStoreFacade getResultStoreFacade() {
        return this.resultStoreFacade;
    }
    
    @Override
    protected void sendToCmdQueue(byte command, byte[] data) throws IOException {
        if (command == Commands.EVAL_STORAGE_READY_SIGNAL) {
            startedLatch.countDown();
        }
        super.sendToCmdQueue(command, data);
    }

    public void waitUntilStarted() {
        try {
            startedLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

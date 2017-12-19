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

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Random;
import java.util.concurrent.ExecutionException;

import org.apache.commons.io.FileUtils;
import org.hobbit.core.data.ResultPair;
import org.hobbit.evaluationstorage.data.SerializableResult;
import org.hobbit.evaluationstorage.mock.EvaluationStorageMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for the {@link EvaluationStorage}.
 *
 * @author Ruben Taelman (ruben.taelman@ugent.be)
 */
public class EvaluationStorageTest {

    private static final String TASK1 = "task1";
    private static final String TASK2 = "task2";
    private static final String TASK3 = "task3";
    private static final Random RANDOM = new Random();

    private EvaluationStorageMock evaluationStorage;

    @Before
    public void init() throws Exception {
        File storageDir = new File(FileUtils.getTempDirectoryPath() + File.separator + RANDOM.nextInt());
        storageDir.deleteOnExit();
        storageDir.mkdirs();
        evaluationStorage = new EvaluationStorageMock(storageDir.getAbsolutePath());
        evaluationStorage.init();
        new Thread(() -> {
            try {
                evaluationStorage.run();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
        evaluationStorage.waitUntilStarted();
    }

    @After
    public void deinit() throws IOException {
        evaluationStorage.receiveCommand((byte) 10, null);
        evaluationStorage.close();
    }

    @Test
    public void testSendExpectedResponseData() throws ExecutionException, InterruptedException {
        byte[] task1DataExpected = new byte[]{3, 1};
        byte[] task2DataExpected = new byte[]{29, 12, 3, 2, 89, 2, 1};
        byte[] task3DataExpected = new byte[]{29, 92, 3, 18, 39, 29, 103, 1};
        evaluationStorage.receiveExpectedResponseData(TASK1, 0, task1DataExpected);
        evaluationStorage.receiveExpectedResponseData(TASK2, 1, task2DataExpected);
        evaluationStorage.receiveExpectedResponseData(TASK3, 2, task3DataExpected);

        SerializableResult result1 = evaluationStorage.getResultStoreFacade().get(ResultType.EXPECTED, TASK1).get();
        SerializableResult result2 = evaluationStorage.getResultStoreFacade().get(ResultType.EXPECTED, TASK2).get();
        SerializableResult result3 = evaluationStorage.getResultStoreFacade().get(ResultType.EXPECTED, TASK3).get();
        assertThat(result1.getData(), is(task1DataExpected));
        assertThat(result2.getData(), is(task2DataExpected));
        assertThat(result3.getData(), is(task3DataExpected));
        assertThat(result1.getSentTimestamp(), is(0L));
        assertThat(result2.getSentTimestamp(), is(1L));
        assertThat(result3.getSentTimestamp(), is(2L));
    }

    @Test
    public void testSendResponseData() throws ExecutionException, InterruptedException {
        byte[] task1DataActual = new byte[]{0, 1};
        byte[] task2DataActual = new byte[]{12, 3, 2, 89, 2, 1};
        byte[] task3DataActual = new byte[]{92, 3, 18, 39, 29, 103, 1};
        evaluationStorage.receiveResponseData(TASK1, 0, task1DataActual);
        evaluationStorage.receiveResponseData(TASK2, 1, task2DataActual);
        evaluationStorage.receiveResponseData(TASK3, 2, task3DataActual);

        SerializableResult result1 = evaluationStorage.getResultStoreFacade().get(ResultType.ACTUAL, TASK1).get();
        SerializableResult result2 = evaluationStorage.getResultStoreFacade().get(ResultType.ACTUAL, TASK2).get();
        SerializableResult result3 = evaluationStorage.getResultStoreFacade().get(ResultType.ACTUAL, TASK3).get();
        assertThat(result1.getData(), is(task1DataActual));
        assertThat(result2.getData(), is(task2DataActual));
        assertThat(result3.getData(), is(task3DataActual));
        assertThat(result1.getSentTimestamp(), is(0L));
        assertThat(result2.getSentTimestamp(), is(1L));
        assertThat(result3.getSentTimestamp(), is(2L));
    }

    @Test
    public void testIterator() {
        evaluationStorage.setMaxObjectSize(5);
        byte[] task1DataExpected = new byte[]{3, 2};
        byte[] task2DataExpected = new byte[]{29, 12, 3, 2, 89, 2, 2};
        byte[] task3DataExpected = new byte[]{29, 92, 3, 18, 39, 29, 103, 2};
        evaluationStorage.receiveExpectedResponseData(TASK1, 0, task1DataExpected);
        evaluationStorage.receiveExpectedResponseData(TASK2, 1, task2DataExpected);
        evaluationStorage.receiveExpectedResponseData(TASK3, 2, task3DataExpected);

        byte[] task1DataActual = new byte[]{0, 2};
        byte[] task2DataActual = new byte[]{92, 3, 18, 39, 29, 103, 2};
        byte[] task3DataActual = new byte[]{12, 3, 2};
        evaluationStorage.receiveResponseData(TASK1, 0, task1DataActual);
        evaluationStorage.receiveResponseData(TASK2, 1, task2DataActual);
        evaluationStorage.receiveResponseData(TASK3, 2, task3DataActual);

        Iterator<ResultPair> it = evaluationStorage.createIterator();
        ResultPair resultPair;

        resultPair = it.next();
        assertThat(resultPair.getExpected().getData(), is(task1DataExpected));
        assertThat(resultPair.getActual().getData(), is(task1DataActual));

        resultPair = it.next();
        assertThat(resultPair.getExpected().getData(), is(task2DataExpected));
        assertThat(resultPair.getActual().getData(), is(task2DataActual));

        resultPair = it.next();
        assertThat(resultPair.getExpected().getData(), is(task3DataExpected));
        assertThat(resultPair.getActual().getData(), is(task3DataActual));
    }

}

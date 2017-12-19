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

import java.io.File;
import java.util.Iterator;
import java.util.Random;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.BasicConfigurator;
import org.hobbit.core.data.ResultPair;
import org.hobbit.evaluationstorage.resultstore.FileResultStoreFacade;
import org.junit.Assert;
import org.junit.Test;

/**
 * Tests for the {@link FileResultStoreFacadeTest}.
 *
 * @author Ruben Taelman (ruben.taelman@ugent.be)
 */
public class FileResultStoreFacadeTest {

    private static final String TASK1 = "task1";
    private static final String TASK2 = "task2";
    private static final String TASK3 = "task3";

    static {
        BasicConfigurator.configure();
    }

    private Random random = new Random();

    @Test
    public void setPutExpected() throws Exception {
        FileResultStoreFacade resultStoreFacade = new FileResultStoreFacade();
        try {
            resultStoreFacade.setDefaultPath(FileUtils.getTempDirectoryPath() + File.separator + random.nextInt());
            resultStoreFacade.init();
            resultStoreFacade.run();
            SerializableResult task1Expected = new SerializableResult(0, new byte[] { 3 });
            SerializableResult task2Expected = new SerializableResult(1, new byte[] { 29, 12, 3, 2, 89, 2 });
            SerializableResult task3Expected = new SerializableResult(2, new byte[] { 29, 92, 3, 18, 39, 29, 103 });
            resultStoreFacade.put(ResultType.EXPECTED, TASK1, task1Expected);
            resultStoreFacade.put(ResultType.EXPECTED, TASK2, task2Expected);
            resultStoreFacade.put(ResultType.EXPECTED, TASK3, task3Expected);

            SerializableResult result1 = resultStoreFacade.get(ResultType.EXPECTED, TASK1).get();
            SerializableResult result2 = resultStoreFacade.get(ResultType.EXPECTED, TASK2).get();
            SerializableResult result3 = resultStoreFacade.get(ResultType.EXPECTED, TASK3).get();
            Assert.assertEquals(result1, task1Expected);
            Assert.assertEquals(result2, task2Expected);
            Assert.assertEquals(result3, task3Expected);
        } finally {
            resultStoreFacade.close();
        }
    }

    @Test
    public void testPutActual() throws Exception {
        FileResultStoreFacade resultStoreFacade = new FileResultStoreFacade();
        try {
            resultStoreFacade.setDefaultPath(FileUtils.getTempDirectoryPath() + File.separator + random.nextInt());
            resultStoreFacade.init();
            resultStoreFacade.run();
            SerializableResult task1Actual = new SerializableResult(0, new byte[] { 0 });
            SerializableResult task2Actual = new SerializableResult(1, new byte[] { 12, 3, 2, 89, 2 });
            SerializableResult task3Actual = new SerializableResult(2, new byte[] { 92, 3, 18, 39, 29, 103 });
            resultStoreFacade.put(ResultType.ACTUAL, TASK1, task1Actual);
            resultStoreFacade.put(ResultType.ACTUAL, TASK2, task2Actual);
            resultStoreFacade.put(ResultType.ACTUAL, TASK3, task3Actual);

            SerializableResult result1 = resultStoreFacade.get(ResultType.ACTUAL, TASK1).get();
            SerializableResult result2 = resultStoreFacade.get(ResultType.ACTUAL, TASK2).get();
            SerializableResult result3 = resultStoreFacade.get(ResultType.ACTUAL, TASK3).get();
            Assert.assertEquals(result1, task1Actual);
            Assert.assertEquals(result2, task2Actual);
            Assert.assertEquals(result3, task3Actual);
        } finally {
            resultStoreFacade.close();
        }
    }

    @Test
    public void testIterator() throws Exception {
        FileResultStoreFacade resultStoreFacade = new FileResultStoreFacade();
        try {
            resultStoreFacade.setDefaultPath(FileUtils.getTempDirectoryPath() + File.separator + random.nextInt());
            resultStoreFacade.init();
            resultStoreFacade.run();
            SerializableResult task1Expected = new SerializableResult(0, new byte[] { 3 });
            SerializableResult task2Expected = new SerializableResult(1, new byte[] { 29, 12, 3, 2, 89, 2 });
            SerializableResult task3Expected = new SerializableResult(2, new byte[] { 29, 92, 3, 18, 39, 29, 103 });
            resultStoreFacade.put(ResultType.EXPECTED, TASK1, task1Expected);
            resultStoreFacade.put(ResultType.EXPECTED, TASK2, task2Expected);
            resultStoreFacade.put(ResultType.EXPECTED, TASK3, task3Expected);

            SerializableResult task1Actual = new SerializableResult(0, new byte[] { 0 });
            SerializableResult task2Actual = new SerializableResult(1, new byte[] { 12, 3, 2, 89, 2 });
            SerializableResult task3Actual = new SerializableResult(2, new byte[] { 92, 3, 18, 39, 29, 103 });
            resultStoreFacade.put(ResultType.ACTUAL, TASK1, task1Actual);
            resultStoreFacade.put(ResultType.ACTUAL, TASK2, task2Actual);
            resultStoreFacade.put(ResultType.ACTUAL, TASK3, task3Actual);

            Iterator<ResultPair> it = resultStoreFacade.createIterator();
            ResultPair resultPair;

            resultPair = it.next();
            Assert.assertEquals(resultPair.getExpected(), task1Expected);
            Assert.assertEquals(resultPair.getActual(), task1Actual);

            resultPair = it.next();
            Assert.assertEquals(resultPair.getExpected(), task2Expected);
            Assert.assertEquals(resultPair.getActual(), task2Actual);

            resultPair = it.next();
            Assert.assertEquals(resultPair.getExpected(), task3Expected);
            Assert.assertEquals(resultPair.getActual(), task3Actual);
        } finally {
            resultStoreFacade.close();
        }
    }

    @Test
    public void testMissingResult() throws Exception {
        FileResultStoreFacade resultStoreFacade = new FileResultStoreFacade();
        try {
            resultStoreFacade.setDefaultPath(FileUtils.getTempDirectoryPath() + File.separator + random.nextInt());
            resultStoreFacade.init();
            resultStoreFacade.run();
            Assert.assertNull(resultStoreFacade.get(ResultType.ACTUAL, TASK1).get());
            Assert.assertNull(resultStoreFacade.get(ResultType.EXPECTED, TASK1).get());
        } finally {
            resultStoreFacade.close();
        }
    }

}

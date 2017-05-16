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

import org.apache.commons.io.FileUtils;
import org.apache.log4j.BasicConfigurator;
import org.hobbit.core.data.ResultPair;
import org.hobbit.evaluationstorage.resultstore.FileResultStoreFacade;
import org.hobbit.evaluationstorage.resultstore.RiakResultStoreFacade;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.Iterator;
import java.util.concurrent.ExecutionException;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

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

    private FileResultStoreFacade resultStoreFacade;

    @Before
    public void init() throws Exception {
        resultStoreFacade = new FileResultStoreFacade();
        resultStoreFacade.setDefaultPath(FileUtils.getTempDirectoryPath());
        resultStoreFacade.init();
        resultStoreFacade.run();
    }

    @After
    public void deinit() throws IOException {
        resultStoreFacade.close();
    }

    @Test
    public void setPutExpected() throws ExecutionException, InterruptedException {
        SerializableResult task1Expected = new SerializableResult(0, new byte[]{3});
        SerializableResult task2Expected = new SerializableResult(1, new byte[]{29, 12, 3, 2, 89, 2});
        SerializableResult task3Expected = new SerializableResult(2, new byte[]{29, 92, 3, 18, 39, 29, 103});
        resultStoreFacade.put(ResultType.EXPECTED, TASK1, task1Expected);
        resultStoreFacade.put(ResultType.EXPECTED, TASK2, task2Expected);
        resultStoreFacade.put(ResultType.EXPECTED, TASK3, task3Expected);

        SerializableResult result1 = resultStoreFacade.get(ResultType.EXPECTED, TASK1).get();
        SerializableResult result2 = resultStoreFacade.get(ResultType.EXPECTED, TASK2).get();
        SerializableResult result3 = resultStoreFacade.get(ResultType.EXPECTED, TASK3).get();
        assertThat(result1, is(task1Expected));
        assertThat(result2, is(task2Expected));
        assertThat(result3, is(task3Expected));

    }

    @Test
    public void testPutActual() throws ExecutionException, InterruptedException {
        SerializableResult task1Actual = new SerializableResult(0, new byte[]{0});
        SerializableResult task2Actual = new SerializableResult(1, new byte[]{12, 3, 2, 89, 2});
        SerializableResult task3Actual = new SerializableResult(2, new byte[]{92, 3, 18, 39, 29, 103});
        resultStoreFacade.put(ResultType.ACTUAL, TASK1, task1Actual);
        resultStoreFacade.put(ResultType.ACTUAL, TASK2, task2Actual);
        resultStoreFacade.put(ResultType.ACTUAL, TASK3, task3Actual);

        SerializableResult result1 = resultStoreFacade.get(ResultType.ACTUAL, TASK1).get();
        SerializableResult result2 = resultStoreFacade.get(ResultType.ACTUAL, TASK2).get();
        SerializableResult result3 = resultStoreFacade.get(ResultType.ACTUAL, TASK3).get();
        assertThat(result1, is(task1Actual));
        assertThat(result2, is(task2Actual));
        assertThat(result3, is(task3Actual));
    }

    @Test
    public void testIterator() {
        SerializableResult task1Expected = new SerializableResult(0, new byte[]{3});
        SerializableResult task2Expected = new SerializableResult(1, new byte[]{29, 12, 3, 2, 89, 2});
        SerializableResult task3Expected = new SerializableResult(2, new byte[]{29, 92, 3, 18, 39, 29, 103});
        resultStoreFacade.put(ResultType.EXPECTED, TASK1, task1Expected);
        resultStoreFacade.put(ResultType.EXPECTED, TASK2, task2Expected);
        resultStoreFacade.put(ResultType.EXPECTED, TASK3, task3Expected);

        SerializableResult task1Actual = new SerializableResult(0, new byte[]{0});
        SerializableResult task2Actual = new SerializableResult(1, new byte[]{12, 3, 2, 89, 2});
        SerializableResult task3Actual = new SerializableResult(2, new byte[]{92, 3, 18, 39, 29, 103});
        resultStoreFacade.put(ResultType.ACTUAL, TASK1, task1Actual);
        resultStoreFacade.put(ResultType.ACTUAL, TASK2, task2Actual);
        resultStoreFacade.put(ResultType.ACTUAL, TASK3, task3Actual);

        Iterator<ResultPair> it = resultStoreFacade.createIterator();
        ResultPair resultPair;

        resultPair = it.next();
        assertThat(resultPair.getExpected(), is(task1Expected));
        assertThat(resultPair.getActual(), is(task1Actual));

        resultPair = it.next();
        assertThat(resultPair.getExpected(), is(task2Expected));
        assertThat(resultPair.getActual(), is(task2Actual));

        resultPair = it.next();
        assertThat(resultPair.getExpected(), is(task3Expected));
        assertThat(resultPair.getActual(), is(task3Actual));
    }

}

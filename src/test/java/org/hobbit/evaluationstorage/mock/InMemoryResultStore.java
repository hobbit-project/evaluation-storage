package org.hobbit.evaluationstorage.mock;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.hobbit.core.data.ResultPair;
import org.hobbit.evaluationstorage.ResultType;
import org.hobbit.evaluationstorage.data.ResultFuture;
import org.hobbit.evaluationstorage.data.SerializableResult;
import org.hobbit.evaluationstorage.data.SerializableResultPair;
import org.hobbit.evaluationstorage.data.SimpleResultFuture;
import org.hobbit.evaluationstorage.resultstore.ResultStoreFacade;

public class InMemoryResultStore implements ResultStoreFacade {

    protected Map<String, SerializableResultPair> results = new HashMap<>();

    @Override
    public void close() throws IOException {
    }

    @Override
    public void containerStopped(String containerName, int exitCode) {
    }

    @Override
    public void init() throws Exception {
    }

    @Override
    public void put(ResultType resultType, String taskId, SerializableResult result) {
        SerializableResultPair pair = null;
        if (results.containsKey(taskId)) {
            pair = results.get(taskId);
        } else {
            pair = new SerializableResultPair(null, null);
            results.put(taskId, pair);
        }
        if (resultType == ResultType.EXPECTED) {
            pair.setExpected(result);
        } else {
            pair.setActual(result);
        }
    }

    @Override
    public ResultFuture get(ResultType resultType, String taskId) {
        SerializableResult result = null;
        if (results.containsKey(taskId)) {
            SerializableResultPair pair = results.get(taskId);
            if (resultType == ResultType.EXPECTED) {
                result = pair.getExpectedSerializable();
            } else {
                result = pair.getActualSerializable();
            }
        }
        return new SimpleResultFuture(result);
    }

    @Override
    public Iterator<? extends ResultPair> createIterator() {
        return results.values().iterator();
    }

}

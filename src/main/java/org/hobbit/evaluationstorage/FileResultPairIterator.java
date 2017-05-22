package org.hobbit.evaluationstorage;

import org.hobbit.core.data.ResultPair;
import org.hobbit.evaluationstorage.resultstore.ResultStoreFacade;

import java.util.Iterator;
import java.util.concurrent.ExecutionException;

/**
 * Created by Tim Ermilov on 16.05.17.
 */
public class FileResultPairIterator implements Iterator<ResultPair> {
    private final Iterator<String> expectedLocations;
    private final ResultStoreFacade resultRetriever;

    public FileResultPairIterator(Iterator<String> expectedLocations, ResultStoreFacade resultRetriever) {
        this.expectedLocations = expectedLocations;
        this.resultRetriever = resultRetriever;
    }

    @Override
    public boolean hasNext() {
        return expectedLocations.hasNext();
    }

    @Override
    public ResultPair next() {
        String taskId = expectedLocations.next();
        ResultFuture expected = resultRetriever.get(ResultType.EXPECTED, taskId);
        ResultFuture actual = resultRetriever.get(ResultType.ACTUAL, taskId);
        try {
            return new SerializableResultPair(expected.get(), actual.get());
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        return null;
    }
}

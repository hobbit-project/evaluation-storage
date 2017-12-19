package org.hobbit.evaluationstorage.resultstore;

import java.util.Iterator;

import org.apache.commons.io.IOUtils;
import org.hobbit.core.data.ResultPair;
import org.hobbit.evaluationstorage.ResultFuture;
import org.hobbit.evaluationstorage.ResultType;
import org.hobbit.evaluationstorage.data.SerializableResult;

public abstract class AbstractResultStoreFacadeDecorator implements ResultStoreFacadeDecorator {

    protected ResultStoreFacade decorated;

    public AbstractResultStoreFacadeDecorator(ResultStoreFacade decorated) {
        this.decorated = decorated;
    }

    @Override
    public void init() throws Exception {
        decorated.init();
    }

    @Override
    public void put(ResultType resultType, String taskId, SerializableResult result) {
        decorated.put(resultType, taskId, result);
    }

    @Override
    public ResultFuture get(ResultType resultType, String taskId) {
        return decorated.get(resultType, taskId);
    }

    @Override
    public Iterator<ResultPair> createIterator() {
        return decorated.createIterator();
    }

    @Override
    public void close() {
        IOUtils.closeQuietly(decorated);
    }

    @Override
    public void containerStopped(String containerName, int exitCode) {
        decorated.containerStopped(containerName, exitCode);
    }

    @Override
    public ResultStoreFacade getDecorated() {
        return decorated;
    }
}

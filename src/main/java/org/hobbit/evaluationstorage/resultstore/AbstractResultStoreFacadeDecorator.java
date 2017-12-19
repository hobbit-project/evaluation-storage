package org.hobbit.evaluationstorage.resultstore;

import java.util.Iterator;
import java.util.concurrent.ExecutionException;

import org.apache.commons.io.IOUtils;
import org.hobbit.core.data.Result;
import org.hobbit.core.data.ResultPair;
import org.hobbit.evaluationstorage.ResultType;
import org.hobbit.evaluationstorage.data.ResultFuture;
import org.hobbit.evaluationstorage.data.SerializableResult;
import org.hobbit.evaluationstorage.data.SerializableResultPair;
import org.hobbit.evaluationstorage.data.SimpleResultFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractResultStoreFacadeDecorator implements ResultStoreFacadeDecorator {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractResultStoreFacadeDecorator.class);

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
        ResultFuture future = decorated.get(resultType, taskId);
        SerializableResult result = null;
        try {
            result = future.get();
        } catch (InterruptedException | ExecutionException e) {
            LOGGER.error("Exception while trying to get the result. Returning null as result.", e);
        }
        return new SimpleResultFuture(handleResult(resultType, result));
    }

    protected abstract SerializableResult handleResult(ResultType resultType, Result result);

    @Override
    public Iterator<? extends ResultPair> createIterator() {
        return new IteratorDecorator(this, decorated.createIterator());
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

    /**
     * A simple iterator that decorates an iterator that is retrieved from the
     * decorated {@link ResultStoreFacade}. Every retrieved result pair that
     * this iterator gets from the decorated iterator,
     * {@link AbstractResultStoreFacadeDecorator#handleResult(ResultType, Result)}
     * of the decorator instance is called.
     * 
     * @author Michael R&ouml;der (roeder@informatik.uni-leipzig.de)
     *
     */
    protected static class IteratorDecorator implements Iterator<ResultPair> {

        protected AbstractResultStoreFacadeDecorator facadeDecorator;
        protected Iterator<? extends ResultPair> decorated;

        public IteratorDecorator(AbstractResultStoreFacadeDecorator facadeDecorator,
                Iterator<? extends ResultPair> decorated) {
            super();
            this.facadeDecorator = facadeDecorator;
            this.decorated = decorated;
        }

        @Override
        public boolean hasNext() {
            return decorated.hasNext();
        }

        @Override
        public ResultPair next() {
            ResultPair pair = decorated.next();
            if (pair != null) {
                pair = new SerializableResultPair(facadeDecorator.handleResult(ResultType.EXPECTED, pair.getExpected()),
                        facadeDecorator.handleResult(ResultType.ACTUAL, pair.getActual()));
            }
            return pair;
        }

    }

}

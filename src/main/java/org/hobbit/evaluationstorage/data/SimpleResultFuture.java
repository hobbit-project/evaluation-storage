package org.hobbit.evaluationstorage.data;

import java.util.concurrent.TimeUnit;

/**
 * Created by Tim Ermilov on 16.05.17.
 */
public class SimpleResultFuture implements ResultFuture {
    private final SerializableResult value;

    public SimpleResultFuture(SerializableResult value) {
        this.value = value;
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        return false;
    }

    @Override
    public boolean isCancelled() {
        return false;
    }

    @Override
    public boolean isDone() {
        return true;
    }

    @Override
    public SerializableResult get() throws InterruptedException {
        return value;
    }

    @Override
    public SerializableResult get(long timeout, TimeUnit unit) throws InterruptedException {
        return value;
    }
}

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

import com.basho.riak.client.api.cap.UnresolvedConflictException;
import com.basho.riak.client.api.commands.kv.FetchValue;
import com.basho.riak.client.core.RiakFuture;
import com.basho.riak.client.core.query.Location;
import com.basho.riak.client.core.query.RiakObject;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Promise for Riak results.
 *
 * @author Ruben Taelman (ruben.taelman@ugent.be)
 */
public class RiakResultFuture implements ResultFuture {

    private static final Logger LOGGER = LoggerFactory.getLogger(RiakResultFuture.class);

    private final RiakFuture<FetchValue.Response, Location> riakFuture;

    public RiakResultFuture(RiakFuture<FetchValue.Response, Location> riakFuture) {
        this.riakFuture = riakFuture;
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        return riakFuture.cancel(mayInterruptIfRunning);
    }

    @Override
    public boolean isCancelled() {
        return riakFuture.isCancelled();
    }

    @Override
    public boolean isDone() {
        return riakFuture.isDone();
    }

    protected static SerializableResult responseToResult(FetchValue.Response response) {
        try {
            if (response.hasValues()) {
                return SerializableResult.deserialize(response.getValue(RiakObject.class).getValue().getValue());
            } else {
                LOGGER.info("Got a response without a value. Returning null.");
            }
        } catch (Exception e) {
            LOGGER.error("Got an exception while trying to deserialize the result from Riak. Returning null.", e);
        }
        return null;
    }

    @Override
    public SerializableResult get() throws InterruptedException, ExecutionException {
        return responseToResult(riakFuture.get());
    }

    @Override
    public SerializableResult get(long timeout, TimeUnit unit)
            throws InterruptedException, ExecutionException, TimeoutException {
        return responseToResult(riakFuture.get(timeout, unit));
    }
}

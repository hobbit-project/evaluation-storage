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

import com.basho.riak.client.core.query.Location;
import org.hobbit.core.data.ResultPair;
import org.hobbit.evaluationstorage.data.ResultFuture;
import org.hobbit.evaluationstorage.data.SerializableResultPair;
import org.hobbit.evaluationstorage.resultstore.ResultStoreFacade;

import java.util.Iterator;
import java.util.concurrent.ExecutionException;

/**
 * Iterates over result pairs.
 *
 * @author Ruben Taelman (ruben.taelman@ugent.be)
 */
public class ResultPairIterator implements Iterator<ResultPair> {

    private final Iterator<Location> expectedLocations;
    private final ResultStoreFacade resultRetriever;

    public ResultPairIterator(Iterator<Location> expectedLocations, ResultStoreFacade resultRetriever) {
        this.expectedLocations = expectedLocations;
        this.resultRetriever = resultRetriever;
    }

    @Override
    public boolean hasNext() {
        return expectedLocations.hasNext();
    }

    @Override
    public ResultPair next() {
        Location location = expectedLocations.next();
        String taskId = location.getKeyAsString();
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

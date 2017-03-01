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
package org.hobbit.evaluationstorage.resultstore;

import org.hobbit.core.components.Component;
import org.hobbit.core.components.ContainerStateObserver;
import org.hobbit.core.data.ResultPair;
import org.hobbit.evaluationstorage.ResultFuture;
import org.hobbit.evaluationstorage.ResultType;
import org.hobbit.evaluationstorage.SerializableResult;

import java.util.Iterator;

/**
 * Facade for result stores.
 *
 * @author Ruben Taelman (ruben.taelman@ugent.be)
 */
public interface ResultStoreFacade extends Component, ContainerStateObserver {

    /**
     * Insert a result.
     * @param resultType The type of result.
     * @param taskId The unique task id of the result, scoped by the resultType.
     * @param result The result that needs to be stored.
     */
    public void put(ResultType resultType, String taskId, SerializableResult result);

    /**
     * Retrieve a result.
     * @param resultType The type of result.
     * @param taskId The unique task id of the result, scoped by the resultType.
     * @return The result that was stored, or null.
     */
    public ResultFuture get(ResultType resultType, String taskId);

    /**
     * The iterator will loop over result pairs (expected and actual),
     * in the order of expected result insertion.
     * @return A new iterator that will loop over all the result pairs.
     */
    public Iterator<ResultPair> createIterator();

}

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
package org.hobbit.evaluationstorage.data;

import org.hobbit.core.data.Result;
import org.hobbit.core.data.ResultPair;

/**
 * Data holder for expected and actual data.
 *
 * @author Ruben Taelman (ruben.taelman@ugent.be)
 */
public class SerializableResultPair implements ResultPair {

    private SerializableResult expected;
    private SerializableResult actual;

    public SerializableResultPair(SerializableResult expected, SerializableResult actual) {
        this.expected = expected;
        this.actual = actual;
    }

    @Override
    public Result getExpected() {
        return expected;
    }
    
    public SerializableResult getExpectedSerializable() {
        return expected;
    }

    @Override
    public Result getActual() {
        return actual;
    }
    
    public SerializableResult getActualSerializable() {
        return actual;
    }
    
    public void setActual(SerializableResult actual) {
        this.actual = actual;
    }
    
    public void setExpected(SerializableResult expected) {
        this.expected = expected;
    }
}

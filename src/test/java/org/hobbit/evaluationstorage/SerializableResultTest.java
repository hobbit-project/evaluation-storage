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

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;

/**
 * Tests for the result data holder.
 *
 * @author Ruben Taelman (ruben.taelman@ugent.be)
 */
public class SerializableResultTest {

    @Test
    public void testEquals() {
        SerializableResult resultA = new SerializableResult(100, new byte[]{0, 67, 27});
        SerializableResult resultB = new SerializableResult(100, new byte[]{0, 67, 27});
        SerializableResult resultC = new SerializableResult(101, new byte[]{0, 67, 27});
        SerializableResult resultD = new SerializableResult(100, new byte[]{0, 67, 27, 83});
        SerializableResult resultE = new SerializableResult(102, new byte[]{0, 67, 27, 83});

        assertThat(resultA, is(resultA));
        assertThat(resultA, is(resultB));
        assertThat(resultA, not(resultC));
        assertThat(resultA, not(resultD));
        assertThat(resultA, not(resultE));
    }

    @Test
    public void testSerialization() {
        SerializableResult resultIn = new SerializableResult(100, new byte[]{0, 67, 27});

        byte[] serialized = resultIn.serialize();
        SerializableResult resultOut = SerializableResult.deserialize(serialized);

        assertThat(resultIn, is(resultOut));
    }

}

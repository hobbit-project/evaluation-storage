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

import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * Data holder for a result.
 *
 * @author Ruben Taelman (ruben.taelman@ugent.be)
 */
public class SerializableResult implements Result {

    private static final int LONG_SIZE = Long.SIZE / Byte.SIZE;

    private final long sentTimestamp;
    private final ResultValueType valueType;
    private byte[] data;

    public SerializableResult(Result result) {
        this(result != null ? result.getSentTimestamp() : 0, ResultValueType.RESULT_DATA,
                result != null ? result.getData() : null);
    }

    public SerializableResult(long sentTimestamp, byte[] data) {
        this(sentTimestamp, ResultValueType.RESULT_DATA, data);
    }

    public SerializableResult(long sentTimestamp, ResultValueType valueType, byte[] data) {
        this.sentTimestamp = sentTimestamp;
        this.valueType = valueType;
        this.data = data;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof SerializableResult) {
            SerializableResult other = (SerializableResult) obj;
            return sentTimestamp == other.sentTimestamp && Arrays.equals(data, other.data);
        }
        return false;
    }

    @Override
    public String toString() {
        return String.format("Result@%s (%s) [%s]", sentTimestamp, valueType.toString(), Arrays.toString(data));
    }

    @Override
    public long getSentTimestamp() {
        return sentTimestamp;
    }

    @Override
    public byte[] getData() {
        return data;
    }

    public ResultValueType getValueType() {
        return valueType;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public byte[] serialize() {
        return ByteBuffer.allocate(LONG_SIZE + 1 + data.length).putLong(sentTimestamp).put(valueType.toByte()).put(data)
                .array();
    }

    public static SerializableResult deserialize(byte[] serializedData) {
        long sentTimestamp = ByteBuffer.wrap(serializedData, 0, LONG_SIZE).getLong();
        ResultValueType valueType = ResultValueType.fromByte(serializedData[LONG_SIZE]);
        byte[] data = Arrays.copyOfRange(serializedData, LONG_SIZE + 1, serializedData.length);
        return new SerializableResult(sentTimestamp, valueType, data);
    }
}

package org.hobbit.evaluationstorage.data;

public enum ResultValueType {
    /**
     * This result contains the result data.
     */
    RESULT_DATA,
    /**
     * This result contains not the data but a reference to a file that contains the
     * data.
     */
    FILE_REF;

    public byte toByte() {
        return (byte) ordinal();
    }

    public static ResultValueType fromByte(byte b) {
        return ResultValueType.values()[b];
    }
}

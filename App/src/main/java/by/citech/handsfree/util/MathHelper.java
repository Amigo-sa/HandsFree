package by.citech.handsfree.util;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class MathHelper {

    public static int packBigEndian(byte[] b) {
        return (b[0] & 0xFF) << 24
             | (b[1] & 0xFF) << 16
             | (b[2] & 0xFF) <<  8
             | (b[3] & 0xFF) <<  0;
    }

    public static int packLittleEndian(byte[] b) {
        return (b[0] & 0xFF) <<  0
             | (b[1] & 0xFF) <<  8
             | (b[2] & 0xFF) << 16
             | (b[3] & 0xFF) << 24;
    }

    public static byte[] unpackBigEndian(int x) {
        return new byte[]{
                (byte) (x >>> 24),
                (byte) (x >>> 16),
                (byte) (x >>>  8),
                (byte) (x >>>  0)
        };
    }

    public static byte[] unpackLittleEndian(int x) {
        return new byte[]{
                (byte) (x >>>  0),
                (byte) (x >>>  8),
                (byte) (x >>> 16),
                (byte) (x >>> 24)
        };
    }

    public static int convertByteArrToIntRaw(byte[] b) {
        if (b.length == 4)
            return (b[0] & 0xFF) << 24
                 | (b[1] & 0xFF) << 16
                 | (b[2] & 0xFF) <<  8
                 | (b[3] & 0xFF) <<  0;
        else if (b.length == 2)
            return (b[0] & 0xff) <<  8
                 | (b[1] & 0xff) <<  0;
        else
            return 0;
    }

    public static byte[] convertIntToByteArrByteBuffer(int value) {
        return ByteBuffer.allocate(4).putInt(value).array();
    }

    public static int convertByteArrToIntByteBuffer(byte[] bytes) {
        return ByteBuffer.wrap(bytes, 0, 4).getInt();
    }

    //------------------ long-byte

    public static long convertByteArrToLongByteBuffer(byte[] bytes) {
        return ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).getInt() & 0xFFFFFFFFL;
    }

    public static byte[] convertLongToByteArrByteBuffer(long value) {
        byte [] data = new byte[4];
        data[3] = (byte) value;
        data[2] = (byte) (value >>> 8);
        data[1] = (byte) (value >>> 16);
        data[0] = (byte) (value >>> 32);
        return data;
    }

}

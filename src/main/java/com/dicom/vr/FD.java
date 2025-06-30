package com.dicom.vr;

/**
 * FD (Floating Point Double) VR类
 * 表示64位双精度浮点数
 * 长度：8字节
 */
public class FD extends VRBase {
    
    private static FD instanceLE = null;
    private static FD instanceBE = null;
    
    private FD(boolean isBE) {
        super(isBE, false);
    }
    
    public static FD getInstance(boolean isBE) {
        if (isBE) {
            if (instanceBE == null) {
                instanceBE = new FD(true);
            }
            return instanceBE;
        } else {
            if (instanceLE == null) {
                instanceLE = new FD(false);
            }
            return instanceLE;
        }
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public <T> T GetValue(byte[] data, int startIndex) {
        if (data == null || startIndex < 0 || startIndex + 8 > data.length) {
            return null;
        }
        
        long longBits;
        if (isBE) {
            longBits = ((long)(data[startIndex] & 0xFF) << 56) |
                      ((long)(data[startIndex + 1] & 0xFF) << 48) |
                      ((long)(data[startIndex + 2] & 0xFF) << 40) |
                      ((long)(data[startIndex + 3] & 0xFF) << 32) |
                      ((long)(data[startIndex + 4] & 0xFF) << 24) |
                      ((long)(data[startIndex + 5] & 0xFF) << 16) |
                      ((long)(data[startIndex + 6] & 0xFF) << 8) |
                      ((long)(data[startIndex + 7] & 0xFF));
        } else {
            longBits = ((long)(data[startIndex + 7] & 0xFF) << 56) |
                      ((long)(data[startIndex + 6] & 0xFF) << 48) |
                      ((long)(data[startIndex + 5] & 0xFF) << 40) |
                      ((long)(data[startIndex + 4] & 0xFF) << 32) |
                      ((long)(data[startIndex + 3] & 0xFF) << 24) |
                      ((long)(data[startIndex + 2] & 0xFF) << 16) |
                      ((long)(data[startIndex + 1] & 0xFF) << 8) |
                      ((long)(data[startIndex] & 0xFF));        }
        
        double value = Double.longBitsToDouble(longBits);
        return (T) Double.valueOf(value);
    }
    
    @Override
    public byte[] SetValue(Object value) {
        if (value == null) {
            return new byte[8]; // Return 8 bytes filled with zeros
        }
        
        double doubleValue;
        if (value instanceof Number) {
            doubleValue = ((Number) value).doubleValue();
        } else {
            try {
                doubleValue = Double.parseDouble(value.toString());
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("FD value must be a valid double: " + value);
            }
        }
        
        long longBits = Double.doubleToLongBits(doubleValue);
        byte[] result = new byte[8];
        
        if (isBE) {
            // Big-endian: most significant byte first
            result[0] = (byte) ((longBits >> 56) & 0xFF);
            result[1] = (byte) ((longBits >> 48) & 0xFF);
            result[2] = (byte) ((longBits >> 40) & 0xFF);
            result[3] = (byte) ((longBits >> 32) & 0xFF);
            result[4] = (byte) ((longBits >> 24) & 0xFF);
            result[5] = (byte) ((longBits >> 16) & 0xFF);
            result[6] = (byte) ((longBits >> 8) & 0xFF);
            result[7] = (byte) (longBits & 0xFF);
        } else {
            // Little-endian: least significant byte first
            result[0] = (byte) (longBits & 0xFF);
            result[1] = (byte) ((longBits >> 8) & 0xFF);
            result[2] = (byte) ((longBits >> 16) & 0xFF);
            result[3] = (byte) ((longBits >> 24) & 0xFF);
            result[4] = (byte) ((longBits >> 32) & 0xFF);
            result[5] = (byte) ((longBits >> 40) & 0xFF);
            result[6] = (byte) ((longBits >> 48) & 0xFF);
            result[7] = (byte) ((longBits >> 56) & 0xFF);
        }
        
        return result;
    }
}

package com.dicom.vr;

/**
 * OD (Other Double String) VR类
 * 其他双精度浮点数字符串
 * 长度：可变，长VR类型
 */
public class OD extends VRBase {
    
    private static OD instanceLE = null;
    private static OD instanceBE = null;
    
    private OD(boolean isBE) {
        super(isBE, true);  // OD是长VR类型
    }
    
    public static OD getInstance(boolean isBE) {
        if (isBE) {
            if (instanceBE == null) {
                instanceBE = new OD(true);
            }
            return instanceBE;
        } else {
            if (instanceLE == null) {
                instanceLE = new OD(false);
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
        
        // 与FD类似，解析双精度浮点数
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
            return new byte[0];
        }
        
        double doubleValue;
        if (value instanceof Double) {
            doubleValue = (Double) value;
        } else if (value instanceof Float) {
            doubleValue = ((Float) value).doubleValue();
        } else if (value instanceof Integer) {
            doubleValue = ((Integer) value).doubleValue();
        } else if (value instanceof Long) {
            doubleValue = ((Long) value).doubleValue();
        } else if (value instanceof String) {
            try {
                doubleValue = Double.parseDouble((String) value);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Invalid OD value: " + value);
            }
        } else {
            throw new IllegalArgumentException("OD value must be numeric or String, got: " + value.getClass().getSimpleName());
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

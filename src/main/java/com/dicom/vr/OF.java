package com.dicom.vr;

/**
 * OF (Other Float String) VR类
 * 其他单精度浮点数字符串
 * 长度：可变，长VR类型
 */
public class OF extends VRBase {
    
    private static OF instanceLE = null;
    private static OF instanceBE = null;
    
    private OF(boolean isBE) {
        super(isBE, true);  // OF是长VR类型
    }
    
    public static OF getInstance(boolean isBE) {
        if (isBE) {
            if (instanceBE == null) {
                instanceBE = new OF(true);
            }
            return instanceBE;
        } else {
            if (instanceLE == null) {
                instanceLE = new OF(false);
            }
            return instanceLE;
        }
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public <T> T GetValue(byte[] data, int startIndex) {
        if (data == null || startIndex < 0 || startIndex + 4 > data.length) {
            return null;
        }
        
        // 与FL类似，解析单精度浮点数
        int intBits;
        if (isBE) {
            intBits = ((data[startIndex] & 0xFF) << 24) |
                     ((data[startIndex + 1] & 0xFF) << 16) |
                     ((data[startIndex + 2] & 0xFF) << 8) |
                     (data[startIndex + 3] & 0xFF);
        } else {        intBits = ((data[startIndex + 3] & 0xFF) << 24) |
                     ((data[startIndex + 2] & 0xFF) << 16) |
                     ((data[startIndex + 1] & 0xFF) << 8) |
                     (data[startIndex] & 0xFF);
        }
        
        float value = Float.intBitsToFloat(intBits);
        return (T) Float.valueOf(value);
    }
    
    @Override
    public <T> byte[] SetValue(T value) {
        if (value == null) {
            return new byte[0];
        }
        
        float floatValue;
        if (value instanceof Float) {
            floatValue = (Float) value;
        } else if (value instanceof Double) {
            floatValue = ((Double) value).floatValue();
        } else if (value instanceof Number) {
            floatValue = ((Number) value).floatValue();
        } else {
            try {
                floatValue = Float.parseFloat(value.toString());
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Cannot convert to float: " + value);
            }
        }
        
        // Convert float to int bits
        int intBits = Float.floatToIntBits(floatValue);
        
        byte[] result = new byte[4];
        if (isBE) {
            // Big-endian: most significant byte first
            result[0] = (byte) ((intBits >> 24) & 0xFF);
            result[1] = (byte) ((intBits >> 16) & 0xFF);
            result[2] = (byte) ((intBits >> 8) & 0xFF);
            result[3] = (byte) (intBits & 0xFF);
        } else {
            // Little-endian: least significant byte first
            result[0] = (byte) (intBits & 0xFF);
            result[1] = (byte) ((intBits >> 8) & 0xFF);
            result[2] = (byte) ((intBits >> 16) & 0xFF);
            result[3] = (byte) ((intBits >> 24) & 0xFF);
        }
        
        return result;
    }
}

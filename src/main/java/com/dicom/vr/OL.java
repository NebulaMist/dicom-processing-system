package com.dicom.vr;

/**
 * OL (Other Long String) VR类
 * 其他长整数字符串
 * 长度：可变，长VR类型
 */
public class OL extends VRBase {
    
    private static OL instanceLE = null;
    private static OL instanceBE = null;
    
    private OL(boolean isBE) {
        super(isBE, true);  // OL是长VR类型
    }
    
    public static OL getInstance(boolean isBE) {
        if (isBE) {
            if (instanceBE == null) {
                instanceBE = new OL(true);
            }
            return instanceBE;
        } else {
            if (instanceLE == null) {
                instanceLE = new OL(false);
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
        
        // 与UL类似，解析32位无符号长整数
        long value;
        if (isBE) {
            value = ((long)(data[startIndex] & 0xFF) << 24) |
                   ((long)(data[startIndex + 1] & 0xFF) << 16) |
                   ((long)(data[startIndex + 2] & 0xFF) << 8) |
                   ((long)(data[startIndex + 3] & 0xFF));
        } else {
            value = ((long)(data[startIndex + 3] & 0xFF) << 24) |
                   ((long)(data[startIndex + 2] & 0xFF) << 16) |
                   ((long)(data[startIndex + 1] & 0xFF) << 8) |
                   ((long)(data[startIndex] & 0xFF));        }
        
        return (T) Long.valueOf(value);
    }
    
    @Override
    public byte[] SetValue(Object value) {
        if (value == null) {
            return new byte[0];
        }
        
        long longValue;
        if (value instanceof Long) {
            longValue = (Long) value;
        } else if (value instanceof Integer) {
            longValue = ((Integer) value).longValue();
        } else if (value instanceof String) {
            try {
                longValue = Long.parseLong((String) value);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Invalid OL value: " + value);
            }
        } else {
            throw new IllegalArgumentException("OL value must be Long, Integer, or String, got: " + value.getClass().getSimpleName());
        }
        
        // Validate range for 32-bit unsigned integer
        if (longValue < 0 || longValue > 0xFFFFFFFFL) {
            throw new IllegalArgumentException("OL value out of range [0, 4294967295]: " + longValue);
        }
        
        byte[] result = new byte[4];
        if (isBE) {
            // Big-endian: most significant byte first
            result[0] = (byte) ((longValue >> 24) & 0xFF);
            result[1] = (byte) ((longValue >> 16) & 0xFF);
            result[2] = (byte) ((longValue >> 8) & 0xFF);
            result[3] = (byte) (longValue & 0xFF);
        } else {
            // Little-endian: least significant byte first
            result[0] = (byte) (longValue & 0xFF);
            result[1] = (byte) ((longValue >> 8) & 0xFF);
            result[2] = (byte) ((longValue >> 16) & 0xFF);
            result[3] = (byte) ((longValue >> 24) & 0xFF);
        }
        
        return result;
    }
}

package com.dicom.vr;

/**
 * SL (Signed Long) VR类
 * 表示32位有符号长整型
 * 长度：4字节
 */
public class SL extends VRBase {
    
    private static SL instanceLE = null;
    private static SL instanceBE = null;
    
    private SL(boolean isBE) {
        super(isBE, false);
    }
    
    public static SL getInstance(boolean isBE) {
        if (isBE) {
            if (instanceBE == null) {
                instanceBE = new SL(true);
            }
            return instanceBE;
        } else {
            if (instanceLE == null) {
                instanceLE = new SL(false);
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
        
        int value;
        if (isBE) {
            value = ((data[startIndex] & 0xFF) << 24) |
                   ((data[startIndex + 1] & 0xFF) << 16) |
                   ((data[startIndex + 2] & 0xFF) << 8) |
                   (data[startIndex + 3] & 0xFF);
        } else {
            value = ((data[startIndex + 3] & 0xFF) << 24) |
                   ((data[startIndex + 2] & 0xFF) << 16) |
                   ((data[startIndex + 1] & 0xFF) << 8) |
                   (data[startIndex] & 0xFF);
        }
        
        return (T) Integer.valueOf(value);
    }
    
    @Override
    public byte[] SetValue(Object value) {
        if (value == null) {
            return new byte[0];
        }
        
        int intValue;
        if (value instanceof Integer) {
            intValue = (Integer) value;
        } else if (value instanceof Long) {
            long longValue = (Long) value;
            if (longValue < Integer.MIN_VALUE || longValue > Integer.MAX_VALUE) {
                throw new IllegalArgumentException("SL值超出范围: " + longValue);
            }
            intValue = (int) longValue;
        } else {
            throw new IllegalArgumentException("SL VR只支持Integer或Long类型的值");
        }
        
        byte[] bytes = new byte[4];
        if (isBE) {
            bytes[0] = (byte) ((intValue >> 24) & 0xFF);
            bytes[1] = (byte) ((intValue >> 16) & 0xFF);
            bytes[2] = (byte) ((intValue >> 8) & 0xFF);
            bytes[3] = (byte) (intValue & 0xFF);
        } else {
            bytes[0] = (byte) (intValue & 0xFF);
            bytes[1] = (byte) ((intValue >> 8) & 0xFF);
            bytes[2] = (byte) ((intValue >> 16) & 0xFF);
            bytes[3] = (byte) ((intValue >> 24) & 0xFF);
        }
        
        return bytes;
    }
}

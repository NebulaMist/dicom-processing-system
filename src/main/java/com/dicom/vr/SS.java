package com.dicom.vr;

/**
 * SS (Signed Short) VR类
 * 表示16位有符号短整型
 * 长度：2字节
 */
public class SS extends VRBase {
    
    private static SS instanceLE = null;
    private static SS instanceBE = null;
    
    private SS(boolean isBE) {
        super(isBE, false);
    }
    
    public static SS getInstance(boolean isBE) {
        if (isBE) {
            if (instanceBE == null) {
                instanceBE = new SS(true);
            }
            return instanceBE;
        } else {
            if (instanceLE == null) {
                instanceLE = new SS(false);
            }
            return instanceLE;
        }
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public <T> T GetValue(byte[] data, int startIndex) {
        if (data == null || startIndex < 0 || startIndex + 2 > data.length) {
            return null;
        }
        
        short value;
        if (isBE) {
            value = (short)(((data[startIndex] & 0xFF) << 8) |
                           (data[startIndex + 1] & 0xFF));
        } else {
            value = (short)(((data[startIndex + 1] & 0xFF) << 8) |
                           (data[startIndex] & 0xFF));
        }
        
        return (T) Short.valueOf(value);
    }
    
    @Override
    public byte[] SetValue(Object value) {
        if (value == null) {
            return new byte[0];
        }
        
        short shortValue;
        if (value instanceof Short) {
            shortValue = (Short) value;
        } else if (value instanceof Integer) {
            int intValue = (Integer) value;
            if (intValue < Short.MIN_VALUE || intValue > Short.MAX_VALUE) {
                throw new IllegalArgumentException("SS值超出范围: " + intValue);
            }
            shortValue = (short) intValue;
        } else {
            throw new IllegalArgumentException("SS VR只支持Short或Integer类型的值");
        }
        
        byte[] bytes = new byte[2];
        if (isBE) {
            bytes[0] = (byte) ((shortValue >> 8) & 0xFF);
            bytes[1] = (byte) (shortValue & 0xFF);
        } else {
            bytes[0] = (byte) (shortValue & 0xFF);
            bytes[1] = (byte) ((shortValue >> 8) & 0xFF);
        }
        
        return bytes;
    }
}

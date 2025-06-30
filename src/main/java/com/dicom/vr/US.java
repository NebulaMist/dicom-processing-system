package com.dicom.vr;

/**
 * US (Unsigned Short) VR类
 * 表示16位无符号短整型
 * 长度：2字节
 */
public class US extends VRBase {
    
    private static US instanceLE = null;
    private static US instanceBE = null;
    
    private US(boolean isBE) {
        super(isBE, false);
    }
    
    public static US getInstance(boolean isBE) {
        if (isBE) {
            if (instanceBE == null) {
                instanceBE = new US(true);
            }
            return instanceBE;
        } else {
            if (instanceLE == null) {
                instanceLE = new US(false);
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
        
        int value;
        if (isBE) {
            value = ((data[startIndex] & 0xFF) << 8) |
                   (data[startIndex + 1] & 0xFF);
        } else {
            value = ((data[startIndex + 1] & 0xFF) << 8) |
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
        } else if (value instanceof Short) {
            intValue = ((Short) value) & 0xFFFF; // 转换为无符号
        } else {
            throw new IllegalArgumentException("US VR只支持Integer或Short类型的值");
        }
        
        // 确保值在无符号短整型范围内
        if (intValue < 0 || intValue > 0xFFFF) {
            throw new IllegalArgumentException("US值必须在0-65535范围内: " + intValue);
        }
        
        byte[] bytes = new byte[2];
        if (isBE) {
            bytes[0] = (byte) ((intValue >> 8) & 0xFF);
            bytes[1] = (byte) (intValue & 0xFF);
        } else {
            bytes[0] = (byte) (intValue & 0xFF);
            bytes[1] = (byte) ((intValue >> 8) & 0xFF);
        }
        
        return bytes;
    }
}

package com.dicom.vr;

/**
 * FL (Floating Point Single) VR类
 * 表示32位单精度浮点数
 * 长度：4字节
 */
public class FL extends VRBase {
    
    private static FL instanceLE = null;
    private static FL instanceBE = null;
    
    private FL(boolean isBE) {
        super(isBE, false);
    }
    
    public static FL getInstance(boolean isBE) {
        if (isBE) {
            if (instanceBE == null) {
                instanceBE = new FL(true);
            }
            return instanceBE;
        } else {
            if (instanceLE == null) {
                instanceLE = new FL(false);
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
        
        int intBits;
        if (isBE) {
            intBits = ((data[startIndex] & 0xFF) << 24) |
                     ((data[startIndex + 1] & 0xFF) << 16) |
                     ((data[startIndex + 2] & 0xFF) << 8) |
                     (data[startIndex + 3] & 0xFF);
        } else {
            intBits = ((data[startIndex + 3] & 0xFF) << 24) |
                     ((data[startIndex + 2] & 0xFF) << 16) |
                     ((data[startIndex + 1] & 0xFF) << 8) |
                     (data[startIndex] & 0xFF);
        }
        
        float value = Float.intBitsToFloat(intBits);
        return (T) Float.valueOf(value);
    }
    
    @Override
    public byte[] SetValue(Object value) {
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
            throw new IllegalArgumentException("FL VR只支持Float、Double或Number类型的值");
        }
        
        int intBits = Float.floatToIntBits(floatValue);
        byte[] bytes = new byte[4];
        
        if (isBE) {
            bytes[0] = (byte) ((intBits >> 24) & 0xFF);
            bytes[1] = (byte) ((intBits >> 16) & 0xFF);
            bytes[2] = (byte) ((intBits >> 8) & 0xFF);
            bytes[3] = (byte) (intBits & 0xFF);
        } else {
            bytes[0] = (byte) (intBits & 0xFF);
            bytes[1] = (byte) ((intBits >> 8) & 0xFF);
            bytes[2] = (byte) ((intBits >> 16) & 0xFF);
            bytes[3] = (byte) ((intBits >> 24) & 0xFF);
        }
        
        return bytes;
    }
}

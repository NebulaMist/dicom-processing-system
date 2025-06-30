package com.dicom.vr;

/**
 * LT (Long Text) VR类
 * 长文本，最多10240字符的文本
 * 长度：可变，偶数长度
 */
public class LT extends VRBase {
    
    private static LT instanceLE = null;
    private static LT instanceBE = null;
    
    private LT(boolean isBE) {
        super(isBE, false);
    }
    
    public static LT getInstance(boolean isBE) {
        if (isBE) {
            if (instanceBE == null) {
                instanceBE = new LT(true);
            }
            return instanceBE;
        } else {
            if (instanceLE == null) {
                instanceLE = new LT(false);
            }
            return instanceLE;
        }
    }
      @Override
    @SuppressWarnings("unchecked")
    public <T> T GetValue(byte[] data, int startIndex) {
        if (data == null || startIndex < 0 || startIndex >= data.length) {
            return null;
        }
        
        int endIndex = startIndex;
        while (endIndex < data.length && data[endIndex] != 0) {
            endIndex++;
        }
        
        String value = new String(data, startIndex, endIndex - startIndex).trim();
        return (T) value;
    }
    
    @Override
    public byte[] SetValue(Object value) {
        if (value == null) {
            return new byte[0];
        }
        
        String str = value.toString();
        
        // LT should be maximum 10240 characters
        if (str.length() > 10240) {
            throw new IllegalArgumentException("LT value exceeds maximum length of 10240 characters: " + str.length());
        }
        
        // Convert to bytes using UTF-8 encoding
        byte[] data = str.getBytes();
        
        // Ensure even length by padding with space if necessary
        if (data.length % 2 != 0) {
            byte[] paddedData = new byte[data.length + 1];
            System.arraycopy(data, 0, paddedData, 0, data.length);
            paddedData[data.length] = 0x20; // Space character
            return paddedData;
        }
        
        return data;
    }
}

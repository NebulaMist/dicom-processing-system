package com.dicom.vr;

/**
 * ST (Short Text) VR类
 * 短文本，最多1024字符的文本
 * 长度：可变，偶数长度
 */
public class ST extends VRBase {
    
    private static ST instanceLE = null;
    private static ST instanceBE = null;
    
    private ST(boolean isBE) {
        super(isBE, false);
    }
    
    public static ST getInstance(boolean isBE) {
        if (isBE) {
            if (instanceBE == null) {
                instanceBE = new ST(true);
            }
            return instanceBE;
        } else {
            if (instanceLE == null) {
                instanceLE = new ST(false);
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
        
        // ST should be maximum 1024 characters
        if (str.length() > 1024) {
            throw new IllegalArgumentException("ST value exceeds maximum length of 1024 characters: " + str.length());
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

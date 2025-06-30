package com.dicom.vr;

/**
 * CS (Code String) VR类
 * 代码字符串，最多16字符的字符串
 * 长度：可变，偶数长度
 */
public class CS extends VRBase {
    
    private static CS instanceLE = null;
    private static CS instanceBE = null;
    
    private CS(boolean isBE) {
        super(isBE, false);
    }
    
    public static CS getInstance(boolean isBE) {
        if (isBE) {
            if (instanceBE == null) {
                instanceBE = new CS(true);
            }
            return instanceBE;
        } else {
            if (instanceLE == null) {
                instanceLE = new CS(false);
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
        
        String str = value.toString().trim();
        
        // CS should be maximum 16 characters
        if (str.length() > 16) {
            throw new IllegalArgumentException("CS value exceeds maximum length of 16 characters: " + str.length());
        }
        
        // Convert to bytes using ASCII encoding
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

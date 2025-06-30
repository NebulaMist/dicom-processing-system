package com.dicom.vr;

/**
 * PN (Person Name) VR类
 * 人名，最多64字符的人名字符串
 * 长度：可变，偶数长度
 */
public class PN extends VRBase {
    
    private static PN instanceLE = null;
    private static PN instanceBE = null;
    
    private PN(boolean isBE) {
        super(isBE, false);
    }
    
    public static PN getInstance(boolean isBE) {
        if (isBE) {
            if (instanceBE == null) {
                instanceBE = new PN(true);
            }
            return instanceBE;
        } else {
            if (instanceLE == null) {
                instanceLE = new PN(false);
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
        
        // PN should be maximum 64 characters
        if (str.length() > 64) {
            throw new IllegalArgumentException("PN value exceeds maximum length of 64 characters: " + str.length());
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

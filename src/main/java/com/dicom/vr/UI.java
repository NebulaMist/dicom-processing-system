package com.dicom.vr;

/**
 * UI (Unique Identifier) VR类
 * 唯一标识符，最多64字符的UID字符串
 * 长度：可变，偶数长度
 */
public class UI extends VRBase {
    
    private static UI instanceLE = null;
    private static UI instanceBE = null;
    
    private UI(boolean isBE) {
        super(isBE, false);
    }
    
    public static UI getInstance(boolean isBE) {
        if (isBE) {
            if (instanceBE == null) {
                instanceBE = new UI(true);
            }
            return instanceBE;
        } else {
            if (instanceLE == null) {
                instanceLE = new UI(false);
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
        
        // UI should be maximum 64 characters
        if (str.length() > 64) {
            throw new IllegalArgumentException("UI value exceeds maximum length of 64 characters: " + str.length());
        }
        
        // UID should only contain digits and dots
        if (!str.matches("[0-9.]+")) {
            throw new IllegalArgumentException("UI value must only contain digits and dots: " + str);
        }
        
        // Convert to bytes using ASCII encoding
        byte[] data = str.getBytes();
        
        // Ensure even length by padding with null byte if necessary
        if (data.length % 2 != 0) {
            byte[] paddedData = new byte[data.length + 1];
            System.arraycopy(data, 0, paddedData, 0, data.length);
            paddedData[data.length] = 0x00; // Null byte for UID
            return paddedData;
        }
        
        return data;
    }
}

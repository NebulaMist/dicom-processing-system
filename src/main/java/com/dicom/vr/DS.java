package com.dicom.vr;

/**
 * DS (Decimal String) VR类
 * 十进制字符串，最多16字符的数字字符串
 * 长度：可变，偶数长度
 */
public class DS extends VRBase {
    
    private static DS instanceLE = null;
    private static DS instanceBE = null;
    
    private DS(boolean isBE) {
        super(isBE, false);
    }
    
    public static DS getInstance(boolean isBE) {
        if (isBE) {
            if (instanceBE == null) {
                instanceBE = new DS(true);
            }
            return instanceBE;
        } else {
            if (instanceLE == null) {
                instanceLE = new DS(false);
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
          String stringValue = new String(data, startIndex, endIndex - startIndex).trim();
        
        // Try to convert to Double
        try {
            Double value = Double.valueOf(stringValue);
            return (T) value;
        } catch (NumberFormatException e) {
            // If conversion fails, return original string
            return (T) stringValue;
        }
    }
    
    @Override
    public byte[] SetValue(Object value) {
        if (value == null) {
            return new byte[0];
        }
        
        String str;
        if (value instanceof Number) {
            str = value.toString();
        } else {
            str = value.toString().trim();
        }
        
        // DS should be maximum 16 characters
        if (str.length() > 16) {
            throw new IllegalArgumentException("DS value exceeds maximum length of 16 characters: " + str.length());
        }
        
        // Validate that it's a valid decimal string
        try {
            Double.parseDouble(str);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("DS value must be a valid decimal string: " + str);
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

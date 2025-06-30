package com.dicom.vr;

/**
 * TM (Time) VR类
 * 时间，格式为HHMMSS.FFFFFF
 * 长度：可变，最多16字节
 */
public class TM extends VRBase {
    
    private static TM instanceLE = null;
    private static TM instanceBE = null;
    
    private TM(boolean isBE) {
        super(isBE, false);
    }
    
    public static TM getInstance(boolean isBE) {
        if (isBE) {
            if (instanceBE == null) {
                instanceBE = new TM(true);
            }
            return instanceBE;
        } else {
            if (instanceLE == null) {
                instanceLE = new TM(false);
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
        
        // TM should be maximum 16 characters and follow format HHMMSS.FFFFFF
        if (str.length() > 16) {
            throw new IllegalArgumentException("TM value exceeds maximum length of 16 characters: " + str.length());
        }
        
        // Basic validation - should start with 6 digits for time part (HHMMSS)
        if (str.length() >= 6 && !str.substring(0, 6).matches("\\d{6}")) {
            throw new IllegalArgumentException("TM value must start with valid time format HHMMSS: " + str);
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

package com.dicom.vr;

/**
 * DT (Date Time) VR类
 * 日期时间，格式为YYYYMMDDHHMMSS.FFFFFF&ZZXX
 * 长度：可变，最多26字节
 */
public class DT extends VRBase {
    
    private static DT instanceLE = null;
    private static DT instanceBE = null;
    
    private DT(boolean isBE) {
        super(isBE, false);
    }
    
    public static DT getInstance(boolean isBE) {
        if (isBE) {
            if (instanceBE == null) {
                instanceBE = new DT(true);
            }
            return instanceBE;
        } else {
            if (instanceLE == null) {
                instanceLE = new DT(false);
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
        
        // DT should be maximum 26 characters and follow format YYYYMMDDHHMMSS.FFFFFF&ZZXX
        if (str.length() > 26) {
            throw new IllegalArgumentException("DT value exceeds maximum length of 26 characters: " + str.length());
        }
        
        // Basic validation - should start with 8 digits for date part (YYYYMMDD)
        if (str.length() >= 8 && !str.substring(0, 8).matches("\\d{8}")) {
            throw new IllegalArgumentException("DT value must start with valid date format YYYYMMDD: " + str);
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

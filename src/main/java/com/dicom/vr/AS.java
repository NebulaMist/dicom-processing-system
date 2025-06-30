package com.dicom.vr;

/**
 * AS (Age String) VR类
 * 年龄字符串，格式为nnnD/nnnW/nnnM/nnnY
 * 长度：固定4字节
 */
public class AS extends VRBase {
    
    private static AS instanceLE = null;
    private static AS instanceBE = null;
    
    private AS(boolean isBE) {
        super(isBE, false);
    }
    
    public static AS getInstance(boolean isBE) {
        if (isBE) {
            if (instanceBE == null) {
                instanceBE = new AS(true);
            }
            return instanceBE;
        } else {
            if (instanceLE == null) {
                instanceLE = new AS(false);
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
        
        String value = new String(data, startIndex, 4).trim();
        return (T) value;
    }
    
    @Override
    public byte[] SetValue(Object value) {
        if (value == null) {
            return new byte[4]; // Return 4 bytes filled with zeros
        }
        
        String str = value.toString().trim();
        
        // AS should match pattern like "012Y", "034M", "045W", "056D"
        if (!str.matches("\\d{3}[YMWD]")) {
            throw new IllegalArgumentException("AS value must match pattern nnnY/nnnM/nnnW/nnnD: " + str);
        }
        
        // AS is fixed 4 bytes
        byte[] result = new byte[4];
        byte[] data = str.getBytes();
        
        if (data.length > 4) {
            throw new IllegalArgumentException("AS value too long: " + str);
        }
        
        // Copy the data and pad with spaces if necessary
        System.arraycopy(data, 0, result, 0, data.length);
        for (int i = data.length; i < 4; i++) {
            result[i] = 0x20; // Space character
        }
        
        return result;
    }
}

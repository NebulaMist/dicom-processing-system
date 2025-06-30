package com.dicom.vr;

/**
 * UR (Universal Resource Identifier/Locator) VR类
 * 通用资源标识符/定位符
 * 长度：可变，长VR类型
 */
public class UR extends VRBase {
    
    private static UR instanceLE = null;
    private static UR instanceBE = null;
    
    private UR(boolean isBE) {
        super(isBE, true);  // UR是长VR类型
    }
    
    public static UR getInstance(boolean isBE) {
        if (isBE) {
            if (instanceBE == null) {
                instanceBE = new UR(true);
            }
            return instanceBE;
        } else {
            if (instanceLE == null) {
                instanceLE = new UR(false);
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
        
        // Convert to bytes using UTF-8 encoding for URI
        byte[] data = str.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        
        // Ensure even length by padding with null byte if necessary
        if (data.length % 2 != 0) {
            byte[] paddedData = new byte[data.length + 1];
            System.arraycopy(data, 0, paddedData, 0, data.length);
            paddedData[data.length] = 0x00; // Null byte
            return paddedData;
        }
        
        return data;
    }
}

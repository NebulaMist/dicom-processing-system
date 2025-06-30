package com.dicom.vr;

/**
 * LO (Long String) VR类
 * 长字符串，最多64字符的字符串
 * 长度：可变，偶数长度
 */
public class LO extends VRBase {
    
    private static LO instanceLE = null;
    private static LO instanceBE = null;
    
    private LO(boolean isBE) {
        super(isBE, false);
    }
    
    public static LO getInstance(boolean isBE) {
        if (isBE) {
            if (instanceBE == null) {
                instanceBE = new LO(true);
            }
            return instanceBE;
        } else {
            if (instanceLE == null) {
                instanceLE = new LO(false);
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
        
        String stringValue;
        if (value instanceof String) {
            stringValue = (String) value;
        } else {
            stringValue = value.toString();
        }
        
        // LO VR最多64个字符
        if (stringValue.length() > 64) {
            throw new IllegalArgumentException("LO字符串长度不能超过64个字符: " + stringValue.length());
        }
        
        try {
            byte[] bytes = stringValue.getBytes("UTF-8");
            
            // 确保长度为偶数（DICOM要求）
            if (bytes.length % 2 != 0) {
                byte[] paddedBytes = new byte[bytes.length + 1];
                System.arraycopy(bytes, 0, paddedBytes, 0, bytes.length);
                paddedBytes[bytes.length] = 0x20; // 用空格填充
                return paddedBytes;
            }
            
            return bytes;
        } catch (Exception e) {
            throw new RuntimeException("字符串编码失败: " + e.getMessage());
        }
    }
}

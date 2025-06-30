package com.dicom.vr;

/**
 * IS (Integer String) VR类
 * 整数字符串，最多12字符的整数字符串
 * 长度：可变，偶数长度
 */
public class IS extends VRBase {
    
    private static IS instanceLE = null;
    private static IS instanceBE = null;
    
    private IS(boolean isBE) {
        super(isBE, false);
    }
    
    public static IS getInstance(boolean isBE) {
        if (isBE) {
            if (instanceBE == null) {
                instanceBE = new IS(true);
            }
            return instanceBE;
        } else {
            if (instanceLE == null) {
                instanceLE = new IS(false);
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
        
        // 尝试转换为Integer
        try {
            Integer value = Integer.valueOf(stringValue);
            return (T) value;
        } catch (NumberFormatException e) {
            // 如果转换失败，返回原字符串
            return (T) stringValue;
        }
    }
    
    @Override
    public byte[] SetValue(Object value) {
        if (value == null) {
            return new byte[0];
        }
        
        String stringValue;
        if (value instanceof Integer) {
            // 如果是整数，转换为字符串
            stringValue = value.toString();
        } else if (value instanceof String) {
            // 如果是字符串，验证是否为有效整数
            String str = (String) value;
            try {
                Integer.valueOf(str.trim());
                stringValue = str.trim();
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("IS VR requires valid integer string, got: " + str);
            }
        } else {
            throw new IllegalArgumentException("IS VR requires Integer or String value, got: " + value.getClass().getSimpleName());
        }
        
        // 验证长度（最多12个字符）
        if (stringValue.length() > 12) {
            throw new IllegalArgumentException("IS VR string length cannot exceed 12 characters, got: " + stringValue.length());
        }
        
        // 转换为字节数组
        byte[] bytes = stringValue.getBytes();
        
        // 如果长度为奇数，需要添加一个null字节使其为偶数长度
        if (bytes.length % 2 == 1) {
            byte[] paddedBytes = new byte[bytes.length + 1];
            System.arraycopy(bytes, 0, paddedBytes, 0, bytes.length);
            paddedBytes[bytes.length] = 0; // null padding
            return paddedBytes;
        }
        
        return bytes;
    }
}

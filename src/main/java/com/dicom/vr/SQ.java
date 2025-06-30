package com.dicom.vr;

/**
 * SQ (Sequence of Items) VR类
 * 项目序列，包含嵌套的数据集
 * 长度：可变，长VR类型
 */
public class SQ extends VRBase {
    
    private static SQ instanceLE = null;
    private static SQ instanceBE = null;
    
    private SQ(boolean isBE) {
        super(isBE, true);  // SQ是长VR类型
    }
    
    public static SQ getInstance(boolean isBE) {
        if (isBE) {
            if (instanceBE == null) {
                instanceBE = new SQ(true);
            }
            return instanceBE;
        } else {
            if (instanceLE == null) {
                instanceLE = new SQ(false);
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
        
        // For SQ type, return sequence identifier string
        String value = "SEQUENCE";
        return (T) value;
    }
    
    @Override
    public byte[] SetValue(Object value) {
        if (value == null) {
            return new byte[0];
        }
        
        // SQ (Sequence of Items) is a complex type that typically contains
        // nested data sets. For now, we'll return an empty sequence marker.
        // In a full implementation, this would handle nested DCMDataSet objects.
        
        if (value instanceof byte[]) {
            return (byte[]) value;
        } else if (value instanceof String && "SEQUENCE".equals(value)) {
            // Return empty sequence marker
            return new byte[0];
        } else {
            // For other types, convert to string and encode as UTF-8
            String str = value.toString();
            byte[] data = str.getBytes(java.nio.charset.StandardCharsets.UTF_8);
            
            // Ensure even length by padding with null byte if necessary
            if (data.length % 2 != 0) {
                byte[] paddedData = new byte[data.length + 1];
                System.arraycopy(data, 0, paddedData, 0, data.length);
                paddedData[data.length] = 0x00;
                return paddedData;
            }
            
            return data;
        }
    }
}

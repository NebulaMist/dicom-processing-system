package com.dicom.vr;

/**
 * UN (Unknown) VR类
 * 未知类型，用于未定义的VR类型
 * 长度：可变，长VR类型
 */
public class UN extends VRBase {
    
    private static UN instanceLE = null;
    private static UN instanceBE = null;
    
    private UN(boolean isBE) {
        super(isBE, true);  // UN是长VR类型
    }
    
    public static UN getInstance(boolean isBE) {
        if (isBE) {
            if (instanceBE == null) {
                instanceBE = new UN(true);
            }
            return instanceBE;
        } else {
            if (instanceLE == null) {
                instanceLE = new UN(false);
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
        
        // For UN type, return raw byte array
        byte[] value = new byte[data.length - startIndex];
        System.arraycopy(data, startIndex, value, 0, value.length);
        return (T) value;
    }
    
    @Override
    public byte[] SetValue(Object value) {
        if (value == null) {
            return new byte[0];
        }
        
        if (value instanceof byte[]) {
            byte[] data = (byte[]) value;
            // Ensure even length by padding with zero if necessary
            if (data.length % 2 != 0) {
                byte[] paddedData = new byte[data.length + 1];
                System.arraycopy(data, 0, paddedData, 0, data.length);
                paddedData[data.length] = 0x00; // Null byte
                return paddedData;
            }
            return data.clone();
        } else {
            // Convert other types to string and then to bytes
            String str = value.toString();
            byte[] data = str.getBytes();
            
            // Ensure even length by padding with zero if necessary
            if (data.length % 2 != 0) {
                byte[] paddedData = new byte[data.length + 1];
                System.arraycopy(data, 0, paddedData, 0, data.length);
                paddedData[data.length] = 0x00; // Null byte
                return paddedData;
            }
            return data;
        }
    }
}

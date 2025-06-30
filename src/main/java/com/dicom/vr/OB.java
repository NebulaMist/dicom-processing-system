package com.dicom.vr;

/**
 * OB (Other Byte String) VR类
 * 其他字节字符串，用于存储字节数据
 * 长度：可变，长VR类型
 */
public class OB extends VRBase {
    
    private static OB instanceLE = null;
    private static OB instanceBE = null;
    
    private OB(boolean isBE) {
        super(isBE, true);  // OB是长VR类型
    }
    
    public static OB getInstance(boolean isBE) {
        if (isBE) {
            if (instanceBE == null) {
                instanceBE = new OB(true);
            }
            return instanceBE;
        } else {
            if (instanceLE == null) {
                instanceLE = new OB(false);
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
        
        // For OB type, return byte array starting from startIndex
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
            throw new IllegalArgumentException("OB value must be byte array, got: " + value.getClass().getSimpleName());
        }
    }
}

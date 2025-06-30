package com.dicom.vr;

/**
 * SH (Short String) VR class
 * Short String, up to 16 character string
 * Length: Variable, even length
 */
public class SH extends VRBase {
    
    private static SH instanceLE = null;
    private static SH instanceBE = null;
    
    private SH(boolean isBE) {
        super(isBE, false);
    }
    
    public static SH getInstance(boolean isBE) {
        if (isBE) {
            if (instanceBE == null) {
                instanceBE = new SH(true);
            }
            return instanceBE;
        } else {
            if (instanceLE == null) {
                instanceLE = new SH(false);
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
    
    /**
     * 实现SetValue方法：将字符串转换为字节数组
     * @param <T> 值的类型
     * @param obj 要转换的值（应为String类型）
     * @return 转换后的字节数组
     */
    @Override
    public <T> byte[] SetValue(T obj) {
        if(obj instanceof String) {
            String val = (String)obj;
            int len = val.length();
            java.nio.ByteBuffer buffer = java.nio.ByteBuffer.allocate(len);

            byte[] bytes = val.getBytes();
            buffer.put(bytes);
            return buffer.array();
        }
        else
            // 处理其他类型或抛出异常
            throw new IllegalArgumentException("string type needed, but received: " + 
                obj.getClass().getSimpleName());
    }
}

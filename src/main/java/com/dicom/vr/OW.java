package com.dicom.vr;

/**
 * OW (Other Word String) VR class
 * Other Word String, 16-bit word data
 * Length: Variable, long VR type
 */
public class OW extends VRBase {
    
    private static OW instanceLE = null;
    private static OW instanceBE = null;
    
    private OW(boolean isBE) {
        super(isBE, true);  // OW是长VR类型
    }
    
    public static OW getInstance(boolean isBE) {
        if (isBE) {
            if (instanceBE == null) {
                instanceBE = new OW(true);
            }
            return instanceBE;
        } else {
            if (instanceLE == null) {
                instanceLE = new OW(false);
            }
            return instanceLE;
        }
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public <T> T GetValue(byte[] data, int startIndex) {
        if (data == null || startIndex < 0 || startIndex + 2 > data.length) {
            return null;
        }
        
        // 与US类似，解析16位无符号短整数
        int value;
        if (isBE) {
            value = ((data[startIndex] & 0xFF) << 8) |
                   (data[startIndex + 1] & 0xFF);
        } else {
            value = ((data[startIndex + 1] & 0xFF) << 8) |
                   (data[startIndex] & 0xFF);
        }
        
        return (T) Integer.valueOf(value);
    }
    
    /**
     * 实现SetValue方法：将short数组转换为字节数组
     * @param <T> 值的类型
     * @param obj 要转换的值（应为short[]类型）
     * @return 转换后的字节数组
     */
    @Override
    public <T> byte[] SetValue(T obj) {
        if ("short[]".equals(obj.getClass().getSimpleName())) {
            short[] val = (short[])obj;
            java.nio.ByteBuffer buffer;
            if (isBE)
                buffer = java.nio.ByteBuffer.allocate(val.length * 2).order(java.nio.ByteOrder.BIG_ENDIAN);
            else
                buffer = java.nio.ByteBuffer.allocate(val.length * 2).order(java.nio.ByteOrder.LITTLE_ENDIAN);

            for (Short aShort : val) buffer.putShort(aShort);
            return buffer.array();
        }
        // 处理其他类型或抛出异常
        throw new IllegalArgumentException("short[] type needed, but got: " + 
            obj.getClass().getSimpleName());
    }
}

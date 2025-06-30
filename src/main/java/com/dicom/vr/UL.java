package com.dicom.vr;

/**
 * UL (Unsigned Long) VR class
 * Represents 32-bit unsigned long integer
 * Length: 4 bytes
 */
public class UL extends VRBase {
      /**
     * Flyweight pattern: static instance to reduce memory usage
     */
    private static UL instanceLE = null;  // Little Endian实例
    private static UL instanceBE = null;  // Big Endian实例
    
    /**
     * 私有构造函数，防止外部直接实例化
     * @param isBE 是否为BigEndian解码
     */
    private UL(boolean isBE) {
        super(isBE, false);  // UL不是长VR类型
    }
    
    /**
     * 享元模式：获取实例
     * @param isBE 是否为BigEndian解码
     * @return UL实例
     */
    public static UL getInstance(boolean isBE) {
        if (isBE) {
            if (instanceBE == null) {
                instanceBE = new UL(true);
            }
            return instanceBE;
        } else {
            if (instanceLE == null) {
                instanceLE = new UL(false);
            }
            return instanceLE;
        }
    }
    
    /**
     * 从字节数组中获取32位无符号长整型值
     * @param <T> 返回类型为Long
     * @param data 字节数组
     * @param startIndex 起始索引
     * @return 解析后的长整型值
     */
    @Override
    @SuppressWarnings("unchecked")
    public <T> T GetValue(byte[] data, int startIndex) {
        if (data == null || startIndex < 0 || startIndex + 4 > data.length) {
            return null;
        }
        
        long value;
        if (isBE) {
            // Big Endian: 高位字节在前
            value = ((long)(data[startIndex] & 0xFF) << 24) |
                   ((long)(data[startIndex + 1] & 0xFF) << 16) |
                   ((long)(data[startIndex + 2] & 0xFF) << 8) |
                   ((long)(data[startIndex + 3] & 0xFF));
        } else {
            // Little Endian: 低位字节在前
            value = ((long)(data[startIndex + 3] & 0xFF) << 24) |
                   ((long)(data[startIndex + 2] & 0xFF) << 16) |
                   ((long)(data[startIndex + 1] & 0xFF) << 8) |
                   ((long)(data[startIndex] & 0xFF));
        }
        
        return (T) Long.valueOf(value);
    }
    
    /**
     * 实现SetValue方法：将整数值转换为字节数组
     * @param <T> 值的类型
     * @param val 要转换的值（应为Integer类型）
     * @return 转换后的字节数组
     */
    @Override
    public <T> byte[] SetValue(T val) {
        if (val instanceof Integer) {
            java.nio.ByteBuffer buffer;
            if (isBE)
                buffer = java.nio.ByteBuffer.allocate(4).order(java.nio.ByteOrder.BIG_ENDIAN);
            else
                buffer = java.nio.ByteBuffer.allocate(4).order(java.nio.ByteOrder.LITTLE_ENDIAN);

            buffer.putInt((Integer)val);
            return buffer.array();
        }
        // 处理其他类型或抛出异常
        throw new IllegalArgumentException("int type needed, but got: " + 
            val.getClass().getSimpleName());
    }
}

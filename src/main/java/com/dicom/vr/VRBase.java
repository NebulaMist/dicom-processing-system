package com.dicom.vr;

/**
 * VRBase抽象基类
 * 实现DICOM值表示（Value Representation）的基础功能
 * 采用享元模式优化内存使用
 */
public abstract class VRBase {
    
    /**
     * 是否为BigEndian字节序
     */
    public boolean isBE;
    
    /**
     * 是否为长VR类型（OB/OF/OD/OW/OL/SQ/UT/UN/UR/UC）
     */
    public boolean isLongVR;
    
    /**
     * 构造函数
     * @param isBE 是否为BigEndian解码
     * @param isLongVR 是否为长VR类型
     */
    public VRBase(boolean isBE, boolean isLongVR) {
        this.isBE = isBE;
        this.isLongVR = isLongVR;
    }
    
    /**
     * 抽象泛型方法：从字节数组中获取值表示法对应类型的值
     * @param <T> 返回值类型
     * @param data 字节数组
     * @param startIndex 起始索引
     * @return 解析后的值
     */
    public abstract <T> T GetValue(byte[] data, int startIndex);
    
    /**
     * 抽象方法：将值转换为字节数组
     * @param <T> 值的类型
     * @param val 要转换的值
     * @return 转换后的字节数组
     */
    public abstract <T> byte[] SetValue(T val);
    
    /**
     * 转换为字符串表示
     * @param data 字节数组
     * @param startIndex 起始索引
     * @param head 前缀字符串
     * @return 拼接后的字符串
     */
    public String ToString(byte[] data, int startIndex, String head) {
        Object value = GetValue(data, startIndex);
        return head + (value != null ? value.toString() : "");
    }
}

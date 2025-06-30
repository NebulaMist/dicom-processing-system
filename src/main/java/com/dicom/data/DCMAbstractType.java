package com.dicom.data;

import com.dicom.vr.VRBase;

/**
 * DICOM抽象类型 - 抽象构件类
 * 用于表示DICOM数据元素的基本结构
 */
public abstract class DCMAbstractType {
    // 公共字段
    public Short gtag;          // 组号
    public Short etag;          // 元素号
    public String name;         // 名称
    public String vr;           // Value Representation
    public String vm;           // Value Multiplicity
    public int length;          // 值长度
    public byte[] value;        // 值
    public VRBase vrparser;     // VR解析器
    
    // 保护字段
    protected TransferSyntax syntax;  // 传输语法对象
    
    /**
     * 构造函数
     * @param ts 传输语法实例
     */
    public DCMAbstractType(TransferSyntax ts) {
        this.syntax = ts;
    }
    
    /**
     * 抽象方法：输出解码结果字符串
     * @param head 前缀字符串
     * @return 格式化的字符串
     */
    public abstract String ToString(String head);
    
    /**
     * 抽象方法：解码数据集
     * @param data 待解码的字节数组
     * @param idx 当前解码位置的索引数组（用于返回新的位置）
     * @return 解码后的DCMAbstractType对象
     */
    public abstract DCMAbstractType Parse(byte[] data, int[] idx);
    
    /**
     * 抽象方法：编码为字节数组
     * @return 编码后的字节数组
     */
    public abstract byte[] Encode();
    
    /**
     * 抽象方法：编码为字节数组（带未定义长度参数）
     * @param isUndefinedLength 是否使用未定义长度
     * @return 编码后的字节数组
     */
    public abstract byte[] Encode(boolean isUndefinedLength);

    /**
     * 模板方法：根据DICOM标签获取值
     * @param dicomTag DICOM标签常量
     * @return 指定标签的值
     */
    public <T> T GetValue(int dicomTag) {
        return null;
    }
    
    /**
     * 模板方法：根据DICOM标签获取数据元素名称
     * @param dicomTag DICOM标签常量
     * @return 指定标签的名称
     */
    public String GetName(int dicomTag) {
        return "";
    }
    
    /**
     * 模板方法：根据DICOM标签获取VR
     * @param dicomTag DICOM标签常量
     * @return 指定标签的VR
     */
    public String GetVR(int dicomTag) {
        return "";
    }
    
    /**
     * 模板方法：根据DICOM标签获取VM
     * @param dicomTag DICOM标签常量
     * @return 指定标签的VM
     */
    public String GetVM(int dicomTag) {
        return "";
    }
    
    /**
     * 泛型方法：设置值
     * @param <T> 值的类型
     * @param value 要设置的值
     */
    public <T> void SetValue(T value) {
        if (vrparser != null) {
            this.value = vrparser.SetValue(value);
            this.length = this.value.length;
        } else {
            throw new IllegalStateException("VR parser not initialized");
        }
    }
}

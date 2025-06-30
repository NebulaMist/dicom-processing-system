package com.dicom.data;

/**
 * DICOM数据元素类 - 叶子构件类
 * 继承DCMAbstractType，表示具体的DICOM数据元素
 */
public class DCMDataElement extends DCMAbstractType {
    
    /**
     * 构造函数
     * @param ts 传输语法实例
     */
    public DCMDataElement(TransferSyntax ts) {
        super(ts);
    }
    
    /**
     * 返回各字段的字符串表示
     * @param head 前缀字符串
     * @return 用制表符分割的各字段值
     */
    @Override
    public String ToString(String head) {
        StringBuilder sb = new StringBuilder();
        
        // 添加前缀
        if (head != null && !head.isEmpty()) {
            sb.append(head);
        }
        
        // 构建标签字符串
        String tag = String.format("(%04X,%04X)", 
            gtag != null ? gtag : 0, 
            etag != null ? etag : 0);
        
        sb.append(tag).append("\t");
        sb.append(name != null ? name : "").append("\t");
        sb.append(vr != null ? vr : "").append("\t");
        sb.append(vm != null ? vm : "").append("\t");
        sb.append(length).append("\t");
          // 值的字符串表示
        if (value != null && value.length > 0) {
            if (vrparser != null) {
                // 使用VR解析器将值转换为字符串
                String valueStr = vrparser.ToString(value, 0, "");
                sb.append(valueStr);
            } else {
                // 备用处理：显示十六进制
                if (value.length <= 32) {
                    StringBuilder hexValue = new StringBuilder();
                    for (int i = 0; i < Math.min(value.length, 16); i++) {
                        hexValue.append(String.format("%02X ", value[i]));
                    }
                    if (value.length > 16) {
                        hexValue.append("...");
                    }
                    sb.append(hexValue.toString().trim());
                } else {
                    sb.append(String.format("[%d bytes]", value.length));
                }
            }
        } else {
            sb.append("(empty)");
        }
        
        return sb.toString();
    }
    
    /**
     * 解码方法（叶子节点直接返回null）
     * @param data 待解码的字节数组
     * @param idx 当前解码位置的索引数组
     * @return null（叶子节点不进行解码操作）
     */
    @Override
    public DCMAbstractType Parse(byte[] data, int[] idx) {
        // 叶子构件直接返回null
        return null;
    }
    
    /**
     * 实现编码方法
     * @return 编码后的字节数组
     */
    @Override
    public byte[] Encode() {
        return syntax.Encode(this, false);
    }
    
    /**
     * 实现带参数的编码方法
     * @param isUndefinedLength 是否使用未定义长度
     * @return 编码后的字节数组
     */
    @Override
    public byte[] Encode(boolean isUndefinedLength) {
        return syntax.Encode(this, isUndefinedLength);
    }
}

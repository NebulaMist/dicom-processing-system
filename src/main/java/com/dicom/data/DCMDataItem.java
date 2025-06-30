package com.dicom.data;

/**
 * DICOM数据条目类 - SQ序列中的条目
 * 继承DCMDataSet类，用于表示序列中的数据条目
 */
public class DCMDataItem extends DCMDataSet {

    /**
     * 构造函数
     * @param ts 传输语法实例
     */
    public DCMDataItem(TransferSyntax ts) {
        super(ts);
    }

    /**
     * 重写Parse方法：从条目开始标记起始，读子数据集中的每条数据元素
     * 需要处理未定义长度，把缓冲区剩余字节都作为值域，通过条目结束标记结束
     * 
     * @param data 待解码的字节数组
     * @param idx 当前解码位置的索引数组（用于返回新的位置）
     * @return 解码后的数据条目列表
     */
    @Override
    public DCMAbstractType Parse(byte[] data, int[] idx) {
        // 清空现有items
        items.clear();
        
        try {
            System.out.println("    DCMDataItem.Parse开始: idx=" + idx[0] + ", length=" + length + ", 剩余字节=" + (data.length - idx[0]));
            
            if (length == (int)0xffffffff) {   // 条目未定义长度
                while (idx[0] < data.length) {  // 循环条件为数据长度，以条目定界符结束
                    System.out.println("      解析位置: " + idx[0] + ", 剩余: " + (data.length - idx[0]));
                    DCMAbstractType item = syntax.Decode(data, idx);  // 解析一条数据元素或数据集
                    if (item == null) {
                        System.err.println("      解析条目时出错: 解析返回null");
                        break;
                    }
                    
                    System.out.println("      解析到元素: (" + String.format("%04X", item.gtag & 0xFFFF) + 
                                     "," + String.format("%04X", item.etag & 0xFFFF) + ") VR=" + item.vr + " length=" + item.length);
                    
                    if (item.gtag == (short) 0xfffe && item.etag == (short) 0xe00d) {   // 条目定界标记
                        System.out.println("      遇到条目定界符，结束解析");
                        break;
                    } else {
                        items.add(item);  // 添加到解析结果
                    }
                }
            } else {                  // 条目确定长度
                int offset = idx[0];
                while (idx[0] < this.length + offset) { // 循环条件为条目长度加偏移量
                    DCMAbstractType item = syntax.Decode(data, idx);
                    if (item == null) {
                        System.err.println("解析条目时出错: 解析返回null");
                        break;
                    }
                    items.add(item);
                }
            }
            
            System.out.println("    DCMDataItem.Parse完成: 解析了 " + items.size() + " 个元素");
            
        } catch (Exception e) {
            System.err.println("解析条目时出错: " + e.getMessage());
            e.printStackTrace();
        }
        
        return this;
    }

    /**
     * 重写ToString方法
     * 
     * @param head 前缀字符串
     * @return 格式化的条目字符串
     */
    @Override
    public String ToString(String head) {
        System.out.println("      DCMDataItem.ToString: items.size=" + items.size());
        
        if (items.isEmpty()) {
            System.out.println("      返回: (empty item)");
            return head + "(empty item)";
        }
        
        StringBuilder str = new StringBuilder();
        for (int i = 0; i < items.size(); i++) {
            DCMAbstractType elem = items.get(i);
            System.out.println("        元素 " + i + ": " + (elem != null ? elem.getClass().getSimpleName() : "null"));
            if (elem != null) {
                if (i > 0) str.append("\n");
                str.append(elem.ToString(head));
            }
        }
        
        String result = str.toString();
        System.out.println("      返回内容长度: " + result.length() + " 内容: [" + result + "]");
        return result;
    }
}

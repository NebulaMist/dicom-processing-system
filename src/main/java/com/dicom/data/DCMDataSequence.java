package com.dicom.data;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Collections;

/**
 * DICOM数据序列类 - SQ序列
 * 继承DCMDataSet类，用于表示DICOM序列类型的数据元素
 */
public class DCMDataSequence extends DCMDataSet {

    /**
     * 构造函数
     * @param ts 传输语法实例
     */
    public DCMDataSequence(TransferSyntax ts) {
        super(ts);
    }

    /**
     * 重写Parse方法：解析SQ序列中的数据条目
     * 
     * @param data 待解码的字节数组
     * @param idx 当前解码位置的索引数组（用于返回新的位置）
     * @return 解码后的序列条目列表
     */
    @Override
    public DCMAbstractType Parse(byte[] data, int[] idx) {
        // 清空现有items
        items.clear();
        
        try {
            System.out.println("  DCMDataSequence.Parse开始: idx=" + idx[0] + ", length=" + length + ", 剩余字节=" + (data.length - idx[0]));
            
            if (length == (int)0xffffffff) {   // sq未定义长度
                while (idx[0] < data.length) {  // 循环条件为数据长度
                    System.out.println("    解析位置: " + idx[0] + ", 剩余: " + (data.length - idx[0]));
                    
                    // 检查是否还有足够的数据来解析一个完整的元素头部
                    if (idx[0] + 8 > data.length) {
                        System.out.println("    数据不足，结束解析");
                        break;
                    }
                    
                    DCMAbstractType item = syntax.Decode(data, idx);     // 解析一个条目或定界符
                    if (item == null) {
                        System.err.println("    解析序列时出错: 解析返回null");
                        break;
                    }
                    
                    System.out.println("    解析到元素: (" + String.format("%04X", item.gtag & 0xFFFF) + 
                                     "," + String.format("%04X", item.etag & 0xFFFF) + ") VR=" + item.vr + " length=" + item.length);
                    
                    if (item.gtag == (short) 0xfffe && item.etag == (short) 0xe0dd) {  // sq定界符，结束
                        System.out.println("    遇到SQ定界符，结束解析");
                        break;
                    } else if (item.gtag == (short) 0xfffe && item.etag == (short) 0xe000) {
                        // 条目开始标记，创建DCMDataItem并解析内容
                        System.out.println("    遇到条目开始标记，创建DCMDataItem");
                        
                        // 检查是否还有数据用于解析条目内容
                        if (idx[0] >= data.length) {
                            System.out.println("    警告：条目开始标记后没有数据，跳过");
                            break;
                        }
                        
                        DCMDataItem dataItem = new DCMDataItem(syntax);
                        dataItem.gtag = item.gtag;
                        dataItem.etag = item.etag;
                        dataItem.length = item.length;
                        dataItem.vr = ""; // 条目没有VR
                        dataItem.name = "";
                        dataItem.syntax = this.syntax; // 设置传输语法
                        dataItem.Parse(data, idx);  // 解析条目内容
                        System.out.println("    条目解析完成，包含 " + dataItem.getItemCount() + " 个子元素");
                        
                        // 总是添加解析的条目，即使是空的（符合DICOM标准）
                        items.add(dataItem);
                    } else {
                        // 其他类型的元素直接添加（不应该出现在序列中）
                        System.err.println("    警告：序列中发现非条目元素: " + 
                                         String.format("(%04X,%04X)", item.gtag & 0xFFFF, item.etag & 0xFFFF));
                        items.add(item);
                    }
                }
            } else {      // sq确定长度
                int offset = idx[0];
                while (idx[0] < length + offset) {
                    DCMAbstractType item = syntax.Decode(data, idx);
                    if (item == null) {
                        System.err.println("解析序列时出错: 解析返回null");
                        break;
                    }
                    
                    if (item.gtag == (short) 0xfffe && item.etag == (short) 0xe000) {
                        // 条目开始标记，创建DCMDataItem并解析内容
                        DCMDataItem dataItem = new DCMDataItem(syntax);
                        dataItem.gtag = item.gtag;
                        dataItem.etag = item.etag;
                        dataItem.length = item.length;
                        dataItem.Parse(data, idx);  // 解析条目内容
                        items.add(dataItem);
                    } else {
                        items.add(item);
                    }
                }
            }
            
            System.out.println("  DCMDataSequence.Parse完成: 解析了 " + items.size() + " 个条目");
            
        } catch (Exception e) {
            System.err.println("解析序列时出错: " + e.getMessage());
            e.printStackTrace();
        }
        
        return this;
    }

    /**
     * 重写ToString方法，枚举items列表，先拼接"ITEM"标识和换行符，
     * 再调用其ToString方法（通过在head参数上增加">"来反映嵌套层次关系），拼接好返回字符串。
     * （所有VR子类的ToString方法需忽略head参数）
     * 
     * @param head 前缀字符串
     * @return 格式化的序列字符串
     */
    @Override
    public String ToString(String head) {
        StringBuilder str = new StringBuilder();
        str.append(String.format("%04X", gtag & 0xFFFF)).append("\t")
           .append(String.format("%04X", etag & 0xFFFF)).append("\t")
           .append(name).append("\t")
           .append(vr).append("\t")
           .append(length);

        System.out.println("DCMDataSequence.ToString: 序列包含 " + items.size() + " 个条目");
        
        if (items.isEmpty()) {
            str.append(" (empty)");
        } else {
            for (int i = 0; i < items.size(); i++) {
                DCMAbstractType elem = items.get(i);
                System.out.println("  条目 " + i + ": " + (elem != null ? elem.getClass().getSimpleName() : "null"));
                if (elem != null) {
                    str.append("\n").append(head).append(">item");
                    if (elem instanceof DCMDataItem) {
                        // 对于DCMDataItem，直接显示其内容
                        DCMDataItem dataItem = (DCMDataItem) elem;
                        System.out.println("    DCMDataItem包含 " + dataItem.getItemCount() + " 个元素");
                        String itemContent = elem.ToString(head + ">");
                        System.out.println("    ToString结果长度: " + itemContent.length() + " 内容: [" + itemContent + "]");
                        if (!itemContent.trim().isEmpty()) {
                            str.append("\n").append(itemContent);
                        } else {
                            str.append("\n").append(head).append(">(empty content)");
                        }
                    } else {
                        // 对于其他类型的元素
                        str.append("\n").append(head).append(">").append(elem.ToString(head + ">"));
                    }
                }
            }
        }
        return str.toString();
    }
    
    /**
     * 覆盖DCMAbstractType基类的SetValue方法，接收DCMDataItem[]类型的值
     * @param obj DCMDataItem数组
     */
    @Override
    public <T> void SetValue(T obj) {
        if("DCMDataItem[]".equals(obj.getClass().getSimpleName())) {
            DCMDataItem[] val = (DCMDataItem[]) obj;
            
            // 清空现有条目并添加新的条目
            items.clear();
            Collections.addAll(items, val);
            length = 0xffffffff; // 设置为未定义长度
        } else {
            // 处理其他类型或抛出异常
            throw new IllegalArgumentException("DCMDataItem[] type needed, but received: " + 
                obj.getClass().getSimpleName());
        }
    }
    
    /**
     * 实现Encode方法 - 编码SQ序列为字节数组
     * @param isUndefinedLength 是否使用未定义长度编码
     * @return 编码后的字节数组
     */
    @Override
    public byte[] Encode(boolean isUndefinedLength) {
        ByteBuffer buff = ByteBuffer.allocate(1024*1024) // 编码缓冲区1M
                .order(syntax.isBE ? ByteOrder.BIG_ENDIAN : ByteOrder.LITTLE_ENDIAN);
        length = 0;
        
        try {
            byte[] head = syntax.Encode(this, isUndefinedLength);   // 编码前三部分
            buff.put(head);
            int lenpos = buff.position() - 4;  // SQ长度域开始位置
            
            // 编码值域：遍历SQ中每条条目
            for (DCMAbstractType elem : items) {
                // 条目开始标记，隐式VR，UL
                buff.putShort((short)0xfffe);
                buff.putShort((short)0xe000);
                byte[] data = elem.Encode(isUndefinedLength); // 调用数据元素的编码方法
                
                if(isUndefinedLength) {  // 未定义长度
                    buff.putInt(0xffffffff);
                    buff.put(data); // 值即条目的编码结果
                    
                    // 条目结束标记，隐式VR，UL
                    buff.putShort((short)0xfffe);
                    buff.putShort((short)0xe00d);
                    buff.putInt(0);
                } else {        // 确定长度
                    buff.putInt(data.length);
                    buff.put(data); // 值即条目的编码结果
                }
            }
            
            if(isUndefinedLength) {  // SQ未定义长度
                buff.putShort((short)0xfffe);  // 序列结束标记
                buff.putShort((short)0xe0dd);
                buff.putInt(0);
                length = 0xffffffff;
            } else {
                length = buff.position() - lenpos - 4;  // 修正length
            }
            
            int allpos = buff.position();  // 暂存最后位置
            buff.position(lenpos);  // 跳到长度域起始位置
            buff.putInt(length);    // 保存长度
            buff.position(allpos);  // 恢复最后位置
            
            byte[] result = new byte[buff.position()];
            buff.position(0);
            buff.get(result, 0, result.length);
            
            return result;                    // 得到编码结果字节数组
        } catch (Exception e) {
            System.err.println("编码序列时出错: " + e.getMessage());
            e.printStackTrace();
            return new byte[0];
        }
    }
}

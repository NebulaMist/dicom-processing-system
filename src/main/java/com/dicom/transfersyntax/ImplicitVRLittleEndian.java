package com.dicom.transfersyntax;

import com.dicom.data.TransferSyntax;
import com.dicom.data.DCMAbstractType;
import com.dicom.data.DCMDataElement;
import com.dicom.data.DCMDataItem;
import com.dicom.data.DCMDataSequence;

/**
 * 隐式VR小端传输语法类
 */
public class ImplicitVRLittleEndian extends TransferSyntax {
      /**
     * 构造函数
     */
    public ImplicitVRLittleEndian() {
        super(false); // 调用父类带参数构造函数，传入小端字节序
        // 初始化基类字段
        this.isExplicit = false;
        this.name = "ImplicitVRLittleEndian";
        this.uid = "1.2.840.10008.1.2";
    }
    
    /**
     * 实现抽象解码方法
     * 隐式VR格式：Tag(4字节) + Length(4字节) + Value
     * @param buff 字节缓冲区
     * @return 解码后的数据元素
     */
    @Override
    protected DCMAbstractType Decode(DicomByteBuffer buff) {
        if (buff == null || !buff.hasRemaining()) {
            return null;
        }
          try {
            // 读取标签（4字节）
            short gtag = buff.readShort();  // 组号（2字节）
            short etag = buff.readShort();  // 元素号（2字节）
            
            // 读取长度（4字节）
            int length = buff.readInt();
            
            // 检查是否为序列或条目相关的特殊标签
            if (gtag == (short) 0xfffe) {
                if (etag == (short) 0xe000) {
                    // Item - 创建数据条目
                    DCMDataItem item = new DCMDataItem(this);
                    item.gtag = gtag;
                    item.etag = etag;
                    item.length = length;
                    item.name = "Item";
                    item.vr = "";
                    item.vm = "1";
                    // 条目的值数据包含子数据集，需要进一步解析
                    if (length > 0 && length < buff.remaining()) {
                        item.value = buff.readBytes(length);
                    } else if (length == 0 || length == 0xffffffff) {
                        item.value = new byte[0];
                    } else {
                        item.value = buff.readBytes(buff.remaining());
                        item.length = item.value.length;
                    }
                    return item;
                } else if (etag == (short) 0xe00d || etag == (short) 0xe0dd) {
                    // Item Delimitation 或 Sequence Delimitation
                    DCMDataElement delimiter = new DCMDataElement(this);
                    delimiter.gtag = gtag;
                    delimiter.etag = etag;
                    delimiter.length = length;
                    delimiter.value = new byte[0];
                    if (etag == (short) 0xe00d) {
                        delimiter.name = "Item Delimitation Item";
                    } else {
                        delimiter.name = "Sequence Delimitation Item";
                    }
                    delimiter.vr = "";
                    delimiter.vm = "1";
                    return delimiter;
                }
            }
              // 普通数据元素
            DCMDataElement element = new DCMDataElement(this);
            element.gtag = gtag;
            element.etag = etag;
            element.length = length;
            
            // 从字典查询VR和名称信息
            LookupDictionary(element);
            
            // 检查是否为SQ序列类型
            if ("SQ".equals(element.vr)) {
                TransferSyntax syn = new ImplicitVRLittleEndian();
                DCMDataSequence sq = new DCMDataSequence(syn);
                sq.gtag = element.gtag;
                sq.etag = element.etag;
                sq.name = element.name;
                sq.vr = element.vr;
                sq.vrparser = element.vrparser;
                sq.length = element.length;
                sq.value = new byte[0];
                
                // 创建序列值的字节数组用于解析
                byte[] seqValue = new byte[element.length];
                if (element.length > 0 && element.length < buff.remaining()) {
                    seqValue = buff.readBytes(element.length);
                }
                
                int[] idx = {0};  // 从序列值开始解析
                sq.Parse(seqValue, idx);
                return sq;
            }
            
            // 读取值数据
            if (element.length > 0 && element.length < buff.remaining()) {
                element.value = buff.readBytes(element.length);
            } else if (element.length == 0) {
                element.value = new byte[0];
            } else {
                // 长度超出剩余数据，读取所有剩余数据
                element.value = buff.readBytes(buff.remaining());
                element.length = element.value.length;
            }
            
            return element;
            
        } catch (Exception e) {
            System.err.println("隐式VR小端解码出错: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * 实现编码方法 - 隐式VR格式
     * @param buff 字节缓冲区
     * @param element 数据元素
     * @return 编码的字节数
     */
    @Override
    protected int Encode(java.nio.ByteBuffer buff, DCMAbstractType element) {
        long pos = buff.position();
        //写入TAG
        buff.putShort(element.gtag);
        buff.putShort(element.etag);
        //隐式VR不写入VR字段，直接写入4字节长度
        int len = element.value.length;
        if (len % 2 != 0) len++;          //调整为偶数
        buff.putInt(len);        //四字节长度
        //写入值
        WriteValue(buff, element);
        
        return (int)(buff.position() - pos);
    }
}

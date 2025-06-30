package com.dicom.transfersyntax;

import com.dicom.data.TransferSyntax;
import com.dicom.data.DCMAbstractType;
import com.dicom.data.DCMDataElement;
import com.dicom.data.DCMDataSequence;

/**
 * 显式VR大端传输语法类
 */
public class ExplicitVRBigEndian extends TransferSyntax {
      /**
     * 构造函数
     */
    public ExplicitVRBigEndian() {
        super(true); // 调用父类带参数构造函数，传入大端字节序
        // 初始化基类字段
        this.isExplicit = true;
        this.name = "ExplicitVRBigEndian";
        this.uid = "1.2.840.10008.1.2.2";
    }
    
    /**
     * 实现抽象解码方法
     * 显式VR格式：Tag(4字节) + VR(2字节) + Length(2/4字节) + Value
     * @param buff 字节缓冲区
     * @return 解码后的数据元素
     */
    @Override
    protected DCMAbstractType Decode(DicomByteBuffer buff) {
        if (buff == null || !buff.hasRemaining()) {
            return null;
        }
        
        try {
            // 创建数据元素
            DCMDataElement element = new DCMDataElement(this);
            
            // 读取标签（4字节）
            element.gtag = buff.readShort();  // 组号（2字节）
            element.etag = buff.readShort();  // 元素号（2字节）
            
            // 读取VR（2字节）
            element.vr = buff.readString(2);
            
            // 根据VR类型决定长度字段的大小
            if (isLongVR(element.vr)) {
                // 长VR：跳过保留字段（2字节），读取4字节长度
                buff.readShort(); // 跳过保留字段
                element.length = buff.readInt();
            } else {
                // 短VR：读取2字节长度
                element.length = buff.readShort() & 0xFFFF; // 转换为无符号short
                
                // 检查异常长度值
                if (element.length == 65535 && !element.vr.equals("SQ")) {
                    System.err.println("警告：数据元素长度无效: " + element.length + " 在位置 " + buff.getPosition() + 
                                     " VR=" + element.vr + " Tag=(" + String.format("%04X", element.gtag & 0xFFFF) + 
                                     "," + String.format("%04X", element.etag & 0xFFFF) + ")");
                }
            }
              // 检查是否为SQ序列类型
            if (element.vr.equals("SQ")) {
                TransferSyntax syn = new ExplicitVRBigEndian();
                DCMDataSequence sq = new DCMDataSequence(syn);
                sq.gtag = element.gtag;
                sq.etag = element.etag;
                sq.name = element.name;
                sq.vr = element.vr;
                sq.vrparser = element.vrparser;
                sq.length = element.length;
                sq.value = new byte[0];
                
                // 对于SQ序列，直接在原数据流上解析，不需要单独的字节数组
                if (element.length == (int)0xffffffff) {
                    // 未定义长度的SQ：直接在buff上解析，由序列类处理定界符
                    int[] idx = {buff.getPosition()};
                    sq.Parse(buff.getData(), idx);
                    buff.setPosition(idx[0]); // 更新buff的位置
                } else if (element.length > 0) {
                    // 确定长度的SQ：读取指定长度的数据进行解析
                    if (element.length <= buff.remaining()) {
                        byte[] seqValue = buff.readBytes(element.length);
                        int[] idx = {0};
                        sq.Parse(seqValue, idx);
                    } else {
                        System.err.println("SQ序列长度超出剩余数据: " + element.length + " > " + buff.remaining());
                    }
                } else {
                    // 长度为0的空序列
                    // 已经初始化为空，无需额外操作
                }
                
                return sq;
            }
            
            // 从字典查询补充信息（主要是名称和VM）
            LookupDictionary(element);
            
            // 读取值数据
            if (element.length > 0 && element.length <= buff.remaining()) {
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
            System.err.println("显式VR大端解码出错: " + e.getMessage());
            return null;
        }
    }
    /**
     * 实现编码方法
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
        //写入VR
        buff.put(element.vr.getBytes());
        //写入值长度
        int len = (element.value != null) ? element.value.length : 0;
        if (len % 2 != 0) len++;          //调整为偶数
        if(isLongVR(element.vr)) {
            buff.putShort((short) 0);     //保留2字节00
            buff.putInt(len);        //四字节长度
        }
        else
            buff.putShort((short)len);  //二字节长度
        
        //写入值
        if (element.value != null && element.value.length > 0) {
            buff.put(element.value);
            if (element.value.length % 2 != 0) buff.put((byte) 0); //奇数长度时补零
        }
        
        return (int)(buff.position() - pos);
    }
}

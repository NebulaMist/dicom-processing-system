package com.dicom.data;

import com.dicom.dictionary.DicomDictionary;
import com.dicom.dictionary.DicomDictionaryEntry;
import com.dicom.transfersyntax.DicomByteBuffer;
import com.dicom.vr.VRFactory;
import java.nio.ByteOrder;

/**
 * 传输语法基类
 * 用于处理DICOM数据的传输语法
 */
public abstract class TransferSyntax {
    // 公共字段
    public boolean isBE;        // 是否大端字节序
    public boolean isExplicit;  // 是否显式VR
    public String name;         // 传输语法名称
    public String uid;          // 传输语法UID
    
    // 私有字段
    private DicomByteBuffer buffer;  // 缓存区
      // 保护字段
    protected DicomDictionary dict;  // 数据字典对象
    protected VRFactory vrfactory;   // VR工厂实例
      /**
     * 默认构造函数
     */
    public TransferSyntax() {
        this.buffer = null;
        // 初始化数据字典
        try {
            this.dict = DicomDictionary.loadFromClasspath();
        } catch (Exception e) {
            System.err.println("加载DICOM字典失败: " + e.getMessage());
            this.dict = new DicomDictionary();
        }
        // 初始化VR工厂
        this.vrfactory = new VRFactory(isBE);
    }
    
    /**
     * 带字节序参数的构造函数
     * @param isBE 是否为大端字节序
     */
    public TransferSyntax(boolean isBE) {
        this.isBE = isBE;
        this.buffer = null;
        // 初始化数据字典
        try {
            this.dict = DicomDictionary.loadFromClasspath();
        } catch (Exception e) {
            System.err.println("加载DICOM字典失败: " + e.getMessage());
            this.dict = new DicomDictionary();
        }
        // 初始化VR工厂
        this.vrfactory = new VRFactory(isBE);
    }    /**
     * 查询数据字典，填充元素的名称、VR、VM等信息
     * @param element 要查询的数据元素
     */
    public void LookupDictionary(DCMDataElement element) {
        LookupDictionary(element, null);
    }
    
    /**
     * 查询数据字典，填充元素的名称、VR、VM等信息
     * @param element 要查询的数据元素
     * @param receiver VR接收器，用于处理复合VR
     */
    public void LookupDictionary(DCMDataElement element, VRReceiver receiver) {
        if (element == null || dict == null) {
            return;
        }
        
        // 将short转换为十六进制字符串进行查询
        String gtag = String.format("%04X", element.gtag & 0xFFFF);
        String etag = String.format("%04X", element.etag & 0xFFFF);
        
        DicomDictionaryEntry entry = dict.lookup(gtag, etag);
        if(entry != null) {
            element.name = entry.getName();
            if(entry.getVr().length() == 2) {
                if (element.vr == null || element.vr.isEmpty() || 
                    element.vr.equals("\0\0"))
                    element.vr = entry.getVr();
            }
            else if(receiver != null)
                element.vr = receiver.action(element.gtag, element.etag);
            else
                element.vr = "OW";
                
            element.vm = entry.getVm();
        } else {
            // 未找到时设置默认值
            element.name = "Unknown";
            if (!isExplicit && element.vr == null) {
                element.vr = "UN";  // Unknown
            }
            element.vm = "1";
        }
        
        // 使用VR工厂创建VR解析器实例
        if (element.vr != null && vrfactory != null) {
            element.vrparser = vrfactory.GetVR(element.vr);
        }
    }
    
    /**
     * 判断是否为长VR
     * @param vr VR字符串
     * @return 是否为长VR
     */
    protected boolean isLongVR(String vr) {
        if (vr == null || vr.length() == 0) {
            return false;
        }
        
        // 长VR类型：OB/OF/OD/OW/OL/SQ/UT/UN/UR/UC
        return vr.equals("OB") || vr.equals("OF") || vr.equals("OD") ||
               vr.equals("OW") || vr.equals("OL") || vr.equals("SQ") ||
               vr.equals("UT") || vr.equals("UN") || vr.equals("UR") ||
               vr.equals("UC");
    }    /**
     * 解码方法（公共接口）
     * @param data 字节数据
     * @param idx 位置索引数组
     * @return 解码后的数据元素
     */
    public DCMAbstractType Decode(byte[] data, int[] idx) {
        if (data == null || idx == null || idx.length == 0) {
            return null;
        }
        
        // 检查是否需要重新加载数据到buffer
        if (buffer == null || data.length != data.length 
                || buffer.getPosition() != idx[0]) { // 有数据更改就重新加载数据
            ByteOrder byteOrder = isBE ? ByteOrder.BIG_ENDIAN : ByteOrder.LITTLE_ENDIAN;
            buffer = new DicomByteBuffer(data, idx[0], byteOrder);
        }        DCMAbstractType element;
        short gtag = buffer.readShort();

        if (gtag == (short)0xfffe) {   // 三个特殊标记按照隐式VR读
            short etag = buffer.readShort();
            
            if (etag == (short)0xe000) {
                // 条目开始标记 - 创建DCMDataItem
                element = new DCMDataItem(this);
            } else {
                // 序列定界符(0xE0DD)或条目结束定界符(0xE00D) - 创建普通元素
                element = new DCMDataElement(this);
            }

            // 读取TAG
            element.gtag = gtag;
            element.etag = etag;
            element.vr = "UL";
            element.vrparser = VRFactory.getVRInstance(element.vr, isBE);
            element.length = buffer.readInt(); // 读4字节值长度
            idx[0] = buffer.getPosition();
            
            // 只有条目开始标记需要进一步解析内容
            if (etag == (short)0xe000) {
                element.Parse(data, idx);
            }
        } else {
            buffer.setPosition(buffer.getPosition() - 2);  // 回退2字节
            element = this.Decode(buffer);  // 调用具体传输语法类解码数据元素
        }
        idx[0] = buffer.getPosition();  // 返回解码数据元素后的位置
        return element;
    }
    
    /**
     * 值域编码方法，对值域长度为奇数时填充0或0x20
     * @param buff 字节缓冲区
     * @param element 数据元素
     */
    protected void WriteValue(java.nio.ByteBuffer buff, DCMAbstractType element) {
        buff.put(element.value);
        if (element.value.length % 2 != 0) {    //填充为偶数字节长
            if (element.vr.equals("OB") || element.vr.equals("UI"))
                buff.put((byte)0x00);
            else
                buff.put((byte)0x20);
        }
    }
    
    /**
     * 编码抽象方法接口
     * @param buff 字节缓冲区
     * @param item 数据元素
     * @return 编码的字节数
     */
    protected abstract int Encode(java.nio.ByteBuffer buff, DCMAbstractType item);
      /**
     * 编码重载方法
     * @param item 数据元素
     * @param isUndefinedLength 是否使用未定义长度
     * @return 编码后的字节数组
     */
    public byte[] Encode(DCMAbstractType item, boolean isUndefinedLength) {
        if (isUndefinedLength && "SQ".equals(item.vr)) {
            // 对于SQ序列，使用未定义长度编码
            return encodeSequenceUndefinedLength(item);
        } else {
            // 使用确定长度编码（原有逻辑）
            int len = 2 + 2 + (isExplicit?2:0) + (isLongVR(item.vr)?6:2) + item.length;
            if(item.length%2 != 0) len++;
            java.nio.ByteBuffer buffer = java.nio.ByteBuffer.allocate(len)
                    .order(isBE ? java.nio.ByteOrder.BIG_ENDIAN : java.nio.ByteOrder.LITTLE_ENDIAN);
            
            if (isUndefinedLength) {
                // 对于普通元素的未定义长度编码
                encodeUndefinedLength(buffer, item);
            } else {
                // 确定长度编码
                Encode(buffer, item);
            }
            return buffer.array();
        }
    }
    
    /**
     * 编码SQ序列为未定义长度格式
     * @param item SQ序列元素
     * @return 编码后的字节数组
     */
    private byte[] encodeSequenceUndefinedLength(DCMAbstractType item) {
        java.nio.ByteBuffer buffer = java.nio.ByteBuffer.allocate(1024 * 1024) // 1MB缓冲区
                .order(isBE ? java.nio.ByteOrder.BIG_ENDIAN : java.nio.ByteOrder.LITTLE_ENDIAN);
        
        // 1. 编码标签 (4字节)
        buffer.putShort(item.gtag);
        buffer.putShort(item.etag);
        
        // 2. 编码VR (如果是显式VR)
        if (isExplicit) {
            buffer.put("SQ".getBytes());
            buffer.putShort((short) 0); // 保留字段
        }
        
        // 3. 编码未定义长度
        buffer.putInt(0xFFFFFFFF);
        
        // 4. 编码序列值（由DCMDataSequence处理）
        if (item.value != null && item.value.length > 0) {
            buffer.put(item.value);
        }
        
        // 5. 序列定界符
        buffer.putShort((short) 0xFFFE);
        buffer.putShort((short) 0xE0DD);
        buffer.putInt(0);
        
        // 返回实际使用的字节
        byte[] result = new byte[buffer.position()];
        buffer.flip();
        buffer.get(result);
        return result;
    }
    
    /**
     * 编码普通元素为未定义长度格式
     * @param buffer 字节缓冲区
     * @param item 数据元素
     */
    private void encodeUndefinedLength(java.nio.ByteBuffer buffer, DCMAbstractType item) {
        // 1. 编码标签 (4字节)
        buffer.putShort(item.gtag);
        buffer.putShort(item.etag);
        
        // 2. 编码VR (如果是显式VR)
        if (isExplicit) {
            buffer.put(item.vr.getBytes());
            if (isLongVR(item.vr)) {
                buffer.putShort((short) 0); // 保留字段
                buffer.putInt(0xFFFFFFFF); // 未定义长度
            } else {
                buffer.putShort((short) 0xFFFF); // 未定义长度标记
            }
        } else {
            buffer.putInt(0xFFFFFFFF); // 隐式VR的未定义长度
        }
        
        // 3. 编码值数据
        if (item.value != null && item.value.length > 0) {
            buffer.put(item.value);
            // 确保偶数长度
            if (item.value.length % 2 != 0) {
                buffer.put((byte) 0);
            }
        }
    }
    
    /**
     * 抽象解码方法（由具体传输语法类实现）
     * @param buff 字节缓冲区
     * @return 解码后的数据元素
     */
    protected abstract DCMAbstractType Decode(DicomByteBuffer buff);
}

package com.dicom.data;

import java.util.List;
import java.util.ArrayList;
import com.dicom.vr.VRFactory;
import com.dicom.vr.US;
import com.dicom.vr.SS;
import com.dicom.vr.UL;
import com.dicom.vr.SL;
import com.dicom.vr.OW;
import com.dicom.dictionary.DicomDictionary;

/**
 * DICOM数据集类 - 容器构件类
 * 继承DCMAbstractType，用于数据集、条目、序列、文件头元素等结构
 */
public class DCMDataSet extends DCMAbstractType {
    
    /**
     * 所容纳的数据元素或条目列表
     */
    protected List<DCMAbstractType> items;
    
    /**
     * DICOM字典实例，用于查找标签的VR
     */
    private static DicomDictionary dictionary;
    
    // 静态初始化字典
    static {
        try {
            dictionary = DicomDictionary.loadFromClasspath();
        } catch (Exception e) {
            System.err.println("Warning: Failed to load DICOM dictionary: " + e.getMessage());
            dictionary = null;
        }
    }
    
    /**
     * 构造函数
     * @param ts 传输语法实例
     */
    public DCMDataSet(TransferSyntax ts) {
        super(ts);
        this.items = new ArrayList<DCMAbstractType>();
    }
    
    /**
     * 遍历items中每个数据元素，调用其ToString()方法
     * 把每个数据元素的输出结果以换行符为分隔符组合起来
     * @param head 前缀字符串
     * @return 格式化的数据集字符串
     */
    @Override
    public String ToString(String head) {
        final StringBuilder result = new StringBuilder();
        
        // 使用传统for循环代替forEach（兼容Java 7）
        for (DCMAbstractType elem : items) {
            if (elem != null) {
                if (result.length() > 0) {
                    result.append("\n");  // 两个数据元素之间用换行符分隔
                }
                result.append(elem.ToString(head));
            }
        }
        
        return result.toString();
    }
    
    /**
     * 解码方法 - 从字节数组中解码整个数据集
     * @param data 待解码的字节数组
     * @param idx 当前解码位置的索引数组（用于返回新的位置）
     * @return 解码后的数据元素列表
     */    @Override
    public DCMAbstractType Parse(byte[] data, int[] idx) {
        // 清空现有items
        items.clear();
        
        System.out.println("开始解析数据集，位置: " + idx[0] + ", 剩余字节: " + (data.length - idx[0]));
        
        int elementCount = 0;
        while (idx[0] < data.length - 8) { // 至少需要8字节（标签4字节+长度4字节）
            try {
                // 创建新的数据元素
                DCMDataElement element = new DCMDataElement(syntax);
                
                // 保存起始位置
                int startPos = idx[0];
                  // 1. 读取标签（4字节：gtag + etag）
                if (idx[0] + 4 > data.length) {
                    break;
                }
                
                int gtag = readUInt16(data, idx[0]);
                int etag = readUInt16(data, idx[0] + 2);
                idx[0] += 4;
                  element.gtag = (short) gtag;
                element.etag = (short) etag;
                
                // 2. 根据传输语法确定是否为显式VR
                String vr = "";
                int length = 0;
                
                if (syntax.isExplicit) {
                    // 显式VR：读取VR（2字节）
                    if (idx[0] + 2 > data.length) {
                        break;
                    }
                    
                    vr = new String(data, idx[0], 2, "ASCII").trim();
                    idx[0] += 2;
                    
                    // 检查VR是否需要额外的2字节保留字段
                    if (needsReservedField(vr)) {
                        // 跳过2字节保留字段
                        idx[0] += 2;
                        // 读取4字节长度
                        if (idx[0] + 4 > data.length) {
                            break;
                        }
                        length = readUInt32(data, idx[0]);
                        idx[0] += 4;
                    } else {
                        // 读取2字节长度
                        if (idx[0] + 2 > data.length) {
                            break;
                        }
                        length = readUInt16(data, idx[0]);
                        idx[0] += 2;
                    }
                } else {
                    // 隐式VR：直接读取4字节长度
                    if (idx[0] + 4 > data.length) {
                        break;
                    }
                    
                    length = readUInt32(data, idx[0]);
                    idx[0] += 4;
                    
                    // 从字典获取VR
                    vr = getDictionaryVR(gtag, etag);
                }
                  // 检查长度的有效性
                // 长度为0xFFFFFFFF（-1）是DICOM中的未定义长度，不是错误
                if (length != (int)0xFFFFFFFF && (length < 0 || idx[0] + length > data.length)) {
                    System.err.println("警告：数据元素长度无效: " + length + " 在位置 " + startPos);
                    break;
                }
                  // 3. 读取值数据
                byte[] value;
                if (length == (int)0xFFFFFFFF) {
                    // 未定义长度，对于序列和条目，值数据将在Parse方法中处理
                    value = new byte[0];
                } else {
                    value = new byte[length];
                    if (length > 0) {
                        System.arraycopy(data, idx[0], value, 0, length);
                        idx[0] += length;
                    }
                }// 4. 填充element的字段
                element.vr = vr;
                element.vm = "1"; // 简化处理，默认为1
                element.length = length;
                element.value = value;
                element.name = getElementName(gtag, etag);
                  // 5. 创建VR解析器
                if (vr != null && !vr.isEmpty()) {
                    element.vrparser = VRFactory.getVRInstance(vr, syntax.isBE);
                }
                  // 6. 检查是否为序列类型，如果是则创建DCMDataSequence而不是普通元素
                if ("SQ".equals(vr)) {
                    // 创建序列对象
                    DCMDataSequence sequence = new DCMDataSequence(syntax);
                    sequence.gtag = element.gtag;
                    sequence.etag = element.etag;
                    sequence.vr = element.vr;
                    sequence.vm = element.vm;
                    sequence.length = element.length;
                    sequence.name = element.name;
                    sequence.vrparser = element.vrparser;
                    
                    // 解析序列内容
                    try {
                        if (length == (int)0xFFFFFFFF) {
                            // 未定义长度的序列，直接从当前位置开始解析
                            sequence.Parse(data, idx);
                        } else {
                            // 确定长度的序列，从值数据中解析
                            int[] seqIdx = {0};
                            sequence.Parse(value, seqIdx);
                        }
                        items.add(sequence);
                        System.out.println("序列元素: " + element.name + " (已解析内容，长度=" + 
                            (length == (int)0xFFFFFFFF ? "未定义" : String.valueOf(length)) + ")");
                    } catch (Exception e) {
                        System.err.println("解析序列时出错: " + e.getMessage());
                        // 如果序列解析失败，仍然添加普通元素
                        items.add(element);
                    }} else {
                    // 普通数据元素
                    items.add(element);
                }
                elementCount++;
                
                // 输出调试信息（限制输出数量）
                if (elementCount <= 10) {
                    System.out.println("解析元素 #" + elementCount + ": " + 
                        String.format("(%04X,%04X)", gtag, etag) + " " + 
                        element.name + " [" + vr + "] 长度=" + length);
                } else if (elementCount == 11) {
                    System.out.println("... （继续解析更多元素）");
                }
                
            } catch (Exception e) {
                // 解码出错时跳出循环
                System.err.println("解码数据集第" + (elementCount + 1) + "个元素时出错: " + e.getMessage());
                break;
            }
        }
        
        System.out.println("数据集解析完成，共解析了 " + elementCount + " 个元素");
        return this;
    }
    
    /**
     * 获取数据元素列表
     * @return items列表
     */
    public List<DCMAbstractType> getItems() {
        return items;
    }
    
    /**
     * 添加数据元素
     * @param item 要添加的数据元素
     */
    public void addItem(DCMAbstractType item) {
        if (item != null) {
            items.add(item);
        }
    }
    
    /**
     * 添加数据元素（向后兼容方法）
     * @param element 要添加的数据元素
     */
    public void Add(DCMAbstractType element) {
        addItem(element);
    }
    
    /**
     * 获取数据元素数量
     * @return 元素数量
     */
    public int getItemCount() {
        return items.size();
    }    /**
     * 智能获取数据元素方法：
     * - 如果参数 < 1000，视为数组索引，返回对应位置的元素
     * - 如果参数 >= 1000，视为DICOM标签，返回元素包装器支持SetValue操作
     * @param indexOrTag 数组索引或DICOM标签
     * @return 数据元素或元素包装器
     */
    public Object Item(int indexOrTag) {
        if (indexOrTag < 1000) {
            // 视为数组索引
            if (indexOrTag < 0 || indexOrTag >= items.size()) {
                return null;
            }
            return items.get(indexOrTag);
        } else {
            // 视为DICOM标签，返回包装器
            return new DCMElementWrapper(this, indexOrTag);
        }
    }
    
    /**
     * 实现Encode方法：将数据集编码为字节数组
     * @return 编码后的字节数组
     */
    @Override
    public byte[] Encode() {
        try {
            java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
            
            // 编码所有子项
            for (DCMAbstractType item : items) {
                if (item != null) {
                    byte[] encodedItem = item.Encode();
                    if (encodedItem != null && encodedItem.length > 0) {
                        baos.write(encodedItem);
                    }
                }
            }
            
            return baos.toByteArray();
        } catch (Exception e) {
            System.err.println("编码数据集时出错: " + e.getMessage());
            return new byte[0];
        }
    }
    
    /**
     * 重载的Encode方法：支持未定义长度编码
     * @param isUndefinedLength 是否使用未定义长度
     * @return 编码后的字节数组
     */
    @Override
    public byte[] Encode(boolean isUndefinedLength) {
        try {
            java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
            
            // 编码所有子项，传递未定义长度参数
            for (DCMAbstractType item : items) {
                if (item != null) {
                    byte[] encodedItem = item.Encode(isUndefinedLength);
                    if (encodedItem != null && encodedItem.length > 0) {
                        baos.write(encodedItem);
                    }
                }
            }
            
            return baos.toByteArray();
        } catch (Exception e) {
            System.err.println("编码数据集时出错: " + e.getMessage());
            return new byte[0];
        }
    }
      /**
     * 创建或获取指定DICOM标签的数据元素（特殊方法用于支持类似dcm.ItemByTag(tag).SetValue()的语法）
     * @param dicomTag DICOM标签常量
     * @return 数据元素的包装器，支持SetValue操作
     */
    public DCMElementWrapper ItemByTag(int dicomTag) {
        return new DCMElementWrapper(this, dicomTag);
    }
    
    /**
     * 数据元素包装器类，支持SetValue操作
     */
    public static class DCMElementWrapper {
        private DCMDataSet dataset;
        private int dicomTag;
        
        public DCMElementWrapper(DCMDataSet dataset, int dicomTag) {
            this.dataset = dataset;
            this.dicomTag = dicomTag;
        }
        
        /**
         * 设置元素的值
         * @param value 要设置的值
         */
        public void SetValue(Object value) {
            // 将标签拆分为gtag和etag
            short gtag = (short) ((dicomTag >> 16) & 0xFFFF);
            short etag = (short) (dicomTag & 0xFFFF);
            
            // 查找现有元素
            DCMDataElement element = null;
            for (DCMAbstractType item : dataset.items) {
                if (item instanceof DCMDataElement) {
                    DCMDataElement elem = (DCMDataElement) item;
                    if (elem.gtag == gtag && elem.etag == etag) {
                        element = elem;
                        break;
                    }
                }
            }
            
            // 如果元素不存在，创建新元素
            if (element == null) {
                element = new DCMDataElement(dataset.syntax);
                element.gtag = gtag;
                element.etag = etag;
                element.name = getElementName(gtag, etag);
                element.vr = getDictionaryVR(gtag, etag);
                element.vm = "1"; // 默认VM
                dataset.items.add(element);
            }
            
            // 根据VR类型设置值
            try {
                if (value instanceof String) {
                    if ("UI".equals(element.vr) || "SH".equals(element.vr) || "LO".equals(element.vr) || 
                        "PN".equals(element.vr) || "CS".equals(element.vr) || "DA".equals(element.vr) || 
                        "TM".equals(element.vr) || "DT".equals(element.vr) || "AS".equals(element.vr) ||
                        "IS".equals(element.vr) || "DS".equals(element.vr) || "AE".equals(element.vr)) {
                        String strValue = (String) value;
                        element.value = strValue.getBytes("UTF-8");
                        // 确保长度为偶数
                        if (element.value.length % 2 != 0) {
                            byte[] paddedValue = new byte[element.value.length + 1];
                            System.arraycopy(element.value, 0, paddedValue, 0, element.value.length);
                            // 对于UI和其他不同VR使用不同的填充字符
                            if ("UI".equals(element.vr)) {
                                paddedValue[element.value.length] = 0x00; // UI用null填充
                            } else {
                                paddedValue[element.value.length] = 0x20; // 其他用空格填充
                            }
                            element.value = paddedValue;
                        }
                    } else {
                        throw new IllegalArgumentException("VR " + element.vr + " 不支持String类型的值");
                    }
                } else if (value instanceof Short) {
                    if ("US".equals(element.vr)) {
                        US usVR = US.getInstance(dataset.syntax.isBE);
                        element.value = usVR.SetValue(value);
                    } else if ("SS".equals(element.vr)) {
                        SS ssVR = SS.getInstance(dataset.syntax.isBE);
                        element.value = ssVR.SetValue(value);
                    } else {
                        throw new IllegalArgumentException("VR " + element.vr + " 不支持Short类型的值");
                    }
                } else if (value instanceof Integer) {
                    if ("UL".equals(element.vr)) {
                        UL ulVR = UL.getInstance(dataset.syntax.isBE);
                        element.value = ulVR.SetValue(value);
                    } else if ("SL".equals(element.vr)) {
                        SL slVR = SL.getInstance(dataset.syntax.isBE);
                        element.value = slVR.SetValue(value);
                    } else if ("US".equals(element.vr)) {
                        US usVR = US.getInstance(dataset.syntax.isBE);
                        element.value = usVR.SetValue(value);
                    } else {
                        throw new IllegalArgumentException("VR " + element.vr + " 不支持Integer类型的值");
                    }
                } else if (value instanceof short[]) {
                    if ("OW".equals(element.vr)) {
                        OW owVR = OW.getInstance(dataset.syntax.isBE);
                        element.value = owVR.SetValue(value);
                    } else {
                        throw new IllegalArgumentException("VR " + element.vr + " 不支持short[]类型的值");
                    }
                } else if (value instanceof byte[]) {
                    // 直接使用byte[]值，适用于OB、UN等VR类型以及文件头元素
                    byte[] byteValue = (byte[]) value;
                    element.value = byteValue;
                    // 确保长度为偶数
                    if (element.value.length % 2 != 0) {
                        byte[] paddedValue = new byte[element.value.length + 1];
                        System.arraycopy(element.value, 0, paddedValue, 0, element.value.length);
                        paddedValue[element.value.length] = 0x00; // 用null填充
                        element.value = paddedValue;
                    }
                } else {
                    throw new IllegalArgumentException("不支持的值类型: " + value.getClass().getSimpleName());
                }
                
                element.length = element.value.length;
                
                // 创建VR解析器
                element.vrparser = VRFactory.getVRInstance(element.vr, dataset.syntax.isBE);
                
            } catch (Exception e) {
                throw new RuntimeException("设置元素值时出错: " + e.getMessage(), e);
            }
        }
        
        /**
         * 获取元素名称
         */
        private String getElementName(short gtag, short etag) {
            int tag = (gtag & 0xFFFF) * 65536 + (etag & 0xFFFF);
            switch (tag) {
                case 0x00080016: return "SOP Class UID";
                case 0x00080018: return "SOP Instance UID";
                case 0x00080020: return "Study Date";
                case 0x00080030: return "Study Time";
                case 0x00080060: return "Modality";
                case 0x00080090: return "Referring Physician's Name";
                case 0x00100010: return "Patient's Name";
                case 0x00100020: return "Patient ID";
                case 0x00100030: return "Patient's Birth Date";
                case 0x00100040: return "Patient's Sex";
                case 0x00200010: return "Study ID";
                case 0x0020000D: return "Study Instance UID";
                case 0x0020000E: return "Series Instance UID";
                case 0x00200011: return "Series Number";
                case 0x00200013: return "Instance Number";
                case 0x00280010: return "Rows";
                case 0x00280011: return "Columns";
                case 0x7FE00010: return "Pixel Data";
                case 0x00081150: return "Referenced SOP Class UID";
                case 0x00081155: return "Referenced SOP Instance UID";
                default: return String.format("(%04X,%04X)", gtag & 0xFFFF, etag & 0xFFFF);
            }
        }
        
        /**
         * 获取元素VR
         */
        private String getDictionaryVR(short gtag, short etag) {
            int tag = (gtag & 0xFFFF) * 65536 + (etag & 0xFFFF);
            switch (tag) {
                case 0x00080016: return "UI"; // SOP Class UID
                case 0x00080018: return "UI"; // SOP Instance UID
                case 0x00080020: return "DA"; // Study Date
                case 0x00080030: return "TM"; // Study Time
                case 0x00080060: return "CS"; // Modality
                case 0x00080090: return "PN"; // Referring Physician's Name
                case 0x00100010: return "PN"; // Patient's Name
                case 0x00100020: return "LO"; // Patient ID
                case 0x00100030: return "DA"; // Patient's Birth Date
                case 0x00100040: return "CS"; // Patient's Sex
                case 0x00200010: return "SH"; // Study ID
                case 0x0020000D: return "UI"; // Study Instance UID
                case 0x0020000E: return "UI"; // Series Instance UID
                case 0x00200011: return "IS"; // Series Number
                case 0x00200013: return "IS"; // Instance Number
                case 0x00280010: return "US"; // Rows
                case 0x00280011: return "US"; // Columns
                case 0x7FE00010: return "OW"; // Pixel Data
                case 0x00081150: return "UI"; // Referenced SOP Class UID
                case 0x00081155: return "UI"; // Referenced SOP Instance UID
                default: return "UN"; // Unknown
            }
        }
    }
    
    /**
     * 重写GetValue方法：根据DICOM标签获取值
     * @param dicomTag DICOM标签常量
     * @return 指定标签的值
     */
    @Override
    public <T> T GetValue(int dicomTag) {
        try {
            for (DCMAbstractType item : items) {
                if (item != null && (item.gtag * 65536 + item.etag) == dicomTag) {
                    if (item.vrparser != null) {
                        return item.vrparser.GetValue(item.value, 0);
                    }
                    break;
                }
            }
        } catch (Exception e) {
            System.err.println("获取值时出错，标签: 0x" + Integer.toHexString(dicomTag) + ", 错误: " + e.getMessage());
        }
        return null;
    }
      /**
     * 重写GetName方法：根据DICOM标签获取数据元素名称
     * @param dicomTag DICOM标签常量
     * @return 指定标签的名称
     */
    @Override
    public String GetName(int dicomTag) {
        try {
            for (DCMAbstractType item : items) {
                if (item != null && (item.gtag * 65536 + item.etag) == dicomTag) {
                    return item.name != null ? item.name : "";
                }
            }
        } catch (Exception e) {
            System.err.println("获取名称时出错，标签: 0x" + Integer.toHexString(dicomTag) + ", 错误: " + e.getMessage());
        }
        return "";
    }
      /**
     * 重写GetVR方法：根据DICOM标签获取VR
     * @param dicomTag DICOM标签常量
     * @return 指定标签的VR
     */
    @Override
    public String GetVR(int dicomTag) {
        try {
            for (DCMAbstractType item : items) {
                if (item != null && (item.gtag * 65536 + item.etag) == dicomTag) {
                    return item.vr != null ? item.vr : "";
                }
            }
        } catch (Exception e) {
            System.err.println("获取VR时出错，标签: 0x" + Integer.toHexString(dicomTag) + ", 错误: " + e.getMessage());
        }
        return "";
    }
      /**
     * 重写GetVM方法：根据DICOM标签获取VM
     * @param dicomTag DICOM标签常量
     * @return 指定标签的VM
     */
    @Override
    public String GetVM(int dicomTag) {
        try {
            for (DCMAbstractType item : items) {
                if (item != null && (item.gtag * 65536 + item.etag) == dicomTag) {
                    return item.vm != null ? item.vm : "";
                }
            }
        } catch (Exception e) {
            System.err.println("获取VM时出错，标签: 0x" + Integer.toHexString(dicomTag) + ", 错误: " + e.getMessage());
        }
        return "";
    }
      /**
     * 小端序读取16位无符号整数
     * @param data 字节数组
     * @param offset 偏移位置
     * @return 16位整数值
     */
    private int readUInt16LE(byte[] data, int offset) {
        return (data[offset] & 0xFF) | ((data[offset + 1] & 0xFF) << 8);
    }
    
    /**
     * 小端序读取32位无符号整数
     * @param data 字节数组
     * @param offset 偏移位置
     * @return 32位整数值
     */
    private int readUInt32LE(byte[] data, int offset) {
        return (data[offset] & 0xFF) | 
               ((data[offset + 1] & 0xFF) << 8) | 
               ((data[offset + 2] & 0xFF) << 16) | 
               ((data[offset + 3] & 0xFF) << 24);
    }
    
    /**
     * 大端序读取16位无符号整数
     * @param data 字节数组
     * @param offset 偏移位置
     * @return 16位整数值
     */
    private int readUInt16BE(byte[] data, int offset) {
        return ((data[offset] & 0xFF) << 8) | (data[offset + 1] & 0xFF);
    }
    
    /**
     * 大端序读取32位无符号整数
     * @param data 字节数组
     * @param offset 偏移位置
     * @return 32位整数值
     */
    private int readUInt32BE(byte[] data, int offset) {
        return ((data[offset] & 0xFF) << 24) | 
               ((data[offset + 1] & 0xFF) << 16) | 
               ((data[offset + 2] & 0xFF) << 8) | 
               (data[offset + 3] & 0xFF);
    }
    
    /**
     * 根据字节序读取16位无符号整数
     * @param data 字节数组
     * @param offset 偏移位置
     * @return 16位整数值
     */
    private int readUInt16(byte[] data, int offset) {
        return syntax.isBE ? readUInt16BE(data, offset) : readUInt16LE(data, offset);
    }
    
    /**
     * 根据字节序读取32位无符号整数
     * @param data 字节数组
     * @param offset 偏移位置
     * @return 32位整数值
     */
    private int readUInt32(byte[] data, int offset) {
        return syntax.isBE ? readUInt32BE(data, offset) : readUInt32LE(data, offset);
    }
    
    /**
     * 检查VR是否需要2字节保留字段
     * @param vr VR字符串
     * @return 是否需要保留字段
     */
    private boolean needsReservedField(String vr) {
        // OB, OW, OF, SQ, UT, UN 等VR需要保留字段
        return "OB".equals(vr) || "OW".equals(vr) || "OF".equals(vr) || 
               "SQ".equals(vr) || "UT".equals(vr) || "UN".equals(vr);
    }
    
    /**
     * 从字典获取VR
     * @param gtag 组标签
     * @param etag 元素标签
     * @return VR字符串
     */
    private String getDictionaryVR(int gtag, int etag) {
        // 首先尝试从字典查找
        if (dictionary != null) {
            String vr = dictionary.getVR(String.format("%04X", gtag), String.format("%04X", etag));
            if (vr != null && !vr.trim().isEmpty()) {
                return vr.trim();
            }
        }
        
        // 如果字典查找失败，使用预定义的常见标签
        int tag = gtag * 65536 + etag;
        switch (tag) {
            case 0x00080016: return "UI"; // SOP Class UID
            case 0x00080018: return "UI"; // SOP Instance UID
            case 0x00080020: return "DA"; // Study Date
            case 0x00080060: return "CS"; // Modality
            case 0x00100010: return "PN"; // Patient's Name
            case 0x00100020: return "LO"; // Patient ID
            case 0x00100030: return "DA"; // Patient's Birth Date
            case 0x00100040: return "CS"; // Patient's Sex
            case 0x00101010: return "AS"; // Patient's Age
            case 0x00101030: return "DS"; // Patient's Weight
            case 0x00200010: return "SH"; // Study ID
            case 0x0020000D: return "UI"; // Study Instance UID
            case 0x0020000E: return "UI"; // Series Instance UID
            case 0x00280010: return "US"; // Rows
            case 0x00280011: return "US"; // Columns
            case 0x00280100: return "US"; // Bits Allocated
            case 0x00280101: return "US"; // Bits Stored
            case 0x00280102: return "US"; // High Bit
            case 0x00280103: return "US"; // Pixel Representation
            default: return "UN"; // Unknown
        }
    }
    
    /**
     * 根据标签获取元素名称
     * @param gtag 组标签
     * @param etag 元素标签
     * @return 元素名称
     */
    private String getElementName(int gtag, int etag) {
        // 简化实现：根据常见标签返回名称
        int tag = gtag * 65536 + etag;
        switch (tag) {
            case 0x00080016: return "SOP Class UID";
            case 0x00080018: return "SOP Instance UID";
            case 0x00080020: return "Study Date";
            case 0x00080060: return "Modality";
            case 0x00100010: return "Patient's Name";
            case 0x00100020: return "Patient ID";
            case 0x00280010: return "Rows";
            case 0x00280011: return "Columns";
            default: return String.format("(%04X,%04X)", gtag, etag);
        }
    }
}

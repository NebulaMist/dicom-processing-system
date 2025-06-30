package com.dicom.data;

import com.dicom.vr.VRFactory;

/**
 * DICOM文件头元素处理类
 * 继承DCMDataSet，专门用于处理DICOM文件的头部信息（组号为0002的元素）
 * 
 * DICOM文件头包含：
 * - Transfer Syntax UID
 * - Media Storage SOP Class UID
 * - Media Storage SOP Instance UID
 * - Implementation Class UID
 * - Implementation Version Name
 * 等文件元信息
 */
public class DCMFileMeta extends DCMDataSet {
    
    /**
     * 构造函数
     * @param ts 传输语法实例
     */
    public DCMFileMeta(TransferSyntax ts) {
        super(ts);
    }
    
    /**
     * 重写Parse方法，专门解析DICOM文件头部元素
     * 文件头部只包含组号为0x0002的元素，且总是使用显式VR小端序
     * 
     * @param data 待解码的字节数组
     * @param idx 当前解码位置的索引数组（用于返回新的位置）
     * @return 解码后的文件头数据集
     */
    @Override
    public DCMAbstractType Parse(byte[] data, int[] idx) {
        // 清空现有items
        items.clear();
        
        System.out.println("开始解析DICOM文件头部元素...");
        
        try {
            // 文件头部元素总是使用显式VR小端序传输语法
            // 每个数据元素结构：标签(4字节) + VR(2字节) + 保留(2字节) + 长度(4字节) + 值数据
            
            while (idx[0] < data.length) {
                // 检查是否还有足够的字节读取一个完整的元素头部
                if (idx[0] + 12 > data.length) {
                    break;
                }
                
                // 读取标签（4字节）
                int gtag = readUInt16LE(data, idx[0]);
                int etag = readUInt16LE(data, idx[0] + 2);
                idx[0] += 4;
                
                // 检查是否还是组号0x0002的元素
                if (gtag != 0x0002) {
                    // 已经不是文件头元素，回退位置并结束解析
                    idx[0] -= 4;
                    break;
                }
                
                // 读取VR（2字节）
                String vr = new String(data, idx[0], 2, "ASCII");
                idx[0] += 2;
                
                // 读取保留字段（2字节，应该为0）
                idx[0] += 2;
                
                // 读取长度（4字节）
                long length = readUInt32LE(data, idx[0]);
                idx[0] += 4;
                
                // 检查长度的有效性
                if (length < 0 || idx[0] + length > data.length) {
                    System.err.println("警告：数据元素长度无效: " + length);
                    break;
                }
                
                // 读取值数据
                byte[] value = new byte[(int)length];
                System.arraycopy(data, idx[0], value, 0, (int)length);
                idx[0] += (int)length;
                  // 创建数据元素
                DCMDataElement element = new DCMDataElement(syntax);
                element.gtag = (short) gtag;
                element.etag = (short) etag;
                element.vr = vr;
                element.vm = "1"; // 文件头元素通常是单值
                element.length = (int)length;
                element.value = value;
                
                // 根据字典获取元素名称
                try {
                    element.name = getElementName(gtag, etag);
                } catch (Exception e) {
                    element.name = String.format("(%04X,%04X)", gtag, etag);
                }
                
                // 创建VR解析器
                if (vr != null && !vr.isEmpty()) {
                    element.vrparser = VRFactory.getVRInstance(vr, syntax.isBE);
                }
                
                // 添加到items列表
                items.add(element);
                
                System.out.println("解析文件头元素: " + element.name + " [" + vr + "] 长度=" + length);
            }
            
            System.out.println("文件头解析完成，共解析了 " + items.size() + " 个元素");
            
        } catch (Exception e) {
            System.err.println("解析文件头时出错: " + e.getMessage());
            e.printStackTrace();
        }
        
        return this;
    }
      /**
     * 获取传输语法UID
     * @return 传输语法UID字符串
     */
    public String getTransferSyntaxUID() {
        // Transfer Syntax UID 的标签是 (0002,0010)
        Object value = GetValue(0x00020010);
        return value != null ? value.toString().trim() : "";
    }
      /**
     * 获取媒体存储SOP类UID
     * @return 媒体存储SOP类UID字符串
     */
    public String getMediaStorageSOPClassUID() {
        // Media Storage SOP Class UID 的标签是 (0002,0002)
        Object value = GetValue(0x00020002);
        return value != null ? value.toString().trim() : "";
    }
      /**
     * 获取媒体存储SOP实例UID
     * @return 媒体存储SOP实例UID字符串
     */
    public String getMediaStorageSOPInstanceUID() {
        // Media Storage SOP Instance UID 的标签是 (0002,0003)
        Object value = GetValue(0x00020003);
        return value != null ? value.toString().trim() : "";
    }
      /**
     * 获取实现类UID
     * @return 实现类UID字符串
     */
    public String getImplementationClassUID() {
        // Implementation Class UID 的标签是 (0002,0012)
        Object value = GetValue(0x00020012);
        return value != null ? value.toString().trim() : "";
    }
      /**
     * 获取实现版本名称
     * @return 实现版本名称字符串
     */
    public String getImplementationVersionName() {
        // Implementation Version Name 的标签是 (0002,0013)
        Object value = GetValue(0x00020013);
        return value != null ? value.toString().trim() : "";
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
     * @return 32位长整数值
     */
    private long readUInt32LE(byte[] data, int offset) {
        return (data[offset] & 0xFF) | 
               ((data[offset + 1] & 0xFF) << 8) | 
               ((data[offset + 2] & 0xFF) << 16) | 
               ((data[offset + 3] & 0xFF) << 24);
    }
    
    /**
     * 根据标签获取元素名称
     * @param gtag 组标签
     * @param etag 元素标签
     * @return 元素名称
     */
    private String getElementName(int gtag, int etag) {
        // 文件头常见元素名称映射
        int tag = gtag * 65536 + etag;
        switch (tag) {
            case 0x00020001: return "File Meta Information Version";
            case 0x00020002: return "Media Storage SOP Class UID";
            case 0x00020003: return "Media Storage SOP Instance UID";
            case 0x00020010: return "Transfer Syntax UID";
            case 0x00020012: return "Implementation Class UID";
            case 0x00020013: return "Implementation Version Name";
            case 0x00020016: return "Source Application Entity Title";
            case 0x00020100: return "Private Information Creator UID";
            case 0x00020102: return "Private Information";
            default: return String.format("(%04X,%04X)", gtag, etag);
        }
    }
    
    /**
     * 输出文件头信息摘要
     * @return 文件头信息字符串
     */
    public String getSummary() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== DICOM文件头信息 ===\n");
        sb.append("传输语法UID: ").append(getTransferSyntaxUID()).append("\n");
        sb.append("媒体存储SOP类UID: ").append(getMediaStorageSOPClassUID()).append("\n");
        sb.append("媒体存储SOP实例UID: ").append(getMediaStorageSOPInstanceUID()).append("\n");
        sb.append("实现类UID: ").append(getImplementationClassUID()).append("\n");
        sb.append("实现版本名称: ").append(getImplementationVersionName()).append("\n");
        sb.append("文件头元素总数: ").append(items.size()).append("\n");
        return sb.toString();
    }
}

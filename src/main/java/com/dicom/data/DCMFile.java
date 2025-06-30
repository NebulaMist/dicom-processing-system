package com.dicom.data;

import com.dicom.transfersyntax.ImplicitVRLittleEndian;
import com.dicom.transfersyntax.ExplicitVRLittleEndian;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * DICOM文件处理类
 * 继承DCMDataSet，用于处理完整的DICOM文件
 * 
 * DICOM文件结构：
 * 1. 128字节前导（通常为0）
 * 2. 4字节"DICM"标识符
 * 3. 文件头部元素（组号0x0002，显式VR）
 * 4. 数据集元素（根据传输语法确定格式）
 */
public class DCMFile extends DCMDataSet {
    
    /**
     * DICOM文件路径
     */
    private String filename;
    
    /**
     * 文件头对象
     */
    private DCMFileMeta filemeta;
    
    /**
     * 128字节前导数据
     */
    private byte[] preamble;
    
    /**
     * "DICM"标识符
     */
    private byte[] dicmPrefix;
    
    /**
     * 构造函数
     * @param filename DICOM文件路径
     */
    public DCMFile(String filename) {
        super(new ImplicitVRLittleEndian()); // 默认传输语法，后续会根据文件头更新
        this.filename = filename;
        this.filemeta = null;
        this.preamble = new byte[128];
        this.dicmPrefix = new byte[4];
    }
    
    /**
     * 构造函数（无文件名）
     */
    public DCMFile() {
        this("");
    }
    
    /**
     * 带传输语法的构造函数
     * @param ts 传输语法
     */
    public DCMFile(TransferSyntax ts) {
        super(ts);
        this.filename = "";
        // 头元素：显式VRLE传输语法
        this.filemeta = new DCMFileMeta(new ExplicitVRLittleEndian());
        this.preamble = new byte[128];
        this.dicmPrefix = new byte[4];
    }
    
    /**
     * 解析DICOM文件
     * @return 解析是否成功
     */
    public boolean Parse() {
        if (filename == null || filename.isEmpty()) {
            System.err.println("错误：未指定文件名");
            return false;
        }
        
        try {            // 读取文件到字节数组
            byte[] fileData = Files.readAllBytes(Paths.get(filename));
            int[] idx = {0};
            
            // 调用重写的Parse方法并检查结果
            DCMAbstractType result = Parse(fileData, idx);
            return result != null;
            
        } catch (IOException e) {
            System.err.println("读取文件失败: " + e.getMessage());
            return false;
        }
    }
      /**
     * 重写Parse方法，解析完整的DICOM文件
     * @param data 文件字节数据
     * @param idx 当前位置索引
     * @return 解析后的对象
     */
    @Override
    public DCMAbstractType Parse(byte[] data, int[] idx) {
        System.out.println("开始解析DICOM文件: " + filename);
        System.out.println("文件大小: " + data.length + " 字节");
        
        try {
            // 检查文件是否包含标准DICOM前导和DICM标识符
            boolean hasStandardHeader = checkForStandardDICOMHeader(data);
            
            if (hasStandardHeader) {
                // 标准DICOM文件解析
                System.out.println("检测到标准DICOM文件格式");
                return parseStandardDICOM(data, idx);
            } else {
                // 非标准DICOM文件解析（如CT文件）
                System.out.println("检测到非标准DICOM文件格式，尝试直接解析数据集");
                return parseNonStandardDICOM(data, idx);
            }
            
        } catch (Exception e) {
            System.err.println("解析DICOM文件时出错: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
      /**
     * 检查文件是否有标准DICOM头部
     * @param data 文件数据
     * @return 是否包含标准DICOM头部
     */
    private boolean checkForStandardDICOMHeader(byte[] data) {
        if (data.length < 132) {
            return false;
        }
        
        // 检查位置128-131是否是"DICM"
        String prefix = new String(data, 128, 4);
        return "DICM".equals(prefix);
    }
    
    /**
     * 解析标准DICOM文件
     * @param data 文件数据
     * @param idx 当前位置
     * @return 解析结果
     */
    private DCMAbstractType parseStandardDICOM(byte[] data, int[] idx) {
        // 第1步：读取128字节前导
        if (!parsePreamble(data, idx)) {
            return null;
        }
        
        // 第2步：读取"DICM"标识符
        if (!parseDICMPrefix(data, idx)) {
            return null;
        }
        
        // 第3步：解析文件头部元素（组号0x0002）
        if (!parseFileMeta(data, idx)) {
            return null;
        }
        
        // 第4步：根据文件头中的传输语法UID更新传输语法
        if (!updateTransferSyntax()) {
            return null;
        }
        
        // 第5步：解析数据集元素
        if (!parseDataset(data, idx)) {
            return null;
        }
        
        System.out.println("标准DICOM文件解析完成");
        System.out.println("数据集元素数量: " + items.size());
        
        return this;
    }
    
    /**
     * 解析非标准DICOM文件（直接从数据集开始）
     * @param data 文件数据
     * @param idx 当前位置
     * @return 解析结果
     */
    private DCMAbstractType parseNonStandardDICOM(byte[] data, int[] idx) {
        try {
            // 创建默认文件头信息
            filemeta = new DCMFileMeta(new ExplicitVRLittleEndian());
            
            // 尝试使用不同的传输语法解析
            boolean success = false;
            
            // 首先尝试Implicit VR Little Endian
            System.out.println("尝试使用 Implicit VR Little Endian 解析...");
            this.syntax = new ImplicitVRLittleEndian();
            idx[0] = 0; // 从文件开始解析
            
            try {
                super.Parse(data, idx);
                if (items.size() > 0) {
                    success = true;
                    System.out.println("使用 Implicit VR Little Endian 解析成功");
                }
            } catch (Exception e) {
                System.out.println("Implicit VR Little Endian 解析失败: " + e.getMessage());
            }
            
            // 如果失败，尝试Explicit VR Little Endian
            if (!success) {
                System.out.println("尝试使用 Explicit VR Little Endian 解析...");
                this.syntax = new ExplicitVRLittleEndian();
                idx[0] = 0; // 重置位置
                items.clear(); // 清除之前的尝试
                
                try {
                    super.Parse(data, idx);
                    if (items.size() > 0) {
                        success = true;
                        System.out.println("使用 Explicit VR Little Endian 解析成功");
                    }
                } catch (Exception e) {
                    System.out.println("Explicit VR Little Endian 解析失败: " + e.getMessage());
                }
            }
            
            if (success) {
                System.out.println("非标准DICOM文件解析完成");
                System.out.println("数据集元素数量: " + items.size());
                return this;
            } else {
                System.err.println("无法解析该文件，可能不是有效的DICOM文件");
                return null;
            }
            
        } catch (Exception e) {
            System.err.println("解析非标准DICOM文件时出错: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * 解析128字节前导
     * @param data 文件数据
     * @param idx 当前位置
     * @return 是否成功
     */
    private boolean parsePreamble(byte[] data, int[] idx) {
        if (data.length < 128) {
            System.err.println("错误：文件太小，无法包含完整的前导");
            return false;
        }
        
        // 读取128字节前导
        System.arraycopy(data, idx[0], preamble, 0, 128);
        idx[0] += 128;
        
        System.out.println("已读取128字节前导");
        return true;
    }
    
    /**
     * 解析"DICM"标识符
     * @param data 文件数据
     * @param idx 当前位置
     * @return 是否成功
     */
    private boolean parseDICMPrefix(byte[] data, int[] idx) {
        if (idx[0] + 4 > data.length) {
            System.err.println("错误：文件太小，无法包含DICM标识符");
            return false;
        }
        
        // 读取4字节"DICM"
        System.arraycopy(data, idx[0], dicmPrefix, 0, 4);
        idx[0] += 4;
        
        String prefix = new String(dicmPrefix);
        if (!"DICM".equals(prefix)) {
            System.err.println("错误：不是有效的DICOM文件，缺少DICM标识符。找到: " + prefix);
            return false;
        }
        
        System.out.println("找到DICM标识符");
        return true;
    }
    
    /**
     * 解析文件头部元素
     * @param data 文件数据
     * @param idx 当前位置
     * @return 是否成功
     */
    private boolean parseFileMeta(byte[] data, int[] idx) {
        try {
            // 创建文件头对象（总是使用显式VR小端序）
            filemeta = new DCMFileMeta(new ExplicitVRLittleEndian());
            
            // 解析文件头
            filemeta.Parse(data, idx);
            
            if (filemeta.getItemCount() == 0) {
                System.err.println("警告：未找到文件头元素");
                return false;
            }
            
            System.out.println("文件头解析完成:");
            System.out.println(filemeta.getSummary());
            
            return true;
            
        } catch (Exception e) {
            System.err.println("解析文件头时出错: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 根据文件头中的传输语法UID更新传输语法
     * @return 是否成功
     */
    private boolean updateTransferSyntax() {
        if (filemeta == null) {
            System.err.println("错误：文件头未解析");
            return false;
        }
        
        String transferSyntaxUID = filemeta.getTransferSyntaxUID();
        if (transferSyntaxUID.isEmpty()) {
            System.err.println("警告：文件头中未找到传输语法UID，使用默认值");
            transferSyntaxUID = "1.2.840.10008.1.2"; // Implicit VR Little Endian
        }
        
        System.out.println("传输语法UID: " + transferSyntaxUID);
        
        // 根据UID选择合适的传输语法
        switch (transferSyntaxUID) {
            case "1.2.840.10008.1.2":     // Implicit VR Little Endian
                this.syntax = new ImplicitVRLittleEndian();
                System.out.println("使用传输语法: Implicit VR Little Endian");
                break;
                
            case "1.2.840.10008.1.2.1":   // Explicit VR Little Endian
                this.syntax = new ExplicitVRLittleEndian();
                System.out.println("使用传输语法: Explicit VR Little Endian");
                break;
                
            default:
                System.err.println("警告：不支持的传输语法 " + transferSyntaxUID + "，使用默认值");
                this.syntax = new ImplicitVRLittleEndian();
                break;
        }
        
        return true;
    }
    
    /**
     * 解析数据集元素
     * @param data 文件数据
     * @param idx 当前位置
     * @return 是否成功
     */
    private boolean parseDataset(byte[] data, int[] idx) {
        try {
            System.out.println("开始解析数据集，当前位置: " + idx[0] + "/" + data.length);
            
            // 调用父类的Parse方法解析数据集
            super.Parse(data, idx);
            
            System.out.println("数据集解析完成，解析了 " + items.size() + " 个元素");
            return true;
            
        } catch (Exception e) {
            System.err.println("解析数据集时出错: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 获取文件名
     * @return 文件名
     */
    public String getFilename() {
        return filename;
    }
    
    /**
     * 设置文件名
     * @param filename 文件名
     */
    public void setFilename(String filename) {
        this.filename = filename;
    }
    
    /**
     * 获取文件头对象
     * @return 文件头对象
     */
    public DCMFileMeta getFileMeta() {
        return filemeta;
    }
    
    /**
     * 获取前导字节
     * @return 128字节前导
     */
    public byte[] getPreamble() {
        return preamble.clone();
    }
    
    /**
     * 获取DICM前缀
     * @return DICM字节数组
     */
    public byte[] getDICMPrefix() {
        return dicmPrefix.clone();
    }
    
    /**
     * 检查是否为有效的DICOM文件
     * @return 是否有效
     */
    public boolean isValidDICOMFile() {
        return filemeta != null && 
               "DICM".equals(new String(dicmPrefix)) &&
               !filemeta.getTransferSyntaxUID().isEmpty();
    }
      /**
     * 获取患者姓名
     * @return 患者姓名
     */
    public String getPatientName() {
        Object value = GetValue(0x00100010); // Patient's Name
        return value != null ? value.toString().trim() : "";
    }
      /**
     * 获取患者ID
     * @return 患者ID
     */
    public String getPatientID() {
        Object value = GetValue(0x00100020); // Patient ID
        return value != null ? value.toString().trim() : "";
    }
      /**
     * 获取检查日期
     * @return 检查日期
     */
    public String getStudyDate() {
        Object value = GetValue(0x00080020); // Study Date
        return value != null ? value.toString().trim() : "";
    }
      /**
     * 获取模态
     * @return 模态（CT, MR, US等）
     */
    public String getModality() {
        Object value = GetValue(0x00080060); // Modality
        return value != null ? value.toString().trim() : "";
    }
      /**
     * 获取图像行数
     * @return 行数
     */
    public int getRows() {
        Object value = GetValue(0x00280010); // Rows
        try {
            if (value instanceof Integer) {
                return (Integer) value;
            } else if (value instanceof String) {
                return Integer.parseInt(((String) value).trim());
            }
            return 0;
        } catch (Exception e) {
            return 0;
        }
    }
      /**
     * 获取图像列数
     * @return 列数
     */
    public int getColumns() {
        Object value = GetValue(0x00280011); // Columns
        try {
            if (value instanceof Integer) {
                return (Integer) value;
            } else if (value instanceof String) {
                return Integer.parseInt(((String) value).trim());
            }
            return 0;
        } catch (Exception e) {
            return 0;
        }
    }
    
    /**
     * 输出文件信息摘要
     * @return 文件信息字符串
     */
    public String getFileSummary() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== DICOM文件信息摘要 ===\n");
        sb.append("文件名: ").append(filename).append("\n");
        sb.append("有效DICOM文件: ").append(isValidDICOMFile() ? "是" : "否").append("\n");
        
        if (filemeta != null) {
            sb.append("\n").append(filemeta.getSummary());
        }
        
        sb.append("\n=== 主要数据元素 ===\n");
        sb.append("患者姓名: ").append(getPatientName()).append("\n");
        sb.append("患者ID: ").append(getPatientID()).append("\n");
        sb.append("检查日期: ").append(getStudyDate()).append("\n");
        sb.append("模态: ").append(getModality()).append("\n");
        sb.append("图像尺寸: ").append(getRows()).append(" x ").append(getColumns()).append("\n");
        sb.append("数据集元素总数: ").append(items.size()).append("\n");
        
        return sb.toString();
    }
    
    /**
     * 保存解析结果到文本文件
     * @param outputPath 输出文件路径
     * @return 是否成功
     */
    public boolean saveReport(String outputPath) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(outputPath))) {
            writer.println(getFileSummary());
            writer.println("\n=== 完整数据元素列表 ===");
            writer.println(ToString(""));
            
            if (filemeta != null) {
                writer.println("\n=== 文件头元素列表 ===");
                writer.println(filemeta.ToString(""));
            }
            
            System.out.println("解析报告已保存到: " + outputPath);
            return true;
            
        } catch (IOException e) {
            System.err.println("保存报告失败: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 保存dcm文件的Save方法
     * @param filename 保存的文件名
     * @param isUndefinedLength 是否使用未定义长度
     * @return 是否保存成功
     */
    public boolean Save(String filename, boolean isUndefinedLength) {
        try {
            try (FileOutputStream fos = new FileOutputStream(filename)) {
                // 写入128字节前导符（全0）
                fos.write(new byte[128], 0, 128);
                
                // 写入DICM标志
                fos.write("DICM".getBytes());
                
                // 简化：跳过文件头，直接写入数据集
                // 编码数据集
                byte[] data = Encode(isUndefinedLength);
                fos.write(data);
                
                fos.close();
                return true;
            }
        } catch (IOException e) {
            System.err.println("保存DICOM文件失败: " + e.getMessage());
            return false;
        } catch (Exception e) {
            System.err.println("编码DICOM文件失败: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}

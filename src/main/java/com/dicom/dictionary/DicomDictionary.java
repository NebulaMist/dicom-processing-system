package com.dicom.dictionary;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.regex.Pattern;

/**
 * DICOM数据字典类
 * 用于加载和查询DICOM数据字典
 */
public class DicomDictionary {
    private HashMap<String, DicomDictionaryEntry> dict;
    
    /**
     * 默认构造函数
     */
    public DicomDictionary() {
        this.dict = new HashMap<>();
    }
    
    /**
     * 构造函数，加载指定路径的字典文件
     */
    public DicomDictionary(String filePath) {
        this.dict = new HashMap<>();
        loadDictionary(filePath);
    }
      /**
     * 从类路径加载字典
     */
    public static DicomDictionary loadFromClasspath() {
        DicomDictionary dict = new DicomDictionary();
        dict.loadDictionaryFromResource();
        return dict;
    }
      /**
     * 从资源文件加载字典
     */
    public void loadDictionaryFromResource() {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        InputStream is = null;
        BufferedReader reader = null;
        
        try {
            is = classLoader.getResourceAsStream("com/dicom/dictionary/dicom.dic");
            if (is == null) {
                throw new RuntimeException("无法找到资源文件: com/dicom/dictionary/dicom.dic");
            }
            
            reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            
            String line;
            int lineNumber = 0;
            
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                
                // 跳过空行和注释行
                if (line.trim().isEmpty() || line.trim().startsWith("//")) {
                    continue;
                }
                
                try {
                    // 清理行中的非法字符
                    line = cleanLine(line);
                    
                    // 解析一行数据
                    DicomDictionaryEntry entry = parseLine(line);
                    if (entry != null) {
                        // 使用完整标签作为key
                        String key = entry.getFullTag();
                        dict.put(key, entry);
                    }
                } catch (Exception e) {
                    System.err.println("解析第" + lineNumber + "行时出错: " + line);
                    System.err.println("错误: " + e.getMessage());
                    // 继续处理下一行
                }
            }
            
            System.out.println("成功加载 " + dict.size() + " 个DICOM字典条目");
            
        } catch (IOException e) {
            throw new RuntimeException("读取字典资源文件失败", e);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    System.err.println("关闭reader时出错: " + e.getMessage());
                }
            }
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    System.err.println("关闭输入流时出错: " + e.getMessage());
                }
            }
        }
    }
      /**
     * 加载数据字典文件
     */
    public void loadDictionary(String filePath) {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(filePath), "UTF-8"));
            
            String line;
            int lineNumber = 0;
            
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                
                // 跳过空行和注释行
                if (line.trim().isEmpty() || line.trim().startsWith("//")) {
                    continue;
                }
                
                try {
                    // 清理行中的非法字符
                    line = cleanLine(line);
                    
                    // 解析一行数据
                    DicomDictionaryEntry entry = parseLine(line);
                    if (entry != null) {
                        // 使用完整标签作为key
                        String key = entry.getFullTag();
                        dict.put(key, entry);
                    }
                } catch (Exception e) {
                    System.err.println("解析第" + lineNumber + "行时出错: " + line);
                    System.err.println("错误: " + e.getMessage());
                    // 继续处理下一行
                }
            }
            
            System.out.println("成功加载 " + dict.size() + " 个DICOM字典条目");
            
        } catch (IOException e) {
            throw new RuntimeException("读取字典文件失败: " + filePath, e);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    System.err.println("关闭文件时出错: " + e.getMessage());
                }
            }
        }
    }
    
    /**
     * 清理行中的非法字符
     */
    private String cleanLine(String line) {
        // 移除不可见字符和特殊字符，但保留制表符和空格
        StringBuilder cleaned = new StringBuilder();
        for (char c : line.toCharArray()) {
            if (c == '\t' || c == ' ' || (c >= 32 && c <= 126) || c > 127) {
                cleaned.append(c);
            }
        }
        return cleaned.toString();
    }
    
    /**
     * 解析一行数据
     */
    private DicomDictionaryEntry parseLine(String line) {
        try {
            // 按制表符分割
            String[] parts = line.split("\t");
            
            if (parts.length < 4) {
                return null; // 数据不完整
            }
            
            // 解析标签 (xxxx,xxxx)
            String tagStr = parts[0].trim();
            if (!tagStr.startsWith("(") || !tagStr.endsWith(")")) {
                return null;
            }
            
            tagStr = tagStr.substring(1, tagStr.length() - 1);
            String[] tagParts = tagStr.split(",");
            if (tagParts.length != 2) {
                return null;
            }
            
            String gtag = tagParts[0].trim();
            String etag = tagParts[1].trim();
            
            String name = parts.length > 1 ? parts[1].trim() : "";
            String keyword = parts.length > 2 ? parts[2].trim() : "";
            String vr = parts.length > 3 ? parts[3].trim() : "";
            String vm = parts.length > 4 ? parts[4].trim() : "";
            String retired = parts.length > 5 ? parts[5].trim() : "";
            
            // 清理关键字中的特殊字符
            keyword = keyword.replaceAll("[^a-zA-Z0-9_]", "");
            
            return new DicomDictionaryEntry(gtag, etag, name, keyword, vr, vm, retired);
            
        } catch (Exception e) {
            System.err.println("解析行时出错: " + line + ", 错误: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * 根据组号和元素号查询数据元素
     */
    public DicomDictionaryEntry lookup(String gtag, String etag) {
        String key = "(" + gtag + "," + etag + ")";
        return dict.get(key);
    }
    
    /**
     * 根据组号和元素号查询数据元素（十六进制整数参数）
     */
    public DicomDictionaryEntry lookup(int gtag, int etag) {
        String gtagStr = String.format("%04X", gtag);
        String etagStr = String.format("%04X", etag);
        return lookup(gtagStr, etagStr);
    }
    
    /**
     * 根据完整标签查询数据元素
     */
    public DicomDictionaryEntry lookup(String fullTag) {
        return dict.get(fullTag);
    }
    
    /**
     * 获取指定标签的VR
     */
    public String getVR(String gtag, String etag) {
        DicomDictionaryEntry entry = lookup(gtag, etag);
        return entry != null ? entry.getVr() : null;
    }
    
    /**
     * 获取指定标签的名称
     */
    public String getName(String gtag, String etag) {
        DicomDictionaryEntry entry = lookup(gtag, etag);
        return entry != null ? entry.getName() : null;
    }
    
    /**
     * 获取指定标签的关键字
     */
    public String getKeyword(String gtag, String etag) {
        DicomDictionaryEntry entry = lookup(gtag, etag);
        return entry != null ? entry.getKeyword() : null;
    }
    
    /**
     * 获取指定标签的VM
     */
    public String getVM(String gtag, String etag) {
        DicomDictionaryEntry entry = lookup(gtag, etag);
        return entry != null ? entry.getVm() : null;
    }
    
    /**
     * 检查指定标签是否已退役
     */
    public boolean isRetired(String gtag, String etag) {
        DicomDictionaryEntry entry = lookup(gtag, etag);
        if (entry == null) return false;
        String retired = entry.getRetired();
        return retired != null && !retired.trim().isEmpty() && 
               (retired.contains("RET") || retired.toLowerCase().contains("retired"));
    }
    
    /**
     * 获取字典中条目的总数
     */
    public int getSize() {
        return dict.size();
    }
    
    /**
     * 获取字典的HashMap引用（只读访问）
     */
    public HashMap<String, DicomDictionaryEntry> getDict() {
        return new HashMap<>(dict); // 返回副本以保护内部数据
    }
      /**
     * 打印字典统计信息
     */
    public void printStatistics() {
        System.out.println("DICOM字典统计:");
        System.out.println("总条目数: " + dict.size());
        
        // 统计已退役条目数量
        int retiredCount = 0;
        for (DicomDictionaryEntry entry : dict.values()) {
            String retired = entry.getRetired();
            if (retired != null && !retired.trim().isEmpty() && 
                (retired.contains("RET") || retired.toLowerCase().contains("retired"))) {
                retiredCount++;
            }
        }
            
        System.out.println("已退役条目数: " + retiredCount);
        System.out.println("活跃条目数: " + (dict.size() - retiredCount));
    }
    
    /**
     * 生成DicomTags.java文件
     * 遍历字典中的每一条数据元素，生成常量定义
     */
    public void GenerateDicomTags() {
        try {
            FileWriter fw = new FileWriter("DicomTags.java");
            fw.write("package com.dicom.dictionary;\n\n");
            fw.write("/**\n");
            fw.write(" * DICOM标签常量定义类\n");
            fw.write(" * 自动生成，包含所有DICOM数据元素的常量定义\n");
            fw.write(" */\n");
            fw.write("public final class DicomTags {\n");
            
            dict.forEach((key, value) -> {
                try {
                    String keyword = value.getKeyword();
                    if (keyword != null && !keyword.trim().isEmpty()) {
                        // 清理关键字，移除问号和其他特殊字符
                        keyword = keyword.replace("?", "").trim();
                        
                        if (!keyword.isEmpty()) {
                            fw.write("    public static final int " + keyword);
                            
                            // 处理标签，将gtag和etag组合成一个整数
                            String gtag = value.getGtag().replace("x", "F");
                            String etag = value.getEtag().replace("x", "F");
                            
                            fw.write(" = 0x" + gtag + etag + ";\n");
                        }
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
            
            fw.write("}\n");
            fw.close();
            
            System.out.println("成功生成 DicomTags.java 文件");
            
        } catch (Exception exception) {
            System.err.println("生成DicomTags.java文件时出错: " + exception.getMessage());
            exception.printStackTrace();
        }
    }
}

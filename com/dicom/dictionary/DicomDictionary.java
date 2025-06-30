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
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        try {
            Path packagePath = Paths.get(classLoader.getResource("com/dicom/dictionary").toURI());
            return new DicomDictionary(packagePath + "/dicom.dic");
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
    
    /**
     * 加载数据字典文件
     */
    public void loadDictionary(String filePath) {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(filePath), "UTF-8"))) {
            
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
        
        long retiredCount = dict.values().stream()
            .filter(entry -> {
                String retired = entry.getRetired();
                return retired != null && !retired.trim().isEmpty() && 
                       (retired.contains("RET") || retired.toLowerCase().contains("retired"));
            })
            .count();
            
        System.out.println("已退役条目数: " + retiredCount);
        System.out.println("活跃条目数: " + (dict.size() - retiredCount));
    }
}

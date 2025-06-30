package com.dicom.dictionary;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;

/**
 * 简化的DICOM字典测试主程序
 */
public class SimpleDicomTest {
    public static void main(String[] args) {
        System.out.println("=== 简化的DICOM字典测试程序 ===");
        
        try {
            // 测试是否能找到资源文件
            InputStream is = SimpleDicomTest.class.getClassLoader()
                .getResourceAsStream("com/dicom/dictionary/dicom.dic");
            
            if (is == null) {
                System.err.println("错误：无法找到dicom.dic文件");
                System.err.println("请确保文件位于src/main/resources/com/dicom/dictionary/目录下");
                return;
            }
            
            System.out.println("成功找到dicom.dic文件");
            
            // 读取前几行看看内容
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"))) {
                System.out.println("文件内容预览：");
                String line;
                int count = 0;
                while ((line = reader.readLine()) != null && count < 5) {
                    if (!line.trim().isEmpty() && !line.trim().startsWith("//")) {
                        System.out.println("第" + (count + 1) + "行: " + line);
                        count++;
                    }
                }
            }
            
            // 测试字典加载
            System.out.println("\n=== 测试字典加载 ===");
            DicomDictionary dict = DicomDictionary.loadFromClasspath();
            dict.printStatistics();
            
            // 测试查询
            System.out.println("\n=== 测试查询功能 ===");
            testSimpleQuery(dict);
            
        } catch (Exception e) {
            System.err.println("程序执行出错: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static void testSimpleQuery(DicomDictionary dict) {
        // 测试一些常见的DICOM标签
        String[][] testTags = {
            {"0008", "0018", "SOP Instance UID"},
            {"0010", "0010", "Patient's Name"},
            {"0020", "000D", "Study Instance UID"},
            {"7FE0", "0010", "Pixel Data"}
        };
        
        for (String[] tag : testTags) {
            System.out.println("\n查询标签 (" + tag[0] + "," + tag[1] + ") - " + tag[2] + ":");
            DicomDictionaryEntry entry = dict.lookup(tag[0], tag[1]);
            if (entry != null) {
                System.out.println("  找到: " + entry.getName());
                System.out.println("  VR: " + entry.getVr());
                System.out.println("  VM: " + entry.getVm());
            } else {
                System.out.println("  未找到该标签");
            }
        }
    }
}

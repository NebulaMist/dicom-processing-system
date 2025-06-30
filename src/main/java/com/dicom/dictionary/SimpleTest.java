package com.dicom.dictionary;

import java.io.*;

/**
 * 最简单的测试类，验证基本功能
 */
public class SimpleTest {
    public static void main(String[] args) {
        System.out.println("=== 简单测试程序 ===");
        
        try {
            // 测试1：创建DicomDictionaryEntry
            System.out.println("测试1：创建DicomDictionaryEntry对象");
            DicomDictionaryEntry entry = new DicomDictionaryEntry(
                "0008", "0018", "SOP Instance UID", "SOPInstanceUID", "UI", "1", ""
            );
            System.out.println("创建成功: " + entry.toString());
            
            // 测试2：创建DicomDictionary
            System.out.println("\n测试2：创建DicomDictionary对象");
            DicomDictionary dict = new DicomDictionary();
            System.out.println("字典创建成功，当前大小: " + dict.getSize());
            
            // 测试3：检查资源文件是否存在
            System.out.println("\n测试3：检查资源文件");
            InputStream is = SimpleTest.class.getClassLoader()
                .getResourceAsStream("com/dicom/dictionary/dicom.dic");
            if (is != null) {
                System.out.println("资源文件找到了！");
                is.close();
            } else {
                System.out.println("资源文件未找到！");
            }
            
            System.out.println("\n=== 基本测试完成 ===");
            
        } catch (Exception e) {
            System.err.println("测试出错: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

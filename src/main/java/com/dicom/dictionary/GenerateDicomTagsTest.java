package com.dicom.dictionary;

/**
 * 测试生成DicomTags文件的主程序
 */
public class GenerateDicomTagsTest {
    public static void main(String[] args) {
        try {
            // 加载DICOM字典
            System.out.println("正在加载DICOM字典...");
            DicomDictionary dict = DicomDictionary.loadFromClasspath();
            
            System.out.println("字典加载完成，包含 " + dict.getSize() + " 个条目");
            
            // 生成DicomTags.java文件
            System.out.println("正在生成DicomTags.java文件...");
            dict.GenerateDicomTags();
            
            System.out.println("DicomTags.java文件生成完成!");
            
        } catch (Exception e) {
            System.err.println("生成过程中出现错误: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

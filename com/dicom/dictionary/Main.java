package com.dicom.dictionary;

/**
 * DICOM字典主程序入口
 */
public class Main {
    public static void main(String[] args) {
        System.out.println("=== DICOM字典程序启动 ===");
        
        try {
            // 创建字典实例
            DicomDictionary dict = new DicomDictionary();
            
            // 尝试加载字典文件
            String dictPath = "com/dicom/dictionary/dicom.dic";
            System.out.println("正在加载字典文件: " + dictPath);
            
            dict.loadDictionary(dictPath);
            
            // 显示加载结果
            dict.printStatistics();
            
            // 测试几个查询
            System.out.println("\n=== 测试查询功能 ===");
            
            // 测试查询Patient's Name
            testQuery(dict, "0010", "0010", "Patient's Name");
            
            // 测试查询SOP Instance UID  
            testQuery(dict, "0008", "0018", "SOP Instance UID");
            
            // 测试查询Pixel Data
            testQuery(dict, "7FE0", "0010", "Pixel Data");
            
            System.out.println("\n=== 程序执行完毕 ===");
            
        } catch (Exception e) {
            System.err.println("程序执行出错: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 测试查询功能
     */
    private static void testQuery(DicomDictionary dict, String gtag, String etag, String description) {
        System.out.println("\n查询测试: " + description);
        System.out.println("标签: (" + gtag + "," + etag + ")");
        
        DicomDictionaryEntry entry = dict.lookup(gtag, etag);
        if (entry != null) {
            System.out.println("结果: " + entry);
        } else {
            System.out.println("未找到该标签");
        }
    }
}

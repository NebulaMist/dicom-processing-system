package com.dicom.dictionary;

/**
 * DICOM字典主程序入口
 */
public class Main {
    public static void main(String[] args) {
        System.out.println("=== DICOM字典程序启动 ===");
        
        try {            // 方法1：使用资源文件加载（推荐）
            DicomDictionary dict;
            try {
                dict = DicomDictionary.loadFromClasspath();
                System.out.println("成功从classpath加载字典");
            } catch (Exception e) {
                System.out.println("从classpath加载失败，尝试直接路径加载: " + e.getMessage());
                // 备用方法：直接指定文件路径
                dict = new DicomDictionary();
                String dictPath = "src/main/resources/com/dicom/dictionary/dicom.dic";
                dict.loadDictionary(dictPath);
                System.out.println("使用直接路径加载成功");
            }
            
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

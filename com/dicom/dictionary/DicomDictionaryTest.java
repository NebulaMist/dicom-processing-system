package com.dicom.dictionary;

/**
 * DICOM字典测试类
 */
public class DicomDictionaryTest {
    public static void main(String[] args) {        try {
            // 测试加载字典
            System.out.println("=== 测试DICOM字典加载 ===");
            
            // 方法1：使用您要求的classpath方式加载
            DicomDictionary dict;
            try {
                dict = DicomDictionary.loadFromClasspath();
                System.out.println("成功从classpath加载字典");
            } catch (Exception e) {
                System.out.println("从classpath加载失败，尝试直接路径加载: " + e.getMessage());
                // 备用方法：直接指定文件路径
                String dictPath = "com/dicom/dictionary/dicom.dic";
                dict = new DicomDictionary(dictPath);
                System.out.println("使用直接路径加载成功");
            }
            
            // 打印统计信息
            dict.printStatistics();
            
            // 测试查询功能
            System.out.println("\n=== 测试查询功能 ===");
            
            // 测试一些已知的标签
            testLookup(dict, "0008", "0018", "SOP Instance UID");
            testLookup(dict, "0010", "0010", "Patient's Name");
            testLookup(dict, "0020", "000D", "Study Instance UID");
            testLookup(dict, "7FE0", "0010", "Pixel Data");
            
            // 测试不存在的标签
            testLookup(dict, "FFFF", "FFFF", "不存在的标签");
            
            // 测试使用整数参数的查询
            System.out.println("\n=== 测试整数参数查询 ===");
            DicomDictionaryEntry entry = dict.lookup(0x0008, 0x0018);
            if (entry != null) {
                System.out.println("查询(0008,0018): " + entry);
            } else {
                System.out.println("未找到标签(0008,0018)");
            }
            
        } catch (Exception e) {
            System.err.println("测试失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static void testLookup(DicomDictionary dict, String gtag, String etag, String description) {
        System.out.println("\n测试查询: " + description);
        System.out.println("标签: (" + gtag + "," + etag + ")");
        
        DicomDictionaryEntry entry = dict.lookup(gtag, etag);
        if (entry != null) {
            System.out.println("查询结果: " + entry);
            System.out.println("VR: " + entry.getVr());
            System.out.println("VM: " + entry.getVm());
            System.out.println("是否已退役: " + dict.isRetired(gtag, etag));
        } else {
            System.out.println("未找到该标签");
        }
    }
}

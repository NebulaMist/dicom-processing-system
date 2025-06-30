package com.dicom.vr;

/**
 * VRFactory类的享元模式测试
 * 验证实例方法的享元模式实现
 */
public class VRFactoryTest {
    
    public static void main(String[] args) {
        System.out.println("=== VRFactory享元模式动态创建测试 ===\n");
        
        // 测试基本享元模式功能
        testBasicFlyweightPattern();
        
        // 测试不同字节序的工厂实例
        testDifferentEndianFactories();
        
        // 测试VR解析功能
        testVRParsing();
        
        // 测试所有VR类型创建
        testAllVRTypes();
        
        System.out.println("\n=== VRFactory测试完成 ===");
    }
    
    /**
     * 测试基本享元模式功能
     */
    private static void testBasicFlyweightPattern() {
        System.out.println("1. 基本享元模式测试:");
        
        // 创建Little Endian工厂
        VRFactory factoryLE = new VRFactory(false);
        
        // 多次获取同一类型的VR实例，应该返回同一个对象
        VRBase ul1 = factoryLE.GetVR("UL");
        VRBase ul2 = factoryLE.GetVR("UL");
        VRBase ul3 = factoryLE.GetVR("ul"); // 测试大小写不敏感
        
        System.out.println("UL实例1和实例2是否相同: " + (ul1 == ul2));
        System.out.println("UL实例1和实例3是否相同: " + (ul1 == ul3));
        System.out.println("UL实例字节序设置: " + (ul1.isBE ? "Big Endian" : "Little Endian"));
        System.out.println("工厂共享池大小: " + factoryLE.getVRPoolSize());
        System.out.println();
    }
    
    /**
     * 测试不同字节序的工厂实例
     */
    private static void testDifferentEndianFactories() {
        System.out.println("2. 不同字节序工厂测试:");
        
        // 创建两个不同字节序的工厂
        VRFactory factoryLE = new VRFactory(false);  // Little Endian
        VRFactory factoryBE = new VRFactory(true);   // Big Endian
        
        VRBase ulLE = factoryLE.GetVR("UL");
        VRBase ulBE = factoryBE.GetVR("UL");
        
        System.out.println("Little Endian工厂的UL实例字节序: " + (ulLE.isBE ? "Big Endian" : "Little Endian"));
        System.out.println("Big Endian工厂的UL实例字节序: " + (ulBE.isBE ? "Big Endian" : "Little Endian"));
        System.out.println("两个工厂创建的UL实例是否不同: " + (ulLE != ulBE));
        System.out.println("Little Endian工厂共享池大小: " + factoryLE.getVRPoolSize());
        System.out.println("Big Endian工厂共享池大小: " + factoryBE.getVRPoolSize());
        System.out.println();
    }
    
    /**
     * 测试VR解析功能
     */
    private static void testVRParsing() {
        System.out.println("3. VR解析功能测试:");
        
        VRFactory factory = new VRFactory(false);  // Little Endian
        
        // 测试UL（32位无符号长整型）
        byte[] ulData = {0x78, 0x56, 0x34, 0x12};  // Little Endian: 0x12345678
        VRBase ul = factory.GetVR("UL");
        Long ulValue = ul.GetValue(ulData, 0);
        
        System.out.println("UL解析测试:");
        System.out.println("  输入字节: [0x78, 0x56, 0x34, 0x12]");
        System.out.println("  解析结果: " + ulValue + " (0x" + Long.toHexString(ulValue) + ")");
        System.out.println("  预期值: 305419896 (0x12345678)");
        System.out.println("  测试结果: " + (ulValue.longValue() == 0x12345678L ? "通过" : "失败"));
        
        // 测试字符串类型
        byte[] stringData = "DICOM".getBytes();
        VRBase ae = factory.GetVR("AE");
        String aeValue = ae.GetValue(stringData, 0);
        
        System.out.println("AE字符串解析测试:");
        System.out.println("  输入字节: " + java.util.Arrays.toString(stringData));
        System.out.println("  解析结果: '" + aeValue + "'");
        System.out.println("  预期值: 'DICOM'");
        System.out.println("  测试结果: " + ("DICOM".equals(aeValue) ? "通过" : "失败"));
        System.out.println();
    }
    
    /**
     * 测试所有VR类型创建
     */
    private static void testAllVRTypes() {
        System.out.println("4. 所有VR类型创建测试:");
        
        VRFactory factory = new VRFactory(false);
        
        // 所有标准DICOM VR类型
        String[] allVRTypes = {
            "AE", "AS", "AT", "CS", "DA", "DS", "DT", "FD", "FL", "IS",
            "LO", "LT", "OB", "OD", "OF", "OL", "OW", "PN", "SH", "SL",
            "SQ", "SS", "ST", "TM", "UC", "UI", "UL", "UN", "UR", "US", "UT"
        };
        
        int successCount = 0;
        for (String vrType : allVRTypes) {
            VRBase vr = factory.GetVR(vrType);
            if (vr != null) {
                successCount++;
                System.out.println("  " + vrType + ": " + vr.getClass().getSimpleName() + 
                                 " (长VR=" + vr.isLongVR + 
                                 ", 字节序=" + (vr.isBE ? "BE" : "LE") + ")");
            } else {
                System.out.println("  " + vrType + ": 创建失败");
            }
        }
        
        System.out.println("成功创建VR类型数量: " + successCount + "/" + allVRTypes.length);
        System.out.println("工厂共享池最终大小: " + factory.getVRPoolSize());
        
        // 测试未知VR类型
        VRBase unknown = factory.GetVR("XX");
        System.out.println("未知VR类型'XX'返回: " + 
                          (unknown != null ? unknown.getClass().getSimpleName() : "null"));
        System.out.println("应该返回UN类型: " + (unknown instanceof UN ? "通过" : "失败"));
    }
}

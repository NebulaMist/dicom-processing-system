package com.dicom.vr;

/**
 * VR类测试
 * 验证享元模式和各种VR类型的解析功能
 */
public class VRTest {
    
    public static void main(String[] args) {
        System.out.println("=== DICOM VR类享元模式测试 ===\n");
        
        // 测试享元模式
        testFlyweightPattern();
        
        // 测试各种VR类型的解析
        testVRParsing();
        
        // 测试VR工厂
        testVRFactory();
        
        System.out.println("\n=== VR测试完成 ===");
    }
    
    /**
     * 测试享元模式
     */
    private static void testFlyweightPattern() {
        System.out.println("1. 享元模式测试:");
        
        // 多次获取同一类型的VR实例，应该返回同一个对象
        VRBase ul1 = VRFactory.getVRInstance("UL", false);
        VRBase ul2 = VRFactory.getVRInstance("UL", false);
        VRBase ul3 = VRFactory.getVRInstance("ul", false); // 测试大小写不敏感
        
        System.out.println("UL实例1和实例2是否相同: " + (ul1 == ul2));
        System.out.println("UL实例1和实例3是否相同: " + (ul1 == ul3));
        
        // 不同字节序应该是不同的实例
        VRBase ulBE = VRFactory.getVRInstance("UL", true);
        VRBase ulLE = VRFactory.getVRInstance("UL", false);
        
        System.out.println("UL大端和小端实例是否不同: " + (ulBE != ulLE));
        System.out.println("缓存池大小: " + VRFactory.getPoolSize());
        System.out.println();
    }
    
    /**
     * 测试各种VR类型的解析
     */
    private static void testVRParsing() {
        System.out.println("2. VR类型解析测试:");
        
        // 测试UL（32位无符号长整型）- Little Endian
        testULParsing();
        
        // 测试US（16位无符号短整型）
        testUSParsing();
        
        // 测试FL（32位单精度浮点数）
        testFLParsing();
        
        // 测试字符串类型
        testStringParsing();
        
        // 测试AT（属性标签）
        testATParsing();
        
        // 测试长VR类型标识
        testLongVRIdentification();
        
        System.out.println();
    }
    
    /**
     * 测试UL类型解析
     */
    private static void testULParsing() {
        // Little Endian: 0x12345678 -> 0x78, 0x56, 0x34, 0x12
        byte[] ulData = {0x78, 0x56, 0x34, 0x12};
        
        VRBase ul = VRFactory.getVRInstance("UL", false);
        Long value = ul.GetValue(ulData, 0);
        
        System.out.println("UL解析测试 (Little Endian):");
        System.out.println("  输入字节: [0x78, 0x56, 0x34, 0x12]");
        System.out.println("  解析结果: " + value + " (0x" + Long.toHexString(value) + ")");
        System.out.println("  预期值: 305419896 (0x12345678)");
        System.out.println("  测试结果: " + (value.longValue() == 0x12345678L ? "通过" : "失败"));
    }
    
    /**
     * 测试US类型解析
     */
    private static void testUSParsing() {
        // Big Endian: 0x1234 -> 0x12, 0x34
        byte[] usData = {0x12, 0x34};
        
        VRBase us = VRFactory.getVRInstance("US", true);
        Integer value = us.GetValue(usData, 0);
        
        System.out.println("US解析测试 (Big Endian):");
        System.out.println("  输入字节: [0x12, 0x34]");
        System.out.println("  解析结果: " + value + " (0x" + Integer.toHexString(value) + ")");
        System.out.println("  预期值: 4660 (0x1234)");
        System.out.println("  测试结果: " + (value.intValue() == 0x1234 ? "通过" : "失败"));
    }
    
    /**
     * 测试FL类型解析
     */
    private static void testFLParsing() {
        // 3.14f的IEEE 754表示: 0x40490FDB
        // Little Endian: 0xDB, 0x0F, 0x49, 0x40
        byte[] flData = {(byte)0xDB, 0x0F, 0x49, 0x40};
        
        VRBase fl = VRFactory.getVRInstance("FL", false);
        Float value = fl.GetValue(flData, 0);
        
        System.out.println("FL解析测试 (Little Endian):");
        System.out.println("  输入字节: [0xDB, 0x0F, 0x49, 0x40]");
        System.out.println("  解析结果: " + value);
        System.out.println("  预期值: 约3.14");
        System.out.println("  测试结果: " + (Math.abs(value - 3.14f) < 0.01f ? "通过" : "失败"));
    }
    
    /**
     * 测试字符串类型解析
     */
    private static void testStringParsing() {
        byte[] stringData = "HELLO".getBytes();
        
        VRBase ae = VRFactory.getVRInstance("AE", false);
        String value = ae.GetValue(stringData, 0);
        
        System.out.println("AE字符串解析测试:");
        System.out.println("  输入字节: " + java.util.Arrays.toString(stringData));
        System.out.println("  解析结果: '" + value + "'");
        System.out.println("  预期值: 'HELLO'");
        System.out.println("  测试结果: " + ("HELLO".equals(value) ? "通过" : "失败"));
    }
    
    /**
     * 测试AT类型解析
     */
    private static void testATParsing() {
        // Group: 0x0008, Element: 0x0010 -> Little Endian: 0x08, 0x00, 0x10, 0x00
        byte[] atData = {0x08, 0x00, 0x10, 0x00};
        
        VRBase at = VRFactory.getVRInstance("AT", false);
        String value = at.GetValue(atData, 0);
        
        System.out.println("AT标签解析测试 (Little Endian):");
        System.out.println("  输入字节: [0x08, 0x00, 0x10, 0x00]");
        System.out.println("  解析结果: " + value);
        System.out.println("  预期值: (0008,0010)");
        System.out.println("  测试结果: " + ("(0008,0010)".equals(value) ? "通过" : "失败"));
    }
    
    /**
     * 测试长VR类型标识
     */
    private static void testLongVRIdentification() {
        System.out.println("长VR类型标识测试:");
        
        String[] longVRTypes = {"OB", "OF", "OD", "OW", "OL", "SQ", "UT", "UN", "UR", "UC"};
        String[] shortVRTypes = {"AE", "AS", "AT", "CS", "DA", "DS", "DT", "FD", "FL", "IS",
                                 "LO", "LT", "PN", "SH", "SL", "SS", "ST", "TM", "UI", "UL", "US"};
        
        System.out.println("  长VR类型检查:");
        for (String vrType : longVRTypes) {
            boolean isLong = VRFactory.isLongVR(vrType);
            VRBase vr = VRFactory.getVRInstance(vrType, false);
            System.out.println("    " + vrType + ": 工厂检查=" + isLong + 
                             ", 实例检查=" + vr.isLongVR + 
                             " (" + (isLong && vr.isLongVR ? "通过" : "失败") + ")");
        }
        
        System.out.println("  短VR类型检查:");
        for (String vrType : shortVRTypes) {
            boolean isLong = VRFactory.isLongVR(vrType);
            VRBase vr = VRFactory.getVRInstance(vrType, false);
            System.out.println("    " + vrType + ": 工厂检查=" + isLong + 
                             ", 实例检查=" + vr.isLongVR + 
                             " (" + (!isLong && !vr.isLongVR ? "通过" : "失败") + ")");
        }
    }
    
    /**
     * 测试VR工厂
     */
    private static void testVRFactory() {
        System.out.println("3. VR工厂测试:");
        
        int initialPoolSize = VRFactory.getPoolSize();
        System.out.println("初始缓存池大小: " + initialPoolSize);
        
        // 获取所有VR类型的实例
        String[] allVRTypes = {"AE", "AS", "AT", "CS", "DA", "DS", "DT", "FD", "FL", "IS",
                               "LO", "LT", "OB", "OD", "OF", "OL", "OW", "PN", "SH", "SL",
                               "SQ", "SS", "ST", "TM", "UC", "UI", "UL", "UN", "UR", "US", "UT"};
        
        for (String vrType : allVRTypes) {
            VRFactory.getVRInstance(vrType, false); // Little Endian
            VRFactory.getVRInstance(vrType, true);  // Big Endian
        }
        
        int finalPoolSize = VRFactory.getPoolSize();
        System.out.println("创建所有VR实例后缓存池大小: " + finalPoolSize);
        System.out.println("预期大小: " + (allVRTypes.length * 2) + " (31种VR × 2种字节序)");
        System.out.println("测试结果: " + (finalPoolSize == allVRTypes.length * 2 ? "通过" : "失败"));
        
        // 测试未知VR类型
        VRBase unknown = VRFactory.getVRInstance("XX", false);
        System.out.println("未知VR类型'XX'返回实例类型: " + 
                          (unknown != null ? unknown.getClass().getSimpleName() : "null"));
        System.out.println("应该返回UN类型: " + (unknown instanceof UN ? "通过" : "失败"));
    }
}

package com.dicom.data;

import com.dicom.transfersyntax.ExplicitVRBigEndian;

/**
 * SQ序列特定测试用例
 * 测试您提供的具体测试数据
 */
public class SQSpecificTest {
    
    public static void main(String[] args) {
        System.out.println("=== SQ序列特定测试用例 ===\n");
        
        // 测试1: SQ确定长度(显式VR, BE)
        test1_SQDefinedLength();
        
        // 测试2: SQ未定义长度(显式VR, BE) 
        test2_SQUndefinedLength();
        
        // 测试3: SQ未定义长度+未定义长度两层嵌套
        test3_SQNestedUndefinedLength();
        
        System.out.println("\n=== 所有特定测试完成 ===");
    }
    
    /**
     * 测试1: SQ确定长度(显式VR, BE)
     */
    private static void test1_SQDefinedLength() {
        System.out.println("1. 测试SQ确定长度(显式VR, BE):");
        
        try {
            // 您提供的测试数据
            byte[] data = {
                0x00,0x08,0x11,0x40,0x53,0x51,0x00,0x00,0x00,0x00,0x00,0x68,(byte)0xFF,(byte)0xFE,(byte)0xE0,0x00,0x00,0x00,0x00,0x2C,
                0x00,0x08,0x11,0x50,0x55,0x49,0x00,0x0E,0x31,0x2E,0x32,0x2E,0x38,0x34,0x30,0x2E,0x31,0x2E,0x31,0x2E,0x32,0x00,
                0x00,0x08,0x11,0x55,0x55,0x49,0x00,0x0E,0x31,0x2E,0x32,0x2E,0x38,0x34,0x30,0x2E,0x31,0x31,0x33,0x2E,0x31,0x32,
                (byte)0xFF,(byte)0xFE,(byte)0xE0,0x00,0x00,0x00,0x00,0x2C,0x00,0x08,0x11,0x50,0x55,0x49,0x00,0x0E,
                0x31,0x2E,0x32,0x2E,0x38,0x34,0x30,0x2E,0x31,0x2E,0x31,0x2E,0x32,0x00,0x00,0x08,0x11,0x55,0x55,0x49,0x00,0x0E,
                0x31,0x2E,0x32,0x2E,0x38,0x34,0x30,0x2E,0x31,0x31,0x33,0x2E,0x31,0x30
            };
            
            // 实例化数据集并运行
            DCMDataSet dcm = new DCMDataSet(new ExplicitVRBigEndian());
            int[] idx = {0};
            dcm.Parse(data, idx);
            
            System.out.println("   ✓ 解析成功");
            System.out.println("   解析结果:");
            System.out.println(dcm.ToString("   "));
            
        } catch (Exception e) {
            System.err.println("   ✗ 测试1失败: " + e.getMessage());
            e.printStackTrace();
        }
        
        System.out.println();
    }
    
    /**
     * 测试2: SQ未定义长度(显式VR, BE)
     */
    private static void test2_SQUndefinedLength() {
        System.out.println("2. 测试SQ未定义长度(显式VR, BE):");
        
        try {
            // 您提供的测试数据
            byte[] data = {
                0x00, 0x08, 0x11, 0x40, 0x53, 0x51, 0x00, 0x00, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, 
                (byte)0xff, (byte)0xfe, (byte)0xe0, 0x00, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, 
                0x00, 0x08, 0x11, 0x50, 0x55, 0x49, 0x00, 0x1a, 0x31, 0x2e, 0x32, 0x2e, 0x38, 0x34, 0x30, 0x2e, 0x31, 0x30, 0x30, 0x30, 0x38, 0x2e, 0x35, 0x2e,
                0x31, 0x2e, 0x34, 0x2e, 0x31, 0x2e, 0x31, 0x2e, 0x32, 0x00, 0x00, 0x08, 0x11, 0x55, 0x55, 0x49, 0x00, 0x24, 0x31, 0x2e, 0x32, 0x2e, 0x38, 0x34, 0x30, 0x2e, 0x31, 0x31, 0x33,
                0x36, 0x31, 0x39, 0x2e, 0x32, 0x2e, 0x36, 0x35, 0x2e, 0x31, 0x2e, 0x31, 0x30, 0x32, 0x36, 0x36, 0x36, 0x38, 0x33, 0x35, 0x33, 0x2e, 0x31, 0x32, 0x00, 
                (byte)0xff, (byte)0xfe, (byte)0xe0, 0x0d, 0x00, 0x00, 0x00, 0x00, 
                (byte)0xff, (byte)0xfe, (byte)0xe0, 0x00, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, 0x00, 0x08, 0x11, 0x50, 0x55, 0x49, 0x00, 0x1a, 0x31, 0x2e, 0x32, 0x2e, 0x38, 0x34, 0x30, 0x2e, 0x31, 0x30,
                0x30, 0x30, 0x38, 0x2e, 0x35, 0x2e, 0x31, 0x2e, 0x34, 0x2e, 0x31, 0x2e, 0x31, 0x2e, 0x32, 0x00, 0x00, 0x08, 0x11, 0x55, 0x55, 0x49, 0x00, 0x24, 0x31, 0x2e, 0x32, 0x2e, 0x38,
                0x34, 0x30, 0x2e, 0x31, 0x31, 0x33, 0x36, 0x31, 0x39, 0x2e, 0x32, 0x2e, 0x36, 0x35, 0x2e, 0x31, 0x2e, 0x31, 0x30, 0x32, 0x36, 0x36, 0x36, 0x38, 0x33, 0x35, 0x33, 0x2e, 0x31, 0x33, 0x00, 
                (byte)0xff, (byte)0xfe, (byte)0xe0, 0x0d, 0x00, 0x00, 0x00, 0x00, 
                (byte)0xff, (byte)0xfe, (byte)0xe0, 0x00, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, 0x00, 0x08, 0x11, 0x50, 0x55, 0x49, 0x00, 0x1a, 0x31, 0x2e, 0x32, 0x2e, 0x38, 0x34, 0x30, 0x2e, 0x31, 0x30,
                0x30, 0x30, 0x38, 0x2e, 0x35, 0x2e, 0x31, 0x2e, 0x34, 0x2e, 0x31, 0x2e, 0x31, 0x2e, 0x32, 0x00, 0x00, 0x08, 0x11, 0x55, 0x55, 0x49, 0x00, 0x24, 0x31, 0x2e, 0x32, 0x2e, 0x38,
                0x34, 0x30, 0x2e, 0x31, 0x31, 0x33, 0x36, 0x31, 0x39, 0x2e, 0x32, 0x2e, 0x36, 0x35, 0x2e, 0x31, 0x2e, 0x31, 0x30, 0x32, 0x36, 0x36, 0x36, 0x38, 0x33, 0x35, 0x33, 0x2e, 0x31, 0x30, 0x00, 
                (byte)0xff, (byte)0xfe, (byte)0xe0, 0x0d, 0x00, 0x00, 0x00, 0x00, 
                (byte)0xff, (byte)0xfe, (byte)0xe0, (byte)0xdd, 0x00, 0x00, 0x00, 0x00
            };
            
            // 实例化数据集并运行
            DCMDataSet dcm = new DCMDataSet(new ExplicitVRBigEndian());
            int[] idx = {0};
            dcm.Parse(data, idx);
            
            System.out.println("   ✓ 解析成功");
            System.out.println("   解析结果:");
            System.out.println(dcm.ToString("   "));
            
        } catch (Exception e) {
            System.err.println("   ✗ 测试2失败: " + e.getMessage());
            e.printStackTrace();
        }
        
        System.out.println();
    }
    
    /**
     * 测试3: SQ未定义长度+未定义长度两层嵌套
     */
    private static void test3_SQNestedUndefinedLength() {
        System.out.println("3. 测试SQ未定义长度+两层嵌套:");
        
        try {
            // 您提供的完整嵌套测试数据
            byte[] data = parseHexString(
                "00 08 11 40 53 51 00 00 ff ff ff ff ff fe e0 00 ff ff ff ff 00 08 11 50 55 49 00 1a 31 " +
                "2e 32 2e 38 34 30 2e 31 30 30 30 38 2e 35 2e 31 2e 34 2e 31 2e 31 2e 32 00 00 " +
                "08 11 55 55 49 00 24 31 2e 32 2e 38 34 30 2e 31 31 33 36 31 39 2e 32 2e 36 35 " +
                "2e 31 2e 31 30 32 36 36 36 38 33 35 33 2e 31 32 00 00 08 11 40 53 51 00 00 ff ff " +
                "ff ff ff fe e0 00 ff ff ff ff 00 08 11 50 55 49 00 1a 31 2e 32 2e 38 34 30 2e 31 30 " +
                "30 30 38 2e 35 2e 31 2e 34 2e 31 2e 31 2e 32 00 00 08 11 55 55 49 00 24 31 2e " +
                "32 2e 38 34 30 2e 31 31 33 36 31 39 2e 32 2e 36 35 2e 31 2e 31 30 32 36 36 36 " +
                "38 33 35 33 2e 31 32 00 ff fe e0 0d 00 00 00 00 ff fe e0 00 ff ff ff ff 00 08 11 50 " +
                "55 49 00 1a 31 2e 32 2e 38 34 30 2e 31 30 30 30 38 2e 35 2e 31 2e 34 2e 31 2e " +
                "31 2e 32 00 00 08 11 55 55 49 00 24 31 2e 32 2e 38 34 30 2e 31 31 33 36 31 39 " +
                "2e 32 2e 36 35 2e 31 2e 31 30 32 36 36 36 38 33 35 33 2e 31 33 00 ff fe e0 0d 00 " +
                "00 00 00 ff fe e0 00 ff ff ff ff 00 08 11 50 55 49 00 1a 31 2e 32 2e 38 34 30 2e 31 " +
                "30 30 30 38 2e 35 2e 31 2e 34 2e 31 2e 31 2e 32 00 00 08 11 55 55 49 00 24 31 " +
                "2e 32 2e 38 34 30 2e 31 31 33 36 31 39 2e 32 2e 36 35 2e 31 2e 31 30 32 36 36 " +
                "36 38 33 35 33 2e 31 30 00 ff fe e0 0d 00 00 00 00 ff fe e0 dd 00 00 00 00 ff fe e0 " +
                "0d 00 00 00 00 ff fe e0 00 ff ff ff ff 00 08 11 50 55 49 00 1a 31 2e 32 2e 38 34 30 " +
                "2e 31 30 30 30 38 2e 35 2e 31 2e 34 2e 31 2e 31 2e 32 00 00 08 11 55 55 49 00 " +
                "24 31 2e 32 2e 38 34 30 2e 31 31 33 36 31 39 2e 32 2e 36 35 2e 31 2e 31 30 32 " +
                "36 36 36 38 33 35 33 2e 31 33 00 ff fe e0 0d 00 00 00 00 ff fe e0 00 ff ff ff ff 00 " +
                "08 11 50 55 49 00 1a 31 2e 32 2e 38 34 30 2e 31 30 30 30 38 2e 35 2e 31 2e 34 " +
                "2e 31 2e 31 2e 32 00 00 08 11 55 55 49 00 24 31 2e 32 2e 38 34 30 2e 31 31 33 " +
                "36 31 39 2e 32 2e 36 35 2e 31 2e 31 30 32 36 36 36 38 33 35 33 2e 31 30 00 ff fe " +
                "e0 0d 00 00 00 00 ff fe e0 dd 00 00 00 00"
            );
            
            System.out.println("   数据长度: " + data.length + " 字节");
            
            // 实例化数据集并运行
            DCMDataSet dcm = new DCMDataSet(new ExplicitVRBigEndian());
            int[] idx = {0};
            dcm.Parse(data, idx);
            
            System.out.println("   ✓ 解析成功");
            System.out.println("   解析结果:");
            System.out.println(dcm.ToString("   "));
            
            // 分析嵌套结构
            System.out.println("   结构分析:");
            if (dcm.getItemCount() > 0) {
                Object firstItem = dcm.Item(0);
                if (firstItem != null) {
                    System.out.println("   - 顶层包含 " + dcm.getItemCount() + " 个元素");
                    System.out.println("   - 第一个元素类型: " + firstItem.getClass().getSimpleName());
                }
            }
            
        } catch (Exception e) {
            System.err.println("   ✗ 测试3失败: " + e.getMessage());
            e.printStackTrace();
        }
        
        System.out.println();
    }
    
    /**
     * 解析十六进制字符串为字节数组
     * @param hexString 十六进制字符串，如"00 01 02 FF"
     * @return 字节数组
     */
    private static byte[] parseHexString(String hexString) {
        String[] hexBytes = hexString.replaceAll("\\s+", " ").trim().split(" ");
        byte[] result = new byte[hexBytes.length];
        for (int i = 0; i < hexBytes.length; i++) {
            result[i] = (byte) Integer.parseInt(hexBytes[i], 16);
        }
        return result;
    }
}

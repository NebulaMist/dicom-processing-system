package com.dicom.transfersyntax;

import com.dicom.data.DCMAbstractType;
import com.dicom.data.DCMDataElement;

/**
 * 传输语法测试程序
 * 测试各种传输语法格式的数据解析
 */
public class TransferSyntaxTest {
    
    /**
     * 将十六进制字符串转换为字节数组
     * @param hexString 十六进制字符串（空格分隔）
     * @return 字节数组
     */
    private static byte[] hexStringToByteArray(String hexString) {
        // 移除空格
        String cleanHex = hexString.replaceAll("\\s+", "");
        int len = cleanHex.length();
        byte[] data = new byte[len / 2];
        
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(cleanHex.charAt(i), 16) << 4)
                                 + Character.digit(cleanHex.charAt(i+1), 16));
        }
        return data;
    }
    
    /**
     * 打印字节数组为十六进制格式
     * @param data 字节数组
     * @return 十六进制字符串
     */
    private static String byteArrayToHexString(byte[] data) {
        if (data == null) {
            return "null";
        }
        StringBuilder sb = new StringBuilder();
        for (byte b : data) {
            sb.append(String.format("%02X ", b));
        }
        return sb.toString().trim();
    }
    
    /**
     * 测试隐式VR小端传输语法
     */
    private static void testImplicitVRLittleEndian() {
        System.out.println("=== 测试隐式VR小端传输语法 ===");
        
        // 测试数据：28 00 10 00 02 00 00 00 b3 02
        String testData = "28 00 10 00 02 00 00 00 b3 02";
        byte[] data = hexStringToByteArray(testData);
        
        System.out.println("原始数据: " + testData);
        
        ImplicitVRLittleEndian transferSyntax = new ImplicitVRLittleEndian();
        int[] idx = {0};
        
        DCMAbstractType result = transferSyntax.Decode(data, idx);
        
        if (result instanceof DCMDataElement) {
            DCMDataElement element = (DCMDataElement) result;
            System.out.println("解析结果:");
            System.out.println("  Group Tag: " + String.format("0x%04X", element.gtag & 0xFFFF));
            System.out.println("  Element Tag: " + String.format("0x%04X", element.etag & 0xFFFF));
            System.out.println("  Name: " + element.name);
            System.out.println("  VR: " + element.vr);
            System.out.println("  Length: " + element.length);
            System.out.println("  Value: " + byteArrayToHexString(element.value));
            System.out.println("  解析位置: " + idx[0]);
            System.out.println("  ToString: " + element.ToString(""));
        } else {
            System.out.println("解析失败");
        }
        System.out.println();
    }
    
    /**
     * 测试显式VR大端传输语法
     */
    private static void testExplicitVRBigEndian() {
        System.out.println("=== 测试显式VR大端传输语法 ===");
        
        // 测试数据：00 10 10 10 41 53 00 04 30 36 35 59
        String testData = "00 10 10 10 41 53 00 04 30 36 35 59";
        byte[] data = hexStringToByteArray(testData);
        
        System.out.println("原始数据: " + testData);
        
        ExplicitVRBigEndian transferSyntax = new ExplicitVRBigEndian();
        int[] idx = {0};
        
        DCMAbstractType result = transferSyntax.Decode(data, idx);
        
        if (result instanceof DCMDataElement) {
            DCMDataElement element = (DCMDataElement) result;
            System.out.println("解析结果:");
            System.out.println("  Group Tag: " + String.format("0x%04X", element.gtag & 0xFFFF));
            System.out.println("  Element Tag: " + String.format("0x%04X", element.etag & 0xFFFF));
            System.out.println("  Name: " + element.name);
            System.out.println("  VR: " + element.vr);
            System.out.println("  Length: " + element.length);
            System.out.println("  Value: " + byteArrayToHexString(element.value));
            System.out.println("  Value (String): " + new String(element.value));            System.out.println("  解析位置: " + idx[0]);
            System.out.println("  ToString: " + element.ToString(""));
        } else {
            System.out.println("解析失败");
        }
        System.out.println();
    }
    
    /**
     * 测试默认传输语法（隐式VR小端）- 多个数据元素
     */
    private static void testDefaultTransferSyntaxMultiple() {
        System.out.println("=== 测试默认传输语法（多个数据元素）===");
        
        // 测试数据：08 00 21 00 08 00 00 00 31 39 39 35 30 36 30 38 10 00 20 00 06 00 00 00 47 45 30 35 31 34 28 00 00 01 02 00 00 00 10 00
        String testData = "08 00 21 00 08 00 00 00 31 39 39 35 30 36 30 38 10 00 20 00 06 00 00 00 47 45 30 35 31 34 28 00 00 01 02 00 00 00 10 00";
        byte[] data = hexStringToByteArray(testData);
        
        System.out.println("原始数据: " + testData);
        
        ImplicitVRLittleEndian transferSyntax = new ImplicitVRLittleEndian();
        int[] idx = {0};
        int elementCount = 0;
        
        // 连续解析多个数据元素
        while (idx[0] < data.length) {
            DCMAbstractType result = transferSyntax.Decode(data, idx);
            
            if (result instanceof DCMDataElement) {
                elementCount++;
                DCMDataElement element = (DCMDataElement) result;
                System.out.println("数据元素 " + elementCount + ":");
                System.out.println("  Group Tag: " + String.format("0x%04X", element.gtag & 0xFFFF));
                System.out.println("  Element Tag: " + String.format("0x%04X", element.etag & 0xFFFF));
                System.out.println("  Name: " + element.name);
                System.out.println("  VR: " + element.vr);
                System.out.println("  Length: " + element.length);
                System.out.println("  Value: " + byteArrayToHexString(element.value));
                
                // 尝试将值转换为字符串（如果是文本类型）
                if (element.vr != null && (element.vr.equals("DA") || element.vr.equals("SH") || 
                    element.vr.equals("LO") || element.vr.equals("CS"))) {
                    System.out.println("  Value (String): " + new String(element.value).trim());
                }
                  System.out.println("  ToString: " + element.ToString(""));
                System.out.println("  当前位置: " + idx[0]);
                System.out.println();
            } else {
                System.out.println("解析失败，停止解析");
                break;
            }
        }
        
        System.out.println("共解析了 " + elementCount + " 个数据元素");
        System.out.println();
    }
    
    /**
     * 主函数
     */
    public static void main(String[] args) {
        System.out.println("DICOM传输语法测试程序");
        System.out.println("========================");
        
        try {
            // 测试隐式VR小端
            testImplicitVRLittleEndian();
            
            // 测试显式VR大端
            testExplicitVRBigEndian();
            
            // 测试默认传输语法（多个数据元素）
            testDefaultTransferSyntaxMultiple();
            
            System.out.println("所有测试完成！");
            
        } catch (Exception e) {
            System.err.println("测试过程中发生错误: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

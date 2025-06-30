package com.dicom.data;

import com.dicom.transfersyntax.*;
import com.dicom.dictionary.DicomTags;

/**
 * 综合编码测试程序
 * 实现您要求的完整测试用例
 */
public class ComprehensiveEncodeTest {
    
    public static void main(String[] args) {
        System.out.println("=== DICOM综合编码测试 ===\n");
        
        // 测试1: 实例化dataset并执行SetValue操作
        testDataSetCreationAndSetValue();
        
        // 测试2: 编码为字节数组
        testEncoding();
        
        // 测试3: 用编码数据进行解析验证
        testRoundTripParsing();
        
        System.out.println("\n=== 所有测试完成 ===");
    }
    
    /**
     * 测试1: 实例化dataset并执行SetValue操作
     */
    private static void testDataSetCreationAndSetValue() {
        System.out.println("1. 测试数据集创建和SetValue操作:");
        
        try {
            // 实例化dataset
            DCMDataSet dcm = new DCMDataSet(new ExplicitVRBigEndian());
            
            // 设置各种数据元素
            dcm.ItemByTag(DicomTags.Rows).SetValue((short)256);
            dcm.ItemByTag(DicomTags.Columns).SetValue((short)512);
            dcm.ItemByTag(DicomTags.PatientID).SetValue("GE05112");
            dcm.ItemByTag(DicomTags.ReferencedSOPInstanceUID).SetValue("1.2.840.10072.35435.34.667");
            dcm.ItemByTag(DicomTags.ReferencedSOPClassUID).SetValue("1.2.840.10008.5.1.4.1.1.1");
            
            // 设置像素数据
            short[] vals = {(short)0x4065, (short)0x4066, (short)0x4067, (short)0x4069, (short)0x404A};
            dcm.ItemByTag(DicomTags.PixelData).SetValue(vals);
            
            System.out.println("   ✓ 数据元素设置成功");
            System.out.println("   数据集内容:");
            System.out.println(dcm.ToString("   "));
            
        } catch (Exception e) {
            System.err.println("   ✗ 数据集创建测试失败: " + e.getMessage());
            e.printStackTrace();
        }
        
        System.out.println();
    }
    
    /**
     * 测试2: 编码为字节数组
     */
    private static void testEncoding() {
        System.out.println("2. 测试编码为字节数组:");
        
        try {
            // 创建数据集
            DCMDataSet dcm = new DCMDataSet(new ExplicitVRBigEndian());
            
            // 设置数据元素
            dcm.ItemByTag(DicomTags.ReferencedSOPInstanceUID).SetValue("1.2.840.10008.5.1.4.1.1.1");
            dcm.ItemByTag(DicomTags.ReferencedSOPClassUID).SetValue("1.2.840.10072.35435.34.667");
            dcm.ItemByTag(DicomTags.PatientID).SetValue("GE05112");
            dcm.ItemByTag(DicomTags.Rows).SetValue((short)256);
            dcm.ItemByTag(DicomTags.Columns).SetValue((short)512);
            
            // 设置像素数据
            short[] vals = {(short)0x4065, (short)0x4066, (short)0x4067, (short)0x4069, (short)0x404A};
            dcm.ItemByTag(DicomTags.PixelData).SetValue(vals);
            
            // 编码为字节数组
            byte[] data = dcm.Encode(false);
            
            System.out.println("   ✓ 编码成功，总长度: " + data.length + " 字节");
            System.out.println("   编码结果:");
            printHexData(data);
            
        } catch (Exception e) {
            System.err.println("   ✗ 编码测试失败: " + e.getMessage());
            e.printStackTrace();
        }
        
        System.out.println();
    }
    
    /**
     * 测试3: 用编码数据进行解析验证
     */
    private static void testRoundTripParsing() {
        System.out.println("3. 测试编码解码往返:");
        
        try {
            // 第一步：创建原始数据集
            DCMDataSet originalDcm = new DCMDataSet(new ExplicitVRBigEndian());
            
            // 设置测试数据
            originalDcm.ItemByTag(DicomTags.ReferencedSOPInstanceUID).SetValue("1.2.840.10008.5.1.4.1.1.1");
            originalDcm.ItemByTag(DicomTags.ReferencedSOPClassUID).SetValue("1.2.840.10072.35435.34.667");
            originalDcm.ItemByTag(DicomTags.PatientID).SetValue("GE05112");
            originalDcm.ItemByTag(DicomTags.Rows).SetValue((short)256);
            originalDcm.ItemByTag(DicomTags.Columns).SetValue((short)512);
            
            // 第二步：编码
            byte[] data = originalDcm.Encode(false);
            System.out.println("   原始数据编码长度: " + data.length + " 字节");
            
            // 第三步：解析编码数据
            DCMDataSet parsedDcm = new DCMDataSet(new ExplicitVRBigEndian());
            int[] idx = {0};
            parsedDcm.Parse(data, idx);
            
            System.out.println("   ✓ 解析成功");
            System.out.println("   解析结果:");
            System.out.println(parsedDcm.ToString("   "));
            
            // 第四步：验证数据一致性
            boolean success = true;
            
            // 验证Patient ID
            String originalPatientID = originalDcm.GetValue(DicomTags.PatientID);
            String parsedPatientID = parsedDcm.GetValue(DicomTags.PatientID);
            boolean patientIDMatch = originalPatientID != null && originalPatientID.trim().equals(parsedPatientID != null ? parsedPatientID.trim() : "");
            
            // 验证Rows
            Integer originalRows = originalDcm.GetValue(DicomTags.Rows);
            Integer parsedRows = parsedDcm.GetValue(DicomTags.Rows);
            boolean rowsMatch = originalRows != null && originalRows.equals(parsedRows);
            
            // 验证Columns
            Integer originalColumns = originalDcm.GetValue(DicomTags.Columns);
            Integer parsedColumns = parsedDcm.GetValue(DicomTags.Columns);
            boolean columnsMatch = originalColumns != null && originalColumns.equals(parsedColumns);
            
            success = patientIDMatch && rowsMatch && columnsMatch;
            
            System.out.println("   验证结果:");
            System.out.println("   Patient ID匹配: " + (patientIDMatch ? "✓" : "✗") + 
                             " (原始: '" + originalPatientID + "', 解析: '" + parsedPatientID + "')");
            System.out.println("   Rows匹配: " + (rowsMatch ? "✓" : "✗") + 
                             " (原始: " + originalRows + ", 解析: " + parsedRows + ")");
            System.out.println("   Columns匹配: " + (columnsMatch ? "✓" : "✗") + 
                             " (原始: " + originalColumns + ", 解析: " + parsedColumns + ")");
            System.out.println("   往返测试: " + (success ? "✓ 成功" : "✗ 失败"));
            
        } catch (Exception e) {
            System.err.println("   ✗ 往返测试失败: " + e.getMessage());
            e.printStackTrace();
        }
        
        System.out.println();
    }
    
    /**
     * 打印十六进制数据
     * @param data 字节数组
     */
    private static void printHexData(byte[] data) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < data.length; i++) {
            if (i > 0) {
                sb.append(",");
            }
            if (i % 16 == 0) {
                sb.append("\n   ");
            }
            sb.append(String.format("0x%02X", data[i] & 0xFF));
        }
        System.out.println(sb.toString());
    }
}

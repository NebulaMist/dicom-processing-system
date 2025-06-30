package com.dicom.data;

import com.dicom.transfersyntax.ExplicitVRBigEndian;
import com.dicom.dictionary.DicomTags;

/**
 * 完整的DICOM编码测试程序
 * 按照用户需求实现测试用例
 */
public class CompleteEncodeTest {
    
    public static void main(String[] args) {
        System.out.println("=== 完整DICOM编码测试 ===\n");
        
        try {
            // 1. 实例化dataset并执行设置操作
            DCMDataSet dcm = new DCMDataSet(new ExplicitVRBigEndian());
            
            // 使用你要求的语法设置值
            ((DCMDataSet.DCMElementWrapper)dcm.Item(DicomTags.Rows)).SetValue((short)256);
            ((DCMDataSet.DCMElementWrapper)dcm.Item(DicomTags.Columns)).SetValue((short)512);
            ((DCMDataSet.DCMElementWrapper)dcm.Item(DicomTags.PatientID)).SetValue("GE05112");
            ((DCMDataSet.DCMElementWrapper)dcm.Item(DicomTags.ReferencedSOPInstanceUID)).SetValue("1.2.840.10072.35435.34.667"); // UI 26
            ((DCMDataSet.DCMElementWrapper)dcm.Item(DicomTags.ReferencedSOPClassUID)).SetValue("1.2.840.10008.5.1.4.1.1.1"); // UI 25
            
            short[] vals = {(short)0x4065, (short)0x4066, (short)0x4067, (short)0x4069, (short)0x404A};
            ((DCMDataSet.DCMElementWrapper)dcm.Item(DicomTags.PixelData)).SetValue(vals);
            
            // 可选的其他测试
            // dcm.Item(DicomTags.PixelAspectRatio).SetValue(0.97654321);  //IS 
            // dcm.Item(DicomTags.WindowCenter).SetValue(1024);   //DS 
            
            // 2. 显示数据集结构
            System.out.println("数据元素添加成功：");
            System.out.println(dcm.ToString(""));
            
            // 3. 编码为字节数组
            System.out.println("\n编码为字节数组：");
            byte[] data = dcm.Encode();
            
            // 4. 显示编码结果
            System.out.println("编码结果长度: " + data.length + " 字节");
            System.out.print("编码结果: ");
            for (int i = 0; i < data.length; i++) {
                if (i > 0) System.out.print(",");
                System.out.print(String.format("0x%02X", data[i] & 0xFF));
            }
            System.out.println();
            
            // 5. 用编码数据进行解析验证
            System.out.println("\n解析验证：");
            DCMDataSet dcm2 = new DCMDataSet(new ExplicitVRBigEndian());
            int[] idx = {0};
            dcm2.Parse(data, idx);
            
            System.out.println("解析结果：");
            System.out.println(dcm2.ToString(""));
              // 6. 验证往返一致性
            System.out.println("\n往返一致性验证：");
            boolean success = true;
            
            // 验证关键字段 - 直接使用GetValue方法比较
            Object originalRows = dcm.GetValue(DicomTags.Rows);
            Object parsedRows = dcm2.GetValue(DicomTags.Rows);
            
            System.out.println("原始Rows: " + originalRows);
            System.out.println("解析Rows: " + parsedRows);
            
            Object originalPatientID = dcm.GetValue(DicomTags.PatientID);
            Object parsedPatientID = dcm2.GetValue(DicomTags.PatientID);
            
            System.out.println("原始PatientID: " + originalPatientID);
            System.out.println("解析PatientID: " + parsedPatientID);
            
            // 验证一致性
            boolean rowsMatch = (originalRows != null && originalRows.equals(parsedRows)) ||
                               (originalRows == null && parsedRows == null);
            boolean patientIDMatch = (originalPatientID != null && originalPatientID.toString().trim().equals(
                                     parsedPatientID != null ? parsedPatientID.toString().trim() : "")) ||
                                    (originalPatientID == null && parsedPatientID == null);
            
            System.out.println("Rows验证: " + (rowsMatch ? "✓ 通过" : "✗ 失败"));
            System.out.println("PatientID验证: " + (patientIDMatch ? "✓ 通过" : "✗ 失败"));
            System.out.println("整体验证: " + (rowsMatch && patientIDMatch ? "✓ 成功" : "✗ 失败"));
            
            System.out.println("\n测试完成！");
            
        } catch (Exception e) {
            System.err.println("测试过程中出现错误：");
            e.printStackTrace();
        }
    }
    
    /**
     * 将字节数组转换为十六进制字符串显示
     */
    private static String bytesToHex(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            return "";
        }
        
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < bytes.length; i++) {
            if (i > 0) sb.append(" ");
            sb.append(String.format("%02X", bytes[i] & 0xFF));
        }
        return sb.toString();
    }
}

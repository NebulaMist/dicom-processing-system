package com.dicom.data;

import com.dicom.dictionary.DicomTags;
import com.dicom.transfersyntax.DicomByteBuffer;

/**
 * DCMDataSet测试类
 * 测试数据集的基本功能
 */
public class DCMDataSetTest {
    
    public static void main(String[] args) {
        System.out.println("=== DCMDataSet功能测试 ===\n");
          // Create a simple transfer syntax implementation for testing
        TransferSyntax testSyntax = new TransferSyntax() {
            @Override
            protected DCMAbstractType Decode(DicomByteBuffer buffer) {
                // Simple test implementation
                return null;
            }

            @Override
            protected int Encode(java.nio.ByteBuffer buffer, DCMAbstractType value) {
                // Simple test implementation
                return 0;
            }
        };
        
        // 创建数据集实例
        DCMDataSet dataSet = new DCMDataSet(testSyntax);
        
        // 测试基本功能
        System.out.println("1. 测试数据集基本功能...");
        
        // 创建测试数据元素
        DCMDataElement element = new DCMDataElement(testSyntax);
        element.gtag = (short) 0x0010;
        element.etag = (short) 0x0010;
        element.vr = "PN";
        element.name = "Patient's Name";
        element.value = "Test^Patient".getBytes();
        element.length = element.value.length;
        
        // 添加到数据集
        dataSet.Add(element);
        
        // 测试访问方法
        String patientName = dataSet.GetValue(DicomTags.PatientName);
        System.out.println("   患者姓名: " + patientName);
        
        String elementName = dataSet.GetName(DicomTags.PatientName);
        System.out.println("   元素名称: " + elementName);
        
        String vr = dataSet.GetVR(DicomTags.PatientName);
        System.out.println("   VR: " + vr);
        
        String vm = dataSet.GetVM(DicomTags.PatientName);
        System.out.println("   VM: " + vm);
        
        System.out.println("   元素数量: " + dataSet.getItemCount());
        
        System.out.println("\n✓ DCMDataSet测试完成");
    }
}
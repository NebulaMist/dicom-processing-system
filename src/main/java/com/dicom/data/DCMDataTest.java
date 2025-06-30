package com.dicom.data;

import com.dicom.dictionary.DicomDictionary;
import com.dicom.dictionary.DicomDictionaryEntry;
import com.dicom.transfersyntax.ImplicitVRLittleEndian;

/**
 * DICOM数据类型测试程序
 * 测试新建的DCM相关类
 */
public class DCMDataTest {
    public static void main(String[] args) {
        System.out.println("=== DICOM数据类型测试程序 ===");
        
        try {            // 1. 测试TransferSyntax类
            System.out.println("1. 测试TransferSyntax类创建");
            TransferSyntax ts = new ImplicitVRLittleEndian();
            System.out.println("TransferSyntax对象创建成功: " + ts.name);
            
            // 2. 测试DCMDataElement类
            System.out.println("\n2. 测试DCMDataElement类");
            DCMDataElement element = new DCMDataElement(ts);
            
            // 设置一些测试数据
            element.gtag = (short) 0x0010;
            element.etag = (short) 0x0010;
            element.name = "Patient's Name";
            element.vr = "PN";
            element.vm = "1";
            element.length = 12;
            element.value = "SMITH^JOHN".getBytes();
            
            System.out.println("DCMDataElement创建成功");
            System.out.println("元素信息: " + element.ToString(""));
            
            // 3. 测试DCMDataSet类
            System.out.println("\n3. 测试DCMDataSet类");
            DCMDataSet dataSet = new DCMDataSet(ts);
            
            // 添加第一个元素
            dataSet.addItem(element);
            
            // 创建第二个元素
            DCMDataElement element2 = new DCMDataElement(ts);
            element2.gtag = (short) 0x0008;
            element2.etag = (short) 0x0018;
            element2.name = "SOP Instance UID";
            element2.vr = "UI";
            element2.vm = "1";
            element2.length = 26;
            element2.value = "1.2.3.4.5.6.7.8.9.0.1.2.3.4".getBytes();
            
            dataSet.addItem(element2);
            
            System.out.println("DCMDataSet创建成功");
            System.out.println("数据集包含 " + dataSet.getItemCount() + " 个元素");
            System.out.println("\n数据集内容:");
            System.out.println(dataSet.ToString("  "));
            
            // 4. 测试与字典的结合使用
            System.out.println("\n4. 测试与DICOM字典的结合使用");
            DicomDictionary dict = DicomDictionary.loadFromClasspath();
            
            // 使用字典查询元素信息
            DicomDictionaryEntry dictEntry = dict.lookup("0010", "0010");
            if (dictEntry != null) {
                System.out.println("从字典查询到的信息:");
                System.out.println("  名称: " + dictEntry.getName());
                System.out.println("  VR: " + dictEntry.getVr());
                System.out.println("  VM: " + dictEntry.getVm());
            }
            
            System.out.println("\n=== 所有测试完成，编译通过！ ===");
            
        } catch (Exception e) {
            System.err.println("测试过程中出错: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

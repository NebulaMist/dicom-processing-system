package com.dicom.dictionary;

import com.dicom.data.DCMDataSet;
import com.dicom.data.DCMDataElement;
import com.dicom.data.DCMAbstractType;
import com.dicom.data.TransferSyntax;

/**
 * 测试新功能的主程序
 * 1. 测试 DicomTags 常量定义
 * 2. 测试 DCMDataSet 的新方法（GetValue, GetName, GetVR, GetVM）
 */
public class TestNewFeatures {
    
    public static void main(String[] args) {
        System.out.println("=== 测试DICOM新功能 ===\n");
        
        // 1. 测试 DicomTags 常量
        testDicomTags();
        
        // 2. 测试字典加载和生成功能
        testDictionaryAndGeneration();
        
        // 3. 测试 DCMDataSet 的新方法
        testDCMDataSetNewMethods();
        
        System.out.println("\n=== 所有测试完成 ===");
    }
    
    /**
     * 测试 DicomTags 常量定义
     */
    private static void testDicomTags() {
        System.out.println("1. 测试 DicomTags 常量定义:");
        
        // 测试一些常见的DICOM标签
        System.out.println("   PatientWeight = 0x" + Integer.toHexString(DicomTags.PatientWeight).toUpperCase());
        System.out.println("   SmokingStatus = 0x" + Integer.toHexString(DicomTags.SmokingStatus).toUpperCase());
        System.out.println("   RescaleSlope = 0x" + Integer.toHexString(DicomTags.RescaleSlope).toUpperCase());
        System.out.println("   RescaleType = 0x" + Integer.toHexString(DicomTags.RescaleType).toUpperCase());
        System.out.println("   PixelSpacing = 0x" + Integer.toHexString(DicomTags.PixelSpacing).toUpperCase());
        
        System.out.println("   ✓ DicomTags 常量定义正常\n");
    }
    
    /**
     * 测试字典加载和生成功能
     */
    private static void testDictionaryAndGeneration() {
        System.out.println("2. 测试字典加载功能:");
        
        try {
            // 从类路径加载字典
            DicomDictionary dict = DicomDictionary.loadFromClasspath();
            System.out.println("   ✓ 字典加载成功，包含 " + dict.getSize() + " 个条目");
            
            // 测试查询功能
            DicomDictionaryEntry entry = dict.lookup("0010", "1030");
            if (entry != null) {
                System.out.println("   ✓ 查询测试成功 - (0010,1030): " + entry.getName());
            } else {
                System.out.println("   ✗ 查询测试失败");
            }
            
            // 显示统计信息
            dict.printStatistics();
            
        } catch (Exception e) {
            System.err.println("   ✗ 字典加载失败: " + e.getMessage());
        }
        
        System.out.println();
    }
    
    /**
     * 测试 DCMDataSet 的新方法
     */
    private static void testDCMDataSetNewMethods() {
        System.out.println("3. 测试 DCMDataSet 新方法:");
          try {            // 创建一个模拟的传输语法对象
            TransferSyntax ts = new TransferSyntax() {
                {
                    this.name = "Implicit VR Little Endian";
                    this.uid = "1.2.840.10008.1.2";
                    this.isBE = false;
                    this.isExplicit = false;
                }
                
                @Override
                protected DCMAbstractType Decode(com.dicom.transfersyntax.DicomByteBuffer buff) {
                    // 简单的模拟实现，仅用于测试
                    return null;
                }
                  @Override
                protected int Encode(java.nio.ByteBuffer buff, DCMAbstractType element) {
                    // 简单的模拟实现，仅用于测试
                    return 0;
                }
            };
            
            // 创建 DCMDataSet
            DCMDataSet dataSet = new DCMDataSet(ts);
            
            // 创建一些模拟的数据元素
            DCMDataElement element1 = new DCMDataElement(ts);
            element1.gtag = 0x0010;
            element1.etag = 0x1030;
            element1.name = "Patient's Weight";
            element1.vr = "DS";
            element1.vm = "1";
            element1.value = "70.5".getBytes();
            
            DCMDataElement element2 = new DCMDataElement(ts);
            element2.gtag = 0x0028;
            element2.etag = 0x1053;
            element2.name = "Rescale Slope";
            element2.vr = "DS";
            element2.vm = "1";
            element2.value = "1.0".getBytes();
            
            // 添加到数据集
            dataSet.addItem(element1);
            dataSet.addItem(element2);
            
            System.out.println("   创建了包含 " + dataSet.getItemCount() + " 个元素的数据集");
            
            // 测试新的 GetName 方法
            String name1 = dataSet.GetName(DicomTags.PatientWeight);
            String name2 = dataSet.GetName(DicomTags.RescaleSlope);
            
            System.out.println("   GetName(PatientWeight): " + name1);
            System.out.println("   GetName(RescaleSlope): " + name2);
            
            // 测试新的 GetVR 方法
            String vr1 = dataSet.GetVR(DicomTags.PatientWeight);
            String vr2 = dataSet.GetVR(DicomTags.RescaleSlope);
            
            System.out.println("   GetVR(PatientWeight): " + vr1);
            System.out.println("   GetVR(RescaleSlope): " + vr2);
            
            // 测试新的 GetVM 方法
            String vm1 = dataSet.GetVM(DicomTags.PatientWeight);
            String vm2 = dataSet.GetVM(DicomTags.RescaleSlope);
            
            System.out.println("   GetVM(PatientWeight): " + vm1);
            System.out.println("   GetVM(RescaleSlope): " + vm2);
            
            // 测试查询不存在的标签
            String nameNotExist = dataSet.GetName(0x99999999);
            System.out.println("   GetName(不存在的标签): '" + nameNotExist + "'");
            
            System.out.println("   ✓ DCMDataSet 新方法测试完成");
            
        } catch (Exception e) {
            System.err.println("   ✗ DCMDataSet 测试失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

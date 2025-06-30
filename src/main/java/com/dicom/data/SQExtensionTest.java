package com.dicom.data;

import com.dicom.transfersyntax.ImplicitVRLittleEndian;

/**
 * SQ扩展功能测试程序
 * 测试DCMDataItem和DCMDataSequence类的功能
 */
public class SQExtensionTest {

    public static void main(String[] args) {
        System.out.println("=== SQ扩展功能测试程序 ===\n");
        
        // 测试1: 基本的DCMDataItem功能
        testDCMDataItem();
        
        // 测试2: 基本的DCMDataSequence功能
        testDCMDataSequence();
        
        // 测试3: 嵌套序列测试
        testNestedSequence();
        
        System.out.println("\n=== 所有测试完成 ===");
    }
    
    /**
     * 测试DCMDataItem基本功能
     */
    private static void testDCMDataItem() {
        System.out.println("1. 测试DCMDataItem基本功能:");
        
        try {
            TransferSyntax ts = new ImplicitVRLittleEndian();
            DCMDataItem item = new DCMDataItem(ts);
            
            // 创建测试数据元素
            DCMDataElement element1 = new DCMDataElement(ts);
            element1.gtag = (short) 0x0008;
            element1.etag = (short) 0x0060;
            element1.name = "Modality";
            element1.vr = "CS";
            element1.vm = "1";
            element1.value = "CT".getBytes();
            element1.length = element1.value.length;
            
            DCMDataElement element2 = new DCMDataElement(ts);
            element2.gtag = (short) 0x0020;
            element2.etag = (short) 0x0013;
            element2.name = "Instance Number";
            element2.vr = "IS";
            element2.vm = "1";
            element2.value = "1".getBytes();
            element2.length = element2.value.length;
            
            // 添加元素到条目
            item.addItem(element1);
            item.addItem(element2);
            
            System.out.println("   ✓ DCMDataItem创建成功");
            System.out.println("   ✓ 条目包含 " + item.getItemCount() + " 个元素");
            System.out.println("   ✓ ToString输出:");
            System.out.println(item.ToString("     "));
            
        } catch (Exception e) {
            System.err.println("   ✗ DCMDataItem测试失败: " + e.getMessage());
            e.printStackTrace();
        }
        System.out.println();
    }
    
    /**
     * 测试DCMDataSequence基本功能
     */
    private static void testDCMDataSequence() {
        System.out.println("2. 测试DCMDataSequence基本功能:");
        
        try {
            TransferSyntax ts = new ImplicitVRLittleEndian();
            DCMDataSequence sequence = new DCMDataSequence(ts);
            
            // 设置序列属性
            sequence.gtag = (short) 0x0008;
            sequence.etag = (short) 0x1140;
            sequence.name = "Referenced Image Sequence";
            sequence.vr = "SQ";
            sequence.vm = "1";
            sequence.length = 0;
            
            // 创建条目1
            DCMDataItem item1 = new DCMDataItem(ts);
            DCMDataElement elem1 = new DCMDataElement(ts);
            elem1.gtag = (short) 0x0008;
            elem1.etag = (short) 0x1150;
            elem1.name = "Referenced SOP Class UID";
            elem1.vr = "UI";
            elem1.value = "1.2.840.10008.5.1.4.1.1.2".getBytes();
            elem1.length = elem1.value.length;
            item1.addItem(elem1);
            
            // 创建条目2
            DCMDataItem item2 = new DCMDataItem(ts);
            DCMDataElement elem2 = new DCMDataElement(ts);
            elem2.gtag = (short) 0x0008;
            elem2.etag = (short) 0x1155;
            elem2.name = "Referenced SOP Instance UID";
            elem2.vr = "UI";
            elem2.value = "1.2.3.4.5.6.7.8.9".getBytes();
            elem2.length = elem2.value.length;
            item2.addItem(elem2);
            
            // 添加条目到序列
            sequence.addItem(item1);
            sequence.addItem(item2);
            
            System.out.println("   ✓ DCMDataSequence创建成功");
            System.out.println("   ✓ 序列包含 " + sequence.getItemCount() + " 个条目");
            System.out.println("   ✓ ToString输出:");
            System.out.println(sequence.ToString("     "));
            
        } catch (Exception e) {
            System.err.println("   ✗ DCMDataSequence测试失败: " + e.getMessage());
            e.printStackTrace();
        }
        System.out.println();
    }
    
    /**
     * 测试嵌套序列功能
     */
    private static void testNestedSequence() {
        System.out.println("3. 测试嵌套序列功能:");
        
        try {
            TransferSyntax ts = new ImplicitVRLittleEndian();
            
            // 创建外层数据集
            DCMDataSet dataSet = new DCMDataSet(ts);
            
            // 创建普通元素
            DCMDataElement normalElement = new DCMDataElement(ts);
            normalElement.gtag = (short) 0x0010;
            normalElement.etag = (short) 0x0010;
            normalElement.name = "Patient's Name";
            normalElement.vr = "PN";
            normalElement.value = "SMITH^JOHN".getBytes();
            normalElement.length = normalElement.value.length;
            
            // 创建序列元素
            DCMDataSequence sequence = new DCMDataSequence(ts);
            sequence.gtag = (short) 0x0008;
            sequence.etag = (short) 0x1140;
            sequence.name = "Referenced Image Sequence";
            sequence.vr = "SQ";
            sequence.vm = "1";
            
            // 创建序列中的条目
            DCMDataItem item = new DCMDataItem(ts);
            DCMDataElement seqElement = new DCMDataElement(ts);
            seqElement.gtag = (short) 0x0008;
            seqElement.etag = (short) 0x1150;
            seqElement.name = "Referenced SOP Class UID";
            seqElement.vr = "UI";
            seqElement.value = "1.2.840.10008.5.1.4.1.1.2".getBytes();
            seqElement.length = seqElement.value.length;
            item.addItem(seqElement);
            sequence.addItem(item);
            
            // 添加到数据集
            dataSet.addItem(normalElement);
            dataSet.addItem(sequence);
            
            System.out.println("   ✓ 嵌套序列创建成功");
            System.out.println("   ✓ 数据集包含 " + dataSet.getItemCount() + " 个元素");
            System.out.println("   ✓ 完整ToString输出:");
            System.out.println(dataSet.ToString("     "));
            
        } catch (Exception e) {
            System.err.println("   ✗ 嵌套序列测试失败: " + e.getMessage());
            e.printStackTrace();
        }
        System.out.println();
    }
}

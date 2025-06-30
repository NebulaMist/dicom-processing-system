package com.dicom.data;

import com.dicom.transfersyntax.ExplicitVRBigEndian;
import com.dicom.dictionary.DicomTags;

/**
 * 真正的定长vs未定长编码差异测试
 * 专门测试SQ序列的编码差异
 */
public class RealLengthDifferenceTest {
    
    public static void main(String[] args) {
        System.out.println("=== 真正的定长vs未定长编码差异测试 ===\n");
        
        try {
            // 测试1: 创建包含SQ序列的数据集
            System.out.println("=== 测试：包含SQ序列的数据集编码差异 ===");
            
            // 创建主数据集
            DCMFile dcmFile = new DCMFile(new ExplicitVRBigEndian());
            
            // 添加基本信息
            ((DCMDataSet.DCMElementWrapper)dcmFile.Item(DicomTags.PatientName)).SetValue("Test^Patient^^");
            ((DCMDataSet.DCMElementWrapper)dcmFile.Item(DicomTags.PatientID)).SetValue("TEST001");
            ((DCMDataSet.DCMElementWrapper)dcmFile.Item(DicomTags.Modality)).SetValue("CT");
            
            // 创建Referenced Image Sequence (0008,1140) - 这是一个典型的SQ序列
            DCMDataSequence referencedImageSeq = new DCMDataSequence(new ExplicitVRBigEndian());
            referencedImageSeq.gtag = (short) 0x0008;
            referencedImageSeq.etag = (short) 0x1140;
            referencedImageSeq.vr = "SQ";
            referencedImageSeq.name = "Referenced Image Sequence";
            
            // 创建序列中的第一个条目
            DCMDataItem item1 = new DCMDataItem(new ExplicitVRBigEndian());
            ((DCMDataSet.DCMElementWrapper)item1.Item(0x00081150)).SetValue("1.2.840.10008.5.1.4.1.1.2"); // Referenced SOP Class UID
            ((DCMDataSet.DCMElementWrapper)item1.Item(0x00081155)).SetValue("1.2.3.4.5.6.7.8.9.1"); // Referenced SOP Instance UID
            
            // 创建序列中的第二个条目
            DCMDataItem item2 = new DCMDataItem(new ExplicitVRBigEndian());
            ((DCMDataSet.DCMElementWrapper)item2.Item(0x00081150)).SetValue("1.2.840.10008.5.1.4.1.1.2"); // Referenced SOP Class UID
            ((DCMDataSet.DCMElementWrapper)item2.Item(0x00081155)).SetValue("1.2.3.4.5.6.7.8.9.2"); // Referenced SOP Instance UID
            
            // 将条目添加到序列
            DCMDataItem[] items = {item1, item2};
            referencedImageSeq.SetValue(items);
            
            // 将序列添加到数据集
            dcmFile.addItem(referencedImageSeq);
            
            System.out.println("数据集创建完成，包含 " + items.length + " 个序列条目");
            
            // 显示数据集内容
            System.out.println("\n=== 数据集内容 ===");
            System.out.println(dcmFile.ToString(""));
            
            // 测试定长编码
            System.out.println("\n=== 保存定长编码文件 ===");
            boolean definedResult = dcmFile.Save("real_test_defined.dcm", false);
            System.out.println("定长编码保存结果: " + (definedResult ? "成功" : "失败"));
            
            // 测试未定长编码
            System.out.println("\n=== 保存未定长编码文件 ===");
            boolean undefinedResult = dcmFile.Save("real_test_undefined.dcm", true);
            System.out.println("未定长编码保存结果: " + (undefinedResult ? "成功" : "失败"));
            
            // 比较文件大小
            System.out.println("\n=== 文件大小比较 ===");
            java.io.File definedFile = new java.io.File("real_test_defined.dcm");
            java.io.File undefinedFile = new java.io.File("real_test_undefined.dcm");
            
            if (definedFile.exists() && undefinedFile.exists()) {
                long definedSize = definedFile.length();
                long undefinedSize = undefinedFile.length();
                
                System.out.println("定长编码文件大小: " + definedSize + " 字节");
                System.out.println("未定长编码文件大小: " + undefinedSize + " 字节");
                System.out.println("差异: " + (undefinedSize - definedSize) + " 字节");
                
                if (definedSize == undefinedSize) {
                    System.out.println("⚠️  警告：两个文件大小相同，可能编码逻辑有问题！");
                } else {
                    System.out.println("✓ 文件大小不同，编码差异正常");
                }
                
                // 详细的十六进制对比
                System.out.println("\n=== 详细的文件内容对比 ===");
                compareFileContents("real_test_defined.dcm", "real_test_undefined.dcm");
                
            } else {
                System.out.println("✗ 文件保存失败，无法比较");
            }
            
            // 额外测试：直接比较序列编码
            System.out.println("\n=== 直接序列编码对比 ===");
            try {
                byte[] seqDefined = referencedImageSeq.Encode(false);
                byte[] seqUndefined = referencedImageSeq.Encode(true);
                
                System.out.println("序列定长编码: " + seqDefined.length + " 字节");
                System.out.println("序列未定长编码: " + seqUndefined.length + " 字节");
                System.out.println("序列编码差异: " + (seqUndefined.length - seqDefined.length) + " 字节");
                
                if (seqDefined.length != seqUndefined.length) {
                    System.out.println("✓ 序列编码有差异");
                    System.out.println("\n定长编码前50字节:");
                    printHexData(seqDefined, 50);
                    System.out.println("\n未定长编码前50字节:");
                    printHexData(seqUndefined, 50);
                } else {
                    System.out.println("⚠️  序列编码无差异");
                }
            } catch (Exception e) {
                System.out.println("序列编码测试失败: " + e.getMessage());
                e.printStackTrace();
            }
            
        } catch (Exception e) {
            System.err.println("测试失败:");
            e.printStackTrace();
        }
    }
    
    /**
     * 比较两个文件的内容
     */
    private static void compareFileContents(String file1, String file2) {
        try {
            byte[] data1 = java.nio.file.Files.readAllBytes(java.nio.file.Paths.get(file1));
            byte[] data2 = java.nio.file.Files.readAllBytes(java.nio.file.Paths.get(file2));
            
            System.out.println("文件1 (" + file1 + "): " + data1.length + " 字节");
            System.out.println("文件2 (" + file2 + "): " + data2.length + " 字节");
            
            // 找到第一个不同的字节
            int diffPos = -1;
            int minLen = Math.min(data1.length, data2.length);
            
            for (int i = 0; i < minLen; i++) {
                if (data1[i] != data2[i]) {
                    diffPos = i;
                    break;
                }
            }
            
            if (diffPos == -1 && data1.length != data2.length) {
                diffPos = minLen;
            }
            
            if (diffPos == -1) {
                System.out.println("⚠️  文件内容完全相同！");
            } else {
                System.out.println("✓ 文件在位置 " + diffPos + " 开始不同");
                
                // 显示差异位置周围的数据
                int startPos = Math.max(0, diffPos - 16);
                int endPos = Math.min(Math.max(data1.length, data2.length), diffPos + 32);
                
                System.out.println("\n文件1 在差异位置周围的数据:");
                if (startPos < data1.length) {
                    printHexData(java.util.Arrays.copyOfRange(data1, startPos, Math.min(data1.length, endPos)), endPos - startPos);
                }
                
                System.out.println("\n文件2 在差异位置周围的数据:");
                if (startPos < data2.length) {
                    printHexData(java.util.Arrays.copyOfRange(data2, startPos, Math.min(data2.length, endPos)), endPos - startPos);
                }
            }
            
        } catch (Exception e) {
            System.out.println("文件比较失败: " + e.getMessage());
        }
    }
    
    /**
     * 打印十六进制数据
     */
    private static void printHexData(byte[] data, int maxBytes) {
        int limit = Math.min(data.length, maxBytes);
        int bytesPerLine = 16;
        
        for (int i = 0; i < limit; i += bytesPerLine) {
            System.out.printf("%04X: ", i);
            
            for (int j = 0; j < bytesPerLine && (i + j) < limit; j++) {
                System.out.printf("%02X ", data[i + j] & 0xFF);
            }
            
            System.out.print(" | ");
            for (int j = 0; j < bytesPerLine && (i + j) < limit; j++) {
                byte b = data[i + j];
                if (b >= 32 && b <= 126) {
                    System.out.print((char) b);
                } else {
                    System.out.print(".");
                }
            }
            System.out.println();
        }
    }
}

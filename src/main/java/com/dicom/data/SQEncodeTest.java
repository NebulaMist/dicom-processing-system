package com.dicom.data;

import com.dicom.transfersyntax.ExplicitVRBigEndian;
import com.dicom.dictionary.DicomTags;

/**
 * SQ（序列）编码测试程序
 * 
 * 这个程序演示了：
 * 1. 如何创建DCMDataSequence序列
 * 2. 如何向序列中添加DCMDataItem条目
 * 3. 如何使用SetValue方法设置序列内容
 * 4. 如何编码SQ序列为字节数组
 * 5. 如何显示编码结果的十六进制表示
 */
public class SQEncodeTest {

    public static void main(String[] args) {
        System.out.println("=== SQ序列编码测试 ===\n");
        
        try {
            // 1. 创建主数据集
            DCMDataSet mainDataSet = new DCMDataSet(new ExplicitVRBigEndian());
            
            // 2. 添加基本的患者信息
            ((DCMDataSet.DCMElementWrapper)mainDataSet.Item(DicomTags.PatientName)).SetValue("Zhang^Wei^Ming^^Dr");
            ((DCMDataSet.DCMElementWrapper)mainDataSet.Item(DicomTags.PatientID)).SetValue("PATIENT_001");
            
            // 3. 创建一个SQ序列
            DCMDataSequence sequence = new DCMDataSequence(new ExplicitVRBigEndian());
            sequence.gtag = (short) 0x0008;  // 示例标签组号
            sequence.etag = (short) 0x1140;  // Referenced Image Sequence
            sequence.vr = "SQ";
            sequence.vm = "1";
            sequence.name = "Referenced Image Sequence";
            
            // 4. 创建序列中的条目
            DCMDataItem item1 = new DCMDataItem(new ExplicitVRBigEndian());
            // 在条目中添加一些数据元素
            ((DCMDataSet.DCMElementWrapper)item1.Item(DicomTags.StudyInstanceUID)).SetValue("1.2.3.4.5.6.7.8.9");
            ((DCMDataSet.DCMElementWrapper)item1.Item(DicomTags.SeriesInstanceUID)).SetValue("1.2.3.4.5.6.7.8.10");
            
            DCMDataItem item2 = new DCMDataItem(new ExplicitVRBigEndian());
            ((DCMDataSet.DCMElementWrapper)item2.Item(DicomTags.StudyInstanceUID)).SetValue("1.2.3.4.5.6.7.8.11");
            ((DCMDataSet.DCMElementWrapper)item2.Item(DicomTags.SeriesInstanceUID)).SetValue("1.2.3.4.5.6.7.8.12");
            
            // 5. 使用SetValue方法将条目数组设置到序列中
            DCMDataItem[] items = {item1, item2};
            sequence.SetValue(items);
            
            // 6. 将序列添加到主数据集中
            mainDataSet.addItem(sequence);
            
            System.out.println("=== 创建的数据集内容 ===");
            System.out.println(mainDataSet.ToString(""));
            
            // 7. 测试序列的编码
            System.out.println("\n=== SQ序列编码测试 ===");
            
            // 测试未定义长度编码
            System.out.println("1. 未定义长度编码:");
            byte[] undefinedLengthData = sequence.Encode(true);
            System.out.println("编码成功，数据长度: " + undefinedLengthData.length + " 字节");
            printHexData(undefinedLengthData, Math.min(undefinedLengthData.length, 200));
            
            // 测试确定长度编码
            System.out.println("\n2. 确定长度编码:");
            byte[] definedLengthData = sequence.Encode(false);
            System.out.println("编码成功，数据长度: " + definedLengthData.length + " 字节");
            printHexData(definedLengthData, Math.min(definedLengthData.length, 200));
            
            // 8. 测试整个数据集的编码
            System.out.println("\n=== 完整数据集编码测试 ===");
            byte[] fullDataSetEncoded = mainDataSet.Encode();
            System.out.println("完整数据集编码成功，数据长度: " + fullDataSetEncoded.length + " 字节");
            printHexData(fullDataSetEncoded, Math.min(fullDataSetEncoded.length, 300));
            
            // 9. 测试DCMFile保存功能
            System.out.println("\n=== DICOM文件保存测试 ===");
            DCMFile dcmFile = new DCMFile(new ExplicitVRBigEndian());
            // 将主数据集的内容复制到文件对象中
            for (DCMAbstractType item : mainDataSet.getItems()) {
                dcmFile.addItem(item);
            }
            
            boolean saveResult = dcmFile.Save("test_sq_output.dcm", true);
            System.out.println("DICOM文件保存结果: " + (saveResult ? "成功" : "失败"));
            
            System.out.println("\n=== 测试完成 ===");
            System.out.println("这个程序展示了SQ序列的完整编码过程，包括:");
            System.out.println("- DCMDataSequence.SetValue()方法的使用");
            System.out.println("- SQ序列的未定义长度和确定长度编码");
            System.out.println("- 嵌套数据结构的处理");
            System.out.println("- DICOM文件的保存功能");

        } catch (Exception e) {
            System.err.println("测试过程中出现错误：");
            e.printStackTrace();
        }
    }

    /**
     * 帮助方法：显示字节数组的十六进制表示（格式化输出）
     */
    private static void printHexData(byte[] data, int maxBytes) {
        System.out.println("编码数据的十六进制表示:");
        int limit = Math.min(data.length, maxBytes);
        int bytesPerLine = 16; // 每行显示16个字节
        
        for (int i = 0; i < limit; i += bytesPerLine) {
            // 打印行首地址
            System.out.printf("%04X: ", i);
            
            // 打印十六进制数据
            for (int j = 0; j < bytesPerLine && (i + j) < limit; j++) {
                if (j > 0 && j % 2 == 0) System.out.print(" "); // 每2个字节加空格
                System.out.printf("0x%02X,", data[i + j] & 0xFF);
            }
            
            // 补齐行的格式
            int remaining = bytesPerLine - Math.min(bytesPerLine, limit - i);
            for (int k = 0; k < remaining; k++) {
                System.out.print("     ");
                if (k > 0 && k % 2 == 1) System.out.print(" ");
            }
            
            // 打印可打印字符
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
        
        if (data.length > maxBytes) {
            System.out.println("... (还有 " + (data.length - maxBytes) + " 个字节未显示)");
        }
        System.out.println("总共 " + data.length + " 字节");
    }
}

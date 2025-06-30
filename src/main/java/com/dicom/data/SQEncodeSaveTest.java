package com.dicom.data;

import com.dicom.transfersyntax.ExplicitVRBigEndian;
import com.dicom.dictionary.DicomTags;

/**
 * SQ序列编码和保存测试
 * 测试创建包含SQ序列的DICOM文件并保存
 */
public class SQEncodeSaveTest {
    
    public static void main(String[] args) {
        System.out.println("=== SQ序列编码和保存测试 ===\n");
        
        try {
            // 1. 初始化DCMFile，使用ExplicitVRBigEndian传输语法
            DCMFile dcm = new DCMFile(new ExplicitVRBigEndian());

            System.out.println("开始创建包含SQ序列的DICOM数据集...\n");

            // 2. 添加基本的患者信息
            System.out.println("设置基本患者信息...");
            ((DCMDataSet.DCMElementWrapper)dcm.Item(DicomTags.PatientName)).SetValue("Test^Patient^^Dr");
            ((DCMDataSet.DCMElementWrapper)dcm.Item(DicomTags.PatientID)).SetValue("TEST_ID_001");
            ((DCMDataSet.DCMElementWrapper)dcm.Item(DicomTags.StudyDate)).SetValue("20250627");
            ((DCMDataSet.DCMElementWrapper)dcm.Item(DicomTags.Modality)).SetValue("CT");

            // 3. 创建SQ序列数据
            System.out.println("创建SQ序列数据...");
            
            // 创建第一个条目 (Item 1)
            DCMDataItem item1 = new DCMDataItem(new ExplicitVRBigEndian());
            ((DCMDataSet.DCMElementWrapper)item1.Item(0x00081150)).SetValue("1.2.840.10008.5.1.4.1.1.2"); // Referenced SOP Class UID
            ((DCMDataSet.DCMElementWrapper)item1.Item(0x00081155)).SetValue("1.2.840.113619.2.65.1.1026668353.12"); // Referenced SOP Instance UID
            
            // 创建第二个条目 (Item 2)
            DCMDataItem item2 = new DCMDataItem(new ExplicitVRBigEndian());
            ((DCMDataSet.DCMElementWrapper)item2.Item(0x00081150)).SetValue("1.2.840.10008.5.1.4.1.1.2"); // Referenced SOP Class UID
            ((DCMDataSet.DCMElementWrapper)item2.Item(0x00081155)).SetValue("1.2.840.113619.2.65.1.1026668353.13"); // Referenced SOP Instance UID
            
            // 创建序列数组
            DCMDataItem[] items = {item1, item2};
            
            // 创建SQ序列
            DCMDataSequence sequence = new DCMDataSequence(new ExplicitVRBigEndian());
            sequence.gtag = (short) 0x0008;
            sequence.etag = (short) 0x1140;
            sequence.vr = "SQ";
            sequence.name = "Referenced Image Sequence";
            sequence.SetValue(items);
            
            // 添加序列到数据集
            dcm.addItem(sequence);
            
            System.out.println("SQ序列创建完成，包含 " + items.length + " 个条目");

            // 4. 显示创建的数据集
            System.out.println("\n=== 创建的DICOM数据集内容 ===");
            System.out.println(dcm.ToString(""));

            // 5. 编码测试
            System.out.println("\n=== 编码测试 ===");
            byte[] encodedData = dcm.Encode();
            System.out.println("编码成功，数据长度: " + encodedData.length + " 字节");
            
            // 6. 显示编码结果的十六进制表示
            System.out.println("\n=== 编码结果（十六进制前100字节） ===");
            printHexData(encodedData, 100);

            // 7. 保存DICOM文件测试
            System.out.println("\n=== 保存DICOM文件测试 ===");
            
            // 保存确定长度编码的文件
            System.out.println("保存确定长度编码文件...");
            if (dcm.Save("sq_test_defined.dcm", false)) {
                System.out.println("✓ 确定长度文件保存成功: sq_test_defined.dcm");
            } else {
                System.out.println("✗ 确定长度文件保存失败");
            }
            
            // 保存未定义长度编码的文件
            System.out.println("保存未定义长度编码文件...");
            if (dcm.Save("sq_test_undefined.dcm", true)) {
                System.out.println("✓ 未定义长度文件保存成功: sq_test_undefined.dcm");
            } else {
                System.out.println("✗ 未定义长度文件保存失败");
            }

            // 8. 验证保存的文件
            System.out.println("\n=== 验证保存的文件 ===");
            testSavedFile("sq_test_defined.dcm", "确定长度");
            testSavedFile("sq_test_undefined.dcm", "未定义长度");

            System.out.println("\n=== 测试完成 ===");
            System.out.println("生成的文件可以用DICOM查看器打开验证内容。");

        } catch (Exception e) {
            System.err.println("测试过程中出现错误：");
            e.printStackTrace();
        }
    }

    /**
     * 测试保存的文件是否可以正确解析
     */
    private static void testSavedFile(String filename, String description) {
        try {
            System.out.println("验证" + description + "文件: " + filename);
            DCMFile testFile = new DCMFile(filename);
            if (testFile.Parse()) {
                System.out.println("  ✓ 文件解析成功");
                
                // 检查一些关键字段
                Object patientName = testFile.GetValue(DicomTags.PatientName);
                Object patientID = testFile.GetValue(DicomTags.PatientID);
                
                System.out.println("  - 患者姓名: " + patientName);
                System.out.println("  - 患者ID: " + patientID);
                System.out.println("  - 总元素数: " + testFile.getItemCount());
            } else {
                System.out.println("  ✗ 文件解析失败");
            }
        } catch (Exception e) {
            System.out.println("  ✗ 验证失败: " + e.getMessage());
        }
        System.out.println();
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

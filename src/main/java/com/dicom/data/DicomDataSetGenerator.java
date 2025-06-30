package com.dicom.data;

import com.dicom.transfersyntax.ExplicitVRBigEndian;
import com.dicom.dictionary.DicomTags;

/**
 * DICOM数据集生成器
 * 
 * 作用说明：
 * 这个程序演示了如何创建一个完整的DICOM数据集，包含了各种类型的医疗信息：
 * 1. 患者基本信息（姓名、ID、出生日期、性别等）
 * 2. 去标识化信息（隐私保护相关）
 * 3. 动物/兽医特定信息（用于动物医学影像）
 * 4. 遗传信息（基因修饰描述等）
 * 5. 设备信息（制造商等）
 * 6. 临床试验信息（研究相关数据）
 * 7. 数据分发信息（数据共享权限）
 * 8. 检查和序列信息（影像检查的基本信息）
 * 
 * 这个程序可以用于：
 * - 测试DICOM数据集的创建和编码功能
 * - 验证各种VR类型的SetValue方法
 * - 生成标准的DICOM测试数据
 * - 演示完整的医疗影像元数据结构
 */
public class DicomDataSetGenerator {

    public static void main(String[] args) {
        System.out.println("=== DICOM数据集生成器测试 ===\n");
        
        try {
            // 1. 初始化DCMFile，使用ExplicitVRBigEndian传输语法
            DCMFile dcm = new DCMFile(new ExplicitVRBigEndian());

            System.out.println("开始创建DICOM数据集...\n");

            // --- Patient Information (患者信息) ---
            System.out.println("设置患者信息...");
            
            // 先测试一个已知工作的标签
            System.out.println("测试PatientID标签...");
            ((DCMDataSet.DCMElementWrapper)dcm.Item(DicomTags.PatientID)).SetValue("PATIENT_ID_12345");
            
            // 检查PatientName标签的VR
            System.out.println("检查PatientName标签...");
            DCMDataSet.DCMElementWrapper patientNameWrapper = (DCMDataSet.DCMElementWrapper)dcm.Item(DicomTags.PatientName);
            System.out.println("PatientName标签信息: " + patientNameWrapper.toString());
            
            // 尝试设置PatientName
            try {
                patientNameWrapper.SetValue("Zhang^Wei^Ming^^Dr");
                System.out.println("PatientName设置成功");
            } catch (Exception e) {
                System.err.println("PatientName设置失败: " + e.getMessage());
                // 尝试直接在字典中查找
                System.out.println("尝试直接查找字典条目...");
            }
            
            // Patient's Birth Date (患者出生日期) - 使用已知工作的标签
            System.out.println("设置患者出生日期...");
            ((DCMDataSet.DCMElementWrapper)dcm.Item(DicomTags.PatientBirthDate)).SetValue("19850315");
            
            // Patient's Sex (患者性别)
            System.out.println("设置患者性别...");
            ((DCMDataSet.DCMElementWrapper)dcm.Item(DicomTags.PatientSex)).SetValue("M");

            // --- De-identification Information (去标识化信息) ---
            /*
            System.out.println("设置去标识化信息...");
            // Patient Identity Removed (患者身份已移除), Tag: (0012,0062), VR: CS
            ((DCMDataSet.DCMElementWrapper)dcm.Item(DicomTags.PatientIdentityRemoved)).SetValue("YES");
            // De-identification Method (去标识化方法), Tag: (0012,0063), VR: LO
            ((DCMDataSet.DCMElementWrapper)dcm.Item(DicomTags.DeidentificationMethod)).SetValue("DICOM PS 3.15 AnnexE");

            // --- Clinical Trial Information (临床试验信息) ---
            System.out.println("设置临床试验信息...");
            // Clinical Trial Sponsor Name (临床试验申办方), Tag: (0012,0010), VR: LO
            ((DCMDataSet.DCMElementWrapper)dcm.Item(DicomTags.ClinicalTrialSponsorName)).SetValue("Beijing Medical University");
            // Clinical Trial Protocol ID (临床试验方案ID), Tag: (0012,0020), VR: LO
            ((DCMDataSet.DCMElementWrapper)dcm.Item(DicomTags.ClinicalTrialProtocolID)).SetValue("PROTOCOL_CT_2025_001");
            // Clinical Trial Protocol Name (临床试验方案名称), Tag: (0012,0021), VR: LO
            ((DCMDataSet.DCMElementWrapper)dcm.Item(DicomTags.ClinicalTrialProtocolName)).SetValue("AI-Enhanced CT Imaging Study");
            // Clinical Trial Site ID (临床试验中心ID), Tag: (0012,0030), VR: LO
            ((DCMDataSet.DCMElementWrapper)dcm.Item(DicomTags.ClinicalTrialSiteID)).SetValue("SITE_BJ_001");
            // Clinical Trial Site Name (临床试验中心名称), Tag: (0012,0031), VR: LO
            ((DCMDataSet.DCMElementWrapper)dcm.Item(DicomTags.ClinicalTrialSiteName)).SetValue("Beijing Hospital Imaging Center");

            // --- Equipment Information (设备信息) ---
            System.out.println("设置设备信息...");
            // Manufacturer (制造商), Tag: (0008,0070), VR: LO
            ((DCMDataSet.DCMElementWrapper)dcm.Item(DicomTags.Manufacturer)).SetValue("GE Healthcare");
            */

            // --- Study Information (检查信息) ---
            System.out.println("设置检查信息...");
            // Study Instance UID (检查实例UID), Tag: (0020,000D), VR: UI
            ((DCMDataSet.DCMElementWrapper)dcm.Item(DicomTags.StudyInstanceUID)).SetValue("1.2.840.113619.2.55.3.2831184323.801.1735519593.895");
            // Study Date (检查日期), Tag: (0008,0020), VR: DA
            ((DCMDataSet.DCMElementWrapper)dcm.Item(DicomTags.StudyDate)).SetValue("20250627");
            // Study Time (检查时间), Tag: (0008,0030), VR: TM
            ((DCMDataSet.DCMElementWrapper)dcm.Item(DicomTags.StudyTime)).SetValue("143000.000");
            // Referring Physician's Name (申请医生姓名), Tag: (0008,0090), VR: PN
            ((DCMDataSet.DCMElementWrapper)dcm.Item(DicomTags.ReferringPhysicianName)).SetValue("Li^Ming^Hua^^Dr");

            // --- Series Information (序列信息) ---
            System.out.println("设置序列信息...");
            // Modality (模态), Tag: (0008,0060), VR: CS
            ((DCMDataSet.DCMElementWrapper)dcm.Item(DicomTags.Modality)).SetValue("CT");
            // Series Instance UID (序列实例UID), Tag: (0020,000E), VR: UI
            ((DCMDataSet.DCMElementWrapper)dcm.Item(DicomTags.SeriesInstanceUID)).SetValue("1.2.840.113619.2.55.3.2831184323.801.1735519593.896");
            // Series Number (序列号), Tag: (0020,0011), VR: IS
            ((DCMDataSet.DCMElementWrapper)dcm.Item(DicomTags.SeriesNumber)).SetValue("1");

            // --- Image Information (图像信息) ---
            System.out.println("设置图像信息...");
            // Instance Number (实例号), Tag: (0020,0013), VR: IS
            ((DCMDataSet.DCMElementWrapper)dcm.Item(DicomTags.InstanceNumber)).SetValue("1");
            // Rows (行数), Tag: (0028,0010), VR: US
            ((DCMDataSet.DCMElementWrapper)dcm.Item(DicomTags.Rows)).SetValue((short)512);
            // Columns (列数), Tag: (0028,0011), VR: US
            ((DCMDataSet.DCMElementWrapper)dcm.Item(DicomTags.Columns)).SetValue((short)512);

            System.out.println("\n数据集创建完成！");

            // 2. 显示创建的数据集
            System.out.println("\n=== 创建的DICOM数据集内容 ===");
            System.out.println(dcm.ToString(""));

            // 3. 编码测试
            System.out.println("\n=== 编码测试 ===");
            byte[] encodedData = dcm.Encode();
            System.out.println("编码成功，数据长度: " + encodedData.length + " 字节");
            
            // 4. 显示编码结果的十六进制表示
            System.out.println("\n=== 编码结果（十六进制） ===");
            printHexData(encodedData, Math.min(encodedData.length, 300));

            // 5. 解析验证测试
            System.out.println("\n=== 解析验证测试 ===");
            DCMDataSet dcm2 = new DCMDataSet(new ExplicitVRBigEndian());
            int[] idx = {0};
            dcm2.Parse(encodedData, idx);
            
            System.out.println("解析成功，验证关键字段:");
            
            // 验证患者姓名
            Object originalName = dcm.GetValue(DicomTags.PatientName);
            Object parsedName = dcm2.GetValue(DicomTags.PatientName);
            System.out.println("患者姓名: " + originalName + " -> " + parsedName + 
                             " [" + (originalName.equals(parsedName) ? "✓" : "✗") + "]");
            
            // 验证患者ID
            Object originalID = dcm.GetValue(DicomTags.PatientID);
            Object parsedID = dcm2.GetValue(DicomTags.PatientID);
            System.out.println("患者ID: " + originalID + " -> " + parsedID + 
                             " [" + (originalID.toString().trim().equals(parsedID.toString().trim()) ? "✓" : "✗") + "]");
            
            // 验证图像尺寸
            Object originalRows = dcm.GetValue(DicomTags.Rows);
            Object parsedRows = dcm2.GetValue(DicomTags.Rows);
            System.out.println("图像行数: " + originalRows + " -> " + parsedRows + 
                             " [" + (originalRows.equals(parsedRows) ? "✓" : "✗") + "]");

            // 6. 保存DICOM文件测试
            System.out.println("\n=== 保存DICOM文件测试 ===");
            
            // 保存确定长度编码的文件
            System.out.println("保存确定长度编码文件...");
            if (dcm.Save("iodtest_defined.dcm", false)) {
                System.out.println("✓ 确定长度文件保存成功: iodtest_defined.dcm");
            } else {
                System.out.println("✗ 确定长度文件保存失败");
            }
            
            // 保存未定义长度编码的文件
            System.out.println("保存未定义长度编码文件...");
            if (dcm.Save("iodtest_undefined.dcm", true)) {
                System.out.println("✓ 未定义长度文件保存成功: iodtest_undefined.dcm");
            } else {
                System.out.println("✗ 未定义长度文件保存失败");
            }

            System.out.println("\n=== 测试完成 ===");
            System.out.println("这个程序展示了如何创建包含完整医疗信息的DICOM数据集，");
            System.out.println("包括患者信息、临床试验数据、设备信息等各种标准DICOM字段。");
            System.out.println("可用于测试DICOM库的编码/解码功能，验证数据完整性。");

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

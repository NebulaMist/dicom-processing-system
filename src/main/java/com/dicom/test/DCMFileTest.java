package com.dicom.test;

import com.dicom.data.*;
import com.dicom.dictionary.DicomTags;
import java.io.*;

/**
 * DICOM文件处理测试程序
 * 测试DCMFile和DCMFileMeta类的功能
 */
public class DCMFileTest {
    
    public static void main(String[] args) {
        System.out.println("=== DICOM文件处理功能测试 ===\n");
        
        // 测试1：创建模拟DICOM文件
        System.out.println("1. 创建模拟DICOM文件...");
        String testFilePath = "test_dicom.dcm";
        if (createMockDICOMFile(testFilePath)) {
            System.out.println("   ✓ 模拟DICOM文件创建成功: " + testFilePath);
        } else {
            System.err.println("   ✗ 模拟DICOM文件创建失败");
            return;
        }
        
        // 测试2：解析DICOM文件
        System.out.println("\n2. 测试DCMFile解析功能...");
        testDCMFileParsing(testFilePath);
        
        // 测试3：测试DCMFileMeta功能
        System.out.println("\n3. 测试DCMFileMeta功能...");
        testDCMFileMeta();
        
        // 测试4：测试访问方法
        System.out.println("\n4. 测试数据访问方法...");
        testDataAccess(testFilePath);
        
        System.out.println("\n=== 所有测试完成 ===");
    }
    
    /**
     * 创建一个简单的模拟DICOM文件
     * @param filename 文件名
     * @return 是否成功
     */
    private static boolean createMockDICOMFile(String filename) {
        try (FileOutputStream fos = new FileOutputStream(filename)) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            
            // 1. 写入128字节前导（全零）
            byte[] preamble = new byte[128];
            baos.write(preamble);
            
            // 2. 写入"DICM"标识符
            baos.write("DICM".getBytes("ASCII"));
            
            // 3. 写入文件头元素（组号0x0002）
            // 文件头元素使用显式VR小端序格式
            
            // (0002,0001) File Meta Information Version = 00\01
            writeFileMetaElement(baos, 0x0002, 0x0001, "OB", new byte[]{0x00, 0x01});
            
            // (0002,0002) Media Storage SOP Class UID = CT Image Storage
            String sopClassUID = "1.2.840.10008.5.1.4.1.1.2";
            writeFileMetaElement(baos, 0x0002, 0x0002, "UI", sopClassUID.getBytes("ASCII"));
            
            // (0002,0003) Media Storage SOP Instance UID
            String sopInstanceUID = "1.2.3.4.5.6.7.8.9.0.1.2.3.4.5";
            writeFileMetaElement(baos, 0x0002, 0x0003, "UI", sopInstanceUID.getBytes("ASCII"));
            
            // (0002,0010) Transfer Syntax UID = Implicit VR Little Endian
            String transferSyntaxUID = "1.2.840.10008.1.2";
            writeFileMetaElement(baos, 0x0002, 0x0010, "UI", transferSyntaxUID.getBytes("ASCII"));
            
            // (0002,0012) Implementation Class UID
            String implClassUID = "1.2.3.4.5.TEST";
            writeFileMetaElement(baos, 0x0002, 0x0012, "UI", implClassUID.getBytes("ASCII"));
            
            // (0002,0013) Implementation Version Name
            String implVersionName = "TEST_VERSION_1.0";
            writeFileMetaElement(baos, 0x0002, 0x0013, "SH", implVersionName.getBytes("ASCII"));
            
            // 4. 写入数据集元素（使用隐式VR小端序）
            
            // (0008,0016) SOP Class UID
            writeDatasetElement(baos, 0x0008, 0x0016, sopClassUID.getBytes("ASCII"));
            
            // (0008,0018) SOP Instance UID  
            writeDatasetElement(baos, 0x0008, 0x0018, sopInstanceUID.getBytes("ASCII"));
            
            // (0008,0020) Study Date
            writeDatasetElement(baos, 0x0008, 0x0020, "20240101".getBytes("ASCII"));
            
            // (0008,0060) Modality
            writeDatasetElement(baos, 0x0008, 0x0060, "CT".getBytes("ASCII"));
            
            // (0010,0010) Patient's Name
            writeDatasetElement(baos, 0x0010, 0x0010, "Test^Patient".getBytes("ASCII"));
            
            // (0010,0020) Patient ID
            writeDatasetElement(baos, 0x0010, 0x0020, "12345".getBytes("ASCII"));
            
            // (0028,0010) Rows
            writeDatasetElement(baos, 0x0028, 0x0010, shortToBytes(512));
            
            // (0028,0011) Columns
            writeDatasetElement(baos, 0x0028, 0x0011, shortToBytes(512));
            
            // 写入文件
            fos.write(baos.toByteArray());
            return true;
            
        } catch (IOException e) {
            System.err.println("创建模拟DICOM文件失败: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 写入文件头元素（显式VR格式）
     */
    private static void writeFileMetaElement(ByteArrayOutputStream baos, int gtag, int etag, String vr, byte[] value) throws IOException {
        // 标签（4字节，小端序）
        baos.write(gtag & 0xFF);
        baos.write((gtag >> 8) & 0xFF);
        baos.write(etag & 0xFF);
        baos.write((etag >> 8) & 0xFF);
        
        // VR（2字节）
        baos.write(vr.getBytes("ASCII"));
        
        // 保留字段（2字节）
        baos.write(0x00);
        baos.write(0x00);
        
        // 长度（4字节，小端序）
        int length = value.length;
        baos.write(length & 0xFF);
        baos.write((length >> 8) & 0xFF);
        baos.write((length >> 16) & 0xFF);
        baos.write((length >> 24) & 0xFF);
        
        // 值
        baos.write(value);
    }
    
    /**
     * 写入数据集元素（隐式VR格式）
     */
    private static void writeDatasetElement(ByteArrayOutputStream baos, int gtag, int etag, byte[] value) throws IOException {
        // 标签（4字节，小端序）
        baos.write(gtag & 0xFF);
        baos.write((gtag >> 8) & 0xFF);
        baos.write(etag & 0xFF);
        baos.write((etag >> 8) & 0xFF);
        
        // 长度（4字节，小端序）
        int length = value.length;
        baos.write(length & 0xFF);
        baos.write((length >> 8) & 0xFF);
        baos.write((length >> 16) & 0xFF);
        baos.write((length >> 24) & 0xFF);
        
        // 值
        baos.write(value);
    }
    
    /**
     * 转换short为字节数组
     */
    private static byte[] shortToBytes(int value) {
        return new byte[] {
            (byte)(value & 0xFF),
            (byte)((value >> 8) & 0xFF)
        };
    }
    
    /**
     * 测试DCMFile解析功能
     */
    private static void testDCMFileParsing(String filename) {
        try {
            DCMFile dcmFile = new DCMFile(filename);
            
            System.out.println("   开始解析文件: " + filename);
            boolean success = dcmFile.Parse();
            
            if (success) {
                System.out.println("   ✓ 文件解析成功");
                System.out.println("   ✓ 有效DICOM文件: " + dcmFile.isValidDICOMFile());
                System.out.println("   ✓ 数据集元素数量: " + dcmFile.getItemCount());
                
                // 保存解析报告
                dcmFile.saveReport("test_dicom_report.txt");
                
            } else {
                System.err.println("   ✗ 文件解析失败");
            }
            
        } catch (Exception e) {
            System.err.println("   ✗ 解析过程中出错: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 测试DCMFileMeta功能
     */
    private static void testDCMFileMeta() {
        try {
            // 创建测试用的文件头数据
            byte[] testData = createFileMetaTestData();
            int[] idx = {0};
            
            DCMFileMeta fileMeta = new DCMFileMeta(new com.dicom.transfersyntax.ExplicitVRLittleEndian());
            fileMeta.Parse(testData, idx);
            
            System.out.println("   ✓ DCMFileMeta解析成功");
            System.out.println("   ✓ 文件头元素数量: " + fileMeta.getItemCount());
            System.out.println("   ✓ 传输语法UID: " + fileMeta.getTransferSyntaxUID());
            System.out.println("   ✓ 媒体存储SOP类UID: " + fileMeta.getMediaStorageSOPClassUID());
            
        } catch (Exception e) {
            System.err.println("   ✗ DCMFileMeta测试失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 创建文件头测试数据
     */
    private static byte[] createFileMetaTestData() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        
        // (0002,0001) File Meta Information Version
        writeFileMetaElement(baos, 0x0002, 0x0001, "OB", new byte[]{0x00, 0x01});
        
        // (0002,0010) Transfer Syntax UID
        String transferSyntaxUID = "1.2.840.10008.1.2";
        writeFileMetaElement(baos, 0x0002, 0x0010, "UI", transferSyntaxUID.getBytes("ASCII"));
        
        return baos.toByteArray();
    }
    
    /**
     * 测试数据访问方法
     */
    private static void testDataAccess(String filename) {
        try {
            DCMFile dcmFile = new DCMFile(filename);
            dcmFile.Parse();
            
            System.out.println("   测试数据访问方法:");
            System.out.println("   患者姓名: '" + dcmFile.getPatientName() + "'");
            System.out.println("   患者ID: '" + dcmFile.getPatientID() + "'");
            System.out.println("   检查日期: '" + dcmFile.getStudyDate() + "'");
            System.out.println("   模态: '" + dcmFile.getModality() + "'");
            System.out.println("   图像尺寸: " + dcmFile.getRows() + " x " + dcmFile.getColumns());
              // 测试通用访问方法
            String patientName = dcmFile.GetValue(DicomTags.PatientName);
            String modality = dcmFile.GetValue(DicomTags.Modality);
            
            System.out.println("   通过DicomTags获取患者姓名: '" + patientName + "'");
            System.out.println("   通过DicomTags获取模态: '" + modality + "'");
            
            System.out.println("   ✓ 数据访问测试完成");
            
        } catch (Exception e) {
            System.err.println("   ✗ 数据访问测试失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

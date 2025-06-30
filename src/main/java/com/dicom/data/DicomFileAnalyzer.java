package com.dicom.data;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * DICOM文件分析工具
 * 用于检查生成的DICOM文件内容
 */
public class DicomFileAnalyzer {
    
    public static void main(String[] args) {
        System.out.println("=== DICOM文件内容分析 ===\n");
        
        // 分析所有生成的DICOM文件
        String[] files = {
            "iodtest_defined.dcm",
            "iodtest_undefined.dcm", 
            "length_test_defined.dcm",
            "length_test_undefined.dcm"
        };
        
        for (String filename : files) {
            analyzeFile(filename);
            System.out.println();
        }
    }
    
    public static void analyzeFile(String filename) {
        System.out.println("=== 分析文件: " + filename + " ===");
        
        try {
            File file = new File(filename);
            if (!file.exists()) {
                System.out.println("文件不存在: " + filename);
                return;
            }
            
            byte[] data = Files.readAllBytes(Paths.get(filename));
            System.out.println("文件大小: " + data.length + " 字节");
            
            if (data.length == 0) {
                System.out.println("⚠️  文件为空！");
                return;
            }
            
            // 检查DICOM文件头
            checkDicomHeader(data);
            
            // 显示文件的前128字节内容
            System.out.println("\n文件前128字节的十六进制内容：");
            printHexData(data, Math.min(128, data.length));
            
            // 检查是否全为0
            boolean allZeros = true;
            for (byte b : data) {
                if (b != 0) {
                    allZeros = false;
                    break;
                }
            }
            
            if (allZeros) {
                System.out.println("⚠️  警告：文件内容全为0！");
            } else {
                System.out.println("✓ 文件包含非零数据");
            }
            
            // 尝试解析DICOM文件
            tryParseDicom(filename);
            
        } catch (IOException e) {
            System.err.println("读取文件失败: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("分析文件时出错: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static void checkDicomHeader(byte[] data) {
        System.out.println("\n=== DICOM文件头检查 ===");
        
        if (data.length < 132) {
            System.out.println("⚠️  文件太小，不包含完整的DICOM头部");
            return;
        }
        
        // 检查前128字节（DICOM前导）
        boolean preambleAllZeros = true;
        for (int i = 0; i < 128; i++) {
            if (data[i] != 0) {
                preambleAllZeros = false;
                break;
            }
        }
        
        if (preambleAllZeros) {
            System.out.println("✓ DICOM前导（前128字节）：全为0（正常）");
        } else {
            System.out.println("⚠️  DICOM前导包含非零数据");
        }
        
        // 检查DICM标识符
        if (data.length >= 132) {
            String dicm = new String(data, 128, 4);
            if ("DICM".equals(dicm)) {
                System.out.println("✓ DICM标识符：正确");
            } else {
                System.out.println("⚠️  DICM标识符错误：" + dicm);
                // 显示实际的字节值
                System.out.printf("实际字节值: [%02X %02X %02X %02X]\n", 
                    data[128] & 0xFF, data[129] & 0xFF, data[130] & 0xFF, data[131] & 0xFF);
            }
        }
    }
    
    private static void tryParseDicom(String filename) {
        System.out.println("\n=== DICOM解析测试 ===");
        try {
            DCMFile dcmFile = new DCMFile(filename);
            if (dcmFile.Parse()) {
                System.out.println("✓ DICOM文件解析成功");
                System.out.println("数据元素数量: " + dcmFile.getItemCount());
                
                // 尝试获取一些基本信息
                Object patientName = dcmFile.GetValue(0x00100010); // Patient Name
                Object patientID = dcmFile.GetValue(0x00100020);   // Patient ID
                
                if (patientName != null) {
                    System.out.println("患者姓名: " + patientName);
                }
                if (patientID != null) {
                    System.out.println("患者ID: " + patientID);
                }
            } else {
                System.out.println("⚠️  DICOM文件解析失败");
            }
        } catch (Exception e) {
            System.out.println("⚠️  DICOM解析出错: " + e.getMessage());
        }
    }
    
    private static void printHexData(byte[] data, int maxBytes) {
        int limit = Math.min(data.length, maxBytes);
        int bytesPerLine = 16;
        
        for (int i = 0; i < limit; i += bytesPerLine) {
            System.out.printf("%04X: ", i);
            
            // 打印十六进制
            for (int j = 0; j < bytesPerLine && (i + j) < limit; j++) {
                if (j > 0) System.out.print(" ");
                System.out.printf("%02X", data[i + j] & 0xFF);
            }
            
            // 补齐空格
            int remaining = bytesPerLine - Math.min(bytesPerLine, limit - i);
            for (int k = 0; k < remaining; k++) {
                System.out.print("   ");
            }
            
            // 打印可见字符
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
            System.out.println("... (省略剩余 " + (data.length - maxBytes) + " 字节)");
        }
    }
}

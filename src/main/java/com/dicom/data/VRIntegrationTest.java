package com.dicom.data;

import com.dicom.transfersyntax.ExplicitVRBigEndian;
import com.dicom.transfersyntax.ExplicitVRLittleEndian;
import com.dicom.transfersyntax.ImplicitVRLittleEndian;

/**
 * VR类与传输语法集成测试
 * 测试VR解析器在不同传输语法中的使用
 */
public class VRIntegrationTest {
    
    public static void main(String[] args) {
        System.out.println("=== VR类与传输语法集成测试 ===\n");
        
        // 测试显式VR大端数据
        testExplicitVRBigEndian();
        
        // 测试隐式VR小端数据（默认传输语法）
        testImplicitVRLittleEndian();
        
        // 测试显式VR小端数据
        testExplicitVRLittleEndian();
        
        System.out.println("\n=== 集成测试完成 ===");
    }
    
    /**
     * 测试显式VR,Big Endian数据
     * 数据：00 10 10 10 41 53 00 04 30 36 35 59 00 40 40 10 44 54 00 0C 32 30 31 39 30 35 31 37 31 33 30 37
     */
    private static void testExplicitVRBigEndian() {
        System.out.println("1. 显式VR Big Endian测试:");
        
        byte[] data = {
            (byte)0x00, (byte)0x10, (byte)0x10, (byte)0x10, // Tag: (0010,1010)
            (byte)0x41, (byte)0x53,                         // VR: AS
            (byte)0x00, (byte)0x04,                         // Length: 4
            (byte)0x30, (byte)0x36, (byte)0x35, (byte)0x59, // Value: "065Y"
            
            (byte)0x00, (byte)0x40, (byte)0x40, (byte)0x10, // Tag: (0040,4010)
            (byte)0x44, (byte)0x54,                         // VR: DT
            (byte)0x00, (byte)0x0C,                         // Length: 12
            (byte)0x32, (byte)0x30, (byte)0x31, (byte)0x39, // Value: "20190517"
            (byte)0x30, (byte)0x35, (byte)0x31, (byte)0x37, // Value: "1307"
            (byte)0x31, (byte)0x33, (byte)0x30, (byte)0x37
        };
        
        ExplicitVRBigEndian ts = new ExplicitVRBigEndian();
        int[] idx = {0};
        
        System.out.println("传输语法: " + ts.name + " (大端=" + ts.isBE + ", 显式=" + ts.isExplicit + ")");
        
        // 解析第一个数据元素
        DCMAbstractType element1 = ts.Decode(data, idx);
        if (element1 instanceof DCMDataElement) {
            DCMDataElement de1 = (DCMDataElement) element1;
            System.out.println("元素1: " + de1.ToString("  "));
        }
        
        // 解析第二个数据元素
        DCMAbstractType element2 = ts.Decode(data, idx);
        if (element2 instanceof DCMDataElement) {
            DCMDataElement de2 = (DCMDataElement) element2;
            System.out.println("元素2: " + de2.ToString("  "));
        }
        
        System.out.println();
    }
    
    /**
     * 测试隐式VR小端数据（默认传输语法）
     * 数据：08 00 21 00 08 00 00 00 31 39 39 35 30 36 30 38 10 00 20 00 06 00 00 00 47 45 30 35 31 34 28 00 00 01 02 00 00 00 10 00
     */
    private static void testImplicitVRLittleEndian() {
        System.out.println("2. 隐式VR Little Endian测试:");
        
        byte[] data = {
            (byte)0x08, (byte)0x00, (byte)0x21, (byte)0x00, // Tag: (0008,0021)
            (byte)0x08, (byte)0x00, (byte)0x00, (byte)0x00, // Length: 8
            (byte)0x31, (byte)0x39, (byte)0x39, (byte)0x35, // Value: "19950608"
            (byte)0x30, (byte)0x36, (byte)0x30, (byte)0x38,
            
            (byte)0x10, (byte)0x00, (byte)0x20, (byte)0x00, // Tag: (0010,0020)
            (byte)0x06, (byte)0x00, (byte)0x00, (byte)0x00, // Length: 6
            (byte)0x47, (byte)0x45, (byte)0x30, (byte)0x35, // Value: "GE0514"
            (byte)0x31, (byte)0x34,
            
            (byte)0x28, (byte)0x00, (byte)0x00, (byte)0x01, // Tag: (0028,0100)
            (byte)0x02, (byte)0x00, (byte)0x00, (byte)0x00, // Length: 2
            (byte)0x10, (byte)0x00                          // Value: 16
        };
        
        ImplicitVRLittleEndian ts = new ImplicitVRLittleEndian();
        int[] idx = {0};
        
        System.out.println("传输语法: " + ts.name + " (大端=" + ts.isBE + ", 显式=" + ts.isExplicit + ")");
        
        // 解析三个数据元素
        for (int i = 1; i <= 3; i++) {
            DCMAbstractType element = ts.Decode(data, idx);
            if (element instanceof DCMDataElement) {
                DCMDataElement de = (DCMDataElement) element;
                System.out.println("元素" + i + ": " + de.ToString("  "));
            }
        }
        
        System.out.println();
    }
    
    /**
     * 测试显式VR小端数据
     * 数据：28 00 00 00 55 4c 04 00 3c 00 00 00 28 00 02 00 55 53 02 00 01 00 28 00 10 00 55 53 02 00 02 00 28 00 11 00 55 53 02 00 04 00 28 00 00 01 55 53 02 00 10 00 28 00 01 01 55 53 02 00 0c 00 28 00 02 01 55 53 02 00 0b 00 e0 7f 00 00 55 4c 04 00 1c 00 00 00 e0 7f 10 00 4f 57 00 00 10 00 00 00 64 01 c3 00 a5 00 68 07 c4 04 58 08 21 02 b4 0e
     */
    private static void testExplicitVRLittleEndian() {
        System.out.println("3. 显式VR Little Endian测试:");
        
        byte[] data = {
            (byte)0x28, (byte)0x00, (byte)0x00, (byte)0x00, // Tag: (0028,0000)
            (byte)0x55, (byte)0x4c,                         // VR: UL
            (byte)0x04, (byte)0x00,                         // Length: 4
            (byte)0x3c, (byte)0x00, (byte)0x00, (byte)0x00, // Value: 60
            
            (byte)0x28, (byte)0x00, (byte)0x02, (byte)0x00, // Tag: (0028,0002)
            (byte)0x55, (byte)0x53,                         // VR: US
            (byte)0x02, (byte)0x00,                         // Length: 2
            (byte)0x01, (byte)0x00,                         // Value: 1
            
            (byte)0x28, (byte)0x00, (byte)0x10, (byte)0x00, // Tag: (0028,0010)
            (byte)0x55, (byte)0x53,                         // VR: US
            (byte)0x02, (byte)0x00,                         // Length: 2
            (byte)0x02, (byte)0x00,                         // Value: 2
            
            (byte)0x28, (byte)0x00, (byte)0x11, (byte)0x00, // Tag: (0028,0011)
            (byte)0x55, (byte)0x53,                         // VR: US
            (byte)0x02, (byte)0x00,                         // Length: 2
            (byte)0x04, (byte)0x00,                         // Value: 4
            
            (byte)0x28, (byte)0x00, (byte)0x00, (byte)0x01, // Tag: (0028,0100)
            (byte)0x55, (byte)0x53,                         // VR: US
            (byte)0x02, (byte)0x00,                         // Length: 2
            (byte)0x10, (byte)0x00,                         // Value: 16
            
            (byte)0x28, (byte)0x00, (byte)0x01, (byte)0x01, // Tag: (0028,0101)
            (byte)0x55, (byte)0x53,                         // VR: US
            (byte)0x02, (byte)0x00,                         // Length: 2
            (byte)0x0c, (byte)0x00,                         // Value: 12
            
            (byte)0x28, (byte)0x00, (byte)0x02, (byte)0x01, // Tag: (0028,0102)
            (byte)0x55, (byte)0x53,                         // VR: US
            (byte)0x02, (byte)0x00,                         // Length: 2
            (byte)0x0b, (byte)0x00,                         // Value: 11
            
            (byte)0xe0, (byte)0x7f, (byte)0x00, (byte)0x00, // Tag: (7fe0,0000)
            (byte)0x55, (byte)0x4c,                         // VR: UL
            (byte)0x04, (byte)0x00,                         // Length: 4
            (byte)0x1c, (byte)0x00, (byte)0x00, (byte)0x00, // Value: 28
            
            (byte)0xe0, (byte)0x7f, (byte)0x10, (byte)0x00, // Tag: (7fe0,0010)
            (byte)0x4f, (byte)0x57,                         // VR: OW
            (byte)0x00, (byte)0x00,                         // Reserved: 0000
            (byte)0x10, (byte)0x00, (byte)0x00, (byte)0x00, // Length: 16
            // Pixel data: 16 bytes
            (byte)0x64, (byte)0x01, (byte)0xc3, (byte)0x00, 
            (byte)0xa5, (byte)0x00, (byte)0x68, (byte)0x07, 
            (byte)0xc4, (byte)0x04, (byte)0x58, (byte)0x08, 
            (byte)0x21, (byte)0x02, (byte)0xb4, (byte)0x0e
        };
        
        ExplicitVRLittleEndian ts = new ExplicitVRLittleEndian();
        int[] idx = {0};
        
        System.out.println("传输语法: " + ts.name + " (大端=" + ts.isBE + ", 显式=" + ts.isExplicit + ")");
        
        // 解析多个数据元素
        for (int i = 1; i <= 9; i++) {
            DCMAbstractType element = ts.Decode(data, idx);
            if (element instanceof DCMDataElement) {
                DCMDataElement de = (DCMDataElement) element;
                System.out.println("元素" + i + ": " + de.ToString("  "));
            }
            
            // 如果到达数据末尾，停止解析
            if (idx[0] >= data.length) {
                break;
            }
        }
        
        System.out.println();
    }
}

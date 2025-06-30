package com.dicom.transfersyntax;

import java.nio.ByteOrder;

/**
 * DICOM字节缓冲区类
 * 用于处理DICOM数据的字节读取，支持大端和小端字节序
 */
public class DicomByteBuffer {
    private byte[] data;
    private int position;
    private ByteOrder byteOrder;
    
    /**
     * 构造函数
     * @param data 字节数据
     * @param startPosition 起始位置
     * @param byteOrder 字节序（大端或小端）
     */
    public DicomByteBuffer(byte[] data, int startPosition, ByteOrder byteOrder) {
        this.data = data;
        this.position = startPosition;
        this.byteOrder = byteOrder;
    }
    
    /**
     * 读取一个字节
     * @return 字节值
     */
    public byte readByte() {
        if (position >= data.length) {
            throw new IndexOutOfBoundsException("缓冲区位置超出范围");
        }
        return data[position++];
    }
    
    /**
     * 读取短整数（2字节）
     * @return 短整数值
     */
    public short readShort() {
        if (position + 1 >= data.length) {
            throw new IndexOutOfBoundsException("缓冲区位置超出范围");
        }
        
        short value;
        if (byteOrder == ByteOrder.LITTLE_ENDIAN) {
            value = (short) ((data[position] & 0xFF) | ((data[position + 1] & 0xFF) << 8));
        } else {
            value = (short) (((data[position] & 0xFF) << 8) | (data[position + 1] & 0xFF));
        }
        position += 2;
        return value;
    }
    
    /**
     * 读取整数（4字节）
     * @return 整数值
     */
    public int readInt() {
        if (position + 3 >= data.length) {
            throw new IndexOutOfBoundsException("缓冲区位置超出范围");
        }
        
        int value;
        if (byteOrder == ByteOrder.LITTLE_ENDIAN) {
            value = (data[position] & 0xFF) |
                    ((data[position + 1] & 0xFF) << 8) |
                    ((data[position + 2] & 0xFF) << 16) |
                    ((data[position + 3] & 0xFF) << 24);
        } else {
            value = ((data[position] & 0xFF) << 24) |
                    ((data[position + 1] & 0xFF) << 16) |
                    ((data[position + 2] & 0xFF) << 8) |
                    (data[position + 3] & 0xFF);
        }
        position += 4;
        return value;
    }
    
    /**
     * 读取指定长度的字节数组
     * @param length 要读取的长度
     * @return 字节数组
     */
    public byte[] readBytes(int length) {
        if (position + length > data.length) {
            throw new IndexOutOfBoundsException("缓冲区位置超出范围");
        }
        
        byte[] result = new byte[length];
        System.arraycopy(data, position, result, 0, length);
        position += length;
        return result;
    }
    
    /**
     * 读取字符串（按指定长度）
     * @param length 字符串长度
     * @return 字符串
     */
    public String readString(int length) {
        byte[] bytes = readBytes(length);
        return new String(bytes).trim();
    }
    
    /**
     * 获取当前位置
     * @return 当前位置
     */
    public int getPosition() {
        return position;
    }
    
    /**
     * 设置当前位置
     * @param position 新位置
     */
    public void setPosition(int position) {
        this.position = position;
    }
    
    /**
     * 检查是否还有剩余数据
     * @return 是否有剩余数据
     */
    public boolean hasRemaining() {
        return position < data.length;
    }
    
    /**
     * 获取剩余字节数
     * @return 剩余字节数
     */
    public int remaining() {
        return data.length - position;
    }
    
    /**
     * 获取底层数据数组
     * @return 数据数组
     */
    public byte[] getData() {
        return data;
    }
}

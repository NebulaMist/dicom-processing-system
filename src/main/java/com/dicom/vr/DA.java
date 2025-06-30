package com.dicom.vr;

/**
 * DA (Date) VR类
 * 日期，格式为YYYYMMDD
 * 长度：固定8字节
 */
public class DA extends VRBase {
    
    private static DA instanceLE = null;
    private static DA instanceBE = null;
    
    private DA(boolean isBE) {
        super(isBE, false);
    }
    
    public static DA getInstance(boolean isBE) {
        if (isBE) {
            if (instanceBE == null) {
                instanceBE = new DA(true);
            }
            return instanceBE;
        } else {
            if (instanceLE == null) {
                instanceLE = new DA(false);
            }
            return instanceLE;
        }
    }
      @Override
    @SuppressWarnings("unchecked")
    public <T> T GetValue(byte[] data, int startIndex) {
        if (data == null || startIndex < 0 || startIndex + 8 > data.length) {
            return null;
        }
        
        String value = new String(data, startIndex, 8).trim();
        return (T) value;
    }
    
    @Override
    public <T> byte[] SetValue(T value) {
        if (value == null) {
            return new byte[0];
        }
        
        String dateStr = value.toString();
        
        // 验证日期格式，应该是YYYYMMDD（8位数字）
        if (dateStr.length() != 8) {
            throw new IllegalArgumentException("DA date value must be exactly 8 characters (YYYYMMDD): " + dateStr);
        }
        
        // 验证是否为有效的数字
        try {
            Integer.parseInt(dateStr);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("DA date value must contain only digits (YYYYMMDD): " + dateStr);
        }
        
        // 简单的日期格式验证
        validateDateFormat(dateStr);
        
        return dateStr.getBytes();
    }
    
    /**
     * 验证日期格式的基本合理性
     */
    private void validateDateFormat(String dateStr) {
        if (dateStr.length() != 8) return;
        
        try {
            int year = Integer.parseInt(dateStr.substring(0, 4));
            int month = Integer.parseInt(dateStr.substring(4, 6));
            int day = Integer.parseInt(dateStr.substring(6, 8));
            
            if (year < 1900 || year > 2100) {
                throw new IllegalArgumentException("Invalid year in DA date: " + year);
            }
            if (month < 1 || month > 12) {
                throw new IllegalArgumentException("Invalid month in DA date: " + month);
            }
            if (day < 1 || day > 31) {
                throw new IllegalArgumentException("Invalid day in DA date: " + day);
            }
            
            // 基本月份天数验证
            if (month == 2 && day > 29) {
                throw new IllegalArgumentException("Invalid day for February: " + day);
            }
            if ((month == 4 || month == 6 || month == 9 || month == 11) && day > 30) {
                throw new IllegalArgumentException("Invalid day for month " + month + ": " + day);
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid DA date format: " + dateStr);
        }
    }
}

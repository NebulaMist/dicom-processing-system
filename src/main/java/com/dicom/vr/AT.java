package com.dicom.vr;

/**
 * AT (Attribute Tag) VR类
 * 属性标签，包含组号和元素号
 * 长度：固定4字节
 */
public class AT extends VRBase {
    
    private static AT instanceLE = null;
    private static AT instanceBE = null;
    
    private AT(boolean isBE) {
        super(isBE, false);
    }
    
    public static AT getInstance(boolean isBE) {
        if (isBE) {
            if (instanceBE == null) {
                instanceBE = new AT(true);
            }
            return instanceBE;
        } else {
            if (instanceLE == null) {
                instanceLE = new AT(false);
            }
            return instanceLE;
        }
    }
      @Override
    @SuppressWarnings("unchecked")
    public <T> T GetValue(byte[] data, int startIndex) {
        if (data == null || startIndex < 0 || startIndex + 4 > data.length) {
            return null;
        }
        
        int group, element;
        if (isBE) {
            group = ((data[startIndex] & 0xFF) << 8) | (data[startIndex + 1] & 0xFF);
            element = ((data[startIndex + 2] & 0xFF) << 8) | (data[startIndex + 3] & 0xFF);
        } else {
            group = ((data[startIndex + 1] & 0xFF) << 8) | (data[startIndex] & 0xFF);
            element = ((data[startIndex + 3] & 0xFF) << 8) | (data[startIndex + 2] & 0xFF);
        }
        
        // Return formatted tag string
        String value = String.format("(%04X,%04X)", group, element);
        return (T) value;
    }
    
    @Override
    public byte[] SetValue(Object value) {
        if (value == null) {
            return new byte[0];
        }
        
        int tag;
        if (value instanceof Integer) {
            tag = (Integer) value;
        } else if (value instanceof String) {
            String str = (String) value;
            // Handle formatted tag strings like "(0010,0010)"
            if (str.matches("\\([0-9A-Fa-f]{4},[0-9A-Fa-f]{4}\\)")) {
                str = str.substring(1, str.length() - 1); // Remove parentheses
                String[] parts = str.split(",");
                int group = Integer.parseInt(parts[0], 16);
                int element = Integer.parseInt(parts[1], 16);
                tag = (group << 16) | element;
            } else {
                // Try to parse as hex string
                try {
                    tag = Integer.parseInt(str.replaceAll("[^0-9A-Fa-f]", ""), 16);
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("Invalid AT value: " + value);
                }
            }
        } else {
            throw new IllegalArgumentException("AT value must be Integer or String, got: " + value.getClass().getSimpleName());
        }
        
        byte[] result = new byte[4];
        int group = (tag >> 16) & 0xFFFF;
        int element = tag & 0xFFFF;
        
        if (isBE) {
            // Big-endian: most significant byte first
            result[0] = (byte) ((group >> 8) & 0xFF);
            result[1] = (byte) (group & 0xFF);
            result[2] = (byte) ((element >> 8) & 0xFF);
            result[3] = (byte) (element & 0xFF);
        } else {
            // Little-endian: least significant byte first
            result[0] = (byte) (group & 0xFF);
            result[1] = (byte) ((group >> 8) & 0xFF);
            result[2] = (byte) (element & 0xFF);
            result[3] = (byte) ((element >> 8) & 0xFF);
        }
        
        return result;
    }
}

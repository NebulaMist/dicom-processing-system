package com.dicom.vr;

import java.util.HashMap;
import java.util.Map;

/**
 * VR工厂类
 * 实现享元模式，管理所有VR实例
 * 避免重复创建相同的VR对象，优化内存使用
 */
public class VRFactory {
    
    /**
     * 是否为BigEndian解码
     */
    protected boolean isBE;
    
    /**
     * VR实例共享池
     * Key: VR类型名称
     * Value: VR实例
     */
    private HashMap<String, VRBase> VRs = new HashMap<String, VRBase>();
    
    /**
     * 静态VR实例缓存池（保持向后兼容）
     * Key: VR类型名称 + 字节序标识
     * Value: VR实例
     */
    private static final Map<String, VRBase> vrPool = new HashMap<String, VRBase>();    
    /**
     * 构造函数
     * @param isBE 是否为BigEndian解码
     */
    public VRFactory(boolean isBE) {
        this.isBE = isBE;
    }
    
    /**
     * 获取VR实例（享元模式实例方法）
     * @param key VR类型（如"UL", "US", "AE"等）
     * @return 对应的VR实例
     */
    public VRBase GetVR(String key) {
        if (key == null || key.trim().isEmpty()) {
            return null;
        }
        
        String vrType = key.toUpperCase();
        VRBase vrInstance = VRs.get(vrType);
        
        if (vrInstance == null) {
            vrInstance = createVRInstance(vrType, this.isBE);
            if (vrInstance != null) {
                VRs.put(vrType, vrInstance);
            }
        }
        
        return vrInstance;
    }
    
    /**
     * 获取VR实例（享元模式静态方法，保持向后兼容）
     * @param vrType VR类型（如"UL", "US", "AE"等）
     * @param isBE 是否为BigEndian字节序
     * @return 对应的VR实例
     */
    public static VRBase getVRInstance(String vrType, boolean isBE) {
        if (vrType == null || vrType.trim().isEmpty()) {
            return null;
        }
        
        String key = vrType.toUpperCase() + "_" + (isBE ? "BE" : "LE");
        VRBase vrInstance = vrPool.get(key);
        
        if (vrInstance == null) {
            vrInstance = createVRInstance(vrType.toUpperCase(), isBE);
            if (vrInstance != null) {
                vrPool.put(key, vrInstance);
            }
        }
        
        return vrInstance;
    }
      /**
     * 创建VR实例
     * @param vrType VR类型
     * @param isBE 是否为BigEndian字节序
     * @return VR实例
     */
    private static VRBase createVRInstance(String vrType, boolean isBE) {
        if ("AE".equals(vrType)) return AE.getInstance(isBE);
        if ("AS".equals(vrType)) return AS.getInstance(isBE);
        if ("AT".equals(vrType)) return AT.getInstance(isBE);
        if ("CS".equals(vrType)) return CS.getInstance(isBE);
        if ("DA".equals(vrType)) return DA.getInstance(isBE);
        if ("DS".equals(vrType)) return DS.getInstance(isBE);
        if ("DT".equals(vrType)) return DT.getInstance(isBE);
        if ("FD".equals(vrType)) return FD.getInstance(isBE);
        if ("FL".equals(vrType)) return FL.getInstance(isBE);
        if ("IS".equals(vrType)) return IS.getInstance(isBE);
        if ("LO".equals(vrType)) return LO.getInstance(isBE);
        if ("LT".equals(vrType)) return LT.getInstance(isBE);
        if ("OB".equals(vrType)) return OB.getInstance(isBE);
        if ("OD".equals(vrType)) return OD.getInstance(isBE);
        if ("OF".equals(vrType)) return OF.getInstance(isBE);
        if ("OL".equals(vrType)) return OL.getInstance(isBE);
        if ("OW".equals(vrType)) return OW.getInstance(isBE);
        if ("PN".equals(vrType)) return PN.getInstance(isBE);
        if ("SH".equals(vrType)) return SH.getInstance(isBE);
        if ("SL".equals(vrType)) return SL.getInstance(isBE);
        if ("SQ".equals(vrType)) return SQ.getInstance(isBE);
        if ("SS".equals(vrType)) return SS.getInstance(isBE);
        if ("ST".equals(vrType)) return ST.getInstance(isBE);
        if ("TM".equals(vrType)) return TM.getInstance(isBE);
        if ("UC".equals(vrType)) return UC.getInstance(isBE);
        if ("UI".equals(vrType)) return UI.getInstance(isBE);
        if ("UL".equals(vrType)) return UL.getInstance(isBE);
        if ("UN".equals(vrType)) return UN.getInstance(isBE);
        if ("UR".equals(vrType)) return UR.getInstance(isBE);
        if ("US".equals(vrType)) return US.getInstance(isBE);
        if ("UT".equals(vrType)) return UT.getInstance(isBE);
        
        // 未知VR类型，返回UN实例
        return UN.getInstance(isBE);
    }    
    /**
     * 获取实例共享池大小
     * @return 实例共享池中的VR实例数量
     */
    public int getVRPoolSize() {
        return VRs.size();
    }
    
    /**
     * 清空实例共享池
     */
    public void clearVRPool() {
        VRs.clear();
    }
    
    /**
     * 获取缓存池大小（用于测试和监控，静态方法保持向后兼容）
     * @return 缓存池中的实例数量
     */
    public static int getPoolSize() {
        return vrPool.size();
    }
    
    /**
     * 清空缓存池（主要用于测试）
     */
    public static void clearPool() {
        vrPool.clear();
    }
    
    /**
     * 检查VR类型是否为长VR类型
     * @param vrType VR类型
     * @return 是否为长VR类型
     */
    public static boolean isLongVR(String vrType) {
        if (vrType == null) return false;
        
        String vr = vrType.toUpperCase();
        return "OB".equals(vr) || "OF".equals(vr) || "OD".equals(vr) ||
               "OW".equals(vr) || "OL".equals(vr) || "SQ".equals(vr) ||
               "UT".equals(vr) || "UN".equals(vr) || "UR".equals(vr) ||
               "UC".equals(vr);
    }
}

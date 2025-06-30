package com.dicom.data;

/**
 * VR接收器接口
 * 用于处理复合VR的回调
 */
public interface VRReceiver {
    /**
     * 根据标签返回合适的VR
     * @param gtag 组标签
     * @param etag 元素标签
     * @return VR字符串
     */
    String action(short gtag, short etag);
}

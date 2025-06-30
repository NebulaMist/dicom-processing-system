package com.dicom.dictionary;

/**
 * DICOM数据字典条目类
 * 保存一条数据元素的定义
 */
public class DicomDictionaryEntry {
    private String gtag;    // 组号
    private String etag;    // 元素号
    private String name;    // 名称
    private String keyword; // 关键字
    private String vr;      // VR (Value Representation)
    private String vm;      // VM (Value Multiplicity)
    private String retired; // 是否已退役
    
    /**
     * 构造函数
     */
    public DicomDictionaryEntry() {
    }
    
    /**
     * 带参数构造函数
     */
    public DicomDictionaryEntry(String gtag, String etag, String name, 
                               String keyword, String vr, String vm, String retired) {
        this.gtag = gtag;
        this.etag = etag;
        this.name = name;
        this.keyword = keyword;
        this.vr = vr;
        this.vm = vm;
        this.retired = retired;
    }
    
    // Getter 和 Setter 方法
    public String getGtag() {
        return gtag;
    }
    
    public void setGtag(String gtag) {
        this.gtag = gtag;
    }
    
    public String getEtag() {
        return etag;
    }
    
    public void setEtag(String etag) {
        this.etag = etag;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getKeyword() {
        return keyword;
    }
    
    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }
    
    public String getVr() {
        return vr;
    }
    
    public void setVr(String vr) {
        this.vr = vr;
    }
    
    public String getVm() {
        return vm;
    }
    
    public void setVm(String vm) {
        this.vm = vm;
    }
    
    public String getRetired() {
        return retired;
    }
    
    public void setRetired(String retired) {
        this.retired = retired;
    }
    
    /**
     * 获取完整的标签（组号+元素号）
     */
    public String getFullTag() {
        return "(" + gtag + "," + etag + ")";
    }
    
    /**
     * 重写toString方法，输出各成员的值
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("DicomDictionaryEntry{");
        sb.append("tag=").append(getFullTag());
        sb.append(", name='").append(name != null ? name : "").append("'");
        sb.append(", keyword='").append(keyword != null ? keyword : "").append("'");
        sb.append(", vr='").append(vr != null ? vr : "").append("'");
        sb.append(", vm='").append(vm != null ? vm : "").append("'");
        if (retired != null && !retired.trim().isEmpty()) {
            sb.append(", retired='").append(retired).append("'");
        }
        sb.append("}");
        return sb.toString();
    }
}

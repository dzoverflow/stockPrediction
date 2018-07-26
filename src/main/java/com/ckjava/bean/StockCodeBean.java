package com.ckjava.bean;

import com.ckjava.xutils.Constants;
import com.ckjava.xutils.StringUtils;

public class StockCodeBean implements Constants {

    private String code;
    private String area;
    private String name;
    private String desc;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getArea() {
        return area;
    }

    public void setArea(String area) {
        this.area = area;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public StockCodeBean(String code, String area, String name) {
        this.code = code;
        this.area = area;
        this.name = name;
    }

    public StockCodeBean() {
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append(area);
        sb.append(", ").append(name);
        sb.append(", ").append(desc);
        return sb.toString();
    }

    public String getBlacklistString() {
        final StringBuilder sb = new StringBuilder();
        sb.append(code).append(SPLITER.COMMA)
        .append(area).append(SPLITER.COMMA)
        .append(desc).append(StringUtils.LF);
        return sb.toString();
    }
}

package com.ckjava.bean;

public class StockCodeBean {

    private String code;
    private String area;
    private String name;

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

    public StockCodeBean(String code, String area, String name) {
        this.code = code;
        this.area = area;
        this.name = name;
    }

    public StockCodeBean() {
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("StockCodeBean{");
        sb.append("code='").append(code).append('\'');
        sb.append(", area='").append(area).append('\'');
        sb.append(", name='").append(name).append('\'');
        sb.append('}');
        return sb.toString();
    }
}

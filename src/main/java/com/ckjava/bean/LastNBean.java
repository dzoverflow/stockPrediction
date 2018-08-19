package com.ckjava.bean;

import java.math.BigDecimal;

public class LastNBean {

    private StockCodeBean stockCodeBean;
    private BigDecimal up1;
    private BigDecimal up2;
    private BigDecimal up3;
    private BigDecimal down1;
    private BigDecimal down2;
    private BigDecimal down3;
    private String upDesc;
    private String downDesc;

    public StockCodeBean getStockCodeBean() {
        return stockCodeBean;
    }

    public void setStockCodeBean(StockCodeBean stockCodeBean) {
        this.stockCodeBean = stockCodeBean;
    }

    public BigDecimal getUp1() {
        return up1;
    }

    public void setUp1(BigDecimal up1) {
        this.up1 = up1;
    }

    public BigDecimal getUp2() {
        return up2;
    }

    public void setUp2(BigDecimal up2) {
        this.up2 = up2;
    }

    public BigDecimal getUp3() {
        return up3;
    }

    public void setUp3(BigDecimal up3) {
        this.up3 = up3;
    }

    public BigDecimal getDown1() {
        return down1;
    }

    public void setDown1(BigDecimal down1) {
        this.down1 = down1;
    }

    public BigDecimal getDown2() {
        return down2;
    }

    public void setDown2(BigDecimal down2) {
        this.down2 = down2;
    }

    public BigDecimal getDown3() {
        return down3;
    }

    public void setDown3(BigDecimal down3) {
        this.down3 = down3;
    }

    public LastNBean(StockCodeBean stockCodeBean) {
        this.stockCodeBean = stockCodeBean;
    }

    public int compareUpTo(LastNBean lastNBean) {
        return lastNBean.getUp2().compareTo(this.up2);
    }

    public int compareDownTo(LastNBean lastNBean) {
        return lastNBean.getDown2().negate().compareTo(this.down2.negate());
    }

    public LastNBean() {
    }

    public String getUpDesc() {
        return upDesc;
    }

    public void setUpDesc(String upDesc) {
        this.upDesc = upDesc;
    }

    public String getDownDesc() {
        return downDesc;
    }

    public void setDownDesc(String downDesc) {
        this.downDesc = downDesc;
    }

    public String toUpString() {
        final StringBuilder sb = new StringBuilder();
        sb.append(getAreaString(stockCodeBean.getArea()));
        sb.append(",").append(stockCodeBean.getName());
        sb.append(",").append(upDesc);
        return sb.toString();
    }

    public String toDownString() {
        final StringBuilder sb = new StringBuilder();
        sb.append(getAreaString(stockCodeBean.getArea()));
        sb.append(",").append(stockCodeBean.getName());
        sb.append(",").append(downDesc);
        return sb.toString();
    }

    private String getAreaString(String area) {
        if (area.equals("sh")) {
            return "上海";
        } else if (area.equals("sz")) {
            return "深圳";
        } else if (area.equals("xg")) {
            return "香港";
        } else {
            return area;
        }
    }
}

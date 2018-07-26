package com.ckjava.bean;

import java.math.BigDecimal;

public class LastNUpBean {

    private StockCodeBean stockCodeBean;
    private BigDecimal last2;
    private BigDecimal last3;
    private String desc;

    public StockCodeBean getStockCodeBean() {
        return stockCodeBean;
    }

    public void setStockCodeBean(StockCodeBean stockCodeBean) {
        this.stockCodeBean = stockCodeBean;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public BigDecimal getLast2() {
        return last2;
    }

    public void setLast2(BigDecimal last2) {
        this.last2 = last2;
    }

    public BigDecimal getLast3() {
        return last3;
    }

    public void setLast3(BigDecimal last3) {
        this.last3 = last3;
    }

    public LastNUpBean(StockCodeBean stockCodeBean, String desc) {
        this.stockCodeBean = stockCodeBean;
        this.desc = desc;
    }

    public int compareTo(LastNUpBean lastNUpBean) {
        return lastNUpBean.getLast2().compareTo(this.last2);
    }

    public LastNUpBean() {
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append(stockCodeBean.getArea());
        sb.append(",").append(stockCodeBean.getName());
        sb.append(",").append(desc);
        return sb.toString();
    }
}

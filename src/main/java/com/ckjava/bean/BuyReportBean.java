package com.ckjava.bean;

import com.ckjava.xutils.CollectionUtils;
import com.ckjava.xutils.Constants;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class BuyReportBean {

    private StockCodeBean stockCodeBean;
    private List<String> plusList = new ArrayList<>();
    private BigDecimal totalChange = BigDecimal.ZERO;
    private BigDecimal firstChange = BigDecimal.ZERO;

    public StockCodeBean getStockCodeBean() {
        return stockCodeBean;
    }

    public void setStockCodeBean(StockCodeBean stockCodeBean) {
        this.stockCodeBean = stockCodeBean;
    }

    public List<String> getPlusList() {
        return plusList;
    }

    public void setPlusList(List<String> plusList) {
        this.plusList = plusList;
    }

    public BigDecimal getTotalChange() {
        return totalChange;
    }

    public void setTotalChange(BigDecimal totalChange) {
        this.totalChange = totalChange;
    }

    public BigDecimal getFirstChange() {
        return firstChange;
    }

    public void setFirstChange(BigDecimal firstChange) {
        this.firstChange = firstChange;
    }

    public BuyReportBean(StockCodeBean stockCodeBean) {
        this.stockCodeBean = stockCodeBean;
    }

    public int compareTo(BuyReportBean buyReportBean) {
        return buyReportBean.getFirstChange().compareTo(firstChange);
    }

    public BuyReportBean() {
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append(stockCodeBean.getCode()).append(Constants.SPLITER.COLON);
        sb.append(stockCodeBean.getName()).append(Constants.SPLITER.COLON);
        for (int i = 0, c = CollectionUtils.getSize(plusList); i < c; i++) {
            sb.append(plusList.get(i));
        }
        sb.append(totalChange).append(Constants.SPLITER.COLON);
        sb.append(firstChange);
        return sb.toString();
    }
}

package com.ckjava.service;

import com.ckjava.bean.StockCodeBean;
import com.ckjava.xutils.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@PropertySource(value = { "classpath:app.properties" })
public class FileDownloadService implements Constants {

    private static final Logger logger = LoggerFactory.getLogger(FileDownloadService.class);

    @Value("${dataPath}")
    private String dataPath;
    @Value("${downloadLimit}")
    private Integer downloadLimit;

    /**
     * 从文件中读取股票代码
     *
     * @param area
     * @return
     */
    public List<StockCodeBean> getStockCodeList(String area) {
        List<StockCodeBean> dataList = new ArrayList<>();
        String targetString = null;
        try {
            targetString = FileUtils.readFileContent(new File(dataPath + "stockCode-"+area+".txt"), CHARSET.UTF8);
        } catch (Exception e) {
            logger.error(this.getClass().getName().concat(".getStockCodeList has error"), e);
            return dataList;
        }
        List<String> codeList = extractVariable(targetString);
        logger.info("getStockCodeList size ={}", codeList.size());
        for (int i = 0, c = (downloadLimit > 0 ? downloadLimit : codeList.size()); i < c; i++) {
            dataList.add(new StockCodeBean(codeList.get(i), area, null));
        }
        return dataList;
    }

    /**
     * 下载股票数据文件
     *
     * @param stockCodeBean
     * @return
     */
    public boolean downloadStockDataFile(StockCodeBean stockCodeBean) {
        String downloadCode = stockCodeBean.getArea().equals("sh") ? "0".concat(stockCodeBean.getCode()) : "1".concat(stockCodeBean.getCode());

        Map<String, String> placeholderMap = new HashMap<>();
        placeholderMap.put("code", stockCodeBean.getCode());
        placeholderMap.put("downloadCode", downloadCode);

        String descUrl = "http://quotes.money.163.com/trade/lsjysj_${code}.html";
        String url = "http://quotes.money.163.com/service/chddata.html?code=${downloadCode}&fields=TCLOSE;HIGH;LOW;TOPEN;LCLOSE;CHG;PCHG;TURNOVER;VOTURNOVER;VATURNOVER;TCAP;MCAP";
        url = StringUtils.replaceVariable(url, placeholderMap);
        descUrl = StringUtils.replaceVariable(descUrl, placeholderMap);
        logger.info("desc url = {}", descUrl);

        String datas = HttpClientUtils.get(url, getHeaders(), null);

        String dateString = DateUtils.formatTime(new Date().getTime(), "yyyyMMdd");
        String dataFilePath = dataPath + stockCodeBean.getCode() + "/money163/" + stockCodeBean.getCode() + "_data_"+ dateString +".csv";

        FileUtils.writeStringToFile(new File(dataFilePath), datas, false, CHARSET.UTF8);

        return true;
    }

    private Map<String, String> getHeaders() {
        Map<String, String> headers = new HashMap<>();
        headers.put("User-Agent", "Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/45.0.2454.85 Safari/537.36");
        headers.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
        headers.put("Accept-Encoding", "gzip, deflate, sdch");
        headers.put("Accept-Language", "zh-CN,zh;q=0.8,en;q=0.6,zh-TW;q=0.4");
        headers.put("Cache-Control", "no-cache");
        return headers;
    }

    private List<String> extractVariable(String targetString) {
        List<String> variableList = new ArrayList<>();
        if (StringUtils.isNotBlank(targetString) && targetString.contains("(") && targetString.contains(")")) {
            Pattern pattern = Pattern.compile("(\\([^\\).]*\\))");
            Matcher matcher = pattern.matcher(targetString);
            while (matcher.find()) {
                String matcherStr = matcher.group();
                String variable = matcherStr.replaceAll("\\(", "").replaceAll("\\)", "");
                variableList.add(variable);
            }
        }
        return variableList;
    }

}

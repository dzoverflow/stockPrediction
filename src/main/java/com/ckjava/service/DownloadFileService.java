package com.ckjava.service;

import com.ckjava.bean.StockCodeBean;
import com.ckjava.xutils.*;
import com.ckjava.xutils.http.HttpResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@PropertySource(value = { "classpath:app.properties" })
public class DownloadFileService extends FileUtils implements Constants {

    private static final Logger logger = LoggerFactory.getLogger(DownloadFileService.class);

    @Value("${downloadLimit}")
    private Integer downloadLimit;
    @Value("${isDownloadRawFile}")
    private Boolean isDownloadRawFile;
    @Value("${downloadDateString}")
    private String downloadDateString;
    @Value("${areaCode}")
    private String areaCode;
    @Value("${downloadDateRange}")
    private String downloadDateRange;
    @Autowired
    private FileService fileService;

    public Boolean getIsDownloadRawFile() {
        return isDownloadRawFile;
    }

    public String getDownloadDateString() {
        return downloadDateString;
    }

    /**
     * 从文件中读取股票代码
     *
     * @return
     */
    public List<StockCodeBean> getStockCodeList() {
        String[] areas = areaCode.split(SPLITER.COMMA);
        List<StockCodeBean> dataList = new ArrayList<>();
        for (String area: areas) {
            String targetString = null;
            try {
                targetString = FileUtils.readFileContent(fileService.getStockCodeFile(area), CHARSET.UTF8);
            } catch (Exception e) {
                logger.error(this.getClass().getName().concat(".getStockCodeList has error"), e);
                return dataList;
            }
            List<StockCodeBean> codeList = extractVariable(targetString);
            logger.info("getStockCodeList size = {}", codeList.size());
            for (int i = 0, c = (downloadLimit > 0 ? downloadLimit : codeList.size()); i < c; i++) {
                StockCodeBean stockCodeBean = codeList.get(i);
                dataList.add(new StockCodeBean(stockCodeBean.getCode(), area, stockCodeBean.getName()));
            }
        }
        return dataList;
    }

    /**
     * 下载股票数据文件
     *
     * @param stockCodeBean
     * @return
     */
    public boolean downloadStockDataFile(String dateString, StockCodeBean stockCodeBean) {
        Map<String, String> placeholderMap = new HashMap<>();
        placeholderMap.put("code", stockCodeBean.getCode());
        String descUrl = "http://quotes.money.163.com/trade/lsjysj_${code}.html";
        descUrl = StringUtils.replaceVariable(descUrl, placeholderMap);
        logger.info("desc url = {}", descUrl);

        String url = "http://quotes.money.163.com/service/chddata.html";

        Map<String, String> params = new HashMap<>();
        if (StringUtils.isNotBlank(downloadDateRange)) {
            int range = Integer.parseInt(downloadDateRange);
            Date now = new Date();
            Date start = DateUtils.getAssignDay(now, -range);

            params.put("code", stockCodeBean.getArea().equals("sh") ? "0".concat(stockCodeBean.getCode()) : "1".concat(stockCodeBean.getCode()));
            params.put("start", DateUtils.formatTime(start.getTime(),"yyyyMMdd"));
            params.put("end", DateUtils.formatTime(now.getTime(), "yyyyMMdd"));
            params.put("fields", "TCLOSE;HIGH;LOW;TOPEN;LCLOSE;CHG;PCHG;TURNOVER;VOTURNOVER;VATURNOVER;TCAP;MCAP");
        }

        try {
            HttpResult result = HttpClientUtils.get(url, getHeaders(), params);

            File rawDataFile = fileService.getRawDataFile(dateString, stockCodeBean.getArea(), stockCodeBean.getCode());
            writeStringToFile(rawDataFile, result.getBodyString(), Boolean.FALSE, CHARSET.UTF8);
            return true;
        } catch (Exception e) {
            logger.error("downloadStockDataFile has error, stockCodeBean = " + stockCodeBean.toString(), e);
            return false;
        }
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

    private List<StockCodeBean> extractVariable(String targetString) {
        List<String> codeList = new ArrayList<>();
        // 提取 code
        if (StringUtils.isNotBlank(targetString) && targetString.contains("(") && targetString.contains(")")) {
            Pattern pattern = Pattern.compile("(\\([^\\).]*\\))");
            Matcher matcher = pattern.matcher(targetString);
            while (matcher.find()) {
                String matcherStr = matcher.group();
                String variable = matcherStr.replaceAll("\\(", "").replaceAll("\\)", "");
                codeList.add(variable);
            }
        }
        List<String> codeNameList = new ArrayList<>();
        // 提取 code 名称
        if (StringUtils.isNotBlank(targetString) && targetString.contains(">") && targetString.contains("(")) {
            Pattern pattern = Pattern.compile("(>[^>.]*</a>)");
            Matcher matcher = pattern.matcher(targetString);
            while (matcher.find()) {
                String matcherStr = matcher.group();
                String variable = matcherStr.replaceAll("</a>", "").replaceAll(">", "");
                codeNameList.add(variable);
            }
        }
        List<StockCodeBean> codeBeans = new ArrayList<>();
        // 生成 codeList
        for (int i = 0, c = CollectionUtils.getSize(codeNameList); i < c; i++) {
            String codeNameString = codeNameList.get(i);
            for (int j = 0, d = CollectionUtils.getSize(codeList); j < d; j++) {
                String codeString = codeList.get(j);
                if (codeNameString.contains(codeString)) {
                    codeBeans.add(new StockCodeBean(codeString, null, codeNameString));
                    break;
                }
            }
        }
        return codeBeans;
    }

}

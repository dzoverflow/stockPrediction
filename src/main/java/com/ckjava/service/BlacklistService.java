package com.ckjava.service;

import com.ckjava.bean.StockCodeBean;
import com.ckjava.xutils.ArrayUtils;
import com.ckjava.xutils.CollectionUtils;
import com.ckjava.xutils.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
@PropertySource(value = { "classpath:app.properties" })
public class BlacklistService extends FileUtils {

    private static final Logger logger = LoggerFactory.getLogger(DownloadFileService.class);

    @Autowired
    private FileService fileService;

    private List<StockCodeBean> blacklist;

    /**
     * 初始化黑名单列表
     *
     * @return
     */
    private synchronized void initBlacklist() {
        blacklist = new ArrayList<>();
        File blacklistFile = fileService.getBlacklistFile();
        if (blacklistFile.exists()) {
            try {
                List<String> originalList = readLines(blacklistFile, CHARSET.UTF8);
                for (int i = 0, c = CollectionUtils.getSize(originalList); i < c; i++) {
                    String dataString = originalList.get(i);
                    String[] datas = dataString.split(SPLITER.COMMA);
                    blacklist.add(new StockCodeBean(ArrayUtils.getValue(datas, 0), ArrayUtils.getValue(datas, 1), null));
                }
                logger.info("BlacklistService.initBlacklist success");
            } catch (IOException e) {
                logger.error("BlacklistService.initBlacklist has error", e);
            }
        } else {
            createFile(blacklistFile.getAbsolutePath());
        }
    }

    /**
     * 加入黑名单
     *
     * @param stockCodeBean
     */
    public synchronized void putBlacklist(StockCodeBean stockCodeBean) {
        if (!isInBlacklist(stockCodeBean)) {
            writeStringToFile(fileService.getBlacklistFile(), stockCodeBean.getBlacklistString(), Boolean.TRUE, CHARSET.UTF8);
        }
    }

    /**
     * 判断是否在黑名单中
     *
     * @param stockCodeBean
     * @return
     */
    public synchronized boolean isInBlacklist(StockCodeBean stockCodeBean) {
        if (blacklist == null) {
            initBlacklist();
        }
        for (int i = 0, c = CollectionUtils.getSize(blacklist); i < c; i++) {
            StockCodeBean codeBean = blacklist.get(i);
            if (codeBean.getCode().equals(stockCodeBean.getCode())
                    && codeBean.getArea().equals(stockCodeBean.getArea())) {
                return true;
            }
        }
        return false;
    }

}

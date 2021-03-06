/***
 * StockFileReader is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * StockFileReader is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Neuroph. If not, see <http://www.gnu.org/licenses/>.
 */

package com.ckjava.service;

import com.ckjava.bean.StockCodeBean;
import com.ckjava.samples.stockmarket.StockInfo;
import com.ckjava.xutils.Constants;
import com.ckjava.xutils.DateUtils;
import com.ckjava.xutils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 *
 * @author Dr.V.Steinhauer
 */
@Service
public class StockFileReaderService implements Constants {

    private static final Logger logger = LoggerFactory.getLogger(StockFileReaderService.class);

    @Autowired
    private BlacklistService blacklistService;
    @Autowired
    private FileService fileService;

    private int maxCounter;
    private String[] valuesRow;

    public String[] getValuesRow() {
        return valuesRow;
    }

    public void setValuesRow(String[] valuesRow) {
        this.valuesRow = valuesRow;
    }

    public int getMaxCounter() {
        return maxCounter;
    }

    public void setMaxCounter(int maxCounter) {
        this.maxCounter = maxCounter;
    }

    public StockFileReaderService() {
        this.setMaxCounter(100);
    }

    public StockFileReaderService(int maxCounter) {
        this.setMaxCounter(maxCounter);
    }

    public List<StockInfo> readMoney163StockDataFile(String dataDateString, StockCodeBean stockCodeBean) {
        File file = fileService.getRawDataFile(dataDateString, stockCodeBean.getArea(), stockCodeBean.getCode());
    	List<StockInfo> dataList = new ArrayList<>();
        if (!file.exists()) {
            return dataList;
        }
        int counter = 0;
        try {
            FileInputStream fis = new FileInputStream(file);
            BufferedReader dis = new BufferedReader(new InputStreamReader(fis));
            String s;
            while ((s = dis.readLine()) != null) {
                String[] s1 = s.split(SPLITER.COMMA);
                try {
                    // 判断是否是 *ST 的
                    String code = cleanData(s1[1]);
                    String title = cleanData(s1[2]);
                    if (StringUtils.isNotBlank(title) && title.contains("ST")) {
                        stockCodeBean.setDesc(title);
                        blacklistService.putBlacklist(stockCodeBean);
                        return Collections.EMPTY_LIST;
                    }

                	StockInfo info = new StockInfo();
                	info.setDate(cleanData(s1[0]));
                	info.setDateValue(DateUtils.parseDate(cleanData(s1[0]), TIMEFORMAT.DATE).getTime());
                	info.setCode(code);
                	info.setTitle(title);
                	info.setFinalPrice(cleanData(s1[3]));
                	info.setHighestPrice(cleanData(s1[4]));
                	info.setLowestPrice(cleanData(s1[5]));
                	info.setStartPrice(cleanData(s1[6]));
                	info.setLastFinishPrice(cleanData(s1[7]));
                	info.setChangePrice(cleanData(s1[8]));
                	info.setChangeRange(cleanData(s1[9]));
                	info.setChangeRate(cleanData(s1[10]));
                	info.setDealAmount(cleanData(s1[11]));
                	info.setDealMoney(cleanData(s1[12]));
                	info.setMarketValue(cleanData(s1[13]));
                	info.setCirculateValue(cleanData(s1[14]));

                	if (StringUtils.isBlank(info.getFinalPrice()) || new BigDecimal(info.getFinalPrice()).compareTo(BigDecimal.ZERO) == 0) {
                	    continue;
                    }
                	
                	dataList.add(info);
                    counter = counter + 1;
				} catch (Exception e) {
				}
            }
            fis.close();
        } catch (IOException e) {
            logger.error("StockFileReader.readMoney163StockDataFile has error", e);
        }
        logger.info("StockFileReader.readMoney163StockDataFile dataSize={}",  dataList.size());
        if (dataList.size() > 2) {
            StockInfo info = dataList.get(1);
            logger.info("StockFileReader.readMoney163StockDataFile code={}, name={}", info.getCode(), info.getTitle());
        }

        // 倒序排列
        Collections.sort(dataList, new Comparator<StockInfo>() {
			@Override
			public int compare(StockInfo o1, StockInfo o2) {
				return BigDecimal.valueOf(o1.getDateValue()).compareTo(BigDecimal.valueOf(o2.getDateValue()));
			}
		});
        
        return dataList;
    }
    
    private String cleanData(String original) {
    	return original.replace('\"', ' ').replace('\'', ' ').trim();
    }
    
}

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

package com.ckjava.samples.stockmarket;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.ckjava.xutils.Constants;
import com.ckjava.xutils.DateUtils;

/**
 *
 * @author Dr.V.Steinhauer
 */
public class StockFileReader implements Constants {

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

    public StockFileReader() {
        this.setMaxCounter(100);
    }

    public StockFileReader(int maxCounter) {
        this.setMaxCounter(maxCounter);
    }

    public List<StockInfo> readMoney163StockDataFile(String fileName) {
    	List<StockInfo> dataList = new ArrayList<>();
        File file = new File(fileName);
        if (!file.exists()) {
            return dataList;
        }
        System.out.println("file = " + fileName+". It will be filtered the values for the moment of the market opened");
        int counter = 0;
        try {
            FileInputStream fis = new FileInputStream(file);
            BufferedReader dis = new BufferedReader(new InputStreamReader(fis));
            String s;
            while ((s = dis.readLine()) != null) {
                String[] s1 = s.split(",");
                try {
                	StockInfo info = new StockInfo();
                	info.setDate(cleanData(s1[0]));
                	info.setDateValue(DateUtils.parseDate(cleanData(s1[0]), TIMEFORMAT.DATE).getTime());
                	info.setCode(cleanData(s1[1]));
                	info.setTitle(cleanData(s1[2]));
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
                	
                	dataList.add(info);
                    counter = counter + 1;
				} catch (Exception e) {
				}
            }
            fis.close();
        } catch (IOException ioe) {
            System.out.println("Oops- an IOException happened.");
            ioe.printStackTrace();
            System.exit(1);
        }
        System.out.println("dataList.size=" + dataList.size());
        
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

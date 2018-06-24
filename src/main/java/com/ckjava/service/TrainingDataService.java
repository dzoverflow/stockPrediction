package com.ckjava.service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import com.ckjava.samples.stockmarket.StockInfo;
import org.neuroph.core.learning.SupervisedTrainingElement;
import org.neuroph.core.learning.TrainingElement;
import org.neuroph.core.learning.TrainingSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 *
 * @author Dr.V.Steinhauer
 */
@Service
public class TrainingDataService {

    private static final Logger logger = LoggerFactory.getLogger(TrainingDataService.class);

    private Date limitDate;
    private double minlevel = 0.00D;

	public double getMinlevel() {
		return minlevel;
	}

	public void setMinlevel(double minlevel) {
		this.minlevel = minlevel;
	}

	public TrainingSet getTrainingSet(List<StockInfo> dataList, double normolizer) {
        TrainingSet trainingSet = new TrainingSet();
        int length = dataList.size();
        if (length < 5) {
            logger.warn("dataList.size < 5");
            return trainingSet;
        }
        try {
            int print = 0;
            for (int i = 0, c = dataList.size(); i+4 < c; i++) {
            	if (limitDate != null && dataList.get(i).getDateValue() < limitDate.getTime()) {
            		continue;
            	}
            	
                String s1 = dataList.get(i).getFinalPrice();
                String s2 = dataList.get(i+1).getFinalPrice();
                String s3 = dataList.get(i+2).getFinalPrice();
                String s4 = dataList.get(i+3).getFinalPrice();
                String s5 = dataList.get(i+4).getFinalPrice();
                
                double d1 = BigDecimal.valueOf(Double.parseDouble(s1) - minlevel).divide(BigDecimal.valueOf(normolizer), 4, BigDecimal.ROUND_HALF_UP).doubleValue();
                double d2 = BigDecimal.valueOf(Double.parseDouble(s2) - minlevel).divide(BigDecimal.valueOf(normolizer), 4, BigDecimal.ROUND_HALF_UP).doubleValue();
                double d3 = BigDecimal.valueOf(Double.parseDouble(s3) - minlevel).divide(BigDecimal.valueOf(normolizer), 4, BigDecimal.ROUND_HALF_UP).doubleValue();
                double d4 = BigDecimal.valueOf(Double.parseDouble(s4) - minlevel).divide(BigDecimal.valueOf(normolizer), 4, BigDecimal.ROUND_HALF_UP).doubleValue();
                double d5 = BigDecimal.valueOf(Double.parseDouble(s5) - minlevel).divide(BigDecimal.valueOf(normolizer), 4, BigDecimal.ROUND_HALF_UP).doubleValue();
                trainingSet.addElement(new SupervisedTrainingElement(new double[]{d1, d2, d3, d4}, new double[]{d5}));
                
                if (print < 10) {
                    logger.info("加载训练数据:" + print + " " + d1 + " " + d2 + " " + d3 + " " + d4 + " ->" + d5);
                }

                print ++;
            }

            logger.info("finish load TrainingSet");

            return trainingSet;
        } catch (Exception e) {
            logger.error("TrainingDataService.getTrainingSet has error", e);
            return trainingSet;
        }
    }
    
    public TrainingSet getTestTrainingSet(List<StockInfo> dataList, double normolizer) {
        TrainingSet testTrainingSet = new TrainingSet();
        int length = dataList.size();
        if (length < 5) {
            logger.warn("dataList.size < 5");
            return testTrainingSet;
        }
        try {
            for (int i = dataList.size(); i >0;) {
                String s1 = dataList.get(i-4).getFinalPrice();
                String s2 = dataList.get(i-3).getFinalPrice();
                String s3 = dataList.get(i-2).getFinalPrice();
                String s4 = dataList.get(i-1).getFinalPrice();
                
                double d1 = BigDecimal.valueOf(Double.parseDouble(s1) - minlevel).divide(BigDecimal.valueOf(normolizer), 4, BigDecimal.ROUND_HALF_UP).doubleValue();
                double d2 = BigDecimal.valueOf(Double.parseDouble(s2) - minlevel).divide(BigDecimal.valueOf(normolizer), 4, BigDecimal.ROUND_HALF_UP).doubleValue();
                double d3 = BigDecimal.valueOf(Double.parseDouble(s3) - minlevel).divide(BigDecimal.valueOf(normolizer), 4, BigDecimal.ROUND_HALF_UP).doubleValue();
                double d4 = BigDecimal.valueOf(Double.parseDouble(s4) - minlevel).divide(BigDecimal.valueOf(normolizer), 4, BigDecimal.ROUND_HALF_UP).doubleValue();
                testTrainingSet.addElement(new TrainingElement(new double[]{d1, d2, d3, d4}));
                
                break;
            }

            logger.info("finish load TestTrainingSet");
            return testTrainingSet;
        } catch (Exception e) {
            logger.error("TrainingDataService.getTrainingSet has error", e);
            return testTrainingSet;
        }
    }

}

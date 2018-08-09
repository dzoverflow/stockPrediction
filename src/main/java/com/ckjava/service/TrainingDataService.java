package com.ckjava.service;

import java.io.File;
import java.math.BigDecimal;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import com.ckjava.bean.StockCodeBean;
import com.ckjava.samples.stockmarket.StockInfo;
import com.ckjava.xutils.CollectionUtils;
import com.ckjava.xutils.Constants;
import com.ckjava.xutils.FileUtils;
import com.ckjava.xutils.StringUtils;
import org.neuroph.core.learning.SupervisedTrainingElement;
import org.neuroph.core.learning.TrainingElement;
import org.neuroph.core.learning.TrainingSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author Dr.V.Steinhauer
 */
@Service
public class TrainingDataService {

    private static final Logger logger = LoggerFactory.getLogger(TrainingDataService.class);

    @Autowired
    private FileService fileService;
    @Autowired
    private StockFileReaderService stockFileReaderService;

    private Date limitDate;
    private double minlevel = 0.00D;

	public double getMinlevel() {
		return minlevel;
	}

	public void setMinlevel(double minlevel) {
		this.minlevel = minlevel;
	}

    /**
     * 从原始数据文件中读取训练数据
     *
     * @param dataDateString
     * @param stockCodeBean
     * @param normolizer
     * @return
     */
	public TrainingSet getTrainingSet(String dataDateString, StockCodeBean stockCodeBean, double normolizer) {
        TrainingSet trainingSet = new TrainingSet();

        List<StockInfo> dataList = stockFileReaderService.readMoney163StockDataFile(dataDateString, stockCodeBean);
        if (CollectionUtils.getSize(dataList) <= 0) {
            return trainingSet;
        }

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

    /**
     * TODO 如果有数据文件就直接读取, 否则从原始数据文件中读取
     * @param dataDateString
     * @param stockCodeBean
     * @return TrainingSet
     */
    public TrainingSet loadTrainingSet(String dataDateString, StockCodeBean stockCodeBean) {
        TrainingSet trainingSet = getTrainingSet(dataDateString, stockCodeBean, 100.00D);
        if (trainingSet != null) {
            File trainingSetDataFile = fileService.getTrainingSetDataFile(dataDateString, stockCodeBean.getArea(), stockCodeBean.getCode());
            if (trainingSetDataFile.exists()) {
                return trainingSet;
            }

            // 将训练数据写入数据文件中
            StringBuilder trainingData = new StringBuilder();
            for (Iterator<TrainingElement> it = trainingSet.iterator(); it.hasNext();) {
                SupervisedTrainingElement trainingElement = (SupervisedTrainingElement) it.next();
                Vector<Double> vector = trainingElement.getInput();

                StringBuilder data = new StringBuilder();
                for (Iterator<Double> vit = vector.iterator(); vit.hasNext();) {
                    data.append(vit.next()).append(Constants.SPLITER.COMMA);
                }
                data.append(trainingElement.getDesiredOutput().get(0));
                data.append(StringUtils.LF);
                trainingData.append(data);
            }
            FileUtils.writeStringToFile(trainingSetDataFile, trainingData.toString(), Boolean.TRUE, Constants.CHARSET.UTF8);
        }
        return trainingSet;
    }

    /**
     * 加载测试数据
     *
     * @param dataDateString
     * @param stockCodeBean
     * @param normolizer
     * @return
     */
    public TrainingSet loadTestTrainingSet(String dataDateString, StockCodeBean stockCodeBean, double normolizer) {
        TrainingSet testTrainingSet = new TrainingSet();

        List<StockInfo> dataList = stockFileReaderService.readMoney163StockDataFile(dataDateString, stockCodeBean);
        if (CollectionUtils.getSize(dataList) <= 0) {
            return testTrainingSet;
        }

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

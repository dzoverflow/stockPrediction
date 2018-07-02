package com.ckjava.service;

import com.ckjava.bean.BuyReportBean;
import com.ckjava.bean.StockCodeBean;
import com.ckjava.samples.stockmarket.StockInfo;
import com.ckjava.xutils.*;
import org.neuroph.core.NeuralNetwork;
import org.neuroph.core.learning.SupervisedTrainingElement;
import org.neuroph.core.learning.TrainingElement;
import org.neuroph.core.learning.TrainingSet;
import org.neuroph.nnet.MultiLayerPerceptron;
import org.neuroph.nnet.learning.LMS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
@PropertySource(value = {"classpath:app.properties"})
public class StockPredictionService extends FileUtils implements Constants {

    private static final Logger logger = LoggerFactory.getLogger(StockPredictionService.class);

    @Value("${isPrediction}")
    private Boolean isPrediction;
    @Value("${dataDateString}")
    private String dataDateString;
    @Value("${neuralDateString}")
    private String neuralDateString;
    @Autowired
    private FileService fileService;
    @Autowired
    private StockFileReaderService stockFileReaderService;
    @Autowired
    private TrainingDataService trainingDataService;

    public String getDataDateString() {
        return dataDateString;
    }

    public String getNeuralDateString() {
        return neuralDateString;
    }

    private NumberFormat nt = NumberFormat.getPercentInstance();

    public Boolean getIsPrediction() {
        return isPrediction;
    }

    public BuyReportBean doPrediction(String dataDateString, String neuralDateString, StockCodeBean stockCodeBean) {
        BuyReportBean buyReportBean = new BuyReportBean(stockCodeBean);

        StringBuilder report = new StringBuilder();

        // 加载原始数据文件
        List<StockInfo> dataList = stockFileReaderService.readMoney163StockDataFile(dataDateString, stockCodeBean);
        if (CollectionUtils.getSize(dataList) <= 0) {
            return buyReportBean;
        }

        // 封装训练数据集
        TrainingSet trainingSet = getTrainingSet(dataDateString, stockCodeBean, dataList);

        // 使用数据集训练神经网络
        File neuralFile = fileService.getNeuralNetFile(neuralDateString, stockCodeBean.getCode());
        NeuralNetwork neuralNet = null;
        if (neuralFile.exists() && neuralFile.length() > 0) {
            logger.info("neuralNetFile 存在，开始加载。。。");
            try {
                neuralNet = NeuralNetwork.load(new FileInputStream(neuralFile));
                logger.info("load exists neural file success");
            } catch (Exception e) {
                logger.error(this.getClass().getName().concat(".doPrediction has error"), e);
                return buyReportBean;
            }
        } else {
            String neuralNetFilePath = neuralFile.getAbsolutePath();
            boolean createNeuralFile = createFile(neuralNetFilePath);
            if (createNeuralFile) {
                logger.info("创建 neuralNetFile 文件:{} 成功", neuralNetFilePath);
            } else {
                logger.info("创建 neuralNetFile 文件:{} 失败", neuralNetFilePath);
            }

            try {
                logger.info("start training neural net");
                neuralNet = new MultiLayerPerceptron(4, 9, 1);
                int maxIterations = 10000;
                ((LMS) neuralNet.getLearningRule()).setMaxError(0.001);//0-1
                ((LMS) neuralNet.getLearningRule()).setLearningRate(0.7);//0-1
                ((LMS) neuralNet.getLearningRule()).setMaxIterations(maxIterations);//0-1
                neuralNet.learnInSameThread(trainingSet);
                logger.info("training neural net success");
                neuralNet.save(neuralNetFilePath);
                logger.info("save neural file success, path:{}", neuralNetFilePath);
            } catch (Exception e) {
                logger.error("training neuralNet has error, e");
            }

        }
        logger.info("Time stamp N2:" + new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss:MM").format(new Date()));

        if (neuralNet == null) {
            return buyReportBean;
        }

        // 使用测试集
        // 加载最近的 4 天数据
        TrainingSet testSet = trainingDataService.getTestTrainingSet(dataList, 100.00D);
        if (testSet == null || testSet.isEmpty()) {
            return buyReportBean;
        }

        // 找到最近连续上涨幅度超过 n% 的股票
        findLastNUp(testSet, dataDateString, stockCodeBean);

        // 基于当前的预测再预测多少次
        Date now = new Date();
        report.append("start neural prediction").append(StringUtils.LF);
        BigDecimal totalChange = BigDecimal.ZERO;
        List<String> plusList = new ArrayList<>();
        nt.setMinimumFractionDigits(2);
        for (int i = 0; i < 30; i++) {
            // 获取测试集中的最后一个元素
            TrainingElement testElement = testSet.trainingElements().get(i);
            neuralNet.setInput(testElement.getInput());
            neuralNet.calculate();
            Vector<Double> networkOutput = neuralNet.getOutput();
            double outputData = BigDecimal.valueOf(networkOutput.get(0)).setScale(4, BigDecimal.ROUND_HALF_UP).doubleValue();

            now = DateUtils.addDays(now, 1);

            report.append(DateUtils.formatTime(now.getTime(), "yyyy-MM-dd") + " " + (i + 1) + " -> Input: " + testElement.getInput());
            report.append(" Output: " + outputData);
            BigDecimal lastFinalPrice = BigDecimal.valueOf(testElement.getInput().get(3)).multiply(BigDecimal.valueOf(100d));
            BigDecimal pridictionPrice = BigDecimal.valueOf(outputData).multiply(BigDecimal.valueOf(100d));
            BigDecimal valueChange = pridictionPrice.subtract(lastFinalPrice).setScale(2, BigDecimal.ROUND_HALF_UP);
            BigDecimal valueChangePercent = BigDecimal.ZERO;
            if (valueChange.compareTo(BigDecimal.ZERO) > 0) {
                valueChangePercent = valueChange.divide(lastFinalPrice, 4, BigDecimal.ROUND_HALF_UP);
                plusList.add(valueChange.toString().concat(SPLITER.AT).concat(nt.format(valueChangePercent)).concat(SPLITER.COMMA));
            }
            if (i == 0) {
                buyReportBean.setFirstChange(valueChangePercent);
            }
            totalChange = totalChange.add(valueChange);
            report.append(" 相比上一次变化: " + valueChange.toString()).append(StringUtils.LF);
            // 将预测值再加入到测试集中
            Vector<Double> lastTestInput = testElement.getInput();
            double d1 = lastTestInput.get(1);
            double d2 = lastTestInput.get(2);
            double d3 = lastTestInput.get(3);
            double d4 = outputData;
            testSet.addElement(new TrainingElement(new double[]{d1, d2, d3, d4}));
        }

        buyReportBean.setPlusList(plusList);
        buyReportBean.setTotalChange(totalChange.setScale(2, BigDecimal.ROUND_HALF_UP));

        report.append("Time stamp N3:" + new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss:MM").format(new Date()));
        logger.info("report = {}", report.toString());

        FileUtils.writeStringToFile(fileService.getPredictionResultFile(dataDateString, stockCodeBean.getCode()), report.toString(), Boolean.FALSE, Constants.CHARSET.UTF8);

        return buyReportBean;
    }

    private void findLastNUp(TrainingSet testSet, String dataDateString, StockCodeBean stockCodeBean) {
        TrainingElement testElement = testSet.trainingElements().get(0);
        Vector<Double> lastTestInput = testElement.getInput();
        double d0 = lastTestInput.get(0);
        double d1 = lastTestInput.get(1);
        double d2 = lastTestInput.get(2);
        double d3 = lastTestInput.get(3);
        if (d0 <= 0 || d1 <= 0 || d2 <= 0 || d3 <= 0) {
            return;
        }
        // 连续3天的涨幅
        BigDecimal up1 = BigDecimal.valueOf(d1).subtract(BigDecimal.valueOf(d0)).divide(BigDecimal.valueOf(d0), 4, BigDecimal.ROUND_HALF_UP);
        BigDecimal up2 = BigDecimal.valueOf(d2).subtract(BigDecimal.valueOf(d1)).divide(BigDecimal.valueOf(d1), 4, BigDecimal.ROUND_HALF_UP);
        BigDecimal up3 = BigDecimal.valueOf(d3).subtract(BigDecimal.valueOf(d2)).divide(BigDecimal.valueOf(d2), 4, BigDecimal.ROUND_HALF_UP);
        StringBuilder data = new StringBuilder();

        // 分别超过 1% 到 10%
        BigDecimal upFactor = BigDecimal.ZERO;
        for (int i = 1; i <= 10; i++) {
            BigDecimal up = BigDecimal.valueOf(i/100D);
            if (up1.compareTo(up) >= 0
                    && up2.compareTo(up) >= 0
                    && up3.compareTo(up) >= 0) {
                upFactor = up;
            }
        }

        if (upFactor.compareTo(BigDecimal.ZERO) > 0) {
            stockCodeBean.setDesc("最近连续3天上涨:" + nt.format(upFactor));
            data.append(stockCodeBean.toString()).append(StringUtils.LF);
            writeStringToFile(fileService.getNUpFile(dataDateString), data.toString(), Boolean.TRUE, Constants.CHARSET.UTF8);
        }
    }

    private TrainingSet getTrainingSet(String dataDateString, StockCodeBean stockCodeBean, List<StockInfo> dataList) {
        TrainingSet trainingSet = trainingDataService.getTrainingSet(dataList, 100.00D);
        if (trainingSet != null) {
            File trainingSetDataFile = fileService.getTrainingSetDataFile(dataDateString, stockCodeBean.getCode());
            if (trainingSetDataFile.exists()) {
                return trainingSet;
            }

            StringBuilder trainingData = new StringBuilder();
            for (Iterator<TrainingElement> it = trainingSet.iterator(); it.hasNext();) {
                SupervisedTrainingElement trainingElement = (SupervisedTrainingElement) it.next();
                Vector<Double> vector = trainingElement.getInput();

                StringBuilder data = new StringBuilder();
                for (Iterator<Double> vit = vector.iterator(); vit.hasNext();) {
                    data.append(vit.next()).append(SPLITER.COMMA);
                }
                data.append(trainingElement.getDesiredOutput().get(0));
                data.append(StringUtils.LF);
                trainingData.append(data);
            }
            FileUtils.writeStringToFile(trainingSetDataFile, trainingData.toString(), Boolean.TRUE, CHARSET.UTF8);
        }
        return trainingSet;
    }
}

package com.ckjava.service;

import com.ckjava.bean.StockCodeBean;
import com.ckjava.samples.stockmarket.StockFileReader;
import com.ckjava.samples.stockmarket.StockInfo;
import com.ckjava.samples.stockmarket.TrainingData;
import com.ckjava.xutils.*;
import org.neuroph.core.NeuralNetwork;
import org.neuroph.core.learning.TrainingElement;
import org.neuroph.core.learning.TrainingSet;
import org.neuroph.nnet.MultiLayerPerceptron;
import org.neuroph.nnet.learning.LMS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Vector;

@Service
@PropertySource(value = {"classpath:app.properties"})
public class StockPredictionService implements Constants {

    private static final Logger logger = LoggerFactory.getLogger(FileDownloadService.class);

    @Value("${dataPath}")
    private String dataPath;

    private String currentDateString = DateUtils.formatTime(new Date().getTime(), "yyyyMMdd");
    public String getDataPath() {
        return dataPath;
    }
    public String getCurrentDateString() {
        return currentDateString;
    }

    public String doPrediction(StockCodeBean stockCodeBean) {
        StringBuilder simpleResult = new StringBuilder();
        simpleResult.append(stockCodeBean.getCode()).append(SPLITER.COLON);

        StringBuilder report = new StringBuilder();

        String trainingSetFilePath = dataPath + stockCodeBean.getCode() + "/money163/" + "data_" + currentDateString + ".csv";
        String neuralNetFilePath = dataPath + stockCodeBean.getCode() + "/neural/" + "neural_" + currentDateString;
        String predictionFilePath = dataPath + stockCodeBean.getCode() + "/report/" + "neural_" + currentDateString + ".txt";

        StockFileReader fileReader = new StockFileReader();
        List<StockInfo> dataList = fileReader.readMoney163StockDataFile(trainingSetFilePath);
        if (CollectionUtils.getSize(dataList) <= 0) {
            return simpleResult.toString();
        }

        // 封装训练数据集
        TrainingData trainingData = new TrainingData(dataList);
        trainingData.setNormolizer(100.00D);
        TrainingSet trainingSet = trainingData.getTrainingSet();

        // 使用数据集训练神经网络
        File neuralFile = new File(neuralNetFilePath);
        NeuralNetwork neuralNet = null;
        if (neuralFile.exists()) {
            try {
                neuralNet = NeuralNetwork.load(new FileInputStream(neuralFile));
                logger.info("load exists neural file success");
            } catch (Exception e) {
                logger.error(this.getClass().getName().concat(".doPrediction has error"), e);
            }
        } else {
            boolean createNeuralFile = FileUtils.createFile(neuralNetFilePath);
            if (createNeuralFile) {
                logger.info("创建文件:{} 成功", neuralNetFilePath);
            } else {
                logger.info("创建文件:{} 失败", neuralNetFilePath);
            }

            neuralNet = new MultiLayerPerceptron(4, 9, 1);
            int maxIterations = 10000;
            ((LMS) neuralNet.getLearningRule()).setMaxError(0.001);//0-1
            ((LMS) neuralNet.getLearningRule()).setLearningRate(0.7);//0-1
            ((LMS) neuralNet.getLearningRule()).setMaxIterations(maxIterations);//0-1
            neuralNet.learnInSameThread(trainingSet);
            logger.info("training neural net success");
            neuralNet.save(neuralNetFilePath);
            logger.info("save neural file success, path:{}", neuralNetFilePath);
        }
        logger.info("Time stamp N2:" + new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss:MM").format(new Date()));

        // 使用测试集
        // 加载最近的 4 天数据
        TrainingSet testSet = trainingData.getTestTrainingSet();

        // 基于当前的预测再预测多少次
        Date now = new Date();
        report.append("start neural prediction").append(StringUtils.LF);
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
            BigDecimal valueChange = BigDecimal.valueOf(outputData).subtract(BigDecimal.valueOf(testElement.getInput().get(3))).multiply(BigDecimal.valueOf(100d)).setScale(2, BigDecimal.ROUND_HALF_UP);
            if (valueChange.compareTo(BigDecimal.ZERO) > 0) {
                simpleResult.append("+");
            }
            report.append(" 相比上一次变化: " + valueChange.toString()).append(StringUtils.LF);
            // 将预测值再加入到测试集中
            Vector<Double> lastTestInput = testElement.getInput();
            double d1 = lastTestInput.get(1);
            double d2 = lastTestInput.get(2);
            double d3 = lastTestInput.get(3);
            double d4 = outputData;
            testSet.addElement(new TrainingElement(new double[]{d1, d2, d3, d4}));
        }

        report.append("Time stamp N3:" + new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss:MM").format(new Date()));
        logger.info("report = {}", report.toString());

        FileUtils.writeStringToFile(new File(predictionFilePath), report.toString(), Boolean.FALSE, Constants.CHARSET.UTF8);

        return simpleResult.toString();
    }
}

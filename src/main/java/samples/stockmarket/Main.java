/***
 * The Example is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * The Example is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Neuroph. If not, see <http://www.gnu.org/licenses/>.
 */
package samples.stockmarket;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Vector;

import org.neuroph.core.NeuralNetwork;
import org.neuroph.core.learning.SupervisedTrainingElement;
import org.neuroph.core.learning.TrainingElement;
import org.neuroph.core.learning.TrainingSet;
import org.neuroph.nnet.MultiLayerPerceptron;
import org.neuroph.nnet.learning.LMS;

import com.ckjava.xutils.Constants;
import com.ckjava.xutils.DateUtils;
import com.ckjava.xutils.FileUtils;
import com.ckjava.xutils.StringUtils;

/**
 *
 * @author Dr.V.Steinhauer
 */
public class Main {
	
    public static void main(String[] args) throws FileNotFoundException {
    	String dataPath = "D:/git-workspace/stockPrediction/data/";
    	String code = "600332";
    	String dateString = DateUtils.formatTime(new Date().getTime(), "yyyyMMdd");
    	
    	StringBuilder report = new StringBuilder();
    	
        //test1();
    	String trainingSetFilePath = dataPath + "money163/" + code +  "_data_" + dateString + ".csv";
    	String neuralNetFilePath = dataPath + "neural/" + code + "_neural_" + dateString;
    	String predictionFilePath = dataPath + "report/" + code + "_neural_" + dateString + ".txt";
    	
    	StockFileReader fileReader = new StockFileReader();
    	List<StockInfo> dataList = fileReader.readMoney163StockDataFile(trainingSetFilePath);
    	
    	// 封装训练数据集
    	TrainingData trainingData = new TrainingData(dataList);
    	trainingData.setNormolizer(100.00D);
    	TrainingSet trainingSet = trainingData.getTrainingSet();
    	
    	// 使用数据集训练神经网络
    	File neuralFile = new File(neuralNetFilePath);
    	NeuralNetwork neuralNet = null;
    	if (neuralFile.exists()) {
    		neuralNet = NeuralNetwork.load(new FileInputStream(neuralFile));
    		System.out.println("load exists neural file success");
    	} else {
            neuralNet = new MultiLayerPerceptron(4, 9, 1);
            int maxIterations = 10000;
            ((LMS) neuralNet.getLearningRule()).setMaxError(0.001);//0-1
            ((LMS) neuralNet.getLearningRule()).setLearningRate(0.7);//0-1
            ((LMS) neuralNet.getLearningRule()).setMaxIterations(maxIterations);//0-1
            neuralNet.learnInSameThread(trainingSet);
            System.out.println("training neural net success");
            neuralNet.save(neuralNetFilePath);
            System.out.println("save neural file success, path:"+neuralNetFilePath);
    	}
        System.out.println("Time stamp N2:" + new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss:MM").format(new Date()));

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
            
            report.append(DateUtils.formatTime(now.getTime(), "yyyy-MM-dd") + " " + (i+1) + " -> Input: " + testElement.getInput());
            report.append(" Output: " + outputData);
            BigDecimal valueChange = BigDecimal.valueOf(outputData).subtract(BigDecimal.valueOf(testElement.getInput().get(3))).multiply(BigDecimal.valueOf(100d)).setScale(2, BigDecimal.ROUND_HALF_UP);
            report.append(" 相比上一次变化: " + valueChange.toString()).append(StringUtils.LF);;
        	// 将预测值再加入到测试集中
            Vector<Double> lastTestInput = testElement.getInput();
            double d1 = lastTestInput.get(1);
            double d2 = lastTestInput.get(2);
            double d3 = lastTestInput.get(3);
            double d4 = outputData;
            testSet.addElement(new TrainingElement(new double[]{d1, d2, d3, d4}));
		}

        report.append("Time stamp N3:" + new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss:MM").format(new Date()));
        System.out.println(report.toString());
        
        FileUtils.writeStringToFile(new File(predictionFilePath), report.toString(), Boolean.FALSE, Constants.CHARSET.UTF8);
        
        System.exit(0);
    }

	public static void test1() {
		System.out.println("Time stamp N1:" + new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss:MM").format(new Date()));

        int maxIterations = 10000;
        NeuralNetwork neuralNet = new MultiLayerPerceptron(4, 9, 1);
        ((LMS) neuralNet.getLearningRule()).setMaxError(0.001);//0-1
        ((LMS) neuralNet.getLearningRule()).setLearningRate(0.7);//0-1
        ((LMS) neuralNet.getLearningRule()).setMaxIterations(maxIterations);//0-1
        TrainingSet trainingSet = new TrainingSet();

        double daxmax = 10000.0D;
        trainingSet.addElement(new SupervisedTrainingElement(new double[]{3710.0D / daxmax, 3690.0D / daxmax, 3890.0D / daxmax, 3695.0D / daxmax}, new double[]{3666.0D / daxmax}));
        trainingSet.addElement(new SupervisedTrainingElement(new double[]{3690.0D / daxmax, 3890.0D / daxmax, 3695.0D / daxmax, 3666.0D / daxmax}, new double[]{3692.0D / daxmax}));
        trainingSet.addElement(new SupervisedTrainingElement(new double[]{3890.0D / daxmax, 3695.0D / daxmax, 3666.0D / daxmax, 3692.0D / daxmax}, new double[]{3886.0D / daxmax}));
        trainingSet.addElement(new SupervisedTrainingElement(new double[]{3695.0D / daxmax, 3666.0D / daxmax, 3692.0D / daxmax, 3886.0D / daxmax}, new double[]{3914.0D / daxmax}));
        trainingSet.addElement(new SupervisedTrainingElement(new double[]{3666.0D / daxmax, 3692.0D / daxmax, 3886.0D / daxmax, 3914.0D / daxmax}, new double[]{3956.0D / daxmax}));
        trainingSet.addElement(new SupervisedTrainingElement(new double[]{3692.0D / daxmax, 3886.0D / daxmax, 3914.0D / daxmax, 3956.0D / daxmax}, new double[]{3953.0D / daxmax}));
        trainingSet.addElement(new SupervisedTrainingElement(new double[]{3886.0D / daxmax, 3914.0D / daxmax, 3956.0D / daxmax, 3953.0D / daxmax}, new double[]{4044.0D / daxmax}));
        trainingSet.addElement(new SupervisedTrainingElement(new double[]{3914.0D / daxmax, 3956.0D / daxmax, 3953.0D / daxmax, 4044.0D / daxmax}, new double[]{3987.0D / daxmax}));
        trainingSet.addElement(new SupervisedTrainingElement(new double[]{3956.0D / daxmax, 3953.0D / daxmax, 4044.0D / daxmax, 3987.0D / daxmax}, new double[]{3996.0D / daxmax}));
        trainingSet.addElement(new SupervisedTrainingElement(new double[]{3953.0D / daxmax, 4044.0D / daxmax, 3987.0D / daxmax, 3996.0D / daxmax}, new double[]{4043.0D / daxmax}));
        trainingSet.addElement(new SupervisedTrainingElement(new double[]{4044.0D / daxmax, 3987.0D / daxmax, 3996.0D / daxmax, 4043.0D / daxmax}, new double[]{4068.0D / daxmax}));
        trainingSet.addElement(new SupervisedTrainingElement(new double[]{3987.0D / daxmax, 3996.0D / daxmax, 4043.0D / daxmax, 4068.0D / daxmax}, new double[]{4176.0D / daxmax}));
        trainingSet.addElement(new SupervisedTrainingElement(new double[]{3996.0D / daxmax, 4043.0D / daxmax, 4068.0D / daxmax, 4176.0D / daxmax}, new double[]{4187.0D / daxmax}));
        trainingSet.addElement(new SupervisedTrainingElement(new double[]{4043.0D / daxmax, 4068.0D / daxmax, 4176.0D / daxmax, 4187.0D / daxmax}, new double[]{4223.0D / daxmax}));
        trainingSet.addElement(new SupervisedTrainingElement(new double[]{4068.0D / daxmax, 4176.0D / daxmax, 4187.0D / daxmax, 4223.0D / daxmax}, new double[]{4259.0D / daxmax}));
        trainingSet.addElement(new SupervisedTrainingElement(new double[]{4176.0D / daxmax, 4187.0D / daxmax, 4223.0D / daxmax, 4259.0D / daxmax}, new double[]{4203.0D / daxmax}));
        trainingSet.addElement(new SupervisedTrainingElement(new double[]{4187.0D / daxmax, 4223.0D / daxmax, 4259.0D / daxmax, 4203.0D / daxmax}, new double[]{3989.0D / daxmax}));
        neuralNet.learnInSameThread(trainingSet);
        System.out.println("Time stamp N2:" + new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss:MM").format(new Date()));

        TrainingSet testSet = new TrainingSet();
        testSet.addElement(new TrainingElement(new double[]{4223.0D / daxmax, 4259.0D / daxmax, 4203.0D / daxmax, 3989.0D / daxmax}));

        for (TrainingElement testElement : testSet.trainingElements()) {
            neuralNet.setInput(testElement.getInput());
            neuralNet.calculate();
            Vector<Double> networkOutput = neuralNet.getOutput();
            System.out.print("Input: " + testElement.getInput());
            System.out.println(" Output: " + networkOutput);
        }

        //Experiments:
        //                   calculated
        //31;3;2009;4084,76 -> 4121 Error=0.01 Rate=0.7 Iterat=100
        //31;3;2009;4084,76 -> 4096 Error=0.01 Rate=0.7 Iterat=1000
        //31;3;2009;4084,76 -> 4093 Error=0.01 Rate=0.7 Iterat=10000
        //31;3;2009;4084,76 -> 4108 Error=0.01 Rate=0.7 Iterat=100000
        //31;3;2009;4084,76 -> 4084 Error=0.001 Rate=0.7 Iterat=10000

        System.out.println("Time stamp N3:" + new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss:MM").format(new Date()));
        System.exit(0);
	}
}

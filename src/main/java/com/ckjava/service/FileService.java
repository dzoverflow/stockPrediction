package com.ckjava.service;

import com.ckjava.xutils.FileUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Service;

import java.io.File;

@Service
@PropertySource(value = { "classpath:app.properties" })
public class FileService extends FileUtils {

    @Value("${dataPath}")
    private String dataPath;

    public File getStockCodeFile(String area) {
        return new File(joinPath(new String[] {dataPath, "stockCode-"+area+".txt"}));
    }

    public File getRawDataFile(String dateString, String code) {
        return new File(joinPath(new String[]{ dataPath, dateString, code, "money163", "raw_data.csv" }));
    }

    public File getTrainingSetDataFile(String dateString, String code) {
        return new File(joinPath(new String[]{ dataPath, dateString, code, "money163", "training_data.csv" }));
    }

    public File getNeuralNetFile(String dateString, String code) {
        return new File(joinPath(new String[]{ dataPath, dateString, code, "neural", "neural.dat" }));
    }

    public File getPredictionResultFile(String dateString, String code) {
        return new File(joinPath(new String[]{ dataPath, dateString, code, "report", "report.txt" }));
    }

    public File getBuyReportFile(String dateString) {
        return new File(joinPath(new String[]{ dataPath, "buyReport_" + dateString + ".txt" }));
    }

    public File getNUpFile(String dateString) {
        return new File(joinPath(new String[]{ dataPath, "buyReport_nup_" + dateString + ".txt" }));
    }

    public File getBlacklistFile() {
        return new File(joinPath(new String[]{ dataPath, "blacklist.txt" }));
    }
}

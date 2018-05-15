/***
 * TrainingData is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * TrainingData is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Neuroph. If not, see <http://www.gnu.org/licenses/>.
 */


package samples.stockmarket;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import org.neuroph.core.learning.SupervisedTrainingElement;
import org.neuroph.core.learning.TrainingElement;
import org.neuroph.core.learning.TrainingSet;

/**
 *
 * @author Dr.V.Steinhauer
 */
public class TrainingData {

    private List<StockInfo> dataList;
    private Date limitDate;
    private TrainingSet trainingSet = new TrainingSet();
    private double normolizer = 10000.0D;
    private double minlevel = 0.00D;

    public List<StockInfo> getDataList() {
		return dataList;
	}

	public void setDataList(List<StockInfo> dataList) {
		this.dataList = dataList;
	}

	public double getMinlevel() {
		return minlevel;
	}

	public void setMinlevel(double minlevel) {
		this.minlevel = minlevel;
	}

	public double getNormolizer() {
        return normolizer;
    }

    public void setNormolizer(double normolizer) {
        this.normolizer = normolizer;
    }

    public Date getLimitDate() {
		return limitDate;
	}

	public void setLimitDate(Date limitDate) {
		this.limitDate = limitDate;
	}

	public TrainingData() {
    }
    
    public TrainingData(List<StockInfo> dataList) {
    	this.dataList = dataList;
    }

    public TrainingData(List<StockInfo> dataList, Date limitDate) {
		super();
		this.dataList = dataList;
		this.limitDate = limitDate;
	}

	public TrainingSet getTrainingSet() {
        int length = dataList.size();
        if (length < 5) {
            System.out.println("dataList.size < 5");
            return null;
        }
        try {
            for (int i = 0, c = dataList.size(); i + 4 < c; i++) {
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
                
                i += 3;
                
                if (i > c-10) {
                	System.out.println(i + " " + d1 + " " + d2 + " " + d3 + " " + d4 + " ->" + d5);	
                }
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return null;
        }
        
        
        System.out.println("finish load TrainingSet");
        
        return trainingSet;
    }
    
    public TrainingSet getTestTrainingSet() {
        int length = dataList.size();
        if (length < 5) {
            System.out.println("dataList.size < 5");
            return null;
        }
        TrainingSet testTrainingSet = new TrainingSet();
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
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return null;
        }
        System.out.println("finish load TestTrainingSet");
        
        return testTrainingSet;
    }

    public void setTrainingSet(TrainingSet trainingSet) {
        this.trainingSet = trainingSet;
    }

    
}

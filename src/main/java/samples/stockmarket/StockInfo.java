package samples.stockmarket;

public class StockInfo {

	private String date; // 日期
	private long dateValue; // 日期的 long 类型
	private String code; // 股票代码
	private String title; // 名称
	private String finalPrice; // 收盘价
	private String highestPrice; // 最高价
	private String lowestPrice; // 最低价
	private String startPrice; // 开盘价
	private String lastFinishPrice; // 前收盘
	private String changePrice; // 涨跌额
	private String changeRange; // 涨跌幅
	private String changeRate; // 换手率
	private String dealAmount; // 成交量
	private String dealMoney; // 成交金额
	private String marketValue; // 总市值
	private String circulateValue; // 流通市值
	public String getDate() {
		return date;
	}
	public void setDate(String date) {
		this.date = date;
	}
	public long getDateValue() {
		return dateValue;
	}
	public void setDateValue(long dateValue) {
		this.dateValue = dateValue;
	}
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getFinalPrice() {
		return finalPrice;
	}
	public void setFinalPrice(String finalPrice) {
		this.finalPrice = finalPrice;
	}
	public String getHighestPrice() {
		return highestPrice;
	}
	public void setHighestPrice(String highestPrice) {
		this.highestPrice = highestPrice;
	}
	public String getLowestPrice() {
		return lowestPrice;
	}
	public void setLowestPrice(String lowestPrice) {
		this.lowestPrice = lowestPrice;
	}
	public String getStartPrice() {
		return startPrice;
	}
	public void setStartPrice(String startPrice) {
		this.startPrice = startPrice;
	}
	public String getLastFinishPrice() {
		return lastFinishPrice;
	}
	public void setLastFinishPrice(String lastFinishPrice) {
		this.lastFinishPrice = lastFinishPrice;
	}
	public String getChangePrice() {
		return changePrice;
	}
	public void setChangePrice(String changePrice) {
		this.changePrice = changePrice;
	}
	public String getChangeRange() {
		return changeRange;
	}
	public void setChangeRange(String changeRange) {
		this.changeRange = changeRange;
	}
	public String getChangeRate() {
		return changeRate;
	}
	public void setChangeRate(String changeRate) {
		this.changeRate = changeRate;
	}
	public String getDealAmount() {
		return dealAmount;
	}
	public void setDealAmount(String dealAmount) {
		this.dealAmount = dealAmount;
	}
	public String getDealMoney() {
		return dealMoney;
	}
	public void setDealMoney(String dealMoney) {
		this.dealMoney = dealMoney;
	}
	public String getMarketValue() {
		return marketValue;
	}
	public void setMarketValue(String marketValue) {
		this.marketValue = marketValue;
	}
	public String getCirculateValue() {
		return circulateValue;
	}
	public void setCirculateValue(String circulateValue) {
		this.circulateValue = circulateValue;
	}
	
	public StockInfo(String date, long dateValue, String code, String title, String finalPrice) {
		super();
		this.date = date;
		this.dateValue = dateValue;
		this.code = code;
		this.title = title;
		this.finalPrice = finalPrice;
	}
	public StockInfo() {
		super();
	}
	@Override
	public String toString() {
		return "StockInfo [date=" + date + ", dateValue=" + dateValue + ", code=" + code + ", title=" + title
				+ ", finalPrice=" + finalPrice + "]";
	}
}

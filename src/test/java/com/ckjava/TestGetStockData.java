package com.ckjava;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.ckjava.xutils.FileUtils;
import com.ckjava.xutils.HttpClientUtils;
import com.ckjava.xutils.StringUtils;

public class TestGetStockData {
	
	public static void main(String[] args) throws Exception {
		String targetString = FileUtils.readFileContent(new File("d:\\Users\\chen_k\\Desktop\\stockCode.txt"), "UTF-8");
		List<String> codeList = extractVariable(targetString);
		System.out.println("size="+codeList.size());
		for (String string : codeList) {
			System.out.println(string);
		}
	}

	public static List<String> extractVariable(String targetString) {
		List<String> variableList = new ArrayList<>();
		if (StringUtils.isNotBlank(targetString) && targetString.contains("(") && targetString.contains(")")) {
			Pattern pattern = Pattern.compile("(\\([^\\).]*\\))");
			Matcher matcher = pattern.matcher(targetString);
			while (matcher.find()) {
				String matcherStr = matcher.group();
				String variable = matcherStr.replaceAll("\\(", "").replaceAll("\\)", "");
				variableList.add(variable);
			}
		}
		return variableList;
	}

	public static void downLoadFile() {
		String code = "0601899";
		Map<String, String> placeholderMap = new HashMap<>();
		placeholderMap.put("code", code);
		
		
		String url = "http://quotes.money.163.com/service/chddata.html?code=${code}&start=20080425&end=20180515&fields=TCLOSE;HIGH;LOW;TOPEN;LCLOSE;CHG;PCHG;TURNOVER;VOTURNOVER;VATURNOVER;TCAP;MCAP";
		url = StringUtils.replaceVariable(url, placeholderMap);
		
		String datas = HttpClientUtils.get(url, null, null);
		
		FileUtils.writeStringToFile(new File("D:\\Users\\chen_k\\Downloads\\" + code + "_data.txt"), datas, false, "UTF-8");
	}

}

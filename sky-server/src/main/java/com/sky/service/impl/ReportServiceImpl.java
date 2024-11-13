package com.sky.service.impl;

import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import com.sky.service.ReportService;
import com.sky.service.WorkspaceService;
import com.sky.vo.BusinessDataVO;
import com.sky.vo.TurnoverReportVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Service
@Slf4j
public class ReportServiceImpl implements ReportService {

  @Autowired
  private OrderMapper orderMapper;

  @Autowired
  private WorkspaceService workspaceService;

  public TurnoverReportVO getTurnoverStatistics(LocalDate begin, LocalDate end) {
	List<LocalDate> dateList = new ArrayList<>();
	dateList.add(begin);
	while (!begin.equals(end)) {
	  begin = begin.plusDays(1);
	  dateList.add(begin);
	}

	List<Double> turnoverList = new ArrayList<>();
	for (LocalDate date : dateList) {
	  LocalDateTime startTime = LocalDateTime.of(date, LocalTime.MIN); // 当天的 00:00
	  LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX); // 当天的最大时间
	  HashMap<Object, Object> map = new HashMap<>();
	  map.put("begin", startTime);
	  map.put("end", endTime);
	  map.put("status", Orders.COMPLETED);
	  Double turnover = orderMapper.sumByMap(map);
	  turnover = turnover == null ? 0.0 : turnover;
	  turnoverList.add(turnover);
	}

	return TurnoverReportVO.builder()
			.dateList(StringUtils.join(dateList, ","))
			.turnoverList(StringUtils.join(turnoverList, ","))
			.build();
  }

  /**
   * 导出营业数据excel报表
   *
   * @param response
   */
  public void exportBusinessData(HttpServletResponse response) throws IOException {
	// 1. 查询数据库获取营业数据 最近30天
	LocalDate DateBegin = LocalDate.now().minusDays(30); // 30天前
	LocalDate DateEnd = LocalDate.now().minusDays(1); // 1天前 当天还未确定
	LocalDateTime beginTime = LocalDateTime.of(DateBegin, LocalTime.MIN);
	LocalDateTime endTime = LocalDateTime.of(DateEnd, LocalTime.MAX);
	BusinessDataVO businessData = workspaceService.getBusinessData(beginTime, endTime);
	// 2. 通过POI将数据写入到Excel文件中
	// 获取 当前类的类加载器的类路径读取资源
	InputStream resourceAsStream = this.getClass().getClassLoader().getResourceAsStream("template/运营数据报表模板.xlsx");
	XSSFWorkbook workbook = new XSSFWorkbook(resourceAsStream);
	XSSFSheet sheet = workbook.getSheet("Sheet1");
	// 填充数据 时间
	XSSFRow row = sheet.getRow(1); // 第二行
	row.getCell(2).setCellValue("时间" + DateBegin + "-" + DateEnd);

	row = sheet.getRow(3);  // 第四行
	row.getCell(2).setCellValue(businessData.getTurnover());
	row.getCell(4).setCellValue(businessData.getOrderCompletionRate());
	row.getCell(6).setCellValue(businessData.getNewUsers());

	row = sheet.getRow(4);  // 第五行
	row.getCell(2).setCellValue(businessData.getValidOrderCount());
	row.getCell(4).setCellValue(businessData.getUnitPrice());

	for (int i = 0; i < 30; i++) {
	  LocalDate date = DateBegin.plusDays(i);
	  // 查询某一天的数据
	  BusinessDataVO data = workspaceService.getBusinessData(LocalDateTime.of(date, LocalTime.MIN), LocalDateTime.of(date, LocalTime.MAX));
	  row = sheet.getRow(7 + i);  // 第 7 + i 行
	  row.getCell(1).setCellValue(date.toString());
	  row.getCell(2).setCellValue(data.getTurnover());
	  row.getCell(3).setCellValue(data.getValidOrderCount());
	  row.getCell(4).setCellValue(data.getOrderCompletionRate());
	  row.getCell(5).setCellValue(data.getUnitPrice());
	  row.getCell(6).setCellValue(data.getNewUsers());
	}

	// 3. 通过输出流 response 将文件下载到客户端浏览器
	ServletOutputStream outputStream = response.getOutputStream();
	workbook.write(outputStream);

	// 关闭io流
	outputStream.close();
	workbook.close();
  }
}

package com.sky.controller.admin;

import com.sky.result.Result;
import com.sky.service.ReportService;
import com.sky.vo.TurnoverReportVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.resource.HttpResource;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/admin/report")
@Api(tags = "数据统计相关接口")
@Slf4j
public class ReportController {

  @Autowired
  private ReportService reportService;

  @GetMapping("/turnoverStatistics")
  @ApiOperation("营业额统计")
  public Result<TurnoverReportVO> turnoverStatistics(@RequestParam("begin") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate begin,
													 @RequestParam("end") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate end) {
	TurnoverReportVO turnoverReportVO = reportService.getTurnoverStatistics(begin, end);
	return Result.success(turnoverReportVO);
  }

  @GetMapping("/export")
  @ApiOperation("导出报表")
  public void export(HttpServletResponse response) throws IOException {
	reportService.exportBusinessData(response);
  }
}

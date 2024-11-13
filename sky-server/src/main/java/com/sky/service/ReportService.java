package com.sky.service;

import com.sky.vo.TurnoverReportVO;
import org.apache.http.HttpResponse;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDate;

public interface ReportService {


  TurnoverReportVO getTurnoverStatistics(LocalDate begin, LocalDate end);

  void exportBusinessData(HttpServletResponse response) throws IOException;
}

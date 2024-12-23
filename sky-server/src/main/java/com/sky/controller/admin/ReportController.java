package com.sky.controller.admin;

import com.sky.result.Result;
import com.sky.service.ReportService;
import com.sky.vo.OrderReportVO;
import com.sky.vo.SalesTop10ReportVO;
import com.sky.vo.TurnoverReportVO;
import com.sky.vo.UserReportVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

/**
 * 数据统计相关接口
 */
@Api("数据统计相关接口")
@RestController
@RequestMapping("/admin/report/")
@Slf4j
public class ReportController {

    @Autowired
    private ReportService reportService;

    /**
     * 营业额统计
     *
     * @param begin 从哪天开始统计
     * @param end   统计到哪天为止
     * @return
     */
    @ApiOperation("统计营业额数据")
    @GetMapping("/turnoverStatistics")
    public Result<TurnoverReportVO> turnoverStatistics(@DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate begin,
                                                       @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate end) {
        //这里要使用注解@DateTimeFormat来把前端传过来的string类型的字符串解析成日期格式，然后赋值给这里的形参变量，
        // 否则后端无法正常接收前端传过来的日期字符串（其实这里不定义这个注解也是可以的，因为之前设置了全局配置，来自动转换前端传过来的日期格式）
        log.info("统计营业额数据");
        TurnoverReportVO turnoverReportVO = reportService.getTurnoverStatistics(begin, end);
        return Result.success(turnoverReportVO);
    }

    /**
     * 用户统计
     *
     * @param begin 从哪天开始统计
     * @param end   统计到哪天为止
     * @return
     */
    @ApiOperation("用户统计")
    @GetMapping("/userStatistics")
    public Result<UserReportVO> userStatistics(@DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate begin,
                                                       @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate end) {
        //这里要使用注解@DateTimeFormat来把前端传过来的string类型的字符串解析成日期格式，然后赋值给这里的形参变量，
        // 否则后端无法正常接收前端传过来的日期字符串（其实这里不定义这个注解也是可以的，因为之前设置了全局配置，来自动转换前端传过来的日期格式）
        log.info("用户统计");
        UserReportVO userReportVO = reportService.getUserStatistics(begin, end);
        return Result.success(userReportVO);
    }

    /**
     * 订单统计
     *
     * @param begin 从哪天开始统计
     * @param end   统计到哪天为止
     * @return
     */
    @ApiOperation("订单统计")
    @GetMapping("/ordersStatistics")
    public Result<OrderReportVO> ordersStatistics(@DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate begin,
                                               @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate end) {
        //这里要使用注解@DateTimeFormat来把前端传过来的string类型的字符串解析成日期格式，然后赋值给这里的形参变量，
        // 否则后端无法正常接收前端传过来的日期字符串（其实这里不定义这个注解也是可以的，因为之前设置了全局配置，来自动转换前端传过来的日期格式）
        log.info("订单统计");
        OrderReportVO orderReportVO = reportService.getOrderStatistics(begin, end);
        return Result.success(orderReportVO);
    }


    /**
     * 销量top10统计
     *
     * @param begin 从哪天开始统计
     * @param end   统计到哪天为止
     * @return
     */
    @ApiOperation("销量top10统计")
    @GetMapping("/top10")
    public Result<SalesTop10ReportVO> top10Statistics(@DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate begin,
                                                      @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate end) {
        //这里要使用注解@DateTimeFormat来把前端传过来的string类型的字符串解析成日期格式，然后赋值给这里的形参变量，
        // 否则后端无法正常接收前端传过来的日期字符串（其实这里不定义这个注解也是可以的，因为之前设置了全局配置，来自动转换前端传过来的日期格式）
        log.info("销量top10统计");
        SalesTop10ReportVO salesTop10ReportVO = reportService.getTop10Statistics(begin, end);
        return Result.success(salesTop10ReportVO);
    }





}

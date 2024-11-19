package com.sky.service.impl;

import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import com.sky.service.ReportService;
import com.sky.vo.TurnoverReportVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.sql.Date;


import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ReportServiceImpl implements ReportService {


    @Autowired
    private OrderMapper orderMapper;

    /**
     * 统计指定时间区间内的营业额数据
     *
     * @param begin
     * @param end
     * @return
     */

    @Override
//    public TurnoverReportVO getTurnoverStatistics(LocalDate begin, LocalDate end) {
//
//        ArrayList<LocalDate> dateList = new ArrayList<>();
//
//        //获取所有的日期数据，然后以逗号为间隔，把他们拼成一个字符串
//        while (begin.isBefore(end)) {
//            dateList.add(begin);
//            begin = begin.plusDays(1);
//        }
//        String dateResult = StringUtils.join(dateList, ",");
//
//        //获取所有日期所对应的营业额数据，然后以逗号为分隔，把他们拼接成一个字符串
//        ArrayList<Double> turnoverList = new ArrayList<>();
//        for (LocalDate localDate : dateList) {
//            LocalDateTime startTime = LocalDateTime.of(localDate, LocalTime.MIN);      //得到当天的最早的时间，即凌晨
//            LocalDateTime endTime = LocalDateTime.of(localDate, LocalTime.MAX);       //得到当天的最晚的时间
//
//            HashMap<String, Object> map = new HashMap<>();
//            map.put("startTime", startTime);
//            map.put("endTime", endTime);
//            map.put("status", Orders.COMPLETED);
//            Double turnover = orderMapper.sumByMap(map);
//            //如果当天没有卖出东西的话，那么营业额查询出来会是null，所以在这里判断一下，如果是Null的话，就把营业额置为0
//            turnover = turnover == null ? 0.0 : turnover;
//            turnoverList.add(turnover);
//        }
//        String turnoverResult = StringUtils.join(turnoverList, ",");
//
//        TurnoverReportVO turnoverReportVO = new TurnoverReportVO();
//        turnoverReportVO.setDateList(dateResult);
//        turnoverReportVO.setTurnoverList(turnoverResult);
//        return turnoverReportVO;
//    }

    public TurnoverReportVO getTurnoverStatistics(LocalDate begin, LocalDate end) {
        // 查询范围内的营业额数据
        LocalDateTime startTime = LocalDateTime.of(begin, LocalTime.MIN);
        LocalDateTime endTime = LocalDateTime.of(end, LocalTime.MAX);

        // 查询数据库并返回结果
        Map<LocalDate, Object> turnoverMap = orderMapper.sumByDateRange(startTime, endTime, Orders.COMPLETED);

        // 构造日期列表和营业额列表
        List<LocalDate> dateList = new ArrayList<>();
        List<Double> turnoverList = new ArrayList<>();

        LocalDate current = begin;
        while (!current.isAfter(end)) {
            dateList.add(current);
            Date sqlDate = Date.valueOf(current);
            // 获取某个 LocalDate 对应的 Map<LocalDate, Double>
            Map<String, Object> nestedMap = (Map<String, Object>) turnoverMap.get(sqlDate);
            if (nestedMap != null) {
                BigDecimal turnover = (BigDecimal) nestedMap.getOrDefault("turnover", 0.0);
                double money = turnover.doubleValue();
                turnoverList.add(money); // 默认值为 0.0
            }else {
                turnoverList.add(0.0);
            }
            current = current.plusDays(1);
        }

        String dateResult = StringUtils.join(dateList, ",");
        String turnoverResult = StringUtils.join(turnoverList, ",");

        // 构造返回对象
        TurnoverReportVO turnoverReportVO = new TurnoverReportVO();
        turnoverReportVO.setDateList(dateResult);
        turnoverReportVO.setTurnoverList(turnoverResult);
        return turnoverReportVO;
    }





}

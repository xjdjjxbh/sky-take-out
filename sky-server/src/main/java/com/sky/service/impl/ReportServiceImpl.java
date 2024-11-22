package com.sky.service.impl;

import com.sky.dto.GoodsSalesDTO;
import com.sky.entity.Orders;
import com.sky.mapper.*;
import com.sky.service.ReportService;
import com.sky.vo.OrderReportVO;
import com.sky.vo.SalesTop10ReportVO;
import com.sky.vo.TurnoverReportVO;
import com.sky.vo.UserReportVO;
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


@Slf4j
@Service
public class ReportServiceImpl implements ReportService {


    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private SetmealMapper setmealMapper;

    @Autowired
    private DishMapper dishMapper;

    @Autowired
    private OrderDetailMapper orderDetailMapper;

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
        begin =begin.plusDays(1);
        end =end.plusDays(1);

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
            } else {
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

    /**
     * 统计指定时间区间内的用户数量
     *
     * @param begin
     * @param end
     * @return
     */
    @Override
    public UserReportVO getUserStatistics(LocalDate begin, LocalDate end) {
        begin =begin.plusDays(1);
        end =end.plusDays(1);

        List<LocalDate> dateList = new ArrayList<>();
        end = end.plusDays(1);

        while (begin.isBefore(end)) {
            dateList.add(begin);
            begin = begin.plusDays(1);
        }

        //存放每天新增用户数量
        ArrayList<Integer> newUserList = new ArrayList<>();
        //存放每天用户总数量
        ArrayList<Integer> totalUserList = new ArrayList<>();


        for (LocalDate localDate : dateList) {
            HashMap<String, LocalDateTime> conditionMap = new HashMap<>();
            LocalDateTime todayBegin = LocalDateTime.of(localDate, LocalTime.MIN);
            LocalDateTime todayEnd = LocalDateTime.of(localDate, LocalTime.MAX);
            conditionMap.put("end", todayEnd);
            Integer todayUserNumber = userMapper.countByMap(conditionMap);
            conditionMap.put("start", todayBegin);
            Integer todayNewUserNumber = userMapper.countByMap(conditionMap);
            totalUserList.add(todayUserNumber);      //添加到今天为止的用户总数
            newUserList.add(todayNewUserNumber);       //添加今天新增的用户数量
        }

        String dateListString = StringUtils.join(dateList, ",");
        String newUserString = StringUtils.join(newUserList, ",");
        String totalUserString = StringUtils.join(totalUserList, ",");
        return UserReportVO
                .builder()
                .dateList(dateListString)
                .newUserList(newUserString)
                .totalUserList(totalUserString)
                .build();
    }

    /**
     * 订单统计
     *
     * @param begin
     * @param end
     * @return
     */
    @Override
    public OrderReportVO getOrderStatistics(LocalDate begin, LocalDate end) {
        begin =begin.plusDays(1);
        end =end.plusDays(1);
        //存放请求日期区间里面的每一天
        List<LocalDate> dateList = new ArrayList<>();
        end = end.plusDays(1);

        while (begin.isBefore(end)) {
            dateList.add(begin);
            begin = begin.plusDays(1);
        }

        //存放每天订单数量
        ArrayList<Integer> orderCountList = new ArrayList<>();
        //存放已完成的订单的数量
        ArrayList<Integer> orderCompletedCountList = new ArrayList<>();
        for (LocalDate localDate : dateList) {
            HashMap conditionMap = new HashMap<>();
            LocalDateTime todayBegin = LocalDateTime.of(localDate, LocalTime.MIN);
            LocalDateTime todayEnd = LocalDateTime.of(localDate, LocalTime.MAX);
            conditionMap.put("end", todayEnd);
            conditionMap.put("start", todayBegin);
            Integer orderNumber = userMapper.countOrderNumberByMap(conditionMap);
            conditionMap.put("status", Orders.COMPLETED);
            Integer orderCompletedNumber = userMapper.countOrderNumberByMap(conditionMap);
            orderCountList.add(orderNumber);
            orderCompletedCountList.add(orderCompletedNumber);

        }

        int totalOrderNumber = orderCountList.stream().mapToInt(Integer::intValue).sum();
        int finishedOrderNumber = orderCompletedCountList.stream().mapToInt(Integer::intValue).sum();

        Double finishRate = totalOrderNumber == 0 ? 0.0 : (finishedOrderNumber + 0.0) / totalOrderNumber;

        return OrderReportVO
                .builder()
                .totalOrderCount(totalOrderNumber)
                .validOrderCount(finishedOrderNumber)
                .dateList(StringUtils.join(dateList, ","))
                .orderCountList(StringUtils.join(orderCountList, ","))
                .validOrderCountList(StringUtils.join(orderCompletedCountList, ","))
                .orderCompletionRate(finishRate)
                .build();
    }

    /**
     * 销量top10统计
     *
     * @param begin
     * @param end
     * @return
     */
    @Override
    public SalesTop10ReportVO getTop10Statistics(LocalDate begin, LocalDate end) {

        end = end.plusDays(1);

        ArrayList<String> nameList = new ArrayList<>();
        ArrayList<Integer> numberList = new ArrayList<>();

        LocalDateTime todayBegin = LocalDateTime.of(begin, LocalTime.MIN);
        LocalDateTime todayEnd = LocalDateTime.of(end, LocalTime.MAX);
        HashMap<Object, Object> map = new HashMap<>();

        map.put("start", todayBegin);
        map.put("end", todayEnd);
        map.put("status", Orders.COMPLETED);
        //获取到销量前10的菜品或者套餐列表
        List<GoodsSalesDTO> goodsSalesDTOList = orderMapper.getSalesTop10(map);

        for (GoodsSalesDTO goodsSalesDTO : goodsSalesDTOList) {
            nameList.add(goodsSalesDTO.getName());
            numberList.add(goodsSalesDTO.getNumber());
        }

        return SalesTop10ReportVO
                .builder()
                .nameList(StringUtils.join(nameList, ","))
                .numberList(StringUtils.join(numberList, ","))
                .build();
    }


}

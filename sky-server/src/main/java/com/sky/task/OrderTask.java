package com.sky.task;

import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 定时任务类，定时处理订单状态
 */
@Component
@Slf4j
public class OrderTask {

    @Autowired
    private OrderMapper orderMapper;


    /**
    处理超时订单
     */
    @Scheduled(cron = "0 * * * * ? ")   //每分钟触发一次
//    @Scheduled(cron = "1/5 * * * * ? ")
    public void processTimeOut() {
        log.info("定时处理超时订单：{}", LocalDateTime.now());

        //获取15分钟之前的时间
        LocalDateTime time = LocalDateTime.now().plusMinutes(-15);
        //处理处于待付款，并且待付款时间超过15分钟的订单
        List<Orders> orders = orderMapper.getByStatusAndOrderTime(Orders.PENDING_PAYMENT, time);

        if (orders !=null && !orders.isEmpty()){
            for (Orders order : orders) {
                order.setStatus(Orders.CANCELLED);
                order.setCancelReason("订单超时，自动取消");
                order.setCancelTime(LocalDateTime.now());
                orderMapper.update(order);
            }
        }
    }

    /**
     *  处理一直处于派送中的订单
     */
    @Scheduled(cron = "0 0 1 * * ? ")      //每天凌晨一点处理一次
//    @Scheduled(cron = "0/5 * * * * ? ")
    public void deleveryOrder() {
        log.info("定时处理处于派送中的订单:{}", LocalDateTime.now());
        LocalDateTime time = LocalDateTime.now().plusMinutes(-60);
        List<Orders> orderList = orderMapper.getByStatusAndOrderTime(Orders.DELIVERY_IN_PROGRESS, time);
        if (orderList!=null && !orderList.isEmpty()){
            for (Orders order : orderList) {
                order.setStatus(Orders.COMPLETED);
                orderMapper.update(order);
            }
        }
    }
}

package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.entity.Orders;
import org.apache.ibatis.annotations.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Mapper
public interface OrderMapper {

    /**
     * 向订单表插入一条数据
     * @param orders
     */
    void insert(Orders orders);

    /**
     * 根据订单号查询订单
     * @param orderNumber
     */
    @Select("select * from orders where number = #{orderNumber}")
    Orders getByNumber(String orderNumber);

    /**
     * 修改订单信息
     * @param orders
     */
    void update(Orders orders);



    /**

     * 用于替换微信支付更新数据库状态的问题

     * @param orderStatus

     * @param orderPaidStatus

     */
    @Update("update orders set status = #{orderStatus},pay_status = #{orderPaidStatus} ,checkout_time = #{check_out_time} where id = #{id}")
    void updateStatus(Integer orderStatus, Integer orderPaidStatus, LocalDateTime check_out_time, Long id);


    /**
     * 分页查询用户的订单数据，这里的分页查询还带有条件查询的效果，因此传过来的DTO参数里面应该携带我们想要查询的条件信息
     * @param ordersPageQueryDTO
     * @return
     */
    Page<Orders> pagerQuery(OrdersPageQueryDTO ordersPageQueryDTO);

    /**
     * 根据订单id获取订单信息
     * @param orderId
     * @return
     */
    @Select("select * from orders where id = #{orderId}")
    Orders getById(Long orderId);

    /**
     * 统计出于某个状态下的订单状态
     * @return
     */
    @Select("select count(*) from orders where status = #{status}" )
    Integer countStatus(Integer status);


    /**
     * 根据订单状态和时间查询订单
     * @param status
     * @param orderTime
     * @return
     */
    @Select("select * from orders where status = #{status} and order_time < #{orderTime}")
    List<Orders> getByStatusAndOrderTime(Integer status, LocalDateTime orderTime);


    /**
     * 统计指定时间段内某个状态的营业额
     * @param
     * @return
     */
    Double sumByMap(HashMap<String, Object> map);

    @MapKey("order_time")
    Map<LocalDate, Object> sumByDateRange(LocalDateTime startTime,
                                          LocalDateTime endTime,
                                          int status);





}

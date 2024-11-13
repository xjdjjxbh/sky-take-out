package com.sky.service.impl;

import com.sky.constant.MessageConstant;
import com.sky.context.BaseContext;
import com.sky.dto.OrdersSubmitDTO;
import com.sky.entity.AddressBook;
import com.sky.entity.OrderDetail;
import com.sky.entity.Orders;
import com.sky.entity.ShoppingCart;
import com.sky.exception.AddressBookBusinessException;
import com.sky.exception.ShoppingCartBusinessException;
import com.sky.mapper.AddressBookMapper;
import com.sky.mapper.OrderDetailMapper;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.ShoppingCartMapper;
import com.sky.service.OrderService;
import com.sky.vo.OrderSubmitVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderDetailMapper orderDetailMapper;

    @Autowired
    private AddressBookMapper addressBookMapper;

    @Autowired
    private ShoppingCartMapper shoppingCartMapper;

    /**
     * 用户下单
     * @param ordersSubmitDTO
     * @return
     */
    @Override
    @Transactional
    public OrderSubmitVO submitOrder(OrdersSubmitDTO ordersSubmitDTO) {

        //处理各种业务异常（地址簿为空，购物车数据为空）
        AddressBook addressBook = addressBookMapper.getById(ordersSubmitDTO.getAddressBookId());
        if (addressBook == null) {
            throw new AddressBookBusinessException(MessageConstant.ADDRESS_BOOK_IS_NULL);
        }


        Long userId = BaseContext.getCurrentId();
        ShoppingCart shoppingCart = new ShoppingCart();
        shoppingCart.setUserId(userId);
        List<ShoppingCart> shoppingCarts = shoppingCartMapper.list(shoppingCart);
        if (shoppingCarts == null || shoppingCarts.isEmpty()) {
            throw new ShoppingCartBusinessException(MessageConstant.SHOPPING_CART_IS_NULL);
        }

        //向订单表插入一条数据
        Orders orders = new Orders();
        BeanUtils.copyProperties(ordersSubmitDTO, orders);     //下面的数据都没有从前端传递过来，那么我们就只有手动插入了
        orders.setOrderTime(LocalDateTime.now());     //设置下单时间
        orders.setPayStatus(Orders.UN_PAID);        //设置支付状态为未付款
        orders.setStatus(Orders.PENDING_PAYMENT);    //设置订单状态为待付款
        orders.setNumber(String.valueOf(System.currentTimeMillis()));    //设置订单编号
        orders.setPhone(addressBook.getPhone());    //之前判断addressBook是否为空的时候已经拿到了对应的addressBook了
        orders.setConsignee(addressBook.getConsignee());      //设置收货人
        orders.setUserId(userId);                    //设置当前订单是属于哪个用户的
        orderMapper.insert(orders);


        ArrayList<OrderDetail> orderDetailList = new ArrayList<>();
        //向订单明细表插入n条数据(也就是这张订单对应的购物车里面的那些数据)
        for (ShoppingCart cart : shoppingCarts) {
            OrderDetail orderDetail = new OrderDetail();
            BeanUtils.copyProperties(cart, orderDetail);
            orderDetail.setOrderId(orders.getId());
            orderDetailList.add(orderDetail);
        }

        //批量插入订单明细数据
        orderDetailMapper.insertBatch(orderDetailList);

        //清空当前用户的购物车数据
        shoppingCartMapper.deleteByUserId(userId);

        //封装vo返回结果
        return OrderSubmitVO.builder()
                .id(orders.getId())       //设置订单的id
                .orderNumber(orders.getNumber())    //设置对应的订单号
                .orderTime(orders.getOrderTime())//设置对应的订单时间
                .orderAmount(orders.getAmount())  //设置订单的金额
                .build();
    }
}

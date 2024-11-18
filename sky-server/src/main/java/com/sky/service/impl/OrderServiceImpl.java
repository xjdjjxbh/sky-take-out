package com.sky.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.context.BaseContext;
import com.sky.dto.*;
import com.sky.entity.*;
import com.sky.exception.AddressBookBusinessException;
import com.sky.exception.OrderBusinessException;
import com.sky.exception.ShoppingCartBusinessException;
import com.sky.mapper.*;
import com.sky.result.PageResult;
import com.sky.service.OrderService;
import com.sky.utils.WeChatPayUtil;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;
import com.sky.websocket.WebSocketServer;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

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

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private WebSocketServer webSocketServer;

    public static Long orderId;

    /**
     * 用户下单
     *
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
        orders.setAddress(addressBook.getProvinceName()
                + addressBook.getCityName() + addressBook.getDistrictName()
                + addressBook.getDetail());     //设置收货地址
        orders.setUserId(userId);                    //设置当前订单是属于哪个用户的
        orderMapper.insert(orders);


        orderId = orders.getId();


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


    /**
     * 订单支付
     *
     * @param ordersPaymentDTO
     * @return
     */
    public OrderPaymentVO payment(OrdersPaymentDTO ordersPaymentDTO) throws Exception {

        OrderPaymentVO vo = null;

        Integer OrderPaidStatus = Orders.PAID; //支付状态，已支付

        Integer OrderStatus = Orders.TO_BE_CONFIRMED;  //订单状态，待接单

        //发现没有将支付时间 check_out属性赋值，所以在这里更新
        LocalDateTime check_out_time = LocalDateTime.now();

        orderMapper.updateStatus(OrderStatus, OrderPaidStatus, check_out_time, orderId);

        //支付成功之后使用webSocket向客户端浏览器推送消息（type orderId content）  第一个表示是下单还是催单，第三个表示订单号
        HashMap<Object, Object> map = new HashMap<>();
        map.put("type", 1);     //1 下单提醒    2 催单提醒
        map.put("orderId", orderId);
        map.put("content", "订单号：" + ordersPaymentDTO.getOrderNumber());

        //把获取到的信息map转为json字符串
        String jsonString = JSON.toJSONString(map);
        //把带有订单信息的json字符串推送到客户端浏览器
        webSocketServer.sendToAllClient(jsonString);

        return vo;

    }

    /**
     * 支付成功，修改订单状态
     *
     * @param outTradeNo
     */
    public void paySuccess(String outTradeNo) {

        // 根据订单号查询订单
        Orders ordersDB = orderMapper.getByNumber(outTradeNo);

        // 根据订单id更新订单的状态、支付方式、支付状态、结账时间
        Orders orders = Orders.builder()
                .id(ordersDB.getId())
                .status(Orders.TO_BE_CONFIRMED)
                .payStatus(Orders.PAID)
                .checkoutTime(LocalDateTime.now())
                .build();
        orderMapper.update(orders);

    }

    /**
     * 分页查询历史订单数据
     *
     * @return
     */
    @Override
    public PageResult pageQuery4User(int page, int pageSize, Integer status) {
        PageHelper.startPage(page, pageSize);

        OrdersPageQueryDTO ordersPageQueryDTO = new OrdersPageQueryDTO();
        ordersPageQueryDTO.setStatus(status);
        ordersPageQueryDTO.setUserId(BaseContext.getCurrentId());

        //其实这里的查询DTO里面可以传入很多的查询条件，但是在此处，我们只用两个条件进行查询
        Page<Orders> pages = orderMapper.pagerQuery(ordersPageQueryDTO);

        //查出每个订单所对应的订单明细信息，封装到OrderVO里面进行响应，因为这里可能会查询出很多的订单，所以我们用list集合来装他们
//        ArrayList<OrderVO> list = new ArrayList<>();
//
//        if (pages!=null && !pages.isEmpty()){   //如果查出来的订单数据不为空
//            for (Orders orders : pages) {     //遍历用户的每一个订单
//                Long ordersId = orders.getId();    //获得订单Id
//                List<OrderDetail> orderDetails = orderDetailMapper.getByOrderId(ordersId);
//
//                OrderVO orderVO = new OrderVO();      //这个OrderVo是继承Order的，所以它包含Order里面的所有信息，此外，它还添加了菜单的明细信息
//                BeanUtils.copyProperties(orders, orderVO);    //把当前订单的基本信息放到orderVO里面去
//                orderVO.setOrderDetailList(orderDetails);    //把当前订单的详细信息放到VO里面去
//
//                list.add(orderVO);
//            }
//        }
        ArrayList<OrderVO> orderVOList = getOrderVOList(pages);

        return new PageResult(pages.getTotal(), orderVOList);
    }

    /**
     * 根据id查询订单详情
     *
     * @param orderId
     * @return
     */
    @Override
    public OrderVO details(Long orderId) {
        List<OrderDetail> orderDetails = orderDetailMapper.getByOrderId(orderId);
        OrderVO orderVO = new OrderVO();

        Orders orders = orderMapper.getById(orderId);
        BeanUtils.copyProperties(orders, orderVO);
        orderVO.setOrderDetailList(orderDetails);
        return orderVO;
    }

    /**
     * 取消订单
     *
     * @param orderId
     */
    @Override
    public void cancelOrder(Long orderId) {

        //查看订单是否真的存在
        Orders orders = orderMapper.getById(orderId);
        if (orders == null) {
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }

        //外卖已经在路上或者订单已经退款
        if (orders.getStatus() > 2) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }

        //下面是如果订单还在待接单状态
        orders.setStatus(Orders.CANCELLED);
        orders.setPayStatus(Orders.REFUND);
        orders.setCancelReason("用户取消");
        orders.setCancelTime(LocalDateTime.now());
        orderMapper.update(orders);
    }

    /**
     * 再来一单
     *
     * @param orderId
     */
    @Override
    public void repetition(Long orderId) {

        //根据订单id查询订单里面的详细信息
        List<OrderDetail> orderDetails = orderDetailMapper.getByOrderId(orderId);

        //将之前点过的菜品数据变为购物车数据，然后批量放到购物车里面去
//        对流中的每个元素（即 orderDetail）进行映射操作，将其转换为目标类型 ShoppingCart。
        List<ShoppingCart> shoppingCartList = orderDetails.stream().map(orderDetail -> {
            ShoppingCart shoppingCart = new ShoppingCart();
            //不应该把订单详情的id赋值给购物车的id，因为购物车的id应该是要自动生成的，所以在进行属性拷贝的时候，不要拷贝id属性
            BeanUtils.copyProperties(orderDetail, shoppingCart, "id");
            shoppingCart.setCreateTime(LocalDateTime.now());        //给新建的购物车设置创建时间
            //因为订单详情里面没有用户的id这个属性，所以我们要给购物车手动赋值id这个属性
            shoppingCart.setUserId(BaseContext.getCurrentId());     //给新建的的购物车设置用户ID
            return shoppingCart;      // 将每次处理的结果返回给流,供后续操作（如收集）使用
        }).collect(Collectors.toList());
        /*
         * collect() 是 Stream API 的终端操作，用于将流中的元素结果收集到容器（如 List、Set、Map）中。
         * 在流式处理完成后，得到的是一个流对象，而不是一个具体的集合。
         * 如果我们需要一个具体的 List，就必须用 collect(Collectors.toList()) 将流的结果转换为列表。
         */

        //往购物车里面批量插入数据
        shoppingCartMapper.insertBatch(shoppingCartList);
    }

    /**
     * 按条件搜索订单
     *
     * @param ordersPageQueryDTO
     * @return
     */
    @Override
    public PageResult conditionSearch(OrdersPageQueryDTO ordersPageQueryDTO) {
        PageHelper.startPage(ordersPageQueryDTO.getPage(), ordersPageQueryDTO.getPageSize());

        Page<Orders> page = orderMapper.pagerQuery(ordersPageQueryDTO);

        //把查询到的对象封装成orderVO
        ArrayList<OrderVO> orderVOS = getOrderVOList(page);

        return new PageResult(page.getTotal(), orderVOS);

    }

    /**
     * 统计订单状态
     *
     * @return
     */
    @Override
    public OrderStatisticsVO statistics() {
        Integer deliveryInProgress = orderMapper.countStatus(Orders.DELIVERY_IN_PROGRESS);  //派送中
        Integer toBeConfirmed = orderMapper.countStatus(Orders.TO_BE_CONFIRMED);            //待接单
        Integer confirmed = orderMapper.countStatus(Orders.CONFIRMED);                      //待派送
        OrderStatisticsVO orderStatisticsVO = new OrderStatisticsVO();
        orderStatisticsVO.setDeliveryInProgress(deliveryInProgress);
        orderStatisticsVO.setToBeConfirmed(toBeConfirmed);
        orderStatisticsVO.setConfirmed(confirmed);
        return orderStatisticsVO;
    }

    /**
     * 商家接单
     */
    @Override
    public void confirm(OrdersConfirmDTO ordersConfirmDTO) {
        Orders orders = Orders.builder()
                .id(ordersConfirmDTO.getId())
                .status(Orders.CONFIRMED)
                .build();
        orderMapper.update(orders);
    }

    /**
     * 商家拒单
     *
     * @param ordersRejectionDTO
     */
    @Override
    public void rejection(OrdersRejectionDTO ordersRejectionDTO) {
        Orders rejectOrder = orderMapper.getById(ordersRejectionDTO.getId());
        if (rejectOrder == null || rejectOrder.getStatus() >= Orders.DELIVERY_IN_PROGRESS) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }

        Orders orders = Orders.builder()
                .id(ordersRejectionDTO.getId())
                .status(Orders.CANCELLED)
                .rejectionReason(ordersRejectionDTO.getRejectionReason())
                .cancelTime(LocalDateTime.now())
                .build();
        orderMapper.update(orders);
    }

    /**
     * 派送订单
     *
     * @param id
     */
    @Override
    public void delivery(Long id) {
        Orders orders = orderMapper.getById(id);
        if (orders != null && orders.getStatus() == Orders.CONFIRMED) {
            Orders orderToDelivery = Orders.builder()
                    .id(id)
                    .status(Orders.DELIVERY_IN_PROGRESS)
                    .build();
            orderMapper.update(orderToDelivery);
        } else {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }
    }

    /**
     * 完成订单
     *
     * @param id
     */
    @Override
    public void complete(Long id) {
        Orders orders = orderMapper.getById(id);
        if (orders != null && orders.getStatus() != Orders.DELIVERY_IN_PROGRESS) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        } else {
            Orders orderCompleted = Orders.builder()
                    .id(id)
                    .status(Orders.COMPLETED)
                    .deliveryTime(LocalDateTime.now())
                    .build();
            orderMapper.update(orderCompleted);
        }
    }

    /**
     * 用户催单
     *
     * @param id
     */
    @Override
    public void reminder(Long id) {
        Orders orders = orderMapper.getById(id);
        if (orders == null) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }

        HashMap<Object, Object> map = new HashMap<>();
        map.put("type", 2);    //2表示用户催单
        map.put("orderId", id);
        map.put("content", "订单号：" + orders.getNumber());
        String jsonString = JSON.toJSONString(map);
        webSocketServer.sendToAllClient(jsonString);
    }

    //因为·把查询到的对象封装成VO这种情况用的很多，所以这里单独提取出一个方法来
    private ArrayList<OrderVO> getOrderVOList(Page<Orders> pages) {
        ArrayList<OrderVO> orderVOS = new ArrayList<>();
        if (pages != null && !pages.isEmpty()) {   //如果查出来的订单数据不为空
            for (Orders orders : pages) {     //遍历用户的每一个订单
                Long ordersId = orders.getId();    //获得订单Id
                List<OrderDetail> orderDetails = orderDetailMapper.getByOrderId(ordersId);

                OrderVO orderVO = new OrderVO();      //这个OrderVo是继承Order的，所以它包含Order里面的所有信息，此外，它还添加了菜单的明细信息
                BeanUtils.copyProperties(orders, orderVO);    //把当前订单的基本信息放到orderVO里面去
                orderVO.setOrderDetailList(orderDetails);    //把当前订单的详细信息放到VO里面去
                String orderDishesStr = getOrderDishesStr(orders);    //获取到当前order里面的所有菜品，然后拼接成一个字符串
                orderVO.setOrderDishes(orderDishesStr);
                orderVOS.add(orderVO);
            }
        }
        return orderVOS;
    }


    /**
     * 根据订单id获取所有菜品信息组成的字符串
     *
     * @param orders
     * @return
     */
    private String getOrderDishesStr(Orders orders) {
        List<OrderDetail> orderDetails = orderDetailMapper.getByOrderId(orders.getId());

        //将订单的每个菜品信息映射成一个字符串，格式是宫保鸡丁*3；
        List<String> dishes = orderDetails.stream().map(orderDetail -> {
            // 将每一条订单菜品信息拼接为字符串（格式：宫保鸡丁*3；）
            return orderDetail.getName() + "*" + orderDetail.getNumber() + ";";
        }).collect(Collectors.toList());

        //使用字符串的方式将list中的所有数据连接起来，他们之间不需要任何分隔，因为在之前的时候就已经加了;作为分隔了
        return String.join("", dishes);
    }

}

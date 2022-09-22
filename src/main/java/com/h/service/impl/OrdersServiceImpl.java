package com.h.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.h.common.BaseContext;
import com.h.common.Code;
import com.h.common.CustomException;
import com.h.common.R;
import com.h.dto.OrderDto;
import com.h.dto.PickOrder;
import com.h.entity.*;
import com.h.mapper.OrdersMapper;
import com.h.service.*;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * <p>
 * 订单表 服务实现类
 * </p>
 *
 * @author h
 * @since 2022-08-21
 */
@Service
public class OrdersServiceImpl extends ServiceImpl<OrdersMapper, Orders> implements IOrdersService {

    @Autowired
    private IShoppingCartService shoppingCartService;
    @Autowired
    private IUserService userService;
    @Autowired
    private IAddressBookService addressBookService;
    @Autowired
    private IOrderDetailService orderDetailService;

    /**
     * 用户下单处理
     * 取用户id对应购物车数据，遍历提，取订单详情数据，插入order_detail表
     * 构建一条订单表数据
     * @param order
     */
    @Transactional
    public void submit(Orders order) {

        //取用户id
        Long userId = BaseContext.getCurrentId();
        //购物车数据
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId, userId);
        List<ShoppingCart> shoppingCarts = shoppingCartService.list(queryWrapper);

        if (shoppingCarts == null || shoppingCarts.size() == 0) {
            throw new CustomException(Code.GET_ONE_ERR, "购物车为空，不能下单");
        }

        //查询用户数据
        User user = userService.getById(userId);
        System.out.println(user);

        //查询地址数据
        Long addressBookId = order.getAddressBookId();
        AddressBook addressBook = addressBookService.getById(addressBookId);
        if (addressBook == null) {
            throw new CustomException(Code.GET_ONE_ERR, "用户地址信息有误，不能下单");
        }

        long orderId = IdWorker.getId();//订单号

        AtomicInteger amount = new AtomicInteger(0);

        //提取订单详情数据
        List<OrderDetail> orderDetails = shoppingCarts.stream().map((item) -> {
            OrderDetail orderDetail = new OrderDetail();
            orderDetail.setOrderId(orderId);
            orderDetail.setNumber(item.getNumber());
            orderDetail.setDishFlavor(item.getDishFlavor());
            orderDetail.setDishId(item.getDishId());
            orderDetail.setSetmealId(item.getSetmealId());
            orderDetail.setName(item.getName());
            orderDetail.setImage(item.getImage());
            orderDetail.setAmount(item.getAmount());
            amount.addAndGet(item.getAmount().multiply(new BigDecimal(item.getNumber())).intValue());
            return orderDetail;
        }).collect(Collectors.toList());

        //构建订单数据
        order.setId(orderId);
        order.setOrderTime(LocalDateTime.now());
        order.setCheckoutTime(LocalDateTime.now());
        order.setStatus(2);
        order.setAmount(new BigDecimal(amount.get()));//总金额
        order.setUserId(userId);
        order.setNumber(String.valueOf(orderId));
        order.setUserName(user.getName());
        order.setConsignee(addressBook.getConsignee());
        order.setPhone(addressBook.getPhone());
        order.setAddress((addressBook.getProvinceName() == null ? "" : addressBook.getProvinceName())
                + (addressBook.getCityName() == null ? "" : addressBook.getCityName())
                + (addressBook.getDistrictName() == null ? "" : addressBook.getDistrictName())
                + (addressBook.getDetail() == null ? "" : addressBook.getDetail()));
        //向订单表插入数据，一条数据
        this.save(order);

        //向订单明细表插入数据，多条数据
        orderDetailService.saveBatch(orderDetails);

        //清空购物车数据
        shoppingCartService.remove(queryWrapper);
    }


    public Page<OrderDto> userPage(Integer page, Integer pageSize) {

        Long userId = BaseContext.getCurrentId();

        LambdaQueryWrapper<Orders> ordersQueryWrapper = new LambdaQueryWrapper<>();
        ordersQueryWrapper.eq(Orders::getUserId, userId)
        .orderByDesc(Orders::getOrderTime)//从最近订单开始展示
        .last("limit " + page + "" + pageSize);
        List<Orders> ordersList = this.list(ordersQueryWrapper);

        List orderDtos = new ArrayList<OrderDto>();

        for (Orders order : ordersList) {
            OrderDto orderDto = new OrderDto();//调试，找不到局部变量'orderDto'?
            BigDecimal amount = BigDecimal.ZERO;
            Integer sumNum = 0;
            LambdaQueryWrapper<OrderDetail> detailQueryWrapper = new LambdaQueryWrapper<>();
            detailQueryWrapper.select().eq(OrderDetail::getOrderId, order.getId());
            List<OrderDetail> orderDetails = orderDetailService.list(detailQueryWrapper);
            orderDto.setOrderDetails(orderDetails);
            orderDto.setOrderTime(order.getOrderTime());
            orderDto.setStatus(order.getStatus());

            for (OrderDetail orderDetail : orderDetails) {
                // BigDecimal累加，要使用 total = total .add(number);这种形式
                //使用这种  total += number；  total还会是初始值
                amount = amount.add(orderDetail.getAmount().multiply(new BigDecimal(orderDetail.getNumber())));
                sumNum += orderDetail.getNumber();
            }
            orderDto.setAmount(amount);
            orderDto.setSumNum(sumNum);
            orderDtos.add(orderDto);
        }

        Page<OrderDto> orderDtoPage = new Page<>();
        orderDtoPage.setRecords(orderDtos);
        return orderDtoPage;
    }

    /**
     * 商家后台订单明显页面
     * @param pick 订单筛选条件和分页信息
     * @return
     */
    public Page<Orders> page(PickOrder pick){
        //筛选条件，订单号、下单时间
        LambdaQueryWrapper<Orders> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(pick.getNumber() != null, Orders::getId, pick.getNumber()).
                between(pick.getBeginTime() != null && pick.getEndTime() != null,
                        Orders::getOrderTime, pick.getBeginTime(), pick.getEndTime());
        //分页信息
        Page<Orders> orderPage = new Page<>(pick.getPage(), pick.getPageSize());
        this.page(orderPage, queryWrapper);

        return  orderPage;
    }

    /**
     * 订单状态更新
     * @param order 待付款 派送 完成 取消
     * @return
     */
    public void updateStatus(Orders order) {
        LambdaQueryWrapper<Orders> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(order.getId() != null, Orders::getId, order.getId());
        this.update(order, queryWrapper);
    }
}

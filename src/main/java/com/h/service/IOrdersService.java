package com.h.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.h.common.R;
import com.h.dto.OrderDto;
import com.h.dto.PickOrder;
import com.h.entity.Orders;
import com.baomidou.mybatisplus.extension.service.IService;

import java.time.LocalDateTime;

/**
 * <p>
 * 订单表 服务类
 * </p>
 *
 * @author h
 * @since 2022-08-21
 */
public interface IOrdersService extends IService<Orders> {
    /**
     * 用户下单
     * @param order
     */
    void submit(Orders order);


    /**
     * 用户订单信息
     * @param page
     * @param pageSize
     * @return
     */
    Page<OrderDto> userPage(Integer page, Integer pageSize);

    /**
     * 商家后台订单明显页面
     * @param pick 订单筛选条件和分页信息
     * @return
     */
    Page<Orders> page(PickOrder pick);

    /**
     * 订单状态更新
     * @param order 待付款 派送 完成 取消
     * @return
     */
    void updateStatus(Orders order);
}

package com.h.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.h.common.Code;
import com.h.common.R;
import com.h.dto.OrderDto;
import com.h.dto.PickOrder;
import com.h.entity.Orders;
import com.h.service.IOrdersService;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.expression.DateTimeLiteralExpression;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * <p>
 * 订单表 前端控制器
 * </p>
 *
 * @author h
 * @since 2022-08-21
 */
@RestController
@RequestMapping("/order")
@Slf4j
public class OrdersController {

    @Autowired
    private IOrdersService ordersService;

    /**
     * 用户下单处理
     * @param order
     * @return
     */
    @PostMapping("/submit")
    public R<String> submit(@RequestBody Orders order){
        log.info("订单提交内容：" , order.toString());
        ordersService.submit(order);
        return R.success(Code.ADD_OK, "下单成功");
    }

    /**
     * 用户订单
     * @param page
     * @param pageSize
     * @return
     */
    @GetMapping("/userPage")
    public R<Page<OrderDto>> userPage(Integer page, Integer pageSize){
        Page<OrderDto> orderDtoPage = ordersService.userPage(page, pageSize);
        return R.success(Code.LOG_OK, orderDtoPage);
    }

    /**
     * 商家后台订单明显页面
     * @param pick 订单筛选条件和分页信息
     * @return
     */
    @GetMapping("/page")
    public R<Page<Orders>> page(PickOrder pick){
        Page<Orders> orderPage = ordersService.page(pick);
        return R.success(Code.GET_PAGE_OK, orderPage);
    }

    /**
     * 订单状态更新
     * @param order 待付款 派送 完成 取消
     * @return
     */
    @PutMapping
    public R<String> updateStatus(@RequestBody Orders order){
        log.info("订单状态更新：", order.toString());
        ordersService.updateStatus(order);
        return R.success(Code.UPDATE_OK, "订单状态更新成功");
    }
}


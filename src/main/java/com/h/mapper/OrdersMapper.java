package com.h.mapper;

import com.h.entity.Orders;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 订单表 Mapper 接口
 * </p>
 *
 * @author h
 * @since 2022-08-21
 */
@Mapper
public interface OrdersMapper extends BaseMapper<Orders> {

}

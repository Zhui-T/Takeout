package com.h.dto;

import com.h.entity.OrderDetail;
import com.h.entity.Orders;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class OrderDto {
    private LocalDateTime orderTime;
    private Integer status;
    private List<OrderDetail> orderDetails;
    private BigDecimal amount;
    private Integer sumNum;
}

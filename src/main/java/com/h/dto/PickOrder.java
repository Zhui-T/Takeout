package com.h.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Data
public class PickOrder {
   private Integer page;
   private Integer pageSize;
   private Long number;
   @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")//前端传递进来的日期时间参数格式
   //@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")//后端对外输出的日期时间格式
   private LocalDateTime beginTime;
   @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
   private java.time.LocalDateTime endTime;
}

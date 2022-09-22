package com.h.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.sql.SQLIntegrityConstraintViolationException;
/*
    @ControllerAdvice(annotations = {RestController.class, Controller.class})
    @ResponseBody
* */
//@RestControllerAdvice
//@Slf4j
public class GlobalExceptionHandler {

    /*@ExceptionHandler(SQLIntegrityConstraintViolationException.class)
    public R<String> exceptionHandler(SQLIntegrityConstraintViolationException e){
        log.error(e.getMessage());

        if(e.getMessage().contains("Duplicate entry")){
            String[] split = e.getMessage().split(" ");
            String msg = split[2] + "已存在";
            return R.error(Code.ADD_ERR,msg);
        }

        return R.error(Code.ADD_ERR,"未知错误");
    }


    *//**
     * 全局通用自定义异常处理
     * @param e
     * @return
     *//*
    @ExceptionHandler(CustomException.class)
    public R<String> customExceptionHandler(CustomException e){
        log.error(e.getMessage());
        return R.error(Code.SYSTEM_ERR, e.getMessage());
    }

    *//**
     * 其他异常处理
     * @param e
     * @return
     *//*
    @ExceptionHandler(Exception.class)
    public R<String> ExceptionHandler(Exception e){
        log.error(e.getMessage());
        return R.error(Code.SYSTEM_ERR, e.getMessage());
    }*/
}

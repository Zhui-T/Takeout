package com.h.common;

import lombok.Data;

/*
* Result的Code字段状态码值
* */
@Data
public class Code {
    //查询相关状态码
    //成功
    public static final Integer ADD_OK = 1;
    public static final Integer DELETE_OK = 1;
    public static final Integer UPDATE_OK = 1;
    public static final Integer GET_ONE_OK = 1;
    public static final Integer GET_PAGE_OK = 1;
    public static final Integer GET_ALL_OK = 1;
    //失败
    public static final Integer ADD_ERR = 0;
    public static final Integer DELETE_ERR = 0;
    public static final Integer UPDATE_ERR = 0;
    public static final Integer GET_ONE_ERR = 0;
    public static final Integer GET_PAGE_ERR = 0;
    public static final Integer GET_ALL_ERR = 0;


    //启用禁用状态码
    public static final Integer ENABLE = 1;
    public static final Integer DISABLE = 0;

    //登录登出，成功失败状态码
    public static final Integer LOG_OK = 1;
    public static final Integer LOG_ERR = 0;

    //上传下载状态码
    public static final Integer UPLOAD_OK = 1;
    public static final Integer UPLOAD_ERR = 0;
    public static final Integer DOWNLOAD_OK = 1;
    public static final Integer DOWNLOAD_ERR = 0;

    //系统错误状态码
    public static final Integer SYSTEM_ERR = 0;
    public static final Integer SYSTEM_TIMEOUT_ERR = 0;
    public static final Integer SYSTEM_UNKNOW_ERR = 0;

    public static final Integer BUSINESS_ERR = 0;
/*    //查询相关状态码
    //成功
    public static final Integer ADD_OK = 20011;
    public static final Integer DELETE_OK = 20021;
    public static final Integer UPDATE_OK = 20031;
    public static final Integer GET_ONE_OK = 20041;
    public static final Integer GET_PAGE_OK = 20051;
    public static final Integer GET_ALL_OK = 20061;
    //失败
    public static final Integer ADD_ERR = 20010;
    public static final Integer DELETE_ERR = 20020;
    public static final Integer UPDATE_ERR = 20030;
    public static final Integer GET_ONE_ERR = 20040;
    public static final Integer GET_PAGE_ERR = 20050;
    public static final Integer GET_ALL_ERR = 20060;


    //启用禁用状态码
    public static final Integer ENABLE = 20071;
    public static final Integer DISABLE = 20070;

    //登录登出，成功失败状态码
    public static final Integer LOG_OK = 20081;
    public static final Integer LOG_ERR = 20080;

    //系统错误状态码
    public static final Integer SYSTEM_ERR = 50001;
    public static final Integer SYSTEM_TIMEOUT_ERR = 50002;
    public static final Integer SYSTEM_UNKNOW_ERR = 59999;

    public static final Integer BUSINESS_ERR = 60002;*/
}

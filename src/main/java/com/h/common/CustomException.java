package com.h.common;

/**
 * 自定义异常类
 */
public class CustomException extends RuntimeException{
    private Integer code;//状态码

    public Integer getCode() {
        return code;
    }
    public void setCode(Integer code) {
        this.code = code;
    }

    public CustomException(Integer code, String message ) {
        super(message);
        this.code = code;
    }

    public CustomException(Integer code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }
}

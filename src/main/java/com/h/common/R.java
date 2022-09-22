package com.h.common;

import lombok.Data;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * 通用返回结果，服务端响应的数据最终都会封装成此对象
 * @param <T>
 *    用泛型类，主要就是动态匹配传入data的类型
 */
@Data
public class R<T> implements Serializable {

    private Integer code;//状态码，Code类中定义的常量

    private String msg;//错误信息

    private T data;//数据;用泛型类，主要就是动态匹配传入data的类型

    private Map map = new HashMap();//动态数据


    //泛型类静态构造方法&泛型方法，注意,参数类型T&返回值类型<T> R<T>
    //静态方法，直接通过类名R调用构造方法，创建新对象

    /**
     *成功返回结果的构造方法
     * @param code
     * @param data
     * @param <T>
     * @return
     */
    public static <T> R<T> success(Integer code, T data){
        R<T> result = new R<>();
        result.code = code;
        result.data = data;
        return result;
    }

    /**
     * 失败返回结果的构造方法
     * @param code
     * @param msg
     * @param <T>
     * @return
     */
    public static <T> R<T> error(Integer code, String msg){
        R<T> result = new R<>();
        result.code = code;
        result.msg = msg;
        return result;
    }

    /**
     * 动态数据的添加方法
     * @param key
     * @param value
     * @return
     */
    public R<T> add(String key, Object value) {
        this.map.put(key, value);
        return this;//返回当前对象可以链式添加动态数据
    }
}

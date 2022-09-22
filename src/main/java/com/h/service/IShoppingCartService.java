package com.h.service;

import com.h.entity.ShoppingCart;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 购物车 服务类
 * </p>
 *
 * @author h
 * @since 2022-08-21
 */
public interface IShoppingCartService extends IService<ShoppingCart> {
    /**
     * 添加购物车，当前用户添加的对应菜品/套餐，无则新建，有则加一
     * @param shoppingCart
     */
    void add(ShoppingCart shoppingCart);
    /**
     * 删减购物车，当前用户去除的对应菜品/套餐，大于1则减1，为1则删除
     * @param shoppingCart
     */
    void sub(ShoppingCart shoppingCart);

}

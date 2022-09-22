package com.h.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.h.common.BaseContext;
import com.h.entity.ShoppingCart;
import com.h.mapper.ShoppingCartMapper;
import com.h.service.IShoppingCartService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * <p>
 * 购物车 服务实现类
 * </p>
 *
 * @author h
 * @since 2022-08-21
 */
@Service
public class ShoppingCartServiceImpl extends ServiceImpl<ShoppingCartMapper, ShoppingCart> implements IShoppingCartService {

    /**
     * 添加购物车，当前用户添加的对应菜品/套餐，无则新建，有则加一
     * @param shoppingCart
     */
    public void add(ShoppingCart shoppingCart) {
        //获取当前用户id，指定当前是哪个用户的购物车数据
        Long currentUserId = BaseContext.getCurrentId();
        shoppingCart.setUserId(currentUserId);

        Long dishId = shoppingCart.getDishId();

        //查询条件，ShoppingCart中指定用户id
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId,currentUserId);

        //查询条件，指定菜品或套餐
        if(dishId != null){
            //添加到购物车的是菜品
            queryWrapper.eq(ShoppingCart::getDishId,dishId);

        }else{
            //添加到购物车的是套餐
            queryWrapper.eq(ShoppingCart::getSetmealId,shoppingCart.getSetmealId());
        }

        //查询当前菜品或者套餐是否在购物车中
        ShoppingCart aShoppingCart = this.getOne(queryWrapper);

        if(aShoppingCart != null){
            //如果已经存在，就在原来数量基础上加一
            Integer number = aShoppingCart.getNumber();
            aShoppingCart.setNumber(number + 1);
            this.updateById(aShoppingCart);
        }else{
            //如果不存在，则添加到购物车，数量默认就是一
            shoppingCart.setNumber(1);
            //未使用通用字段自动填充
            shoppingCart.setCreateTime(LocalDateTime.now());
            this.save(shoppingCart);
            aShoppingCart = shoppingCart;
        }
    }

    /**
     * 删减购物车，当前用户去除的对应菜品/套餐，大于1则减1，为1则删除
     * @param shoppingCart
     */
    public void sub(ShoppingCart shoppingCart) {

        //查询当前菜品或套餐是否在购物车表中，以及具体数量
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        if (shoppingCart.getDishId() != null){//不直接用wrapper的非空判断，更严谨，避免干扰请求：既有dish又有setmeal
            queryWrapper.eq(ShoppingCart::getDishId, shoppingCart.getDishId());
        }else if (shoppingCart.getSetmealId() != null){
            queryWrapper.eq(ShoppingCart::getSetmealId, shoppingCart.getSetmealId());
        }else {
            return;
        }

        //获取当前用户id，指定当前是哪个用户的购物车数据
        Long currentUserId = BaseContext.getCurrentId();
        queryWrapper.eq(currentUserId != null, ShoppingCart::getUserId, currentUserId);
        ShoppingCart aShoppingCart = this.getOne(queryWrapper);

        Integer number = aShoppingCart.getNumber();

        //如果数量大于1则减一
        if(number > 1){
            aShoppingCart.setNumber(number - 1);//number--,用了再减少，没用了
            //debug明明减少了，但数据没库没变化？？？
            //只是方法里创建的购物车对象数据改变，并未将数据同步到数据库
            this.updateById(aShoppingCart);
        }else {
            //数量只有一个就直接从表中删除
            //通过Id删除，要的是主键，，，
            this.removeById(aShoppingCart.getId());
        }
    }
}

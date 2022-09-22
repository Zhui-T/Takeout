package com.h.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.h.common.Code;
import com.h.common.CustomException;
import com.h.entity.Category;
import com.h.entity.Dish;
import com.h.entity.Setmeal;
import com.h.mapper.CategoryMapper;
import com.h.service.ICategoryService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.h.service.IDishService;
import com.h.service.ISetmealService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 菜品及套餐分类 服务实现类
 * </p>
 *
 * @author h
 * @since 2022-08-21
 */
@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements ICategoryService {

    @Autowired
    private IDishService dishService;
    @Autowired
    private ISetmealService setmealService;

    /**
     * 判断当前分类是否可删除
     * @param id
     */
    public void removeById(Long id){
        //查询菜品表中与此分类关联的菜品数量
        LambdaQueryWrapper<Dish> dishQueryWrapper = new LambdaQueryWrapper<>();
        dishQueryWrapper.eq(Dish::getCategoryId,id);
        int count1 = dishService.count();
        //有与当前分类关联的菜品
        if (count1 > 0){
            //关联了菜品，不可删除；抛出自定义异常
            new CustomException(Code.DELETE_ERR,"当前分类下关联了菜品，不可删除");
        }

        //查询套餐表中与此分类关联的套餐数量
        LambdaQueryWrapper<Setmeal> setmealQueryWrapper = new LambdaQueryWrapper<>();
        setmealQueryWrapper.eq(Setmeal::getCategoryId,id);
        int count2 = setmealService.count();
        //有当前分类关联的的套餐
        if (count2 > 0){
            //关联了套餐，不可删除；抛出自定义异常
            new CustomException(Code.DELETE_ERR,"当前分类下关联了套餐，不可删除");
        }

        //未关联以上内容，正常删除
        super.removeById(id);
    }
}

package com.h.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.h.dto.SetmealDto;
import com.h.entity.Setmeal;
import com.h.entity.SetmealDish;
import com.h.mapper.SetmealMapper;
import com.h.service.ISetmealDishService;
import com.h.service.ISetmealService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 套餐 服务实现类
 * </p>
 *
 * @author h
 * @since 2022-08-21
 */
@Component
public class SetmealServiceImpl extends ServiceImpl<SetmealMapper, Setmeal> implements ISetmealService {

    @Autowired
    private ISetmealDishService setmealDishService;

    /**
     * 根据setmealId查询setmeal表对应套餐信息和setmeal_dish表中对应套餐的菜品信息
     *
     * @param setmealId
     * @return
     */
    public SetmealDto getOneWithDish(Long setmealId) {
        //查询套餐信息
        Setmeal setmeal = this.getById(setmealId);

        //Spring拷贝Setmeal Bean属性 到子类 Setmeal Dto
        SetmealDto setmealDto = new SetmealDto();
        BeanUtils.copyProperties(setmeal, setmealDto);

        //查询套餐管理的菜品数据
        LambdaQueryWrapper<SetmealDish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SetmealDish::getSetmealId, setmealId);
        List<SetmealDish> dishes = setmealDishService.list(queryWrapper);

        //赋值菜品信息到SetmealDto
        setmealDto.setSetmealDishes(dishes);

        return setmealDto;
    }


    /**
     * 保存套餐信息，setmeal表和setmeal_dish表中对应套餐的菜品信息
     *
     * @param setmealDto
     */
    @Transactional
    public void saveWithDish(SetmealDto setmealDto) {
        //保存setmeal表套餐信息
        this.save(setmealDto);

        /*
        保存套餐与菜品关系信息到setmeal_dish表
         */
        //在套餐菜品关系表的行数据内填写套餐id字段信息
        List<SetmealDish> dishes = setmealDto.getSetmealDishes();
        dishes = dishes.stream().map(dish -> {
            dish.setSetmealId(String.valueOf(setmealDto.getId()));
            return dish;
        }).collect(Collectors.toList());
        //插入数据，setmeal_dish表
        setmealDishService.saveBatch(dishes);
    }

    /**
     * 修改套餐，套餐表及对应套餐的套餐菜品关系表
     *
     * @param setmealDto
     */
    @Transactional
    public void updateWithDish(SetmealDto setmealDto) {
        //修改套餐表信息
        this.updateById(setmealDto);

        /*
        修改套餐对应的菜品信息，删除重建？？？改动很小时，代太大
        */
        //删除,与套餐关联的菜品
        LambdaQueryWrapper<SetmealDish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SetmealDish::getDishId, setmealDto.getId());
        setmealDishService.remove(queryWrapper);
        //插入
        //每个菜品套餐字段填充,流操作
        List<SetmealDish> dishes = setmealDto.getSetmealDishes();
        dishes = dishes.stream().map(dish -> {
            dish.setSetmealId(String.valueOf(setmealDto.getId()));
            return dish;
        }).collect(Collectors.toList());
        setmealDishService.saveBatch(dishes);
    }

    /**
     * 批量修改套餐销售状态
     * @param status
     * @param ids
     */
    public void changStatus(int status, List<Long> ids) {
        //设置修改值
        Setmeal setmeal = new Setmeal();
        setmeal.setStatus(status);

        //用 in SQL语句批量修改
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(Setmeal::getId, ids);
        this.update(setmeal, queryWrapper);
    }

    /**
     * 批量删除套餐，套餐表及对应套餐的套餐菜品关系表
     * @param ids
     */
    @Transactional
    public void removeWithDish(List<Long> ids) {
        //批量删除套餐表数据
        LambdaQueryWrapper<Setmeal> setmealQueryWrapper = new LambdaQueryWrapper<>();
        setmealQueryWrapper.in(Setmeal::getId, ids);
        this.remove(setmealQueryWrapper);

        //删除每个套餐对应菜品数据
        ids.forEach(id -> {
            LambdaQueryWrapper<SetmealDish> dishQueryWrapper = new LambdaQueryWrapper<>();
            dishQueryWrapper.eq(SetmealDish::getSetmealId, id);
            setmealDishService.remove(dishQueryWrapper);
        });
    }
}

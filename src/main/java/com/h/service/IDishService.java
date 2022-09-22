package com.h.service;

import com.h.dto.DishDto;
import com.h.entity.Dish;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * 菜品管理 服务类
 * </p>
 *
 * @author h
 * @since 2022-08-21
 */
public interface IDishService extends IService<Dish> {

    //新增菜品，同时插入菜品对应的口味数据，需要操作两张表：dish、dish_flavor
    void saveWithFlavor(DishDto dishDto);

    //根据id查询菜品信息和对应的口味信息
    DishDto getByIdWithFlavor(Long id);

    //获取某个菜系的使所有菜品及其口味信息
    List<DishDto> getDishesWithFlavor(Dish dish);

    //更新菜品信息，同时更新对应的口味信息
    void updateWithFlavor(DishDto dishDto);

    //批量删除菜品及其对应口味信息
    void delete(List ids);

    //批量修改菜品销售状态
    void changeStatus(int status, List<Long> ids);

    //批量获取某菜系的全部菜品信息
    List<Dish> getDishes(Long categoryId);
}

package com.h.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.h.common.R;
import com.h.controller.EmployeeController;
import com.h.dto.DishDto;
import com.h.entity.Category;
import com.h.entity.Dish;
import com.h.entity.DishFlavor;
import com.h.mapper.DishMapper;
import com.h.service.ICategoryService;
import com.h.service.IDishFlavorService;
import com.h.service.IDishService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 菜品管理 服务实现类
 * </p>
 *
 * @author h
 * @since 2022-08-21
 */
@Service
public class DishServiceImpl extends ServiceImpl<DishMapper, Dish> implements IDishService {

    @Autowired
    private IDishFlavorService dishFlavorService;
    /*
    //和dishService循环装配了
    @Autowired
    private ICategoryService categoryService;*/


    /**
     * //新增菜品，同时插入菜品对应的口味数据，需要操作两张表：dish、dish_flavor
     * @param dishDto
     */
    @Transactional  //多表数据改写操作，开启事务
    public void saveWithFlavor(DishDto dishDto) {
        //保存菜品数据到dish表
        this.save(dishDto);

        //对每个flavor元素绑定DishId
        long dishId = dishDto.getId();
        //口味信息可能是多种
        List<DishFlavor> flavors = dishDto.getFlavors();
        flavors = flavors.stream().map(flavor -> {
            flavor.setDishId(dishId); //用流（stream）的map()方法对每个flavor元素绑定DishId
            return flavor;
        }).collect(Collectors.toList());

        //保存此菜品的对应口味信息到dish_flavor表
        dishFlavorService.saveBatch(flavors);
    }

    /**
     * //根据id查询菜品信息和对应的口味信息，dish、dish_flavor表
     * @param id
     * @return
     */
    public DishDto getByIdWithFlavor(Long id) {
        //查询菜品表信息，dish表
        Dish dish = this.getById(id);

        DishDto dishDto = new DishDto();
        //Spring的对象拷贝，将Dish Bean 数据拷贝到子类DishDto Bean
        BeanUtils.copyProperties(dish,dishDto);

        //查询与菜品id对应的菜品口味信息，dish_flavor表
        LambdaQueryWrapper<DishFlavor> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DishFlavor::getDishId, id);
        //集合形式接收多个查询数据
        List<DishFlavor> dishFlavors = dishFlavorService.list(queryWrapper);

        //将口味信息赋给dishDto
        dishDto.setFlavors(dishFlavors);

        return dishDto;
    }

    /**
     * user端菜品信息展示，对用菜系的菜品和口味信息
     * @param dish
     * @return
     */
    public List<DishDto> getDishesWithFlavor(Dish dish) {
        //构造查询条件
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(dish.getCategoryId() != null ,Dish::getCategoryId,dish.getCategoryId());
        //添加条件，查询状态为1（起售状态）的菜品
        queryWrapper.eq(Dish::getStatus,1);

        //添加排序条件
        queryWrapper.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);

        List<Dish> list = this.list(queryWrapper);

        List<DishDto> dishDtoList = list.stream().map((item) -> {
            DishDto dishDto = new DishDto();

            BeanUtils.copyProperties(item,dishDto);

            /*
            循环装配了，可以选择仍到controller
                ┌─────┐
                |  categoryService
                ↑     ↓
                |  dishService
                └─────┘
            Long categoryId = item.getCategoryId();//分类id
            //根据id查询分类对象
            Category category = categoryService.getById(categoryId);

            if(category != null){
                String categoryName = category.getName();
                dishDto.setCategoryName(categoryName);
            }*/

            //当前菜品的id
            Long dishId = item.getId();
            LambdaQueryWrapper<DishFlavor> lambdaQueryWrapper = new LambdaQueryWrapper<>();
            lambdaQueryWrapper.eq(DishFlavor::getDishId,dishId);
            //查询当前菜品的口味信息
            List<DishFlavor> dishFlavorList = dishFlavorService.list(lambdaQueryWrapper);
            dishDto.setFlavors(dishFlavorList);
            return dishDto;
        }).collect(Collectors.toList());

        return dishDtoList;
    }

    /**
     * //更新菜品信息，同时更新对应的口味信息，dish、dish_flavor表
     * 口味信息条数可能又变，修改不可完成
     * 需删除后插入
     * @param dishDto
     */
    @Transactional  //多表改写操作，开启事务
    public void updateWithFlavor(DishDto dishDto) {

        //dish表对应数据更新
        this.updateById(dishDto);

        //dish_flavor表数据，直接删除再插入新的？ 可能有数据条数变更，修改无法完成
        //如果修改的时候增加了或减少不同种类的口味，就没有办法修改了
        LambdaQueryWrapper<DishFlavor> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DishFlavor::getDishId,dishDto.getId());
        //删除与此dishId关联的数据
        dishFlavorService.remove(queryWrapper);

        //取全部口味信息，集合形式；并用stream分别加入dishId字段信息
        List<DishFlavor> flavors = dishDto.getFlavors();
        flavors = flavors.stream().map(flavor ->{
            flavor.setDishId(dishDto.getId());
            return flavor;
        }).collect(Collectors.toList());

        //dish_flavor表对应菜品口味信息Insert
        dishFlavorService.saveBatch(flavors);
    }

    /**
     * 批量改变菜品销售状态
     * @param status
     * @param ids
     */
    public void changeStatus(int status, List<Long> ids) {
        //构造修改信息
        Dish dish = new Dish();
        dish.setStatus(status);
        //用 in 条件语句 完成批量操作
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(Dish::getId,ids);
        this.update(dish,queryWrapper);
    }

    /**
     * 删除菜品及其对应菜品口味信息，dish、dish_flavor表
     * @param ids
     */
    @Transactional
    public void delete(List ids) {
        //删除菜品表信息，dish表
        this.removeByIds(ids);

        //删除对应菜品口味信息，dish_flavor表
        ids.forEach(id->{
            LambdaQueryWrapper<DishFlavor> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(DishFlavor::getDishId, id);
            dishFlavorService.remove(queryWrapper);
            //单线程批量插入还主键冲突？
            // nested exception is java.sql.BatchUpdateException: Duplicate entry '1562843859429298179' for key 'PRIMARY'] with root cause
        });

    }

    /**
     * 查询对应菜系的菜品信息
     * @param categoryId
     * @return
     */
    public List<Dish> getDishes(Long categoryId) {
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        //匹配对应菜系的菜品
        queryWrapper.eq(categoryId != null, Dish::getCategoryId,categoryId)
                //菜品启用为状态
                .eq(Dish::getStatus,1)
                .orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);
        List<Dish> list = this.list(queryWrapper);
        list.forEach(System.out::print);

        return list;
    }
}

package com.h.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.h.common.Code;
import com.h.common.R;
import com.h.dto.SetmealDto;

import com.h.entity.Category;
import com.h.entity.Setmeal;
import com.h.service.ICategoryService;
import com.h.service.ISetmealService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 套餐 前端控制器
 * </p>
 *
 * @author h
 * @since 2022-08-21
 */
@RestController
@RequestMapping("/setmeal")
@Slf4j
public class SetmealController {

    @Autowired
    private ISetmealService setmealService;
    @Autowired
    private ICategoryService categoryService;

    /**
     * 管理端套餐管理页面分页数据
     * 带名字查询
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    /*@GetMapping("/page")
    public R<Page<Setmeal>> page(int page, int pageSize, String name) {
        //名字查询，条件封装
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(StringUtils.isNotEmpty(name), Setmeal::getName, name);

        //分页格式
        Page<Setmeal> pageInfo = new Page<>(page, pageSize);
        //查询
        setmealService.page(pageInfo, queryWrapper);

        return R.success(Code.GET_PAGE_OK, pageInfo);
    }*/

    /**
     * 套餐分页查询,携带每个套餐分类名
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @GetMapping("/page")
    public R<Page> page(int page,int pageSize,String name){
        //分页构造器对象
        Page<Setmeal> pageInfo = new Page<>(page,pageSize);
        Page<SetmealDto> dtoPage = new Page<>();

        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        //添加查询条件，根据name进行like模糊查询
        queryWrapper.like(name != null,Setmeal::getName,name);
        //添加排序条件，根据更新时间降序排列
        queryWrapper.orderByDesc(Setmeal::getUpdateTime);

        setmealService.page(pageInfo,queryWrapper);

        //对象拷贝
        BeanUtils.copyProperties(pageInfo,dtoPage,"records");
        List<Setmeal> records = pageInfo.getRecords();

        List<SetmealDto> list = records.stream().map((item) -> {
            SetmealDto setmealDto = new SetmealDto();
            //对象拷贝
            BeanUtils.copyProperties(item,setmealDto);
            //分类id
            Long categoryId = item.getCategoryId();
            //根据分类id查询分类对象
            Category category = categoryService.getById(categoryId);
            if(category != null){
                //分类名称
                String categoryName = category.getName();
                setmealDto.setCategoryName(categoryName);
            }
            return setmealDto;
        }).collect(Collectors.toList());

        dtoPage.setRecords(list);
        return R.success(Code.GET_PAGE_OK, dtoPage);
    }

    /**
     * 查询对应套餐数据，修改页数据回显
     * @param setmealId
     * @return
     */
    @GetMapping("/{setmealId}")
    public R<SetmealDto> getById(@PathVariable long setmealId) {
        //重新方法，查询两表
        SetmealDto setmealDto = setmealService.getOneWithDish(setmealId);
        return R.success(Code.GET_ONE_OK, setmealDto);
    }

    /**
     * 添加套餐及其菜品
     * @param setmealDto
     * @return
     */
    @CachePut(value = "setmealCache", key = "setmealDto.categoryId")
    @PostMapping
    public R<String> save(@RequestBody SetmealDto setmealDto) {
        log.info("添加套餐：{}", setmealDto);
        setmealService.saveWithDish(setmealDto);
        return R.success(Code.ADD_OK, "套餐添加成功");
    }

    /**
     * 修改套餐及其菜品
     * @param setmealDto
     * @return
     */
    @CacheEvict(value = "setmealCache", key = "#setmealDto.categoryId")
    @PutMapping
    public R<String> update(@RequestBody SetmealDto setmealDto) {
        log.info("修改套餐：{}", setmealDto);
        setmealService.updateWithDish(setmealDto);
        return R.success(Code.ADD_OK, "套餐修改成功");
    }

//    @CacheEvict(value = "setmealCache", key = "ids")
    //注解似乎不能实现key为集合数组类的
    //简便，全删；高级通过AOP切面编程使用RedisTemplate删除
    @CacheEvict(value = "setmealCache", allEntries = true)
    @PostMapping("/status/{status}")
    public R<String> changStatus(@PathVariable int status, @RequestParam List<Long> ids) {
        StringBuilder idsStr = new StringBuilder();
        ids.forEach(id -> idsStr.append(id).append(" "));
        log.info("修改套餐销售状态：status: {}  ids: {}", status, idsStr);

        setmealService.changStatus(status, ids);
        return R.success(Code.ADD_OK, status == 1 ? "套餐启用成功" : "套餐停用成功");
    }

    /**
     * 批量删除套餐及其菜品
     * @param ids
     * @return
     */
    @CacheEvict(value = "setmealCache", allEntries = true)
    @DeleteMapping
    public R<String> delete(@RequestParam List<Long> ids) {
        StringBuilder idsStr = new StringBuilder();
        ids.forEach(id -> idsStr.append(id).append(" "));
        log.info("删除套餐：{}", idsStr);

        setmealService.removeWithDish(ids);
        return R.success(Code.ADD_OK, "套餐删除成功");
    }

    /**
     * 根据条件查询套餐数据
     * @param setmeal
     * @return
     */
    @Cacheable(value = "setmealCache", key = "#setmeal.categoryId")
    //@Cacheable(value = "setmealCache", key = "#p0.categoryId")
    //@Cacheable(value = "setmealCache", key = "#root.args[0].categoryId")//似乎不对？
    @GetMapping("/list")
    public R<List<Setmeal>> list(Setmeal setmeal) {
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(setmeal.getCategoryId() != null, Setmeal::getCategoryId, setmeal.getCategoryId())
                .eq(setmeal.getStatus() != null, Setmeal::getStatus, setmeal.getStatus())
                .orderByDesc(Setmeal::getUpdateTime);
        List<Setmeal> list = setmealService.list(queryWrapper);
        return R.success(Code.GET_ALL_OK, list);
    }
}


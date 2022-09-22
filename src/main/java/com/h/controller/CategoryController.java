package com.h.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.h.common.Code;
import com.h.common.R;
import com.h.entity.Category;
import com.h.service.ICategoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>
 * 菜品及套餐分类 前端控制器
 * </p>
 *
 * @author h
 * @since 2022-08-21
 */
@RestController
@RequestMapping("/category")
@Slf4j
public class CategoryController {

    @Autowired
    private ICategoryService categoryService;

    /**
     * 分类管理页面数据
     * @param page
     * @param pageSize
     * @return
     */
    @GetMapping("/page")
    public R<Page<Category>> page(int page, int pageSize) {
        //分页构造器
        Page<Category> pageInfo = new Page<>(page,pageSize);
        //条件构造器
        LambdaQueryWrapper<Category> queryWrapper = new LambdaQueryWrapper<>();
        //添加排序条件，根据sort进行排序
        queryWrapper.orderByAsc(Category::getSort);

        //分页查询
        categoryService.page(pageInfo,queryWrapper);
         return pageInfo != null ? R.success(Code.GET_PAGE_OK, pageInfo) : R.error(Code.GET_PAGE_ERR, "还未添加分类数据");
    }

    /**
     * 添加分类
     * @param category
     * @return
     */
    @PostMapping
    public R<String> save(@RequestBody Category category){
        log.info("添加：category: {}",category);
        categoryService.save(category);
        return R.success(Code.ADD_OK,"菜品分类添加成功");
    }

    /**
     * 修改分类
     * @param category
     * @return
     */
    @PutMapping
    public R<String> updateById(@RequestBody Category category){
        log.info("修改：category: {}",category);
        categoryService.updateById(category);
        return R.success(Code.UPDATE_OK,"修改成功");
    }

    /**
     * 删除分类，删除前在service判断是否被其他表关联
     * @param ids
     * @return
     */
    @DeleteMapping()
    public R<String> removeById(Long ids){
        log.info("删除：category: id={}",ids);
        categoryService.removeById(ids);
        return R.success(Code.DELETE_OK,"删除成功");
    }

    /**
     * 菜品和套餐分类列表内容
     * @param category
     * @return
     */
    @GetMapping("/list")
    public R<List<Category>> list(Category category){
        //条件构造
        LambdaQueryWrapper<Category> queryWrapper = new LambdaQueryWrapper<>();
        //获取分类为对应type的，1菜品分类/2套餐分类
        queryWrapper.eq(category.getType() != null, Category::getType,category.getType());
        //添加排序规则
        queryWrapper.orderByAsc(Category::getSort).orderByAsc(Category::getUpdateTime);

        List<Category> lists = categoryService.list(queryWrapper);

        return R.success(Code.GET_ALL_OK,lists);
    }
}


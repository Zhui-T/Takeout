package com.h.service;

import com.h.entity.Category;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 菜品及套餐分类 服务类
 * </p>
 *
 * @author h
 * @since 2022-08-21
 */
public interface ICategoryService extends IService<Category> {
    //当前分类未关联菜品和套餐方可删除
    void removeById(Long id);
}

package com.h.mapper;

import com.h.entity.Category;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 菜品及套餐分类 Mapper 接口
 * </p>
 *
 * @author h
 * @since 2022-08-21
 */
@Mapper
public interface CategoryMapper extends BaseMapper<Category> {

}

package com.h.service;

import com.h.dto.SetmealDto;
import com.h.entity.Setmeal;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * <p>
 * 套餐 服务类
 * </p>
 *
 * @author h
 * @since 2022-08-21
 */
public interface ISetmealService extends IService<Setmeal> {

    /**
     * 获取套餐信息，包含套餐菜品信息
     * @param categoryId
     * @return
     */
    SetmealDto getOneWithDish(Long categoryId);

    /**
     * 新增套餐，同时需要保存套餐和菜品的关联关系
     * @param setmealDto
     */
    void saveWithDish(SetmealDto setmealDto);

    /**
     * 修改套餐和菜品信息
     * @param setmealDto
     */
    void updateWithDish(SetmealDto setmealDto);

    /**
     * 批量修改套餐销售状态
     * @param status
     * @param ids
     */
    void changStatus(int status, List<Long> ids);

    /**
     * 删除套餐，同时需要删除套餐和菜品的关联数据
     * @param ids
     */
    void removeWithDish(List<Long> ids);
}

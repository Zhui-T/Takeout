package com.h.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.h.common.Code;
import com.h.common.R;
import com.h.dto.DishDto;
import com.h.entity.Dish;
import com.h.service.IDishService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 * 菜品管理 前端控制器
 * </p>
 *
 * @author h
 * @since 2022-08-21
 */
@RestController
@RequestMapping("/dish")
@Slf4j
public class DishController {
    @Autowired
    private IDishService dishService;
    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 分类管理页面分页数据,带名字搜索
     * @param page
     * @param pageSize
     * @return
     */
    @GetMapping("/page")
    public R<Page> page(int page, int pageSize, String name) {
        //搜索名字处理
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(StringUtils.isNotEmpty(name), Dish::getName, name);
        //分页处理
        Page<Dish> pageInfo = new Page<>(page, pageSize);

        dishService.page(pageInfo, queryWrapper);
        return R.success(Code.GET_PAGE_OK, pageInfo);
    }

    /**
     * 根据id查询菜和口味品信息
     * 包含dish、dish_flavor两表操作
     *
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public R<DishDto> getByIdWithFlavor(@PathVariable long id) {
        DishDto dishWithFlavor = dishService.getByIdWithFlavor(id);
        return R.success(Code.GET_ONE_OK, dishWithFlavor);
    }

    /**
     * 新增菜品
     * 包含dish、dish_flavor两表操作
     *
     * @param dishDto
     * @return
     */
    @PostMapping
    public R<String> saveWithFlavor(@RequestBody DishDto dishDto) {
        log.info("新增菜品： {}", dishDto);
        dishService.saveWithFlavor(dishDto);

        //清理某个分类下面的菜品缓存数据
        String key = "dish_" + dishDto.getCategoryId() + "_1";
        redisTemplate.delete(key);

        return R.success(Code.ADD_OK, "新增菜品成功");
    }

    /**
     * 修改菜品信息
     * 包含dish、dish_flavor两表操作
     *
     * @param dishDto
     * @return
     */
    @PutMapping
    public R<String> updateWithFlavor(@RequestBody DishDto dishDto) {
        log.info("修改菜品： {}", dishDto);
        dishService.updateWithFlavor(dishDto);

        /*
        清理所有菜品的缓存数据
        Set keys = redisTemplate.keys("dish_*");
        redisTemplate.delete(keys);
        */
        //清理某个分类下面的菜品缓存数据
        String key = "dish_" + dishDto.getCategoryId() + "_1";
        redisTemplate.delete(key);

        return R.success(Code.UPDATE_OK, "菜品修改成功");
    }

    /**
     * 变更起售、停售状态
     *
     * @param status
     * @param ids
     * @return
     */
    @PostMapping("/status/{status}")
    public R<String> changeStatus(@PathVariable int status, @RequestParam List<Long> ids) {
        //日志信息，输出List集合内容
        StringBuilder idsStr = new StringBuilder();
        ids.forEach(id -> idsStr.append(id).append(" "));
        log.info("变更销售状态，ids：{}  {}", status, idsStr);

        dishService.changeStatus(status, ids);
        /*
         * 理应删除相关缓存，还未完善；前端应发来分类数据
         * */
        return R.success(Code.UPDATE_OK, status == 1 ? "起售成功" : "停售成功");
    }

    /**
     * 删除菜品
     *
     * @param ids
     * @return
     */
    @DeleteMapping
    public R<String> delete(@RequestParam List<Long> ids) {
        //日志信息，输出List集合内容
        StringBuilder idsStr = new StringBuilder();
        ids.forEach(id -> idsStr.append(id).append(" "));
        log.info("删除，ids：{}", idsStr);
        /*
        * 理应删除相关缓存，还未完善；前端应发来分类数据
        * */
        //多表删除
        dishService.delete(ids);
        return R.success(Code.DELETE_OK, "删除成功");
    }

    /**
     * 管理端套餐增改时对应菜系的菜品信息
     * @param categoryId
     * @return
     */
    /*@GetMapping("/list")
    public R<List<Dish>> list(Long categoryId){
        log.info("菜品分类id：{}", categoryId);
        List<Dish> list = dishService.getDishes(categoryId);
        return R.success(Code.GET_ALL_OK,list);
    }*/


    /**
     * user端菜品信息展示，对用菜系的菜品和口味信息
     *
     * @param dish
     * @return
     */
    @GetMapping("/list")
    public R<List<DishDto>> list4user(Dish dish) {
        log.info("/dishDtoList for user");

        //动态构建Redis缓存的每个菜品分类信息
        String key = "dish_" + dish.getCategoryId() + "_" + dish.getStatus();//dish_1397844391040167938_1
        //先查询缓存是否有此数据
        List<DishDto> dishDtoList = (List<DishDto>) redisTemplate.opsForValue().get(key);
        //如果缓存查询数据不存在，去SQL数据库查询
        if (dishDtoList == null) {
            log.warn("缓存未命中，菜品类：{}", key);
            dishDtoList = dishService.getDishesWithFlavor(dish);
            //查询后将此数据加入缓存；有效期60分钟
            //直接使用String类型即可，value就是查询结果整个dishDtolist
            //优点是，简单且快速；
            // 缺点：是其中任何一个小小数据的改动都得删除缓存的旧数据
            redisTemplate.opsForValue().set(key, dishDtoList, 60, TimeUnit.MINUTES);
        }
        return R.success(Code.GET_ALL_OK, dishDtoList);
    }
}


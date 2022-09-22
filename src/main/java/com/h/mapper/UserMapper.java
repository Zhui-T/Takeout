package com.h.mapper;

import com.h.entity.User;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 用户信息 Mapper 接口
 * </p>
 *
 * @author h
 * @since 2022-08-21
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {

}

package com.h.mapper;

import com.h.entity.Employee;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 员工信息 Mapper 接口
 * </p>
 *
 * @author h
 * @since 2022-08-21
 */
@Mapper
public interface EmployeeMapper extends BaseMapper<Employee> {

}

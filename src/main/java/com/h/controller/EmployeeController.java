package com.h.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.h.common.Code;
import com.h.common.R;
import com.h.entity.Employee;
import com.h.service.IEmployeeService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

/**
 * <p>
 * 员工信息 前端控制器
 * </p>
 *
 * @author h
 * @since 2022-08-21
 */
@RestController
@RequestMapping("/employee")
@Slf4j
public class EmployeeController {

    @Autowired
    private IEmployeeService employeeService;

    /**
     * 员工登录
     *
     * @param request
     * @param employee
     * @return
     */
    @PostMapping("/login")
    public R<Employee> login(HttpServletRequest request, @RequestBody Employee employee) {
        //将页面提交的密码password进行md5加密处理,DigestUtils.md5DigestAsHex()
        String inputPassword = employee.getPassword();
        inputPassword = DigestUtils.md5DigestAsHex(inputPassword.getBytes());

        //查询employ表校验账号是否存在
        LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Employee::getUsername, employee.getUsername());
        Employee emp = employeeService.getOne(queryWrapper);

        //账号不存在
        if (emp == null) {
            return R.error(Code.GET_ONE_ERR, "账号不存在。");
        }

        //账号存在，比对密码是否正确
        if (!inputPassword.equals(emp.getPassword())) {//注意==和equals的使用
            return R.error(Code.LOG_ERR, "密码错误。");
        }

        //账号是否被禁用
        if (emp.getStatus() == 0) {
            return R.error(Code.LOG_ERR, "账号已禁用");
        }

        //通过验证，将员工id存入Session并返回登录成功结果
        request.getSession().setAttribute("employeeId", emp.getId());
        return R.success(Code.LOG_OK, emp);
    }

    /**
     * 员工登出
     *
     * @param request
     * @return
     */
    @PostMapping("/logout")
    public R<String> logout(HttpServletRequest request) {
        request.getSession().removeAttribute("employeeId");
        return R.success(Code.LOG_OK, "退出成功");
    }

    /**
     * 员工信息分页查询
     *
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @GetMapping("/page")
    public R<Page<Employee>> page(int page, int pageSize, String name) {
        log.info("page = {},pageSize = {},name = {}", page, pageSize, name);
        //分页详情设置
        Page<Employee> iPage = new Page<>(page, pageSize);
        //查询条件封装,记得判空
        LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(StringUtils.isNotEmpty(name), Employee::getName, name);

        //执行分页查询
        employeeService.page(iPage, queryWrapper);//结果就封装在参数iPage对象
        return R.success(Code.GET_PAGE_OK, iPage);
    }

    /**
     * 新增员工
     *
     * @param request
     * @param employee
     * @return
     */
    @PostMapping
    public R<String> save(HttpServletRequest request, @RequestBody Employee employee) {
        log.info("新增员工，员工信息：{}", employee.toString());
        //设置初始密码123456，需要进行md5加密处理,DigestUtils.md5DigestAsHex()
        employee.setPassword(DigestUtils.md5DigestAsHex("123456".getBytes()));

        /*employee.setCreateTime(LocalDateTime.now());
        employee.setUpdateTime(LocalDateTime.now());

        //获得当前登录用户的id
        Long empId = (Long) request.getSession().getAttribute("employeeId");

        employee.setCreateUser(empId);
        employee.setUpdateUser(empId);*/

        employeeService.save(employee);
        return R.success(Code.ADD_OK, "添加成功");
    }

    /**
     * 修改员工信息
     *
     * @param request
     * @param employee
     * @return
     */
    @PutMapping
    public R<String> updateById(HttpServletRequest request, @RequestBody Employee employee) {
        log.info("修改员工，员工信息：{}", employee.toString());

        employee.setUpdateTime(LocalDateTime.now());

        /*//获得当前登录用户的id
        Long empId = (Long) request.getSession().getAttribute("employeeId");
        employee.setUpdateUser(empId);*/

        employeeService.updateById(employee);
        return R.success(Code.UPDATE_OK, "修改成功");
    }

    /**
     * 根据id查询员工信息
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public R<Employee> getById(@PathVariable Long id){
        log.info("根据id查询员工信息...id={}",id);
        Employee employee = employeeService.getById(id);
        if(employee != null){
            return R.success(Code.GET_ONE_OK,employee);
        }
        return R.error(Code.GET_ONE_ERR,"没有查询到对应员工信息");
    }
}



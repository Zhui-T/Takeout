package com.h.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.h.common.BaseContext;
import com.h.common.Code;
import com.h.common.R;
import com.h.dto.OrderDto;
import com.h.entity.OrderDetail;
import com.h.entity.Orders;
import com.h.entity.User;
import com.h.service.IOrderDetailService;
import com.h.service.IOrdersService;
import com.h.service.IUserService;
import com.h.utils.ValidateCodeUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * <p>
 * 用户信息 前端控制器
 * </p>
 *
 * @author h
 * @since 2022-08-21
 */
@RestController
@RequestMapping("/user")
@Slf4j
public class UserController {

    @Autowired
    private IUserService userService;
    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 发送手机短信验证码
     *
     * @param user
     * @return
     */
    @PostMapping("/sendMsg")
    public R<String> sendMsg(@RequestBody User user) {
        //获取手机号
        String phone = user.getPhone();

        if (StringUtils.isNotEmpty(phone)) {
            //生成随机的4位验证码
            String code = ValidateCodeUtils.generateValidateCode(6).toString();
            log.info("/sendMsg phone={} code={}", phone, code);

            //调用阿里云提供的短信服务API完成发送短信
            //SMSUtils.sendMessage("Takeout","",phone,code);

            /*
            //需要将生成的验证码保存到Session，用于后续登录验证
            session.setAttribute(phone, code);
            */
            //改用Redis缓存验证码; 有效时间五分钟
            redisTemplate.opsForValue().set(phone, code, 5, TimeUnit.MINUTES);

            return R.success(Code.LOG_OK, "手机验证码短信发送成功");
        }

        return R.error(Code.LOG_ERR, "短信发送失败");
    }

    /**
     * 移动端用户登录
     *
     * @param map
     * @param session
     * @return
     */
    @PostMapping("/login")
    public R<User> login(@RequestBody Map map, HttpSession session) {
        log.info(map.toString());

        //获取手机号
        String phone = map.get("phone").toString();

        //获取验证码
        String code = map.get("code").toString();

        /*
        //从Session中获取保存的验证码
        Object codeInSession = session.getAttribute(phone);
        */
        //redis取缓存的验证码
        Object codeInCache = redisTemplate.opsForValue().get(phone);

        //进行验证码的比对（页面提交的验证码和Session中保存的验证码比对）；比对成功，则登录成功
        if (codeInCache != null && codeInCache.equals(code)) {

            //查询user表中有无对应手机号，以判断对应的用户是否为新用户
            LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(User::getPhone, phone);
            User user = userService.getOne(queryWrapper);

            if (user == null) {
                //是新用户就自动完成注册
                user = new User();
                user.setPhone(phone);
                user.setStatus(1);
                userService.save(user);
            }
            session.setAttribute("userId", user.getId());
            //登录成功，删除redis缓存的验证码
            redisTemplate.delete(phone);
            return R.success(Code.LOG_OK, user);
        }
        return R.error(Code.LOG_ERR, "登录失败");
    }

    @PostMapping("/loginout")
    public R<String> logout(HttpSession session){
        session.removeAttribute("userId");
        return R.success(Code.LOG_OK, "退出成功");
    }
}


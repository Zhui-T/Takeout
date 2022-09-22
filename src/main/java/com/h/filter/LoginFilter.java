package com.h.filter;

import com.alibaba.fastjson.JSON;
import com.h.common.BaseContext;
import com.h.common.Code;
import com.h.common.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebFilter(filterName = "loginCheckFilter",urlPatterns = "/*")
@Slf4j
public class LoginFilter implements Filter {

    //路径匹配器，支持通配符
    public static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    //定义不需要处理的请求路径
    private String[] uris = {
            "/employee/login",
            "/employee/logout",
            "/backend/**",
            "/front/**",
            "/common/**",
            "/user/sendMsg",
            "/user/login"

    };

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        //当前请求uri
        String requestURI = request.getRequestURI();
        log.info("当前请求：{}",requestURI);

        //uri为登录注册相关资源，放行
        if (checkURI(requestURI)){
            log.info("本次请求不需要处理");
            filterChain.doFilter(request,response);
            return;
        }

        //其他登录后可访问资源
        //员工登录
        Object employeeId =  request.getSession().getAttribute("employeeId");
        if(employeeId != null){
            log.info("用户已登录，用户id为：{}",employeeId);
            //基于threadLocal设置当前用户Id
            BaseContext.setCurrentId((Long) employeeId);
            filterChain.doFilter(request,response);
            return;
        }
        //用户登录
        Object userId = request.getSession().getAttribute("userId");
        if (userId != null){
            log.info("用户已登录，用户id为：{}",userId);
            //基于threadLocal设置当前用户Id
            BaseContext.setCurrentId((Long) userId);
            filterChain.doFilter(request,response);
            return;
        }

        //未登录
        response.getWriter().write(JSON.toJSONString(R.error(Code.LOG_ERR,"NOTLOGIN")));
    }

    private boolean checkURI(String requestURI) {
        for (String uri : uris) {
            if(PATH_MATCHER.match(uri, requestURI)){
                return true;
            }
        }
        return false;
    }
}

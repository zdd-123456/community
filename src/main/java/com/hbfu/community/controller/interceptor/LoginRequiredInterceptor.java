package com.hbfu.community.controller.interceptor;

import com.hbfu.community.annotation.LoginRequired;
import com.hbfu.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.HandlerMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;


@Component
public class LoginRequiredInterceptor implements HandlerInterceptor {
    @Autowired
    private HostHolder hostHolder;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
       //判断拦截的是否为方法
        if (handler instanceof HandlerMethod){
            HandlerMethod handlerMethod=(HandlerMethod) handler;
            Method method=handlerMethod.getMethod();
            LoginRequired loginRequired=method.getAnnotation(LoginRequired.class);
            if(loginRequired!=null&&hostHolder.getUser()==null){
                response.sendRedirect(request.getContextPath()+"/login");
                return false;
            }
        }
        return true;
    }
}

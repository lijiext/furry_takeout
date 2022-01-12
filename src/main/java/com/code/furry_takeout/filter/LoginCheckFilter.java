package com.code.furry_takeout.filter;

import com.alibaba.fastjson.JSON;
import com.code.furry_takeout.common.BaseContext;
import com.code.furry_takeout.common.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.AntPathMatcher;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

// 2. 添加 WebFilter 注解，设置过滤器生效的路径
@Slf4j
@WebFilter(filterName = "loginCheckFilter", urlPatterns = "/*")
// 1. implements javax.servlet.filter
public class LoginCheckFilter implements Filter {
    // 6.1 创建通配符匹配器
    public static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
//        log.info("DoFilter 线程ID：" + Thread.currentThread().getId());

        // 3. 获取 req 和 res 对象
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        // 4. 获取请求到的 URI
        String requestURI = request.getRequestURI();

        // 5. 设置不需要鉴权的白名单路径
        String[] whiteList = new String[]{
                "/employee/login",
                "/employee/logout",
                "/backend/**",
                "/front/**",
                "/common/**",
                "/user/sendMsg",
                "/user/login"
        };

        // 7. 判断 URI 是否需要登录
        if (isInWhiteList(requestURI, whiteList)) {
//            log.info("白名单放行" + requestURI);
            filterChain.doFilter(request, servletResponse);
            return;
        }
        // 移动端登录判断
        if (request.getSession().getAttribute("user") != null) {
            // 1. 用户已经登录，获取 ID
            Long userId = (Long) request.getSession().getAttribute("user");
            log.info("移动端用户 {} 已登录", userId);
            // 2. 设置 ThreadLocal
            BaseContext.setCurrentEmployeeId(userId);
            // 3. 过筛
            filterChain.doFilter(request, response);
            BaseContext.remove();
            return;
        }
        // 8. URI 需要登录 根据 session 判断用户是否已登录
        if (request.getSession().getAttribute("employee") != null) {
            log.info("用户 {} 已登录", request.getSession().getAttribute("employee"));
            // 在线程中记录操作者
            // 注意本处由于没有移除 thread 中的 variable 可能产生内存溢出风险
            Long employeeId = (Long) request.getSession().getAttribute("employee");
            BaseContext.setCurrentEmployeeId(employeeId);

            filterChain.doFilter(request, servletResponse);

            // 到本处 服务端操作已经完成
            // 移除 ThreadLocal 中的变量
            BaseContext.remove();
            return;
        }
        // 9. URI 需要登录 用户没有登录 返回 NOTLOGIN flag 前端处理
        else {
            log.info("用户没有登录");
            response.getWriter().write(JSON.toJSONString(R.error("NOTLOGIN")));
        }

        return;
    }

    // 6. 判断请求 URI 是否符合白名单的模式
    private boolean isInWhiteList(String requestURI, String[] whiteList) {
        for (String item : whiteList) {
            boolean isMatch = PATH_MATCHER.match(item, requestURI);
            if (isMatch) return true;
        }
        return false;
    }
}

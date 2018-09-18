package tech.ascs.icity.iform;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/** 跨域过滤器 */
@Component
@Order(Integer.MIN_VALUE)
public class CosFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletResponse response = (HttpServletResponse)servletResponse;
        // 跨域参数只需要设置Access-Control-Allow-Origin就行了，Access-Control-Allow-Origin设置成*，一劳永逸
        response.setHeader("Access-Control-Allow-Origin", "*");
        // 前后端分离，好像前端不走代理，直接访问时，如果是POST，PUT等请求，可能会有问题，Access-Control-Allow-Methods设置成*，一劳永逸
        response.setHeader("Access-Control-Allow-Methods", "*");
        // 前后端分离，好像前端不走代理，浏览器有各种问题，可能会有问题，Access-Control-Allow-Headers设置成*，一劳永逸
        response.setHeader("Access-Control-Allow-Headers", "*");
        filterChain.doFilter(servletRequest, response);
    }

    @Override
    public void destroy() {

    }
}
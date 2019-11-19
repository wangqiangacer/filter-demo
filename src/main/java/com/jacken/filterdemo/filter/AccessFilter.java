package com.jacken.filterdemo.filter;

import com.google.gson.Gson;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 过滤器的生命周期方法init  doFilter  destroy
 *
 * 以及过滤器的执行流程
 */
@WebFilter(urlPatterns = "*")
public class AccessFilter implements Filter {
    private static Log logger = LogFactory.getLog(AccessFilter.class);

    private static final String[] ignoredPostfixs = new String[]{"js", "css", "png", "jpg", "gz", "zip", "apk", "gif",
            "svg", "json", "woff2", "eot", "ttf"};
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    private final static Gson GSON = new Gson();
    private List<String> noAuthUriList= Arrays.asList("/index","/login");
//    @Autowired
//    ServletFilterConfig servletFilterConfig;
    @Autowired
    StringRedisTemplate stringRedisTemplate;

    private boolean validateToken(String token) {
        String cacheToken = stringRedisTemplate.opsForValue().get("token:sale:" + token);
        if (cacheToken == null) {
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {

        logger.info("-------------------------接收到请求-----------------------");
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        String requestURI = request.getRequestURI();
        System.out.println(requestURI);

        if (request.getMethod().equalsIgnoreCase("OPTIONS")) {
            filterChain.doFilter(request, response);
            return;
        }
        logger.info(request.getRequestURI() + "------------------------跨过了OPTIONS-----------------------");
        String uri = request.getRequestURI();
        String errorMsg = "";
        if (!isStaticUri(uri) && !isExclude(uri)) {
            boolean isAuth = false;
            String token = getTokenByRequest(request);
            if (token == null) {
                errorMsg = "Token is missing.";
            } else {
                request.setAttribute("token", token);
                isAuth = validateToken(token);
                if (!isAuth) {
                    errorMsg = "Token invalid.";
                    logger.info("Invalid token:" + token);
                }
            }

            if (!isAuth) {
                response.setContentType("application/json;charset=UTF-8");
               // response.getWriter().write(GSON.toJson(ResultModel.createFail(ExceptionEnum.TOKEN_ERROR.getCode(), ExceptionEnum.TOKEN_ERROR.getMessage())));
                return;
            }
        }
        filterChain.doFilter(request, response);
        return;

    }

    @Override
    public void destroy() {

    }

    protected boolean isExclude(String url) {
        for (String excludeUri : noAuthUriList) {
            if (url.startsWith(excludeUri))
                return true;
        }
        return false;
    }

    private void corsHandler(HttpServletResponse response) {
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Headers", "*");
        response.setHeader("Access-Control-Allow-Methods", "PUT,POST,GET,DELETE,OPTIONS");
        response.setHeader("Access-Control-Max-Age", "18000L");
    }

    private String getTokenByRequest(HttpServletRequest request) {
        String token = request.getParameter("Token");
        if (token == null) {
            token = request.getHeader("Token");
            if (token == null) {
                token = request.getHeader("Token");
            }
        }
        return token;
    }

    private boolean isStaticUri(String uri) {
        boolean ignored = false;
        if (uri.lastIndexOf(".") != -1) {
            String postfix = uri.substring(uri.lastIndexOf(".") + 1, uri.length());
            for (int i = 0; i < ignoredPostfixs.length; i++) {
                if (ignoredPostfixs[i].equalsIgnoreCase(postfix)) {
                    ignored = true;
                    break;
                }
            }
        }
        return ignored;
    }
}

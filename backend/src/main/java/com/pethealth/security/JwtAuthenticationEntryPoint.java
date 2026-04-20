package com.pethealth.security;

import com.alibaba.fastjson2.JSON;
import com.pethealth.common.Result;
import com.pethealth.common.ResultCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.PrintWriter;

/**
 * JWT认证入口点 - 处理未认证请求
 */
@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        Result<?> result = Result.error(ResultCode.UNAUTHORIZED.getCode(), "未登录或Token已过期");

        try (PrintWriter writer = response.getWriter()) {
            writer.write(JSON.toJSONString(result));
        }
    }
}

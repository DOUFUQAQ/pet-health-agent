package com.pethealth.utils;

import com.pethealth.security.LoginUser;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * 安全工具类 - 获取当前登录用户
 */
public class SecurityUtil {

    /**
     * 获取当前登录用户
     */
    public static LoginUser getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof LoginUser)) {
            return null;
        }
        return (LoginUser) authentication.getPrincipal();
    }

    /**
     * 获取当前用户ID
     */
    public static Long getCurrentUserId() {
        LoginUser user = getCurrentUser();
        return user == null ? null : user.getUserId();
    }

    /**
     * 获取当前用户名
     */
    public static String getCurrentUsername() {
        LoginUser user = getCurrentUser();
        return user == null ? null : user.getUsername();
    }

    /**
     * 是否已登录
     */
    public static boolean isAuthenticated() {
        return getCurrentUser() != null;
    }
}

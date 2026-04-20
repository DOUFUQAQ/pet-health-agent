package com.pethealth.utils;

import cn.hutool.crypto.digest.BCrypt;
import cn.hutool.crypto.digest.DigestUtil;

/**
 * 密码工具类
 */
public class PasswordUtil {

    /**
     * BCrypt加密密码
     */
    public static String encode(String rawPassword) {
        return BCrypt.hashpw(rawPassword, BCrypt.gensalt());
    }

    /**
     * BCrypt校验密码
     */
    public static boolean matches(String rawPassword, String encodedPassword) {
        try {
            return BCrypt.checkpw(rawPassword, encodedPassword);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * MD5加密（用于简单场景，不推荐用于密码存储）
     */
    public static String md5(String str) {
        return DigestUtil.md5Hex(str);
    }

    /**
     * SHA256加密
     */
    public static String sha256(String str) {
        return DigestUtil.sha256Hex(str);
    }
}

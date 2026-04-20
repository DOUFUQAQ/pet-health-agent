package com.pethealth.controller;

import com.pethealth.common.Result;
import com.pethealth.dto.LoginDTO;
import com.pethealth.dto.RegisterDTO;
import com.pethealth.service.IUserService;
import com.pethealth.utils.SecurityUtil;
import com.pethealth.vo.UserVO;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

/**
 * 认证控制器
 */
@RestController
@RequestMapping("/auth")
public class AuthController {

    @Resource
    private IUserService userService;

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public Result<UserVO> login(@Valid @RequestBody LoginDTO loginDTO) {
        return Result.success(userService.login(loginDTO));
    }

    /**
     * 用户注册
     */
    @PostMapping("/register")
    public Result<UserVO> register(@Valid @RequestBody RegisterDTO registerDTO) {
        return Result.success(userService.register(registerDTO));
    }

    /**
     * 用户登出
     */
    @PostMapping("/logout")
    public Result<Void> logout() {
        Long userId = SecurityUtil.getCurrentUserId();
        if (userId != null) {
            userService.logout(userId);
        }
        return Result.success();
    }
}

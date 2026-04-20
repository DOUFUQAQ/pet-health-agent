package com.pethealth.controller;

import com.pethealth.common.Result;
import com.pethealth.entity.User;
import com.pethealth.service.IUserService;
import com.pethealth.utils.SecurityUtil;
import com.pethealth.vo.UserVO;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 用户控制器
 */
@RestController
@RequestMapping("/user")
public class UserController {

    @Resource
    private IUserService userService;

    /**
     * 获取当前用户信息
     */
    @GetMapping("/info")
    public Result<UserVO> getUserInfo() {
        return Result.success(userService.getUserInfo(SecurityUtil.getCurrentUserId()));
    }

    /**
     * 更新用户信息
     */
    @PutMapping("/info")
    public Result<UserVO> updateUserInfo(@RequestBody User user) {
        return Result.success(userService.updateUserInfo(SecurityUtil.getCurrentUserId(), user));
    }

    /**
     * 修改密码
     */
    @PutMapping("/password")
    public Result<Void> updatePassword(@RequestBody Map<String, String> params) {
        userService.updatePassword(SecurityUtil.getCurrentUserId(),
                params.get("oldPassword"), params.get("newPassword"));
        return Result.success();
    }
}

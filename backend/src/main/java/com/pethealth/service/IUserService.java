package com.pethealth.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.pethealth.dto.LoginDTO;
import com.pethealth.dto.RegisterDTO;
import com.pethealth.entity.User;
import com.pethealth.vo.UserVO;

/**
 * 用户服务接口
 */
public interface IUserService extends IService<User> {

    /**
     * 用户登录
     */
    UserVO login(LoginDTO loginDTO);

    /**
     * 用户注册
     */
    UserVO register(RegisterDTO registerDTO);

    /**
     * 获取用户信息
     */
    UserVO getUserInfo(Long userId);

    /**
     * 修改密码
     */
    void updatePassword(Long userId, String oldPassword, String newPassword);

    /**
     * 更新用户信息
     */
    UserVO updateUserInfo(Long userId, User user);

    /**
     * 用户登出
     */
    void logout(Long userId);
}

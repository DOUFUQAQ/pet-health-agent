package com.pethealth.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pethealth.common.Constants;
import com.pethealth.common.Result;
import com.pethealth.entity.Alert;
import com.pethealth.entity.Device;
import com.pethealth.entity.Pet;
import com.pethealth.entity.User;
import com.pethealth.mapper.AlertMapper;
import com.pethealth.mapper.DeviceMapper;
import com.pethealth.mapper.PetMapper;
import com.pethealth.mapper.UserMapper;
import com.pethealth.vo.AdminOverviewVO;
import jakarta.annotation.Resource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 管理员后台接口
 */
@RestController
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    @Resource
    private UserMapper userMapper;

    @Resource
    private PetMapper petMapper;

    @Resource
    private DeviceMapper deviceMapper;

    @Resource
    private AlertMapper alertMapper;

    @GetMapping("/overview")
    public Result<AdminOverviewVO> overview() {
        AdminOverviewVO vo = new AdminOverviewVO();

        vo.setTotalUsers(userMapper.selectCount(new LambdaQueryWrapper<>()));
        vo.setTotalPets(petMapper.selectCount(new LambdaQueryWrapper<>()));
        vo.setTotalDevices(deviceMapper.selectCount(new LambdaQueryWrapper<>()));

        LambdaQueryWrapper<Device> onlineWrapper = new LambdaQueryWrapper<>();
        onlineWrapper.eq(Device::getStatus, Constants.DeviceStatus.ONLINE);
        vo.setOnlineDevices(deviceMapper.selectCount(onlineWrapper));

        LambdaQueryWrapper<Alert> unreadWrapper = new LambdaQueryWrapper<>();
        unreadWrapper.eq(Alert::getStatus, Constants.AlertStatus.UNREAD);
        vo.setUnreadAlerts(alertMapper.selectCount(unreadWrapper));

        LambdaQueryWrapper<Alert> todayWrapper = new LambdaQueryWrapper<>();
        todayWrapper.ge(Alert::getAlertTime, LocalDate.now().atStartOfDay());
        vo.setTodayAlerts(alertMapper.selectCount(todayWrapper));

        return Result.success(vo);
    }

    @GetMapping("/users")
    public Result<IPage<User>> users(@RequestParam(defaultValue = "1") Integer pageNum,
                                     @RequestParam(defaultValue = "10") Integer pageSize,
                                     @RequestParam(required = false) String keyword,
                                     @RequestParam(required = false) Integer status,
                                     @RequestParam(required = false) Integer role) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.and(keyword != null && !keyword.isBlank(), w -> w
                        .like(User::getUsername, keyword)
                        .or().like(User::getPhone, keyword)
                        .or().like(User::getNickname, keyword))
                .eq(status != null, User::getStatus, status)
                .eq(role != null, User::getRole, role)
                .orderByDesc(User::getCreateTime);

        return Result.success(userMapper.selectPage(new Page<>(pageNum, pageSize), wrapper));
    }

    @PutMapping("/users/{id}/status")
    public Result<Void> updateUserStatus(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        Integer status = body.get("status") == null ? null : Integer.valueOf(body.get("status").toString());
        if (status == null) {
            return Result.error("status不能为空");
        }
        User user = userMapper.selectById(id);
        if (user == null) {
            return Result.error("用户不存在");
        }
        user.setStatus(status);
        user.setUpdateTime(LocalDateTime.now());
        userMapper.updateById(user);
        return Result.success();
    }

    @GetMapping("/pets")
    public Result<IPage<Pet>> pets(@RequestParam(defaultValue = "1") Integer pageNum,
                                   @RequestParam(defaultValue = "10") Integer pageSize,
                                   @RequestParam(required = false) String keyword) {
        LambdaQueryWrapper<Pet> wrapper = new LambdaQueryWrapper<>();
        wrapper.and(keyword != null && !keyword.isBlank(), w -> w
                        .like(Pet::getName, keyword)
                        .or().like(Pet::getBreed, keyword)
                        .or().like(Pet::getSpecies, keyword))
                .orderByDesc(Pet::getCreateTime);
        return Result.success(petMapper.selectPage(new Page<>(pageNum, pageSize), wrapper));
    }

    @GetMapping("/devices")
    public Result<IPage<Device>> devices(@RequestParam(defaultValue = "1") Integer pageNum,
                                         @RequestParam(defaultValue = "10") Integer pageSize,
                                         @RequestParam(required = false) String keyword,
                                         @RequestParam(required = false) Integer status) {
        LambdaQueryWrapper<Device> wrapper = new LambdaQueryWrapper<>();
        wrapper.and(keyword != null && !keyword.isBlank(), w -> w
                        .like(Device::getDeviceId, keyword)
                        .or().like(Device::getDeviceName, keyword))
                .eq(status != null, Device::getStatus, status)
                .orderByDesc(Device::getCreateTime);
        return Result.success(deviceMapper.selectPage(new Page<>(pageNum, pageSize), wrapper));
    }

    @GetMapping("/alerts")
    public Result<IPage<Alert>> alerts(@RequestParam(defaultValue = "1") Integer pageNum,
                                       @RequestParam(defaultValue = "10") Integer pageSize,
                                       @RequestParam(required = false) Integer status,
                                       @RequestParam(required = false) Integer level) {
        LambdaQueryWrapper<Alert> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(status != null, Alert::getStatus, status)
                .eq(level != null, Alert::getAlertLevel, level)
                .orderByDesc(Alert::getAlertTime);
        return Result.success(alertMapper.selectPage(new Page<>(pageNum, pageSize), wrapper));
    }
}

package com.pethealth.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.pethealth.common.Result;
import com.pethealth.entity.Alert;
import com.pethealth.service.IAlertService;
import com.pethealth.utils.SecurityUtil;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

/**
 * 预警控制器
 */
@RestController
@RequestMapping("/alert")
public class AlertController {

    @Resource
    private IAlertService alertService;

    /**
     * 分页查询预警
     */
    @GetMapping("/list")
    public Result<IPage<Alert>> list(@RequestParam(defaultValue = "1") Integer pageNum,
                                      @RequestParam(defaultValue = "20") Integer pageSize,
                                      @RequestParam(required = false) Integer status) {
        return Result.success(alertService.listUserAlerts(
                SecurityUtil.getCurrentUserId(), pageNum, pageSize, status));
    }

    /**
     * 获取未读预警数量
     */
    @GetMapping("/unread/count")
    public Result<Integer> unreadCount() {
        return Result.success(alertService.getUnreadCount(SecurityUtil.getCurrentUserId()));
    }

    /**
     * 标记为已读
     */
    @PutMapping("/{id}/read")
    public Result<Void> markAsRead(@PathVariable Long id) {
        alertService.markAsRead(SecurityUtil.getCurrentUserId(), id);
        return Result.success();
    }

    /**
     * 全部标记已读
     */
    @PutMapping("/read/all")
    public Result<Void> markAllAsRead() {
        alertService.markAllAsRead(SecurityUtil.getCurrentUserId());
        return Result.success();
    }

    /**
     * 处理预警
     */
    @PutMapping("/{id}/handle")
    public Result<Void> handle(@PathVariable Long id) {
        alertService.handleAlert(SecurityUtil.getCurrentUserId(), id);
        return Result.success();
    }
}

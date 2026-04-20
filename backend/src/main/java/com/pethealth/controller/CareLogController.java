package com.pethealth.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.pethealth.common.Result;
import com.pethealth.dto.CareLogDTO;
import com.pethealth.entity.CareLog;
import com.pethealth.service.ICareLogService;
import com.pethealth.utils.SecurityUtil;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

/**
 * 养护日志控制器
 */
@RestController
@RequestMapping("/carelog")
public class CareLogController {

    @Resource
    private ICareLogService careLogService;

    /**
     * 分页查询养护日志
     */
    @GetMapping("/list")
    public Result<IPage<CareLog>> list(@RequestParam Long petId,
                                        @RequestParam(required = false) String logType,
                                        @RequestParam(defaultValue = "1") Integer pageNum,
                                        @RequestParam(defaultValue = "20") Integer pageSize) {
        return Result.success(careLogService.listPetCareLogs(
                SecurityUtil.getCurrentUserId(), petId, logType, pageNum, pageSize));
    }

    /**
     * 添加日志
     */
    @PostMapping
    public Result<CareLog> add(@Valid @RequestBody CareLogDTO dto) {
        return Result.success(careLogService.addCareLog(SecurityUtil.getCurrentUserId(), dto));
    }

    /**
     * 更新日志
     */
    @PutMapping
    public Result<CareLog> update(@Valid @RequestBody CareLogDTO dto) {
        return Result.success(careLogService.updateCareLog(SecurityUtil.getCurrentUserId(), dto));
    }

    /**
     * 删除日志
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        careLogService.deleteCareLog(SecurityUtil.getCurrentUserId(), id);
        return Result.success();
    }
}

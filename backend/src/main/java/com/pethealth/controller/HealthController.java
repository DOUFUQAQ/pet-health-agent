package com.pethealth.controller;

import com.pethealth.common.Result;
import com.pethealth.entity.DeviceData;
import com.pethealth.entity.HealthRecord;
import com.pethealth.service.IDeviceDataService;
import com.pethealth.service.IHealthService;
import com.pethealth.utils.SecurityUtil;
import com.pethealth.vo.HealthDashboardVO;
import jakarta.annotation.Resource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 健康数据控制器
 */
@RestController
@RequestMapping("/health")
public class HealthController {

    @Resource
    private IHealthService healthService;

    @Resource
    private IDeviceDataService deviceDataService;

    /**
     * 健康看板
     */
    @GetMapping("/dashboard/{petId}")
    public Result<HealthDashboardVO> dashboard(@PathVariable Long petId) {
        return Result.success(healthService.getDashboard(SecurityUtil.getCurrentUserId(), petId));
    }

    /**
     * 健康历史记录
     */
    @GetMapping("/history/{petId}")
    public Result<List<HealthRecord>> history(@PathVariable Long petId,
                                              @RequestParam(defaultValue = "30") Integer days) {
        return Result.success(healthService.getHistoryRecords(petId, days));
    }

    /**
     * 原始数据查询
     */
    @GetMapping("/data")
    public Result<List<DeviceData>> queryData(
            @RequestParam Long petId,
            @RequestParam String dataType,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        return Result.success(deviceDataService.queryByPetAndType(petId, dataType, startTime, endTime));
    }

    /**
     * 获取趋势数据
     */
    @GetMapping("/trend")
    public Result<List<Map<String, Object>>> trend(
            @RequestParam Long petId,
            @RequestParam String dataType,
            @RequestParam(defaultValue = "7") Integer days) {
        return Result.success(deviceDataService.getDailyTrend(petId, dataType, days));
    }

    /**
     * 触发AI健康分析
     */
    @PostMapping("/analyze/{petId}")
    public Result<String> analyze(@PathVariable Long petId) {
        return Result.success(healthService.analyzeHealthWithAI(petId, java.time.LocalDate.now()));
    }

    /**
     * 宠物最近 N 小时 GPS 轨迹
     * 返回数组：[{lat, lng, sats, timestamp}]，按时间升序
     */
    @GetMapping("/track/{petId}")
    public Result<List<Map<String, Object>>> track(@PathVariable Long petId,
                                                   @RequestParam(defaultValue = "24") Integer hours) {
        LocalDateTime start = LocalDateTime.now().minusHours(hours);
        LocalDateTime end = LocalDateTime.now();
        List<DeviceData> rows = deviceDataService.queryByPetAndType(petId, "location", start, end);
        List<Map<String, Object>> points = new ArrayList<>();
        for (DeviceData d : rows) {
            try {
                com.alibaba.fastjson2.JSONObject json = com.alibaba.fastjson2.JSON.parseObject(d.getDataValue());
                Map<String, Object> p = new HashMap<>();
                p.put("lat", json.getDouble("lat"));
                p.put("lng", json.getDouble("lng"));
                p.put("sats", json.getInteger("sats"));
                p.put("timestamp", d.getTimestamp());
                points.add(p);
            } catch (Exception ignore) {
                // skip malformed
            }
        }
        return Result.success(points);
    }
}

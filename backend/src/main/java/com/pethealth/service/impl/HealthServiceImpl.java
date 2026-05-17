package com.pethealth.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pethealth.ai.PromptBuilder;
import com.pethealth.ai.QwenLLMService;
import com.pethealth.common.Constants;
import com.pethealth.common.ResultCode;
import com.pethealth.entity.DeviceData;
import com.pethealth.entity.HealthRecord;
import com.pethealth.entity.Pet;
import com.pethealth.exception.BusinessException;
import com.pethealth.mapper.HealthRecordMapper;
import com.pethealth.mapper.PetMapper;
import com.pethealth.service.IAlertService;
import com.pethealth.service.IDeviceDataService;
import com.pethealth.service.IHealthService;
import com.pethealth.utils.DateUtil;
import com.pethealth.vo.HealthDashboardVO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 健康服务实现
 */
@Slf4j
@Service
public class HealthServiceImpl extends ServiceImpl<HealthRecordMapper, HealthRecord> implements IHealthService {

    @Resource
    private PetMapper petMapper;

    @Resource
    private IDeviceDataService deviceDataService;

    @Resource
    private IAlertService alertService;

    @Resource
    private QwenLLMService llmService;

    @Override
    public HealthDashboardVO getDashboard(Long userId, Long petId) {
        Pet pet = petMapper.selectById(petId);
        if (pet == null || !pet.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.PET_NOT_FOUND);
        }

        HealthDashboardVO vo = new HealthDashboardVO();
        vo.setPetId(petId);
        vo.setPetName(pet.getName());

        // 获取最新数据
        vo.setCurrentTemperature(parseValue(deviceDataService.getLatest(petId, Constants.DataType.TEMPERATURE)));
        BigDecimal hr = parseValue(deviceDataService.getLatest(petId, Constants.DataType.HEART_RATE));
        vo.setCurrentHeartRate(hr == null ? null : hr.intValue());
        vo.setCurrentEnvTemp(parseValue(deviceDataService.getLatest(petId, "env_temp")));
        vo.setCurrentHumidity(parseValue(deviceDataService.getLatest(petId, "humidity")));

        // 今日汇总数据
        HealthRecord todayRecord = baseMapper.selectByPetAndDate(petId, LocalDate.now());
        if (todayRecord != null) {
            vo.setHealthScore(todayRecord.getHealthScore());
            vo.setTodaySteps(todayRecord.getActivitySteps());
            vo.setTodaySleep(todayRecord.getSleepDuration());
            vo.setTodayFood(todayRecord.getFoodIntake());
            vo.setTodayWater(todayRecord.getWaterIntake());
            vo.setAiAnalysis(todayRecord.getAiAnalysis());
        } else {
            vo.setHealthScore(Constants.DefaultValue.DEFAULT_HEALTH_SCORE);
        }

        // 未读预警数
        vo.setUnreadAlerts(alertService.getUnreadCount(userId));

        // 7天趋势
        vo.setTemperatureTrend(toTrend(deviceDataService.getDailyTrend(petId, Constants.DataType.TEMPERATURE, 7)));
        vo.setHeartRateTrend(toTrend(deviceDataService.getDailyTrend(petId, Constants.DataType.HEART_RATE, 7)));
        vo.setActivityTrend(toTrend(deviceDataService.getDailyTrend(petId, Constants.DataType.ACTIVITY, 7)));

        return vo;
    }

    @Override
    public HealthRecord generateDailyReport(Long petId, LocalDate date) {
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.atTime(23, 59, 59);

        HealthRecord record = baseMapper.selectByPetAndDate(petId, date);
        if (record == null) {
            record = new HealthRecord();
            record.setPetId(petId);
            record.setRecordDate(date);
            record.setCreateTime(LocalDateTime.now());
        }

        // 聚合体温
        List<DeviceData> tempList = deviceDataService.queryByPetAndType(petId, Constants.DataType.TEMPERATURE, start, end);
        aggregateTemperature(record, tempList);

        // 聚合心率
        List<DeviceData> hrList = deviceDataService.queryByPetAndType(petId, Constants.DataType.HEART_RATE, start, end);
        aggregateHeartRate(record, hrList);

        // 健康评分
        record.setHealthScore(calculateHealthScore(petId, date));

        // AI分析
        try {
            record.setAiAnalysis(analyzeHealthWithAI(petId, date));
        } catch (Exception e) {
            log.warn("AI分析失败: {}", e.getMessage());
        }

        if (record.getId() == null) {
            baseMapper.insert(record);
        } else {
            baseMapper.updateById(record);
        }
        return record;
    }

    @Override
    public List<HealthRecord> getHistoryRecords(Long petId, Integer days) {
        return baseMapper.selectRecentRecords(petId, days);
    }

    @Override
    public Integer calculateHealthScore(Long petId, LocalDate date) {
        // 基础分100，根据异常扣分
        int score = 100;

        HealthRecord record = baseMapper.selectByPetAndDate(petId, date);
        if (record != null) {
            if (record.getTemperatureMax() != null && record.getTemperatureMax().doubleValue() > 39.5) score -= 15;
            if (record.getTemperatureMin() != null && record.getTemperatureMin().doubleValue() < 37.5) score -= 15;
            if (record.getAbnormalBehaviors() != null) score -= 10;
        }

        // 未处理预警数量扣分
        return Math.max(score, 0);
    }

    @Override
    public String analyzeHealthWithAI(Long petId, LocalDate date) {
        Pet pet = petMapper.selectById(petId);
        HealthRecord record = baseMapper.selectByPetAndDate(petId, date);
        if (pet == null || record == null) return null;

        String petInfo = String.format("名字:%s, 物种:%s, 年龄:%s",
                pet.getName(), pet.getSpecies(),
                pet.getBirthday() != null ? DateUtil.calculateAge(pet.getBirthday()) + "岁" : "未知");

        StringBuilder data = new StringBuilder();
        if (record.getTemperatureAvg() != null) data.append("平均体温:").append(record.getTemperatureAvg()).append("°C ");
        if (record.getHeartRateAvg() != null) data.append("平均心率:").append(record.getHeartRateAvg()).append("次/分 ");
        if (record.getActivitySteps() != null) data.append("活动步数:").append(record.getActivitySteps()).append(" ");
        if (record.getSleepDuration() != null) data.append("睡眠:").append(record.getSleepDuration()).append("分钟 ");

        String prompt = PromptBuilder.buildHealthAnalysisPrompt(petInfo, data.toString());
        QwenLLMService.ChatResponse resp = llmService.chat(
                "你是一位专业的宠物健康顾问。", prompt);
        return resp.getContent();
    }

    // ==================== 私有方法 ====================

    private BigDecimal parseValue(DeviceData data) {
        if (data == null || data.getDataValue() == null) return null;
        try {
            com.alibaba.fastjson2.JSONObject json = com.alibaba.fastjson2.JSON.parseObject(data.getDataValue());
            return json.getBigDecimal("value");
        } catch (Exception e) {
            return null;
        }
    }

    private void aggregateTemperature(HealthRecord record, List<DeviceData> list) {
        if (list == null || list.isEmpty()) return;
        BigDecimal sum = BigDecimal.ZERO, max = null, min = null;
        int count = 0;
        for (DeviceData d : list) {
            BigDecimal v = parseValue(d);
            if (v == null) continue;
            sum = sum.add(v);
            count++;
            if (max == null || v.compareTo(max) > 0) max = v;
            if (min == null || v.compareTo(min) < 0) min = v;
        }
        if (count > 0) {
            record.setTemperatureAvg(sum.divide(BigDecimal.valueOf(count), 2, RoundingMode.HALF_UP));
            record.setTemperatureMax(max);
            record.setTemperatureMin(min);
        }
    }

    private void aggregateHeartRate(HealthRecord record, List<DeviceData> list) {
        if (list == null || list.isEmpty()) return;
        int sum = 0, max = Integer.MIN_VALUE, min = Integer.MAX_VALUE, count = 0;
        for (DeviceData d : list) {
            BigDecimal v = parseValue(d);
            if (v == null) continue;
            int iv = v.intValue();
            sum += iv;
            count++;
            if (iv > max) max = iv;
            if (iv < min) min = iv;
        }
        if (count > 0) {
            record.setHeartRateAvg(sum / count);
            record.setHeartRateMax(max);
            record.setHeartRateMin(min);
        }
    }

    private List<HealthDashboardVO.TrendData> toTrend(List<Map<String, Object>> data) {
        List<HealthDashboardVO.TrendData> result = new ArrayList<>();
        if (data == null) return result;
        for (Map<String, Object> m : data) {
            HealthDashboardVO.TrendData t = new HealthDashboardVO.TrendData();
            t.setDate(String.valueOf(m.get("date")));
            Object avgValue = m.get("avg_value");
            if (avgValue != null) {
                t.setValue(new BigDecimal(avgValue.toString()));
            }
            result.add(t);
        }
        return result;
    }
}

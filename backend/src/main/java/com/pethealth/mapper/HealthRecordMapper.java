package com.pethealth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pethealth.entity.HealthRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

/**
 * 健康记录Mapper
 */
@Mapper
public interface HealthRecordMapper extends BaseMapper<HealthRecord> {

    /**
     * 查询宠物某日期的健康记录
     */
    HealthRecord selectByPetAndDate(@Param("petId") Long petId, @Param("recordDate") LocalDate recordDate);

    /**
     * 查询宠物最近N天的健康记录
     */
    List<HealthRecord> selectRecentRecords(@Param("petId") Long petId, @Param("days") Integer days);
}

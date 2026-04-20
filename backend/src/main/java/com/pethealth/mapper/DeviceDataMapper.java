package com.pethealth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pethealth.entity.DeviceData;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 设备数据Mapper
 */
@Mapper
public interface DeviceDataMapper extends BaseMapper<DeviceData> {

    /**
     * 查询宠物某时间段内某类型的数据
     */
    List<DeviceData> selectByPetAndTypeAndTime(@Param("petId") Long petId,
                                                @Param("dataType") String dataType,
                                                @Param("startTime") LocalDateTime startTime,
                                                @Param("endTime") LocalDateTime endTime);

    /**
     * 按天聚合查询数据（用于趋势图）
     */
    List<Map<String, Object>> selectDailyAggregate(@Param("petId") Long petId,
                                                    @Param("dataType") String dataType,
                                                    @Param("days") Integer days);

    /**
     * 查询宠物最新一条指定类型数据
     */
    DeviceData selectLatestByPetAndType(@Param("petId") Long petId,
                                        @Param("dataType") String dataType);
}

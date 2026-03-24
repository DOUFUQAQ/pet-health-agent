package com.pethealth.vo;

import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 宠物信息VO
 */
@Data
public class PetVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private Long userId;
    private String name;
    private String species;
    private String breed;
    private Integer gender;
    private LocalDate birthday;
    private BigDecimal weight;
    private String avatar;
    private String medicalHistory;
    private String allergies;
    private String vaccinationRecords;
    private Integer status;
    private LocalDateTime createTime;
    
    /**
     * 年龄（计算得出）
     */
    private Integer age;
    
    /**
     * 最新健康评分
     */
    private Integer healthScore;
    
    /**
     * 绑定设备数量
     */
    private Integer deviceCount;
}

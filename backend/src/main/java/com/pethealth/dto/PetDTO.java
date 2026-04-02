package com.pethealth.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 宠物信息DTO
 */
@Data
public class PetDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 宠物ID（更新时需要）
     */
    private Long id;

    /**
     * 宠物名称
     */
    @NotBlank(message = "宠物名称不能为空")
    private String name;

    /**
     * 物种：dog-狗，cat-猫
     */
    @NotBlank(message = "物种不能为空")
    private String species;

    /**
     * 品种
     */
    private String breed;

    /**
     * 性别：0-母，1-公
     */
    @NotNull(message = "性别不能为空")
    private Integer gender;

    /**
     * 出生日期
     */
    private LocalDate birthday;

    /**
     * 体重（kg）
     */
    private BigDecimal weight;

    /**
     * 头像URL
     */
    private String avatar;

    /**
     * 既往病史
     */
    private String medicalHistory;

    /**
     * 过敏史
     */
    private String allergies;

    /**
     * 疫苗接种记录（JSON）
     */
    private String vaccinationRecords;
}

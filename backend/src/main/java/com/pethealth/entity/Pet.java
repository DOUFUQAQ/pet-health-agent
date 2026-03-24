package com.pethealth.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 宠物档案实体类
 */
@Data
@TableName("pet")
public class Pet implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 宠物ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 所属用户ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 宠物名称
     */
    @TableField("name")
    private String name;

    /**
     * 物种：dog-狗，cat-猫
     */
    @TableField("species")
    private String species;

    /**
     * 品种
     */
    @TableField("breed")
    private String breed;

    /**
     * 性别：0-母，1-公
     */
    @TableField("gender")
    private Integer gender;

    /**
     * 出生日期
     */
    @TableField("birthday")
    private LocalDate birthday;

    /**
     * 体重（kg）
     */
    @TableField("weight")
    private BigDecimal weight;

    /**
     * 头像URL
     */
    @TableField("avatar")
    private String avatar;

    /**
     * 既往病史
     */
    @TableField("medical_history")
    private String medicalHistory;

    /**
     * 过敏史
     */
    @TableField("allergies")
    private String allergies;

    /**
     * 疫苗接种记录（JSON）
     */
    @TableField("vaccination_records")
    private String vaccinationRecords;

    /**
     * 状态：0-正常，1-停用
     */
    @TableField("status")
    private Integer status;

    /**
     * 创建时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /**
     * 逻辑删除：0-未删除，1-已删除
     */
    @TableLogic
    @TableField("deleted")
    private Integer deleted;
}

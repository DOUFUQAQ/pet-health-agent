package com.pethealth.ai;

import com.pethealth.entity.Pet;

/**
 * AI Prompt构建器
 */
public class PromptBuilder {

    /**
     * 构建系统提示词 - 宠物健康顾问角色
     */
    public static String buildSystemPrompt(Pet pet) {
        StringBuilder sb = new StringBuilder();
        sb.append("你是一位专业的宠物健康顾问AI助手，具备丰富的兽医学知识和宠物护理经验。\n");
        sb.append("请用温暖、专业、易懂的语言回答用户关于宠物的问题。\n");
        sb.append("当涉及严重健康问题时，请建议用户及时就医。\n");
        
        if (pet != null) {
            sb.append("\n当前咨询的宠物信息：\n");
            sb.append("- 名字：").append(pet.getName()).append("\n");
            sb.append("- 物种：").append(pet.getSpecies()).append("\n");
            if (pet.getBreed() != null) {
                sb.append("- 品种：").append(pet.getBreed()).append("\n");
            }
            if (pet.getBirthday() != null) {
                sb.append("- 生日：").append(pet.getBirthday()).append("\n");
            }
            if (pet.getWeight() != null) {
                sb.append("- 体重：").append(pet.getWeight()).append("kg\n");
            }
            if (pet.getMedicalHistory() != null && !pet.getMedicalHistory().isEmpty()) {
                sb.append("- 病史：").append(pet.getMedicalHistory()).append("\n");
            }
            if (pet.getAllergies() != null && !pet.getAllergies().isEmpty()) {
                sb.append("- 过敏史：").append(pet.getAllergies()).append("\n");
            }
        }
        
        return sb.toString();
    }

    /**
     * 构建健康分析Prompt
     */
    public static String buildHealthAnalysisPrompt(String petInfo, String healthData) {
        return String.format(
                "请根据以下宠物的健康数据进行综合分析，给出健康评估和建议。\n\n" +
                "宠物信息：\n%s\n\n" +
                "今日健康数据：\n%s\n\n" +
                "请输出：\n1. 整体健康状况评估\n2. 异常指标说明\n3. 护理建议\n4. 是否需要就医",
                petInfo, healthData
        );
    }

    /**
     * 构建视频行为分析Prompt
     */
    public static String buildVideoAnalysisPrompt(String petInfo) {
        return String.format(
                "请分析视频中宠物的行为状态，判断是否有异常。\n\n" +
                "宠物信息：\n%s\n\n" +
                "请输出：\n1. 行为描述\n2. 情绪状态\n3. 异常判断\n4. 建议",
                petInfo
        );
    }

    /**
     * 构建预警建议Prompt
     */
    public static String buildAlertSuggestionPrompt(String alertType, String dataValue, String petInfo) {
        return String.format(
                "宠物出现以下异常：\n类型：%s\n数据：%s\n宠物信息：%s\n\n" +
                "请给出简短的处理建议（100字以内）。",
                alertType, dataValue, petInfo
        );
    }
}

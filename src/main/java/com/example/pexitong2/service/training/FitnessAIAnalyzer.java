package com.example.pexitong2.service.training;

import com.example.pexitong2.entity.training.StudentFitnessProfile;
import com.example.pexitong2.repository.training.StudentFitnessProfileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

/**
 * AI体能分析服务 — 纯算法实现，不依赖外部AI接口，避免过载
 * 基于国家学生体质健康标准评分规则进行本地计算
 */
@Service
public class FitnessAIAnalyzer {

    @Autowired
    private StudentFitnessProfileRepository profileRepository;

    /**
     * 对单个学生进行AI体能分析并更新档案
     */
    public StudentFitnessProfile analyzeAndUpdate(StudentFitnessProfile profile) {
        // 计算体能等级
        String level = calculateFitnessLevel(profile);
        profile.setAiFitnessLevel(level);

        // 分析优势项目
        List<String> strengths = analyzeStrengths(profile);
        profile.setAiStrengths(toJson(strengths));

        // 分析薄弱项目
        List<String> weaknesses = analyzeWeaknesses(profile);
        profile.setAiWeaknesses(toJson(weaknesses));

        // 生成训练建议
        String recommendation = generateRecommendation(profile, strengths, weaknesses);
        profile.setAiRecommendation(recommendation);

        profile.setAiAnalyzedAt(LocalDateTime.now());
        return profileRepository.save(profile);
    }

    /**
     * 综合体能等级评估 (A-E)
     * A: 优秀(90+) B: 良好(75-89) C: 及格(60-74) D: 较差(40-59) E: 差(<40)
     */
    public String calculateFitnessLevel(StudentFitnessProfile p) {
        double totalScore = 0;
        int factors = 0;

        // 体测总分权重 40%
        if (p.getTiceTotalScore() != null) {
            totalScore += p.getTiceTotalScore().doubleValue() * 0.4;
            factors++;
        }

        // 阳光跑活跃度权重 20%
        double runScore = calculateRunScore(p);
        if (runScore > 0) {
            totalScore += runScore * 0.2;
            factors++;
        }

        // 课后作业完成度权重 20%
        double homeworkScore = calculateHomeworkScore(p);
        if (homeworkScore > 0) {
            totalScore += homeworkScore * 0.2;
            factors++;
        }

        // PE积分权重 20%
        double pointsScore = calculatePointsScore(p);
        if (pointsScore > 0) {
            totalScore += pointsScore * 0.2;
            factors++;
        }

        // 如果没有任何数据，返回null
        if (factors == 0) return null;

        // 按实际参与的因子重新归一化
        double normalizedScore = totalScore / (factors > 0 ? 
            (factors == 1 ? 0.4 : factors == 2 ? 0.6 : factors == 3 ? 0.8 : 1.0) : 1.0);

        if (normalizedScore >= 90) return "A";
        if (normalizedScore >= 75) return "B";
        if (normalizedScore >= 60) return "C";
        if (normalizedScore >= 40) return "D";
        return "E";
    }

    /** 阳光跑评分(0-100) */
    private double calculateRunScore(StudentFitnessProfile p) {
        if (p.getRunTotalCount() == null || p.getRunTotalCount() == 0) return 0;
        double score = 0;
        // 跑步次数评分：>=30次满分，线性递减
        score += Math.min(p.getRunTotalCount() / 30.0, 1.0) * 40;
        // 总距离评分：>=60km满分
        if (p.getRunTotalDistance() != null) {
            score += Math.min(p.getRunTotalDistance() / 60000.0, 1.0) * 30;
        }
        // 配速评分：5-7min/km为优秀区间
        if (p.getRunAvgPace() != null && p.getRunAvgPace() > 0) {
            double pace = p.getRunAvgPace();
            if (pace <= 6.0) score += 30;
            else if (pace <= 7.0) score += 25;
            else if (pace <= 8.0) score += 20;
            else if (pace <= 9.0) score += 15;
            else score += 10;
        }
        return score;
    }

    /** 课后作业评分(0-100) */
    private double calculateHomeworkScore(StudentFitnessProfile p) {
        if (p.getHomeworkTotalCount() == null || p.getHomeworkTotalCount() == 0) return 0;
        double score = 0;
        // 完成次数评分：>=20次满分
        score += Math.min(p.getHomeworkTotalCount() / 20.0, 1.0) * 50;
        // 各项最佳成绩评分
        int bestCount = 0;
        double bestScore = 0;
        if (p.getHomeworkSquatBest() != null && p.getHomeworkSquatBest() > 0) {
            bestScore += Math.min(p.getHomeworkSquatBest() / 50.0, 1.0); bestCount++;
        }
        if (p.getHomeworkSitupBest() != null && p.getHomeworkSitupBest() > 0) {
            bestScore += Math.min(p.getHomeworkSitupBest() / 50.0, 1.0); bestCount++;
        }
        if (p.getHomeworkPushupBest() != null && p.getHomeworkPushupBest() > 0) {
            bestScore += Math.min(p.getHomeworkPushupBest() / 40.0, 1.0); bestCount++;
        }
        if (p.getHomeworkPullupBest() != null && p.getHomeworkPullupBest() > 0) {
            bestScore += Math.min(p.getHomeworkPullupBest() / 15.0, 1.0); bestCount++;
        }
        if (p.getHomeworkJumpropeBest() != null && p.getHomeworkJumpropeBest() > 0) {
            bestScore += Math.min(p.getHomeworkJumpropeBest() / 150.0, 1.0); bestCount++;
        }
        if (bestCount > 0) {
            score += (bestScore / bestCount) * 50;
        }
        return score;
    }

    /** PE积分评分(0-100) */
    private double calculatePointsScore(StudentFitnessProfile p) {
        if (p.getPeTotalPoints() == null || p.getPeTotalPoints() == 0) return 0;
        // 100积分满分
        return Math.min(p.getPeTotalPoints() / 100.0, 1.0) * 100;
    }

    /** 分析优势项目 */
    public List<String> analyzeStrengths(StudentFitnessProfile p) {
        List<String> strengths = new ArrayList<>();
        if (p.getVitalCapacity() != null && p.getVitalCapacity() >= 4000) strengths.add("肺活量优秀");
        if (p.getStandingLongJump() != null && p.getStandingLongJump().compareTo(new BigDecimal("220")) >= 0) strengths.add("爆发力强");
        if (p.getSitAndReach() != null && p.getSitAndReach().compareTo(new BigDecimal("15")) >= 0) strengths.add("柔韧性好");
        if (p.getSprint50m() != null && p.getSprint50m().compareTo(new BigDecimal("7.5")) <= 0) strengths.add("速度快");
        if (p.getLongRun() != null && p.getLongRun() <= 240) strengths.add("耐力出色");
        if (p.getPullUps() != null && p.getPullUps() >= 12) strengths.add("上肢力量强");
        if (p.getSitUps() != null && p.getSitUps() >= 45) strengths.add("核心力量好");
        if (p.getRunTotalCount() != null && p.getRunTotalCount() >= 20) strengths.add("跑步习惯良好");
        if (p.getRunBestPace() != null && p.getRunBestPace() <= 5.5) strengths.add("跑步配速优秀");
        if (p.getHomeworkTotalCount() != null && p.getHomeworkTotalCount() >= 15) strengths.add("课后训练积极");
        if (p.getPeTotalPoints() != null && p.getPeTotalPoints() >= 80) strengths.add("PE活动参与度高");
        return strengths;
    }

    /** 分析薄弱项目 */
    public List<String> analyzeWeaknesses(StudentFitnessProfile p) {
        List<String> weaknesses = new ArrayList<>();
        if (p.getVitalCapacity() != null && p.getVitalCapacity() < 2500) weaknesses.add("肺活量偏低");
        if (p.getStandingLongJump() != null && p.getStandingLongJump().compareTo(new BigDecimal("170")) < 0) weaknesses.add("爆发力不足");
        if (p.getSitAndReach() != null && p.getSitAndReach().compareTo(new BigDecimal("5")) < 0) weaknesses.add("柔韧性差");
        if (p.getSprint50m() != null && p.getSprint50m().compareTo(new BigDecimal("9.5")) > 0) weaknesses.add("速度偏慢");
        if (p.getLongRun() != null && p.getLongRun() > 330) weaknesses.add("耐力不足");
        if (p.getPullUps() != null && p.getPullUps() < 3) weaknesses.add("上肢力量薄弱");
        if (p.getSitUps() != null && p.getSitUps() < 25) weaknesses.add("核心力量不足");
        if (p.getBmi() != null) {
            double bmiVal = p.getBmi().doubleValue();
            if (bmiVal > 28) weaknesses.add("体重偏重");
            else if (bmiVal < 18.5) weaknesses.add("体重偏轻");
        }
        if (p.getRunTotalCount() != null && p.getRunTotalCount() < 5) weaknesses.add("跑步锻炼不足");
        if (p.getHomeworkTotalCount() != null && p.getHomeworkTotalCount() < 3) weaknesses.add("课后训练参与少");
        return weaknesses;
    }

    /** 生成综合训练建议 */
    public String generateRecommendation(StudentFitnessProfile p, List<String> strengths, List<String> weaknesses) {
        StringBuilder sb = new StringBuilder();
        String name = p.getRealName() != null ? p.getRealName() : "该同学";

        // 总体评价
        String level = p.getAiFitnessLevel();
        if ("A".equals(level)) {
            sb.append(name).append("体能状况优秀，建议保持现有训练强度，可适当挑战更高目标。");
        } else if ("B".equals(level)) {
            sb.append(name).append("体能状况良好，有一定基础，建议针对薄弱项进行专项提升。");
        } else if ("C".equals(level)) {
            sb.append(name).append("体能状况一般，建议增加训练频率，重点关注薄弱环节。");
        } else if ("D".equals(level)) {
            sb.append(name).append("体能状况较差，建议从基础训练开始，循序渐进提升体能。");
        } else {
            sb.append(name).append("体能状况需要重点关注，建议制定系统训练计划，逐步改善。");
        }

        // 针对薄弱项的具体建议
        if (!weaknesses.isEmpty()) {
            sb.append("\n\n重点改善方向：");
            for (String w : weaknesses) {
                sb.append("\n• ").append(getTrainingSuggestion(w));
            }
        }

        // 优势项鼓励
        if (!strengths.isEmpty()) {
            sb.append("\n\n优势保持：").append(String.join("、", strengths)).append("，继续保持。");
        }

        return sb.toString();
    }

    /** 针对具体薄弱项的训练建议 */
    private String getTrainingSuggestion(String weakness) {
        return switch (weakness) {
            case "肺活量偏低" -> "肺活量：建议每天进行深呼吸训练，增加有氧运动（慢跑、游泳），每周3-4次，每次30分钟以上";
            case "爆发力不足" -> "爆发力：建议进行蛙跳、立定跳远练习，每周3次，配合深蹲训练增强下肢力量";
            case "柔韧性差" -> "柔韧性：建议每天进行拉伸训练15-20分钟，重点关注腿部和腰背部柔韧性";
            case "速度偏慢" -> "速度：建议进行间歇跑训练（50米冲刺+慢走恢复），每周2-3次，提升爆发速度";
            case "耐力不足" -> "耐力：建议从慢跑开始，逐步增加距离和时间，目标每周跑步3次，每次2-3公里";
            case "上肢力量薄弱" -> "上肢力量：建议从辅助引体向上开始，配合俯卧撑训练，每天3组，逐步增加次数";
            case "核心力量不足" -> "核心力量：建议进行平板支撑、仰卧起坐训练，每天3组，每组15-20个";
            case "体重偏重" -> "体重管理：建议增加有氧运动频率，控制饮食，每周运动4-5次";
            case "体重偏轻" -> "体重管理：建议增加力量训练，合理增加营养摄入，保证充足睡眠";
            case "跑步锻炼不足" -> "跑步习惯：建议每周至少进行2次阳光跑，从2公里开始逐步增加";
            case "课后训练参与少" -> "课后训练：建议每周完成2-3次课后作业训练，养成规律运动习惯";
            default -> weakness + "：建议加强相关训练";
        };
    }

    /** 生成班级AI任务建议（教师发布任务时的AI参考） */
    public String generateTaskSuggestion(List<StudentFitnessProfile> classProfiles, String trainingType) {
        if (classProfiles.isEmpty()) return "暂无学生档案数据，建议先让学生上传体测数据建立档案。";

        StringBuilder sb = new StringBuilder();
        sb.append("基于").append(classProfiles.size()).append("名学生的体能档案分析：\n\n");

        // 统计班级整体水平
        Map<String, Integer> levelDist = new HashMap<>();
        List<String> allWeaknesses = new ArrayList<>();
        for (StudentFitnessProfile p : classProfiles) {
            if (p.getAiFitnessLevel() != null) {
                levelDist.merge(p.getAiFitnessLevel(), 1, Integer::sum);
            }
            if (p.getAiWeaknesses() != null) {
                allWeaknesses.addAll(parseJson(p.getAiWeaknesses()));
            }
        }

        // 体能等级分布
        sb.append("【班级体能等级分布】\n");
        for (String l : List.of("A", "B", "C", "D", "E")) {
            int count = levelDist.getOrDefault(l, 0);
            if (count > 0) sb.append(l).append("级: ").append(count).append("人  ");
        }

        // 找出最常见的薄弱项
        Map<String, Integer> weaknessCount = new HashMap<>();
        for (String w : allWeaknesses) {
            weaknessCount.merge(w, 1, Integer::sum);
        }
        List<Map.Entry<String, Integer>> sortedWeaknesses = new ArrayList<>(weaknessCount.entrySet());
        sortedWeaknesses.sort((a, b) -> b.getValue() - a.getValue());

        if (!sortedWeaknesses.isEmpty()) {
            sb.append("\n\n【班级共性薄弱项】\n");
            int limit = Math.min(3, sortedWeaknesses.size());
            for (int i = 0; i < limit; i++) {
                var entry = sortedWeaknesses.get(i);
                sb.append("• ").append(entry.getKey()).append(" (").append(entry.getValue()).append("人)\n");
            }
        }

        // 根据训练类型给出建议
        sb.append("\n【建议训练内容】\n");
        if (trainingType != null) {
            sb.append(getTypeSpecificSuggestion(trainingType, sortedWeaknesses));
        } else {
            sb.append("建议根据班级薄弱项安排针对性训练，优先改善共性问题。");
        }

        return sb.toString();
    }

    private String getTypeSpecificSuggestion(String type, List<Map.Entry<String, Integer>> weaknesses) {
        return switch (type) {
            case "endurance" -> "耐力训练建议：\n• 慢跑2-3公里，配速控制在6-8min/km\n• 间歇跑训练：400米快跑+200米慢走，重复4-6组\n• 建议难度根据班级C/D级学生比例调整";
            case "strength" -> "力量训练建议：\n• 俯卧撑3组×15个，仰卧起坐3组×20个\n• 深蹲3组×20个，平板支撑3组×30秒\n• 引体向上根据个人能力分层要求";
            case "flexibility" -> "柔韧训练建议：\n• 坐位体前屈拉伸练习，每次保持15秒\n• 全身动态拉伸15分钟\n• 瑜伽基础动作练习";
            case "skill" -> "技能训练建议：\n• 根据课程内容安排专项技能练习\n• 分组对抗或配合练习\n• 注意动作规范性指导";
            default -> "综合训练建议：\n• 热身10分钟 + 专项训练30分钟 + 放松拉伸10分钟\n• 根据班级整体水平调整训练强度";
        };
    }

    // JSON工具方法
    private String toJson(List<String> list) {
        if (list == null || list.isEmpty()) return "[]";
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < list.size(); i++) {
            if (i > 0) sb.append(",");
            sb.append("\"").append(list.get(i).replace("\"", "\\\"")).append("\"");
        }
        sb.append("]");
        return sb.toString();
    }

    private List<String> parseJson(String json) {
        if (json == null || json.isEmpty() || "[]".equals(json)) return new ArrayList<>();
        List<String> result = new ArrayList<>();
        String content = json.substring(1, json.length() - 1);
        String[] parts = content.split(",");
        for (String part : parts) {
            String trimmed = part.trim();
            if (trimmed.startsWith("\"") && trimmed.endsWith("\"")) {
                result.add(trimmed.substring(1, trimmed.length() - 1));
            }
        }
        return result;
    }
}

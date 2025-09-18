package com.example.pexitong2.dto.pe;

/**
 * PE积分指标设置请求
 */
public class PeTargetRequest {
    
    private Integer weeklyTarget;   // 周积分指标
    private Integer monthlyTarget;  // 月积分指标
    private Integer totalTarget;    // 总积分指标
    
    // 构造函数
    public PeTargetRequest() {}
    
    public PeTargetRequest(Integer weeklyTarget, Integer monthlyTarget, Integer totalTarget) {
        this.weeklyTarget = weeklyTarget;
        this.monthlyTarget = monthlyTarget;
        this.totalTarget = totalTarget;
    }
    
    // Getters and Setters
    public Integer getWeeklyTarget() { return weeklyTarget; }
    public void setWeeklyTarget(Integer weeklyTarget) { this.weeklyTarget = weeklyTarget; }
    
    public Integer getMonthlyTarget() { return monthlyTarget; }
    public void setMonthlyTarget(Integer monthlyTarget) { this.monthlyTarget = monthlyTarget; }
    
    public Integer getTotalTarget() { return totalTarget; }
    public void setTotalTarget(Integer totalTarget) { this.totalTarget = totalTarget; }
}



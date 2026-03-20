package com.example.pexitong2.dto.race;

import java.util.List;

/**
 * 上传成绩接口响应体中的 data 部分
 */
public class RaceResultUploadResponse {

    private int uploadedCount;
    private int failedCount;
    private List<FailedItem> failedItems;

    public RaceResultUploadResponse() {}

    public RaceResultUploadResponse(int uploadedCount, int failedCount, List<FailedItem> failedItems) {
        this.uploadedCount = uploadedCount;
        this.failedCount   = failedCount;
        this.failedItems   = failedItems;
    }

    public int getUploadedCount() { return uploadedCount; }
    public void setUploadedCount(int uploadedCount) { this.uploadedCount = uploadedCount; }

    public int getFailedCount() { return failedCount; }
    public void setFailedCount(int failedCount) { this.failedCount = failedCount; }

    public List<FailedItem> getFailedItems() { return failedItems; }
    public void setFailedItems(List<FailedItem> failedItems) { this.failedItems = failedItems; }

    /**
     * 失败条目详情
     */
    public static class FailedItem {
        private String studentNumber;
        private String reason;

        public FailedItem() {}

        public FailedItem(String studentNumber, String reason) {
            this.studentNumber = studentNumber;
            this.reason        = reason;
        }

        public String getStudentNumber() { return studentNumber; }
        public void setStudentNumber(String studentNumber) { this.studentNumber = studentNumber; }

        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }
    }
}

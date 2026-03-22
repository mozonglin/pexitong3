package com.example.pexitong2.dto.venue;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class VenueReservationRequest {

    @NotBlank(message = "场馆ID不能为空")
    @JsonProperty("venue_id")
    private String venueId;

    private String purpose;

    @NotBlank(message = "预约日期不能为空")
    @JsonProperty("reservation_date")
    private String reservationDate;

    @NotBlank(message = "开始时间不能为空")
    @JsonProperty("start_time")
    private String startTime;

    @NotBlank(message = "结束时间不能为空")
    @JsonProperty("end_time")
    private String endTime;

    @JsonProperty("people_count")
    private Integer peopleCount = 1;

    private String remark;

    @JsonProperty("booker_phone")
    private String bookerPhone;

    public String getVenueId() { return venueId; }
    public void setVenueId(String venueId) { this.venueId = venueId; }

    public String getPurpose() { return purpose; }
    public void setPurpose(String purpose) { this.purpose = purpose; }

    public String getReservationDate() { return reservationDate; }
    public void setReservationDate(String reservationDate) { this.reservationDate = reservationDate; }

    public String getStartTime() { return startTime; }
    public void setStartTime(String startTime) { this.startTime = startTime; }

    public String getEndTime() { return endTime; }
    public void setEndTime(String endTime) { this.endTime = endTime; }

    public Integer getPeopleCount() { return peopleCount; }
    public void setPeopleCount(Integer peopleCount) { this.peopleCount = peopleCount; }

    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }

    public String getBookerPhone() { return bookerPhone; }
    public void setBookerPhone(String bookerPhone) { this.bookerPhone = bookerPhone; }
}

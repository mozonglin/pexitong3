package com.example.pexitong2.dto.venue;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public class VenueRequest {

    @NotBlank(message = "场馆名称不能为空")
    private String name;

    @NotBlank(message = "场馆类型不能为空")
    private String type;

    @NotNull(message = "容量不能为空")
    @Min(value = 1, message = "容量至少为1")
    private Integer capacity;

    private String location;

    private BigDecimal price;

    @JsonProperty("open_time")
    private String openTime;

    private String description;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public Integer getCapacity() { return capacity; }
    public void setCapacity(Integer capacity) { this.capacity = capacity; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public String getOpenTime() { return openTime; }
    public void setOpenTime(String openTime) { this.openTime = openTime; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}

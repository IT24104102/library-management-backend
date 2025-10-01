package com.hettiarachchi.roomservice.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import lombok.Data;

@Component
@ConfigurationProperties(prefix = "room-booking")
@Data
public class BookingConfigurationProperties {
    
    private Integer maxDurationHours = 4;
    private Integer maxDailyBookings = 2;
    private Integer maxWeeklyBookings = 5;
    private Integer advanceBookingDays = 30;
}
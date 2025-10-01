package com.hettiarachchi.roomservice.constants;

public class BookingConstants {
    
    // Business Rules Configuration
    public static final int MAX_DURATION_HOURS = 4;
    public static final int MAX_DAILY_BOOKINGS = 2;
    public static final int MAX_WEEKLY_BOOKINGS = 5;
    public static final int ADVANCE_BOOKING_DAYS = 30;
    
    // Business Hours Configuration
    public static final int BUSINESS_START_HOUR = 8;
    public static final int BUSINESS_START_MINUTE = 0;
    public static final int BUSINESS_END_HOUR = 18;
    public static final int BUSINESS_END_MINUTE = 0;
    
    // Time Slot Configuration
    public static final int SLOT_DURATION_MINUTES = 60;
    
    // Status Messages
    public static final String BOOKING_CREATED_MESSAGE = "Booking request submitted successfully and is pending approval";
    public static final String BOOKING_APPROVED_MESSAGE = "Booking has been approved";
    public static final String BOOKING_REJECTED_MESSAGE = "Booking has been rejected";
    public static final String BOOKING_CANCELLED_MESSAGE = "Booking has been cancelled";
    
    // Error Messages
    public static final String ROOM_NOT_FOUND = "Room not found";
    public static final String BOOKING_NOT_FOUND = "Booking not found";
    public static final String USER_NOT_FOUND = "User not found";
    public static final String BOOKING_CONFLICT = "The requested time slot conflicts with existing bookings";
    public static final String INVALID_TIME_SLOT = "Start time must be before end time";
    public static final String PAST_DATE_BOOKING = "Cannot book rooms for past dates";
    public static final String ADVANCE_BOOKING_LIMIT = "Cannot book rooms more than " + ADVANCE_BOOKING_DAYS + " days in advance";
    public static final String DURATION_LIMIT_EXCEEDED = "Booking duration cannot exceed " + MAX_DURATION_HOURS + " hours";
    public static final String DAILY_LIMIT_EXCEEDED = "Daily booking limit of " + MAX_DAILY_BOOKINGS + " bookings exceeded";
    public static final String WEEKLY_LIMIT_EXCEEDED = "Weekly booking limit of " + MAX_WEEKLY_BOOKINGS + " bookings exceeded";
    public static final String BUSINESS_HOURS_VIOLATION = "Bookings are only allowed between 8:00 AM and 6:00 PM";
    
    private BookingConstants() {
        // Prevent instantiation
    }
}
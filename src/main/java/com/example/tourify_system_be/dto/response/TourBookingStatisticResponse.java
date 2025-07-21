package com.example.tourify_system_be.dto.response;

public class TourBookingStatisticResponse {
    private String tourId;
    private String tourName;
    private long bookingCount;

    public TourBookingStatisticResponse() {}

    public TourBookingStatisticResponse(String tourId, String tourName, long bookingCount) {
        this.tourId = tourId;
        this.tourName = tourName;
        this.bookingCount = bookingCount;
    }

    // getters & setters
    public String getTourId() { return tourId; }
    public void setTourId(String tourId) { this.tourId = tourId; }

    public String getTourName() { return tourName; }
    public void setTourName(String tourName) { this.tourName = tourName; }

    public long getBookingCount() { return bookingCount; }
    public void setBookingCount(long bookingCount) { this.bookingCount = bookingCount; }
}

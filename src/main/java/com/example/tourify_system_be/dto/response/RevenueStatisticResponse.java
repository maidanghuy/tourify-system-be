package com.example.tourify_system_be.dto.response;

public class RevenueStatisticResponse {
    private String time;         // yyyy-MM-dd, yyyy-MM, hoặc yyyy
    private Long totalRevenue;   // tổng doanh thu trong khoảng time
    private String companyId;    // subCompanyId hoặc null nếu toàn hệ thống
    private String companyName;  // tên công ty hoặc null nếu toàn hệ thống

    public RevenueStatisticResponse() {}

    public RevenueStatisticResponse(String time, Long totalRevenue, String companyId, String companyName) {
        this.time = time;
        this.totalRevenue = totalRevenue;
        this.companyId = companyId;
        this.companyName = companyName;
    }

    // getters & setters
    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }

    public Long getTotalRevenue() { return totalRevenue; }
    public void setTotalRevenue(Long totalRevenue) { this.totalRevenue = totalRevenue; }

    public String getCompanyId() { return companyId; }
    public void setCompanyId(String companyId) { this.companyId = companyId; }

    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }
}

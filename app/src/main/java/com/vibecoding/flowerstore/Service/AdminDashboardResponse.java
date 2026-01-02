package com.vibecoding.flowerstore.Service;

import java.util.Map;

public class AdminDashboardResponse {
    private int totalUsers;
    private int totalShops;
    private int pendingShopRequests;
    private int totalOrders;
    private double totalRevenue;
    private Map<String, Double> monthlyRevenueChartData;
    private int pendingAppealsCount;

    public int getTotalUsers() {
        return totalUsers;
    }

    public void setTotalUsers(int totalUsers) {
        this.totalUsers = totalUsers;
    }

    public int getTotalShops() {
        return totalShops;
    }

    public void setTotalShops(int totalShops) {
        this.totalShops = totalShops;
    }

    public int getPendingShopRequests() {
        return pendingShopRequests;
    }

    public void setPendingShopRequests(int pendingShopRequests) {
        this.pendingShopRequests = pendingShopRequests;
    }

    public int getTotalOrders() {
        return totalOrders;
    }

    public void setTotalOrders(int totalOrders) {
        this.totalOrders = totalOrders;
    }

    public double getTotalRevenue() {
        return totalRevenue;
    }

    public void setTotalRevenue(double totalRevenue) {
        this.totalRevenue = totalRevenue;
    }

    public Map<String, Double> getMonthlyRevenueChartData() {
        return monthlyRevenueChartData;
    }

    public void setMonthlyRevenueChartData(Map<String, Double> monthlyRevenueChartData) {
        this.monthlyRevenueChartData = monthlyRevenueChartData;
    }

    public int getPendingAppealsCount() {
        return pendingAppealsCount;
    }

    public void setPendingAppealsCount(int pendingAppealsCount) {
        this.pendingAppealsCount = pendingAppealsCount;
    }
}

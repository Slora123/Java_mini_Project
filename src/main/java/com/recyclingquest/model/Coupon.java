package com.recyclingquest.model;
import java.time.LocalDate;
public class Coupon {
    private int id;
    private String levelRequired;
    private String companyName;
    private String description;
    private String discount;
    private String couponCode;
    private LocalDate expiryDate;
    private boolean isClaimed;
    private String logoUrl;
    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }
    public String getLevelRequired() {
        return levelRequired;
    }
    public void setLevelRequired(String levelRequired) {
        this.levelRequired = levelRequired;
    }
    public String getCompanyName() {
        return companyName;
    }
    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    public String getDiscount() {
        return discount;
    }
    public void setDiscount(String discount) {
        this.discount = discount;
    }
    public String getCouponCode() {
        return couponCode;
    }
    public void setCouponCode(String couponCode) {
        this.couponCode = couponCode;
    }
    public LocalDate getExpiryDate() {
        return expiryDate;
    }
    public void setExpiryDate(LocalDate expiryDate) {
        this.expiryDate = expiryDate;
    }
    public boolean isClaimed() {
        return isClaimed;
    }
    public void setClaimed(boolean claimed) {
        isClaimed = claimed;
    }
    public String getLogoUrl() {
        return logoUrl;
    }
    public void setLogoUrl(String logoUrl) {
        this.logoUrl = logoUrl;
    }
}



package com.recyclingquest.model;

import java.util.ArrayList;
import java.util.List;

public class User {
    private long id;
    private String nickname;
    private String avatar;
    private String city;
    private int points;
    private String email;
    private String password;
    private List<Coupon> coupons = new ArrayList<>();

    public User() {
    }

    public User(long id, String nickname, String avatar, String city, int points, String email, String password) {
        this.id = id;
        this.nickname = nickname;
        this.avatar = avatar;
        this.city = city;
        this.points = points;
        this.email = email;
        this.password = password;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public List<Coupon> getCoupons() {
        return coupons;
    }

    public void setCoupons(List<Coupon> coupons) {
        this.coupons = coupons != null ? coupons : new ArrayList<>();
    }

    public void addCoupon(Coupon c) {
        if (c != null) {
            this.coupons.add(c);
        }
    }

    public List<Coupon> showCoupons() {
        return new ArrayList<>(this.coupons);
    }
}

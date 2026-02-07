package com.recyclingquest.dao;

import com.recyclingquest.db.Database;
import com.recyclingquest.model.Coupon;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CouponDao {
    private final Database db;

    public CouponDao(Database db) { this.db = db; }

    public Optional<Coupon> getCouponByLevel(String level) {
        String sql = "SELECT id, level_required, company_name, description, discount, coupon_code, expiry_date, is_claimed, logo_url FROM coupons WHERE level_required = ? AND is_claimed = 0 LIMIT 1";
        try (Connection c = db.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, level);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(map(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return Optional.empty();
    }

    public Optional<Coupon> getNextUnlockableCoupon(int currentPoints) {
        String sql = "SELECT id, level_required, company_name, description, discount, coupon_code, expiry_date, is_claimed, logo_url FROM coupons WHERE CAST(level_required AS INTEGER) > ? AND is_claimed = 0 ORDER BY CAST(level_required AS INTEGER) ASC LIMIT 1";
        try (Connection c = db.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, currentPoints);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(map(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return Optional.empty();
    }

    public void updateCoupon(Coupon coupon) {
        String sql = "UPDATE coupons SET level_required=?, company_name=?, description=?, discount=?, coupon_code=?, expiry_date=?, is_claimed=?, logo_url=? WHERE id=?";
        try (Connection c = db.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, coupon.getLevelRequired());
            ps.setString(2, coupon.getCompanyName());
            ps.setString(3, coupon.getDescription());
            ps.setString(4, coupon.getDiscount());
            ps.setString(5, coupon.getCouponCode());
            ps.setString(6, coupon.getExpiryDate() != null ? coupon.getExpiryDate().toString() : null);
            ps.setBoolean(7, coupon.isClaimed());
            ps.setString(8, coupon.getLogoUrl());
            ps.setInt(9, coupon.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Optional<Coupon> getCouponByPoints(int points) {
        // Check if user has enough points
        if (points < 20) {
            return Optional.empty();
        }
        
        // Try to find a coupon matching the description
        String sql = "SELECT id, level_required, company_name, description, discount, coupon_code, expiry_date, is_claimed, logo_url FROM coupons WHERE description LIKE ? AND is_claimed = 0 LIMIT 1";
        try (Connection c = db.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            String searchTerm = "%" + getCouponDescriptionForPoints(points) + "%";
            ps.setString(1, searchTerm);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(map(rs));
                }
            }
        } catch (SQLException e) {
            // If not found by description, create a virtual coupon
        }
        
        // Return a virtual coupon based on points
        return Optional.of(createVirtualCoupon(points));
    }

    private String getCouponDescriptionForPoints(int points) {
        if (points >= 400) return "30% off on eco products";
        else if (points >= 250) return "Plant a Tree Certificate";
        else if (points >= 150) return "20% off LED Bulb";
        else if (points >= 100) return "Free Eco Tote Bag";
        else if (points >= 70) return "15% off Recycled T-shirt";
        else if (points >= 40) return "10% off Eco Toiletries";
        else if (points >= 20) return "5% off Recycled Stationery";
        else return "";
    }

    private Coupon createVirtualCoupon(int points) {
        Coupon c = new Coupon();
        c.setId(-1); // Virtual coupon, not in DB
        if (points >= 400) {
            c.setDescription("30% off on eco products");
            c.setCouponCode("ECO30-" + System.currentTimeMillis() % 10000);
            c.setDiscount("30% OFF");
        } else if (points >= 250) {
            c.setDescription("Plant a Tree Certificate");
            c.setCouponCode("TREE-" + System.currentTimeMillis() % 10000);
            c.setDiscount("Certificate");
        } else if (points >= 150) {
            c.setDescription("20% off LED Bulb");
            c.setCouponCode("LED20-" + System.currentTimeMillis() % 10000);
            c.setDiscount("20% OFF");
        } else if (points >= 100) {
            c.setDescription("Free Eco Tote Bag");
            c.setCouponCode("TOTE-" + System.currentTimeMillis() % 10000);
            c.setDiscount("FREE");
        } else if (points >= 70) {
            c.setDescription("15% off Recycled T-shirt");
            c.setCouponCode("TSHIRT15-" + System.currentTimeMillis() % 10000);
            c.setDiscount("15% OFF");
        } else if (points >= 40) {
            c.setDescription("10% off Eco Toiletries");
            c.setCouponCode("TOIL10-" + System.currentTimeMillis() % 10000);
            c.setDiscount("10% OFF");
        } else if (points >= 20) {
            c.setDescription("5% off Recycled Stationery");
            c.setCouponCode("STAT5-" + System.currentTimeMillis() % 10000);
            c.setDiscount("5% OFF");
        }
        c.setCompanyName("Eco Rewards");
        c.setExpiryDate(java.time.LocalDate.now().plusMonths(6));
        return c;
    }

    public void redeemCouponForUser(long userId, Coupon coupon) {
        if (coupon.getId() == -1) {
            // Virtual coupon: persist it into coupons first to satisfy FK, then record redemption
            String insertCoupon = "INSERT INTO coupons(level_required, company_name, description, discount, coupon_code, expiry_date, is_claimed, logo_url) VALUES(?,?,?,?,?,?,1,?)";
            String insertUserCoupon = "INSERT INTO user_coupons (user_id, coupon_id, redeemed_at) VALUES (?, ?, ?)";
            try (Connection c = db.getConnection()) {
                // Insert coupon and get generated id
                try (PreparedStatement ps = c.prepareStatement(insertCoupon, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setString(1, coupon.getLevelRequired() != null ? coupon.getLevelRequired() : "Virtual");
                    ps.setString(2, coupon.getCompanyName() != null ? coupon.getCompanyName() : "Eco Rewards");
                    ps.setString(3, coupon.getDescription());
                    ps.setString(4, coupon.getDiscount());
                    ps.setString(5, coupon.getCouponCode());
                    ps.setString(6, coupon.getExpiryDate() != null ? coupon.getExpiryDate().toString() : null);
                    ps.setString(7, coupon.getLogoUrl());
                    ps.executeUpdate();
                    try (ResultSet rs = ps.getGeneratedKeys()) {
                        if (rs.next()) {
                            int newId = rs.getInt(1);
                            coupon.setId(newId);
                        }
                    }
                }
                // Record redemption
                try (PreparedStatement ps2 = c.prepareStatement(insertUserCoupon)) {
                    ps2.setLong(1, userId);
                    ps2.setInt(2, coupon.getId());
                    ps2.setLong(3, System.currentTimeMillis());
                    ps2.executeUpdate();
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        } else {
            // Real coupon - mark as claimed and record redemption
            String sql1 = "UPDATE coupons SET is_claimed = 1 WHERE id = ?";
            String sql2 = "INSERT INTO user_coupons (user_id, coupon_id, redeemed_at) VALUES (?, ?, ?)";
            try (Connection c = db.getConnection()) {
                try (PreparedStatement ps1 = c.prepareStatement(sql1)) {
                    ps1.setInt(1, coupon.getId());
                    ps1.executeUpdate();
                }
                try (PreparedStatement ps2 = c.prepareStatement(sql2)) {
                    ps2.setLong(1, userId);
                    ps2.setInt(2, coupon.getId());
                    ps2.setLong(3, System.currentTimeMillis());
                    ps2.executeUpdate();
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public boolean hasUserRedeemedCoupon(long userId, String couponCode) {
        String sql = "SELECT COUNT(1) FROM user_coupons uc JOIN coupons c ON uc.coupon_id = c.id WHERE uc.user_id = ? AND c.coupon_code = ?";
        try (Connection c = db.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, userId);
            ps.setString(2, couponCode);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            // For virtual coupons, check by code pattern
            return false;
        }
        return false;
    }

    public List<Coupon> getAllAvailableCoupons() {
        List<Coupon> coupons = new ArrayList<>();
        String sql = "SELECT id, level_required, company_name, description, discount, coupon_code, expiry_date, is_claimed, logo_url FROM coupons WHERE is_claimed = 0 ORDER BY id";
        try (Connection c = db.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    coupons.add(map(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return coupons;
    }

    public List<Coupon> getUserRedeemedCoupons(long userId) {
        List<Coupon> coupons = new ArrayList<>();
        String sql = "SELECT c.id, c.level_required, c.company_name, c.description, c.discount, c.coupon_code, c.expiry_date, c.is_claimed, c.logo_url " +
                     "FROM coupons c " +
                     "JOIN user_coupons uc ON c.id = uc.coupon_id " +
                     "WHERE uc.user_id = ? " +
                     "ORDER BY uc.redeemed_at DESC";
        try (Connection c = db.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    coupons.add(map(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return coupons;
    }

    public List<Coupon> awardEligibleCoupons(long userId, int totalPoints) {
        List<Coupon> newlyAwarded = new ArrayList<>();
        
        // Define all coupon thresholds
        int[] thresholds = {20, 40, 70, 100, 150, 250, 400};
        String[] descriptions = {
            "5% off Recycled Stationery",
            "10% off Eco Toiletries", 
            "15% off Recycled T-shirt",
            "Free Eco Tote Bag",
            "20% off LED Bulb",
            "Plant a Tree Certificate",
            "30% off on eco products"
        };
        
        // Check each threshold
        for (int i = 0; i < thresholds.length; i++) {
            if (totalPoints >= thresholds[i]) {
                // Check if user already has this coupon
                String checkSql = "SELECT COUNT(1) FROM user_coupons uc JOIN coupons c ON uc.coupon_id = c.id " +
                                "WHERE uc.user_id = ? AND c.description LIKE ?";
                
                try (Connection c = db.getConnection(); PreparedStatement ps = c.prepareStatement(checkSql)) {
                    ps.setLong(1, userId);
                    ps.setString(2, "%" + descriptions[i] + "%");
                    
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next() && rs.getInt(1) == 0) {
                            // User doesn't have this coupon, award it
                            Optional<Coupon> couponOpt = getCouponByPoints(thresholds[i]);
                            if (couponOpt.isPresent()) {
                                Coupon coupon = couponOpt.get();
                                redeemCouponForUser(userId, coupon);
                                newlyAwarded.add(coupon);
                            }
                        }
                    }
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        
        return newlyAwarded;
    }

    private Coupon map(ResultSet rs) throws SQLException {
        Coupon c = new Coupon();
        c.setId(rs.getInt("id"));
        c.setLevelRequired(rs.getString("level_required"));
        c.setCompanyName(rs.getString("company_name"));
        c.setDescription(rs.getString("description"));
        c.setDiscount(rs.getString("discount"));
        c.setCouponCode(rs.getString("coupon_code"));
        String date = rs.getString("expiry_date");
        c.setExpiryDate(date != null ? LocalDate.parse(date) : null);
        c.setClaimed(rs.getBoolean("is_claimed"));
        c.setLogoUrl(rs.getString("logo_url"));
        return c;
    }
}



package io.mastercoding.vssmart.data.model;

import com.google.firebase.firestore.ServerTimestamp;
import io.mastercoding.vssmart.utils.Constants;
import java.io.Serializable;
import java.util.Date;

/**
 * Product Listing Model representing a pre-owned item on the campus marketplace.
 */
public class ListingModel implements Serializable {

    public static final String STATUS_AVAILABLE = "AVAILABLE";
    public static final String STATUS_SOLD = "SOLD";

    private String id;
    private String sellerId;
    private String sellerName;
    private String title;
    private String description;
    private double price;
    private double originalPrice;
    private String category;
    private String condition;
    private String hostel;
    private String roomNo;
    private String whatsappPhone;
    private String imageUrl;
    private String photoUrl;
    private String imageUri;
    private String image;
    private boolean isSold;
    private String status = STATUS_AVAILABLE;
    private boolean isAvailable = true;

    @ServerTimestamp
    private Date soldAt;

    @ServerTimestamp
    private Date createdAt;

    @ServerTimestamp
    private Date timestamp;

    // Required empty constructor for Firestore
    public ListingModel() {
    }

    public ListingModel(String id, String sellerId, String sellerName, String title, String description,
                        double price, double originalPrice, String category, String condition,
                        String hostel, String roomNo, String whatsappPhone, String imageUrl, boolean isSold) {
        this.id = id;
        this.sellerId = sellerId;
        this.sellerName = sellerName;
        this.title = title;
        this.description = description;
        this.price = price;
        this.originalPrice = originalPrice;
        this.category = category;
        this.condition = condition;
        this.hostel = hostel;
        this.roomNo = roomNo;
        this.whatsappPhone = whatsappPhone;
        this.imageUrl = imageUrl;
        this.isSold = isSold;
        this.status = isSold ? STATUS_SOLD : STATUS_AVAILABLE;
        this.isAvailable = !isSold;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @com.google.firebase.firestore.PropertyName("sellerId")
    public String getSellerId() {
        return sellerId != null ? sellerId : "";
    }

    @com.google.firebase.firestore.PropertyName("sellerId")
    public void setSellerId(String sellerId) {
        this.sellerId = sellerId;
    }

    @com.google.firebase.firestore.PropertyName("sellerUid")
    public String getSellerUid() {
        return getSellerId();
    }

    @com.google.firebase.firestore.PropertyName("sellerUid")
    public void setSellerUid(String sellerUid) {
        if (sellerUid != null && !sellerUid.isEmpty()) {
            this.sellerId = sellerUid;
        }
    }

    public String getSellerName() {
        return sellerName != null ? sellerName : "Campus Student";
    }

    public void setSellerName(String sellerName) {
        this.sellerName = sellerName;
    }

    public String getTitle() {
        return title != null ? title : "";
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description != null ? description : "";
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public double getOriginalPrice() {
        return originalPrice;
    }

    public void setOriginalPrice(double originalPrice) {
        this.originalPrice = originalPrice;
    }

    public String getCategory() {
        return category != null ? category : "Other";
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getCondition() {
        return condition != null ? condition : "Good";
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    public String getHostel() {
        return hostel != null ? hostel : "";
    }

    public void setHostel(String hostel) {
        this.hostel = hostel;
    }

    public String getRoomNo() {
        return roomNo != null ? roomNo : "";
    }

    public void setRoomNo(String roomNo) {
        this.roomNo = roomNo;
    }

    public String getWhatsappPhone() {
        return whatsappPhone != null ? whatsappPhone : "";
    }

    public void setWhatsappPhone(String whatsappPhone) {
        this.whatsappPhone = whatsappPhone;
    }

    @com.google.firebase.firestore.PropertyName("imageUrl")
    public String getImageUrl() {
        return imageUrl;
    }

    @com.google.firebase.firestore.PropertyName("imageUrl")
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    @com.google.firebase.firestore.PropertyName("photoUrl")
    public String getPhotoUrl() {
        return photoUrl;
    }

    @com.google.firebase.firestore.PropertyName("photoUrl")
    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    @com.google.firebase.firestore.PropertyName("imageUri")
    public String getImageUri() {
        return imageUri;
    }

    @com.google.firebase.firestore.PropertyName("imageUri")
    public void setImageUri(String imageUri) {
        this.imageUri = imageUri;
    }

    @com.google.firebase.firestore.PropertyName("image")
    public String getImage() {
        return image;
    }

    @com.google.firebase.firestore.PropertyName("image")
    public void setImage(String image) {
        this.image = image;
    }

    private boolean isBanned = false;

    @com.google.firebase.firestore.PropertyName("isBanned")
    public boolean getIsBanned() {
        return isBanned || Constants.STATUS_REMOVED_BY_ADMIN.equalsIgnoreCase(status) || "BANNED".equalsIgnoreCase(status);
    }

    @com.google.firebase.firestore.PropertyName("isBanned")
    public void setIsBanned(boolean banned) {
        this.isBanned = banned;
    }

    @com.google.firebase.firestore.PropertyName("banned")
    public boolean isBanned() {
        return isBanned || Constants.STATUS_REMOVED_BY_ADMIN.equalsIgnoreCase(status) || "BANNED".equalsIgnoreCase(status);
    }

    @com.google.firebase.firestore.PropertyName("banned")
    public void setBanned(boolean banned) {
        this.isBanned = banned;
    }

    public String resolveImageUrl() {
        if (imageUrl != null && !imageUrl.trim().isEmpty()) return imageUrl.trim();
        if (photoUrl != null && !photoUrl.trim().isEmpty()) return photoUrl.trim();
        if (imageUri != null && !imageUri.trim().isEmpty()) return imageUri.trim();
        if (image != null && !image.trim().isEmpty()) return image.trim();
        return null;
    }

    public String getStatus() {
        return status != null ? status : (isSold ? STATUS_SOLD : STATUS_AVAILABLE);
    }

    public void setStatus(String status) {
        this.status = status;
        if (STATUS_SOLD.equalsIgnoreCase(status)) {
            this.isSold = true;
            this.isAvailable = false;
        } else if (STATUS_AVAILABLE.equalsIgnoreCase(status)) {
            this.isSold = false;
            this.isAvailable = true;
        }
    }

    public boolean isAvailable() {
        return isAvailable && !isSold && !STATUS_SOLD.equalsIgnoreCase(status);
    }

    public void setAvailable(boolean available) {
        this.isAvailable = available;
        this.isSold = !available;
        this.status = available ? STATUS_AVAILABLE : STATUS_SOLD;
    }

    @com.google.firebase.firestore.PropertyName("sold")
    public boolean isSold() {
        return isSold || STATUS_SOLD.equalsIgnoreCase(status) || !isAvailable;
    }

    @com.google.firebase.firestore.PropertyName("sold")
    public void setSold(boolean sold) {
        this.isSold = sold;
        this.isAvailable = !sold;
        this.status = sold ? STATUS_SOLD : STATUS_AVAILABLE;
    }

    @com.google.firebase.firestore.PropertyName("isSold")
    public boolean getIsSold() {
        return isSold();
    }

    @com.google.firebase.firestore.PropertyName("isSold")
    public void setIsSold(boolean isSold) {
        setSold(isSold);
    }

    public Date getSoldAt() {
        return soldAt;
    }

    public void setSoldAt(Date soldAt) {
        this.soldAt = soldAt;
    }

    @com.google.firebase.firestore.PropertyName("timestamp")
    public Date getTimestamp() {
        return timestamp != null ? timestamp : createdAt;
    }

    @com.google.firebase.firestore.PropertyName("timestamp")
    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
        if (this.createdAt == null) {
            this.createdAt = timestamp;
        }
    }

    @com.google.firebase.firestore.PropertyName("createdAt")
    public Date getCreatedAt() {
        return createdAt != null ? createdAt : timestamp;
    }

    @com.google.firebase.firestore.PropertyName("createdAt")
    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
        if (this.timestamp == null) {
            this.timestamp = createdAt;
        }
    }

    // Helper methods
    public String getFormattedPrice() {
        return "₹" + (long) price;
    }

    public String getFormattedOriginalPrice() {
        return originalPrice > 0 ? "₹" + (long) originalPrice : "";
    }

    public int getDiscountPercent() {
        if (originalPrice > price && originalPrice > 0) {
            return (int) Math.round(((originalPrice - price) / originalPrice) * 100);
        }
        return 0;
    }

    public String getCampusLocationString() {
        if (hostel != null && !hostel.trim().isEmpty()) {
            if (roomNo != null && !roomNo.trim().isEmpty()) {
                return hostel + " • Rm " + roomNo;
            }
            return hostel;
        }
        return "Campus";
    }
}

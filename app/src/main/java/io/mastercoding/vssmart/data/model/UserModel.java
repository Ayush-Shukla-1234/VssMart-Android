package io.mastercoding.vssmart.data.model;

import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.PropertyName;
import com.google.firebase.firestore.ServerTimestamp;
import java.io.Serializable;
import java.util.Date;

/**
 * User Profile Model representing a registered student on VssMart.
 */
public class UserModel implements Serializable {

    private String uid;
    private String name;
    private String email;
    private String phone = "";
    private String hostel = "";
    private String room = "";
    private String avatarUrl = "";
    private boolean isAdmin = false;

    @ServerTimestamp
    private Date createdAt;

    // Default constructor required for Firestore serialization
    public UserModel() {
    }

    public UserModel(String uid, String name, String email, String phone, String hostel, String room, String avatarUrl) {
        this(uid, name, email, phone, hostel, room, avatarUrl, false);
    }

    public UserModel(String uid, String name, String email, String phone, String hostel, String room, boolean isAdmin) {
        this(uid, name, email, phone, hostel, room, "", isAdmin);
    }

    public UserModel(String uid, String name, String email, String phone, String hostel, String room, String avatarUrl, boolean isAdmin) {
        this.uid = uid;
        this.name = name;
        this.email = email;
        this.phone = phone != null ? phone : "";
        this.hostel = hostel != null ? hostel : "";
        this.room = room != null ? room : "";
        this.avatarUrl = avatarUrl != null ? avatarUrl : "";
        this.isAdmin = isAdmin;
    }

    // Getters and Setters
    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getName() {
        return name != null ? name : "Campus Student";
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email != null ? email : "";
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone != null ? phone : "";
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getHostel() {
        return hostel != null ? hostel : "";
    }

    public void setHostel(String hostel) {
        this.hostel = hostel;
    }

    public String getRoom() {
        return room != null ? room : "";
    }

    public void setRoom(String room) {
        this.room = room;
    }

    public String getAvatarUrl() {
        return avatarUrl != null ? avatarUrl : "";
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    @PropertyName("isAdmin")
    public boolean isAdmin() {
        return isAdmin;
    }

    @PropertyName("isAdmin")
    public void setIsAdmin(boolean admin) {
        this.isAdmin = admin;
    }

    @Exclude
    public boolean getIsAdmin() {
        return isAdmin;
    }

    @PropertyName("admin")
    public void setAdmin(boolean admin) {
        this.isAdmin = admin;
    }

    @Exclude
    public String getFormattedCampusLocation() {
        if (hostel != null && !hostel.isEmpty()) {
            if (room != null && !room.isEmpty()) {
                return hostel + ", Room " + room;
            }
            return hostel;
        }
        return "Hostel not specified";
    }
}

package io.mastercoding.vssmart.utils;

import android.content.Context;
import android.content.SharedPreferences;
import io.mastercoding.vssmart.data.model.UserModel;

/**
 * SharedPreferences Manager for caching current user session and campus details locally.
 */
public class SharedPrefManager {

    private static SharedPrefManager instance;
    private final SharedPreferences prefs;

    private SharedPrefManager(Context context) {
        prefs = context.getApplicationContext().getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE);
    }

    public static synchronized SharedPrefManager getInstance(Context context) {
        if (instance == null) {
            instance = new SharedPrefManager(context);
        }
        return instance;
    }

    public void saveUser(UserModel user) {
        if (user == null) return;
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(Constants.KEY_USER_ID, user.getUid());
        editor.putString(Constants.KEY_USER_NAME, user.getName());
        editor.putString(Constants.KEY_USER_EMAIL, user.getEmail());
        editor.putString(Constants.KEY_USER_PHONE, user.getPhone());
        editor.putString(Constants.KEY_USER_HOSTEL, user.getHostel());
        editor.putString(Constants.KEY_USER_ROOM, user.getRoom());
        editor.putString(Constants.KEY_USER_AVATAR, user.getAvatarUrl());
        editor.putBoolean(Constants.KEY_IS_LOGGED_IN, true);
        editor.putBoolean(Constants.KEY_IS_ADMIN, user.isAdmin());
        editor.apply();
    }

    public UserModel getCachedUser() {
        if (!isLoggedIn()) return null;
        UserModel user = new UserModel();
        user.setUid(prefs.getString(Constants.KEY_USER_ID, ""));
        user.setName(prefs.getString(Constants.KEY_USER_NAME, ""));
        user.setEmail(prefs.getString(Constants.KEY_USER_EMAIL, ""));
        user.setPhone(prefs.getString(Constants.KEY_USER_PHONE, ""));
        user.setHostel(prefs.getString(Constants.KEY_USER_HOSTEL, ""));
        user.setRoom(prefs.getString(Constants.KEY_USER_ROOM, ""));
        user.setAvatarUrl(prefs.getString(Constants.KEY_USER_AVATAR, ""));
        user.setIsAdmin(prefs.getBoolean(Constants.KEY_IS_ADMIN, false));
        return user;
    }

    public void updateCampusDetails(String hostel, String room, String phone) {
        updateCampusDetails(null, phone, hostel, room);
    }

    public void updateCampusDetails(String name, String phone, String hostel, String room) {
        SharedPreferences.Editor editor = prefs.edit();
        if (name != null && !name.trim().isEmpty()) {
            editor.putString(Constants.KEY_USER_NAME, name.trim());
        }
        if (hostel != null) {
            editor.putString(Constants.KEY_USER_HOSTEL, hostel.trim());
        }
        if (room != null) {
            editor.putString(Constants.KEY_USER_ROOM, room.trim());
        }
        if (phone != null) {
            editor.putString(Constants.KEY_USER_PHONE, phone.trim());
        }
        editor.apply();
    }

    public boolean isLoggedIn() {
        return prefs.getBoolean(Constants.KEY_IS_LOGGED_IN, false);
    }

    public String getUserId() {
        return prefs.getString(Constants.KEY_USER_ID, "");
    }

    public String getUserName() {
        return prefs.getString(Constants.KEY_USER_NAME, "Campus Student");
    }

    public String getUserPhone() {
        return prefs.getString(Constants.KEY_USER_PHONE, "");
    }

    public String getUserHostel() {
        return prefs.getString(Constants.KEY_USER_HOSTEL, "");
    }

    public String getUserRoom() {
        return prefs.getString(Constants.KEY_USER_ROOM, "");
    }

    public boolean isAdmin() {
        return prefs.getBoolean(Constants.KEY_IS_ADMIN, false);
    }

    public void setIsAdmin(boolean isAdmin) {
        prefs.edit().putBoolean(Constants.KEY_IS_ADMIN, isAdmin).apply();
    }

    public void clearSession() {
        prefs.edit().clear().apply();
    }
}

package io.mastercoding.vssmart.utils;

public final class Constants {

    private Constants() {
        // Private constructor to prevent instantiation
    }

    // Firestore Collections
    public static final String COLLECTION_USERS = "users";
    public static final String COLLECTION_LISTINGS = "listings";
    public static final String COLLECTION_FEEDBACK = "feedback";

    // Firebase Storage Folders
    public static final String STORAGE_LISTINGS = "listings";
    public static final String STORAGE_PROFILES = "profiles";

    // Intent & Bundle Extras
    public static final String EXTRA_LISTING = "extra_listing";
    public static final String EXTRA_LISTING_ID = "extra_listing_id";
    public static final String EXTRA_PHONE_NUMBER = "extra_phone_number";
    public static final String EXTRA_VERIFICATION_ID = "extra_verification_id";

    // Categories
    public static final String CATEGORY_ALL = "All";
    public static final String CATEGORY_CYCLES = "Cycles";
    public static final String CATEGORY_COOLERS = "Coolers";
    public static final String CATEGORY_STUDY_TABLES = "Study Tables";
    public static final String CATEGORY_BOOKS = "Books";
    public static final String CATEGORY_MATTRESSES = "Mattresses";
    public static final String CATEGORY_KETTLES = "Kettles";
    public static final String CATEGORY_ELECTRONICS = "Electronics";
    public static final String CATEGORY_OTHER = "Other";

    public static final String[] CATEGORIES = new String[]{
            CATEGORY_ALL,
            CATEGORY_CYCLES,
            CATEGORY_COOLERS,
            CATEGORY_STUDY_TABLES,
            CATEGORY_BOOKS,
            CATEGORY_MATTRESSES,
            CATEGORY_KETTLES,
            CATEGORY_ELECTRONICS,
            CATEGORY_OTHER
    };

    public static final String[] FORM_CATEGORIES = new String[]{
            CATEGORY_CYCLES,
            CATEGORY_COOLERS,
            CATEGORY_STUDY_TABLES,
            CATEGORY_BOOKS,
            CATEGORY_MATTRESSES,
            CATEGORY_KETTLES,
            CATEGORY_ELECTRONICS,
            CATEGORY_OTHER
    };

    // Conditions
    public static final String CONDITION_LIKE_NEW = "Like New";
    public static final String CONDITION_GOOD = "Good";
    public static final String CONDITION_FAIR = "Fair";

    public static final String[] CONDITIONS = new String[]{
            CONDITION_LIKE_NEW,
            CONDITION_GOOD,
            CONDITION_FAIR
    };

    // SharedPreferences Keys
    public static final String PREF_NAME = "vssmart_prefs";
    public static final String KEY_USER_ID = "pref_user_id";
    public static final String KEY_USER_NAME = "pref_user_name";
    public static final String KEY_USER_EMAIL = "pref_user_email";
    public static final String KEY_USER_PHONE = "pref_user_phone";
    public static final String KEY_USER_HOSTEL = "pref_user_hostel";
    public static final String KEY_USER_ROOM = "pref_user_room";
    public static final String KEY_USER_AVATAR = "pref_user_avatar";
    public static final String KEY_IS_LOGGED_IN = "pref_is_logged_in";
    public static final String KEY_IS_ADMIN = "pref_is_admin";

    // Listing Moderation Statuses
    public static final String STATUS_REMOVED_BY_ADMIN = "REMOVED_BY_ADMIN";
}

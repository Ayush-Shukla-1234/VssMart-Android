package io.mastercoding.vssmart.data.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.google.firebase.firestore.FirebaseFirestore;
import io.mastercoding.vssmart.data.model.FeedbackModel;
import io.mastercoding.vssmart.data.model.UserModel;
import io.mastercoding.vssmart.utils.Constants;
import io.mastercoding.vssmart.utils.Resource;
import java.util.HashMap;
import java.util.Map;

/**
 * User Repository managing Firestore profile retrieval, updates, and student feedback.
 */
public class UserRepository {

    private static final String TAG = "UserRepository";
    private final FirebaseFirestore firestore;
    private com.google.firebase.firestore.ListenerRegistration userProfileListener;

    public UserRepository() {
        this.firestore = FirebaseFirestore.getInstance();
    }

    /**
     * Listens to the user profile document in real-time.
     */
    public LiveData<Resource<UserModel>> getUserProfile(String uid) {
        MutableLiveData<Resource<UserModel>> userLiveData = new MutableLiveData<>();
        userLiveData.setValue(Resource.loading());

        if (userProfileListener != null) {
            userProfileListener.remove();
        }

        if (uid == null || uid.isEmpty()) {
            userLiveData.setValue(Resource.error("Invalid user ID"));
            return userLiveData;
        }

        userProfileListener = firestore.collection(Constants.COLLECTION_USERS).document(uid)
                .addSnapshotListener((documentSnapshot, error) -> {
                    if (error != null) {
                        android.util.Log.e(TAG, "Failed to listen to user profile: " + error.getMessage(), error);
                        userLiveData.setValue(Resource.error("Failed to load profile: " + error.getMessage()));
                        return;
                    }

                    if (documentSnapshot != null && documentSnapshot.exists()) {
                        UserModel user = documentSnapshot.toObject(UserModel.class);
                        if (user != null) {
                            user.setUid(documentSnapshot.getId());
                            userLiveData.setValue(Resource.success(user));
                            return;
                        }
                    }
                    userLiveData.setValue(Resource.error("User profile not found."));
                });

        return userLiveData;
    }

    public void cleanup() {
        if (userProfileListener != null) {
            userProfileListener.remove();
            userProfileListener = null;
        }
    }

    /**
     * Updates hostel, room number, contact phone, and display name in Firestore.
     */
    public LiveData<Resource<Boolean>> updateProfile(String uid, String name, String phone, String hostel, String room) {
        MutableLiveData<Resource<Boolean>> resultLiveData = new MutableLiveData<>();
        resultLiveData.setValue(Resource.loading());

        Map<String, Object> updates = new HashMap<>();
        if (name != null && !name.trim().isEmpty()) updates.put("name", name.trim());
        if (phone != null && !phone.trim().isEmpty()) updates.put("phone", phone.trim());
        if (hostel != null && !hostel.trim().isEmpty()) updates.put("hostel", hostel.trim());
        if (room != null && !room.trim().isEmpty()) updates.put("room", room.trim());

        firestore.collection(Constants.COLLECTION_USERS).document(uid)
                .update(updates)
                .addOnSuccessListener(aVoid -> resultLiveData.setValue(Resource.success(true)))
                .addOnFailureListener(e -> resultLiveData.setValue(Resource.error("Failed to update profile: " + e.getMessage())));

        return resultLiveData;
    }

    /**
     * Submits student feedback or bug report to Firestore.
     */
    public LiveData<Resource<Boolean>> submitFeedback(FeedbackModel feedback) {
        MutableLiveData<Resource<Boolean>> resultLiveData = new MutableLiveData<>();
        resultLiveData.setValue(Resource.loading());

        String docId = firestore.collection(Constants.COLLECTION_FEEDBACK).document().getId();
        feedback.setId(docId);

        firestore.collection(Constants.COLLECTION_FEEDBACK).document(docId)
                .set(feedback)
                .addOnSuccessListener(aVoid -> resultLiveData.setValue(Resource.success(true)))
                .addOnFailureListener(e -> resultLiveData.setValue(Resource.error("Failed to submit feedback: " + e.getMessage())));

        return resultLiveData;
    }
}

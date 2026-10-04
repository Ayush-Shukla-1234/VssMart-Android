package io.mastercoding.vssmart.data.repository;

import android.util.Log;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import io.mastercoding.vssmart.data.model.UserModel;
import io.mastercoding.vssmart.utils.Constants;
import io.mastercoding.vssmart.utils.Resource;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Authentication Repository handling exclusive Google Sign-In (Gmail)
 * and Cloud Firestore user profile synchronization.
 */
public class AuthRepository {

    private static final String TAG = "AuthRepository";
    private final FirebaseAuth firebaseAuth;
    private final FirebaseFirestore firestore;

    public AuthRepository() {
        this.firebaseAuth = FirebaseAuth.getInstance();
        this.firestore = FirebaseFirestore.getInstance();
    }

    public FirebaseUser getCurrentUser() {
        return firebaseAuth.getCurrentUser();
    }

    public boolean isUserLoggedIn() {
        return firebaseAuth.getCurrentUser() != null;
    }

    /**
     * Signs in using Google Account ID Token and creates/syncs the profile in Firestore.
     */
    public LiveData<Resource<UserModel>> signInWithGoogle(GoogleSignInAccount account) {
        MutableLiveData<Resource<UserModel>> resultLiveData = new MutableLiveData<>();
        resultLiveData.setValue(Resource.loading());

        if (account == null || account.getIdToken() == null) {
            resultLiveData.setValue(Resource.error("Google Account ID Token is missing."));
            return resultLiveData;
        }

        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        firebaseAuth.signInWithCredential(credential)
                .addOnSuccessListener(authResult -> {
                    FirebaseUser firebaseUser = authResult.getUser();
                    if (firebaseUser != null) {
                        syncGoogleUserProfile(firebaseUser, account, resultLiveData);
                    } else {
                        resultLiveData.setValue(Resource.error("Firebase authentication failed."));
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Google Sign-In credential auth failed", e);
                    resultLiveData.setValue(Resource.error(e.getMessage() != null ? e.getMessage() : "Google Sign-In failed"));
                });

        return resultLiveData;
    }

    /**
     * Creates or updates the user profile document in the Firestore `users` collection.
     */
    private void syncGoogleUserProfile(FirebaseUser firebaseUser, GoogleSignInAccount account,
                                      MutableLiveData<Resource<UserModel>> resultLiveData) {
        String uid = firebaseUser.getUid();
        String name = account.getDisplayName() != null ? account.getDisplayName() : (firebaseUser.getDisplayName() != null ? firebaseUser.getDisplayName() : "Campus Student");
        String email = account.getEmail() != null ? account.getEmail() : (firebaseUser.getEmail() != null ? firebaseUser.getEmail() : "");
        String photoUrl = account.getPhotoUrl() != null ? account.getPhotoUrl().toString() : (firebaseUser.getPhotoUrl() != null ? firebaseUser.getPhotoUrl().toString() : "");

        firestore.collection(Constants.COLLECTION_USERS).document(uid)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot != null && documentSnapshot.exists()) {
                        UserModel user = documentSnapshot.toObject(UserModel.class);
                        if (user != null) {
                            Log.d(TAG, "Existing user profile loaded for UID: " + uid + ", isAdmin=" + user.isAdmin());

                            // Update basic Google metadata if needed without touching isAdmin
                            Map<String, Object> updates = new HashMap<>();
                            if (user.getName() == null || user.getName().isEmpty() || "Campus Student".equals(user.getName())) {
                                user.setName(name);
                                updates.put("name", name);
                            }
                            if (user.getEmail() == null || user.getEmail().isEmpty()) {
                                user.setEmail(email);
                                updates.put("email", email);
                            }
                            if ((user.getAvatarUrl() == null || user.getAvatarUrl().isEmpty()) && !photoUrl.isEmpty()) {
                                user.setAvatarUrl(photoUrl);
                                updates.put("avatarUrl", photoUrl);
                            }

                            if (!updates.isEmpty()) {
                                firestore.collection(Constants.COLLECTION_USERS).document(uid)
                                        .set(updates, SetOptions.merge());
                            }

                            resultLiveData.setValue(Resource.success(user));
                            return;
                        }
                    }

                    // Create initial profile for new user ensuring fields match Firestore permissions
                    UserModel newUser = new UserModel(
                            uid,
                            name,
                            email,
                            "", // phone
                            "", // hostel
                            "", // room
                            photoUrl,
                            false // explicitly set isAdmin = false
                    );
                    newUser.setCreatedAt(new Date());

                    firestore.collection(Constants.COLLECTION_USERS).document(uid)
                            .set(newUser, SetOptions.merge())
                            .addOnSuccessListener(aVoid -> {
                                Log.d(TAG, "New user profile initialized successfully in Firestore for UID: " + uid);
                                resultLiveData.setValue(Resource.success(newUser));
                            })
                            .addOnFailureListener(e -> {
                                Log.e(TAG, "Failed to initialize user profile in Firestore", e);
                                resultLiveData.setValue(Resource.error("Failed to initialize user profile: " + e.getMessage()));
                            });
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to read user data from Firestore", e);
                    resultLiveData.setValue(Resource.error("Failed to read user data: " + e.getMessage()));
                });
    }

    public void signOut() {
        firebaseAuth.signOut();
    }
}

package io.mastercoding.vssmart.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import io.mastercoding.vssmart.data.model.FeedbackModel;
import io.mastercoding.vssmart.data.model.ListingModel;
import io.mastercoding.vssmart.data.model.UserModel;
import io.mastercoding.vssmart.data.repository.ListingRepository;
import io.mastercoding.vssmart.data.repository.UserRepository;
import io.mastercoding.vssmart.utils.Resource;
import io.mastercoding.vssmart.utils.SharedPrefManager;
import java.util.List;

/**
 * ViewModel managing user profile, campus hostel/room info, "My Listings", and feedback submission.
 */
public class UserViewModel extends AndroidViewModel {

    private final UserRepository userRepository;
    private final ListingRepository listingRepository;
    private final SharedPrefManager prefManager;
    private final androidx.lifecycle.MediatorLiveData<Resource<UserModel>> currentUserProfile = new androidx.lifecycle.MediatorLiveData<>();
    private LiveData<Resource<UserModel>> currentRepoSource;

    public UserViewModel(@NonNull Application application) {
        super(application);
        this.userRepository = new UserRepository();
        this.listingRepository = new ListingRepository();
        this.prefManager = SharedPrefManager.getInstance(application);

        // Pre-populate with cached user if available
        UserModel cached = prefManager.getCachedUser();
        if (cached != null) {
            currentUserProfile.setValue(Resource.success(cached));
        }
    }

    public LiveData<Resource<UserModel>> getUserProfile(String uid) {
        if (uid == null || uid.isEmpty()) {
            return currentUserProfile;
        }

        if (currentRepoSource != null) {
            currentUserProfile.removeSource(currentRepoSource);
        }

        currentRepoSource = userRepository.getUserProfile(uid);
        currentUserProfile.addSource(currentRepoSource, resource -> {
            if (resource != null && resource.isSuccess() && resource.data != null) {
                prefManager.saveUser(resource.data);
            }
            currentUserProfile.setValue(resource);
        });

        return currentUserProfile;
    }

    public LiveData<Resource<UserModel>> getCurrentUserProfileLiveData() {
        return currentUserProfile;
    }

    public LiveData<Resource<Boolean>> updateCampusProfile(String uid, String name, String phone, String hostel, String room) {
        // 1. Immediate local cache & in-memory LiveData update
        prefManager.updateCampusDetails(name, phone, hostel, room);
        UserModel cached = prefManager.getCachedUser();
        if (cached != null) {
            currentUserProfile.setValue(Resource.success(cached));
        }

        // 2. Remote Firestore update
        LiveData<Resource<Boolean>> resultLiveData = userRepository.updateProfile(uid, name, phone, hostel, room);
        resultLiveData.observeForever(resource -> {
            if (resource != null && resource.isSuccess()) {
                prefManager.updateCampusDetails(name, phone, hostel, room);
                UserModel updated = prefManager.getCachedUser();
                if (updated != null) {
                    currentUserProfile.setValue(Resource.success(updated));
                }
            }
        });
        return resultLiveData;
    }

    public LiveData<Resource<List<ListingModel>>> getMyListings(String sellerId) {
        return listingRepository.getUserListings(sellerId);
    }

    public LiveData<Resource<Boolean>> toggleSoldStatus(String listingId, boolean isSold) {
        return listingRepository.updateListingSoldStatus(listingId, isSold);
    }

    public LiveData<Resource<Boolean>> deleteListing(String listingId) {
        return listingRepository.deleteListing(listingId);
    }

    public LiveData<Resource<Boolean>> deleteListing(ListingModel listing) {
        return listingRepository.deleteListing(listing);
    }

    public LiveData<Resource<Boolean>> submitFeedback(FeedbackModel feedback) {
        return userRepository.submitFeedback(feedback);
    }

    public UserModel getCachedUser() {
        return prefManager.getCachedUser();
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        userRepository.cleanup();
        listingRepository.cleanup();
    }
}

package io.mastercoding.vssmart.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.firebase.auth.FirebaseUser;
import io.mastercoding.vssmart.data.model.UserModel;
import io.mastercoding.vssmart.data.repository.AuthRepository;
import io.mastercoding.vssmart.utils.Resource;
import io.mastercoding.vssmart.utils.SharedPrefManager;

/**
 * Authentication ViewModel managing Google Sign-In (Gmail) sessions and user caching.
 */
public class AuthViewModel extends AndroidViewModel {

    private final AuthRepository authRepository;
    private final SharedPrefManager prefManager;

    public AuthViewModel(@NonNull Application application) {
        super(application);
        this.authRepository = new AuthRepository();
        this.prefManager = SharedPrefManager.getInstance(application);
    }

    public boolean isUserLoggedIn() {
        return authRepository.isUserLoggedIn();
    }

    public FirebaseUser getCurrentFirebaseUser() {
        return authRepository.getCurrentUser();
    }

    public LiveData<Resource<UserModel>> signInWithGoogle(GoogleSignInAccount account) {
        LiveData<Resource<UserModel>> authLiveData = authRepository.signInWithGoogle(account);
        // Cache user in prefs when success
        authLiveData.observeForever(resource -> {
            if (resource != null && resource.isSuccess() && resource.data != null) {
                prefManager.saveUser(resource.data);
            }
        });
        return authLiveData;
    }

    public void signOut() {
        authRepository.signOut();
        prefManager.clearSession();
    }
}

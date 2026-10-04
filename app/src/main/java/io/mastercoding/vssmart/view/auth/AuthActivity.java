package io.mastercoding.vssmart.view.auth;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInStatusCodes;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import io.mastercoding.vssmart.R;
import io.mastercoding.vssmart.databinding.ActivityAuthBinding;
import io.mastercoding.vssmart.view.main.MainActivity;
import io.mastercoding.vssmart.viewmodel.AuthViewModel;

/**
 * Authentication Activity providing exclusive Google Sign-In (Gmail) onboarding.
 */
public class AuthActivity extends AppCompatActivity {

    private static final String TAG = "AuthActivity";

    private ActivityAuthBinding binding;
    private AuthViewModel authViewModel;
    private GoogleSignInClient googleSignInClient;

    // Google Sign-In Activity Result Launcher
    private final ActivityResultLauncher<Intent> googleSignInLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
                try {
                    GoogleSignInAccount account = task.getResult(ApiException.class);
                    if (account != null) {
                        handleGoogleSignInAccount(account);
                    } else {
                        showLoading(false);
                        showError("Unable to retrieve Google Account. Please try again.");
                    }
                } catch (ApiException e) {
                    showLoading(false);
                    int statusCode = e.getStatusCode();
                    Log.e(TAG, "Google Sign-In failed with status code: " + statusCode, e);

                    String userMessage;
                    switch (statusCode) {
                        case CommonStatusCodes.SIGN_IN_REQUIRED:
                        case GoogleSignInStatusCodes.SIGN_IN_CANCELLED:
                            userMessage = "Sign-in cancelled.";
                            break;
                        case GoogleSignInStatusCodes.DEVELOPER_ERROR:
                            userMessage = "Configuration error (Code 10). Please verify SHA-1 fingerprint and Web Client ID in Firebase Console.";
                            break;
                        case CommonStatusCodes.NETWORK_ERROR:
                            userMessage = "Network error. Please check your internet connection.";
                            break;
                        default:
                            userMessage = "Google Sign-In failed (" + statusCode + "): " + e.getLocalizedMessage();
                            break;
                    }
                    showError(userMessage);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO);
        super.onCreate(savedInstanceState);
        binding = ActivityAuthBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        setupGoogleSignInClient();
        setupClickListeners();
    }

    private void setupGoogleSignInClient() {
        String webClientId;
        try {
            int resId = getResources().getIdentifier("default_web_client_id", "string", getPackageName());
            webClientId = resId != 0 ? getString(resId) : getString(R.string.default_web_client_id);
        } catch (Exception e) {
            webClientId = getString(R.string.default_web_client_id);
        }

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(webClientId)
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    private void setupClickListeners() {
        // Prominent Google Sign-In Button
        binding.btnGoogleSignIn.setOnClickListener(v -> {
            showLoading(true);
            // Sign out existing client cache to allow user account selection if needed
            googleSignInClient.signOut().addOnCompleteListener(this, task -> {
                Intent signInIntent = googleSignInClient.getSignInIntent();
                googleSignInLauncher.launch(signInIntent);
            });
        });
    }

    private void handleGoogleSignInAccount(GoogleSignInAccount account) {
        showLoading(true);
        authViewModel.signInWithGoogle(account).observe(this, resource -> {
            if (resource == null) return;

            if (resource.isLoading()) {
                showLoading(true);
            } else if (resource.isSuccess() && resource.data != null) {
                showLoading(false);
                Toast.makeText(this, "Welcome to VssMart, " + resource.data.getName() + "!", Toast.LENGTH_SHORT).show();
                navigateToMainActivity();
            } else if (resource.isError()) {
                showLoading(false);
                showError(resource.message != null ? resource.message : "Authentication failed. Please try again.");
            }
        });
    }

    private void navigateToMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        finish();
    }

    private void showLoading(boolean isLoading) {
        binding.layoutAuthLoading.setVisibility(isLoading ? View.VISIBLE : View.GONE);
    }

    private void showError(String message) {
        Snackbar.make(binding.getRoot(), message, Snackbar.LENGTH_LONG).show();
    }
}

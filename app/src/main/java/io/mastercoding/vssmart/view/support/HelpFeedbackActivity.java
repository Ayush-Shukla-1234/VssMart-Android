package io.mastercoding.vssmart.view.support;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.google.android.material.snackbar.Snackbar;
import io.mastercoding.vssmart.R;
import io.mastercoding.vssmart.data.model.FeedbackModel;
import io.mastercoding.vssmart.data.model.UserModel;
import io.mastercoding.vssmart.databinding.ActivityHelpFeedbackBinding;
import io.mastercoding.vssmart.utils.SharedPrefManager;
import io.mastercoding.vssmart.viewmodel.UserViewModel;

/**
 * Help & Support Activity displaying FAQs and submitting feedback directly to Cloud Firestore.
 */
public class HelpFeedbackActivity extends AppCompatActivity {

    private ActivityHelpFeedbackBinding binding;
    private UserViewModel userViewModel;
    private SharedPrefManager prefManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO);
        super.onCreate(savedInstanceState);
        binding = ActivityHelpFeedbackBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        userViewModel = new ViewModelProvider(this).get(UserViewModel.class);
        prefManager = SharedPrefManager.getInstance(this);

        setupToolbar();
        setupFeedbackSubmission();
    }

    private void setupToolbar() {
        binding.toolbarHelp.setNavigationOnClickListener(v -> finish());
    }

    private void setupFeedbackSubmission() {
        binding.btnSubmitFeedback.setOnClickListener(v -> {
            String subject = binding.etFeedbackSubject.getText() != null ? binding.etFeedbackSubject.getText().toString().trim() : "";
            String message = binding.etFeedbackMessage.getText() != null ? binding.etFeedbackMessage.getText().toString().trim() : "";

            if (subject.isEmpty()) {
                binding.tilFeedbackSubject.setError(getString(R.string.error_required_field));
                return;
            }
            binding.tilFeedbackSubject.setError(null);

            if (message.isEmpty()) {
                binding.tilFeedbackMessage.setError(getString(R.string.error_required_field));
                return;
            }
            binding.tilFeedbackMessage.setError(null);

            UserModel user = prefManager.getCachedUser();
            String userId = user != null ? user.getUid() : "guest";
            String userName = user != null ? user.getName() : "Campus Student";
            String userEmail = user != null ? user.getEmail() : "";

            FeedbackModel feedback = new FeedbackModel();
            feedback.setUserId(userId);
            feedback.setUserName(userName);
            feedback.setUserEmail(userEmail);
            feedback.setSubject(subject);
            feedback.setMessage(message);

            binding.btnSubmitFeedback.setEnabled(false);
            userViewModel.submitFeedback(feedback).observe(this, resource -> {
                binding.btnSubmitFeedback.setEnabled(true);
                if (resource != null && resource.isSuccess()) {
                    Toast.makeText(this, R.string.feedback_submitted, Toast.LENGTH_LONG).show();
                    binding.etFeedbackSubject.setText("");
                    binding.etFeedbackMessage.setText("");
                    finish();
                } else if (resource != null && resource.isError()) {
                    Snackbar.make(binding.getRoot(), resource.message != null ? resource.message : "Failed to submit feedback", Snackbar.LENGTH_LONG).show();
                }
            });
        });
    }
}

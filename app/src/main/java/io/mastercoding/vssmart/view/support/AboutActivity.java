package io.mastercoding.vssmart.view.support;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import io.mastercoding.vssmart.R;
import io.mastercoding.vssmart.databinding.ActivityAboutBinding;

/**
 * About Screen displaying developer info (Ayush Shukla), college background, and tech stack details.
 */
public class AboutActivity extends AppCompatActivity {

    private ActivityAboutBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO);
        super.onCreate(savedInstanceState);
        binding = ActivityAboutBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupToolbar();
        setupSocialLinks();
    }

    private void setupToolbar() {
        binding.toolbarAbout.setNavigationOnClickListener(v -> finish());
    }

    private void setupSocialLinks() {
        binding.btnGitHub.setOnClickListener(v -> {
            openWebUrl(getString(R.string.github_profile_url));
        });

        binding.btnLinkedIn.setOnClickListener(v -> {
            openWebUrl(getString(R.string.linkedin_profile_url));
        });

        binding.btnFeedback.setOnClickListener(v -> {
            startActivity(new Intent(this, HelpFeedbackActivity.class));
        });

        binding.tvTerms.setOnClickListener(v -> showTermsDialog());
        binding.tvPrivacy.setOnClickListener(v -> showPrivacyDialog());
    }

    private void showTermsDialog() {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Terms of Use")
                .setMessage("1. VssMart is exclusively for VSSUT students and campus residents.\n\n"
                        + "2. All transactions are peer-to-peer (P2P). Please inspect items in person inside hostel premises before making payment.\n\n"
                        + "3. Prohibited items (illegal goods, academic dishonesty materials) are strictly banned.\n\n"
                        + "4. VssMart charges 0% commission and acts solely as a discovery platform for students.")
                .setPositiveButton("Got it", null)
                .show();
    }

    private void showPrivacyDialog() {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Privacy Policy")
                .setMessage("1. Your data (name, email, hostel, room number) is only used for verifying campus identity and facilitating direct buyer-seller connections.\n\n"
                        + "2. Phone numbers are used solely for WhatsApp chat and call actions.\n\n"
                        + "3. We do not sell, rent, or share student data with third-party advertisers.\n\n"
                        + "4. You can delete your listings or log out at any time from the 'You' tab.")
                .setPositiveButton("Understood", null)
                .show();
    }

    private void openWebUrl(String url) {
        if (url == null || url.trim().isEmpty()) return;
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url.trim()));
            startActivity(intent);
        } catch (Exception e) {
            android.widget.Toast.makeText(this, "Unable to open browser link", android.widget.Toast.LENGTH_SHORT).show();
        }
    }
}

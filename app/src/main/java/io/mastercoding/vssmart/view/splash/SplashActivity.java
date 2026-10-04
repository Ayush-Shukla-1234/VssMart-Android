package io.mastercoding.vssmart.view.splash;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.animation.OvershootInterpolator;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import io.mastercoding.vssmart.databinding.ActivitySplashBinding;
import io.mastercoding.vssmart.utils.SharedPrefManager;
import io.mastercoding.vssmart.view.auth.AuthActivity;
import io.mastercoding.vssmart.view.main.MainActivity;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Splash screen with hardware-accelerated branding animations decoupled
 * from disk and network I/O to guarantee stutter-free 60/120 FPS rendering.
 */
public class SplashActivity extends AppCompatActivity {

    private static final long MIN_SPLASH_DURATION_MS = 2200;

    private ActivitySplashBinding binding;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final ExecutorService ioExecutor = Executors.newSingleThreadExecutor();

    private Boolean cachedDestinationIsMain = null;
    private boolean isAnimationComplete = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO);
        super.onCreate(savedInstanceState);
        binding = ActivitySplashBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initHardwareAcceleratedAnimation();
        performBackgroundSessionCheck();
    }

    /**
     * Runs hardware-accelerated property animations on RenderThread using .withLayer()
     * timed gracefully to deliver an elegant 2+ second branding sequence.
     */
    private void initHardwareAcceleratedAnimation() {
        // Initial hardware transform states
        binding.ivSplashLogo.setScaleX(0.3f);
        binding.ivSplashLogo.setScaleY(0.3f);
        binding.ivSplashLogo.setAlpha(0.0f);
        binding.tvSplashTitle.setText("VMart");
        binding.tvSplashSubtitle.setAlpha(0.0f);
        binding.tvSplashSubtitle.setTranslationY(20f);

        // Hardware-accelerated Logo entrance on RenderThread (900ms)
        binding.ivSplashLogo.animate()
                .scaleX(1.0f)
                .scaleY(1.0f)
                .alpha(1.0f)
                .setDuration(900)
                .setInterpolator(new OvershootInterpolator(1.1f))
                .withLayer()
                .start();

        // Smooth branding expansion ("VMart" -> "VssMart") starting at 750ms
        mainHandler.postDelayed(() -> {
            if (isFinishing() || isDestroyed()) return;

            binding.tvSplashTitle.animate()
                    .alpha(0.2f)
                    .setDuration(200)
                    .withLayer()
                    .withEndAction(() -> {
                        if (isFinishing() || isDestroyed()) return;
                        binding.tvSplashTitle.setText("VssMart");
                        binding.tvSplashTitle.animate()
                                .alpha(1.0f)
                                .setDuration(350)
                                .withLayer()
                                .start();
                    })
                    .start();
        }, 750);

        // Subtitle reveal with upward translation starting at 900ms (600ms duration)
        mainHandler.postDelayed(() -> {
            if (isFinishing() || isDestroyed()) return;

            binding.tvSplashSubtitle.animate()
                    .alpha(1.0f)
                    .translationY(0f)
                    .setDuration(600)
                    .withLayer()
                    .start();
        }, 900);

        // Minimum animation timer to coordinate smooth departure (at least 2.2 seconds)
        mainHandler.postDelayed(() -> {
            isAnimationComplete = true;
            tryNavigate();
        }, MIN_SPLASH_DURATION_MS);
    }

    /**
     * Decouples Firebase initialization and SharedPreferences disk reads from the UI thread.
     * All storage / auth / network operations execute strictly in the background worker thread.
     */
    private void performBackgroundSessionCheck() {
        ioExecutor.execute(() -> {
            boolean isLoggedIn = false;
            try {
                // 1. Warm-up Firebase & Auth off the main UI thread to prevent Main-thread SQLite/KeyStore locks
                android.content.Context appContext = getApplicationContext();
                com.google.firebase.FirebaseApp.initializeApp(appContext);
                FirebaseAuth auth = FirebaseAuth.getInstance();

                // 2. Read SharedPreferences from disk asynchronously
                SharedPrefManager prefManager = SharedPrefManager.getInstance(appContext);
                isLoggedIn = prefManager.isLoggedIn() && auth.getCurrentUser() != null;
            } catch (Exception ignored) {
                try {
                    SharedPrefManager prefManager = SharedPrefManager.getInstance(getApplicationContext());
                    isLoggedIn = prefManager.isLoggedIn();
                } catch (Exception e) {
                    isLoggedIn = false;
                }
            }

            final boolean destinationIsMain = isLoggedIn;
            mainHandler.post(() -> {
                cachedDestinationIsMain = destinationIsMain;
                tryNavigate();
            });
        });
    }

    /**
     * Seamlessly navigates once both the hardware animation and background I/O are complete.
     */
    private synchronized void tryNavigate() {
        if (isFinishing() || isDestroyed()) return;

        if (isAnimationComplete && cachedDestinationIsMain != null) {
            Intent nextIntent = cachedDestinationIsMain
                    ? new Intent(SplashActivity.this, MainActivity.class)
                    : new Intent(SplashActivity.this, AuthActivity.class);

            startActivity(nextIntent);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mainHandler.removeCallbacksAndMessages(null);
        ioExecutor.shutdown();
        if (binding != null) {
            binding.ivSplashLogo.animate().cancel();
            binding.tvSplashSubtitle.animate().cancel();
        }
    }
}

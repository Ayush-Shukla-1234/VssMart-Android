package io.mastercoding.vssmart.view.main;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import io.mastercoding.vssmart.R;
import io.mastercoding.vssmart.databinding.ActivityMainBinding;

/**
 * Main Activity hosting the BottomNavigationView and fragment transitions.
 */
public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private static final String TAG_MARKET = "tag_market";
    private static final String TAG_SELL = "tag_sell";
    private static final String TAG_YOU = "tag_you";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO);
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        if (savedInstanceState == null) {
            loadFragment(new MarketFragment(), TAG_MARKET);
        }

        setupBottomNavigation();
    }

    private void setupBottomNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragmentContainer);

            if (itemId == R.id.nav_market) {
                if (currentFragment instanceof MarketFragment) {
                    return true; // Don't reload if already on this screen
                }
                loadFragment(new MarketFragment(), TAG_MARKET);
                return true;
            } else if (itemId == R.id.nav_sell) {
                if (currentFragment instanceof AddListingFragment) {
                    return true; // Don't reload if already on this screen
                }
                loadFragment(new AddListingFragment(), TAG_SELL);
                return true;
            } else if (itemId == R.id.nav_you) {
                if (currentFragment instanceof YouFragment) {
                    return true; // Don't reload if already on this screen
                }
                loadFragment(new YouFragment(), TAG_YOU);
                return true;
            }
            return false;
        });

        // Explicitly handle reselection to prevent duplicate transactions or freezes
        binding.bottomNavigation.setOnItemReselectedListener(item -> {
            Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragmentContainer);
            if (item.getItemId() == R.id.nav_market && currentFragment instanceof MarketFragment) {
                ((MarketFragment) currentFragment).scrollToTop();
            }
        });
    }

    private void loadFragment(Fragment fragment, String tag) {
        if (isFinishing() || isDestroyed()) return;

        FragmentManager fm = getSupportFragmentManager();
        fm.beginTransaction()
                .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                .replace(R.id.fragmentContainer, fragment, tag)
                .commitAllowingStateLoss();
    }

    public void selectMarketTab() {
        binding.bottomNavigation.setSelectedItemId(R.id.nav_market);
    }
}

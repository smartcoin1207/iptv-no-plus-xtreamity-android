package com.xtreamity;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.OnBackPressedDispatcher;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.OnUserEarnedRewardListener;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.gms.ads.rewarded.RewardItem;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.navigation.NavigationView;
import com.google.android.play.core.appupdate.AppUpdateInfo;
import com.google.android.play.core.appupdate.AppUpdateManager;
import com.google.android.play.core.appupdate.AppUpdateManagerFactory;
import com.google.android.play.core.install.model.UpdateAvailability;
import com.xtreamity.fragments.LoadingFragment;
import com.xtreamity.utils.AppOpenManager;
import com.xtreamity.utils.ServerDetailsModal;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawerLayout;
    public static List<ServerDetailsModal> serverDetailsList;
    public static InterstitialAd mInterstitialAd;
    public static RewardedAd rewardedAd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        loadBannerAd();
        loadInterstitialAd(this);
        loadRewardedAd(this);

        // Setup the toolbar
        MaterialToolbar toolbar = findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);

        // Setup DrawerLayout and toggle button
        drawerLayout = findViewById(R.id.drawer_layout);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // Setup NavigationView and listener
        NavigationView navigationView = findViewById(R.id.navigation_view);
        navigationView.setNavigationItemSelectedListener(this);

        serverDetailsList = new ArrayList<>();

        // Load default fragment or view
        if (savedInstanceState == null)

            getSupportFragmentManager().beginTransaction().
                    replace(R.id.content_frame, new LoadingFragment())
                    .commit();

        OnBackPressedDispatcher onBackPressedDispatcher = getOnBackPressedDispatcher();
        // Register a callback to handle back presses
        onBackPressedDispatcher.addCallback(this, new

                OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        if (drawerLayout.isDrawerOpen(GravityCompat.START))
                            drawerLayout.closeDrawer(GravityCompat.START);
                        else
                            new AlertDialog.Builder(MainActivity.this)
                                    .setTitle("Exit")
                                    .setMessage("Do you want to exit the application?")
                                    .setPositiveButton("Exit", (dialog, which) -> finishAffinity())
                                    .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                                    .show();
                    }
                });

        checkForAppUpdate();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        if (item.getItemId() == R.id.how_to_use) {

            View dialogView = getLayoutInflater().inflate(R.layout.how_to_use, null, false);
            new AlertDialog.Builder(this)
                    .setView(dialogView)
                    .setTitle("How to use?")
                    .setPositiveButton("Got it!", (dialog, which) -> dialog.dismiss())
                    .show();

        } else if (item.getItemId() == R.id.share_app) {

            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name));
            String shareMessage = "Check out this amazing app! You can generate IPTV Codes for free!\n";
            shareMessage = shareMessage + "https://play.google.com/store/apps/details?id=" + getPackageName();
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage);
            startActivity(Intent.createChooser(shareIntent, "Share via"));

        } else if (item.getItemId() == R.id.rate_app) {

            String packageName = getPackageName();
            try {
                // Try to open the Play Store app
                Uri uri = Uri.parse("market://details?id=" + packageName);
                Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
                goToMarket.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(goToMarket);
            } catch (ActivityNotFoundException e) {
                // If Play Store is not installed, open the browser
                Uri uri = Uri.parse("https://play.google.com/store/apps/details?id=" + packageName);
                Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
                goToMarket.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(goToMarket);
            }

        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private void loadBannerAd() {
        AdView adView1 = findViewById(R.id.main_banner_ad_view_1);
        AdView adView2 = findViewById(R.id.main_banner_ad_view_2);
        AdRequest adRequest = new AdRequest.Builder().build();
        adView1.loadAd(adRequest);
        adView2.loadAd(adRequest);
    }

    public static void loadInterstitialAd(Context context) {
        AdRequest adRequest = new AdRequest.Builder().build();

        InterstitialAd.load(context, context.getString(R.string.admob_interstitial_id), adRequest,
                new InterstitialAdLoadCallback() {
                    @Override
                    public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                        // The interstitialAd reference will be null until
                        // an ad is loaded.
                        mInterstitialAd = interstitialAd;

                        mInterstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                            @Override
                            public void onAdDismissedFullScreenContent() {
                                // Called when ad is dismissed.
                                loadInterstitialAd(context);
                                AppOpenManager.isInterstitialAdShowing = false;
                            }

                            @Override
                            public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
                                // Called when ad fails to show.
                                loadInterstitialAd(context);
                                AppOpenManager.isInterstitialAdShowing = false;
                            }

                            @Override
                            public void onAdShowedFullScreenContent() {
                                // Called when ad is shown.
                                mInterstitialAd = null;  // Set the ad reference to null so you don't show the ad again.
                            }
                        });
                    }

                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError adError) {
                        // Handle the error
                        mInterstitialAd = null;
                        AppOpenManager.isInterstitialAdShowing = false;
                    }
                });
    }

    public static void showInterstitialAd(Activity activity, Context context) {
        if (mInterstitialAd != null) {
            AppOpenManager.isInterstitialAdShowing = true;
            mInterstitialAd.show(activity);
        } else {
            loadInterstitialAd(context);
        }
    }

    public static void loadRewardedAd(Context context) {
        AdRequest adRequest = new AdRequest.Builder().build();

        RewardedAd.load(context, context.getString(R.string.admob_rewarded_id), adRequest, new RewardedAdLoadCallback() {
            @Override
            public void onAdLoaded(@NonNull RewardedAd rAd) {
                rewardedAd = rAd;

                rewardedAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                    @Override
                    public void onAdClicked() {
                        super.onAdClicked();
                    }

                    @Override
                    public void onAdDismissedFullScreenContent() {
                        super.onAdDismissedFullScreenContent();
                        loadRewardedAd(context);
                        AppOpenManager.isRewardedAdShowing = false;
                    }

                    @Override
                    public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
                        super.onAdFailedToShowFullScreenContent(adError);
                        loadRewardedAd(context);
                        AppOpenManager.isRewardedAdShowing = false;
                    }

                    @Override
                    public void onAdImpression() {
                        super.onAdImpression();
                    }

                    @Override
                    public void onAdShowedFullScreenContent() {
                        super.onAdShowedFullScreenContent();
                        loadRewardedAd(context);
                    }
                });
            }

            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                rewardedAd = null;
                loadRewardedAd(context);
                AppOpenManager.isRewardedAdShowing = false;
            }
        });
    }


    public static void showRewardedInterstitialAd(Activity activity, Context context) {
        if (rewardedAd != null) {
            AppOpenManager.isRewardedAdShowing = true;
            rewardedAd.show(activity, new OnUserEarnedRewardListener() {
                @Override
                public void onUserEarnedReward(@NonNull RewardItem rewardItem) {

                }
            });
        } else {
            loadRewardedAd(context);
        }
    }

    private void checkForAppUpdate() {
        AppUpdateManagerFactory.create(this).getAppUpdateInfo()
                .addOnSuccessListener(appUpdateInfo -> {
                    if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE) {
                        new AlertDialog.Builder(MainActivity.this)
                                .setTitle("Update Available")
                                .setMessage("A new version of the app is available. Please update to keep the app running smoothly.")
                                .setNegativeButton("Later", (dialogInterface, i) -> dialogInterface.dismiss())
                                .setPositiveButton("Update now", (dialogInterface, i) -> {

                                }).show();
                    }
                });
    }
}
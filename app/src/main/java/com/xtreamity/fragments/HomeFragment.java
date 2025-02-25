package com.xtreamity.fragments;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.OnBackPressedDispatcher;
import androidx.appcompat.widget.AppCompatRatingBar;
import androidx.fragment.app.Fragment;

import android.os.CountDownTimer;
import android.os.Handler;
import android.text.InputType;
import android.text.method.PasswordTransformationMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.xtreamity.MainActivity;
import com.xtreamity.R;
import com.xtreamity.utils.ServerDetailsModal;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class HomeFragment extends Fragment {

    private TextView tvError;
    private TextView tvServerUrl, tvServerPort, tvServerUsername, tvServerPassword, tvStatus, tvCreateAt, tvExpirationDate, tvTimezone, tvTimeNow, tvMaxConnections, tvIsTrial;
    private List<ServerDetailsModal> serverDetailsList;
    private TextInputEditText etServer, etUsername, etPassword,etRegion;
    private MaterialButton btnGenerate, btnCopy, btnShare;
    private ServerDetailsModal serverDetails;
    private AlertDialog dialog;
    private MediaPlayer success, failed;

    private TextInputLayout tilServer, tilUsername, tilPassword, tilRegion;
    private boolean canCopyPassword = false;

    @SuppressLint("SetTextI18n")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View fragmentView = inflater.inflate(R.layout.fragment_home, container, false);

        serverDetailsList = MainActivity.serverDetailsList;

        tvError = fragmentView.findViewById(R.id.main_text_error);
        tilServer = fragmentView.findViewById(R.id.main_til_server);
        tilRegion = fragmentView.findViewById(R.id.main_til_region);
        tilUsername = fragmentView.findViewById(R.id.main_til_username);
        tilPassword = fragmentView.findViewById(R.id.main_til_password);
        etServer = fragmentView.findViewById(R.id.main_et_server);
        etRegion = fragmentView.findViewById(R.id.main_et_region);
        etUsername = fragmentView.findViewById(R.id.main_et_username);
        etPassword = fragmentView.findViewById(R.id.main_et_password);
        btnGenerate = fragmentView.findViewById(R.id.main_button);
        btnCopy = fragmentView.findViewById(R.id.main_button_copy);
        btnShare = fragmentView.findViewById(R.id.main_button_share);

        tilServer.setEndIconVisible(false);
        tilRegion.setEndIconVisible(false);
        tilUsername.setEndIconVisible(false);
        tilPassword.setEndIconVisible(false);

        View dialogView1 = getLayoutInflater().inflate(R.layout.dialog_server_details,
                fragmentView.findViewById(android.R.id.content), false);

        tvServerUrl = dialogView1.findViewById(R.id.dialog_server_url);
        tvServerPort = dialogView1.findViewById(R.id.dialog_server_port);
        tvServerUsername = dialogView1.findViewById(R.id.dialog_server_username);
        tvServerPassword = dialogView1.findViewById(R.id.dialog_server_password);

        tvStatus = dialogView1.findViewById(R.id.dialog_status);
        tvCreateAt = dialogView1.findViewById(R.id.dialog_created_at);
        tvExpirationDate = dialogView1.findViewById(R.id.dialog_expiration_date);
        tvTimezone = dialogView1.findViewById(R.id.dialog_timezone);
        tvTimeNow = dialogView1.findViewById(R.id.dialog_time_now);
        tvMaxConnections = dialogView1.findViewById(R.id.dialog_max_connections);
        tvIsTrial = dialogView1.findViewById(R.id.dialog_is_trial);

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Server Detail");
        builder.setPositiveButton("Back", (dialog, which) -> dialog.dismiss());
        builder.setView(dialogView1);

        dialog = builder.create();

        btnGenerate.setOnClickListener(v -> {
            if (tvError.getVisibility() == View.VISIBLE)
                tvError.setVisibility(View.GONE);

            String text = btnGenerate.getText().toString();
            if (text.equalsIgnoreCase("Generate")) {

                btnGenerate.setText("Generating");

                fragmentView.findViewById(R.id.main_button_container)
                        .setVisibility(View.GONE);

                new Handler().postDelayed(() -> {
                    Random random = new Random();

                    do {
                        serverDetails = serverDetailsList.get(random.nextInt(serverDetailsList.size() + 1));
                    } while (serverDetails.getUrl().isEmpty() || serverDetails.getPassword().isEmpty() || serverDetails.getUsername().isEmpty());

                    etServer.setText(serverDetails.getUrl());
                    etRegion.setText(serverDetails.getRegion());
                    etUsername.setText(serverDetails.getUsername());
                    etPassword.setText(serverDetails.getPassword());

                    etPassword.setInputType(InputType.TYPE_CLASS_TEXT);
                    etPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    etPassword.setTypeface(etPassword.getTypeface(), Typeface.BOLD);

                    /*
                    etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    etPassword.setTypeface(etPassword.getTypeface(), Typeface.BOLD);

                     */

                    canCopyPassword = false;

                    tilServer.setEndIconVisible(false);
                    tilRegion.setEndIconVisible(false);
                    tilUsername.setEndIconVisible(false);
                    tilPassword.setEndIconVisible(false);

                    btnGenerate.setText("Check Status");

                    //REMOVED MainActivity.showInterstitialAd(requireActivity(), requireContext());
                }, 3000);

            } else if (text.equalsIgnoreCase("Check Status")) {
                //removed - MainActivity.showInterstitialAd(requireActivity(), requireContext());

                btnGenerate.setText("Checking");
                checkServer();
            } else if (text.equalsIgnoreCase("View Password")) {

                MainActivity.showRewardedInterstitialAd(requireActivity(),requireContext());
                btnGenerate.setText("Loading");

                new Handler().postDelayed(() -> {
                    //removed - MainActivity.showInterstitialAd(requireActivity(), requireContext());

                    etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                    etPassword.setTypeface(Typeface.DEFAULT_BOLD); // Ensures bold text

                    /*
                    etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                    etPassword.setTypeface(etPassword.getTypeface(), Typeface.BOLD);

                     */

                    canCopyPassword = true;

                    tilServer.setEndIconVisible(true);
                    tilRegion.setEndIconVisible(true);
                    tilUsername.setEndIconVisible(true);
                    tilPassword.setEndIconVisible(true);

                    btnGenerate.setText("Generate Another");

                    fragmentView.findViewById(R.id.main_button_container)
                            .setVisibility(View.VISIBLE);
                }, 3000);
            } else if (text.equalsIgnoreCase("Generate Another")) {

                btnGenerate.setText("Generating");

                fragmentView.findViewById(R.id.main_button_container)
                        .setVisibility(View.GONE);

                new Handler().postDelayed(() -> {
                    Random random = new Random();

                    do {
                        serverDetails = serverDetailsList.get(random.nextInt(serverDetailsList.size() + 1));
                    } while (serverDetails.getUrl().isEmpty() || serverDetails.getPassword().isEmpty() || serverDetails.getUsername().isEmpty());

                    etServer.setText(serverDetails.getUrl());
                    etRegion.setText(serverDetails.getRegion());
                    etUsername.setText(serverDetails.getUsername());
                    etPassword.setText(serverDetails.getPassword());

                    etPassword.setInputType(InputType.TYPE_CLASS_TEXT);
                    etPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    etPassword.setTypeface(etPassword.getTypeface(), Typeface.BOLD);

                    /*
                    etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    etPassword.setTypeface(etPassword.getTypeface(), Typeface.BOLD);

                     */

                    canCopyPassword = false;

                    tilServer.setEndIconVisible(false);
                    tilRegion.setEndIconVisible(false);
                    tilUsername.setEndIconVisible(false);
                    tilPassword.setEndIconVisible(false);

                    btnGenerate.setText("Check Status");

                    //REMOVED - MainActivity.showInterstitialAd(requireActivity(), requireContext());
                }, 3000);
            }
        });


        fragmentView.findViewById(R.id.frag_home_how_to_use)
                .setOnClickListener(v -> {
                    View dialogView = getLayoutInflater().inflate(R.layout.how_to_use, null, false);
                    new AlertDialog.Builder(requireContext())
                            .setView(dialogView)
                            .setTitle("How to use?")
                            .setPositiveButton("Got it!", (dialog, which) -> dialog.dismiss())
                            .show();
                });

        tilServer.setEndIconOnClickListener(v -> {
            if (serverDetails != null) {
                MainActivity.showInterstitialAd(requireActivity(), requireContext());

                String text = serverDetails.getUrl();

                ClipboardManager clipboard = (ClipboardManager) requireContext().getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("Copied Server", text);
                clipboard.setPrimaryClip(clip);
                Toast.makeText(requireContext(), "Server copied to clipboard", Toast.LENGTH_SHORT).show();
            }
        });

        tilUsername.setEndIconOnClickListener(v -> {

            if (serverDetails != null) {
                MainActivity.showInterstitialAd(requireActivity(), requireContext());

                String text = serverDetails.getUsername();

                ClipboardManager clipboard = (ClipboardManager) requireContext().getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("Copied Username", text);
                clipboard.setPrimaryClip(clip);
                Toast.makeText(requireContext(), "Username copied to clipboard", Toast.LENGTH_SHORT).show();
            }
        });

        tilPassword.setEndIconOnClickListener(v -> {

            if (serverDetails != null) {
                MainActivity.showInterstitialAd(requireActivity(), requireContext());

                String text = serverDetails.getPassword();

                ClipboardManager clipboard = (ClipboardManager) requireContext().getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("Copied Password", text);
                clipboard.setPrimaryClip(clip);
                Toast.makeText(requireContext(), "Password copied to clipboard", Toast.LENGTH_SHORT).show();
            }
        });

        btnCopy.setOnClickListener(v -> {
            MainActivity.showInterstitialAd(requireActivity(), requireContext());

            String text = serverDetails.getUrl() + "\n" + serverDetails.getUsername() + "\n" + serverDetails.getPassword();

            ClipboardManager clipboard = (ClipboardManager) requireContext().getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("Copied Server", text);
            clipboard.setPrimaryClip(clip);
            Toast.makeText(requireContext(), "Server copied to clipboard", Toast.LENGTH_SHORT).show();
        });

        btnShare.setOnClickListener(v -> {
            MainActivity.showInterstitialAd(requireActivity(), requireContext());

            String text = serverDetails.getUrl() + "\n" + serverDetails.getUsername() + "\n" + serverDetails.getPassword();

            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT, text);
            startActivity(Intent.createChooser(shareIntent, "Share via"));

        });

        OnBackPressedDispatcher onBackPressedDispatcher = requireActivity().getOnBackPressedDispatcher();

        // Register a callback to handle back presses
        onBackPressedDispatcher.addCallback(requireActivity(), new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {

                if (canCopyPassword) {
                    View dialogView = getLayoutInflater().inflate(R.layout.dialog_rate, null, false);
                    AppCompatRatingBar ratingBar = dialogView.findViewById(R.id.dialog_rate_bar);

                    new AlertDialog.Builder(requireContext())
                            .setTitle("Love this app?")
                            .setMessage("Please take a moment to rate us")
                            .setView(dialogView)
                            .setPositiveButton("Rate Now", (
                                    dialog, which) -> {
                                if (ratingBar.getRating() >= 3) {
                                    final String appPackageName = requireActivity().getPackageName();
                                    try {
                                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
                                    } catch (android.content.ActivityNotFoundException e) {
                                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
                                    }
                                } else
                                    Toast.makeText(requireContext(), "Thanks for feedback", Toast.LENGTH_SHORT).show();

                                new AlertDialog.Builder(requireContext())
                                        .setTitle("Exit")
                                        .setMessage("Do you want to exit the application?")
                                        .setPositiveButton("Exit", (dialog1, which1) -> requireActivity().finish())
                                        .setNegativeButton("Cancel", (dialog1, which1) -> dialog.dismiss())
                                        .show();
                            })
                            .setNegativeButton("Later", (dialog, which) -> {
                                new AlertDialog.Builder(requireContext())
                                        .setTitle("Exit")
                                        .setMessage("Do you want to exit the application?")
                                        .setPositiveButton("Exit", (dialog1, which1) -> requireActivity().finish())
                                        .setNegativeButton("Cancel", (dialog1, which1) -> dialog.dismiss())
                                        .show();
                                dialog.dismiss();
                            })
                            .show();
                } else
                    new AlertDialog.Builder(requireContext())
                            .setTitle("Exit")
                            .setMessage("Do you want to exit the application?")
                            .setPositiveButton("Exit", (dialog, which) -> requireActivity().finish())
                            .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                            .show();
            }
        });

        return fragmentView;
    }

    @SuppressLint("SetTextI18n")
    private void checkServer() {
        RequestQueue requestQueue = Volley.newRequestQueue(requireContext());

        // Countdown timer for 10 seconds (10000 milliseconds)
        CountDownTimer countDownTimer = new CountDownTimer(10000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                btnGenerate.setText("Checking (" + millisUntilFinished / 1000 + ")");
            }

            @Override
            public void onFinish() {
                requestQueue.cancelAll("serverCheck");

                btnGenerate.setText("Generate");
                tvError.setText("This server is not working. Generate another");
                tvError.setVisibility(View.VISIBLE);
                //Toast.makeText(MainActivity.this, "Server not working. Try again", Toast.LENGTH_SHORT).show();

                playFailedSound();
            }
        };
        countDownTimer.start();

        String url = serverDetails.getUrl() + "/player_api.php?username="
                + serverDetails.getUsername() + "&password=" + serverDetails.getPassword() + "&type=m3u_plus&output=ts";

        // Create a JsonObjectRequest
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.GET, url, null,
                response -> {
                    try {
                        // Parse user_info
                        JSONObject userInfo = response.getJSONObject("user_info");

                        String username = userInfo.getString("username");
                        String password = userInfo.getString("password");
                        //String message = userInfo.getString("message");
                        //int auth = userInfo.getInt("auth");
                        String status = userInfo.getString("status");
                        String expDate = userInfo.getString("exp_date");
                        String isTrial = userInfo.getString("is_trial");
                        //String activeCons = userInfo.getString("active_cons");
                        String createdAt = userInfo.getString("created_at");
                        String maxConnections = userInfo.getString("max_connections");

                        // Parse server_info
                        JSONObject serverInfo = response.getJSONObject("server_info");
                        String url1 = serverInfo.getString("url");
                        String port = serverInfo.getString("port");
                        //String httpsPort = serverInfo.getString("https_port");
                        //String serverProtocol = serverInfo.getString("server_protocol");
                        //String rtmpPort = serverInfo.getString("rtmp_port");
                        String timezone = serverInfo.getString("timezone");
                        //int timestampNow = serverInfo.getInt("timestamp_now");
                        String timeNow = serverInfo.getString("time_now");

                        countDownTimer.cancel();
                        btnGenerate.setText("View Password");

                        if (username.isEmpty() || password.isEmpty() || status.isEmpty() ||
                                expDate.isEmpty() || isTrial.isEmpty() || createdAt.isEmpty() || maxConnections.isEmpty() ||
                                url1.isEmpty() || port.isEmpty() || timezone.isEmpty() || timeNow.isEmpty()) {

                            tvError.setText("Unable to show server details");
                            tvError.setVisibility(View.VISIBLE);

                            return;
                        }

                        //CONVERTING TIMESTAMPS
                        Date date1 = new Date(Integer.parseInt(createdAt) * 1000L);
                        Date date2 = new Date(Integer.parseInt(expDate) * 1000L);

                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                        String formattedCreatedDate = sdf.format(date1);
                        String formattedExpDate = sdf.format(date2);

                        tvServerUrl.setText(url1);
                        tvServerPort.setText(port);
                        tvServerUsername.setText(username);
                        tvServerPassword.setText(password);

                        tvStatus.setText(status);
                        tvCreateAt.setText(formattedCreatedDate);
                        tvExpirationDate.setText(formattedExpDate);
                        tvTimezone.setText(timezone);
                        tvTimeNow.setText(timeNow);
                        tvMaxConnections.setText(maxConnections);
                        if (isTrial.equalsIgnoreCase("0"))
                            tvIsTrial.setText("No");
                        else
                            tvIsTrial.setText("Yes");

                        dialog.show();

                        playSuccessSound();

                    } catch (JSONException e) {
                        e.printStackTrace();
                        tvError.setText("Unable to show server details");
                        tvError.setVisibility(View.VISIBLE);
                        //Toast.makeText(MainActivity.this, "Unable to show server details", Toast.LENGTH_SHORT).show();

                        countDownTimer.cancel();
                        btnGenerate.setText("View Password");
                    }
                },
                error -> {
                    // Handle the error
                    countDownTimer.cancel();
                    btnGenerate.setText("Generate");
                    tvError.setText("This server is not working. Generate another");
                    tvError.setVisibility(View.VISIBLE);
                    //Toast.makeText(MainActivity.this, "Server not working. Try again", Toast.LENGTH_SHORT).show();

                    playFailedSound();
                });

        jsonObjectRequest.setTag("serverCheck");

        jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(
                10000, // Timeout in milliseconds (10 seconds)
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES, // Number of retries
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT // Backoff multiplier
        ));

        // Add the request to the RequestQueue
        requestQueue.add(jsonObjectRequest);
    }


    private void playSuccessSound() {
        // Release any existing MediaPlayer before creating a new one
        if (success != null) {
            success.release();
            success = null;
        }

        // Initialize a new MediaPlayer instance with the sound file
        success = MediaPlayer.create(requireContext(), R.raw.success);

        // Start playing the sound
        if (success != null) {
            success.start();

            // Release the MediaPlayer when playback is complete
            success.setOnCompletionListener(mp -> {
                mp.release();
                success = null; // Set mediaPlayer to null after release
            });
        }
    }

    private void playFailedSound() {
        // Release any existing MediaPlayer before creating a new one
        if (failed != null) {
            failed.release();
            failed = null;
        }

        // Initialize a new MediaPlayer instance with the sound file
        failed = MediaPlayer.create(requireContext(), R.raw.failed);

        // Start playing the sound
        if (failed != null) {
            failed.start();

            // Release the MediaPlayer when playback is complete
            failed.setOnCompletionListener(mp -> {
                mp.release();
                failed = null; // Set mediaPlayer to null after release
            });
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Release the MediaPlayer when the activity is destroyed
        if (success != null) {
            success.release();
            success = null;
        }
        if (failed != null) {
            failed.release();
            failed = null;
        }
    }

}
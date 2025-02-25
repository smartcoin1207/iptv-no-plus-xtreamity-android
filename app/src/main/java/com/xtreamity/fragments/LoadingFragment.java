package com.xtreamity.fragments;

import static kotlinx.coroutines.CoroutineScopeKt.CoroutineScope;

import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.opencsv.CSVReader;
import com.xtreamity.MainActivity;
import com.xtreamity.R;
import com.xtreamity.utils.ServerDetailsModal;
import com.xtreamity.utils.SupabaseHelper;
import com.xtreamity.utils.CloudflareR2Helper;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.zip.GZIPInputStream;

public class LoadingFragment extends Fragment {

    private List<ServerDetailsModal> serverDetailsList;
    private View fragmentView;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (serverDetailsList == null)
            serverDetailsList = new ArrayList<>();
        loadFileFromCloudflareR2();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        fragmentView = inflater.inflate(R.layout.fragment_loading, container, false);

        if (serverDetailsList == null)
            serverDetailsList = new ArrayList<>();

        fragmentView.findViewById(R.id.loading_error_btn)
                .setOnClickListener(v -> loadFileFromCloudflareR2());

        return fragmentView;
    }

    private void loadFileFromCloudflareR2() { 
        CloudflareR2Helper cloudflareR2Helper = new CloudflareR2Helper();

        cloudflareR2Helper.downloadFile("app_servers.csv.gz", new CloudflareR2Helper.Callback() {
            @Override
            public void onSuccess(byte[] data) {
                List<String[]> csvData = decompressAndParseCsv(data);
                populateServerDetailsList(csvData);
                requireActivity().runOnUiThread(() -> {
                    if (!serverDetailsList.isEmpty()) {
                        MainActivity.serverDetailsList = serverDetailsList;
                        requireActivity().getSupportFragmentManager().beginTransaction()
                            .replace(R.id.content_frame, new HomeFragment())
                            .commit();
                    } else {
                        handleFailure();
                    }
                });
            }

            @Override
            public void onFailure(String errorMessage) {
                handleFailure();
            }
        });
    }

    private void loadFileFromAppwrite() {
        // Create a background thread pool
        ExecutorService executorService = Executors.newSingleThreadExecutor();

        // Run the network operation on a background thread
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                String projectId = "67b32a5100047fb91829";  // Replace with your Appwrite project ID
                String bucketId = "67b32a6600026ac1a965";  // Replace with your Appwrite bucket ID
                String fileId = "servers";  // Replace with your Appwrite file ID

                String endpoint = "https://cloud.appwrite.io/v1/storage/buckets/" + bucketId + "/files/" + fileId + "/download";
                String apiKey = "your_api_key_here";  // Replace with your Appwrite API key

                try {
                    URL url = new URL(endpoint);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");
                    connection.setRequestProperty("X-Appwrite-Project", projectId);
                    connection.setRequestProperty("X-Appwrite-Key", apiKey);  // Replace with your Appwrite API key
                    connection.setConnectTimeout(3000);  // Set timeout (optional)
                    connection.setReadTimeout(5000);     // Set read timeout (optional)

                    int statusCode = connection.getResponseCode();
                    if (statusCode == HttpURLConnection.HTTP_OK) {
                        InputStream inputStream = connection.getInputStream();
                        byte[] data = readInputStream(inputStream);
                        List<String[]> csvData = decompressAndParseCsv(data);
                        populateServerDetailsList(csvData);

                        // Post the result back to the main thread
                        requireActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (!serverDetailsList.isEmpty()) {
                                    MainActivity.serverDetailsList = serverDetailsList;
                                    requireActivity().getSupportFragmentManager().beginTransaction()
                                            .replace(R.id.content_frame, new HomeFragment())
                                            .commit();
                                } else {
                                    fragmentView.findViewById(R.id.loading_content).setVisibility(View.GONE);
                                    fragmentView.findViewById(R.id.loading_error).setVisibility(View.VISIBLE);
                                }
                            }
                        });
                    } else {
                        // Handle non-OK response
                        handleFailure();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    handleFailure();
                }
            }
        });

        // You can shut down the executor service when done
        executorService.shutdown();
    }


    private byte[] readInputStream(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int bytesRead;
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            byteArrayOutputStream.write(buffer, 0, bytesRead);
        }
        return byteArrayOutputStream.toByteArray();
    }

    private void handleFailure() {
        requireActivity().runOnUiThread(() -> {
            fragmentView.findViewById(R.id.loading_content).setVisibility(View.GONE);
            fragmentView.findViewById(R.id.loading_error).setVisibility(View.VISIBLE);
        });
    }

    private List<String[]> decompressAndParseCsv(byte[] gzippedData) {
        List<String[]> data = new ArrayList<>();
        try {
            // Decompress the gzipped data
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(gzippedData);
            GZIPInputStream gzipInputStream = new GZIPInputStream(byteArrayInputStream);
            InputStreamReader inputStreamReader = new InputStreamReader(gzipInputStream);
            CSVReader csvReader = new CSVReader(inputStreamReader);
            String[] nextLine;

            // Read the CSV file line by line
            while ((nextLine = csvReader.readNext()) != null) {
                data.add(nextLine);  // Add each line to the list
            }

            csvReader.close();
            gzipInputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return data;
    }

    // Method to populate the server details list
    private void populateServerDetailsList(List<String[]> csvData) {
        for (String[] row : csvData) {

            if (row.length == 4) {
                String url = row[0];
                String username = row[1];
                String password = row[2];
                String region = row[3];

                ServerDetailsModal serverDetails = new ServerDetailsModal(url, username, password, region);
                serverDetailsList.add(serverDetails);
            } else if (row.length == 3) {
                String url = row[0];
                String username = row[1];
                String password = row[2];
                String region = "unknown";

                ServerDetailsModal serverDetails = new ServerDetailsModal(url, username, password, region);
                serverDetailsList.add(serverDetails);
            }
        }
    }


}
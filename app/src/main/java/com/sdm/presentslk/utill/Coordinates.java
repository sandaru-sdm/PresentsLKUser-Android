package com.sdm.presentslk.utill;

import android.os.AsyncTask;
import android.util.Log;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class Coordinates {

    public static class CoordinatesResult {
        private double latitude;
        private double longitude;

        public CoordinatesResult(double latitude, double longitude) {
            this.latitude = latitude;
            this.longitude = longitude;
        }

        public double getLatitude() {
            return latitude;
        }

        public double getLongitude() {
            return longitude;
        }
    }

    public static CoordinatesResult getCoordinates(String address) {
        try {
            String apiKey = "AIzaSyDRcoSvW7t6y8_qTTW4RhUGrRPnvfwpaxU"; // Replace with your actual API key

            // Construct the URL for the Geocoding API
            String apiUrl = "https://maps.googleapis.com/maps/api/geocode/json?address="
                    + address + "&key=" + apiKey;

            URL url = new URL(apiUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            // Read the response
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                response.append(line);
            }

            reader.close();
            connection.disconnect();

            // Parse the JSON response
            JsonObject jsonResponse = JsonParser.parseString(response.toString()).getAsJsonObject();

            // Check if the API request was successful
            String status = jsonResponse.get("status").getAsString();
            if ("OK".equals(status)) {
                // Extract latitude and longitude
                JsonArray results = jsonResponse.getAsJsonArray("results");
                JsonObject location = results.get(0).getAsJsonObject().getAsJsonObject("geometry")
                        .getAsJsonObject("location");

                double latitude = location.get("lat").getAsDouble();
                double longitude = location.get("lng").getAsDouble();

                // Return the result
                return new CoordinatesResult(latitude, longitude);
            } else {
                // Handle the error
                Log.e("MapApi", "Geocoding API request failed with status: " + status);
                return null;
            }

        } catch (Exception e) {
            e.printStackTrace();
            // Handle the error
            Log.e("MapApi", "Exception occurred: " + e.getMessage());
            return null;
        }
    }
}



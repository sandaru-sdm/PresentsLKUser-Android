package com.sdm.presentslk.utill;

import android.os.AsyncTask;
import android.util.Log;

public class MapDetails extends AsyncTask<String, Void, Coordinates.CoordinatesResult> {

    String latitude, longitude;

    @Override
    protected Coordinates.CoordinatesResult doInBackground(String... addresses) {
        return Coordinates.getCoordinates(addresses[0]);
    }

    @Override
    protected void onPostExecute(Coordinates.CoordinatesResult result) {
        if (result != null) {
            latitude = String.valueOf(result.getLatitude());
            longitude = String.valueOf(result.getLongitude());

            // Handle the result (latitude and longitude)
            Log.d("MapApi", "Latitude: " + latitude);
            Log.d("MapApi", "Longitude: " + longitude);

            // Save latitude and longitude to Firebase Database or perform other actions
        } else {
            // Handle the error
            Log.e("MapApi", "Failed to get coordinates");
        }
    }
}

package com.aprbrother.ablight;

import org.json.JSONArray;
import org.json.JSONObject;
import android.os.Bundle;
import android.util.Log;
import java.util.Scanner;
import java.util.Set;
import java.util.Map;
import java.util.TreeMap;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.io.BufferedReader;
import java.io.IOException;
import org.json.JSONException;
import java.io.InputStream;
import java.io.InputStreamReader;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

/**
 * Created by Eric on 6/7/2015.
 */
public class JasonParse {

    // JSON Node names
    private static final String TAG_BEACON = "beacons";
    private static final String TAG_ID = "userID";
    private static final String TAG_UUID = "UUID";
    private static final String TAG_MAJOR = "major";
    private static final String TAG_MINOR = "minor";
    private static final String TAG_RSSI = "rssi";

    public void JasonParse (String jsonStr) throws IOException {
        if (jsonStr != null) {
            try {
                JSONObject jsonObj = new JSONObject(jsonStr);
                // Getting JSON Array node
                JSONArray beacons = jsonObj.getJSONArray(TAG_BEACON);
                TreeMap beaconTreeMap = new TreeMap();
                int[] minorIDArray = new int[beacons.length()];
                double[] rssiArray = new double[beacons.length()];

                // Check if json is to be proecessed
                if (beacons.length() > 3) {
                    // looping through all beacon objects
                    for (int i = 0; i < beacons.length(); i++) {
                        JSONObject c = beacons.getJSONObject(i);

                        String id = c.getString(TAG_ID);
                        String uuid = c.getString(TAG_UUID);
                        String major = c.getString(TAG_MAJOR);
                        String minor = c.getString(TAG_MINOR);
                        String rssi = c.getString(TAG_RSSI);

                        // Casting of variables
                        double rssidouble = (double)Integer.parseInt(rssi.toString());
                        int minorint = Integer.parseInt(minor.toString());

                        // storing variables in data structure
                        beaconTreeMap.put(minorint,rssidouble);
                        minorIDArray[i] = minorint;

                    }
                    // sort the array
                    Arrays.sort(minorIDArray);
                    // get the list of rssi
                    for (int d = 0; d < beacons.length(); d++) {
                        rssiArray[d] = beaconTreeMap.get(minorIDArray[d]);
                    }
                    // call Jack's function with beaconTreeMap
                    Predict p = new Predict(rssiArray);

                } else {
                    Log.e("ServiceHandler", "Couldn't get any enough beacon data");
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            Log.e("ServiceHandler", "Couldn't get any data from the json package");
        }

    }

}

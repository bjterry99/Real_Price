package com.bjterry99gmail.realprice;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Vibrator;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import static java.lang.Double.parseDouble;

public class HomeFrag extends Fragment {
    InputStream inputStream;
    String state;
    String country;
    String tax;
    String USA = "United States";
    String zip;
    String city;
    String checkzip = "0";
    double realtax;
    double lat;
    double lon;
    double round;
    double perc;
    double vib;
    HashMap<String, String> map = new HashMap<>();
    String[] data;
    List<Address> address;
    Geocoder geocoder;

    private AdView mAdView;

    private static final String TAG = Calculator.class.getSimpleName();
    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 34;
    private FusedLocationProviderClient mFusedLocationClient;
    protected Location mLastLocation;

    public HomeFrag() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        final View v =  inflater.inflate(R.layout.fragment_home, container, false);

        // AdMob app ID: ca-app-pub-4416411796336827~6432135856
        // AdMob ad unit ID: ca-app-pub-4416411796336827/4113503398
        // test ID: ca-app-pub-3940256099942544/6300978111
        MobileAds.initialize(this.getActivity(), "ca-app-pub-4416411796336827~6432135856");
        mAdView = v.findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        //setting up stuff
        geocoder = new Geocoder(this.getActivity(), Locale.getDefault());
        final Vibrator vibe = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this.getActivity());

        //button action
        Button button = v.findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText price = v.findViewById(R.id.price);
                TextView result = v.findViewById(R.id.result);
                TextView local = v.findViewById(R.id.local);
                TextView area = v.findViewById(R.id.area);
                TextView tax = v.findViewById(R.id.tax);

                //check for location
                if(mLastLocation == null) {
                    Toast.makeText(getActivity().getApplicationContext(), getString(R.string.permission_rationale), Toast.LENGTH_LONG).show();
                    return;
                }
                //check for number
                if (price.getText().toString().equals("")) {
                    Toast.makeText(getActivity().getApplicationContext(), "Please enter a product price", Toast.LENGTH_SHORT).show();
                    return;
                }
                //check USA
                if (!country.equals(USA)) {
                    Toast.makeText(getActivity().getApplicationContext(), "Real Price only works in the USA", Toast.LENGTH_LONG).show();
                    return;
                }

                double num1 = Double.parseDouble(price.getText().toString());
                if (num1 != vib) vibe.vibrate(25);
                //check for change in location to read data
                if (!checkzip.equals(zip)) {
                    readData();
                    Toast.makeText(getActivity().getApplicationContext(), "Found your local sales tax rate", Toast.LENGTH_SHORT).show();
                }

                checkzip = zip;
                vib = num1;
                double total = (num1 * realtax) + num1;
                round = Math.round(total * 100.0) / 100.0;

                //set result text
                result.setText(String.format( "$%.2f", round));

                perc = realtax * 100.0;
                local.setText("Your local sales tax rate: " + String.format( "%.3f", perc) + "%");
                tax.setText("Tax collected: $" + String.format("%.2f", round - num1));
                area.setText(city + ", " + state + " " + zip);
            }
        });

        return v;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (!checkPermissions()) requestPermissions();
        else getLastLocation();
    }

        //get location
    @SuppressWarnings("MissingPermission")
    public void getLastLocation() {
        Task<Location> key = mFusedLocationClient.getLastLocation().addOnCompleteListener(this.getActivity(), new OnCompleteListener<Location>() {
            @Override
            public void onComplete(@NonNull Task<Location> task) {
                if (task.isSuccessful() && task.getResult() != null) {
                    mLastLocation = task.getResult();

                    //set location
                    lat = mLastLocation.getLatitude();
                    lon = mLastLocation.getLongitude();

                    try {
                        address = geocoder.getFromLocation(lat, lon, 1);

                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    //set area
                    state = address.get(0).getAdminArea();
                    country = address.get(0).getCountryName();
                    zip = address.get(0).getPostalCode();
                    city = address.get(0).getLocality();
                    System.out.println(state);
                    System.out.println(country);
                    System.out.println(zip);
                    System.out.println(city);

                } else {
                    Log.w(TAG, "getLastLocation:exception", task.getException());
                    showSnackbar(getString(R.string.no_location_detected));
                }
            }
        });
    }

    //reading tax data
    public void readData() {
        if (state.equals("Arizona")){
            inputStream = getResources().openRawResource(R.raw.arizona);

            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            try {
                String csvLine;
                while ((csvLine = reader.readLine()) != null)
                {
                    data=csvLine.split(",");
                    try{

                        map.put(data[0], data[1]);
                        System.out.println(map);
                        tax = map.get(zip);

                    }catch (Exception e){
                      //  Log.e("Problem",e.toString());
                    }
                }
            }
            catch (IOException ex) {
                throw new RuntimeException("Error in reading CSV file: "+ex);
            }
            System.out.println(tax);
            realtax = parseDouble(tax);
        }
        if (state.equals("District of Columbia")){
            inputStream = getResources().openRawResource(R.raw.dc);

            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            try {
                String csvLine;
                while ((csvLine = reader.readLine()) != null)
                {
                    data=csvLine.split(",");
                    try{

                        map.put(data[0], data[1]);
                        System.out.println(map);
                        tax = map.get(zip);

                    }catch (Exception e){
                      //  Log.e("Problem",e.toString());
                    }
                }
            }
            catch (IOException ex) {
                throw new RuntimeException("Error in reading CSV file: "+ex);
            }
            System.out.println(tax);
            realtax = parseDouble(tax);
        }
        if (state.equals("California")){
            inputStream = getResources().openRawResource(R.raw.california);

            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            try {
                String csvLine;
                while ((csvLine = reader.readLine()) != null)
                {
                    data=csvLine.split(",");
                    try{

                        map.put(data[0], data[1]);
                        System.out.println(map);
                        tax = map.get(zip);

                    }catch (Exception e){
                     //   Log.e("Problem",e.toString());
                    }
                }
            }
            catch (IOException ex) {
                throw new RuntimeException("Error in reading CSV file: "+ex);
            }
            System.out.println(tax);
            realtax = parseDouble(tax);
        }
        if (state.equals("Hawaii")){
            inputStream = getResources().openRawResource(R.raw.hawaii);

            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            try {
                String csvLine;
                while ((csvLine = reader.readLine()) != null)
                {
                    data=csvLine.split(",");
                    try{

                        map.put(data[0], data[1]);
                        System.out.println(map);
                        tax = map.get(zip);

                    }catch (Exception e){
                      //  Log.e("Problem",e.toString());
                    }
                }
            }
            catch (IOException ex) {
                throw new RuntimeException("Error in reading CSV file: "+ex);
            }
            System.out.println(tax);
            realtax = parseDouble(tax);
        }
        if (state.equals("Florida")){
            inputStream = getResources().openRawResource(R.raw.florida);

            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            try {
                String csvLine;
                while ((csvLine = reader.readLine()) != null)
                {
                    data=csvLine.split(",");
                    try{

                        map.put(data[0], data[1]);
                        System.out.println(map);
                        tax = map.get(zip);

                    }catch (Exception e){
                       // Log.e("Problem",e.toString());
                    }
                }
            }
            catch (IOException ex) {
                throw new RuntimeException("Error in reading CSV file: "+ex);
            }
            System.out.println(tax);
            realtax = parseDouble(tax);
        }
        if (state.equals("Alabama")){
            inputStream = getResources().openRawResource(R.raw.alabama);

            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            try {
                String csvLine;
                while ((csvLine = reader.readLine()) != null)
                {
                    data=csvLine.split(",");
                    try{

                        map.put(data[0], data[1]);
                        System.out.println(map);
                        tax = map.get(zip);

                    }catch (Exception e){
                      //  Log.e("Problem",e.toString());
                    }
                }
            }
            catch (IOException ex) {
                throw new RuntimeException("Error in reading CSV file: "+ex);
            }
            System.out.println(tax);
            realtax = parseDouble(tax);
        }
        if (state.equals("Alaska")){
            inputStream = getResources().openRawResource(R.raw.alaska);

            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            try {
                String csvLine;
                while ((csvLine = reader.readLine()) != null)
                {
                    data=csvLine.split(",");
                    try{

                        map.put(data[0], data[1]);
                        System.out.println(map);
                        tax = map.get(zip);

                    }catch (Exception e){
                       // Log.e("Problem",e.toString());
                    }
                }
            }
            catch (IOException ex) {
                throw new RuntimeException("Error in reading CSV file: "+ex);
            }
            System.out.println(tax);
            realtax = parseDouble(tax);
        }
        if (state.equals("New Jersey")){
            inputStream = getResources().openRawResource(R.raw.newjersey);

            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            try {
                String csvLine;
                while ((csvLine = reader.readLine()) != null)
                {
                    data=csvLine.split(",");
                    try{

                        map.put(data[0], data[1]);
                        System.out.println(map);
                        tax = map.get(zip);

                    }catch (Exception e){
                      //  Log.e("Problem",e.toString());
                    }
                }
            }
            catch (IOException ex) {
                throw new RuntimeException("Error in reading CSV file: "+ex);
            }
            System.out.println(tax);
            realtax = parseDouble(tax);
        }
        if (state.equals("Minnesota")){
            inputStream = getResources().openRawResource(R.raw.minnesota);

            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            try {
                String csvLine;
                while ((csvLine = reader.readLine()) != null)
                {
                    data=csvLine.split(",");
                    try{

                        map.put(data[0], data[1]);
                        System.out.println(map);
                        tax = map.get(zip);

                    }catch (Exception e){
                      //  Log.e("Problem",e.toString());
                    }
                }
            }
            catch (IOException ex) {
                throw new RuntimeException("Error in reading CSV file: "+ex);
            }
            System.out.println(tax);
            realtax = parseDouble(tax);
        }
        if (state.equals("Georgia")){
            inputStream = getResources().openRawResource(R.raw.georgia);

            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            try {
                String csvLine;
                while ((csvLine = reader.readLine()) != null)
                {
                    data=csvLine.split(",");
                    try{

                        map.put(data[0], data[1]);
                        System.out.println(map);
                        tax = map.get(zip);

                    }catch (Exception e){
                      //  Log.e("Problem",e.toString());
                    }
                }
            }
            catch (IOException ex) {
                throw new RuntimeException("Error in reading CSV file: "+ex);
            }
            System.out.println(tax);
            realtax = parseDouble(tax);
        }
        if (state.equals("Washington")){
            inputStream = getResources().openRawResource(R.raw.washington);

            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            try {
                String csvLine;
                while ((csvLine = reader.readLine()) != null)
                {
                    data=csvLine.split(",");
                    try{

                        map.put(data[0], data[1]);
                        System.out.println(map);
                        tax = map.get(zip);

                    }catch (Exception e){
                     //   Log.e("Problem",e.toString());
                    }
                }
            }
            catch (IOException ex) {
                throw new RuntimeException("Error in reading CSV file: "+ex);
            }
            System.out.println(tax);
            realtax = parseDouble(tax);
        }
        if (state.equals("North Carolina")){
            inputStream = getResources().openRawResource(R.raw.northcarolina);

            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            try {
                String csvLine;
                while ((csvLine = reader.readLine()) != null)
                {
                    data=csvLine.split(",");
                    try{

                        map.put(data[0], data[1]);
                        System.out.println(map);
                        tax = map.get(zip);

                    }catch (Exception e){
                       // Log.e("Problem",e.toString());
                    }
                }
            }
            catch (IOException ex) {
                throw new RuntimeException("Error in reading CSV file: "+ex);
            }
            System.out.println(tax);
            realtax = parseDouble(tax);
        }
        if (state.equals("Virgina")){
            inputStream = getResources().openRawResource(R.raw.virginia);

            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            try {
                String csvLine;
                while ((csvLine = reader.readLine()) != null)
                {
                    data=csvLine.split(",");
                    try{

                        map.put(data[0], data[1]);
                        System.out.println(map);
                        tax = map.get(zip);

                    }catch (Exception e){
                     //   Log.e("Problem",e.toString());
                    }
                }
            }
            catch (IOException ex) {
                throw new RuntimeException("Error in reading CSV file: "+ex);
            }
            System.out.println(tax);
            realtax = parseDouble(tax);
        }
        if (state.equals("Tennessee")){
            inputStream = getResources().openRawResource(R.raw.tennessee);

            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            try {
                String csvLine;
                while ((csvLine = reader.readLine()) != null)
                {
                    data=csvLine.split(",");
                    try{

                        map.put(data[0], data[1]);
                        System.out.println(map);
                        tax = map.get(zip);

                    }catch (Exception e){
                    //    Log.e("Problem",e.toString());
                    }
                }
            }
            catch (IOException ex) {
                throw new RuntimeException("Error in reading CSV file: "+ex);
            }
            System.out.println(tax);
            realtax = parseDouble(tax);
        }
        if (state.equals("Michigan")){
            inputStream = getResources().openRawResource(R.raw.michigan);

            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            try {
                String csvLine;
                while ((csvLine = reader.readLine()) != null)
                {
                    data=csvLine.split(",");
                    try{

                        map.put(data[0], data[1]);
                        System.out.println(map);
                        tax = map.get(zip);

                    }catch (Exception e){
                      //  Log.e("Problem",e.toString());
                    }
                }
            }
            catch (IOException ex) {
                throw new RuntimeException("Error in reading CSV file: "+ex);
            }
            System.out.println(tax);
            realtax = parseDouble(tax);
        }
        if (state.equals("Ohio")){
            inputStream = getResources().openRawResource(R.raw.ohio);

            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            try {
                String csvLine;
                while ((csvLine = reader.readLine()) != null)
                {
                    data=csvLine.split(",");
                    try{

                        map.put(data[0], data[1]);
                        System.out.println(map);
                        tax = map.get(zip);

                    }catch (Exception e){
                     //   Log.e("Problem",e.toString());
                    }
                }
            }
            catch (IOException ex) {
                throw new RuntimeException("Error in reading CSV file: "+ex);
            }
            System.out.println(tax);
            realtax = parseDouble(tax);
        }
        if (state.equals("New York")){
            inputStream = getResources().openRawResource(R.raw.newyork);

            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            try {
                String csvLine;
                while ((csvLine = reader.readLine()) != null)
                {
                    data=csvLine.split(",");
                    try{

                        map.put(data[0], data[1]);
                        System.out.println(map);
                        tax = map.get(zip);

                    }catch (Exception e){
                      //  Log.e("Problem",e.toString());
                    }
                }
            }
            catch (IOException ex) {
                throw new RuntimeException("Error in reading CSV file: "+ex);
            }
            System.out.println(tax);
            realtax = parseDouble(tax);
        }
        if (state.equals("Wisconsin")){
            inputStream = getResources().openRawResource(R.raw.wisconsin);

            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            try {
                String csvLine;
                while ((csvLine = reader.readLine()) != null)
                {
                    data=csvLine.split(",");
                    try{

                        map.put(data[0], data[1]);
                        System.out.println(map);
                        tax = map.get(zip);

                    }catch (Exception e){
                       // Log.e("Problem",e.toString());
                    }
                }
            }
            catch (IOException ex) {
                throw new RuntimeException("Error in reading CSV file: "+ex);
            }
            System.out.println(tax);
            realtax = parseDouble(tax);
        }
        if (state.equals("Oregon")){
            inputStream = getResources().openRawResource(R.raw.oregon);

            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            try {
                String csvLine;
                while ((csvLine = reader.readLine()) != null)
                {
                    data=csvLine.split(",");
                    try{

                        map.put(data[0], data[1]);
                        System.out.println(map);
                        tax = map.get(zip);

                    }catch (Exception e){
                     //   Log.e("Problem",e.toString());
                    }
                }
            }
            catch (IOException ex) {
                throw new RuntimeException("Error in reading CSV file: "+ex);
            }
            System.out.println(tax);
            realtax = parseDouble(tax);
        }
        if (state.equals("Missouri")){
            inputStream = getResources().openRawResource(R.raw.missouri);

            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            try {
                String csvLine;
                while ((csvLine = reader.readLine()) != null)
                {
                    data=csvLine.split(",");
                    try{

                        map.put(data[0], data[1]);
                        System.out.println(map);
                        tax = map.get(zip);

                    }catch (Exception e){
                     //   Log.e("Problem",e.toString());
                    }
                }
            }
            catch (IOException ex) {
                throw new RuntimeException("Error in reading CSV file: "+ex);
            }
            System.out.println(tax);
            realtax = parseDouble(tax);
        }
        if (state.equals("Maryland")){
            inputStream = getResources().openRawResource(R.raw.maryland);

            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            try {
                String csvLine;
                while ((csvLine = reader.readLine()) != null)
                {
                    data=csvLine.split(",");
                    try{

                        map.put(data[0], data[1]);
                        System.out.println(map);
                        tax = map.get(zip);

                    }catch (Exception e){
                   //     Log.e("Problem",e.toString());
                    }
                }
            }
            catch (IOException ex) {
                throw new RuntimeException("Error in reading CSV file: "+ex);
            }
            System.out.println(tax);
            realtax = parseDouble(tax);
        }
        if (state.equals("Connecticut")){
            inputStream = getResources().openRawResource(R.raw.connecticut);

            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            try {
                String csvLine;
                while ((csvLine = reader.readLine()) != null)
                {
                    data=csvLine.split(",");
                    try{

                        map.put(data[0], data[1]);
                        System.out.println(map);
                        tax = map.get(zip);

                    }catch (Exception e){
                       // Log.e("Problem",e.toString());
                    }
                }
            }
            catch (IOException ex) {
                throw new RuntimeException("Error in reading CSV file: "+ex);
            }
            System.out.println(tax);
            realtax = parseDouble(tax);
        }
        if (state.equals("Oklahoma")){
            inputStream = getResources().openRawResource(R.raw.oklahoma);

            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            try {
                String csvLine;
                while ((csvLine = reader.readLine()) != null)
                {
                    data=csvLine.split(",");
                    try{

                        map.put(data[0], data[1]);
                        System.out.println(map);
                        tax = map.get(zip);

                    }catch (Exception e){
                      //  Log.e("Problem",e.toString());
                    }
                }
            }
            catch (IOException ex) {
                throw new RuntimeException("Error in reading CSV file: "+ex);
            }
            System.out.println(tax);
            realtax = parseDouble(tax);
        }
        if (state.equals("South Carolina")){
            inputStream = getResources().openRawResource(R.raw.southcarolina);

            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            try {
                String csvLine;
                while ((csvLine = reader.readLine()) != null)
                {
                    data=csvLine.split(",");
                    try{

                        map.put(data[0], data[1]);
                        System.out.println(map);
                        tax = map.get(zip);

                    }catch (Exception e){
                      //  Log.e("Problem",e.toString());
                    }
                }
            }
            catch (IOException ex) {
                throw new RuntimeException("Error in reading CSV file: "+ex);
            }
            System.out.println(tax);
            realtax = parseDouble(tax);
        }
        if (state.equals("Kentucky")){
            inputStream = getResources().openRawResource(R.raw.kentucky);

            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            try {
                String csvLine;
                while ((csvLine = reader.readLine()) != null)
                {
                    data=csvLine.split(",");
                    try{

                        map.put(data[0], data[1]);
                        System.out.println(map);
                        tax = map.get(zip);

                    }catch (Exception e){
                      //  Log.e("Problem",e.toString());
                    }
                }
            }
            catch (IOException ex) {
                throw new RuntimeException("Error in reading CSV file: "+ex);
            }
            System.out.println(tax);
            realtax = parseDouble(tax);
        }
        if (state.equals("Louisiana")){
            inputStream = getResources().openRawResource(R.raw.louisiana);

            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            try {
                String csvLine;
                while ((csvLine = reader.readLine()) != null)
                {
                    data=csvLine.split(",");
                    try{

                        map.put(data[0], data[1]);
                        System.out.println(map);
                        tax = map.get(zip);

                    }catch (Exception e){
                      //  Log.e("Problem",e.toString());
                    }
                }
            }
            catch (IOException ex) {
                throw new RuntimeException("Error in reading CSV file: "+ex);
            }
            System.out.println(tax);
            realtax = parseDouble(tax);
        }
        if (state.equals("Utah")){
            inputStream = getResources().openRawResource(R.raw.utah);

            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            try {
                String csvLine;
                while ((csvLine = reader.readLine()) != null)
                {
                    data=csvLine.split(",");
                    try{

                        map.put(data[0], data[1]);
                        System.out.println(map);
                        tax = map.get(zip);

                    }catch (Exception e){
                      //  Log.e("Problem",e.toString());
                    }
                }
            }
            catch (IOException ex) {
                throw new RuntimeException("Error in reading CSV file: "+ex);
            }
            System.out.println(tax);
            realtax = parseDouble(tax);
        }
        if (state.equals("Indiana")){
            inputStream = getResources().openRawResource(R.raw.indiana);

            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            try {
                String csvLine;
                while ((csvLine = reader.readLine()) != null)
                {
                    data=csvLine.split(",");
                    try{

                        map.put(data[0], data[1]);
                        System.out.println(map);
                        tax = map.get(zip);

                    }catch (Exception e){
                    //    Log.e("Problem",e.toString());
                    }
                }
            }
            catch (IOException ex) {
                throw new RuntimeException("Error in reading CSV file: "+ex);
            }
            System.out.println(tax);
            realtax = parseDouble(tax);
        }
        if (state.equals("New Mexico")){
            inputStream = getResources().openRawResource(R.raw.newmexico);

            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            try {
                String csvLine;
                while ((csvLine = reader.readLine()) != null)
                {
                    data=csvLine.split(",");
                    try{

                        map.put(data[0], data[1]);
                        System.out.println(map);
                        tax = map.get(zip);

                    }catch (Exception e){
                      //  Log.e("Problem",e.toString());
                    }
                }
            }
            catch (IOException ex) {
                throw new RuntimeException("Error in reading CSV file: "+ex);
            }
            System.out.println(tax);
            realtax = parseDouble(tax);
        }
        if (state.equals("Mississippi")){
            inputStream = getResources().openRawResource(R.raw.mississippi);

            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            try {
                String csvLine;
                while ((csvLine = reader.readLine()) != null)
                {
                    data=csvLine.split(",");
                    try{

                        map.put(data[0], data[1]);
                        System.out.println(map);
                        tax = map.get(zip);

                    }catch (Exception e){
                      //  Log.e("Problem",e.toString());
                    }
                }
            }
            catch (IOException ex) {
                throw new RuntimeException("Error in reading CSV file: "+ex);
            }
            System.out.println(tax);
            realtax = parseDouble(tax);
        }
        if (state.equals("Iowa")){
            inputStream = getResources().openRawResource(R.raw.iowa);

            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            try {
                String csvLine;
                while ((csvLine = reader.readLine()) != null)
                {
                    data=csvLine.split(",");
                    try{

                        map.put(data[0], data[1]);
                        System.out.println(map);
                        tax = map.get(zip);

                    }catch (Exception e){
                       // Log.e("Problem",e.toString());
                    }
                }
            }
            catch (IOException ex) {
                throw new RuntimeException("Error in reading CSV file: "+ex);
            }
            System.out.println(tax);
            realtax = parseDouble(tax);
        }
        if (state.equals("Nebraska")){
            inputStream = getResources().openRawResource(R.raw.nebraska);

            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            try {
                String csvLine;
                while ((csvLine = reader.readLine()) != null)
                {
                    data=csvLine.split(",");
                    try{

                        map.put(data[0], data[1]);
                        System.out.println(map);
                        tax = map.get(zip);

                    }catch (Exception e){
                      //  Log.e("Problem",e.toString());
                    }
                }
            }
            catch (IOException ex) {
                throw new RuntimeException("Error in reading CSV file: "+ex);
            }
            System.out.println(tax);
            realtax = parseDouble(tax);
        }
        if (state.equals("Rhode Island")){
            inputStream = getResources().openRawResource(R.raw.rhodeisland);

            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            try {
                String csvLine;
                while ((csvLine = reader.readLine()) != null)
                {
                    data=csvLine.split(",");
                    try{

                        map.put(data[0], data[1]);
                        System.out.println(map);
                        tax = map.get(zip);

                    }catch (Exception e){
                     //   Log.e("Problem",e.toString());
                    }
                }
            }
            catch (IOException ex) {
                throw new RuntimeException("Error in reading CSV file: "+ex);
            }
            System.out.println(tax);
            realtax = parseDouble(tax);
        }
        if (state.equals("Nevada")){
            inputStream = getResources().openRawResource(R.raw.nevada);

            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            try {
                String csvLine;
                while ((csvLine = reader.readLine()) != null)
                {
                    data=csvLine.split(",");
                    try{

                        map.put(data[0], data[1]);
                        System.out.println(map);
                        tax = map.get(zip);

                    }catch (Exception e){
                      //  Log.e("Problem",e.toString());
                    }
                }
            }
            catch (IOException ex) {
                throw new RuntimeException("Error in reading CSV file: "+ex);
            }
            System.out.println(tax);
            realtax = parseDouble(tax);
        }
        if (state.equals("Maine")){
            inputStream = getResources().openRawResource(R.raw.maine);

            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            try {
                String csvLine;
                while ((csvLine = reader.readLine()) != null)
                {
                    data=csvLine.split(",");
                    try{

                        map.put(data[0], data[1]);
                        System.out.println(map);
                        tax = map.get(zip);

                    }catch (Exception e){
                     //   Log.e("Problem",e.toString());
                    }
                }
            }
            catch (IOException ex) {
                throw new RuntimeException("Error in reading CSV file: "+ex);
            }
            System.out.println(tax);
            realtax = parseDouble(tax);
        }
        if (state.equals("Arkansas")){
            inputStream = getResources().openRawResource(R.raw.arkansas);

            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            try {
                String csvLine;
                while ((csvLine = reader.readLine()) != null)
                {
                    data=csvLine.split(",");
                    try{

                        map.put(data[0], data[1]);
                        System.out.println(map);
                        tax = map.get(zip);

                    }catch (Exception e){
                      //  Log.e("Problem",e.toString());
                    }
                }
            }
            catch (IOException ex) {
                throw new RuntimeException("Error in reading CSV file: "+ex);
            }
            System.out.println(tax);
            realtax = parseDouble(tax);
        }
        if (state.equals("New Hampshire")){
            inputStream = getResources().openRawResource(R.raw.newhampshire);

            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            try {
                String csvLine;
                while ((csvLine = reader.readLine()) != null)
                {
                    data=csvLine.split(",");
                    try{

                        map.put(data[0], data[1]);
                        System.out.println(map);
                        tax = map.get(zip);

                    }catch (Exception e){
                     //   Log.e("Problem",e.toString());
                    }
                }
            }
            catch (IOException ex) {
                throw new RuntimeException("Error in reading CSV file: "+ex);
            }
            System.out.println(tax);
            realtax = parseDouble(tax);
        }
        if (state.equals("Wyoming")){
            inputStream = getResources().openRawResource(R.raw.wyoming);

            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            try {
                String csvLine;
                while ((csvLine = reader.readLine()) != null)
                {
                    data=csvLine.split(",");
                    try{

                        map.put(data[0], data[1]);
                        System.out.println(map);
                        tax = map.get(zip);

                    }catch (Exception e){
                     //   Log.e("Problem",e.toString());
                    }
                }
            }
            catch (IOException ex) {
                throw new RuntimeException("Error in reading CSV file: "+ex);
            }
            System.out.println(tax);
            realtax = parseDouble(tax);
        }
        if (state.equals("Vermont")){
            inputStream = getResources().openRawResource(R.raw.vermont);

            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            try {
                String csvLine;
                while ((csvLine = reader.readLine()) != null)
                {
                    data=csvLine.split(",");
                    try{

                        map.put(data[0], data[1]);
                        System.out.println(map);
                        tax = map.get(zip);

                    }catch (Exception e){
                      //  Log.e("Problem",e.toString());
                    }
                }
            }
            catch (IOException ex) {
                throw new RuntimeException("Error in reading CSV file: "+ex);
            }
            System.out.println(tax);
            realtax = parseDouble(tax);
        }
        if (state.equals("Delaware")){
            inputStream = getResources().openRawResource(R.raw.delaware);

            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            try {
                String csvLine;
                while ((csvLine = reader.readLine()) != null)
                {
                    data=csvLine.split(",");
                    try{

                        map.put(data[0], data[1]);
                        System.out.println(map);
                        tax = map.get(zip);

                    }catch (Exception e){
                      //  Log.e("Problem",e.toString());
                    }
                }
            }
            catch (IOException ex) {
                throw new RuntimeException("Error in reading CSV file: "+ex);
            }
            System.out.println(tax);
            realtax = parseDouble(tax);
        }
        if (state.equals("West Virgina")){
            inputStream = getResources().openRawResource(R.raw.westvirginia);

            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            try {
                String csvLine;
                while ((csvLine = reader.readLine()) != null)
                {
                    data=csvLine.split(",");
                    try{

                        map.put(data[0], data[1]);
                        System.out.println(map);
                        tax = map.get(zip);

                    }catch (Exception e){
                     //   Log.e("Problem",e.toString());
                    }
                }
            }
            catch (IOException ex) {
                throw new RuntimeException("Error in reading CSV file: "+ex);
            }
            System.out.println(tax);
            realtax = parseDouble(tax);
        }
        if (state.equals("Idaho")){
            inputStream = getResources().openRawResource(R.raw.idaho);

            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            try {
                String csvLine;
                while ((csvLine = reader.readLine()) != null)
                {
                    data=csvLine.split(",");
                    try{

                        map.put(data[0], data[1]);
                        System.out.println(map);
                        tax = map.get(zip);

                    }catch (Exception e){
                      //  Log.e("Problem",e.toString());
                    }
                }
            }
            catch (IOException ex) {
                throw new RuntimeException("Error in reading CSV file: "+ex);
            }
            System.out.println(tax);
            realtax = parseDouble(tax);
        }
        if (state.equals("North Dakota")){
            inputStream = getResources().openRawResource(R.raw.northdakota);

            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            try {
                String csvLine;
                while ((csvLine = reader.readLine()) != null)
                {
                    data=csvLine.split(",");
                    try{

                        map.put(data[0], data[1]);
                        System.out.println(map);
                        tax = map.get(zip);

                    }catch (Exception e){
                      //  Log.e("Problem",e.toString());
                    }
                }
            }
            catch (IOException ex) {
                throw new RuntimeException("Error in reading CSV file: "+ex);
            }
            System.out.println(tax);
            realtax = parseDouble(tax);
        }
        if (state.equals("South Dakota")){
            inputStream = getResources().openRawResource(R.raw.southdakota);

            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            try {
                String csvLine;
                while ((csvLine = reader.readLine()) != null)
                {
                    data=csvLine.split(",");
                    try{

                        map.put(data[0], data[1]);
                        System.out.println(map);
                        tax = map.get(zip);

                    }catch (Exception e){
                      //  Log.e("Problem",e.toString());
                    }
                }
            }
            catch (IOException ex) {
                throw new RuntimeException("Error in reading CSV file: "+ex);
            }
            System.out.println(tax);
            realtax = parseDouble(tax);
        }
        if (state.equals("Texas")){
            inputStream = getResources().openRawResource(R.raw.texas);

            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            try {
                String csvLine;
                while ((csvLine = reader.readLine()) != null)
                {
                    data=csvLine.split(",");
                    try{

                        map.put(data[0], data[1]);
                        System.out.println(map);
                        tax = map.get(zip);

                    }catch (Exception e){
                      //  Log.e("Problem",e.toString());
                    }
                }
            }
            catch (IOException ex) {
                throw new RuntimeException("Error in reading CSV file: "+ex);
            }
            System.out.println(tax);
            realtax = parseDouble(tax);
        }

    }

    //this is all related to reading location and I copied it all from the internet #notsorry
    private void showSnackbar(final String text) {
        View container = getActivity().findViewById(R.id.price);
        if (container != null) {
            Snackbar.make(container, text, Snackbar.LENGTH_LONG).show();
        }
    }
    private void showSnackbar(final int mainTextStringId, final int actionStringId,
                              View.OnClickListener listener) {
        Snackbar.make(getActivity().findViewById(android.R.id.content),
                getString(mainTextStringId),
                Snackbar.LENGTH_INDEFINITE)
                .setAction(getString(actionStringId), listener).show();
    }
    private boolean checkPermissions() {
        int permissionState = ActivityCompat.checkSelfPermission(this.getActivity(),
                Manifest.permission.ACCESS_COARSE_LOCATION);
        return permissionState == PackageManager.PERMISSION_GRANTED;
    }
    private void startLocationPermissionRequest() {
        ActivityCompat.requestPermissions(HomeFrag.this.getActivity(),
                new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                REQUEST_PERMISSIONS_REQUEST_CODE);
    }
    private void requestPermissions() {
        boolean shouldProvideRationale =
                ActivityCompat.shouldShowRequestPermissionRationale(this.getActivity(),
                        Manifest.permission.ACCESS_COARSE_LOCATION);
        if (shouldProvideRationale) {
           // Log.i(TAG, "Displaying permission rationale to provide additional context.");
            showSnackbar(R.string.permission_rationale, android.R.string.ok,
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            startLocationPermissionRequest();
                        }
                    });
        } else {
          //  Log.i(TAG, "Requesting permission");
            startLocationPermissionRequest();
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
      //  Log.i(TAG, "onRequestPermissionResult");
        if (requestCode == REQUEST_PERMISSIONS_REQUEST_CODE) {
            if (grantResults.length <= 0) {
              //  Log.i(TAG, "User interaction was cancelled.");
            } else if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLastLocation();
            } else {
                showSnackbar(R.string.permission_denied_explanation, R.string.settings,
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent intent = new Intent();
                                intent.setAction(
                                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                Uri uri = Uri.fromParts("package",
                                        BuildConfig.APPLICATION_ID, null);
                                intent.setData(uri);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                            }
                        });
            }
        }
    }
}

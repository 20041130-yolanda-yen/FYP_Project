package com.example.fypproject;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import au.com.bytecode.opencsv.CSVWriter;

public class MainActivity extends AppCompatActivity {

    ImageView btn_cb, btn_l, btn_ws, btn_anpr, btn_r, btn_e;

    DatabaseReference databaseReference;
    ScannedVehiclesHelper scannedVehiclesHelper;
    List<ScannedVehicles> everything;

    public static boolean hasPermissions(Context context, String... permissions) {
        if (context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        int PERMISSION_ALL = 1;
        String[] PERMISSIONS = {
                Manifest.permission.CAMERA,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION

        };

        if (!hasPermissions(this, PERMISSIONS)) {
            ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_ALL);
        }

        final LocationManager manager = (LocationManager) getSystemService( Context.LOCATION_SERVICE );
        if ( !manager.isProviderEnabled( LocationManager.GPS_PROVIDER ) ) {
            buildAlertMessageNoGps();
        }

        btn_cb = findViewById(R.id.btn_color_brand);
        btn_l = findViewById(R.id.btn_location);
        btn_ws = findViewById(R.id.btn_webScrap);
        btn_anpr = findViewById(R.id.btn_ANPR);

        btn_r = findViewById(R.id.btn_retrieve);
        btn_e = findViewById(R.id.btn_export);

        if (isNetworkAvailiable(this)) {
            GetFirebaseData getFirebaseData = new GetFirebaseData();
            getFirebaseData.execute();

        } else {
            diaglog("No internet connection.");
        }
        btn_cb.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, BrandColor.class)));

        btn_l.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, Location.class)));

        btn_ws.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, Webscrap.class)));

        btn_anpr.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, ANPR.class)));

        btn_r.setOnClickListener(v -> {
            if (isNetworkAvailiable(MainActivity.this)) {
                GetFirebaseData getFirebaseData = new GetFirebaseData();
                getFirebaseData.execute();

            } else {
                diaglog("No internet connection.");
            }
        });

        btn_e.setOnClickListener(v -> {
            scannedVehiclesHelper = new ScannedVehiclesHelper(MainActivity.this);
            String dayBefore = getCalculatedDate("dd-MM-yyyy", -1);
            scannedVehiclesHelper.deleteOld(dayBefore);
            importData();
        });
    }

    // ---------------------------
    private class GetFirebaseData extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Toast.makeText(MainActivity.this, "Retrieving data...", Toast.LENGTH_SHORT).show();

        }

        @Override
        protected Void doInBackground(Void... voids) {

            FirebaseDataHelper fireDatabaseHelper = new FirebaseDataHelper(MainActivity.this);
            fireDatabaseHelper.deleteAll();
            databaseReference = FirebaseDatabase.getInstance("https://mytestproject-c4e29-default-rtdb.asia-southeast1.firebasedatabase.app").getReference("Vehicles");
            databaseReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for (DataSnapshot data : snapshot.getChildren()) {
                        String driver_id = data.child("driver_id").getValue().toString().trim();
                        String car_num = data.child("car_num").getValue().toString().trim();
                        String driver_name = data.child("driver_name").getValue().toString().trim();
                        String color = data.child("color").getValue().toString().trim();
                        String brand = data.child("brand").getValue().toString().trim();
                        String approved = data.child("parking_space").getValue().toString().trim();
                        String type = data.child("carpark_type").getValue().toString().trim();

                        FirebaseData firebaseData;
                        try {
                            firebaseData = new FirebaseData(-1, car_num, driver_id, driver_name, color, brand, approved, type);

                        } catch (Exception e) {
                            Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                            firebaseData = new FirebaseData(-1, "Error", "Error", "Error", "Error", "Error", "Error","Error");
                        }
                        fireDatabaseHelper.addOne(firebaseData);
                    }

                    Toast.makeText(MainActivity.this, "Complete.", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);


        }
    }

    // ---------------------------
    public static boolean isNetworkAvailiable(MainActivity ctx) {
        ConnectivityManager connectivityManager = (ConnectivityManager) ctx
                .getSystemService(MainActivity.CONNECTIVITY_SERVICE);
        if ((connectivityManager
                .getNetworkInfo(ConnectivityManager.TYPE_MOBILE) != null && connectivityManager
                .getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED)
                || (connectivityManager
                .getNetworkInfo(ConnectivityManager.TYPE_WIFI) != null && connectivityManager
                .getNetworkInfo(ConnectivityManager.TYPE_WIFI)
                .getState() == NetworkInfo.State.CONNECTED)) {
            return true;
        } else {
            return false;
        }
    }

    // ---------------------------
    public static String getCalculatedDate(String dateFormat, int days) {
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat s = new SimpleDateFormat(dateFormat);
        cal.add(Calendar.DAY_OF_YEAR, days);
        return s.format(new Date(cal.getTimeInMillis()));
    }

    // ---------------------------
    private class ExportDatabaseCSVTask extends AsyncTask<String, String, String> {
        private final ProgressDialog dialog = new ProgressDialog(MainActivity.this);

        @Override
        protected void onPreExecute() {
            this.dialog.setMessage("Exporting database...");
            this.dialog.show();
        }

        protected String doInBackground(final String... args) {
            File exportDir = new File(Environment.getExternalStorageDirectory(), "Download");
            if (!exportDir.exists()) {
                exportDir.mkdirs();
            }

            File file = new File(exportDir, "ExcelFile.csv");
            try {

                file.createNewFile();
                CSVWriter csvWrite = new CSVWriter(new FileWriter(file));

                //Headers
                String arrStr1[] = {"ID", "Car Num", "Location", "Date", "Time", "Type","Result"};
                csvWrite.writeNext(arrStr1);


                for (int i = 0; i < everything.size(); i++) {
                    String arrStr[] = {String.valueOf(everything.get(i).getId()), everything.get(i).getCar_num(), everything.get(i).getLocation(), everything.get(i).getDate(), everything.get(i).getTime(), everything.get(i).getType(),everything.get(i).getResults()};
                    csvWrite.writeNext(arrStr);

                }

                csvWrite.close();
                return "";
            } catch (IOException e) {
                Log.e("MainActivity", e.getMessage(), e);
                return "";
            }
        }

        @SuppressLint("NewApi")
        @Override
        protected void onPostExecute(final String success) {

            if (this.dialog.isShowing()) {
                this.dialog.dismiss();
            }
            if (success.isEmpty()) {
                Toast.makeText(MainActivity.this, "Export successful to downloads folder!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(MainActivity.this, "Export failed!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // ---------------------------
    private void importData() {
        scannedVehiclesHelper = new ScannedVehiclesHelper(MainActivity.this);
        everything = scannedVehiclesHelper.getEveryThing();

        if (everything.size() > 0) {
            if (!isExternalStorageAvailable() || isExternalStorageReadOnly()) {
                Toast.makeText(getApplicationContext(), "Storage not available or read only", Toast.LENGTH_SHORT).show();
            } else {
                ExportDatabaseCSVTask task = new ExportDatabaseCSVTask();
                task.execute();
            }
        } else {
            Toast.makeText(this, "No scanned vehicles", Toast.LENGTH_SHORT).show();
        }
    }
    private static boolean isExternalStorageReadOnly() {
        String externalStorageState = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED_READ_ONLY.equals(externalStorageState);
    }

    private static boolean isExternalStorageAvailable() {
        String externalStorageState = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(externalStorageState);
    }

    // ---------------------------
    private void buildAlertMessageNoGps() {
        final androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setMessage("Your GPS seems to be disabled, do you want to enable it?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        dialog.cancel();
                        finish();
                    }
                });
        final androidx.appcompat.app.AlertDialog alert = builder.create();
        alert.show();
    }

    // ---------------------------
    private void diaglog(String message) {
        AlertDialog.Builder builder1 = new AlertDialog.Builder(MainActivity.this);
        builder1.setMessage(message);
        builder1.setCancelable(false);

        builder1.setPositiveButton(
                "Exit",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        finish();
                    }
                });

        AlertDialog alert11 = builder1.create();
        alert11.show();
    }
}
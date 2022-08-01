package com.example.fypproject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

import org.checkerframework.checker.units.qual.A;
import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ANPR extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2{

    private static final String TYPE = "ANPR";

    LocationManager locationManager;
    LocationListener listener;

    CircularProgressIndicator circularProgressIndicator;

    String output = null;
    String colorsForDB = null;
    String brandName = null;
    String driverName = null;
    String driverOpp = null;
    String result = null;

    ImageView capture_image, current_image, edit_location;
    Bitmap bitmap = null;
    Mat mRgba;

    int clicked = 0;

    TextView output_text, location_text;
    Zoomcameraview zoomcameraview;

    FirebaseDataHelper firebaseDataHelper;

    String carpark = "";
    String location1 = null;

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface
                        .SUCCESS: {
                    Log.i(TYPE, "OpenCv Is loaded");
                    zoomcameraview.enableView();
                }
                default: {
                    super.onManagerConnected(status);
                }
                break;
            }
        }
    };

    @Override
    public void onBackPressed() {
        finish();
    }

    @SuppressLint("MissingPermission")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_anpr);
        zoomcameraview = findViewById(R.id.ZoomCameraView);
        zoomcameraview.setVisibility(SurfaceView.VISIBLE);
        zoomcameraview.setZoomControl((SeekBar) findViewById(R.id.CameraZoomControls));
        zoomcameraview.setCvCameraViewListener(this);

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        output_text = findViewById(R.id.output_text);
        output_text.setVisibility(View.INVISIBLE);

        location_text = findViewById(R.id.location_text);

        listener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                try {
                    Geocoder geocoder = new Geocoder(ANPR.this);
                    List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                    String address = addresses.get(0).getAddressLine(0);
                    location1 = address;
                    updateCarpark(address);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {
            }

            @Override
            public void onProviderEnabled(String s) {
            }

            @Override
            public void onProviderDisabled(String s) {
                Intent i = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(i);
            }
        };
        configure_button();

        edit_location = findViewById(R.id.edit_location);
        edit_location.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialog("Update location manually");
            }
        });

        current_image = findViewById(R.id.current_image);

        capture_image = findViewById(R.id.capture_image);
        capture_image.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    return true;
                }
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (clicked == 0) {
                        if (carpark.equals("") || carpark == null) {
                            showDialog("Unable to determine car park");
                        } else {
                            clicked = 1;
                            capture_image.setColorFilter(Color.DKGRAY);
                            bitmap = Bitmap.createBitmap(mRgba.cols(), mRgba.rows(), Bitmap.Config.ARGB_8888);
                            Utils.matToBitmap(mRgba, bitmap);
                            zoomcameraview.disableView();
                            current_image.setVisibility(View.VISIBLE);
                            current_image.setImageBitmap(bitmap);
                            getText(bitmap);

                        }
                    } else if (clicked == 1) {
                        clicked = 0;
                        configure_button();
                        capture_image.setColorFilter(Color.WHITE);
                        zoomcameraview.enableView();
                        current_image.setVisibility(View.GONE);
                        current_image.setImageBitmap(null);
                        output = null;
                        brandName = null;
                        colorsForDB = null;
                        driverName = null;
                        driverOpp = null;
                        updateUI();
                        output_text.setVisibility(View.INVISIBLE);
                    }
                    return true;
                }
                return false;
            }
        });
    }

    // ---- Trigger GPS ---- //
    void configure_button() {
        // first check for permissions
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.INTERNET}
                        , 10);
            }

            return;
        }
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 5000, 0, listener);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 10:
                configure_button();
                break;
            default:
                break;
        }
    }


    // ---- Camera Method ---- //
    @Override
    public void onCameraViewStarted(int width, int height) {
        mRgba = new Mat(height, width, CvType.CV_8UC4);
    }

    @Override
    public void onCameraViewStopped() {
        mRgba.release();
    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        mRgba = inputFrame.rgba();
        return mRgba;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (OpenCVLoader.initDebug()) {
            //if load success
            Log.d(TYPE, "Opencv initialization is done");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        } else {
            //if not loaded
            Log.d(TYPE, "Opencv is not loaded. try again");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_4_0, this, mLoaderCallback);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (zoomcameraview != null) {
            zoomcameraview.disableView();
        }
    }

    public void onDestroy() {
        super.onDestroy();
        if (zoomcameraview != null) {
            zoomcameraview.disableView();
        }

    }

    // -- Update UI text -- //
    private void updateUI() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                output_text.setText(
                        "Car num: " + output + "\n\n"

                                + "Brand: " + brandName + "\n"
                                + "Color: " + colorsForDB + "\n\n"

                                + "Driver name: " + driverName + "\n"
                                + "Driver occupation: " + driverOpp

                );

                location_text.setText(
                        location1
                );
            }
        });
    }

    // -- Get Car num -- //
    private void getText(Bitmap bitmap) {

        InputImage image = InputImage.fromBitmap(bitmap, 0);

        TextRecognizer textRecognition = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);

        Task<Text> result = textRecognition.process(image).addOnSuccessListener(new OnSuccessListener<Text>() {
            @Override
            public void onSuccess(Text visionText) {

                String text = visionText.getText().replaceAll("\n", "").toUpperCase();
                text = text.replace(".", "");
                text = text.replace(" ", "");

                Pattern p = Pattern.compile("[A-Z]{3}\\d{3,4}[A-Z]");
                Matcher m = p.matcher(text);

                if (m.find()) {
                    output = m.group();
                    verifyData(output);

                } else {
                    Toast.makeText(ANPR.this, "Detected: " + text, Toast.LENGTH_LONG).show();
                }
                updateUI();
            }
        }).addOnFailureListener(e -> {
            // Task failed with an exception
            // ...
        });
    }

    // ---- Check against database ---- //
    private void verifyData(String car_num) {
        firebaseDataHelper = new FirebaseDataHelper(ANPR.this);
        FirebaseData firebaseData1 = firebaseDataHelper.getCarDetails(car_num);
        if (firebaseData1 != null) {
            colorsForDB = firebaseData1.getColor();
            brandName = firebaseData1.getBrand();
            driverName = firebaseData1.getDriver_name();
            driverOpp = firebaseData1.getApproved_parking();
            String occ = firebaseData1.getApproved_parking();
            String cpType = firebaseData1.getCarpark_type();
            String carParks = getApprovedLocation(occ,cpType);

            if (carParks.equals("Car parks 1 & 4")) {
                if (!carpark.equals("P1") && !carpark.equals("P4")) {
                    result = "Not approved";
                } else {
                    result = "Approved";
                }
            }
            else if (carParks.equals("Car parks 1,3,4")) {
                if (!carpark.equals("P1") && !carpark.equals("P3") && !carpark.equals("P4")) {
                    result = "Not approved";
                } else {
                    result = "Approved";
                }
            }
            else if (carParks.equals("Car parks 1,4,5")) {
                if (!carpark.equals("P1") && !carpark.equals("P4") && !carpark.equals("P5")) {
                    result = "Not approved";
                } else {
                    result = "Approved";
                }
            }
            else {
                result = "Approved";
            }
        } else if (firebaseData1 == null) {
            result = "Not approved";
        }

        if (result.equals("Not approved")){
            Toast.makeText(this, "Vehicle is not approved to park here: " + carpark, Toast.LENGTH_SHORT).show();
        }

        updateSQL(TYPE, output, carpark);
    }

    public String getApprovedLocation(String occ, String cpType) {
        String approve = null;
        if (occ.equals("Student")) {
            if (cpType.equals("Outdoor")){
                approve = "Car parks 1 & 4";
            }
            else {
                approve = "Car parks 1,3,4";
            }
        } else if (occ.equals("Lecturer")){
            if (cpType.equals("Outdoor")){
                approve = "Car parks 1,4,5";
            }
            else {
                approve = "Permit in all car parks";
            }
        }
        return approve;
    }

    // ---- Update Car Park Number ---- //
    private void updateCarpark(String address) {

        if (address.equals("17 Woodlands Ave 9, Singapore 738968") || address.equals("19 Woodlands Ave 9, Singapore 738969")) {
            location1 = address + " Car park 4";
            carpark = "P4";
        } else if (address.equals("39 Woodlands Ave 9, Singapore 737903") || address.equals("35 Woodlands Ave 9, Singapore 737905")
        || address.contains("809") || address.contains("876") || address.contains("874") || address.contains("53")
        || address.contains("43")) {
            location1 = address + " Car park 3";
            carpark = "P3";
        } else if (address.equals("27 Woodlands Ave 9, Singapore 737909")) {
            location1 = address + " Car park 2";
            carpark = "P2";
        } else if (address.equals("5 Woodlands Ave 9, Singapore 738962")) {
            location1 = address + " Car park 1";
            carpark = "P1";
        } else if (address.equals("15 Woodlands Ave 9, Singapore 738967")) {
            location1 = address + " Car park 5";
            carpark = "P5";
        } else {
            location1 = address;
            carpark = "";
        }
        updateUI();
    }

    // ---- Dialogbox for location selection ---- //
    private void showDialog(String title) {
        AlertDialog.Builder mBuilder = new AlertDialog.Builder(ANPR.this);
        View mView = getLayoutInflater().inflate(R.layout.dialog_spinner, null);
        mBuilder.setTitle(title);
        Spinner sp1 = (Spinner) mView.findViewById(R.id.spinner);
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(ANPR.this,
                android.R.layout.simple_spinner_item,
                getResources().getStringArray(R.array.carparkList));
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sp1.setAdapter(arrayAdapter);

        mBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (!sp1.getSelectedItem().toString().equals("Edit location")) {
                    carpark = sp1.getSelectedItem().toString();
                    location1 = "*Manual selection: " + sp1.getSelectedItem().toString();
                    Toast.makeText(ANPR.this, "You have selected: " + carpark, Toast.LENGTH_SHORT).show();
                    locationManager.removeUpdates(listener);
                    updateUI();
                    dialog.dismiss();
                } else {
                    Toast.makeText(ANPR.this, "No location change.", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                }
            }
        });
        mBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        mBuilder.setView(mView);
        AlertDialog alertDialog = mBuilder.create();
        alertDialog.show();
    }

    // ---- Insert into SQLite for export ---- //
    private void updateSQL(String TYPE, String carNum, String location) {
        String date = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date());
        String time = new SimpleDateFormat("HH:MM", Locale.getDefault()).format(new Date());


        ScannedVehicles scannedVehicles;
        try {
            scannedVehicles = new ScannedVehicles(-1, carNum, location, date, time, TYPE, result);

        } catch (Exception e) {
            Toast.makeText(ANPR.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            scannedVehicles = new ScannedVehicles(-1, "Error", "Error", "Error", "Error", "Error", "Error");
        }

        ScannedVehiclesHelper databasehelper = new ScannedVehiclesHelper(ANPR.this);
        boolean success = databasehelper.addOne(scannedVehicles);
    }
}
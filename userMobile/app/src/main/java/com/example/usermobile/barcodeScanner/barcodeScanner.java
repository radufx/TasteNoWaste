package com.example.usermobile.barcodeScanner;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.util.SparseArray;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.usermobile.R;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;

import java.io.IOException;
import java.util.Objects;

public class barcodeScanner extends AppCompatActivity {

    private static final int REQUEST_CAMERA_PERMISSION = 201;
    private SurfaceView surfaceView;
    private CameraSource cameraSource;
    private ToneGenerator toneGenerator;
    private TextView barcodeText;
    private String barcodeData;
    private static final int DURATION = Toast.LENGTH_LONG;
    private WebRequest webRequest;
    private String url = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanner);
        toneGenerator = new ToneGenerator(AudioManager.STREAM_MUSIC, 100);
        surfaceView = findViewById(R.id.surface_view);
        barcodeText = findViewById(R.id.barcode_text);
        initialiseDetectorsAndSources();
    }

    private void initialiseDetectorsAndSources() {
        BarcodeDetector barcodeDetector = new BarcodeDetector.Builder(this)
                .setBarcodeFormats(Barcode.ALL_FORMATS)
                .build();

        cameraSource = new CameraSource.Builder(this, barcodeDetector)
                .setRequestedPreviewSize(1920, 1080)
                .setAutoFocusEnabled(true)
                .build();

        surfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                try {
                    if (ActivityCompat.checkSelfPermission(barcodeScanner.this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                        cameraSource.start(surfaceView.getHolder());
                    } else {
                        ActivityCompat.requestPermissions(barcodeScanner.this, new
                                String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                cameraSource.stop();
            }
        });

        barcodeDetector.setProcessor(new Detector.Processor<Barcode>() {
            @Override
            public void release() {

            }

            @Override
            public void receiveDetections(Detector.Detections<Barcode> detections) {
                final SparseArray<Barcode> barcodes = detections.getDetectedItems();
                if (barcodes.size() != 0) {
                    barcodeText.post(() -> {
                        if (barcodes.valueAt(0).email != null) {
                            barcodeText.removeCallbacks(null);
                            barcodeData = barcodes.valueAt(0).email.address;
                            webRequest = new WebRequest();
                            //url = webRequest.sentWebRequest(barcodeData);
//                            Toast toast = Toast.makeText(getApplicationContext(), url, DURATION);
//                            toast.show();
                            barcodeText.setText(barcodeData);
                            toneGenerator.startTone(ToneGenerator.TONE_CDMA_PIP, 150);
                        } else {
                            barcodeData = barcodes.valueAt(0).displayValue;
                            webRequest = new WebRequest();
                            //url = webRequest.sentWebRequest(barcodeData);
                            Toast toast = Toast.makeText(getApplicationContext(), url, DURATION);
                            toast.show();
                            barcodeText.setText(barcodeData);
                            toneGenerator.startTone(ToneGenerator.TONE_CDMA_PIP, 150);
                        }
                    });
                }
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        Objects.requireNonNull(getSupportActionBar()).hide();
        cameraSource.release();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Objects.requireNonNull(getSupportActionBar()).hide();
        initialiseDetectorsAndSources();
    }
}
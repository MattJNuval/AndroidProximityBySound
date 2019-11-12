package com.example.proximitybysound;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Random;

public class MainActivity extends AppCompatActivity {



    private static final int CAMERA_PERMISSION_CODE = 100;
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;

    private MediaRecorder mRecorder  = null;

    Button s;

    TextView statusTv;
    TextView ampTv;
    TextView distTv;
    TextView timerTv;

    CountDownTimer cdt;

    private int timer = 0;


    // Requesting permission to RECORD_AUDIO
    private boolean permissionToRecordAccepted = false;
    private String [] permissions = {Manifest.permission.RECORD_AUDIO};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Checks Permission
        checkPermission(Manifest.permission.RECORD_AUDIO,REQUEST_RECORD_AUDIO_PERMISSION);

        timerTv = (TextView) findViewById(R.id.timer);
        ampTv = (TextView) findViewById(R.id.amplitude);
        distTv = (TextView) findViewById(R.id.distance);

        s = (Button) findViewById(R.id.startAndStop);
        s.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(s.getText().equals("Stop")) {
                    stop();
                    s.setText("Start");
                    timer = 0;
                    cdt.cancel();

                } else {
                    start();
                    s.setText("Stop");

                    cdt = new CountDownTimer(1000,1000) {
                        @Override
                        public void onTick(long millisUntilFinished) {
                            double amp = getAmplitude();
                            ampTv.setText("Amplitude: " + amp);
                            timerTv.setText("Timer: " + String.valueOf(timer++) + "s");
                            DistanceChecker(amp);

                        }

                        @Override
                        public void onFinish() {
                            start();
                        }
                    }.start();

                }
            }
        });

    }

    public void DistanceChecker(double amplitude) {
        if(amplitude > 30000) {
            distTv.setText("Distance: 1");
        }
        else if(amplitude > 20000) {
            distTv.setText("Distance: 2");
        }
        else if(amplitude > 10000) {
            distTv.setText("Distance: 3");
        }
        else if(amplitude > 5000) {
            distTv.setText("Distance: 4");
        } else {
            distTv.setText("Distance: 5");
        }
    }

    public void start() {
        statusTv = (TextView) findViewById(R.id.status);
        try {
            if (mRecorder == null) {
                mRecorder = new MediaRecorder();
                mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
                mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
                mRecorder.setOutputFile("dev/null");
                mRecorder.prepare();
                mRecorder.start();
                statusTv.setText("Status: Active");
            }
        } catch ( Exception e) {
            e.printStackTrace();
            statusTv.setText("Status: Failing");
        }

    }

    public void stop() {
        statusTv = (TextView) findViewById(R.id.status);
        if(mRecorder != null) {
            mRecorder.stop();
            mRecorder.release();
            mRecorder = null;
            statusTv.setText("Status: Inactive");
        }
    }

    public double getAmplitude() {
        if(mRecorder != null) {
            return mRecorder.getMaxAmplitude();
        } else {
            return 0;
        }
    }

    public void checkPermission(String permission, int requestCode) {
        if(ContextCompat.checkSelfPermission(MainActivity.this, permission) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{ permission }, requestCode);
        }
        else {
            Toast.makeText(MainActivity.this,"Permission already granted", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case REQUEST_RECORD_AUDIO_PERMISSION:
                permissionToRecordAccepted  = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                break;
        }
        if (!permissionToRecordAccepted ) finish();
    }
}

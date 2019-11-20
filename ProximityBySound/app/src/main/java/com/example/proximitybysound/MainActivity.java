package com.example.proximitybysound;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Random;

import be.tarsos.dsp.AudioDispatcher;
import be.tarsos.dsp.AudioEvent;
import be.tarsos.dsp.AudioProcessor;
import be.tarsos.dsp.io.android.AudioDispatcherFactory;
import be.tarsos.dsp.onsets.OnsetHandler;
import be.tarsos.dsp.onsets.PercussionOnsetDetector;
import be.tarsos.dsp.pitch.PitchDetectionHandler;
import be.tarsos.dsp.pitch.PitchDetectionResult;
import be.tarsos.dsp.pitch.PitchProcessor;

public class MainActivity extends AppCompatActivity {



    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;

    // AudioDispatcher dispatcher = AudioDispatcherFactory.fromDefaultMicrophone(22050,1024,0);

    private MediaRecorder mRecorder  = null;

    Button s;

    TextView ampTv;
    TextView distTv;

    CountDownTimer cdt;

    private int timer = 0;
    double amp =0;
    private StringBuilder dataStr = new StringBuilder();


    // Requesting permission to RECORD_AUDIO
    private boolean permissionToRecordAccepted = false;
    private String [] permissions = {Manifest.permission.RECORD_AUDIO};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Checks Permission
        checkPermission(Manifest.permission.RECORD_AUDIO,REQUEST_RECORD_AUDIO_PERMISSION);

        ampTv = (TextView) findViewById(R.id.amplitude);
        distTv = (TextView) findViewById(R.id.distance);

        dataStr.append("Distance,Amplitude");


        /*PitchDetectionHandler pdh = new PitchDetectionHandler() {
            @Override
            public void handlePitch(PitchDetectionResult res, AudioEvent e){
                res.
                final float pitchInHz = res.getPitch();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        processPitch(pitchInHz);
                    }
                });
            }
        };
        AudioProcessor pitchProcessor = new PitchProcessor(PitchProcessor.PitchEstimationAlgorithm.FFT_YIN, 22050, 1024, pdh);
        dispatcher.addAudioProcessor(pitchProcessor);

        Thread audioThread = new Thread(dispatcher, "Audio Thread");
        audioThread.start(); */

        s = (Button) findViewById(R.id.startStop);
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
                            amp = getAmplitude();
                            ampTv.setText("Amplitude: " + amp);
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


       /* double threshold = 8;
        double sensitivity = 45;

        PercussionOnsetDetector mPercussionDetector = new PercussionOnsetDetector(22050, 1024,
                new OnsetHandler() {

                    @Override
                    public void handleOnset(double time, double salience) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                System.out.println("YAAAYYY");
                            }
                        });
                    }
                }, sensitivity, threshold);

        dispatcher.addAudioProcessor(mPercussionDetector);
        new Thread(dispatcher,"Audio Dispatcher").start(); */

    }

    public void DistanceChecker(double amplitude) {
        if(amplitude > 30000) {
            dataStr.append("\n" + String.valueOf(1)+ ","+ String.valueOf(amp));
            distTv.setText("Distance: 1");
        }
        else if(amplitude > 20000) {
            dataStr.append("\n" + String.valueOf(2)+ ","+ String.valueOf(amp));
            distTv.setText("Distance: 2");
        }
        else if(amplitude > 10000) {
            dataStr.append("\n" + String.valueOf(3)+ ","+ String.valueOf(amp));
            distTv.setText("Distance: 3");
        }
        else if(amplitude > 5000) {
            dataStr.append("\n" + String.valueOf(4)+ ","+ String.valueOf(amp));
            distTv.setText("Distance: 4");
        } else {
            dataStr.append("\n" + String.valueOf(5)+ ","+ String.valueOf(amp));
            distTv.setText("Distance: 5");
        }
    }

    public void export(View view) {
        try {
            FileOutputStream out = openFileOutput("data.csv", Context.MODE_PRIVATE);
            out.write((dataStr.toString()).getBytes());
            out.close();

            Context context = getApplicationContext();
            File fileLocation = new File(getFilesDir(), "data.csv");
            Uri path = FileProvider.getUriForFile(context,"com.example.proximitybysound.fileprovider", fileLocation);
            Intent fileIntent = new Intent(Intent.ACTION_SEND);
            fileIntent.setType("text/csv");
            fileIntent.putExtra(Intent.EXTRA_SUBJECT, "Data");
            fileIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            fileIntent.putExtra(Intent.EXTRA_STREAM, path);
            startActivity(Intent.createChooser(fileIntent,"Sent Mail"));
        }
        catch (Exception e) {
            e.printStackTrace();
        }

    }


    public void start() {
        try {
            if (mRecorder == null) {
                mRecorder = new MediaRecorder();
                mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
                mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
                mRecorder.setOutputFile("dev/null");
                mRecorder.prepare();
                mRecorder.start();
            }
        } catch ( Exception e) {
            e.printStackTrace();
        }

    }

    public void stop() {
        if(mRecorder != null) {
            mRecorder.stop();
            mRecorder.release();
            mRecorder = null;
        }
    }

    public double getAmplitude() {
        if(mRecorder != null) {
            return mRecorder.getMaxAmplitude();
        } else {
            return 0;
        }
    }


    /*public void processPitch(float pitchInHz) {

        pitchText.setText("" + pitchInHz);

        if(pitchInHz >= 110 && pitchInHz < 123.47) {
            //A
            noteText.setText("A");
        }
        else if(pitchInHz >= 123.47 && pitchInHz < 130.81) {
            //B
            noteText.setText("B");
        }
        else if(pitchInHz >= 130.81 && pitchInHz < 146.83) {
            //C
            noteText.setText("C");
        }
        else if(pitchInHz >= 146.83 && pitchInHz < 164.81) {
            //D
            noteText.setText("D");
        }
        else if(pitchInHz >= 164.81 && pitchInHz <= 174.61) {
            //E
            noteText.setText("E");
        }
        else if(pitchInHz >= 174.61 && pitchInHz < 185) {
            //F
            noteText.setText("F");
        }
        else if(pitchInHz >= 185 && pitchInHz < 196) {
            //G
            noteText.setText("G");
        }
    } */



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

package com.alexandra.arnauld.whisperit;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.media.MediaRecorder;
import android.os.Environment;
import java.io.File;
import java.io.IOException;

public class WhisperMenu extends AppCompatActivity {

    private TextView mTextMessage;

    //Pour l'enregistrement
    private boolean onRecord;
    private Button record;
    private static final String AUDIO_RECORDER_FOLDER = "Whispers";
    private static final String AUDIO_RECORDER_FILE_EXT_3GP = ".3gp";
    private static final String AUDIO_RECORDER_FILE_EXT_MP4 = ".mp4";
    private MediaRecorder enregistreur = null;
    private int currentFormat = 0;
    private int output_formats[] = { MediaRecorder.OutputFormat.MPEG_4, MediaRecorder.OutputFormat.THREE_GPP };
    private String file_exts[] = { AUDIO_RECORDER_FILE_EXT_MP4, AUDIO_RECORDER_FILE_EXT_3GP };

    //Declaration pour la progressBar
    private final int TIMER_RUNTIME = 10000;
    private ProgressBar progressBar;
    private int progressStatus = 0;
    private Handler handler = new Handler();

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    mTextMessage.setText(R.string.title_home);
                    return true;
                case R.id.navigation_dashboard:
                    mTextMessage.setText(R.string.title_dashboard);
                    return true;
                case R.id.navigation_notifications:
                    mTextMessage.setText(R.string.title_notifications);
                    return true;
            }
            return false;
        }

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_whisper_menu);

        mTextMessage = (TextView) findViewById(R.id.message);
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        progressBar.setMax(100);

        record = (Button) findViewById(R.id.button);
        addRecorderOnButton(record);

    }

    private void addRecorderOnButton(Button bouton){
        bouton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch(event.getAction()){
                    case MotionEvent.ACTION_DOWN:
                        AppLog.logString("enregistrement commencé");
                        startRecord();
                        break;
                    case MotionEvent.ACTION_UP:
                        AppLog.logString("enregistrement arrêté");
                        stopRecord();
                        break;
                }
                return false;
            }
        });
    }

    private String getFilename(){
        String filepath = Environment.getExternalStorageDirectory().getPath();
        File file = new File(filepath,AUDIO_RECORDER_FOLDER);
        if(!file.exists()){
            file.mkdirs();
        }
        return (file.getAbsolutePath() + "/" + System.currentTimeMillis() + file_exts[currentFormat]);
    }

    private void startRecord() {
        onRecord = true;

        new Thread(new Runnable() {
            public void run() {
                AppLog.logString("Thread record en train de tourner");
                long debut = System.currentTimeMillis();
                long fin = System.currentTimeMillis() + TIMER_RUNTIME;
                long temps;
                do {
                    temps = System.currentTimeMillis();
                    progressBar.setProgress((int) ((progressBar.getMax() * (temps - debut)) / TIMER_RUNTIME));
                } while (temps < fin && onRecord);
                stopRecord();
            }
        }).start();

        enregistreur = new MediaRecorder();
        enregistreur.setAudioSource(MediaRecorder.AudioSource.MIC);
        enregistreur.setOutputFormat(output_formats[currentFormat]);
        enregistreur.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        enregistreur.setOutputFile(getFilename());
        enregistreur.setOnErrorListener(errorListener);
        enregistreur.setOnInfoListener(infoListener);

        try {
            enregistreur.prepare();
            enregistreur.start();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void stopRecord() {
        AppLog.logString("stopRecord : " + onRecord);
        if (enregistreur!=null && onRecord){
            AppLog.logString("stopRecord : " + onRecord);
            onRecord = false;
            progressStatus = 0;
            progressBar.setProgress(0);
            enregistreur.stop();
            enregistreur.reset();
            enregistreur.release();
            enregistreur = null;
        }
    }

    private MediaRecorder.OnErrorListener errorListener = new MediaRecorder.OnErrorListener() {
        @Override
        public void onError(MediaRecorder mr, int what, int extra) {
            AppLog.logString("Erreur: " + what + ", " + extra);
        }
    };

    private MediaRecorder.OnInfoListener infoListener = new MediaRecorder.OnInfoListener() {
        @Override
        public void onInfo(MediaRecorder mr, int what, int extra) {
            AppLog.logString("Attention: " + what + ", " + extra);
        }
    };
}

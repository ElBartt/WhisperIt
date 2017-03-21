package com.alexandra.arnauld.whisperit;

import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
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

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Inet4Address;
import java.net.InetAddress;

public class WhisperMenu extends AppCompatActivity {

    private TextView mTextMessage;

    //Pour l'enregistrement
    private boolean onRecord;
    private boolean recorded;
    private Button record;
    private static final String AUDIO_RECORDER_FOLDER = "Whispers";
    private static final String AUDIO_RECORDER_FILE_EXT_3GP = ".3gp";
    private static final String AUDIO_RECORDER_FILE_EXT_MP4 = ".mp4";
    private MediaRecorder enregistreur = null;
    private int currentFormat = 0;
    private int output_formats[] = {MediaRecorder.OutputFormat.MPEG_4, MediaRecorder.OutputFormat.THREE_GPP};
    private String file_exts[] = {AUDIO_RECORDER_FILE_EXT_MP4, AUDIO_RECORDER_FILE_EXT_3GP};
    private String filePath;
    private String nom;

    //Declaration pour la progressBar
    private final int TIMER_RUNTIME = 10000;
    private ProgressBar progressBar;
    private int progressStatus = 0;
    private Handler handler = new Handler();

    //Pour l'envoie sur le serveur
    private Button upload;
    String server = "ftp.cluster1.easy-hebergement.net";
    int port = 21;
    String user = "arnauldalex1";
    String pass = "12345678";

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
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_whisper_menu);

        mTextMessage = (TextView) findViewById(R.id.message);
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        progressBar.setMax(200);

        record = (Button) findViewById(R.id.button);
        addRecorderOnButton(record);

        upload = (Button) findViewById(R.id.button2);
        addUploadOnButton(upload);
    }

    private void addUploadOnButton(Button bouton) {
        bouton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    public void run() {
                        if (recorded) {
                            try {
                                AppLog.logString("FTP : " + filePath);
                                AppLog.logString("FTP : " + nom);
                                progressBar.setProgress(0);
                                //connection au ftp
                                FTPClient ftpClient = new FTPClient();
                                AppLog.logString("FTP : Lancement");
                                ftpClient.connect(InetAddress.getByName(server), 21);
                                ftpClient.login(user, pass);
                                ftpClient.enterLocalPassiveMode();
                                AppLog.logString("FTP status : " + ftpClient.getStatus());

                                progressBar.setProgress(progressBar.getMax() * 1 / 3);
                                //upload au ftp
                                File leWhisper = new File(filePath);
                                String remoteFile = nom;
                                InputStream is = new FileInputStream(leWhisper);
                                boolean done = ftpClient.storeFile(remoteFile, is);
                                is.close();
                                if (done) {
                                    progressBar.setProgress(progressBar.getMax() * 2 / 3);
                                    AppLog.logString("FTP The file is uploaded successfully.");
                                }

                                //liste des fichiers dedans
                                FTPFile[] files = ftpClient.listFiles("/");
                                AppLog.logString("FTP taille liste : " + files.length);

                                for (FTPFile file : files) {
                                    String details = file.getName();
                                    AppLog.logString("FTP fichiers : " + details);
                                }

                                ftpClient.logout();
                                ftpClient.disconnect();
                                progressBar.setProgress(progressBar.getMax() * 1);
                            } catch (Exception e) {
                                e.printStackTrace();
                                progressBar.setProgress(0);
                            } finally {
                                progressBar.setProgress(0);
                            }
                        }
                    }
                }).start();
            }
        });
    }

    private void addRecorderOnButton(Button bouton) {
        bouton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        startRecord();
                        break;
                    case MotionEvent.ACTION_UP:
                        stopRecord();
                        break;
                }
                return false;
            }
        });
    }

    private String getFilename() {
        String filepath = Environment.getExternalStorageDirectory().getPath();
        nom = "whisper-" + System.currentTimeMillis() + file_exts[currentFormat];
        File file = new File(filepath, AUDIO_RECORDER_FOLDER);
        if (!file.exists()) {
            file.mkdirs();
        }
        filePath = file.getAbsolutePath() + "/" + nom;
        return (filePath);
    }

    private void startRecord() {
        AppLog.logString("enregistrement commencé");
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

        recorded = true;
    }

    private void stopRecord() {
        AppLog.logString("enregistrement arrêté");
        AppLog.logString("onRecord : " + onRecord);
        if (enregistreur != null && onRecord) {
            onRecord = false;
            AppLog.logString("onRecord : " + onRecord);
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

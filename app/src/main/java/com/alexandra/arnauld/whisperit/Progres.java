package com.alexandra.arnauld.whisperit;

import android.widget.ProgressBar;

import java.io.InterruptedIOException;

/**
 * Created by arnauld on 18/03/2017.
 */

class Progres extends Thread {
    private ProgressBar pb;
    private static final int TIMER_RUNTIME = 10000;
    private boolean progressBarActive;

    public Progres(ProgressBar progressBar) {
        pb = progressBar;
    }

    @Override
    public void run() {
        progressBarActive = true;

        if (progressBarActive) {
            AppLog.logString("Thread en train de tourner");
            long debut = System.currentTimeMillis();
            long fin = System.currentTimeMillis() + TIMER_RUNTIME;
            long temps;

            try {
                do {
                    temps = System.currentTimeMillis();
                    if (progressBarActive) {
                        updateProgressBar(temps, debut);
                    }
                } while (temps < fin && !this.isInterrupted());
            } finally {
                onContinue();
            }
        }
    }



    @Override
    public void interrupt() {
        super.interrupt();
        AppLog.logString("Thread Interrompu");
        progressBarActive = false;
        pb.setProgress(0);
    }

    private void updateProgressBar(long temps, long debut) {
        if (pb != null) {
            final int progress = (int) (pb.getMax() * (temps - debut)) / TIMER_RUNTIME;
            pb.setProgress(progress);
        }
    }

    private void onContinue() {
        AppLog.logString("onContinue");
    }
}

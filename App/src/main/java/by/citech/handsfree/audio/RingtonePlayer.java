package by.citech.handsfree.audio;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Handler;

import by.citech.handsfree.R;
import by.citech.handsfree.application.ThisApp;
import timber.log.Timber;

import static android.content.Context.AUDIO_SERVICE;

public class RingtonePlayer implements
        MediaPlayer.OnPreparedListener,
        MediaPlayer.OnCompletionListener {

    private MediaPlayer mediaPlayer;
    private AudioManager audioManager;
    private Handler handler;

    public RingtonePlayer(Handler handler) {
        this.handler = handler;
        Context context = ThisApp.getAppContext();
        audioManager = (AudioManager) context.getSystemService(AUDIO_SERVICE);
        mediaPlayer = MediaPlayer.create(context, R.raw.ring);
        mediaPlayer.setLooping(true);
        mediaPlayer.setOnCompletionListener(this);
    }

    //--------------------- main

    public void turnOn() {
        if (!isReady()) return;
        mediaPlayer.start();
    }

    public void turnOff() {
        if (!isReady()) return;
        mediaPlayer.stop();
        mediaPlayer.release();
    }

    //--------------------- OnCompletionListener

    @Override
    public void onCompletion(MediaPlayer mp) {
        Timber.i("onCompletion");
    }

    //--------------------- OnPreparedListener

    @Override
    public void onPrepared(MediaPlayer mp) {
        Timber.i("onPrepared");
    }

    //--------------------- additioonal

    private boolean isReady() {
        return mediaPlayer != null;
    }

}

package de.einfachhans.PlayDefaultSounds;

import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.LOG;
import org.json.JSONArray;
import org.json.JSONException;

import java.util.Timer;
import java.util.TimerTask;

public class PlayDefaultSounds extends CordovaPlugin {

    private static final String LOG_TAG = "[PlayDefaultSound]";
    private static final String ACTION_PLAY = "play";

    private MediaPlayer player;
    private Uri ringtone;
    private Timer endSoundScheduler;

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        if (action.equals(ACTION_PLAY)) {
            this.play();
            return true;
        }
        return false;
    }

    @Override
    public void onDestroy() {
        //Release player before GC
        if(player != null){
            player.release();
        }
    }

    @Override
    protected void pluginInitialize() {
        ringtone = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);


        // Tried with Motorola Moto G5 Plus (Android 8), those listeners simply DON'T WORK before manually
        // calling stop(), so they're unuseful
//        player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
//            @Override
//            public void onCompletion(MediaPlayer mp) {
//                if(player.isPlaying()){
//                    player.stop();
//                }
//            }
//        });
//
//        player.setOnSeekCompleteListener(new MediaPlayer.OnSeekCompleteListener() {
//            @Override
//            public void onSeekComplete(MediaPlayer mp) {
//                if(player.isPlaying()){
//                    player.stop();
//                }
//            }
//        });
    }

    /**
     * Beep plays the default notification ringtone.
     */
    private void play() {
        cordova.getThreadPool().execute(new Runnable() {
            public void run() {
                // If this is called before a possible previous sound has completed
                if(player != null){
                    if(player.isPlaying()){
                        player.stop();
                    }

                    //Release player before GC
                    player.release();
                    player = null;
                }

                if(endSoundScheduler != null){
                    endSoundScheduler.cancel();
                    endSoundScheduler = null;
                }

                player = MediaPlayer.create(cordova.getActivity(), ringtone);

                // Tried with Motorola Moto G5 Plus (Android 8), this flag doesn't work (it keeps playing
                // in a loop)
                player.setLooping(false);

                player.start();
                LOG.d(LOG_TAG, "Sound played");


                // Force player to stop when it reaches the end of the sound
                endSoundScheduler = new Timer();
                endSoundScheduler.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        if(player.isPlaying()){
                            player.stop();
                        }
                        endSoundScheduler = null;
                    }
                }, player.getDuration());

            }
        });
    }
}

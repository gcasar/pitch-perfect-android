package si.bbd.pitch_perfect;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.IBinder;

import si.bbd.pitch_perfect.util.CircularBuffer;

/**
 * Created by gcasar on 25.4.15.
 */
public class AudioStreamService extends Service{

    StreamRecorder recorder;
    StreamPlayer player;

    CircularBuffer buffer = new CircularBuffer(1440, 1000);


    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {


        recorder = new StreamRecorder(buffer);
        StreamPlayer.clearInstance();
        player = StreamPlayer.getInstance(buffer);
        if(intent.hasExtra("GAIN_dB")){
            player.setGain(intent.getFloatExtra("GAIN_dB", 0));
        }

        recorder.start();
        player.start();

        return Service.START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        recorder.end();
        player.end();
        super.onDestroy();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    @Override
    public void onRebind(Intent intent) {
        super.onRebind(intent);
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
    }
}

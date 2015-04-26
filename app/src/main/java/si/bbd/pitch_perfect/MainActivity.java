package si.bbd.pitch_perfect;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.Image;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.Toast;


public class MainActivity extends ActionBarActivity {

    public static final float MAX_dB_GAIN = 20;
    public static final float MIN_dB_GAIN = -0;
    public static final float DELTA_dB_GAIN = MAX_dB_GAIN-MIN_dB_GAIN;
    public static final int NUM_STEPS = 10;
    public static final float STEP_dB_VALUE = DELTA_dB_GAIN/NUM_STEPS;


    private float mGain_dB = 1;
    private Toast mToast = null;

    private boolean mRunning = false;

    private ImageButton mBtnPlus;
    private ImageButton mBtnMinus;

    private Button mBtnDonate;
    private ImageButton mBtnToggle;

    private SeekBar mSeekBar;

    private BroadcastReceiver mBroadcastReceiver = new MusicIntentReceiver();

    private AudioManager mAudioManager;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAudioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);


        mBtnPlus = (ImageButton)findViewById(R.id.btnPlus);
        mBtnMinus = (ImageButton)findViewById(R.id.btnMinus);
        mBtnToggle = (ImageButton)findViewById(R.id.btnToggle);
        mBtnDonate = (Button)findViewById(R.id.btnDonate);

        mSeekBar = (SeekBar)findViewById(R.id.seekBar);
        mSeekBar.setMax((int)DELTA_dB_GAIN);

        mBtnDonate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, CharitiesActivity.class));
            }
        });

        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(fromUser) {
                    setGain(progress + MIN_dB_GAIN);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                setGain(seekBar.getProgress()+MIN_dB_GAIN);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                setGain(seekBar.getProgress()+MIN_dB_GAIN);
            }
        });

        mBtnPlus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!mRunning)setRunning(true);
                changeGain(+STEP_dB_VALUE);
            }
        });

        mBtnMinus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!mRunning)setRunning(true);
                changeGain(-STEP_dB_VALUE);
            }
        });

        mBtnToggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setRunning(!mRunning);
            }
        });

        bindViews();

    }


    @Override
    public void onResume() {
        IntentFilter filter = new IntentFilter(Intent.ACTION_HEADSET_PLUG);
        registerReceiver(mBroadcastReceiver, filter);
        super.onResume();
    }

    private class MusicIntentReceiver extends BroadcastReceiver {
        @Override public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Intent.ACTION_HEADSET_PLUG)) {
                int state = intent.getIntExtra("state", -1);
                switch (state) {
                    case 0:
                        setRunning(false);
                        showToast( "Headset is unplugged");
                        break;
                    case 1:
                        showToast( "Headset is plugged");
                        break;
                    default:
                        showToast( "I have no idea what the headset state is");
                }
            }
        }
    }

    @Override
    public void onPause() {
        unregisterReceiver(mBroadcastReceiver);
        super.onPause();
    }

    public void bindViews(){
        mSeekBar.setProgress((int)(mGain_dB-MIN_dB_GAIN));
        if(mRunning)
            mBtnToggle.setImageResource(R.drawable.uho);
        else
            mBtnToggle.setImageResource(R.mipmap.uho_off);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_start) {
            setRunning(true);
        }else if(id == R.id.action_stop){
            setRunning(false);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch(keyCode){
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                changeGain(-STEP_dB_VALUE);
                return true;
            case KeyEvent.KEYCODE_VOLUME_UP:
                changeGain(+STEP_dB_VALUE);
                return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    public void changeGain(float delta_dB){
        setGain(mGain_dB + delta_dB);
    }

    public void setGain(float dB){
        mGain_dB = dB;
        if(mGain_dB>MAX_dB_GAIN)mGain_dB = MAX_dB_GAIN;
        else if(mGain_dB<MIN_dB_GAIN)mGain_dB = MIN_dB_GAIN;
        StreamPlayer.getInstance(null).setGain(mGain_dB);
        showToast("dB: "+mGain_dB);
        mSeekBar.setProgress((int)(dB-MIN_dB_GAIN));
    }


    public void setRunning(boolean run){
        if(run&& !mAudioManager.isWiredHeadsetOn()){
            showToast("Headset required for use!");
            return;//block
        }
        mRunning = run;
        if(!mRunning){
            stopService(new Intent(MainActivity.this, AudioStreamService.class));
            mBtnToggle.setImageResource(R.mipmap.uho_off);
            showToast("Stopped");
        }else{
            Intent intent = new Intent(MainActivity.this,AudioStreamService.class );
            intent.putExtra("GAIN_dB", mGain_dB);
            startService(intent);
            mBtnToggle.setImageResource(R.drawable.uho);
            showToast("Started");
        }
    }


    public void showToast(String text){
        if(mToast==null){
            mToast = Toast.makeText(this, text, Toast.LENGTH_SHORT);
        }
        mToast.setText(text);
        mToast.show();
    }
}

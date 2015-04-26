package si.bbd.pitch_perfect;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;


public class MainActivity extends ActionBarActivity {

    private float mGain_dB = 6;
    private Toast mToast = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        startService(new Intent(this,AudioStreamService.class ));
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
            startService(new Intent(this,AudioStreamService.class ));
            return true;
        }else if(id == R.id.action_stop){
            stopService(new Intent(this, AudioStreamService.class));
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch(keyCode){
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                changeGain(-1);
                return true;
            case KeyEvent.KEYCODE_VOLUME_UP:
                changeGain(+1);
                return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    public void changeGain(float delta_dB){
        mGain_dB+=delta_dB;
        StreamPlayer.getInstance(null).setGain(mGain_dB);
        showToast("dB: "+mGain_dB);
    }


    public void showToast(String text){
        if(mToast==null){
            mToast = Toast.makeText(this, text, Toast.LENGTH_SHORT);
        }
        mToast.setText(text);
        mToast.show();
    }
}

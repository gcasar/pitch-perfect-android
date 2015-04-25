package si.bbd.pitch_perfect;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.support.v4.util.CircularArray;
import android.util.Log;
import android.view.View.OnClickListener;

import si.bbd.pitch_perfect.util.Buffer;
import si.bbd.pitch_perfect.util.CircularBuffer;
import si.bbd.pitch_perfect.util.Constants;

/**
 * Records audio to a buffer
 * @author gcasar
 *
 */
public class StreamRecorder extends Thread {


    private static final String TAG = "StreamRecorder";

    CircularBuffer mBuffer;

    boolean mIsRunning=true;

    /**
     * Used to sample audio from hw
     */
    AudioRecord mRecorder;

    private int mSampleRate = Constants.SAMPLE_RATE;

    Buffer mActiveBuffer;

    private Callback mListener = null;

    public StreamRecorder(CircularBuffer buffer){
        mBuffer = buffer;
    }

    /**
     * Calculated as per http://en.wikipedia.org/wiki/Bit_rate
     * @return bitrate
     */
    public long getBitrate(){
        return mSampleRate*2*2;
    }

    public void end(){
        mIsRunning = false;
    }



    @Override
    public void run(){

        //in bits
        int buffersize = 0;
        // Prepare the AudioRecord & AudioTrack
        try {
            buffersize = AudioRecord.getMinBufferSize(mSampleRate,
                    AudioFormat.CHANNEL_CONFIGURATION_MONO,
                    AudioFormat.ENCODING_PCM_16BIT) * 2;
            if(buffersize>mBuffer.getUnitSize()){
                Log.e(TAG, "Buffer too small! Required "+buffersize+" have:"+mBuffer.getUnitSize());
                mIsRunning = false;
                return;
            }

            Log.i(TAG,"Initializing Audio Record and Audio Playing objects");

            mRecorder = new AudioRecord(MediaRecorder.AudioSource.CAMCORDER,
                    mSampleRate, AudioFormat.CHANNEL_CONFIGURATION_MONO,
                    AudioFormat.ENCODING_PCM_16BIT, buffersize/2);

        } catch (Throwable t) {
            Log.e(TAG, "Initializing Audio Record and Play objects Failed "+t.getLocalizedMessage());
            return;
        }

        mRecorder.startRecording();
        Log.i(TAG,"Audio Recording started");

        mActiveBuffer = mBuffer.getNextWrite();
        while (mIsRunning) {
            //multiply by 2 because read returns in hwords (16bits) and our arrays are 8bit

            mActiveBuffer.taken = mRecorder.read(mActiveBuffer.data, 0, buffersize/2)*2;
            if(mListener!=null)mListener.onRecord(mActiveBuffer);
            mActiveBuffer = mBuffer.nextWrite();
        }

        mRecorder.release();

        Log.i(TAG, "recorder exit");

    }

    public void setCallback(Callback listener) {
        this.mListener = listener;
    }

    public interface Callback{
        void onRecord(Buffer b);
    }
}

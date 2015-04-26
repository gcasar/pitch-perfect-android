package si.bbd.pitch_perfect;

import si.bbd.pitch_perfect.util.AudioManipulator;
import si.bbd.pitch_perfect.util.Buffer;
import si.bbd.pitch_perfect.util.CircularBuffer;
import si.bbd.pitch_perfect.util.Constants;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.util.Log;

/**
 * Records audio to a buffer
 * @author gcasar
 *
 */
public class StreamPlayer extends Thread {

    protected static StreamPlayer instance;
    public static StreamPlayer getInstance(CircularBuffer buffer){
        if(instance == null)instance = new StreamPlayer(buffer);
        return instance;
    }
    public static void clearInstance(){
        instance = null;
    }
	
	
	private static final String TAG = "StreamRecorder";
	
	CircularBuffer mBuffer;
	
	boolean mIsRunning=true;
	
	private int mSampleRate = Constants.SAMPLE_RATE;

    //float gainFactor = (float)Math.pow( 10., dB / 20. );
    private float mGainFactor = 1;
	
	Buffer mActiveBuffer;
	
	AudioTrack mPlayer;

	private Callback mListener;
	
	protected StreamPlayer(CircularBuffer buffer){
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

            mPlayer = new AudioTrack(AudioManager.STREAM_MUSIC,
                    mSampleRate, AudioFormat.CHANNEL_CONFIGURATION_MONO,
                    AudioFormat.ENCODING_PCM_16BIT, buffersize/2,
                    AudioTrack.MODE_STREAM);
            
        } catch (Throwable t) {
            Log.e(TAG, "Initializing Audio Record and Play objects Failed "+t.getLocalizedMessage());
            return;
        }


        mPlayer.play();
        Log.i(TAG,"Audio Playing started");

        mActiveBuffer = null;
        while (mIsRunning) {


        	mActiveBuffer = mBuffer.getNextRead();

            while(mActiveBuffer==null){
            	
            	try {
					sleep(100);
				} catch (InterruptedException e) {
					//nothing special, just retry sooner
				}
            	mActiveBuffer = mBuffer.getNextRead();
            }
            AudioManipulator.gain(mActiveBuffer, buffersize / 2, mGainFactor);
            //AudioManipulator.cut(mActiveBuffer,buffersize/2, -200,200);

            mPlayer.write(mActiveBuffer.data, 0, buffersize/2);
             //mPlayer.flush();
            mBuffer.nextRead();
            if(mListener!=null)mListener.onPlayFinished(mActiveBuffer);
        }
        
        mPlayer.release();

        Log.i(TAG, "player exit");
		
	}

    public float getGainFactor() {
        return mGainFactor;
    }

    public void setGainFactor(float gain) {
        this.mGainFactor = gain;
    }

    public void setGain(float dB){
        mGainFactor = (float)Math.pow( 10., dB / 20. );
    }

    public void setCallback(Callback listener) {
		this.mListener = listener;
	}
	
	public interface Callback{
		void onPlayFinished(Buffer b);
	}

}

package si.bbd.pitch_perfect.util;

import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * FIFO byte buffor where minimum read and write is set .
 * 
 * Used for packet data buffer
 * 
 * @author gcasar
 *
 */
public class CircularBuffer {
	

	/**
	 * Minimum size was calculated as a factor of AES block size (16B) and
	 * minimum buffer size for MONO, 16Bit PCM at 8kHz. (1280B).
	 * 
	 * An additional 160B are added for packet headers.
	 * 
	 * This is also a nice size for packets since most devices have 1.5k MTU and will not
	 * get segmented this way.
	 */
	public static final int DEFAULT_UNIT_SIZE = 1440;
	
	final int mUnitSize;
	
	/**
	 * Used to make this thread-safe (an easy method to sync data flow)
	 */
	public final LinkedBlockingQueue<Buffer> mQueue; 
	
	/**
	 * Contains all buffers
	 */
	final ArrayList<Buffer> mBuffers;
	
	/**
	 * Index of the next buffer to read from. Check totalUnread before reading
	 */
	int posRead = 0;
	
	/**
	 * Index of the next buffer in the circular array to write into
	 */
	int posWrite = 0;
	
	int totalWritten = 0;
	
	int totalRead = 0;
	
	int totalUnread = 0;
	
	final int mBuffersSize;
	
	public CircularBuffer(){
		this(DEFAULT_UNIT_SIZE,10);
	}
	
	/**
	 * @param unit_size every write will be padded to a factor of this size
	 * @param capacity number of unit_size buffers to contain
	 */
	public CircularBuffer(int unit_size,int capacity){
		mUnitSize = unit_size;
		mQueue = new LinkedBlockingQueue<Buffer>();
		mBuffers = new ArrayList<Buffer>(capacity);
		mBuffersSize = capacity;
		
		reset();
	}
	
	/**
	 * Not thread safe. Resets all buffers by creating new ones
	 */
	public void reset(){
		totalRead = totalWritten = totalUnread = posRead = posWrite = 0;
		for(int i=0; i<mBuffersSize; i++){
			mBuffers.add(new Buffer(mUnitSize));
		}
	}
	
	/**
	 * @return number of buffers to read. 
	 */
	public int getUnreadCount(){
		return totalUnread;
	}
	
	/**
	 * Get buffer handle with this than call push to add buffer to queue
	 * @return can be null (if pos is -1)
	 */
	public Buffer getNextRead(){
		if(totalUnread<=0)return null;
		else return mBuffers.get(posRead);
	}
	
	/**
	 * Call write 
	 * @return
	 */
	public Buffer getNextWrite(){
		return mBuffers.get(posWrite);
	}
	
	/**
	 * Moves ptr than returns buffer to read if any exist (or null).
	 * DO NOT HOLD THIS BUFFER FOR LONG AS IT MIGHT BE SKIPPED. 
	 * Rather call getNextRead before reading.
	 * @return
	 */
	public Buffer nextRead(){
		if(totalUnread<=0)return null;
		if(++posRead>=mBuffersSize){
			posRead = 0;
		}
		totalRead++;
		totalUnread--;
		return mBuffers.get(posRead);
	}
	
	/**
	 * Moves ptr than returns buffer
	 * @return
	 */
	public Buffer nextWrite(){
		if(++posWrite>=mBuffersSize){
			posWrite = 0;
		}
		totalWritten++;
		//fix read pos if we would overflow
		if(totalUnread==mBuffersSize){
			if(++posRead>=mBuffersSize){
				posRead = 0;
			}
		}else{
			totalUnread++;
		}
		return mBuffers.get(posWrite);
	}
	

	public int getReadPosition(){
		return posRead;
	}

	public int getWritePosition(){
		return posWrite;
	}


	public int getCapacity() {
		return mBuffersSize;
	}
	
	public int getCapacityBytes(){
		return mBuffersSize*mUnitSize;
	}

	public int getUnitSize() {
		return mUnitSize;
	}

	/**
	 * This implementation does not require the buffers to be released
	 */
	public void release() {
		// does nothing atm
	}
}

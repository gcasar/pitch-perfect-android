package si.bbd.pitch_perfect.util;

import java.nio.charset.Charset;

/**
	 * Wrapper for data
	 * @author gcasar
	 *
	 */
	public class Buffer{
		public final byte[] data;
		
		public int taken = 0;
		
		public Buffer(int size){
			data = new byte[size];
		}
		
		/**
		 * Deep copy of b.
		 * Both buffers must be of same parent
		 * @param b
		 */
		public Buffer(Buffer b){
			data = new byte[b.data.length];
			for(int i=0; i<data.length; i++)
				data[i] = b.data[i];
		}
		
		/**
		 * Conveniance constructor. Converts string to byte array
		 * @param string
		 */
		public Buffer(String string) {
			this.data = string.getBytes(Charset.forName("UTF-8"));
			taken = data.length;
		}

		public String toString(){
			return new String(data,0,taken);
		}

		/**
		 * does not check for length
		 * @param payload
		 */
		public void fromString(String str) {
			byte[] tmp = str.getBytes(Charset.forName("UTF-8"));
			System.arraycopy(tmp, 0, data, 0, tmp.length);
			taken = tmp.length;
		}
		
	}
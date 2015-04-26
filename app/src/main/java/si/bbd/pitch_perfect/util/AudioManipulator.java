package si.bbd.pitch_perfect.util;

/**
 * Created by gcasar on 25.4.15.
 */
public class AudioManipulator {

    /**
     * 16 bit sampling, 16kbit/s bitrate
     * @param buffer
     * @param length
     * @param change
     */
    public static void gain(Buffer buffer, int length, float change){
        for(int i=0; i<buffer.taken; i+=2){//16bit, so every second, LITTLE ENDIAN
            float sample = (float)( buffer.data[i  ] & 0xFF
                    | buffer.data[i+1] << 8 );

            // THIS is the point were the work is done:
            // Increase level by about 6dB:
            sample *= change;
            // Or increase level by 20dB:
            // sample *= 10;
            // Or if you prefer any dB value, then calculate the gain factor outside the loop
            // float gainFactor = (float)Math.pow( 10., dB / 20. );    // dB to gain factor
            // sample *= gainFactor;

            // Avoid 16-bit-integer overflow when writing back the manipulated data:
            if ( sample >= 32767f ) {
                buffer.data[i  ] = (byte)0xFF;
                buffer.data[i+1] =       0x7F;
            } else if ( sample <= -32768f ) {
                buffer.data[i  ] =       0x00;
                buffer.data[i+1] = (byte)0x80;
            } else {
                int s = (int)( 0.5f + sample );  // Here, dithering would be more appropriate
                buffer.data[i  ] = (byte)(s & 0xFF);
                buffer.data[i+1] = (byte)(s >> 8 & 0xFF);
            }
        }
    }

    public static void cut(Buffer buffer, int length, float min_dB, float max_dB){
        float min = dBToValue(min_dB), max = dBToValue(max_dB);
        for(int i=0; i<buffer.taken; i+=2){//16bit, so every second, LITTLE ENDIAN
            float sample = (float)( buffer.data[i  ] & 0xFF
                    | buffer.data[i+1] << 8 );

            if ( sample >= max ) {
                sample = max;
            } else if ( sample <= min ) {
                sample = min;
            }

            if ( sample >= 32767f ) {
                buffer.data[i  ] = (byte)0xFF;
                buffer.data[i+1] =       0x7F;
            } else if ( sample <= -32768f ) {
                buffer.data[i  ] =       0x00;
                buffer.data[i+1] = (byte)0x80;
            } else {
                int s = (int)( 0.5f + sample );  // Here, dithering would be more appropriate
                buffer.data[i  ] = (byte)(s & 0xFF);
                buffer.data[i+1] = (byte)(s >> 8 & 0xFF);
            }
        }
    }

    public static float dBToValue(float dB){
        return (float)Math.pow( 10., dB / 20. );
    }


}

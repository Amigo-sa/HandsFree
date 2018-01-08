package by.citech.handsfree.codec.audio;

import by.citech.handsfree.settings.Settings;

public class SitAudioCodec_3_0_java
        extends AudioCodecFactory {

    //------------------- AHTUNG ГОВНОКОД НАЧАЛО

    private static final boolean debug = Settings.debug;
    private static final EAudioCodecType audioCodecType = EAudioCodecType.Sit_3_0_java;
    private static final String TAG = audioCodecType.getSettingName();
    private static final short[] decodedData = new short[audioCodecType.getDecodedShortsSize()];
    private static final byte[] encodedData = new byte[audioCodecType.getEncodedBytesSize()];

    private CodecState decoderState;
    private CodecState encoderState;

    @Override
    public void initiateDecoder() {
        if (decoderState == null) {
            decoderState = new CodecState();
        } else {
            decoderState.initiate();
        }
    }

    @Override
    public void initiateEncoder() {
        if (encoderState == null) {
            encoderState = new CodecState();
        } else {
            encoderState.initiate();
        }
    }

    @Override
    public short[] getDecodedData(byte[] dataToDecode) {
        if (decoderState == null) {
            initiateDecoder();
        }
        decode(dataToDecode, decodedData, decoderState);
        return decodedData;
    }

    @Override
    public byte[] getEncodedData(short[] dataToEncode) {
        if (encoderState == null) {
            initiateDecoder();
        }
        encode(dataToEncode, encodedData, encoderState);
        return encodedData;
    }

    private class CodecState {

        private short prevsample;
        private int previndex;

        private void initiate() {
            prevsample = 0;
            previndex = 0;
        }

    }

    //------------------- AHTUNG ГОВНОКОД ОКОНЧАНИЕ

    private static byte[] IndexTable = {
            -1, -1, -1, -1, 2, 4, 6, 8,
            -1, -1, -1, -1, 2, 4, 6, 8,
    };

    private static int[] StepSizeTable = {
            7,     8,     9,     10,    11,    12,    13,    14,    16,    17,
            19,    21,    23,    25,    28,    31,    34,    37,    41,    45,
            50,    55,    60,    66,    73,    80,    88,    97,    107,   118,
            130,   143,   157,   173,   190,   209,   230,   253,   279,   307,
            337,   371,   408,   449,   494,   544,   598,   658,   724,   796,
            876,   963,   1060,  1166,  1282,  1411,  1552,  1707,  1878,  2066,
            2272,  2499,  2749,  3024,  3327,  3660,  4026,  4428,  4871,  5358,
            5894,  6484,  7132,  7845,  8630,  9493,  10442, 11487, 12635, 13899,
            15289, 16818, 18500, 20350, 22385, 24623, 27086, 29794, 32767
    };

    private static void encode(short[] toEncode, byte[] encoded, CodecState state) {
        int i;
        byte smp;

        for (i = 0; i < 80; i += 4) {
            smp = ADPCMEncoder(toEncode[i], state);
            smp |= ADPCMEncoder(toEncode[i + 2], state) << 4;
            encoded[i >> 2] = smp;
        }
    }

    private static void decode(byte[] toDecode, short[] decoded, CodecState state) {
        int i;
        byte smp;

        for (i = 0; i < 20; i++) {
            smp = (byte) (toDecode[i] & 0x0F);
            decoded[(i << 2)] = (short) ADPCMDecoder(smp, state);
            smp = (byte) ((toDecode[i] >> 4) & 0x0F);
            decoded[(i << 2) + 2] = (short) ADPCMDecoder(smp, state);
        }

        for (i = 1; i < 79; i += 2) {
            decoded[i] = (short) ((((int) decoded[i - 1] + (int) decoded[i + 1])) >> 1);
        }
        decoded[79] = decoded[78];
    }

    private static byte ADPCMEncoder(short sample, CodecState state) {
        int code; // ADPCM output value
        int diff; // Difference between sample and the predicted sample
        int step; // Quantizer step size
        int predsample; // Output of ADPCM predictor
        int diffq; // Dequantized predicted difference
        int index; // Index into step size table

        // Restore previous values of predicted sample and quantizer step size index
        predsample = (int) (state.prevsample);
        index = state.previndex;
        step = StepSizeTable[index];

        // Compute the difference between the acutal sample
        // (sample) and the the predicted sample (predsample)
        diff = sample - predsample;
        if (diff >= 0) {
            code = 0;
        } else {
            code = 8;
            diff = -diff;
        }

        // Quantize the difference into the 4-bit ADPCM
        // code using the the quantizer step size.
	    // Inverse quantize the ADPCM code into a predicted
        // difference using the quantizer step size.

        diffq = step >> 3;
        if (diff >= step) {
            code |= 4;
            diff -= step;
            diffq += step;
        }
        step >>= 1;
        if (diff >= step) {
            code |= 2;
            diff -= step;
            diffq += step;
        }
        step >>= 1;
        if (diff >= step) {
            code |= 1;
            diffq += step;
        }

	    // Fixed predictor computes new predicted sample by
        // adding the old predicted sample to predicted difference
        if ((code & 8) != 0) predsample -= diffq;
        else                 predsample += diffq;

	    // Check for overflow of the new predicted sample
        if      (predsample >  32767) predsample =  32767;
        else if (predsample < -32767) predsample = -32767;

	    // Find new quantizer stepsize index by adding the old indexto a table lookup using the ADPCM code
        index += IndexTable[code];
	    // Check for overflow of the new quantizer step size index
        if (index < 0)  index = 0;
        if (index > 88) index = 88;

	    // Save the predicted sample and quantizer step size index for next iteration
        state.prevsample = (short) predsample;
        state.previndex = index;

	    // Return the new ADPCM code
        return (byte) (code & 0x0f);
    }

    private static int ADPCMDecoder(byte code, CodecState state) {
        int step; // Quantizer step size
        int predsample; // Output of ADPCM predictor
        int diffq; // Dequantized predicted difference
        int index; // Index into step size table

	    // Restore previous values of predicted sample and quantizer step size index
        predsample = (int) (state.prevsample);
        index = state.previndex;

	    // Find quantizer step size from lookup table using index
        step = StepSizeTable[index];

	    // Inverse quantize the ADPCM code into a difference using the quantizer step size
        diffq = step >> 3;
        if ((code & 4) != 0) diffq += step;
        if ((code & 2) != 0) diffq += step >> 1;
        if ((code & 1) != 0) diffq += step >> 2;

	    // Add the difference to the predicted sample
        if ((code & 8) != 0) predsample -= diffq;
        else                 predsample += diffq;

	    // Check for overflow of the new predicted sample
        if      (predsample >  32767) predsample =  32767;
        else if (predsample < -32767) predsample = -32767;

	    // Find new quantizer step size by adding the old index and a table lookup using the ADPCM code
        index += IndexTable[code];
	    // Check for overflow of the new quantizer step size index
        if (index < 0)  index = 0;
        if (index > 88) index = 88;

	    // Save predicted sample and quantizer step size index for next iteration
        state.prevsample = (short) predsample;
        state.previndex = index;

    	// Return the new speech sample
        return (predsample);
    }

}

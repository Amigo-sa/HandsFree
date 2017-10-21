package by.citech.util;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

import by.citech.param.Tags;

public class Decode {

    public static String bytesToHexMark1(byte[] bytes) {
        char[] hexArray = "0123456789ABCDEF".toCharArray();
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    /**
     * Convert byte array to hex string
     * @param bytes
     * @return
     */
    public static String bytesToHexMark2(byte[] bytes) {
        StringBuilder sbuf = new StringBuilder();
        for (int idx = 0; idx < bytes.length; idx++) {
            int intVal = bytes[idx] & 0xff;
            if (intVal < 0x10) sbuf.append("0");
            sbuf.append(Integer.toHexString(intVal).toUpperCase());
        }
        return sbuf.toString();
    }

    /**
     * Get utf8 byte array.
     * @param str
     * @return  array of NULL if error was found
     */
    public static byte[] getUTF8Bytes(String str) {
        try {
            return str.getBytes("UTF-8");
        } catch (Exception ex) {
            return null;
        }
    }

    /**
     * Load UTF8withBOM or any ansi text file.
     * @param filename
     * @return
     * @throws java.io.IOException
     */
    public static String loadFileAsString(String filename) throws java.io.IOException {
        final int BUFLEN = 1024;
        BufferedInputStream is = new BufferedInputStream(new FileInputStream(filename), BUFLEN);
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream(BUFLEN);
            byte[] bytes = new byte[BUFLEN];
            boolean isUTF8 = false;
            int read, count = 0;
            while ((read = is.read(bytes)) != -1) {
                if (count == 0 && bytes[0] == (byte) 0xEF && bytes[1] == (byte) 0xBB && bytes[2] == (byte) 0xBF) {
                    isUTF8 = true;
                    baos.write(bytes, 3, read - 3); // drop UTF8 bom marker
                } else {
                    baos.write(bytes, 0, read);
                }
                count += read;
            }
            return isUTF8 ? new String(baos.toByteArray(), "UTF-8") : new String(baos.toByteArray());
        } finally {
            try {
                is.close();
            } catch (Exception ex) {
            }
        }
    }

    public void logStaticFields(Class<?> clazz) throws IllegalAccessException {
        for (Field f : clazz.getDeclaredFields()) {
            if (Modifier.isStatic(f.getModifiers())) {
                boolean wasAccessible = f.isAccessible();
                f.setAccessible(true);
                Log.i (Tags.DECODE, "Debug output of static field " + f.getName() + ": " + f.get( null ) );
                f.setAccessible( wasAccessible );
            }
        }
    }

    public void printStaticFields(Class<?> clazz) throws IllegalAccessException {
        for (Field f : clazz.getDeclaredFields()) {
            if (Modifier.isStatic(f.getModifiers())) {
                boolean wasAccessible = f.isAccessible();
                f.setAccessible(true);
                Log.i (Tags.DECODE, "Debug output of static field " + f.getName() + ": " + f.get( null ) );
                f.setAccessible(wasAccessible);
            }
        }
    }

    public static <T> String getConstantNameMap(Class<?> clazz, T var) throws IllegalAccessException {
        Map<T, String> cNames = new HashMap<T, String>();
        Class<?> varClazz = var.getClass();
//      String result = null;

        for (Field field : clazz.getDeclaredFields()){
            boolean wasAccessible = field.isAccessible();
            field.setAccessible(true);
            Log.i (Tags.DECODE, "getConstantName Found field: " + field.getName());
            field.setAccessible(wasAccessible);
            if ((field.getModifiers() & (Modifier.FINAL | Modifier.STATIC)) != 0) {
                Log.i (Tags.DECODE, "getConstantName: modifiers are OK");
//              if (varClazz == field.getType()) {
                if (varClazz.isInstance(field.getType())) {
                    Log.i (Tags.DECODE, "getConstantName: type is match");
                    if (field == var) {
                        Log.i (Tags.DECODE, "getConstantName: got one match");
                        wasAccessible = field.isAccessible();
                        field.setAccessible(true);
                        cNames.put((T) field.get(null), field.getName());
//                      result = field.getName();
                        field.setAccessible(wasAccessible);
                        break;
                    } else {
                        Log.i (Tags.DECODE, "getConstantName: mismatch");
                    }
                }
            }
        }
        Log.i (Tags.DECODE, "Searching for fields done.");
        return cNames.get(var);
//      return result;
    }
	
	    public static <T> String getConstantName(Class<?> clazz, T var, boolean debug) throws IllegalAccessException {
        Class<?> varClazz = var.getClass();
        String result = null;
        for (Field field : clazz.getDeclaredFields()){
            boolean wasAccessible = field.isAccessible();
            field.setAccessible(true);
            if (debug) System.out.println("Found field <" + field.getName() + ">.");
            field.setAccessible(wasAccessible);
            if ((field.getModifiers() & (Modifier.FINAL | Modifier.STATIC)) != 0) {
                if (debug) System.out.print("Modifiers are matching. ");
//              if (varClazz == field.getType()) {
//              if (varClazz.isInstance(field.getType())) {
                if (field.get(null).getClass() == varClazz) {
                    if (debug) System.out.print("Type is matching. ");
//                  if (field.get(null) == var) {
//                  if (field.equals(var)) {
                    if (field.get(null).equals(var)) {
                        if (debug) System.out.println("Value is matching. Got it!");
                        wasAccessible = field.isAccessible();
                        field.setAccessible(true);
//                      cNames.put((T) field.get(null), field.getName());
                        result = field.getName();
                        field.setAccessible(wasAccessible);
                        break;
                    } else if (debug) System.out.println("Value is not matching.");
                } else if (debug) System.out.println("Type is not matching.");
            } else if (debug) System.out.println("Modifiers is not matching.");
        }
        return result;
    }

    private AudioRecord findAudioRecord(boolean debug) {
        int[] mSampleRates = new int[] {
                8000,
                11025,
                22050,
                32000,
                44100 };
        short[] mAudioFormatChannels = new short[] {
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.CHANNEL_IN_STEREO };
        final short[] mAudioFormatEncodings = new short[] {
                AudioFormat.ENCODING_PCM_8BIT,
                AudioFormat.ENCODING_PCM_16BIT,
                AudioFormat.ENCODING_PCM_FLOAT };
        if (debug) Log.i(Tags.NET_STREAM_AUDIO, "findAudioRecord");
        int bufferLenghtActual;
        AudioRecord recorder = null;
        byte[] buffer;
        for (int rate : mSampleRates) {
            for (short encoding : mAudioFormatEncodings) {
                for (short channel : mAudioFormatChannels) {
                    try {
                        int bufferSizeMinimal = AudioRecord.getMinBufferSize(rate, channel, encoding);
                        if (debug) Log.i(Tags.NET_STREAM_AUDIO,
                                "SampleRate: "          + rate              + ". " +
                                        "Encoding: "            + encoding          + ". " +
                                        "Channels: "            + channel           + ". " +
                                        "Minimal buffer size: " + bufferSizeMinimal + ".");
                        if (bufferSizeMinimal != AudioRecord.ERROR_BAD_VALUE) {
                            recorder = new AudioRecord(MediaRecorder.AudioSource.DEFAULT, rate, channel, encoding, bufferSizeMinimal * 10);
                            if (debug) Log.i(Tags.NET_STREAM_AUDIO, "findAudioRecord new AudioRecord");
                            Thread.sleep(100);
                            if (recorder.getState() == AudioRecord.STATE_INITIALIZED) {
                                Log.i(Tags.NET_STREAM_AUDIO, "findAudioRecord recorder is initialized");
                                bufferLenghtActual = bufferSizeMinimal * 10;
                                buffer = new byte[bufferLenghtActual];
                                return recorder;
                            }
                            if (debug) Log.i(Tags.NET_STREAM_AUDIO, "findAudioRecord recorder is not initialized, release");
                            recorder.release();
                            recorder = null;
                            Thread.sleep(100);
                        }
                    } catch (Exception e) {
                        if (debug) Log.i(Tags.NET_STREAM_AUDIO, "findAudioRecord Exception");
                        if (recorder != null) {
                            recorder.release();
                        }
                        recorder = null;
                    }
                }
            }
        }
        return null;
    }
}
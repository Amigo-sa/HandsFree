package by.citech.codec.audio;

public class SitAudioCodec_2_1_native implements ICodec {

//    static {
//        System.loadLibrary("SitCodecWrapper");
//    }

    private static final int decodedShortCnt = AudioCodecType.Sit_2_1_java.getDecodedShortsSize();
    private static final int encodedByteCnt = AudioCodecType.Sit_2_1_java.getEncodedBytesSize();

    private CodecState decoderState;
    private CodecState encoderState;
    private byte[] encodedData;
    private short[] decodedData;

//    public native void nativeEncode(short[] dataToEncode, byte[] encodedData, CodecState codecState);
//    public native void nativeDecode(byte[] dataToDecode, short[] decodedData, CodecState codecState);

    public SitAudioCodec_2_1_native() {
        decodedData = new short[decodedShortCnt];
        encodedData = new byte[encodedByteCnt];
    }

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
//        nativeDecode(dataToDecode, decodedData, decoderState);
        return decodedData;
    }

    @Override
    public byte[] getEncodedData(short[] dataToEncode) {
        if (encoderState == null) {
            initiateEncoder();
        }
        return encodedData;
    }

    private class CodecState {

        private int   yl   ;
        private int   yu   ;
        private int   dms  ;
        private int   dml  ;
        private int   ap   ;
        private int   a  [];
        private int   b  [];
        private int   pk [];
        private short dq [];
        private int   sr [];
        private int   td   ;

        private CodecState() {
            a     = new int  [2];
            b     = new int  [6];
            pk    = new int  [2];
            dq    = new short[6];
            sr    = new int  [2];
            initiate();
        }

        private void initiate() {
            yl                   = 34816;
            yu                   = 544  ;
            dms                  = 0    ;
            dml                  = 0    ;
            ap                   = 0    ;
            for (int   i : a ) i = 0    ;
            for (int   i : b ) i = 0    ;
            for (int   i : pk) i = 0    ;
            for (short i : dq) i = 32   ;
            for (int   i : sr) i = 32   ;
            td                   = 0    ;
        }

        public int getYl() {
            return yl;
        }

        public void setYl(int yl) {
            this.yl = yl;
        }

        public int getYu() {
            return yu;
        }

        public void setYu(int yu) {
            this.yu = yu;
        }

        public int getDms() {
            return dms;
        }

        public void setDms(int dms) {
            this.dms = dms;
        }

        public int getDml() {
            return dml;
        }

        public void setDml(int dml) {
            this.dml = dml;
        }

        public int getAp() {
            return ap;
        }

        public void setAp(int ap) {
            this.ap = ap;
        }

        public int[] getA() {
            return a;
        }

        public void setA(int[] a) {
            this.a = a;
        }

        public int[] getB() {
            return b;
        }

        public void setB(int[] b) {
            this.b = b;
        }

        public int[] getPk() {
            return pk;
        }

        public void setPk(int[] pk) {
            this.pk = pk;
        }

        public short[] getDq() {
            return dq;
        }

        public void setDq(short[] dq) {
            this.dq = dq;
        }

        public int[] getSr() {
            return sr;
        }

        public void setSr(int[] sr) {
            this.sr = sr;
        }

        public int getTd() {
            return td;
        }

        public void setTd(int td) {
            this.td = td;
        }
    }

}

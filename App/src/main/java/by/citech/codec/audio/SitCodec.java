package by.citech.codec.audio;

public class SitCodec {

//    public native void initiate();
//    public native void encode(byte[] dataToEncode, byte[] encodedData);
//    public native void decode(byte[] dataToDecode, byte[] decodedData);

    private class SitCodecState {

        private long  yl   ;
        private int   yu   ;
        private int   dms  ;
        private int   dml  ;
        private int   ap [];
        private int   a  [];
        private int   b  [];
        private int   pk [];
        private short dq [];
        private int   sr [];
        private int   td   ;
        private boolean isInitiated;

        private SitCodecState() {
            yl  =             34816 ;
            yu  =             544   ;
            dms =             0     ;
            dml =             0     ;
            ap  = new int  []{0    };
            a   = new int  []{2    };
            b   = new int  []{6    };
            pk  = new int  []{2    };
            dq  = new short[]{6    };
            sr  = new int  []{2    };
            td  =             0     ;
        }

        //private copyState()

        private void initiate() {
            if (!isInitiated) {
                isInitiated = true;
                return;
            }
            reset();
        }

        private void reset() {
            yl  =             34816 ;
            yu  =             544   ;
            dms =             0     ;
            dml =             0     ;
            ap  = new int  []{0    };
            a   = new int  []{2    };
            b   = new int  []{6    };
            pk  = new int  []{2    };
            dq  = new short[]{6    };
            sr  = new int  []{2    };
            td  =             0     ;
        }

    }

    static {
        System.loadLibrary("SitCodec");
    }

}

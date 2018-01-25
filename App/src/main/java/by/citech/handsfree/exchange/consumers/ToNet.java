package by.citech.handsfree.exchange.consumers;

import java.io.ByteArrayOutputStream;

import by.citech.handsfree.data.StorageData;
import by.citech.handsfree.exchange.IRxComplex;
import by.citech.handsfree.exchange.IStreamer;
import by.citech.handsfree.parameters.StatusMessages;
import by.citech.handsfree.settings.Settings;
import by.citech.handsfree.parameters.Tags;
import timber.log.Timber;

public class ToNet
        implements IStreamer {

    private static final String STAG = Tags.ToNet;
    private final String TAG;
    private static final boolean debug = Settings.debug;
    private static int objCount;
    private final int objNumber;
    static {objCount = 0;}

    //--------------------- preparation

    private boolean netSignificantAll;
    private int netSendSize;
    private int netChunkSignificantBytes;
    private int netChunkSize;
    private int netFactor;
    private byte[] netChunk;
    private IRxComplex iRxComplex;
    private boolean isStreaming;
    private boolean isFinished;
    private boolean isPrepared;
    private StorageData<byte[]> source;

    {
        objCount++;
        objNumber = objCount;
        TAG = STAG + " " + objNumber;
        netSignificantAll = Settings.Network.netSignificantAll;
        netChunkSignificantBytes = Settings.Network.netChunkSignificantBytes;
        netChunkSize = netSignificantAll ? Settings.Network.netChunkSize : netChunkSignificantBytes;
        netFactor = Settings.Network.netFactor;
        netSendSize = netChunkSize * netFactor;
        netChunk = new byte[netChunkSize];
    }

    //--------------------- constructor

    public ToNet(StorageData<byte[]> source) throws Exception {
        if (source == null) {
            throw new Exception(TAG + " " + StatusMessages.ERR_PARAMETERS);
        }
        this.source = source;
    }

    //--------------------- IStreamer

    @Override
    public void prepareStream(IRxComplex receiver) throws Exception {
        if (isFinished) {
            if (debug) Timber.tag(TAG).w("prepareStream stream is finished, return");
            return;
        } else if (receiver == null) {
            throw new Exception(TAG + " " + StatusMessages.ERR_PARAMETERS);
        } else {
            if (debug) Timber.tag(TAG).i("prepareStream");
            this.iRxComplex = receiver;
        }
        if (debug) Timber.tag(TAG).w("streamOn parameters is:" +
                        " netSignificantAll is %b," +
                        " netChunkSignificantBytes is %d," +
                        " netChunkSize is %d," +
                        " netFactor is %d," +
                        " netSendSize is %d",
                netSignificantAll,
                netChunkSignificantBytes,
                netChunkSize,
                netFactor,
                netSendSize
        );
        isPrepared = true;
    }

    @Override
    public void finishStream() {
        if (debug) Timber.tag(TAG).i("finishStream");
        isFinished = true;
        streamOff();
        iRxComplex = null;
        source = null;
    }

    @Override
    public void streamOff() {
        if (debug) Timber.tag(TAG).i("streamOff");
        isStreaming = false;
    }

    @Override
    public boolean isStreaming() {
        return isStreaming;
    }

    @Override
    public boolean isReadyToStream() {
        if (isFinished) {
            if (debug) Timber.tag(TAG).w("isReadyToStream finished");
            return false;
        } else if (!isPrepared) {
            if (debug) Timber.tag(TAG).w("isReadyToStream not prepared");
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void streamOn() {
        if (isStreaming() || !isReadyToStream()) {
            return;
        } else {
            if (debug) Timber.tag(TAG).i("streamOn");
        }
        isStreaming = true;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int netChunkSizeActual = 0;
        int netChunkCount = 0;
        while (isStreaming()) {
            while (isStreaming() && isReadyToStream() && source.isEmpty()) {
                try {
                    Thread.sleep(5);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            if (!isStreaming() || !isReadyToStream()) return;
            netChunk = source.getData();
            if (netChunk != null) {
                netChunkSizeActual = netChunk.length;
                if (netChunkSizeActual != netChunkSize) {
                    if (debug) Timber.tag(TAG).e("streamOn readed chunk of length %d, expected %d", netChunkSizeActual, netChunkSize);
                } else {
                    baos.write(netChunk, 0, netChunkSize);
                    netChunkCount++;
                }
            } else {
                if (debug) Timber.tag(TAG).e("streamOn readed null data from storage");
                return;
            }
            if (debug) Timber.tag(TAG).i("streamOn net out buff contains %d netChunks of %d bytes each", netChunkCount, netChunkSize);
            if (netChunkCount == netFactor) {
                if (debug) Timber.tag(TAG).w("streamOn net out buff contains enough data of %d bytes, sending", baos.size());
                if (isStreaming() && isReadyToStream()) {
                    iRxComplex.sendData(baos.toByteArray());
                }
                netChunkCount = 0;
                baos.reset();
            } else if (netChunkCount > netFactor) {
                if (debug) Timber.tag(TAG).e("streamOn too much data in net out buff");
                netChunkCount = 0;
                baos.reset();
            }
        }
        if (debug) Timber.tag(TAG).i("streamOn done");
    }

}

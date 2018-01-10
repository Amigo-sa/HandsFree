package by.citech.handsfree.experimental;

import by.citech.handsfree.common.IProcessing;
import by.citech.handsfree.data.StorageData;
import by.citech.handsfree.parameters.StatusMessages;

public abstract class DataExchanger<F, fromB, toB, T>
        implements IProcessing, ITx<F> {

    private boolean isPrepared;
    private boolean isStarted;
    private boolean isPaused;
    private boolean isFinished;

    private ITxCtrl<F> src0;
    private StorageData<F> src1;

    private ITx<T> dst0;
    private StorageData<T> dst1;

    private IConverter<?,?> conv;
    private Buffer<?,?> buff;

    //--------------------- constructor

    //--------------------- getters and setters

    abstract void setSrcAndDst(ITxCtrl<F> src, ITx<T> dst);
    abstract void setSrcAndDst(ITxCtrl<F> src, StorageData<T> dst);
    abstract void setSrcAndDst(StorageData<F> src, ITx<T> dst);
    abstract void setSrcAndDst(StorageData<F> src, StorageData<T> dst);

    protected void setConv(IConverter<F, T> converter) {

    }

    protected void setBuffAndConv(Buffer<F, fromB> buffer, IConverter<fromB, T> converter) {

    }

    protected void setBuffAndConv(IConverter<F, toB> converter, Buffer<toB, T> buffer) {

    }

    protected void setBuff(Buffer<F, T> buffer) {

    }

    //--------------------- checks

    private void checkBuffAndConv() throws Exception {
        if (buff != null || conv != null) {
            throw new Exception(StatusMessages.ERR_PARAMETERS);
        }
    }

    private void checkSrcAndDst() throws Exception {
        if (src0 != null || src1 != null || dst0 != null || dst1 != null) {
//            throw new Exception(StatusMessages.ERR_ALREADY_SET);
        }
    }

    //--------------------- IProcessing

    @Override
    public boolean isTaskCompleted(Task task) {
        return false;
    }

    @Override
    public synchronized boolean processTask(Task task) {
        switch (task) {
            case Prepare:
                onPrepare();
            case Start:
                onStart();
            case Pause:
                onPause();
            case Finish:
                onFinish();
        }
        return true;
    }

    //--------------------- main

    private void onPrepare() {

    }

    private void onStart() {

    }

    private void onPause() {

    }

    private void onFinish() {

    }

}

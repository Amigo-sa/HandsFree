package by.citech.handsfree.util;

public class ArrayHelper {

    public static double[] getRevertedDoubleArr(double[] arrToRevert) {
        int length = arrToRevert.length;
        double[] revertedArr = new double[length];
        for (int i = 0; i < length; i++) revertedArr[i] = arrToRevert[length-i-1];
        return revertedArr;
    }

    public static double[] revertDoubleArr(double[] arrToRevert) {
        for (int i = 0; i < arrToRevert.length / 2; i++) {
            double temp = arrToRevert[i];
            arrToRevert[i] = arrToRevert[arrToRevert.length-i-1];
            arrToRevert[arrToRevert.length-i-1] = temp;
        }
        return arrToRevert;
    }

    public static void invertDoubleArr(double[] array) {
        for (int i = 0; i < array.length; i++) array[i] = 0.0D - array[i];
    }

    public static void arrayDoubleToShort(double[] doubleArr, short[] shortArr) {
        for (int i = 0; i < doubleArr.length; i++) {
            shortArr[i] = (short) ((int) doubleArr[i]);
        }
    }

}

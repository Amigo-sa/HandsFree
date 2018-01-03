package by.citech.handsfree.util;

import java.util.*;

public class AppSine {

    private static final double MAX_COS = 1.0D;
    private static final double MIN_COS = 0.0D;
    private static final int QUARTER_1 = 1;
    private static final int QUARTER_2 = 2;
    private static final int QUARTER_3 = 3;
    private static final int QUARTER_4 = 4;
    private static final int QPP = 4;

    public static void main(String[] args) {
        AppSine app = new AppSine();
        app.runTheApp();
    }

    private void runTheApp() {
        int div = 80;
        double delta = (MAX_COS - MIN_COS) / (double) div;
        double[] cos = new double[div];
        Double[] sin = new Double[div];
        double[] hypo = new double[div];
        double[] acos = new double[div];
        double[] degree = new double[div];

        for (int i = 0; i < div; i++) {
            cos[i] =  (double) i * delta;
            sin[i] = Math.sqrt((1.0D-cos[i])*((1.0D+cos[i])));
            hypo[i] = sin[i]*sin[i] + cos[i]*cos[i];
            acos[i] = Math.acos(cos[i]);
            degree[i] = Math.toDegrees(acos[i]);
            System.out.println(cos[i] + " " + acos[i] + " " + degree[i] + " " + sin[i] + " " + hypo[i]);
        }

        Double[] sinCopy1 = new Double[div];
        Double[] sinCopy2 = new Double[div];
        System.arraycopy(sin, 0, sinCopy1, 0, div);
        System.arraycopy(sin, 0, sinCopy2, 0, div);

        Double[] sinCopy3 = new Double[div];
        System.arraycopy(sin, 0, sinCopy3, 0, div);
        for (int i = 0; i < sinCopy3.length; i++) sinCopy3[i] = 0.0D - sinCopy3[i];

        Double[] sinCopy4 = new Double[div];
        System.arraycopy(sinCopy3, 0, sinCopy4, 0, div);

        List<Double> quarter1 = Arrays.asList(sinCopy1);
        List<Double> quarter2 = Arrays.asList(sinCopy2);
        List<Double> quarter3 = Arrays.asList(sinCopy3);
        List<Double> quarter4 = Arrays.asList(sinCopy4);

        Collections.reverse(quarter2);
        Collections.reverse(quarter4);

        System.out.println("quarta1:" + quarter1);
        System.out.println("quarta4:" + quarter4);
        System.out.println("quarta3:" + quarter3);
        System.out.println("quarta2:" + quarter2);

    }

}

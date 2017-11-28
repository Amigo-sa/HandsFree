package by.citech.util;

public class Pair<X, y> {
    public final X x;
    public final y y;

    public Pair(X x, y y) {
        this.x = x;
        this.y = y;
    }

    public X getX() {
        return x;
    }

    public y getY() {
        return y;
    }
}
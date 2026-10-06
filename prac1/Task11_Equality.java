package prac1;

import java.util.Objects;

class Point {
    protected final int x;
    protected final int y;

    Point(int x, int y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Point other)) return false;
        return x == other.x && y == other.y;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }
}

class ColoredPoint extends Point {
    private final String color;

    ColoredPoint(int x, int y, String color) {
        super(x, y);
        this.color = color;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ColoredPoint other)) return false;
        return x == other.x && y == other.y && Objects.equals(color, other.color);
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y, color);
    }
}

final class PointValue {
    private final int x;
    private final int y;

    PointValue(int x, int y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PointValue that = (PointValue) o;
        return x == that.x && y == that.y;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }
}

public class Task11_Equality {
    public static void main(String[] args) {
        Point p = new Point(1, 2);
        ColoredPoint cp = new ColoredPoint(1, 2, "red");

        System.out.println(p.equals(cp));
        System.out.println(cp.equals(p));

        System.out.println("Fix 1: strict getClass() equality in a closed/final value class.");
        System.out.println("Fix 2: composition: Point contains an optional Color instead of subclassing.");
    }
}

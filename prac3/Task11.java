import java.util.Objects;

/*
 * Задание 11. Равенство в иерархии
 *
 * Воспроизведите нарушение симметричности между Point и ColoredPoint. Предложите два
 * исправления: strict class equality и композиция цвета. Защитите, почему final value class
 * проще расширяемой equality-иерархии.
 *
 * Пояснение:
 * Нарушение: BrokenPoint.equals использует instanceof, поэтому point.equals(colored) == true,
 * а colored.equals(point) == false (ещё и транзитивность ломается при двух цветах).
 * Исправление 1 (strict class equality): сравнивать getClass(); равенство симметрично, но
 * подкласс уже не подставляется вместо родителя в сравнениях (компромисс по LSP).
 * Исправление 2 (композиция): ColoredPoint содержит Point, а не наследует его - равенство
 * компонентов определено однозначно.
 * final value class (record) проще: нет подклассов -> нет проблемы наследования equals,
 * контракт (рефлексивность, симметрия, транзитивность) выполняется тривиально.
 */
public class Task11 {

    static class BrokenPoint {
        final int x, y;
        BrokenPoint(int x, int y) { this.x = x; this.y = y; }
        @Override public boolean equals(Object o) {
            if (!(o instanceof BrokenPoint p)) return false;
            return x == p.x && y == p.y;
        }
        @Override public int hashCode() { return Objects.hash(x, y); }
    }

    static class BrokenColoredPoint extends BrokenPoint {
        final String color;
        BrokenColoredPoint(int x, int y, String color) { super(x, y); this.color = color; }
        @Override public boolean equals(Object o) {
            if (!(o instanceof BrokenColoredPoint c)) return false;
            return super.equals(o) && color.equals(c.color);
        }
        @Override public int hashCode() { return Objects.hash(super.hashCode(), color); }
    }

    static class StrictPoint {
        final int x, y;
        StrictPoint(int x, int y) { this.x = x; this.y = y; }
        @Override public boolean equals(Object o) {
            if (o == this) return true;
            if (o == null || getClass() != o.getClass()) return false;
            StrictPoint p = (StrictPoint) o;
            return x == p.x && y == p.y;
        }
        @Override public int hashCode() { return Objects.hash(x, y); }
    }

    static class StrictColoredPoint extends StrictPoint {
        final String color;
        StrictColoredPoint(int x, int y, String color) { super(x, y); this.color = color; }
        @Override public boolean equals(Object o) {
            return super.equals(o) && color.equals(((StrictColoredPoint) o).color);
        }
        @Override public int hashCode() { return Objects.hash(super.hashCode(), color); }
    }

    record Point(int x, int y) { }
    record ColoredPoint(Point point, String color) { }

    public static void main(String[] args) {
        BrokenPoint bp = new BrokenPoint(1, 2);
        BrokenColoredPoint bcp = new BrokenColoredPoint(1, 2, "red");
        System.out.println("Нарушение: p.equals(cp)=" + bp.equals(bcp) + ", cp.equals(p)=" + bcp.equals(bp));

        StrictPoint sp = new StrictPoint(1, 2);
        StrictColoredPoint scp = new StrictColoredPoint(1, 2, "red");
        System.out.println("Strict: p.equals(cp)=" + sp.equals(scp) + ", cp.equals(p)=" + scp.equals(sp));

        Point p = new Point(1, 2);
        ColoredPoint cp = new ColoredPoint(p, "red");
        System.out.println("Композиция: p.equals(cp)=" + p.equals(cp) + ", cp.equals(p)=" + cp.equals(p)
                + ", cp.point().equals(p)=" + cp.point().equals(p));
    }
}

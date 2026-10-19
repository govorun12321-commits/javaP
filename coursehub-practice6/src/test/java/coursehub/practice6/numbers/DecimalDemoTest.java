package coursehub.practice6.numbers;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DecimalDemoTest {

    @Test
    void doubleConstructorExposesBinaryError() {
        BigDecimal value = DecimalDemo.fromDoubleConstructor();
        assertTrue(value.toPlainString().startsWith("0.1000000000000000055511151231257827"));
        assertNotEquals(new BigDecimal("0.1"), value);
        assertTrue(value.scale() > 50);
    }

    @Test
    void valueOfAndStringConstructorGiveTheSameNumber() {
        assertEquals(new BigDecimal("0.1"), DecimalDemo.fromValueOf());
        assertEquals(DecimalDemo.fromString(), DecimalDemo.fromValueOf());
        assertEquals(1, DecimalDemo.fromString().scale());
    }

    @Test
    void threeCoursesAddUpExactly() {
        assertEquals(new BigDecimal("99.70"), DecimalDemo.totalOfThreeCourses());
        assertEquals(new BigDecimal("99.7"), DecimalDemo.totalFromValueOf());
    }

    @Test
    void plainDoubleSumDrifts() {
        assertNotEquals(99.7, DecimalDemo.totalOfThreeCoursesAsDouble());
    }

    @Test
    void doubleConstructorSumDoesNotEqualDecimalSum() {
        assertNotEquals(0, DecimalDemo.totalFromDoubleConstructor().compareTo(new BigDecimal("99.70")));
    }

    @Test
    void equalsDependsOnScaleButCompareToDoesNot() {
        assertFalse(DecimalDemo.sameByEquals("2.0", "2.00"));
        assertTrue(DecimalDemo.sameByCompareTo("2.0", "2.00"));
    }

    @Test
    void describeShowsUnscaledValueAndScale() {
        assertEquals("value=1.50 unscaled=150 scale=2 precision=3", DecimalDemo.describe(new BigDecimal("1.50")));
    }
}

package coursehub.practice6.numbers;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.OptionalInt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IntegerPolicyTest {

    @Test
    void unboxingNullThrowsNullPointerException() {
        assertThrows(NullPointerException.class, () -> IntegerPolicy.unboxUnsafely(null));
        assertThrows(NullPointerException.class, () -> IntegerPolicy.sumUnsafely(1, null));
    }

    @Test
    void missingMapValueThrowsWhenUnboxedUnsafely() {
        Map<String, Integer> quantities = new HashMap<>();
        quantities.put("JAVA-101", 3);
        assertEquals(3, IntegerPolicy.quantityUnsafely(quantities, "JAVA-101"));
        assertThrows(NullPointerException.class, () -> IntegerPolicy.quantityUnsafely(quantities, "SQL-001"));
    }

    @Test
    void valueComparisonWorksForAnyNumber() {
        for (int value : new int[]{0, 100, 127, 128, 1000, -129, 100_000}) {
            Integer first = value;
            Integer second = Integer.valueOf(value);
            assertTrue(IntegerPolicy.sameValue(first, second), "value " + value);
        }
    }

    @Test
    void valueComparisonHandlesNull() {
        assertTrue(IntegerPolicy.sameValue(null, null));
        assertFalse(IntegerPolicy.sameValue(null, 1));
        assertFalse(IntegerPolicy.sameValue(1, null));
    }

    @Test
    void explicitContractFailsWithReadableMessage() {
        NullPointerException exception =
                assertThrows(NullPointerException.class, () -> IntegerPolicy.requireValue(null, "quantity"));
        assertEquals("quantity must not be null", exception.getMessage());
        assertEquals(5, IntegerPolicy.requireValue(5, "quantity"));
    }

    @Test
    void requiredValuesAreComparedAfterUnboxing() {
        assertTrue(IntegerPolicy.sameRequiredValue(1000, 1000));
        assertFalse(IntegerPolicy.sameRequiredValue(1000, 1001));
        assertThrows(NullPointerException.class, () -> IntegerPolicy.sameRequiredValue(null, 1));
    }

    @Test
    void defaultAndOptionalVariantsDoNotThrow() {
        assertEquals(7, IntegerPolicy.valueOrDefault(null, 7));
        assertEquals(3, IntegerPolicy.valueOrDefault(3, 7));
        assertEquals(OptionalInt.empty(), IntegerPolicy.toOptional(null));
        assertEquals(OptionalInt.of(4), IntegerPolicy.toOptional(4));
    }

    @Test
    void missingQuantityBecomesZero() {
        Map<String, Integer> quantities = Map.of("JAVA-101", 2);
        assertEquals(2, IntegerPolicy.quantityOrZero(quantities, "JAVA-101"));
        assertEquals(0, IntegerPolicy.quantityOrZero(quantities, "SQL-001"));
    }

    @Test
    void identityResultsCoverEveryRequestedValue() {
        assertEquals(5, IntegerPolicy.identityResults(1, 100, 127, 1000, -500).size());
    }
}

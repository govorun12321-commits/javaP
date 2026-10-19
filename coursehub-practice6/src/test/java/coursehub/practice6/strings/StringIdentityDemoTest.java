package coursehub.practice6.strings;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StringIdentityDemoTest {

    @Test
    void literalsAreTheSameObject() {
        assertTrue(StringIdentityDemo.literalsShareReference());
    }

    @Test
    void newStringIsAnotherObjectButEqualText() {
        assertFalse(StringIdentityDemo.literalAndNewStringShareReference());
        assertTrue(StringIdentityDemo.literalAndNewStringAreEqual());
    }

    @Test
    void compileTimeConstantsAreFoldedIntoOneLiteral() {
        assertTrue(StringIdentityDemo.compileTimeConcatenationSharesReference());
        assertTrue(StringIdentityDemo.finalVariableConcatenationSharesReference());
    }

    @Test
    void runtimeConcatenationCreatesNewObject() {
        assertFalse(StringIdentityDemo.runtimeConcatenationSharesReference());
        assertTrue(StringIdentityDemo.runtimeConcatenationIsEqual());
    }

    @Test
    void internReturnsPooledInstance() {
        assertTrue(StringIdentityDemo.internedConcatenationSharesReference());
    }

    @Test
    void textComparisonUsesEqualsAndIsNullSafe() {
        assertTrue(StringIdentityDemo.sameText(new String("java"), new String("java")));
        assertFalse(StringIdentityDemo.sameText("java", null));
        assertTrue(StringIdentityDemo.sameText(null, null));
        assertTrue(StringIdentityDemo.sameTextIgnoringCase("Java", "jAVA"));
        assertFalse(StringIdentityDemo.sameTextIgnoringCase(null, "java"));
    }

    @Test
    void reportContainsAllCases() {
        assertEquals(8, StringIdentityDemo.report().size());
    }
}

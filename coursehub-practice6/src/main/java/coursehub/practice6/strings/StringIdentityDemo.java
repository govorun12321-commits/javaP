package coursehub.practice6.strings;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public final class StringIdentityDemo {

    private StringIdentityDemo() {
    }

    public static boolean literalsShareReference() {
        String first = "java";
        String second = "java";
        return first == second;
    }

    public static boolean literalAndNewStringShareReference() {
        String literal = "java";
        String created = new String("java");
        return literal == created;
    }

    public static boolean literalAndNewStringAreEqual() {
        String literal = "java";
        String created = new String("java");
        return literal.equals(created);
    }

    public static boolean compileTimeConcatenationSharesReference() {
        String joined = "ja" + "va";
        String literal = "java";
        return joined == literal;
    }

    public static boolean finalVariableConcatenationSharesReference() {
        final String head = "ja";
        String joined = head + "va";
        String literal = "java";
        return joined == literal;
    }

    public static boolean runtimeConcatenationSharesReference() {
        String head = "ja";
        String joined = head + "va";
        String literal = "java";
        return joined == literal;
    }

    public static boolean runtimeConcatenationIsEqual() {
        String head = "ja";
        String joined = head + "va";
        return joined.equals("java");
    }

    public static boolean internedConcatenationSharesReference() {
        String head = "ja";
        String joined = head + "va";
        String literal = "java";
        return joined.intern() == literal;
    }

    public static boolean sameText(String left, String right) {
        return Objects.equals(left, right);
    }

    public static boolean sameTextIgnoringCase(String left, String right) {
        if (left == null || right == null) {
            return left == right;
        }
        return left.equalsIgnoreCase(right);
    }

    public static Map<String, Boolean> report() {
        Map<String, Boolean> report = new LinkedHashMap<>();
        report.put("\"java\" == \"java\"", literalsShareReference());
        report.put("\"java\" == new String(\"java\")", literalAndNewStringShareReference());
        report.put("\"java\".equals(new String(\"java\"))", literalAndNewStringAreEqual());
        report.put("\"ja\" + \"va\" == \"java\"", compileTimeConcatenationSharesReference());
        report.put("final head + \"va\" == \"java\"", finalVariableConcatenationSharesReference());
        report.put("head + \"va\" == \"java\"", runtimeConcatenationSharesReference());
        report.put("(head + \"va\").equals(\"java\")", runtimeConcatenationIsEqual());
        report.put("(head + \"va\").intern() == \"java\"", internedConcatenationSharesReference());
        return report;
    }

    public static void main(String[] args) {
        report().forEach((expression, result) -> System.out.println(expression + " -> " + result));
    }
}

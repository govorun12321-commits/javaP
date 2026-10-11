/*
 * Задание 6.
 * Реализуйте `Result<T>` или `Page<T>` без raw types.
 * Добавьте фабрики успеха/ошибки, инварианты и методы чтения.
 * Покажите две конкретизации и compile-time защиту от смешения.
 *
 * Пояснения:
 * Инвариант:
 *   - ровно одно из value/error не null;
 *   - конструктор приватный, создание только фабриками.
 *
 * Compile-time защита (не компилируется):
 *     Result<String> s = Result.success("x");
 *     Result<Integer> mixed = s;                    // Result<String> нельзя присвоить Result<Integer>
 *     int n = Result.<String>success("x").value();  // String нельзя присвоить int
 *
 * Защита работает на этапе компиляции (стирание типов),
 * в рантайме Result<String> и Result<Integer> - один класс.
 */

import java.util.*;
import java.util.function.Function;

public class task_06 {

    static Result<Integer> parseAge(String s) {
        try {
            int age = Integer.parseInt(s.trim());

            return age < 0
                    ? Result.failure("Возраст < 0: " + age)
                    : Result.success(age);
        } catch (NumberFormatException e) {
            return Result.failure("Не число: '" + s + "'");
        }
    }

    static Result<String> findTitle(Map<String, String> catalog, String id) {
        String title = catalog.get(id);

        return title == null
                ? Result.failure("Нет курса " + id)
                : Result.success(title);
    }

    public static void main(String[] args) {
        // 1. Две конкретизации Result<Integer>
        Result<Integer> a = parseAge("19");
        Result<Integer> b = parseAge("abc");

        System.out.println(a + " | " + b);
        System.out.println(
                "a.value()+1 = " + (a.value() + 1)
                        + ", b.orElse(-1) = " + b.orElse(-1)
        );
        System.out.println(
                "map: " + a.map(x -> x * 2)
                        + " | " + b.map(x -> x * 2)
        );

        // 2. Конкретизация Result<String>
        Map<String, String> catalog = Map.of("c1", "Java");

        Result<String> t1 = findTitle(catalog, "c1");
        Result<String> t2 = findTitle(catalog, "c9");

        System.out.println(
                t1 + " | " + t2
                        + " | длина=" + t1.map(String::length)
        );

        // 3. Проверка инвариантов
        try {
            Result.success(null);
        } catch (NullPointerException e) {
            System.out.println("success(null) -> NPE");
        }

        try {
            Result.failure(" ");
        } catch (IllegalArgumentException e) {
            System.out.println("failure(\" \") -> IAE");
        }

        try {
            b.value();
        } catch (IllegalStateException e) {
            System.out.println("value() у Failure -> " + e.getMessage());
        }

        try {
            a.error();
        } catch (IllegalStateException e) {
            System.out.println("error() у Success -> " + e.getMessage());
        }
    }
}

final class Result<T> {
    private final T value;
    private final String error;

    private Result(T value, String error) {
        this.value = value;
        this.error = error;
    }

    static <T> Result<T> success(T value) {
        return new Result<>(Objects.requireNonNull(value, "value"), null);
    }

    static <T> Result<T> failure(String error) {
        Objects.requireNonNull(error, "error");

        if (error.isBlank()) {
            throw new IllegalArgumentException(
                    "Сообщение об ошибке не должно быть пустым"
            );
        }

        return new Result<>(null, error);
    }

    boolean isSuccess() {
        return error == null;
    }

    T value() {
        if (!isSuccess()) {
            throw new IllegalStateException("Нет значения, ошибка: " + error);
        }

        return value;
    }

    String error() {
        if (isSuccess()) {
            throw new IllegalStateException("Ошибки нет, результат успешен");
        }

        return error;
    }

    T orElse(T fallback) {
        return isSuccess() ? value : fallback;
    }

    <R> Result<R> map(Function<? super T, ? extends R> f) {
        return isSuccess()
                ? Result.success(f.apply(value))
                : Result.failure(error);
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Result<?> r
                && Objects.equals(value, r.value)
                && Objects.equals(error, r.error);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value, error);
    }

    @Override
    public String toString() {
        return isSuccess()
                ? "Success(" + value + ")"
                : "Failure(" + error + ")";
    }
}
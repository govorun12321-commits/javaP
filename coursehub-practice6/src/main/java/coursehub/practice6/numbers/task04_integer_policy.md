# Задание 4. Autoboxing и `null`

## Условие

Воспроизведите NPE при unboxing `Integer`, сравните `Integer` через `==` на разных значениях и исправьте код через явный контракт. Не полагайтесь на cache wrapper-объектов.

## Где код

- `IntegerPolicy.java`
- тесты: `IntegerPolicyTest.java`

## Воспроизведение NPE

- `unboxUnsafely(null)` и `sumUnsafely(1, null)` — неявный вызов `intValue()` на `null`.
- `quantityUnsafely(map, "SQL-001")` — классический случай: `map.get` вернул `null`, присваивание в `int` упало.

## `==` на разных значениях

Результат `IntegerPolicy.main` (два раза `Integer x = value`, затем `==`):

| Значение | `==` |
|---|---|
| 100 | true |
| 127 | true |
| 128 | false |
| 1000 | false |
| -129 | false |

Результат совпал с известным кэшем `-128..127`, но это деталь реализации (граница настраивается `-XX:AutoBoxCacheMax`, тесты на это не опираются). Тесты проверяют только сравнение по значению для 0, 100, 127, 128, 1000, -129 и 100 000.

## Явный контракт

Правила в `IntegerPolicy`:

1. Если значения обязательны, проверяем на входе: `requireValue(value, "quantity")` бросает `NullPointerException` с понятным сообщением `quantity must not be null`.
2. Если отсутствие допустимо, выбираем семантику явно: `valueOrDefault(value, fallback)`, `toOptional(value)`, `quantityOrZero(map, key)`.
3. `Integer` сравниваем через `Objects.equals` (`sameValue`) или после явной распаковки обязательных значений (`sameRequiredValue`). `==` для обёрток не используем.
4. Там, где `null` невозможен по смыслу, используем примитив `int`.

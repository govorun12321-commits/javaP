# Задание 11. `Instant` и пользовательская зона

## Условие

Храните момент вебинара как `Instant`, отображайте в трёх `ZoneId`. Покажите, почему серверная default zone не должна неявно менять бизнес-результат.

## Где код

- `WebinarSchedule.java`
- тесты: `WebinarScheduleTest.java`

## Решение

`WebinarSchedule` хранит `startsAt` как `Instant` и `duration` как `Duration`. Зона нужна только для отображения:

| Зона | Время начала для `2026-11-05T22:30:00Z` |
|---|---|
| `Europe/Riga` | 2026-11-06 00:30 (+02:00) |
| `America/New_York` | 2026-11-05 17:30 (−05:00) |
| `Asia/Tokyo` | 2026-11-06 07:30 (+09:00) |

Это один и тот же момент: `toInstant()` у всех трёх значений совпадает, `isEqual` даёт `true`, а `equals` у `ZonedDateTime` — `false`, потому что зоны разные.

## Почему default zone опасна

Бизнес-дата вебинара (например, «в какой день он идёт») зависит от зоны:

- в `UTC` и `America/New_York` это 5 ноября, в `Europe/Riga` — 6 ноября;
- метод `dateInSystemDefaultZone()` использует `ZoneId.systemDefault()`. Тест подменяет default zone: на `Pacific/Auckland` получается `2026-11-06`, на `America/Los_Angeles` — `2026-11-05`. Один и тот же код и данные дают разный результат на разных серверах;
- метод `dateIn(zone)` с явной зоной от этого не зависит (тест `explicitOrganizerZoneGivesStableResult`).

Правило: default zone допустима только на самой границе системы (например, как значение по умолчанию для UI), а в бизнес-логику зона всегда передаётся явно.

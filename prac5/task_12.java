/*
 * Задание 12.
 * Спроектируйте `Repository<ID,T>` с `save`, `findById`, `findAll`,
 * `deleteById`.
 * In-memory реализация:
 *   - не отдаёт наружу изменяемую внутреннюю коллекцию,
 *   - не принимает `null`,
 *   - определяет семантику повторного save.
 *
 * Пояснения:
 * - Повторный save = upsert:
 *     сущность с тем же id заменяется, позиция в findAll не меняется.
 * - LinkedHashMap: поиск O(1) + детерминированный порядок.
 *   findAll возвращает List.copyOf (неизменяемый снимок).
 * - Сущности - неизменяемые record, иначе наружу утекает состояние.
 *   null отвергается requireNonNull.
 */

import java.util.*;
import java.util.function.Function;

public class task_12 {

    record Course(String id, String title) {}

    public static void main(String[] args) {
        Repository<String, Course> repo = new InMemoryRepository<>(Course::id);


        repo.save(new Course("c1", "Java"));
        repo.save(new Course("c2", "SQL"));
        repo.save(new Course("c1", "Java 21"));   

        System.out.println("findAll: " + repo.findAll());
        System.out.println(
                "findById(c1): " + repo.findById("c1")
                        + ", findById(zzz): " + repo.findById("zzz")
        );

        List<Course> snapshot = repo.findAll();

        try {
            snapshot.add(new Course("c3", "Go"));
        } catch (UnsupportedOperationException e) {
            System.out.println(
                    "снимок неизменяем -> UnsupportedOperationException"
            );
        }

        repo.save(new Course("c3", "Go"));
        System.out.println(
                "ранее взятый снимок не изменился: " + snapshot.size()
                        + ", репозиторий: " + repo.findAll().size()
        );

        System.out.println(
                "deleteById(c2) = " + repo.deleteById("c2")
                        + ", повторно = " + repo.deleteById("c2")
        );

        try {
            repo.save(null);
        } catch (NullPointerException e) {
            System.out.println("save(null) -> NPE: " + e.getMessage());
        }

        try {
            repo.findById(null);
        } catch (NullPointerException e) {
            System.out.println("findById(null) -> NPE: " + e.getMessage());
        }

        try {
            repo.deleteById(null);
        } catch (NullPointerException e) {
            System.out.println("deleteById(null) -> NPE: " + e.getMessage());
        }
    }
}

interface Repository<ID, T> {
    T save(T entity);

    Optional<T> findById(ID id);

    List<T> findAll();

    boolean deleteById(ID id);
}

final class InMemoryRepository<ID, T> implements Repository<ID, T> {

    private final Map<ID, T> storage = new LinkedHashMap<>();
    private final Function<? super T, ? extends ID> idExtractor;

    InMemoryRepository(Function<? super T, ? extends ID> idExtractor) {
        this.idExtractor = Objects.requireNonNull(
                idExtractor,
                "idExtractor"
        );
    }

    @Override
    public T save(T entity) {
        Objects.requireNonNull(entity, "entity");

        ID id = Objects.requireNonNull(
                idExtractor.apply(entity),
                "id сущности"
        );

        storage.put(id, entity);
        return entity;
    }

    @Override
    public Optional<T> findById(ID id) {
        Objects.requireNonNull(id, "id");
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public List<T> findAll() {
        return List.copyOf(storage.values());
    }

    @Override
    public boolean deleteById(ID id) {
        Objects.requireNonNull(id, "id");
        return storage.remove(id) != null;
    }
}
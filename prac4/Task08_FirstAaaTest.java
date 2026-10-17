import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/*
 * Задание 8. Первый тест по AAA
 *
 * Настройте JUnit Jupiter в Maven. Напишите тест успешного `Course.completeHours`:
 * отдельные Arrange, Act, Assert, осмысленное имя и одна причина падения.
 *
 * Краткое объяснение:
 *  Настройка Maven (полный файл лежит в prac4/pom.xml). Главное:
 *
 *    <properties>
 *        <maven.compiler.release>17</maven.compiler.release>
 *        <junit.version>5.10.2</junit.version>
 *    </properties>
 *    <dependencies>
 *        <dependency>
 *            <groupId>org.junit.jupiter</groupId>
 *            <artifactId>junit-jupiter</artifactId>
 *            <version>${junit.version}</version>
 *            <scope>test</scope>
 *        </dependency>
 *    </dependencies>
 *    <build><plugins><plugin>
 *        <artifactId>maven-surefire-plugin</artifactId>
 *        <version>3.2.5</version>   <!-- нужен 3.x, чтобы запускать JUnit 5 -->
 *    </plugin></plugins></build>
 *
 *  Запуск: mvn test
 *
 *  Имя теста: метод_условие_результат. Блоки Arrange / Act / Assert разделены комментариями.
 *  Одна причина падения: проверяется только completedHours (остальное — отдельные тесты).
 */
class Task08_FirstAaaTest {

    @Test
    void completeHours_hoursWithinRemaining_increasesCompletedHours() {
        // Arrange
        Course course = new Course(10, "Java Fundamentals", 32);

        // Act
        course.completeHours(6);

        // Assert
        assertEquals(6, course.completedHours());
    }
}

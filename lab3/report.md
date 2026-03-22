# Практическая работа №3

## Тема: "Статический и динамический анализ приложения"

**Дисциплина:** Методы тестирования программного обеспечения

**Выполнил:** студент группы ________ ФИО ________

**Преподаватель:** ________

**Дата:** 2026

---

## Содержание

1. [Введение](#введение)
2. [Описание фаззинг-тестирования](#1-динамический-анализ-фаззинг-тестирование)
   - [Описание фаззера и основных команд](#11-описание-фаззера-jazzer)
   - [Последовательность действий по запуску фаззера](#12-последовательность-действий-по-запуску-фаззера)
   - [Описание тестовых программ](#13-описание-тестовых-программ-фаззинг-целей)
   - [Описание экспериментов по фаззингу](#14-описание-экспериментов-по-фаззингу)
3. [Описание статического анализа](#2-статический-анализ)
   - [Анализатор 1: SonarQube](#21-анализатор-1-sonarqube-из-списка-а)
   - [Анализатор 2: SpotBugs](#22-анализатор-2-spotbugs-произвольный)
   - [Результаты статического анализа](#23-результаты-статического-анализа)
   - [Комплексная таблица дефектов](#24-комплексная-таблица-дефектов)
   - [Сравнение анализаторов](#25-сравнение-анализаторов)
4. [Заключение](#заключение)
5. [Источники](#источники)
6. [Приложение: код](#приложение-код)

---

## Введение

Данная работа является продолжением практических работ №1 и №2, в которых было разработано консольное Java-приложение для решения NP-полной задачи **Subset Sum** двумя алгоритмами динамического программирования:

- **ArrayDPSolver** — ДП на двумерном массиве `boolean[][]`, работает только с неотрицательными числами, сложность O(n x target);
- **HashMapDPSolver** — ДП на `HashMap<Integer, Integer>`, поддерживает отрицательные числа, сложность O(n x |достижимые суммы|).

Приложение было покрыто 128+ модульными тестами (JUnit 5, Mockito, Hamcrest) и BDD-сценариями (Cucumber).

В рамках текущей работы проводится:

1. **Динамический анализ** — фаззинг-тестирование двух модулей программы с помощью фаззера **Jazzer** для Java;
2. **Статический анализ** — поиск дефектов в коде с помощью двух статических анализаторов: **SonarQube** (из списка "А") и **SpotBugs** (произвольный), с классификацией дефектов по **CWE** и **БДУ ФСТЭК**.

### Внесённые ошибки

Для демонстрации возможностей инструментов анализа в код были намеренно внесены 2 ошибки:

| # | Модуль | Описание ошибки | CWE |
|---|--------|-----------------|-----|
| 1 | `ArrayDPSolver.solve()` | Отсутствие проверки на переполнение `target + 1` при выделении массива `boolean[n+1][target+1]`. При `target = Integer.MAX_VALUE` значение `target + 1` переполняется до `Integer.MIN_VALUE`, вызывая `NegativeArraySizeException` или `OutOfMemoryError` | CWE-190 |
| 2 | `JsonFileReader.readFromFilePath()` | Создание `FileInputStream` без последующего закрытия ресурса. Поток не оборачивается в `try-with-resources`, что приводит к утечке файловых дескрипторов | CWE-404 |

---

## 1. Динамический анализ: фаззинг-тестирование

### 1.1. Описание фаззера Jazzer

**Jazzer** — это coverage-guided фаззер для JVM-приложений, разработанный компанией Code Intelligence. Jazzer основан на **libFuzzer** (часть проекта LLVM) и использует инструментацию байткода JVM для получения обратной связи по покрытию кода.

**Ключевые характеристики:**
- Интеграция с JUnit 5 через аннотацию `@FuzzTest`
- Поддержка Java, Kotlin, Scala и других JVM-языков
- Автоматическая генерация и мутация входных данных
- Обнаружение крэшей, необработанных исключений, OOM, утверждений
- Сохранение минимизированных воспроизводящих входов (crash corpus)
- Возможность работы в режиме регрессионного тестирования

**Основные команды и параметры:**

| Команда / параметр | Описание |
|-------------------|----------|
| `@FuzzTest` | Аннотация JUnit 5 для объявления фаззинг-теста |
| `@FuzzTest(maxDuration = "5m")` | Ограничение времени фаззинга (по умолчанию — бесконечно) |
| `FuzzedDataProvider` | Интерфейс для структурированной генерации данных из случайных байтов |
| `data.consumeInt(min, max)` | Генерация случайного int в диапазоне [min, max] |
| `data.consumeInt()` | Генерация случайного int во всём диапазоне Integer |
| `JAZZER_FUZZ=1` | Переменная окружения для запуска в режиме фаззинга (не регрессии) |
| `FuzzerSecurityIssueLow` | Исключение, сигнализирующее об обнаруженном дефекте безопасности |

**Режимы работы:**
1. **Режим фаззинга** (`JAZZER_FUZZ=1`): непрерывная генерация входных данных с мутациями, управляемыми обратной связью по покрытию кода
2. **Режим регрессии** (без `JAZZER_FUZZ`): воспроизведение ранее найденных крэшей из корпуса

### 1.2. Последовательность действий по запуску фаззера

#### Настройка окружения

**1. Добавление зависимости Jazzer в `pom.xml`:**

```xml
<dependency>
    <groupId>com.code-intelligence</groupId>
    <artifactId>jazzer-junit</artifactId>
    <version>0.22.1</version>
    <scope>test</scope>
    <exclusions>
        <!-- Исключение конфликтующих версий JUnit Platform -->
        <exclusion>
            <groupId>org.junit.platform</groupId>
            <artifactId>junit-platform-commons</artifactId>
        </exclusion>
        <exclusion>
            <groupId>org.junit.platform</groupId>
            <artifactId>junit-platform-launcher</artifactId>
        </exclusion>
    </exclusions>
</dependency>
<dependency>
    <groupId>org.junit.platform</groupId>
    <artifactId>junit-platform-launcher</artifactId>
    <version>1.10.2</version>
    <scope>test</scope>
</dependency>
```

> **Важно:** Jazzer 0.22.1 поставляется с `junit-platform-commons:1.9.0` и `junit-platform-launcher:1.9.0`, которые конфликтуют с JUnit 5.10.2 (platform 1.10.2). Без исключений тест-движок JUnit Jupiter не может обнаружить тесты: `TestEngine with ID 'junit-jupiter' failed to discover tests`.

**2. Создание фаззинг-целей** в пакете `com.subsetsum.fuzz`:

```
src/test/java/com/subsetsum/fuzz/
    ArrayDPSolverFuzzTest.java
    HashMapDPSolverFuzzTest.java
```

**3. Компиляция:**

```bash
mvn compile test-compile
```

**4. Запуск фаззинга:**

```bash
# Запуск фаззинга ArrayDPSolver
JAZZER_FUZZ=1 mvn test -Pfuzz -Dtest="ArrayDPSolverFuzzTest#fuzzArrayDPSolver"

# Запуск фаззинга HashMapDPSolver
JAZZER_FUZZ=1 mvn test -Pfuzz -Dtest="HashMapDPSolverFuzzTest#fuzzHashMapDPSolver"

# Регрессионный запуск (воспроизведение найденных крэшей)
mvn test -Pfuzz -Dtest="ArrayDPSolverFuzzTest,HashMapDPSolverFuzzTest"
```

> **Примечание:** Профиль `fuzz` (`-Pfuzz`) выполняет две функции:
> 1. Исключает BDD-тесты (Cucumber) из classpath, чтобы они не запускались параллельно с фаззером;
> 2. Увеличивает лимит памяти JVM до 4 ГБ (`-Xmx4g`).
>
> Переменная `JAZZER_FUZZ=1` передаётся отдельно для перевода Jazzer в режим активного фаззинга. Без неё Jazzer работает в режиме регрессии (воспроизведение ранее найденных крэшей из корпуса).

### 1.3. Описание тестовых программ (фаззинг-целей)

#### Фаззинг-цель 1: ArrayDPSolverFuzzTest

```java
@FuzzTest(maxDuration = "5m")
void fuzzArrayDPSolver(FuzzedDataProvider data) {
    int size = data.consumeInt(1, 50);
    int target = data.consumeInt(0, Integer.MAX_VALUE);

    List<Integer> numbers = new ArrayList<>();
    for (int i = 0; i < size; i++) {
        numbers.add(data.consumeInt(0, 10000));
    }

    SubsetSumInput input = new SubsetSumInput(numbers, target);

    try {
        SubsetSumResult result = solver.solve(input);

        // Проверка инварианта
        if (result.isFound()) {
            int actualSum = result.getSubset().stream()
                .mapToInt(Integer::intValue).sum();
            if (actualSum != target) {
                throw new AssertionError("Subset sum mismatch");
            }
        }
    } catch (IllegalArgumentException e) {
        // Ожидаемое исключение — игнорируем
    }
    // NegativeArraySizeException и OOM НЕ перехватываются
}
```

**Стратегия фаззинга:**
- Размер входного массива: от 1 до 50 элементов
- Целевая сумма: от 0 до `Integer.MAX_VALUE` (2 147 483 647)
- Значения элементов: от 0 до 10 000 (неотрицательные, как требует ArrayDP)
- Перехватываются только `IllegalArgumentException` (валидация)
- `OutOfMemoryError` и `NegativeArraySizeException` — это баги, которые Jazzer обнаруживает

#### Фаззинг-цель 2: HashMapDPSolverFuzzTest

```java
@FuzzTest(maxDuration = "5m")
void fuzzHashMapDPSolver(FuzzedDataProvider data) {
    int size = data.consumeInt(1, 30);
    int target = data.consumeInt();

    List<Integer> numbers = new ArrayList<>();
    for (int i = 0; i < size; i++) {
        numbers.add(data.consumeInt());
    }

    SubsetSumInput input = new SubsetSumInput(numbers, target);

    try {
        SubsetSumResult result = solver.solve(input);

        // Проверка инварианта: сумма подмножества == target
        if (result.isFound()) {
            int actualSum = result.getSubset().stream()
                .mapToInt(Integer::intValue).sum();
            if (actualSum != target) {
                throw new AssertionError("Subset sum mismatch: "
                    + actualSum + " != " + target);
            }
        }
    } catch (IllegalArgumentException e) {
        // Ожидаемое
    }
}
```

**Стратегия фаззинга:**
- Размер входного массива: от 1 до 30 элементов
- Целевая сумма и значения элементов: весь диапазон `int` (включая отрицательные)
- Проверка инварианта: если решение найдено, сумма подмножества должна равняться target

### 1.4. Описание экспериментов по фаззингу

#### Условия проведения эксперимента

| Параметр | Значение |
|----------|----------|
| ОС | macOS 26.2 (Darwin 25.2.0) |
| Архитектура | ARM64 (Apple Silicon) |
| Процессор | Apple M3 |
| ОЗУ | 16 GB |
| Java | OpenJDK 17.0.14 (Homebrew) |
| JVM Heap (по умолчанию) | ~4 GB |
| Maven | 3.9.10 |
| Jazzer | 0.22.1 |
| Максимальная длительность | 5 минут на цель |

#### Эксперимент 1: Фаззинг ArrayDPSolver

**Результат:** ошибка обнаружена.

**Время обнаружения:** ~117 секунд (из 300 допустимых).

**Обнаруженная ошибка:**
```
com.code_intelligence.jazzer.api.FuzzerSecurityIssueLow:
    Out of memory (use '-Xmx3686m' to reproduce)
Caused by: java.lang.OutOfMemoryError: Java heap space
    at com.subsetsum.algorithm.ArrayDPSolver.solve(ArrayDPSolver.java:51)
```

**Строка с ошибкой** (`ArrayDPSolver.java:51`):
```java
boolean[][] dp = new boolean[n + 1][target + 1];
```

**Подобранные данные:** Jazzer сгенерировал 5 байт входных данных (`24 83 0a 24 83`), которые при декодировании через `FuzzedDataProvider` создали входные данные с большим значением `target`. При попытке выделить массив `boolean[n+1][target+1]`, где `target` близок к `Integer.MAX_VALUE`, JVM не может аллоцировать такой объём памяти.

**Сохранённый краш-файл:**
```
src/test/resources/com/subsetsum/fuzz/ArrayDPSolverFuzzTestInputs/
    fuzzArrayDPSolver/crash-f5f96ceec260be4b2d5d38b140b3ec42e7bdde43
```

**Описание ошибки:** Алгоритм ArrayDP выделяет двумерный массив размером `(n+1) x (target+1)`. При target = 2 147 483 647 (Integer.MAX_VALUE) значение `target + 1` переполняется до -2 147 483 648 (Integer.MIN_VALUE), что вызывает `NegativeArraySizeException`. Для значений target, близких к MAX_VALUE, но не равных ему, массив формально корректен, но его размер (~2 ГБ x n строк) превышает доступную память JVM, вызывая `OutOfMemoryError`.

**Классификация:** CWE-190 (Integer Overflow or Wraparound), CWE-789 (Memory Allocation with Excessive Size Value).

#### Эксперимент 2: Фаззинг HashMapDPSolver

**Результат:** ошибка НЕ обнаружена.

**Время работы:** 303 секунды (полные 5 минут).

**Количество итераций:** Jazzer выполнил несколько миллионов итераций, генерируя различные комбинации положительных, отрицательных и граничных значений.

**Почему ошибка не найдена:** HashMapDPSolver использует `HashMap` вместо массива, поэтому:
- Нет фиксированного выделения памяти пропорционально `target`
- HashMap растёт инкрементально и пропорционально количеству достижимых сумм
- Алгоритм не зависит от абсолютного значения `target`
- Инвариант (сумма подмножества == target) выполняется корректно для всех найденных Jazzer'ом входов

Это демонстрирует, что HashMap-реализация значительно более устойчива к граничным входным данным.

#### Итоговая таблица фаззинг-экспериментов

| Параметр | ArrayDPSolver | HashMapDPSolver |
|----------|:------------:|:---------------:|
| Время работы | 117 сек | 303 сек (макс.) |
| Результат | Найден крэш | Крэш не найден |
| Тип ошибки | FuzzerSecurityIssueLow (OOM) | — |
| Корневая причина | Integer overflow в `target + 1` | — |
| CWE | CWE-190, CWE-789 | — |
| Размер краш-входа | 5 байт | — |
| Уровень серьёзности | Средний | — |

#### Оценка удобства и эффективности фаззера

**Преимущества Jazzer:**
1. **Простота интеграции** — аннотация `@FuzzTest` интегрируется в существующую инфраструктуру JUnit 5
2. **Автоматическая минимизация** — краш-вход автоматически минимизирован до 5 байт
3. **Регрессионный режим** — найденные крэши автоматически становятся регрессионными тестами
4. **Скорость обнаружения** — OOM найден за ~2 минуты из 5
5. **Детализация** — точный стектрейс с указанием строки кода

**Недостатки:**
1. **Конфликт зависимостей** — требуется ручное исключение JUnit Platform зависимостей
2. **Ложные срабатывания** — OOM может быть артефактом ограниченного heap JVM, а не реальной ошибкой
3. **Ограниченность для логических ошибок** — не нашёл потенциальных проблем с integer overflow в суммах HashMapDP
4. **Затраты времени** — для полноценного фаззинга нужны часы/дни, 5 минут — минимум

---

## 2. Статический анализ

### 2.1. Анализатор 1: SonarQube (из списка "А")

#### Общая информация

**SonarQube** — платформа непрерывного контроля качества кода, разработанная компанией SonarSource (Швейцария). Является одним из наиболее распространённых инструментов статического анализа в индустрии.

| Параметр | Значение |
|----------|----------|
| Разработчик | SonarSource SA |
| Лицензия | Community Edition — LGPL v3 (бесплатно); Developer/Enterprise — коммерческие |
| Скачивание | https://www.sonarsource.com/products/sonarqube/downloads/ |
| Документация | https://docs.sonarsource.com/sonarqube/latest/ |
| Поддерживаемые языки | 30+ (Java, JavaScript, C/C++, Python, C#, Go и др.) |
| Правила для Java | 600+ |
| Страница правил | https://rules.sonarsource.com/java/ |
| Использованная версия | **26.3.0.120487** (Community Edition, Docker) |

#### Основные вехи установки и настройки

1. **Установка SonarQube Server** (через Docker):
```bash
docker pull sonarqube:community
docker run -d --name sonarqube -p 9000:9000 sonarqube:community
```

2. **Ожидание запуска** (~30-60 секунд), проверка готовности:
```bash
curl -s http://localhost:9000/api/system/status
# {"id":"...","version":"26.3.0.120487","status":"UP"}
```

3. **Смена пароля по умолчанию** и создание токена для анализа:
```bash
# Смена пароля (admin/admin → свой)
curl -u admin:admin -X POST \
  "http://localhost:9000/api/users/change_password?login=admin&previousPassword=admin&password=<NEW>"

# Генерация токена
curl -u admin:<NEW> -X POST \
  "http://localhost:9000/api/user_tokens/generate?name=lab3-token&type=GLOBAL_ANALYSIS_TOKEN"
```

4. **Запуск анализа** через Maven SonarScanner (плагин не нужно добавлять в pom.xml):
```bash
mvn compile sonar:sonar \
  -Dsonar.projectKey=subset-sum-solver \
  -Dsonar.projectName="Subset Sum Solver" \
  -Dsonar.host.url=http://localhost:9000 \
  -Dsonar.token=<TOKEN>
```

5. **Просмотр результатов** — веб-интерфейс http://localhost:9000 или API:
```bash
curl -u admin:<PASS> "http://localhost:9000/api/issues/search?componentKeys=subset-sum-solver&scopes=MAIN"
```

#### Виды статического анализа SonarQube

SonarQube осуществляет следующие виды анализа:

1. **Анализ надёжности (Reliability)** — поиск багов: NPE, ресурсные утечки, некорректная логика
2. **Анализ безопасности (Security)** — поиск уязвимостей: инъекции, XSS, CSRF, утечки данных
3. **Анализ сопровождаемости (Maintainability)** — code smells: дублирование, сложность, именование
4. **Security Hotspots** — потенциальные уязвимости, требующие ручной проверки
5. **Анализ покрытия** — интеграция с JaCoCo/Cobertura для метрик покрытия

**Правила SonarQube, сработавшие на данном проекте:**

| Правило | Severity | Описание | Тип |
|---------|----------|----------|-----|
| S3776 | CRITICAL | Cognitive Complexity exceeds threshold | Code Smell |
| S1215 | CRITICAL | Explicit garbage collection call (Runtime.gc()) | Code Smell |
| S106 | MAJOR | Standard outputs should not be used directly | Code Smell |
| S3824 | MAJOR | Map.containsKey() followed by Map.put() | Code Smell |
| S1481 | MINOR | Unused local variables | Code Smell |
| S108 | MAJOR | Nested blocks of code should not be empty | Code Smell |
| S6204 | MAJOR | Use Stream.toList() instead of collect(Collectors.toList()) | Code Smell |

### 2.2. Анализатор 2: SpotBugs (произвольный)

#### Общая информация

**SpotBugs** — статический анализатор байткода Java, являющийся наследником проекта FindBugs. Анализирует скомпилированные `.class` файлы, находя потенциальные ошибки на основе шаблонов (bug patterns).

| Параметр | Значение |
|----------|----------|
| Разработчик | SpotBugs Community |
| Лицензия | LGPL v2.1 (свободная) |
| Скачивание | https://spotbugs.github.io/ |
| Maven плагин | https://spotbugs.readthedocs.io/en/stable/maven.html |
| Репозиторий | https://github.com/spotbugs/spotbugs |
| Количество шаблонов | 400+ |
| Страница шаблонов | https://spotbugs.readthedocs.io/en/stable/bugDescriptions.html |

#### Основные вехи установки и настройки

1. **Добавление Maven-плагина в `pom.xml`:**
```xml
<plugin>
    <groupId>com.github.spotbugs</groupId>
    <artifactId>spotbugs-maven-plugin</artifactId>
    <version>4.8.4.0</version>
    <configuration>
        <effort>Max</effort>
        <threshold>Low</threshold>
        <xmlOutput>true</xmlOutput>
        <htmlOutput>true</htmlOutput>
    </configuration>
</plugin>
```

2. **Запуск анализа:**
```bash
mvn compile spotbugs:spotbugs
```

3. **Просмотр результатов:**
- HTML-отчёт: `target/spotbugs.html`
- XML-отчёт: `target/spotbugsXml.xml`
- GUI (опционально): `mvn spotbugs:gui`

#### Виды статического анализа SpotBugs

SpotBugs классифицирует дефекты по категориям:

1. **Correctness** — ошибки корректности (NPE, бесконечные циклы, неправильные сравнения)
2. **Bad Practice** — нарушения лучших практик (игнорирование возвращаемых значений, нарушение контрактов)
3. **Performance** — проблемы производительности (неэффективные конструкции, лишние аллокации)
4. **Malicious Code Vulnerability** — уязвимости для вредоносного кода (экспозиция внутреннего представления)
5. **Dodgy Code** — подозрительный код (потенциальные NPE, избыточные проверки)
6. **Security** — уязвимости безопасности (инъекции, XSS, ненадёжное шифрование)
7. **Multithreaded Correctness** — проблемы многопоточности (гонки, взаимоблокировки)
8. **Internationalization** — проблемы интернационализации (кодировки, локали)
9. **Experimental** — экспериментальные проверки (ресурсные утечки)

### 2.3. Результаты статического анализа

#### Результаты SonarQube

SonarQube 26.3.0 Community Edition был развёрнут через Docker и проанализировал проект.

**Общие метрики проекта:**

| Метрика | Значение |
|---------|----------|
| Строк кода (ncloc) | 804 |
| Bugs | **0** |
| Vulnerabilities | **0** |
| Security Hotspots | **0** |
| Code Smells | **18** (6 в main, 12 в тестах) |
| Дублирование | 0.0% |
| Reliability Rating | **A** |
| Security Rating | **A** |
| Maintainability Rating | **A** |
| Время анализа | 3.7 сек |

**Дефекты в основном коде (6 шт.):**

| # | Правило | Severity | Файл | Строка | Описание |
|---|---------|----------|------|--------|----------|
| 1 | **S3776** | **CRITICAL** | ArrayDPSolver.java | 23 | Cognitive Complexity = 20 (допустимо: 15) |
| 2 | **S1215** | **CRITICAL** | ArrayDPSolver.java | 47 | Явный вызов runtime.gc() — непредсказуемое поведение |
| 3 | **S1215** | **CRITICAL** | HashMapDPSolver.java | 41 | Явный вызов runtime.gc() — непредсказуемое поведение |
| 4 | S3824 | MAJOR | HashMapDPSolver.java | 62 | containsKey() + put() → использовать computeIfAbsent() |
| 5 | S106 | MAJOR | App.java | 11 | System.out вместо логгера |
| 6 | **S3776** | **CRITICAL** | ConsoleMenu.java | 128 | Cognitive Complexity = 17 (допустимо: 15) |

> **Важное наблюдение:** SonarQube **не обнаружил** утечку ресурсов в `JsonFileReader.readFromFilePath()` (CWE-404), несмотря на наличие правила S2095 ("Resources should be closed"). Это связано с тем, что Jackson `ObjectMapper.readValue()` самостоятельно закрывает переданный `InputStream` после чтения, и SonarQube учитывает это поведение при анализе. Тем не менее, полагаться на такое поведение библиотеки — плохая практика, и **SpotBugs корректно отмечает это как потенциальную утечку**.

#### Результаты SpotBugs

SpotBugs обнаружил **13 дефектов** (effort=Max, threshold=Low):

| # | Тип | Категория | Приоритет | Файл | Строка | Описание |
|---|-----|-----------|-----------|------|--------|----------|
| 1 | DM_DEFAULT_ENCODING | I18N | 1 (High) | App.java | 10 | Использование Scanner без указания кодировки |
| 2 | EI_EXPOSE_REP2 | MALICIOUS_CODE | 2 (Medium) | JsonFileReader.java | 20 | Хранение ссылки на мутабельный ObjectMapper |
| 3 | **OBL_UNSATISFIED_OBLIGATION** | **EXPERIMENTAL** | **2** | **JsonFileReader.java** | **53** | **Утечка ресурса: FileInputStream не закрыт в readFromFilePath** |
| 4 | EI_EXPOSE_REP2 | MALICIOUS_CODE | 2 | JsonFileWriter.java | 21 | Хранение ссылки на мутабельный ObjectMapper |
| 5 | RV_RETURN_VALUE_IGNORED | BAD_PRACTICE | 2 | JsonFileWriter.java | 34 | Результат File.mkdirs() игнорируется |
| 6 | DM_CONVERT_CASE | I18N | 3 (Low) | ConsoleMenu.java | 160 | toLowerCase() без Locale |
| 7 | EI_EXPOSE_REP2 | MALICIOUS_CODE | 2 | ConsoleMenu.java | 41 | Хранение ссылки на мутабельный jsonWriter |
| 8 | EI_EXPOSE_REP2 | MALICIOUS_CODE | 2 | ConsoleMenu.java | 37 | Хранение ссылки на мутабельный out |
| 9 | EI_EXPOSE_REP2 | MALICIOUS_CODE | 2 | ConsoleMenu.java | 36 | Хранение ссылки на мутабельный scanner |
| 10 | NP_IMMEDIATE_DEREFERENCE | STYLE | 2 | ConsoleMenu.java | 111 | readLine() может вернуть null в handleFileInput |
| 11 | NP_IMMEDIATE_DEREFERENCE | STYLE | 2 | ConsoleMenu.java | 81 | readLine() может вернуть null в handleManualInput |
| 12 | NP_IMMEDIATE_DEREFERENCE | STYLE | 2 | ConsoleMenu.java | 52 | readLine() может вернуть null в run |
| 13 | NP_IMMEDIATE_DEREFERENCE | STYLE | 2 | ConsoleMenu.java | 160 | readLine() может вернуть null в solveAndDisplay |

**Распределение по категориям:**

| Категория | Количество |
|-----------|:----------:|
| MALICIOUS_CODE (EI_EXPOSE_REP2) | 5 |
| STYLE / Dodgy Code (NP_*) | 4 |
| I18N | 2 |
| BAD_PRACTICE | 1 |
| EXPERIMENTAL (ресурсная утечка) | 1 |

### 2.4. Комплексная таблица дефектов

В таблице ниже представлена комплексная информация по обнаруженным дефектам с сопоставлением кодов CWE и БДУ ФСТЭК.

| # | Дефект | SonarQube код | SpotBugs код | CWE | БДУ ФСТЭК | CWE Top-25 |
|---|--------|:------------:|:------------:|:---:|:----------:|:----------:|
| 1 | **Целочисленное переполнение target+1** (ArrayDPSolver:51) | — | — | **CWE-190** (Integer Overflow or Wraparound) | БДУ:2022-00735 | **Да (#14, 2023)** |
| 2 | **Утечка ресурса FileInputStream** (JsonFileReader:53) | — (не обнаружен) | **OBL_UNSATISFIED_OBLIGATION** | **CWE-404** (Improper Resource Shutdown or Release) | БДУ:2022-02301 | Нет |
| 3 | Потенциальный NPE при readLine() (ConsoleMenu:52,81,111,160) | — | **NP_IMMEDIATE_DEREFERENCE_OF_READLINE** | **CWE-476** (NULL Pointer Dereference) | БДУ:2021-01890 | **Да (#12, 2023)** |
| 4 | Высокая когнитивная сложность (ArrayDPSolver:23, ConsoleMenu:128) | **S3776** (CRITICAL) | — | **CWE-1121** (Excessive McCabe Cyclomatic Complexity) | — | Нет |
| 5 | Явный вызов runtime.gc() (ArrayDPSolver:47, HashMapDPSolver:41) | **S1215** (CRITICAL) | — | **CWE-1076** (Insufficient Adherence to Expected Conventions) | — | Нет |
| 6 | Экспозиция внутр. представления (JsonFileReader:20, JsonFileWriter:21, ConsoleMenu:36-41) | — | **EI_EXPOSE_REP2** | **CWE-374** (Passing Mutable Objects to Untrusted Method) | БДУ:2019-02729 | Нет |
| 7 | Игнорирование возврата mkdirs() (JsonFileWriter:34) | — | **RV_RETURN_VALUE_IGNORED** | **CWE-253** (Incorrect Check of Function Return Value) | БДУ:2020-03714 | Нет |
| 8 | System.out вместо логгера (App:11) | **S106** (MAJOR) | **DM_DEFAULT_ENCODING** | **CWE-778** (Insufficient Logging) | — | Нет |
| 9 | containsKey + put → computeIfAbsent (HashMapDPSolver:62) | **S3824** (MAJOR) | — | **CWE-407** (Inefficient Algorithmic Complexity) | — | Нет |

**Дефекты, входящие в CWE Top-25 (2023):**

- **CWE-190 (Integer Overflow or Wraparound)** — позиция **#14** в CWE Top 25 Most Dangerous Software Weaknesses 2023
- **CWE-476 (NULL Pointer Dereference)** — позиция **#12** в CWE Top 25 Most Dangerous Software Weaknesses 2023

### 2.5. Сравнение анализаторов

| Критерий | SonarQube | SpotBugs |
|----------|:---------:|:--------:|
| **Тип анализа** | Исходный код (AST) | Байткод (.class) |
| **Установка** | Docker-сервер + Maven-команда | Maven-плагин (одна команда) |
| **Сложность настройки** | Высокая (Docker, пароль, токен) | Низкая (3 строки в pom.xml) |
| **Время анализа** | 3.7 сек (+ загрузка на сервер) | 1.4 сек |
| **Количество правил (Java)** | 600+ | 400+ |
| **Веб-интерфейс** | Да (полнофункциональный) | Нет (есть GUI-приложение) |
| **CI/CD интеграция** | Встроенная (Quality Gate) | Через отчёты (XML/HTML) |
| **Обнаружение утечки ресурсов** | **Нет** (не обнаружил в данном случае) | **Да** (OBL_UNSATISFIED_OBLIGATION) |
| **Обнаружение NPE** | Нет (не обнаружил в данном случае) | **Да** (NP_IMMEDIATE_DEREFERENCE) |
| **Метрики сложности** | **Да** (Cognitive Complexity) | Нет |
| **Обнаружение bad practices** | **Да** (runtime.gc(), System.out) | **Да** (EI_EXPOSE_REP2, DM_*) |
| **Классификация по CWE** | Да (встроенная) | Частично (для части правил) |
| **Найдено дефектов (main)** | **6** (0 bugs, 6 code smells) | **13** (bugs + code smells) |
| **Стоимость** | Бесплатно (Community) | Бесплатно |

**Выводы по сравнению:**

1. **SpotBugs обнаружил больше критичных дефектов** — 13 против 6 у SonarQube. Ключевое преимущество: SpotBugs нашёл утечку ресурсов (CWE-404) и 4 потенциальных NPE (CWE-476), которые SonarQube пропустил.

2. **SonarQube лучше в анализе сложности** — обнаружил 4 CRITICAL-дефекта, связанных с когнитивной сложностью методов и вызовами runtime.gc(), которые SpotBugs не проверяет.

3. **Инструменты дополняют друг друга** — у них разные фокусы анализа:
   - SpotBugs: ресурсные утечки, NPE, мутабельные объекты (анализ потоков данных)
   - SonarQube: сложность кода, стиль, best practices (анализ AST + метрики)

4. **SonarQube удобнее для команд** — веб-интерфейс, история, Quality Gate, но требует серверной инфраструктуры (Docker).

5. **Рекомендация**: использовать оба анализатора совместно — SpotBugs для обнаружения скрытых багов, SonarQube для контроля качества и метрик.

---

## Заключение

В ходе данной практической работы было проведено комплексное исследование качества кода приложения Subset Sum Solver с применением методов динамического и статического анализа.

**Динамический анализ (фаззинг)** с помощью Jazzer позволил обнаружить критическую ошибку в модуле `ArrayDPSolver` — целочисленное переполнение при вычислении размера массива `boolean[n+1][target+1]` (CWE-190), приводящее к `OutOfMemoryError`. Ошибка была найдена за ~117 секунд из отведённых 5 минут, что демонстрирует высокую эффективность coverage-guided фаззинга для поиска ошибок, связанных с граничными значениями. Фаззинг модуля `HashMapDPSolver` крэшей не выявил, подтвердив большую устойчивость HashMap-реализации к экстремальным входным данным.

**Статический анализ** двумя инструментами — SonarQube (из списка "А") и SpotBugs (произвольный) — выявил в совокупности 9 типов дефектов, два из которых входят в CWE Top-25 наиболее опасных ошибок ПО (CWE-190 — #14, CWE-476 — #12). Анализаторы продемонстрировали взаимодополняющий характер: SpotBugs обнаружил утечку ресурсов (CWE-404) и потенциальные NPE, которые SonarQube пропустил, в то время как SonarQube выявил проблемы когнитивной сложности и нарушения стилевых конвенций, не входящие в область проверок SpotBugs.

Все обнаруженные дефекты были классифицированы по системам CWE и БДУ ФСТЭК, что позволяет оценить их с точки зрения безопасности и соответствия нормативным требованиям.

Результаты работы подтверждают, что совместное применение динамического и статического анализа обеспечивает значительно более полное покрытие потенциальных дефектов, чем использование каждого метода в отдельности.

---

## Источники

1. Банк данных угроз безопасности ФСТЭК России — https://bdu.fstec.ru/threat
2. MITRE Common Weakness Enumeration (CWE) — https://cwe.mitre.org/data/definitions/699.html
3. CWE Top 25 Most Dangerous Software Weaknesses (2023) — https://cwe.mitre.org/top25/archive/2023/2023_top25_list.html
4. Различие CWE, CVE, CVSS — https://habr.com/ru/companies/pvs-studio/articles/580474/
5. Группы ошибок CWE — https://cwe.mitre.org/data/definitions/699.html
6. Jazzer: Coverage-guided, in-process fuzzing for the JVM — https://github.com/CodeIntelligenceTesting/jazzer
7. SpotBugs: Static analysis tool for Java — https://spotbugs.github.io/
8. SpotBugs Bug Descriptions — https://spotbugs.readthedocs.io/en/stable/bugDescriptions.html
9. SonarQube Documentation — https://docs.sonarsource.com/sonarqube/latest/
10. SonarQube Rules for Java — https://rules.sonarsource.com/java/

---

## Приложение: код

### Изменённые и добавленные файлы

#### 1. JsonFileReader.java (добавлен метод `readFromFilePath` с внесённой ошибкой CWE-404)

```java
package com.subsetsum.io;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.subsetsum.model.SubsetSumInput;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class JsonFileReader {

    private final ObjectMapper objectMapper;

    public JsonFileReader() {
        this.objectMapper = new ObjectMapper();
    }

    public JsonFileReader(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public SubsetSumInput readFromFile(String filePath) throws IOException {
        if (filePath == null || filePath.isBlank()) {
            throw new IllegalArgumentException("filePath must not be null or blank");
        }
        File file = new File(filePath);
        if (!file.exists()) {
            throw new IOException("File not found: " + filePath);
        }
        if (!file.canRead()) {
            throw new IOException("Cannot read file: " + filePath);
        }
        return objectMapper.readValue(file, SubsetSumInput.class);
    }

    /**
     * ОШИБКА: FileInputStream не закрывается после чтения (утечка ресурсов).
     * CWE-404: Improper Resource Shutdown or Release
     */
    public SubsetSumInput readFromFilePath(String filePath) throws IOException {
        if (filePath == null || filePath.isBlank()) {
            throw new IllegalArgumentException("filePath must not be null or blank");
        }
        File file = new File(filePath);
        if (!file.exists()) {
            throw new IOException("File not found: " + filePath);
        }
        FileInputStream fis = new FileInputStream(file);  // НЕ закрывается!
        return objectMapper.readValue(fis, SubsetSumInput.class);
    }

    public SubsetSumInput readFromStream(InputStream inputStream) throws IOException {
        if (inputStream == null) {
            throw new IllegalArgumentException("inputStream must not be null");
        }
        return objectMapper.readValue(inputStream, SubsetSumInput.class);
    }
}
```

#### 2. ArrayDPSolver.java (существующая ошибка CWE-190, строка 51)

```java
// Строка 51 — при target = Integer.MAX_VALUE значение target+1 переполняется
boolean[][] dp = new boolean[n + 1][target + 1];
```

Полный код класса не изменялся — ошибка существовала изначально.

#### 3. ArrayDPSolverFuzzTest.java (новый файл)

```java
package com.subsetsum.fuzz;

import com.code_intelligence.jazzer.api.FuzzedDataProvider;
import com.code_intelligence.jazzer.junit.FuzzTest;
import com.subsetsum.algorithm.ArrayDPSolver;
import com.subsetsum.model.SubsetSumInput;
import com.subsetsum.model.SubsetSumResult;

import java.util.ArrayList;
import java.util.List;

class ArrayDPSolverFuzzTest {

    private final ArrayDPSolver solver = new ArrayDPSolver();

    @FuzzTest(maxDuration = "5m")
    void fuzzArrayDPSolver(FuzzedDataProvider data) {
        int size = data.consumeInt(1, 50);
        int target = data.consumeInt(0, Integer.MAX_VALUE);

        List<Integer> numbers = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            numbers.add(data.consumeInt(0, 10000));
        }

        SubsetSumInput input = new SubsetSumInput(numbers, target);

        try {
            SubsetSumResult result = solver.solve(input);

            if (result.isFound()) {
                int actualSum = result.getSubset().stream()
                    .mapToInt(Integer::intValue).sum();
                if (actualSum != target) {
                    throw new AssertionError(
                        "Нарушение инварианта: сумма подмножества "
                        + actualSum + " != целевая сумма " + target);
                }
            }
        } catch (IllegalArgumentException e) {
            // Ожидаемое исключение — игнорируем
        }
        // NegativeArraySizeException и OOM НЕ перехватываются
    }
}
```

#### 4. HashMapDPSolverFuzzTest.java (новый файл)

```java
package com.subsetsum.fuzz;

import com.code_intelligence.jazzer.api.FuzzedDataProvider;
import com.code_intelligence.jazzer.junit.FuzzTest;
import com.subsetsum.algorithm.HashMapDPSolver;
import com.subsetsum.model.SubsetSumInput;
import com.subsetsum.model.SubsetSumResult;

import java.util.ArrayList;
import java.util.List;

class HashMapDPSolverFuzzTest {

    private final HashMapDPSolver solver = new HashMapDPSolver();

    @FuzzTest(maxDuration = "5m")
    void fuzzHashMapDPSolver(FuzzedDataProvider data) {
        int size = data.consumeInt(1, 30);
        int target = data.consumeInt();

        List<Integer> numbers = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            numbers.add(data.consumeInt());
        }

        SubsetSumInput input = new SubsetSumInput(numbers, target);

        try {
            SubsetSumResult result = solver.solve(input);

            if (result.isFound()) {
                int actualSum = result.getSubset().stream()
                    .mapToInt(Integer::intValue).sum();
                if (actualSum != target) {
                    throw new AssertionError(
                        "Нарушение инварианта: сумма подмножества "
                        + actualSum + " != целевая сумма " + target
                        + ", подмножество: " + result.getSubset()
                        + ", входные числа: " + numbers);
                }
            }
        } catch (IllegalArgumentException e) {
            // Ожидаемое исключение — игнорируем
        }
    }
}
```

#### 5. pom.xml — ключевые изменения

Добавлены зависимости:
```xml
<!-- Jazzer для фаззинга -->
<dependency>
    <groupId>com.code-intelligence</groupId>
    <artifactId>jazzer-junit</artifactId>
    <version>0.22.1</version>
    <scope>test</scope>
</dependency>
```

Добавлены плагины:
```xml
<!-- SpotBugs -->
<plugin>
    <groupId>com.github.spotbugs</groupId>
    <artifactId>spotbugs-maven-plugin</artifactId>
    <version>4.8.4.0</version>
</plugin>

```

Добавлен Maven-профиль `fuzz` для изолированного запуска фаззинг-тестов:
```xml
<profile>
    <id>fuzz</id>
    <build>
        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-surefire-plugin</artifactId>
                <configuration>
                    <argLine>-Xmx4g</argLine>
                    <excludes>
                        <exclude>**/bdd/**</exclude>
                        <exclude>**/RunCucumberTest.java</exclude>
                    </excludes>
                </configuration>
            </plugin>
        </plugins>
    </build>
</profile>
```

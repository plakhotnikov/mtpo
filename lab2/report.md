# Практическая работа №2

## Тема: "Разработка кода через тестирование поведения"

---

## Содержание

1. [Введение](#введение)
2. [Описание плана BDD-тестирования](#описание-плана-bdd-тестирования)
   - [Основы Gherkin](#основы-gherkin)
   - [Выбор фреймворка](#выбор-фреймворка)
   - [Описание тестовых случаев и BDD-тестов](#описание-тестовых-случаев-и-bdd-тестов)
3. [Описание экспериментов](#описание-экспериментов)
   - [Условия проведения эксперимента](#условия-проведения-эксперимента)
   - [Алгоритм генерации наборов данных](#алгоритм-генерации-наборов-данных)
   - [Кодировка названий наборов данных](#кодировка-названий-наборов-данных)
   - [Предварительный эксперимент](#предварительный-эксперимент)
   - [Профилирование](#профилирование)
4. [Анализ результатов экспериментов](#анализ-результатов-экспериментов)
5. [Заключение](#заключение)
6. [Источники](#источники)
7. [Приложение](#приложение)

---

## Введение

Данная работа является продолжением практической работы №1, в которой было разработано консольное Java-приложение для решения NP-полной задачи Subset Sum двумя алгоритмами динамического программирования:
- **ArrayDP** — ДП на двумерном массиве `boolean[][]`, работает только с неотрицательными числами, сложность O(n × target);
- **HashMapDP** — ДП на `HashMap<Integer, Integer>`, поддерживает отрицательные числа, сложность O(n × |достижимые суммы|).

Приложение было покрыто 128 модульными тестами (JUnit 5, Mockito, Hamcrest).

В рамках текущей работы необходимо приобрести навыки по разработке через тестирование поведения (Behavior-Driven Development, BDD).

### Почему BDD — эволюция TDD

Дэн Норт в статье "Introducing BDD" [1] описывает путь, который привёл его от TDD к BDD. Он выделяет несколько ключевых проблем TDD:

1. **С чего начать тестирование?** Начинающие (и не только) разработчики теряются перед вопросом, какой тест написать первым и как его назвать. Норт предлагает решение: имена тестовых методов должны быть предложениями, начинающимися со слова "should" (например, `shouldReturnEmptySubsetWhenTargetIsZero`). Это смещает фокус с «тестирования метода X» на «описание поведения компонента».

2. **Непонятно, что именно тестировать.** TDD проверяет техническую корректность отдельных методов, но не гарантирует, что система решает бизнес-задачу. Норт предлагает отталкиваться от пользовательских историй: «Как [роль], я хочу [действие], чтобы [выгода]». Каждый сценарий описывает конкретное поведение системы с точки зрения пользователя.

3. **Тесты непонятны нетехническим участникам.** Названия тестов вроде `testArrayBoundsCheck` ничего не говорят бизнес-аналитику. Норт предлагает язык, понятный всем участникам проекта — именно так родился шаблон **Given-When-Then**: заданы начальные условия (Given), пользователь совершает действие (When), система реагирует определённым образом (Then).

Этот шаблон лёг в основу языка **Gherkin** ("Маринованный огурец") — декларативного языка описания поведения, который одновременно является и документацией, и исполняемой спецификацией.

---

## Описание плана BDD-тестирования

### Основы Gherkin

Gherkin — это структурированный язык описания поведения системы, используемый в BDD-фреймворках (Cucumber, Behave, SpecFlow и др.). Он позволяет записывать тестовые сценарии на естественном языке, приближенном к прозе, который затем автоматически связывается с исполняемым кодом.

**Структура Gherkin-файла (.feature):**

Каждый файл начинается с ключевого слова `Feature`, за которым следует пользовательская история в формате "As a / I want / So that":

```gherkin
Feature: Solving Subset Sum problem with ArrayDP algorithm
  As an application user
  I want to solve the Subset Sum problem using a tabular DP algorithm
  So that I can quickly find a subset with a given sum
```

Внутри Feature размещаются сценарии (`Scenario`), каждый из которых состоит из шагов:

| Ключевое слово | Назначение |
|---|---|
| `Given` | Предусловие — описывает начальное состояние системы |
| `When` | Действие — описывает, что делает пользователь |
| `Then` | Ожидаемый результат — описывает, что должно произойти |
| `And` | Дополнительный шаг того же типа, что и предыдущий |
| `But` | Контрастное утверждение — ожидание, противоречащее интуиции |

Пример сценария:
```gherkin
Scenario: Finding a subset in a simple set
  Given the input numbers are "3, 7, 1, 8, 2"
  And the target sum is 11
  When the solver runs
  Then the result should be "found"
  And the subset sum equals 11
  But the found subset size is less than 5
```

**Дополнительные конструкции:**

- **`Background`** — общие предусловия, выполняемые перед каждым сценарием в Feature (аналог `@BeforeEach` в JUnit);
- **`Rule`** — группировка сценариев по бизнес-правилу (логическая структуризация);
- **`Scenario Outline` + `Examples`** — параметризованный сценарий, выполняемый для каждой строки таблицы (аналог `@ParameterizedTest`);
- **`DataTable`** — входная таблица данных, передаваемая в шаг как параметр;
- **`DocString`** — многострочный текст (например, JSON), передаваемый в шаг;
- **Теги** (`@tag`) — метки для фильтрации и группировки сценариев (например, `@heavy`, `@business-idea`).

### Выбор фреймворка

Для реализации BDD-тестов выбран **Cucumber 7.15.0** для Java. Обоснование:
- Cucumber — стандартный и наиболее зрелый BDD-фреймворк для Java (первый релиз — 2012);
- тесная интеграция с JUnit 5 через модуль `cucumber-junit-platform-engine`;
- автоматическая генерация HTML и JSON отчётов;
- поддержка параметризации (`Scenario Outline`), таблиц данных (`DataTable`), тегов и хуков;
- шаги (step definitions) привязываются к Gherkin через аннотации `@Given`, `@When`, `@Then` с regex/Cucumber Expression.

Зависимости, добавленные в `pom.xml`:

```xml
<!-- Cucumber BDD -->
<dependency>
    <groupId>io.cucumber</groupId>
    <artifactId>cucumber-java</artifactId>
    <version>7.15.0</version>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>io.cucumber</groupId>
    <artifactId>cucumber-junit-platform-engine</artifactId>
    <version>7.15.0</version>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>org.junit.platform</groupId>
    <artifactId>junit-platform-suite</artifactId>
    <version>1.10.2</version>
    <scope>test</scope>
</dependency>
```

Тяжёлые нагрузочные тесты выделены в Maven-профиль `heavy` с увеличенной JVM-памятью (`-Xmx8g`) и фильтром Cucumber по тегу `@heavy`.

### Описание тестовых случаев и BDD-тестов

Разработано **6 feature-файлов** (5 основных + 1 нагрузочный):

---

#### Feature 1: `array_solver.feature` — ArrayDP-солвер (11 сценариев)

**Пользовательская история:** As an application user, I want to solve the Subset Sum problem using a tabular DP algorithm on `boolean[][]`, so that I can quickly find a subset with a given sum for non-negative numbers.

**Демонстрируемые элементы:** Background, Rule (×2), Scenario Outline + Examples, But.

```gherkin
Background:
  Given the solver is "ArrayDP"

Rule: Correctly finding subsets for non-negative numbers

  Scenario: Finding a subset in a simple set
    Given the input numbers are "3, 7, 1, 8, 2"
    And the target sum is 11
    When the solver runs
    Then the result should be "found"
    And the subset sum equals 11
    But the found subset size is less than 5   # ← But: нашли, но не весь набор

Rule: Input data validation

  Scenario: Negative numbers are not supported
    Given the input numbers are "1, -2, 3"
    And the target sum is 2
    When the solver runs
    Then an error should occur with message containing "negative"
```

---

#### Feature 2: `hashmap_solver.feature` — HashMapDP-солвер (8 сценариев)

**Пользовательская история:** As an application user, I want to solve the Subset Sum problem using a HashMap-based DP algorithm, so that I can find subsets even when negative numbers are present.

**Демонстрируемые элементы:** Background, Rule, DataTable (входная таблица), Scenario Outline.

```gherkin
Scenario: Data table with expected results
  When the solver is run for each row of the table:
    | numbers            | targetSum | expected  |
    | 1, 2, 3            | 6         | found     |
    | -1, -2, -3         | -3        | found     |
    | 5, 10, 15          | 7         | not found |
    | -5, 10, -3, 8      | 2         | found     |
    | 100, 200, 300      | 50        | not found |
  Then all table results match expectations
```

---

#### Feature 3: `json_io.feature` — JSON ввод/вывод (4 сценария)

**Пользовательская история:** As an application user, I want to load input data from a JSON file and save results, so that I can automate batch processing.

**Демонстрируемые элементы:** DocString (многострочный JSON), полный цикл read → solve → write.

```gherkin
Scenario: Full cycle — read, solve, write
  Given a JSON file exists with content:
    """
    { "numbers": [10, 20, 30, 40, 50], "targetSum": 60 }
    """
  When the file is read via JsonFileReader
  And the data is solved with "ArrayDP" algorithm
  And the result is written via JsonFileWriter
  Then the JSON string contains "found"
```

---

#### Feature 4: `console_menu.feature` — Консольное меню (4 сценария, @business-idea)

**Пользовательская история (бизнес-идея):** As a student studying algorithms, I want a convenient interface for entering data and viewing results, so that I can visually compare two algorithms for solving an NP-complete problem.

**Демонстрируемые элементы:** тег `@business-idea`, императивный и декларативный стили.

```gherkin
# Императивный стиль — подробные пошаговые действия пользователя
Scenario: Manual data input via console (imperative style)
  When the user selects menu option "1"
  And the user enters numbers "5 10 15 20"
  And the user enters target sum "25"
  And the user declines to save the result
  Then the output contains "Результат"
  And the output contains "ArrayDP"
  And the output contains "HashMapDP"
  And the output does not contain "Ошибка"

# Декларативный стиль — описание результата без деталей взаимодействия
Scenario: Solving a problem with both algorithm results displayed (declarative style)
  When the user solves a problem with numbers "5 10 15 20" and sum "25"
  Then both algorithm results are displayed
  And both algorithms find a solution
```

---

#### Feature 5: `solver_comparison.feature` — Сравнение алгоритмов (12 сценариев)

**Пользовательская история:** As an algorithm researcher, I want to compare execution time of ArrayDP and HashMapDP on various datasets, so that I can identify performance dependencies on input size and characteristics.

**Демонстрируемые элементы:** Rule (×2), сравнение двух алгоритмов на наборах данных 10–1000 элементов, замер времени и памяти. Проверки found/not-found аналогичны unit-тесту `SolverComparisonTest`.

```gherkin
Rule: Both algorithms produce the same result on non-negative data

  Scenario: Results match on a small dataset
    Given a generated dataset "SMALL_POS_10" of 10 non-negative numbers in range [1, 50]
    When both algorithms solve the problem
    Then both algorithms agree on found or not-found result

Rule: Load testing — time and memory profiling

  Scenario: Load test on a large dense dataset
    Given a generated dataset "LOAD_DENSE_1000" of 1000 non-negative numbers in range [1, 100]
    When both algorithms solve the problem
    Then time and memory of both algorithms are measured
```

---

#### Feature 6: `solver_heavy_load.feature` — Тяжёлое нагрузочное тестирование (@heavy)

Выделен в отдельный Maven-профиль (тег `@heavy`, запуск через `mvn test -Pheavy` с `-Xmx8g`).

Содержит три группы сценариев (Rule):
1. **Предварительный эксперимент** (9 Scenario Outline): наборы 500–5000 элементов, два типа (dense/sparse) — для нахождения границы t1.
2. **Полный план 2×2** (4 Scenario): 2 типа данных × 2 размера (medium + maximum) с детальным профилированием всех этапов пайплайна. Для максимальных наборов используется OOM-защита (ArrayDP создаёт таблицу `boolean[n][target]`, которая при n=60000, target=1.5M занимает ~91 ГБ — попытка аллокации перехватывается через `catch (OutOfMemoryError)`).
3. **Сравнение BDD vs Unit** (1 Scenario): тот же набор данных пропускается через BDD и сравнивается с результатами `SolverComparisonTest`.

---

## Описание экспериментов

### Условия проведения эксперимента

| Параметр | Значение |
|---|---|
| ОС | macOS (Darwin 25.2.0) |
| Java Runtime | OpenJDK 23 |
| Java Source Level | 17 |
| Сборка | Maven 3.x |
| Тестовый фреймворк | JUnit 5.10.2 + Cucumber 7.15.0 |
| Процессор | Apple Silicon |
| RAM | 8+ GB |
| JVM Heap (обычные тесты) | По умолчанию (~256 MB) |
| JVM Heap (тяжёлые тесты) | `-Xmx8g` |

### Алгоритм генерации наборов данных

Генерация выполняется в классе `ComparisonSteps.aGeneratedDataset()`:

```java
Random random = new Random(42); // фиксированный seed для воспроизводимости
List<Integer> numbers = new ArrayList<>();
for (int i = 0; i < size; i++) {
    numbers.add(min + random.nextInt(max - min + 1));
}
int totalSum = numbers.stream().mapToInt(Integer::intValue).sum();
int targetSum = totalSum / 2; // половина общей суммы — наихудший случай для DP
```

Параметры генерации:
- **size** — количество элементов в наборе;
- **[min, max]** — диапазон значений элементов. Определяет плотность данных:
  - **плотные** (dense): [1, 100] — малый диапазон, много коллизий среди достижимых сумм, targetSum относительно мал;
  - **разреженные** (sparse): [1, 500–1000] — широкий диапазон, много уникальных достижимых сумм, targetSum велик;
- **seed = 42** — фиксированное начальное состояние генератора для воспроизводимости;
- **targetSum = totalSum / 2** — половина общей суммы обеспечивает наихудший случай для DP: максимальное число промежуточных сумм для проверки.

Плотность данных — ключевая характеристика, влияющая на производительность обоих алгоритмов: при плотных данных targetSum растёт как O(n × 50), при разреженных — как O(n × 250..500), что напрямую определяет размер DP-таблицы ArrayDP и число итераций HashMapDP.

### Кодировка названий наборов данных

Формат имени: `<ЭТАП>_<ПЛОТНОСТЬ>_<РАЗМЕР>`.

| Компонент | Значения | Описание |
|---|---|---|
| Этап | `PRELIM`, `DENSE`, `SPARSE`, `SCALE`, `LOAD` | Фаза эксперимента |
| Плотность | `DENSE` (max=100), `SPARSE` (max=500–1000) | Тип данных |
| Размер | число (500, 1000, ..., 60000) | Количество элементов |

Полный перечень наборов:

| Код набора | n | Диапазон | Назначение |
|---|---|---|---|
| `PRELIM_DENSE_500..5000` | 500–5000 | [1, 100] | Предварительный эксперимент, плотные |
| `PRELIM_SPARSE_500..3000` | 500–3000 | [1, 1000] | Предварительный эксперимент, разреженные |
| `DENSE_MED_1000` | 1000 | [1, 100] | Плотные, средний размер |
| `DENSE_MAX_60000` | 60000 | [1, 100] | Плотные, максимальный (t1 ≈ 2.5 мин) |
| `SPARSE_MED_1000` | 1000 | [1, 1000] | Разреженные, средний размер |
| `SPARSE_MAX_25000` | 25000 | [1, 500] | Разреженные, максимальный (t1 ≈ 2.3 мин) |

Все датасеты сохраняются в `target/datasets/` в формате JSON (например, `DENSE_MED_1000.json`).

### Предварительный эксперимент

Цель: подобрать размер данных n, при котором время работы хотя бы одного из алгоритмов составляет t1 ∈ [2–10] минут.

**Результаты предварительного эксперимента:**

| Набор | n | Target | ArrayDP (мс) | HashMapDP (мс) | Ratio A/H |
|---|---|---|---|---|---|
| PRELIM_DENSE_500 | 500 | 12 786 | 18.72 | 43.16 | 0.43x |
| PRELIM_DENSE_1000 | 1 000 | 25 560 | 43.08 | 76.17 | 0.57x |
| PRELIM_DENSE_2000 | 2 000 | 51 284 | 142.30 | 228.70 | 0.62x |
| PRELIM_DENSE_3000 | 3 000 | 77 587 | 282.88 | 451.94 | 0.63x |
| PRELIM_DENSE_5000 | 5 000 | 128 690 | 798.83 | 1 196.06 | 0.67x |
| PRELIM_SPARSE_500 | 500 | 126 086 | 42.73 | 144.82 | 0.30x |
| PRELIM_SPARSE_1000 | 1 000 | 253 310 | 160.03 | 444.61 | 0.36x |
| PRELIM_SPARSE_2000 | 2 000 | 501 934 | 675.08 | 1 653.29 | 0.41x |
| PRELIM_SPARSE_3000 | 3 000 | 748 387 | 1 980.39 | 3 753.85 | 0.53x |

**Экстраполяция для нахождения t1:**

Анализируя рост HashMapDP (как наиболее медленного алгоритма):
- Dense: при n=5000 → 1.2 с. Рост близок к O(n²). Для 120 с → n ≈ 5000 × √(120/1.2) = 50 000. С запасом: **n = 60 000**.
- Sparse: при n=3000 → 3.75 с. Рост ~O(n^1.8). Для 120 с → n ≈ 3000 × (120/3.75)^(1/1.8) = 21 000. С запасом: **n = 25 000**.
- ArrayDP: упирается в OOM раньше, чем в время. При n=3000, target=748K таблица `boolean[3001][748388]` занимает 2.5 ГБ. При n=5000 sparse — 3.3 ГБ. При n > 10 000 — не помещается в 8 ГБ.

**Определение максимальных границ производительности:**

| Алгоритм | Тип данных | Граница | Причина |
|---|---|---|---|
| ArrayDP | Dense | n ≈ 10 000 | OOM: таблица boolean[n][target] > 8 ГБ |
| ArrayDP | Sparse | n ≈ 5 000 | OOM: таблица boolean[n][target] > 8 ГБ |
| HashMapDP | Dense | n ≈ 60 000 при t1 ≈ 2.5 мин | Время вычисления |
| HashMapDP | Sparse | n ≈ 25 000 при t1 ≈ 2.3 мин | Время вычисления |

### Профилирование

Профилирование выполняется на 4 этапах пайплайна обработки:

```
[1] Генерация данных → [2] Сохранение в JSON → [3] Загрузка из JSON → [4] Решение задачи
```

Время каждого этапа замеряется через `System.nanoTime()`, память — через `Runtime.totalMemory() - Runtime.freeMemory()` (с вызовом `System.gc()` перед замером).

**Результаты полного плана 2×2:**

| Набор | n | Target | Save (мс) | Load (мс) | ArrayDP (мс) | HashMapDP (мс) |
|---|---|---|---|---|---|---|
| DENSE_MED_1000 | 1 000 | 25 560 | 0.15 | 1.05 | 19 | 48 |
| **DENSE_MAX_60000** | **60 000** | **1 520 174** | **0.22** | **—** | **OOM** | **153 372 (2.56 мин)** |
| SPARSE_MED_1000 | 1 000 | 253 310 | 0.15 | 0.65 | 155 | 437 |
| **SPARSE_MAX_25000** | **25 000** | **3 124 645** | **—** | **—** | **OOM** | **139 083 (2.32 мин)** |

Наблюдения:
- **Генерация и I/O** пренебрежимо малы (< 1 мс даже для 60 000 элементов);
- **Решение задачи** доминирует в пайплайне (> 99.9% времени);
- **ArrayDP** не может обработать максимальные наборы: таблица `boolean[60001][1520175]` потребовала бы 91 ГБ, `boolean[25001][3124646]` — 78 ГБ;
- **HashMapDP** работает при любых размерах, но в 2–3× медленнее ArrayDP на тех же данных (когда ArrayDP помещается в память).

---

## Анализ результатов экспериментов

### Зависимость времени от размера данных

**ArrayDP — O(n × targetSum):**

| n | Target | Время (мс) | n × target | Throughput (M ops/s) |
|---|---|---|---|---|
| 500 | 12 786 | 18.72 | 6.4M | 342 |
| 1 000 | 25 560 | 43.08 | 25.6M | 594 |
| 2 000 | 51 284 | 142.30 | 102.6M | 721 |
| 3 000 | 77 587 | 282.88 | 232.8M | 823 |
| 5 000 | 128 690 | 798.83 | 643.5M | 806 |

Пропускная способность стабилизируется на уровне ~800 M ops/s, что подтверждает линейную зависимость O(n × target). Начальный рост throughput объясняется JIT-прогревом.

**HashMapDP — O(n × |reachable sums|):**

| n | Dense (мс) | Sparse (мс) | Dense/Sparse |
|---|---|---|---|
| 500 | 43 | 145 | 3.4× |
| 1 000 | 76 | 445 | 5.9× |
| 2 000 | 229 | 1 653 | 7.2× |
| 3 000 | 452 | 3 754 | 8.3× |
| 5 000 | 1 196 | — | — |

Sparse-данные обрабатываются в 3–8× медленнее dense-данных на одном и том же n, так как широкий диапазон значений [1, 1000] порождает гораздо больше уникальных достижимых сумм, чем узкий [1, 100].

### График зависимости времени от n

```
Время (мс, log)
  10000 ┤
        │                                          S3000: 3754
        │
   1000 ┤                              D5000: 1196
        │                     S2000: 1653
        │              D3000: 452
    100 ┤       S1000: 445    D2000: 229
        │D1000: 76
        │S500: 145    D500: 43
     10 ┤D500: 19
        │
      1 ┼──────┬──────┬──────┬──────┬──────
        0    1000   2000   3000   4000   5000   n

  D = ArrayDP Dense    S = HashMapDP Sparse
```

Наблюдается сверхлинейный рост для HashMapDP: ~O(n^1.6) для dense, ~O(n^1.8) для sparse. ArrayDP растёт линейно по n при фиксированном средне-значении диапазона (target ∝ n).

### Сравнение распределений времени ArrayDP и HashMapDP

Для оценки сходства двух алгоритмов по времени выполнения рассмотрим отношение ArrayDP/HashMapDP на одних и тех же данных:

| n | Dense: A/H | Sparse: A/H |
|---|---|---|
| 500 | 0.43 | 0.30 |
| 1 000 | 0.57 | 0.36 |
| 2 000 | 0.62 | 0.41 |
| 3 000 | 0.63 | 0.53 |
| 5 000 | 0.67 | — |

ArrayDP стабильно быстрее в 1.5–3.3× (ratio 0.30–0.67). Отношение **монотонно растёт** с увеличением n — при малых n разница в пользу ArrayDP максимальна (hash-таблица медленнее на мелких данных из-за fixed overhead), при больших n разница сокращается (ArrayDP начинает тратить время на аллокацию и заполнение гигантской таблицы).

Для sparse-данных разрыв больше (ratio 0.30–0.53), чем для dense (0.43–0.67), поскольку ArrayDP обращается к массиву последовательно (cache-friendly), а HashMapDP при большом числе уникальных сумм испытывает давление на кеш и GC.

### Сравнение ArrayDP и HashMapDP (сводная таблица)

| Критерий | ArrayDP | HashMapDP |
|---|---|---|
| Временная сложность | O(n × target) | O(n × \|reachable\|) |
| Пространственная сложность | O(n × target) | O(\|reachable\|) |
| Поддержка отрицательных чисел | Нет | Да |
| Скорость (при одинаковых данных) | Быстрее в 1.5–3.3× | Медленнее |
| Максимальный n при 8 ГБ (sparse) | ~5 000 (OOM) | 25 000+ |
| Максимальный n при 8 ГБ (dense) | ~10 000 (OOM) | 60 000+ |
| Предсказуемость времени | Детерминированная | Зависит от распределения данных |

---

## Заключение

### Соответствие теоретической оценки алгоритмов экспериментам

- **ArrayDP: O(n × targetSum)** — полностью подтверждено. Throughput стабилен на ~800 M ops/s. Узкое место — не время, а **память**: таблица `boolean[n+1][target+1]` занимает n×target байт (в Java `boolean` = 1 байт). При n=3000, target=748K это 2.5 ГБ; при n=60000, target=1.5M — 91 ГБ.

- **HashMapDP: O(n × |reachable sums|)** — подтверждено. Наблюдаемый рост (~n^1.6–1.8) объясняется тем, что |reachable sums| растёт вместе с n. На максимальных наборах (n=60000 dense, n=25000 sparse) время составило 2.3–2.6 минуты, что попадает в требуемый диапазон t1 ∈ [2–10] мин.

### Выводы из сравнения двух распределений

Отношение времени ArrayDP/HashMapDP (0.30–0.67) показывает, что два алгоритма **не эквивалентны по производительности** несмотря на одинаковый асимптотический класс для плотных данных. Причины:
- ArrayDP использует прямую индексацию `boolean[][]` — O(1) с последовательным доступом к памяти (cache-friendly);
- HashMapDP использует `HashMap.get()`/`HashMap.put()` — O(1) амортизированно, но с существенным constant factor: автобоксинг `int → Integer`, вычисление хеша, работа с цепочками коллизий, давление на GC при создании миллионов объектов `Integer` и `Map.Entry`.

При этом HashMapDP компенсирует проигрыш в скорости **экономией памяти** (хранит только достижимые суммы) и **универсальностью** (поддержка отрицательных чисел, отсутствие OOM на больших данных).

### Сравнение BDD и модульных тестов

| Критерий | Unit-тесты (JUnit 5) | BDD-тесты (Cucumber) |
|---|---|---|
| Уровень абстракции | Методы, классы | Поведение, пользовательские сценарии |
| Читаемость | Для разработчиков | Для всех участников проекта |
| Язык описания | Java | Gherkin (естественный язык) |
| Скорость (128 / 70 тестов) | ~10 сек | ~10 сек |
| Гранулярность | Ветвления, граничные условия | Бизнес-потоки, E2E сценарии |
| Документирование | Косвенное (имена методов) | Явное (.feature-файлы) |
| Мокирование | Полное (Mockito: @Mock, @Spy, mock()) | Ограниченное |
| Параметризация | @CsvSource, @MethodSource | Scenario Outline, DataTable |
| Нагрузочное тестирование | Ручная организация | Структурировано через сценарии и теги |
| Быстродействие фреймворка | Минимальный overhead | +2 сек на инициализацию Cucumber |

На одинаковых данных (набор BDD_VS_UNIT_500, n=500) BDD-тест даёт такие же замеры производительности, что и unit-тест `SolverComparisonTest`. Overhead Cucumber — ~2 секунды на старт (парсинг .feature, reflection для маппинга шагов); далее скорость исполнения идентична.

**Итог:** BDD-тесты дополняют, а не заменяют модульные тесты. Unit-тесты обеспечивают глубокую техническую проверку (ветвления, граничные условия, моки), а BDD-тесты — валидацию на уровне бизнес-сценариев, исполняемую документацию и структурированное нагрузочное тестирование.

---

## Источники

1. North D. Introducing BDD. — https://dannorth.net/introducing-bdd/
2. Cucumber Documentation. Gherkin Reference. — https://cucumber.io/docs/gherkin/reference/
3. Chelimsky D. et al. The RSpec Book. — Pragmatic Bookshelf, 2010. — https://github.com/martinmurciego/good-books/blob/master/the-rspec-book.pdf
4. Гмурман В.Е. Теория вероятностей и математическая статистика: Учеб. пособие для вузов. — 9-е изд. — М.: Высшая школа, 2003. — 479 с.
5. Cucumber JVM. — https://github.com/cucumber/cucumber-jvm
6. JUnit 5 User Guide. — https://junit.org/junit5/docs/current/user-guide/

---

## Приложение

### А. Структура BDD-тестов

```
src/test/
├── java/com/subsetsum/bdd/
│   ├── RunCucumberTest.java       — точка входа Cucumber (JUnit Suite)
│   ├── TestContext.java           — общий контекст между step-классами (ThreadLocal)
│   ├── Hooks.java                 — Cucumber hook (сброс контекста после сценария)
│   ├── SolverSteps.java           — шаги для ArrayDP/HashMapDP (Given/When/Then/But)
│   ├── JsonIoSteps.java           — шаги для JSON I/O
│   ├── ConsoleMenuSteps.java      — шаги для консольного меню
│   ├── ComparisonSteps.java       — шаги сравнения + генерация датасетов
│   └── HeavyLoadSteps.java        — шаги нагрузочного тестирования + профилирование
└── resources/com/subsetsum/bdd/
    ├── array_solver.feature       — 11 сценариев ArrayDP
    ├── hashmap_solver.feature     — 8 сценариев HashMapDP
    ├── json_io.feature            — 4 сценария JSON I/O
    ├── console_menu.feature       — 4 сценария UI (@business-idea)
    ├── solver_comparison.feature  — 12 сценариев сравнения
    └── solver_heavy_load.feature  — 14 сценариев нагрузки (@heavy)
```

### Б. Запуск тестов

```bash
# Все тесты (unit + BDD, без @heavy)
mvn test

# Только BDD-тесты (без @heavy)
mvn test -Dtest=RunCucumberTest

# Тяжёлые нагрузочные тесты (@heavy, JVM с -Xmx8g)
mvn test -Pheavy

# Отчёт Cucumber → target/cucumber-reports/cucumber.html
# Отчёт нагрузки → target/datasets/heavy_load_report.txt
```

### В. Результаты запуска

```
# Обычные тесты:
Tests run: 228, Failures: 0, Errors: 0, Skipped: 9
BUILD SUCCESS (19 сек)

# Тяжёлые нагрузочные тесты:
Tests run: 50, Failures: 0, Errors: 0, Skipped: 22
BUILD SUCCESS (10:37 мин)
```

### Г. Код генерации датасетов (ComparisonSteps.java, фрагмент)

```java
@Given("a generated dataset {string} of {int} non-negative numbers in range [{int}, {int}]")
public void aGeneratedDataset(String name, int size, int min, int max) {
    ctx.setDatasetName(name);

    long start = System.nanoTime();
    Random random = new Random(42);
    List<Integer> numbers = new ArrayList<>();
    for (int i = 0; i < size; i++) {
        numbers.add(min + random.nextInt(max - min + 1));
    }
    int totalSum = numbers.stream().mapToInt(Integer::intValue).sum();
    int targetSum = totalSum / 2;
    generationTimeNs = System.nanoTime() - start;

    ctx.setGeneratedInput(new SubsetSumInput(numbers, targetSum));
    saveDataset(name, numbers, targetSum); // → target/datasets/<name>.json
}
```

### Д. Код профилирования (HeavyLoadSteps.java, фрагмент)

```java
@When("the full profiling pipeline runs with OOM protection")
public void theFullProfilingPipelineRunsWithOomProtection() {
    // ... [1] save to JSON, [2] load from JSON ...

    // [3] ArrayDP с защитой от OOM
    System.gc();
    try {
        long start = System.nanoTime();
        ctx.setArrayResult(arraySolver.solve(input));
        arraySolveTimeNs = System.nanoTime() - start;
    } catch (OutOfMemoryError e) {
        arraySkippedOom = true;
        ctx.setArrayResult(null);
        System.gc();
    }

    // [4] HashMapDP
    System.gc();
    long start = System.nanoTime();
    ctx.setHashMapResult(hashMapSolver.solve(input));
    hashMapSolveTimeNs = System.nanoTime() - start;
}
```

### Е. Полная таблица нагрузочного тестирования

| Набор | n | Target | ArrayDP (мс) | HashMapDP (мс) | ArrayDP mem | HashMapDP mem |
|---|---|---|---|---|---|---|
| PRELIM_DENSE_500 | 500 | 12 786 | 18.72 | 43.16 | 6.5 MB | 11.7 MB |
| PRELIM_DENSE_1000 | 1 000 | 25 560 | 43.08 | 76.17 | 25.8 MB | 27.8 MB |
| PRELIM_DENSE_2000 | 2 000 | 51 284 | 142.30 | 228.70 | 103.5 MB | 80.5 MB |
| PRELIM_DENSE_3000 | 3 000 | 77 587 | 282.88 | 451.94 | 234.4 MB | 162.0 MB |
| PRELIM_DENSE_5000 | 5 000 | 128 690 | 798.83 | 1 196.06 | 655.4 MB | 49.1 MB |
| PRELIM_SPARSE_500 | 500 | 126 086 | 42.73 | 144.82 | 63.7 MB | 42.8 MB |
| PRELIM_SPARSE_1000 | 1 000 | 253 310 | 160.03 | 444.61 | 262.0 MB | 220.4 MB |
| PRELIM_SPARSE_2000 | 2 000 | 501 934 | 675.08 | 1 653.29 | 1 049.5 MB | 567.2 MB |
| PRELIM_SPARSE_3000 | 3 000 | 748 387 | 1 980.39 | 3 753.85 | 2 517.5 MB | 662.2 MB |
| DENSE_MED_1000 | 1 000 | 25 560 | 19.04 | 47.81 | 25.9 MB | 83.3 MB |
| **DENSE_MAX_60000** | **60 000** | **1 520 174** | **OOM** | **153 372** | — | 434.2 MB |
| SPARSE_MED_1000 | 1 000 | 253 310 | 154.62 | 436.61 | 262.0 MB | 153.4 MB |
| **SPARSE_MAX_25000** | **25 000** | **3 124 645** | **OOM** | **139 083** | — | 610.2 MB |

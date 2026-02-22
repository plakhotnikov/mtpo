package com.subsetsum.algorithm;

import com.subsetsum.model.SubsetSumInput;
import com.subsetsum.model.SubsetSumResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Сравнительные тесты двух алгоритмов.
 * Оба солвера должны давать одинаковый ответ (found/not found)
 * и корректную сумму подмножества на одних и тех же данных.
 */
@DisplayName("Сравнение ArrayDPSolver и HashMapDPSolver")
class SolverComparisonTest {

    private ArrayDPSolver arraySolver;
    private HashMapDPSolver hashMapSolver;

    @BeforeEach
    void setUp() {
        arraySolver = new ArrayDPSolver();
        hashMapSolver = new HashMapDPSolver();
    }

    static Stream<Arguments> comparisonCases() {
        return Stream.of(
                Arguments.of(List.of(3, 7, 1, 8, 5), 11),
                Arguments.of(List.of(1, 2, 3, 4, 5), 15),
                Arguments.of(List.of(1, 2, 3, 4, 5), 12),
                Arguments.of(List.of(10, 20, 30), 50),
                Arguments.of(List.of(10, 20, 30), 25),
                Arguments.of(List.of(1), 1),
                Arguments.of(List.of(1), 2),
                Arguments.of(List.of(5, 5, 5, 5), 10),
                Arguments.of(List.of(100, 200, 300, 400, 500), 700),
                Arguments.of(List.of(2, 4, 6, 8, 10), 0),
                Arguments.of(List.of(1, 1, 1, 1, 1, 1, 1, 1, 1, 1), 7)
        );
    }

    @ParameterizedTest(name = "numbers={0}, target={1}")
    @MethodSource("comparisonCases")
    @DisplayName("Оба алгоритма дают одинаковый результат found/not-found")
    void bothSolversAgreeOnResult(List<Integer> numbers, int target) {
        SubsetSumInput input = new SubsetSumInput(numbers, target);

        SubsetSumResult arrayResult = arraySolver.solve(input);
        SubsetSumResult hashResult = hashMapSolver.solve(input);

        assertEquals(arrayResult.isFound(), hashResult.isFound(),
                "Оба алгоритма должны согласиться: найдено или нет");

        if (arrayResult.isFound()) {
            assertThat("ArrayDP: сумма подмножества должна совпадать с target",
                    arrayResult.getSubsetSum(), equalTo(target));
            assertThat("HashMapDP: сумма подмножества должна совпадать с target",
                    hashResult.getSubsetSum(), equalTo(target));
        }
    }
}

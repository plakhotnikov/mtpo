package com.subsetsum.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("SubsetSumResult Tests")
class SubsetSumResultTest {

    @Test
    @DisplayName("Конструктор с параметрами")
    void constructorWithParams() {
        SubsetSumResult result = new SubsetSumResult(true, List.of(3, 8), "TestAlgo");

        assertTrue(result.isFound());
        assertThat(result.getSubset(), contains(3, 8));
        assertEquals("TestAlgo", result.getAlgorithmName());
    }

    @Test
    @DisplayName("Конструктор по умолчанию")
    void defaultConstructor() {
        SubsetSumResult result = new SubsetSumResult();

        assertFalse(result.isFound());
        assertThat(result.getSubset(), is(empty()));
    }

    @Test
    @DisplayName("getSubsetSum вычисляет сумму")
    void getSubsetSum() {
        SubsetSumResult result = new SubsetSumResult(true, List.of(3, 7, 1), "A");
        assertEquals(11, result.getSubsetSum());
    }

    @Test
    @DisplayName("getSubsetSum для пустого подмножества = 0")
    void getSubsetSum_empty() {
        SubsetSumResult result = new SubsetSumResult(true, List.of(), "A");
        assertEquals(0, result.getSubsetSum());
    }

    @Test
    @DisplayName("null subset → пустой список")
    void nullSubset_becomesEmpty() {
        SubsetSumResult result = new SubsetSumResult(true, null, "A");
        assertNotNull(result.getSubset());
        assertThat(result.getSubset(), is(empty()));
    }

    @Test
    @DisplayName("setSubset(null) → пустой список")
    void setSubsetNull() {
        SubsetSumResult result = new SubsetSumResult();
        result.setSubset(null);
        assertThat(result.getSubset(), is(empty()));
    }

    @Test
    @DisplayName("Время выполнения: ns → ms конвертация")
    void executionTimeConversion() {
        SubsetSumResult result = new SubsetSumResult();
        result.setExecutionTimeNs(5_000_000L);
        assertEquals(5.0, result.getExecutionTimeMs(), 0.001);
    }

    @Test
    @DisplayName("Setters/Getters")
    void settersAndGetters() {
        SubsetSumResult result = new SubsetSumResult();
        result.setFound(true);
        result.setSubset(List.of(1, 2));
        result.setAlgorithmName("Algo");
        result.setExecutionTimeNs(100);
        result.setMemoryUsedBytes(512);

        assertTrue(result.isFound());
        assertThat(result.getSubset(), hasSize(2));
        assertEquals("Algo", result.getAlgorithmName());
        assertEquals(100, result.getExecutionTimeNs());
        assertEquals(512, result.getMemoryUsedBytes());
    }

    @Test
    @DisplayName("getSubset возвращает неизменяемый список")
    void getSubsetUnmodifiable() {
        SubsetSumResult result = new SubsetSumResult(true, List.of(1, 2), "A");
        assertThrows(UnsupportedOperationException.class, () -> result.getSubset().add(3));
    }

    @Test
    @DisplayName("equals и hashCode")
    void equalsAndHashCode() {
        SubsetSumResult a = new SubsetSumResult(true, List.of(3), "A");
        SubsetSumResult b = new SubsetSumResult(true, List.of(3), "B");
        SubsetSumResult c = new SubsetSumResult(false, List.of(), "A");

        assertEquals(a, b); // equals не учитывает algorithmName
        assertNotEquals(a, c);
    }

    @Test
    @DisplayName("toString содержит ключевую информацию")
    void toStringTest() {
        SubsetSumResult result = new SubsetSumResult(true, List.of(5), "AlgoX");
        result.setExecutionTimeNs(1_000_000);
        String s = result.toString();

        assertThat(s, containsString("true"));
        assertThat(s, containsString("AlgoX"));
    }
}

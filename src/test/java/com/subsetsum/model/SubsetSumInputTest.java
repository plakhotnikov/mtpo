package com.subsetsum.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("SubsetSumInput Tests")
class SubsetSumInputTest {

    @Test
    @DisplayName("Конструктор с параметрами")
    void constructorWithParams() {
        SubsetSumInput input = new SubsetSumInput(List.of(1, 2, 3), 6);

        assertThat(input.getNumbers(), contains(1, 2, 3));
        assertEquals(6, input.getTargetSum());
        assertEquals(3, input.size());
    }

    @Test
    @DisplayName("Конструктор по умолчанию")
    void defaultConstructor() {
        SubsetSumInput input = new SubsetSumInput();

        assertThat(input.getNumbers(), is(empty()));
        assertEquals(0, input.getTargetSum());
    }

    @Test
    @DisplayName("null numbers → NullPointerException")
    void nullNumbers_throws() {
        assertThrows(NullPointerException.class, () -> new SubsetSumInput(null, 5));
    }

    @Test
    @DisplayName("setNumbers / setTargetSum")
    void setters() {
        SubsetSumInput input = new SubsetSumInput();
        input.setNumbers(List.of(10, 20));
        input.setTargetSum(30);

        assertThat(input.getNumbers(), contains(10, 20));
        assertEquals(30, input.getTargetSum());
    }

    @Test
    @DisplayName("setNumbers(null) → NullPointerException")
    void setNullNumbers_throws() {
        SubsetSumInput input = new SubsetSumInput();
        assertThrows(NullPointerException.class, () -> input.setNumbers(null));
    }

    @Test
    @DisplayName("getNumbers возвращает неизменяемый список")
    void getNumbersIsUnmodifiable() {
        SubsetSumInput input = new SubsetSumInput(List.of(1, 2, 3), 6);
        assertThrows(UnsupportedOperationException.class, () -> input.getNumbers().add(4));
    }

    @Test
    @DisplayName("Защитное копирование: изменение оригинального списка не влияет")
    void defensiveCopy() {
        List<Integer> original = new ArrayList<>(List.of(1, 2, 3));
        SubsetSumInput input = new SubsetSumInput(original, 6);
        original.add(999);

        assertThat(input.getNumbers(), hasSize(3));
        assertThat(input.getNumbers(), not(hasItem(999)));
    }

    @Test
    @DisplayName("validate: пустой список → исключение")
    void validateEmpty_throws() {
        SubsetSumInput input = new SubsetSumInput(Collections.emptyList(), 5);
        assertThrows(IllegalArgumentException.class, input::validate);
    }

    @Test
    @DisplayName("validate: непустой список → OK")
    void validateNonEmpty_ok() {
        SubsetSumInput input = new SubsetSumInput(List.of(1), 1);
        assertDoesNotThrow(input::validate);
    }

    @Test
    @DisplayName("equals и hashCode")
    void equalsAndHashCode() {
        SubsetSumInput a = new SubsetSumInput(List.of(1, 2, 3), 6);
        SubsetSumInput b = new SubsetSumInput(List.of(1, 2, 3), 6);
        SubsetSumInput c = new SubsetSumInput(List.of(1, 2, 3), 7);

        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
        assertNotEquals(a, c);
    }

    @Test
    @DisplayName("toString содержит данные")
    void toStringContainsData() {
        SubsetSumInput input = new SubsetSumInput(List.of(1, 2), 3);
        String s = input.toString();
        assertThat(s, containsString("1"));
        assertThat(s, containsString("2"));
        assertThat(s, containsString("3"));
    }
}

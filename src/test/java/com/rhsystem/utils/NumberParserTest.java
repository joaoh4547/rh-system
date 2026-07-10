package com.rhsystem.utils;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.math.BigInteger;

import static org.junit.jupiter.api.Assertions.*;

class NumberParserTest {

    @ParameterizedTest
    @ValueSource(strings = {"abc"})
    void shouldReturnNUllIfValueIsNotNumber(String value) {
        assertNull(NumberParser.parse(value));
    }

    @Test
    void shouldReturnNullIfValueIsNull() {
        assertNull(NumberParser.parse(null));
    }

    @Test
    void shouldReturnNullIfValueIsEmpty() {
        assertNull(NumberParser.parse(""));
    }

    @Test
    void shouldReturnNullIfValueIsBlank() {
        assertNull(NumberParser.parse(" "));
    }

    @ParameterizedTest
    @ValueSource(strings = {"15",  Integer.MAX_VALUE+""})
    void shouldCreateValueAsInteger(String value){
        var number = NumberParser.parse(value);
        assertNotNull(number);
        assertInstanceOf(Integer.class, number);
    }

    @ParameterizedTest
    @ValueSource(strings = {"2147483648",  Long.MAX_VALUE+""})
    void shouldCreateValueAsLong(String value){
        var number = NumberParser.parse(value);
        assertNotNull(number);
        assertInstanceOf(Long.class, number);
    }

    @ParameterizedTest
    @ValueSource(strings = "1.50")
    void shouldCreteValueAsBigDecimal(String value){
        var number = NumberParser.parse(value);
        assertNotNull(number);
        assertInstanceOf(BigDecimal.class, number);
    }

    @ParameterizedTest
    @ValueSource(strings = "12345678901234567890")
    void shouldCreteValueAsBigInteger(String value){
        var number = NumberParser.parse(value);
        assertNotNull(number);
        assertInstanceOf(BigInteger.class, number);
    }
}
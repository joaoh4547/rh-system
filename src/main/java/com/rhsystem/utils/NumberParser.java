package com.rhsystem.utils;

import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.math.BigInteger;

import static org.apache.commons.lang3.math.NumberUtils.isCreatable;

import static org.apache.commons.lang3.StringUtils.*;

@Slf4j
public class NumberParser {

    public static Number parse(String value) {
        if (isBlank(value)) {
            return null;
        }
        if (!isCreatable(value)) {
            log.warn("Value {} is not a number", value);
            return null;
        }

        var normalized = value.trim();

        if (normalized.contains(".") || normalized.contains(",")) {
            normalized = normalized.replace(",", ".");
            return new BigDecimal(normalized);
        }

        try {
            return Integer.parseInt(normalized);
        } catch (NumberFormatException _) {
        }

        try {
            return Long.parseLong(normalized);
        } catch (NumberFormatException _) {
        }

        return new BigInteger(normalized);
    }

}

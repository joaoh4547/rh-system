package com.rhsystem.domain.model;

/**
 * Represents sorting criteria used for ordering a dataset. The criteria
 * consist of a field to sort by and the direction of sorting (ascending or descending).
 * This record encapsulates these two attributes and provides an immutable structure
 * for sorting configurations.
 *
 * @param field     The name of the field to sort by.
 * @param direction The direction of sorting, which is either ascending (ASC) or descending (DESC).
 */
public record Sorting(String field, Direction direction) {


    /**
     * Represents the direction of sorting for a dataset. Sorting can be performed
     * in either ascending order (ASC) or descending order (DESC).
     * <p>
     * ASC: Indicates that sorting is performed in ascending order, where
     * smaller or earlier values are ordered before larger or later values.
     * DESC: Indicates that sorting is performed in descending order, where
     * larger or later values are ordered before smaller or earlier values.
     * <p>
     * This enumeration is commonly used in sorting configurations to define
     * the desired order of the dataset elements.
     */
    public enum Direction {
        ASC, DESC
    }

}

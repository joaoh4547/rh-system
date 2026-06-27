package com.rhsystem.domain.model;


public record Sorting(String field, Direction direction) {


    public enum Direction {
        ASC, DESC
    }

}

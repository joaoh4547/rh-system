package com.rhsystem.infrastructure.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import com.rhsystem.domain.model.Sorting;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Sort;

class JpaSortUtilTest {

    private static final Sort FALLBACK = Sort.by("firstName").ascending();

    @Test
    void nullCollectionReturnsFallback() {
        assertSame(FALLBACK, JpaSortUtil.createSort(null, FALLBACK));
    }

    @Test
    void emptyCollectionReturnsFallback() {
        assertSame(FALLBACK, JpaSortUtil.createSort(List.of(), FALLBACK));
    }

    @Test
    void mapsAscAndDescDirections() {
        Sort sort = JpaSortUtil.createSort(List.of(
                new Sorting("firstName", Sorting.Direction.ASC),
                new Sorting("email", Sorting.Direction.DESC)), FALLBACK);

        List<Sort.Order> orders = sort.toList();
        assertEquals(2, orders.size());
        assertEquals("firstName", orders.get(0).getProperty());
        assertEquals(Sort.Direction.ASC, orders.get(0).getDirection());
        assertEquals("email", orders.get(1).getProperty());
        assertEquals(Sort.Direction.DESC, orders.get(1).getDirection());
    }

    @Test
    void skipsEntriesWithNullOrBlankField() {
        Sort sort = JpaSortUtil.createSort(Arrays.asList(
                new Sorting(null, Sorting.Direction.ASC),
                new Sorting("  ", Sorting.Direction.ASC),
                new Sorting("name", Sorting.Direction.ASC)), FALLBACK);

        assertEquals(1, sort.toList().size());
        assertEquals("name", sort.toList().getFirst().getProperty());
    }

    @Test
    void onlyInvalidEntriesFallBack() {
        Sort sort = JpaSortUtil.createSort(Arrays.asList(
                new Sorting(null, Sorting.Direction.ASC),
                new Sorting("", Sorting.Direction.DESC)), FALLBACK);

        assertSame(FALLBACK, sort);
    }
}

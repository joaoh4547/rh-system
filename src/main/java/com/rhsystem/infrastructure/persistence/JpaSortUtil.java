package com.rhsystem.infrastructure.persistence;

import com.rhsystem.domain.model.Sorting;
import org.springframework.data.domain.Sort;
import org.springframework.util.CollectionUtils;

import java.util.*;

/**
 * Utility class for creating Spring Data JPA {@link Sort}
 * instances based on domain-specific sorting requirements.
 *
 * <h2>Overview</h2>
 * This class provides methods to dynamically construct a {@link Sort}
 * object based on a collection of sorting specifications. It transforms the provided custom
 * sorting information into a format that can be directly utilized by JPA repositories
 * for database queries.
 *
 * <h2>Key Features</h2>
 * - Handles null or empty sorting collections gracefully, returning a fallback sort.
 * - Filters out invalid sorting criteria, such as null or blank field names.
 * - Supports both ascending and descending sort directions.
 *
 * <h2>Design Considerations</h2>
 * - This class is designed as a utility with a private constructor to prevent instantiation.
 * - Directly interfaces with Spring Data JPA's {@link Sort}.
 */
public class JpaSortUtil {

    /**
     * Private constructor to prevent instantiation of the {@code JpaSortUtil} utility class.
     * <p>
     * This utility class provides static methods for creating JPA {@link Sort} instances
     * based on custom sorting specifications and is not meant to be instantiated.
     */
    private JpaSortUtil() {
    }

    /**
     * Creates a {@code Sort} instance from a collection of custom sorting specifications.
     * If the provided collection is null or empty, the specified fallback sort is returned.
     * Filters out invalid sorting criteria, such as null or blank field names.
     *
     * @param sorting  A collection of custom {@code Sorting} specifications used to define
     *                 the sorting logic. If null or empty, the fallback sort is used.
     * @param fallback A default {@code Sort} instance to be used when the sorting collection
     *                 is null, empty, or contains only invalid entries.
     * @return A {@code Sort} instance based on the specified sorting collection
     * or the fallback sort if the collection is null, empty, or entirely invalid.
     */
    public static Sort createSort(Collection<Sorting> sorting, Sort fallback) {
        if (CollectionUtils.isEmpty(sorting)) {
            return fallback;
        }

        List<Sort.Order> orders = new ArrayList<>(sorting.size());
        for (Sorting s : sorting) {
            if (s.field() == null || s.field().isBlank()) {
                continue;
            }

            orders.add(new Sort.Order(
                    s.direction() == Sorting.Direction.ASC ? Sort.Direction.ASC : Sort.Direction.DESC,
                    s.field()
            ));
        }

        return orders.isEmpty() ? fallback : Sort.by(orders);
    }
}

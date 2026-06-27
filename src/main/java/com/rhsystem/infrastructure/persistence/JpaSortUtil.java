package com.rhsystem.infrastructure.persistence;

import com.rhsystem.domain.model.Sorting;
import org.springframework.data.domain.Sort;
import org.springframework.util.CollectionUtils;

import java.util.*;

public class JpaSortUtil {

    private JpaSortUtil() {
    }

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

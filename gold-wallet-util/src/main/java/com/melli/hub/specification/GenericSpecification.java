package com.melli.hub.specification;

import com.melli.hub.util.StringUtils;
import org.springframework.data.jpa.domain.Specification;

public class GenericSpecification {

    private GenericSpecification() {
    }

    public static <T> Specification<T> containsValue(String field, String value) {
        return ((root, query, criteriaBuilder) -> {
            if (StringUtils.hasText(field) && StringUtils.hasText(value)) {
                String pattern = "%" + value.toLowerCase() + "%";
                return criteriaBuilder.like(criteriaBuilder.lower(root.get(field)), pattern);
            }
            return criteriaBuilder.conjunction();
        });
    }

    public static <T> Specification<T> isNull(String field) {
        return ((root, query, criteriaBuilder) -> {
            if (StringUtils.hasText(field)) {
                return criteriaBuilder.isNull(root.get(field));
            }
            return criteriaBuilder.conjunction();
        });
    }
}

package com.experience_program.be.customer.domain;

import lombok.Builder;
import lombok.Getter;



import java.util.LinkedHashMap;
import java.util.Map;

@Getter
@Builder
public class CustomerRow {

    private String customerId;

    // 컬럼명 → 값 (CSV 원본 + target_segment)
    @Builder.Default
    private Map<String, String> attributes = new LinkedHashMap<>();

    public void put(String column, String value) {
        attributes.put(column, value);
    }

    public String get(String column) {
        return attributes.get(column);
    }
}

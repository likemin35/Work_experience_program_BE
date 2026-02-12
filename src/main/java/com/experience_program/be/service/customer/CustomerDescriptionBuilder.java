package com.experience_program.be.service.customer;

import com.experience_program.be.customer.domain.CustomerRow;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class CustomerDescriptionBuilder {

    public String build(CustomerRow customer) {

        Map<String, String> attrs = customer.getAttributes();
        List<String> parts = new ArrayList<>();

        // 나이
        Integer age = findInt(attrs, List.of("age", "연령", "나이"));
        if (age != null) {
            if (age >= 40) parts.add("중장년층 고객");
            else if (age >= 30) parts.add("30대 고객");
            else if (age >= 20) parts.add("20대 고객");
        }

        // 요금제
        String plan = find(attrs, List.of("plan", "요금제", "plan_name"));
        if (plan != null) {
            parts.add(plan + " 요금제 이용 고객");
        }

        // 데이터 사용량
        Integer data = findInt(attrs, List.of("monthly_data", "data_usage", "월데이터"));
        if (data != null && data >= 50) {
            parts.add("데이터 사용량이 많은 고객");
        }

        // 가입 기간
        Integer years = findInt(attrs, List.of("가입년수", "subscription_years"));
        if (years != null && years >= 3) {
            parts.add("장기 이용 고객");
        }

        // fallback
        if (parts.isEmpty()) {
            return "일반적인 통신 서비스 이용 고객";
        }

        return String.join(", ", parts);
    }

    private String find(Map<String, String> attrs, List<String> keys) {
        for (String key : keys) {
            if (attrs.containsKey(key) && !attrs.get(key).isBlank()) {
                return attrs.get(key).trim();
            }
        }
        return null;
    }

    private Integer findInt(Map<String, String> attrs, List<String> keys) {
        try {
            String value = find(attrs, keys);
            if (value == null) return null;
            return Integer.parseInt(value);
        } catch (Exception e) {
            return null;
        }
    }
}

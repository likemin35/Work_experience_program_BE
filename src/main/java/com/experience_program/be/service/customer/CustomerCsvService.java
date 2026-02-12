package com.experience_program.be.service.customer;

import com.experience_program.be.customer.domain.CustomerRow;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
public class CustomerCsvService {

    /**
     * CSV 업로드 → CustomerRow 리스트
     */
    public List<CustomerRow> parse(MultipartFile file) {

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("CSV 파일이 비어 있습니다.");
        }

        try (
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8)
                );
                CSVParser csvParser = CSVFormat.DEFAULT
                        .withFirstRecordAsHeader()
                        .withTrim()
                        .parse(reader)
        ) {
            List<CustomerRow> rows = new ArrayList<>();
            List<String> headers = new ArrayList<>(csvParser.getHeaderMap().keySet());

            int rowIndex = 0;

            for (CSVRecord record : csvParser) {
                Map<String, String> attributes = new LinkedHashMap<>();

                for (String header : headers) {
                    attributes.put(header, record.get(header));
                }

                String customerId =
                        attributes.getOrDefault("customer_id", String.valueOf(rowIndex));

                rows.add(
                        CustomerRow.builder()
                                .customerId(customerId)
                                .attributes(attributes)
                                .build()
                );

                rowIndex++;
            }

            return rows;

        } catch (IOException e) {
            throw new RuntimeException("CSV 파싱 중 오류 발생", e);
        }
    }

    /**
     * CustomerRow 리스트 → CSV byte[]
     * (기존 컬럼 + target_segment 포함)
     */
    public byte[] write(List<CustomerRow> rows) {

        if (rows == null || rows.isEmpty()) {
            throw new IllegalArgumentException("CSV로 변환할 데이터가 없습니다.");
        }

        try (
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                OutputStreamWriter writer =
                        new OutputStreamWriter(out, StandardCharsets.UTF_8);
                CSVPrinter printer = new CSVPrinter(writer, CSVFormat.DEFAULT)
        ) {
            // 헤더 추출 (첫 row 기준)
            Set<String> headers = rows.get(0).getAttributes().keySet();
            printer.printRecord(headers);

            for (CustomerRow row : rows) {
                List<String> values = new ArrayList<>();
                for (String header : headers) {
                    values.add(row.getAttributes().getOrDefault(header, ""));
                }
                printer.printRecord(values);
            }

            printer.flush();
            return out.toByteArray();

        } catch (IOException e) {
            throw new RuntimeException("CSV 생성 중 오류 발생", e);
        }
    }
}

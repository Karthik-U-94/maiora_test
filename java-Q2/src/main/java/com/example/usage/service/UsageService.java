package com.example.usage.service;

import com.example.usage.dto.OutputDto;
import com.example.usage.entity.Usage;
import com.example.usage.repository.UsageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.*;

@Service
@RequiredArgsConstructor
public class UsageService {

    private final UsageRepository repository;
    private static final List<String> MONTHS_ORDER = Collections.unmodifiableList(Arrays.asList(
            "Apr", "May", "June", "July", "Aug", "Sep", "Oct", "Nov", "Dec", "Jan", "Feb", "March"
    ));

    private static final Map<String, Integer> MONTH_NAME_TO_NUMBER;
    private static final Set<String> APR_TO_DEC;

    static {
        Map<String, Integer> map = new HashMap<>();
        map.put("Jan", 1);
        map.put("Feb", 2);
        map.put("March", 3);
        map.put("Apr", 4);
        map.put("May", 5);
        map.put("June", 6);
        map.put("July", 7);
        map.put("Aug", 8);
        map.put("Sep", 9);
        map.put("Oct", 10);
        map.put("Nov", 11);
        map.put("Dec", 12);
        MONTH_NAME_TO_NUMBER = Collections.unmodifiableMap(map);

        APR_TO_DEC = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
                "Apr", "May", "June", "July", "Aug", "Sep", "Oct", "Nov", "Dec"
        )));
    }

    @Transactional
    public List<OutputDto> transformAndSave(Map<String, Object> request) {
        if (request == null) {
            return Collections.emptyList();
        }

        Object contentObj = request.get("content");
        if (!(contentObj instanceof Map)) {
            return Collections.emptyList();
        }

        @SuppressWarnings("unchecked")
        Map<String, Object> content = (Map<String, Object>) contentObj;

        String fyearStr = Objects.toString(content.get("Fyear"), null);
        if (fyearStr == null) {
            return Collections.emptyList();
        }

        final int fyear;
        try {
            fyear = Integer.parseInt(fyearStr.trim());
        } catch (NumberFormatException e) {
            return Collections.emptyList();
        }

        final String location = Objects.toString(content.get("Location"), "");
        final String code = Objects.toString(content.get("CatName"), "");
        final String unit = Objects.toString(content.get("Uom"), "");

        List<OutputDto> results = new ArrayList<>();
        List<Usage> recordsToSave = new ArrayList<>();

        for (String m : MONTHS_ORDER) {
            String rawVal = Objects.toString(content.get(m), "0");
            double val;
            try {
                val = Double.parseDouble(rawVal.replaceAll(",", ""));
            } catch (Exception e) {
                val = 0.0;
            }

            if (Double.compare(val, 0.0) == 0) {
                continue;
            }

            int monthNumber = MONTH_NAME_TO_NUMBER.getOrDefault(m, -1);

            int yearForMonth = APR_TO_DEC.contains(m) ? fyear - 1 : fyear;

            OutputDto dto = new OutputDto(location, code, monthNumber, yearForMonth, val, unit);
            results.add(dto);

            Usage rec = new Usage(null, location, code, monthNumber, yearForMonth, val, unit);
            recordsToSave.add(rec);
        }

        if (!recordsToSave.isEmpty()) {
            repository.saveAll(recordsToSave);
        }

        return results;
    }
}

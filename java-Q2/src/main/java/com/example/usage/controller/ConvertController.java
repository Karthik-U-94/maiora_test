package com.example.usage.controller;

import com.example.usage.dto.OutputDto;
import com.example.usage.service.UsageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ConvertController {

    private final UsageService transformService;

    @PostMapping(path = "/convert", consumes = "application/json", produces = "application/json")
    public ResponseEntity<List<OutputDto>> convertAndSave(@RequestBody Map<String, Object> request) {
        List<OutputDto> result = transformService.transformAndSave(request);
        return ResponseEntity.ok(result);
    }
}

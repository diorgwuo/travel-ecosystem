package com.travel.travelecosystem.infrastructure.web.controller;

import com.travel.travelecosystem.domain.service.ComplaintService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
public class ComplaintController {

    private final ComplaintService complaintService;

    @PostMapping("/api/complaints")
    @Operation(summary = "Создать жалобу")
    @Tag(name = "Жалобы", description = "Жалобы пользователей")
    public Map<String, Object> createComplaint(
            @Parameter(hidden = true) @RequestAttribute("userId") Long userId,
            @RequestBody Map<String, Object> body) {
        String targetType = body.get("targetType") != null ? body.get("targetType").toString() : null;
        Long targetId = body.get("targetId") instanceof Number ? ((Number) body.get("targetId")).longValue() : null;
        String message = body.get("message") != null ? body.get("message").toString() : null;
        return complaintService.createComplaint(userId, targetType, targetId, message);
    }
}

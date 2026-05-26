package com.travel.travelecosystem.domain.service;

import com.travel.travelecosystem.infrastructure.persistence.entity.ComplaintEntity;
import com.travel.travelecosystem.infrastructure.persistence.entity.ComplaintStatus;
import com.travel.travelecosystem.infrastructure.persistence.entity.UserEntity;
import com.travel.travelecosystem.infrastructure.persistence.repository.ComplaintRepository;
import com.travel.travelecosystem.infrastructure.persistence.repository.UserJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ComplaintService {

    private final ComplaintRepository complaintRepository;
    private final UserJpaRepository userJpaRepository;

    @Transactional
    public Map<String, Object> createComplaint(Long userId, String targetType, Long targetId, String message) {
        if (message == null || message.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Complaint message must not be blank");
        }
        ComplaintEntity saved = complaintRepository.save(ComplaintEntity.builder()
                .userId(userId)
                .targetType(targetType != null ? targetType.trim() : null)
                .targetId(targetId)
                .message(message.trim())
                .status(ComplaintStatus.OPEN)
                .build());
        return toDto(saved);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> listComplaints(Long adminId, String status, int limit, long offset) {
        assertAdmin(adminId);
        int page = (int) (offset / Math.max(limit, 1));
        Page<ComplaintEntity> result;
        if (status == null || status.isBlank()) {
            result = complaintRepository.findAll(PageRequest.of(page, limit));
        } else {
            ComplaintStatus complaintStatus = parseStatus(status);
            result = complaintRepository.findByStatus(complaintStatus, PageRequest.of(page, limit));
        }
        List<Map<String, Object>> items = result.getContent().stream().map(this::toDto).toList();
        return Map.of(
                "items", items,
                "limit", limit,
                "offset", offset,
                "totalSize", result.getTotalElements()
        );
    }

    @Transactional
    public Map<String, Object> processComplaint(Long adminId, Long complaintId, String status, String resolutionComment) {
        assertAdmin(adminId);
        ComplaintEntity entity = complaintRepository.findById(complaintId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Complaint not found"));
        entity.setStatus(parseStatus(status));
        entity.setResolutionComment(resolutionComment != null ? resolutionComment.trim() : null);
        return toDto(complaintRepository.save(entity));
    }

    private void assertAdmin(Long userId) {
        UserEntity actor = userJpaRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
        if (actor.getRole() != UserEntity.UserRole.ROLE_ADMIN) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Admin access required");
        }
    }

    private static ComplaintStatus parseStatus(String status) {
        if (status == null || status.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Status must not be blank");
        }
        try {
            return ComplaintStatus.valueOf(status.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid complaint status: " + status);
        }
    }

    private Map<String, Object> toDto(ComplaintEntity entity) {
        Map<String, Object> dto = new LinkedHashMap<>();
        dto.put("id", entity.getId());
        dto.put("userId", entity.getUserId());
        dto.put("targetType", entity.getTargetType());
        dto.put("targetId", entity.getTargetId());
        dto.put("message", entity.getMessage());
        dto.put("status", entity.getStatus().name());
        dto.put("resolutionComment", entity.getResolutionComment());
        dto.put("createdAt", entity.getCreatedAt());
        dto.put("updatedAt", entity.getUpdatedAt());
        return dto;
    }
}

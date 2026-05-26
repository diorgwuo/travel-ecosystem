package com.travel.travelecosystem.domain.service;

import com.travel.travelecosystem.infrastructure.persistence.entity.ExcursionEntity;
import com.travel.travelecosystem.infrastructure.persistence.entity.PlaceEntity;
import com.travel.travelecosystem.infrastructure.persistence.entity.UserEntity;
import com.travel.travelecosystem.infrastructure.persistence.repository.ExcursionRepository;
import com.travel.travelecosystem.infrastructure.persistence.repository.PlaceRepository;
import com.travel.travelecosystem.infrastructure.persistence.repository.UserJpaRepository;
import com.travel.travelecosystem.infrastructure.web.excursion.dto.ExcursionListResponse;
import com.travel.travelecosystem.infrastructure.web.excursion.dto.ExcursionRequest;
import com.travel.travelecosystem.infrastructure.web.excursion.dto.ExcursionResponse;
import com.travel.travelecosystem.infrastructure.web.place.dto.PlaceListResponse;
import com.travel.travelecosystem.infrastructure.web.place.dto.PlaceRequest;
import com.travel.travelecosystem.infrastructure.web.place.dto.PlaceResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserJpaRepository userJpaRepository;
    private final ExcursionRepository excursionRepository;
    private final PlaceRepository placeRepository;
    private final ExcursionService excursionService;
    private final PlaceService placeService;
    private final ComplaintService complaintService;

    @Transactional(readOnly = true)
    public Map<String, Object> getUsers(Long adminId, int limit, long offset) {
        assertAdmin(adminId);
        int page = (int) (offset / Math.max(limit, 1));
        Page<UserEntity> result = userJpaRepository.findAll(PageRequest.of(page, limit));
        List<Map<String, Object>> items = result.getContent().stream().map(this::toUserDto).toList();
        return Map.of("items", items, "limit", limit, "offset", offset, "totalSize", result.getTotalElements());
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getUserById(Long adminId, Long userId) {
        assertAdmin(adminId);
        UserEntity user = userJpaRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        return toUserDto(user);
    }

    @Transactional
    public Map<String, Object> updateUser(Long adminId, Long userId, Map<String, Object> body) {
        assertAdmin(adminId);
        UserEntity user = userJpaRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        if (body.get("firstName") != null) {
            user.setFirstName(body.get("firstName").toString().trim());
        }
        if (body.get("lastName") != null) {
            user.setLastName(body.get("lastName").toString().trim());
        }
        if (body.get("email") != null) {
            user.setEmail(body.get("email").toString().trim().toLowerCase());
        }
        UserEntity saved = userJpaRepository.save(user);
        return toUserDto(saved);
    }

    @Transactional
    public Map<String, Object> blockUser(Long adminId, Long userId) {
        assertAdmin(adminId);
        UserEntity user = userJpaRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        user.setStatus(UserEntity.UserStatus.BLOCKED);
        return toUserDto(userJpaRepository.save(user));
    }

    @Transactional
    public Map<String, Object> unblockUser(Long adminId, Long userId) {
        assertAdmin(adminId);
        UserEntity user = userJpaRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        user.setStatus(UserEntity.UserStatus.ACTIVE);
        return toUserDto(userJpaRepository.save(user));
    }

    @Transactional(readOnly = true)
    public ExcursionListResponse getAllExcursionsForModeration(Long adminId, int limit, long offset) {
        assertAdmin(adminId);
        int page = (int) (offset / Math.max(limit, 1));
        Page<ExcursionEntity> result = excursionRepository.findAll(PageRequest.of(page, limit));
        List<ExcursionResponse> items = result.getContent().stream()
                .map(e -> excursionService.getById(e.getId(), adminId))
                .toList();
        return new ExcursionListResponse(items, limit, offset, result.getTotalElements());
    }

    @Transactional
    public ExcursionResponse moderateExcursionUpdate(Long adminId, Long excursionId, ExcursionRequest request) {
        assertAdmin(adminId);
        return excursionService.update(excursionId, adminId, request);
    }

    @Transactional
    public ExcursionResponse publishExcursion(Long adminId, Long excursionId) {
        assertAdmin(adminId);
        return excursionService.setPublicationStatusByAdmin(excursionId, adminId, true);
    }

    @Transactional
    public ExcursionResponse rejectExcursion(Long adminId, Long excursionId) {
        assertAdmin(adminId);
        return excursionService.setPublicationStatusByAdmin(excursionId, adminId, false);
    }

    @Transactional(readOnly = true)
    public PlaceListResponse getAllPlacesForModeration(Long adminId, int limit, long offset) {
        assertAdmin(adminId);
        int page = (int) (offset / Math.max(limit, 1));
        Page<PlaceEntity> result = placeRepository.findAll(PageRequest.of(page, limit));
        List<PlaceResponse> items = result.getContent().stream().map(placeService::convertToDto).toList();
        return new PlaceListResponse(items, limit, offset, result.getTotalElements());
    }

    @Transactional
    public PlaceResponse createPlace(Long adminId, PlaceRequest request) {
        assertAdmin(adminId);
        return placeService.createPlace(request);
    }

    @Transactional
    public PlaceResponse updatePlace(Long adminId, Long placeId, PlaceRequest request) {
        assertAdmin(adminId);
        PlaceEntity entity = placeRepository.findById(placeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Place not found"));
        PlaceEntity changed = placeService.convertToEntity(request);
        entity.setName(changed.getName());
        entity.setDescription(changed.getDescription());
        entity.setCategory(changed.getCategory());
        entity.setAddress(changed.getAddress());
        entity.setLocation(changed.getLocation());
        entity.setImageUrl(changed.getImageUrl());
        entity.setRating(changed.getRating());
        placeService.applyTagsFromRequest(entity, request);
        return placeService.convertToDto(placeRepository.save(entity));
    }

    @Transactional
    public void deletePlace(Long adminId, Long placeId) {
        assertAdmin(adminId);
        PlaceEntity entity = placeRepository.findById(placeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Place not found"));
        placeRepository.delete(entity);
    }

    @Transactional
    public PlaceResponse unpublishPlace(Long adminId, Long placeId) {
        assertAdmin(adminId);
        PlaceEntity entity = placeRepository.findById(placeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Place not found"));
        entity.setPublished(false);
        return placeService.convertToDto(placeRepository.save(entity));
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getComplaints(Long adminId, String status, int limit, long offset) {
        return complaintService.listComplaints(adminId, status, limit, offset);
    }

    @Transactional
    public Map<String, Object> processComplaint(Long adminId, Long complaintId, String status, String resolutionComment) {
        return complaintService.processComplaint(adminId, complaintId, status, resolutionComment);
    }

    private void assertAdmin(Long userId) {
        UserEntity actor = userJpaRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
        if (actor.getRole() != UserEntity.UserRole.ROLE_ADMIN) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Admin access required");
        }
    }

    private Map<String, Object> toUserDto(UserEntity user) {
        Map<String, Object> dto = new LinkedHashMap<>();
        dto.put("id", user.getId());
        dto.put("email", user.getEmail());
        dto.put("firstName", user.getFirstName());
        dto.put("lastName", user.getLastName());
        dto.put("role", user.getRole().name());
        dto.put("status", user.getStatus().name());
        dto.put("createdAt", user.getCreatedAt());
        dto.put("updatedAt", user.getUpdatedAt());
        return dto;
    }

}

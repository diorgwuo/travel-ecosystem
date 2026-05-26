package com.travel.travelecosystem.infrastructure.web.controller;

import com.travel.travelecosystem.domain.service.AdminService;
import com.travel.travelecosystem.infrastructure.web.excursion.dto.ExcursionListResponse;
import com.travel.travelecosystem.infrastructure.web.excursion.dto.ExcursionRequest;
import com.travel.travelecosystem.infrastructure.web.excursion.dto.ExcursionResponse;
import com.travel.travelecosystem.infrastructure.web.place.dto.PlaceListResponse;
import com.travel.travelecosystem.infrastructure.web.place.dto.PlaceRequest;
import com.travel.travelecosystem.infrastructure.web.place.dto.PlaceResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Tag(name = "Администрирование", description = "Панель администратора")
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/users")
    @Operation(summary = "Список пользователей")
    public Map<String, Object> users(
            @Parameter(hidden = true) @RequestAttribute("userId") Long adminId,
            @RequestParam(defaultValue = "20") int limit,
            @RequestParam(defaultValue = "0") long offset) {
        return adminService.getUsers(adminId, limit, offset);
    }

    @GetMapping("/users/{id}")
    @Operation(summary = "Пользователь по id")
    public Map<String, Object> userById(
            @Parameter(hidden = true) @RequestAttribute("userId") Long adminId,
            @PathVariable Long id) {
        return adminService.getUserById(adminId, id);
    }

    @PutMapping("/users/{id}")
    @Operation(summary = "Редактировать пользователя")
    public Map<String, Object> updateUser(
            @Parameter(hidden = true) @RequestAttribute("userId") Long adminId,
            @PathVariable Long id,
            @RequestBody Map<String, Object> body) {
        return adminService.updateUser(adminId, id, body);
    }

    @PostMapping("/users/{id}/block")
    @Operation(summary = "Заблокировать пользователя")
    public Map<String, Object> blockUser(
            @Parameter(hidden = true) @RequestAttribute("userId") Long adminId,
            @PathVariable Long id) {
        return adminService.blockUser(adminId, id);
    }

    @PostMapping("/users/{id}/unblock")
    @Operation(summary = "Разблокировать пользователя")
    public Map<String, Object> unblockUser(
            @Parameter(hidden = true) @RequestAttribute("userId") Long adminId,
            @PathVariable Long id) {
        return adminService.unblockUser(adminId, id);
    }

    @GetMapping("/excursions")
    @Operation(summary = "Все экскурсии для модерации")
    public ExcursionListResponse excursions(
            @Parameter(hidden = true) @RequestAttribute("userId") Long adminId,
            @RequestParam(defaultValue = "20") int limit,
            @RequestParam(defaultValue = "0") long offset) {
        return adminService.getAllExcursionsForModeration(adminId, limit, offset);
    }

    @PutMapping("/excursions/{id}")
    @Operation(summary = "Редактировать экскурсию (админ)")
    public ExcursionResponse updateExcursion(
            @Parameter(hidden = true) @RequestAttribute("userId") Long adminId,
            @PathVariable Long id,
            @Valid @RequestBody ExcursionRequest request) {
        return adminService.moderateExcursionUpdate(adminId, id, request);
    }

    @PostMapping("/excursions/{id}/publish")
    @Operation(summary = "Опубликовать экскурсию (админ)")
    public ExcursionResponse publishExcursion(
            @Parameter(hidden = true) @RequestAttribute("userId") Long adminId,
            @PathVariable Long id) {
        return adminService.publishExcursion(adminId, id);
    }

    @PostMapping("/excursions/{id}/reject")
    @Operation(summary = "Отклонить/снять с публикации экскурсию (админ)")
    public ExcursionResponse rejectExcursion(
            @Parameter(hidden = true) @RequestAttribute("userId") Long adminId,
            @PathVariable Long id) {
        return adminService.rejectExcursion(adminId, id);
    }

    @GetMapping("/places")
    @Operation(summary = "Все места для модерации")
    public PlaceListResponse places(
            @Parameter(hidden = true) @RequestAttribute("userId") Long adminId,
            @RequestParam(defaultValue = "20") int limit,
            @RequestParam(defaultValue = "0") long offset) {
        return adminService.getAllPlacesForModeration(adminId, limit, offset);
    }

    @PostMapping("/places")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Добавить место (админ)")
    public PlaceResponse createPlace(
            @Parameter(hidden = true) @RequestAttribute("userId") Long adminId,
            @Valid @RequestBody PlaceRequest request) {
        return adminService.createPlace(adminId, request);
    }

    @PutMapping("/places/{id}")
    @Operation(summary = "Редактировать место (админ)")
    public PlaceResponse updatePlace(
            @Parameter(hidden = true) @RequestAttribute("userId") Long adminId,
            @PathVariable Long id,
            @Valid @RequestBody PlaceRequest request) {
        return adminService.updatePlace(adminId, id, request);
    }

    @DeleteMapping("/places/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Удалить место (админ)")
    public void deletePlace(
            @Parameter(hidden = true) @RequestAttribute("userId") Long adminId,
            @PathVariable Long id) {
        adminService.deletePlace(adminId, id);
    }

    @PostMapping("/places/{id}/unpublish")
    @Operation(summary = "Снять место с публикации (админ)")
    public PlaceResponse unpublishPlace(
            @Parameter(hidden = true) @RequestAttribute("userId") Long adminId,
            @PathVariable Long id) {
        return adminService.unpublishPlace(adminId, id);
    }

    @GetMapping("/complaints")
    @Operation(summary = "Список жалоб")
    public Map<String, Object> complaints(
            @Parameter(hidden = true) @RequestAttribute("userId") Long adminId,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "20") int limit,
            @RequestParam(defaultValue = "0") long offset) {
        return adminService.getComplaints(adminId, status, limit, offset);
    }

    @PatchMapping("/complaints/{id}")
    @Operation(summary = "Обработать жалобу")
    public Map<String, Object> processComplaint(
            @Parameter(hidden = true) @RequestAttribute("userId") Long adminId,
            @PathVariable Long id,
            @RequestBody Map<String, Object> body) {
        String status = body.get("status") != null ? body.get("status").toString() : null;
        String resolutionComment =
                body.get("resolutionComment") != null ? body.get("resolutionComment").toString() : null;
        return adminService.processComplaint(adminId, id, status, resolutionComment);
    }
}

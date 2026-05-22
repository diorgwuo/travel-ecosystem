package com.travel.travelecosystem.infrastructure.web.controller;

import com.travel.travelecosystem.domain.service.ExcursionService;
import com.travel.travelecosystem.infrastructure.web.excursion.dto.ExcursionListResponse;
import com.travel.travelecosystem.infrastructure.web.excursion.dto.ExcursionRequest;
import com.travel.travelecosystem.infrastructure.web.excursion.dto.ExcursionResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/excursions")
@RequiredArgsConstructor
@Tag(name = "Экскурсии", description = "Публичные и личные экскурсии оператора")
public class ExcursionController {

    private final ExcursionService excursionService;

    @GetMapping
    @Operation(summary = "Список опубликованных экскурсий")
    public ExcursionListResponse getAll(
            @RequestParam(defaultValue = "20") int limit,
            @RequestParam(defaultValue = "0") long offset,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {
        return excursionService.getAll(limit, offset, from, to);
    }

    @GetMapping("/my")
    @Operation(summary = "Мои экскурсии (по оператору из токена)")
    public ExcursionListResponse myExcursions(
            @Parameter(hidden = true) @RequestAttribute("userId") Long userId,
            @RequestParam(defaultValue = "20") int limit,
            @RequestParam(defaultValue = "0") long offset) {
        return excursionService.getByOperator(userId, limit, offset);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Экскурсия по id (черновики — только владелец или админ)")
    public ExcursionResponse getById(
            @Parameter(hidden = true) @RequestAttribute(value = "userId", required = false) Long userId,
            @PathVariable Long id) {
        return excursionService.getById(id, userId);
    }

    @PostMapping("/{id}/view")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Увеличить счётчик просмотров")
    public void registerView(@PathVariable Long id) {
        excursionService.incrementViews(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Создать экскурсию")
    public ExcursionResponse create(
            @Parameter(hidden = true) @RequestAttribute("userId") Long userId,
            @Valid @RequestBody ExcursionRequest request) {
        return excursionService.create(userId, request);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Обновить экскурсию")
    public ExcursionResponse update(
            @Parameter(hidden = true) @RequestAttribute("userId") Long userId,
            @PathVariable Long id,
            @Valid @RequestBody ExcursionRequest request) {
        return excursionService.update(id, userId, request);
    }

    @PatchMapping("/{id}/publish")
    @Operation(summary = "Опубликовать экскурсию")
    public ExcursionResponse publish(
            @Parameter(hidden = true) @RequestAttribute("userId") Long userId,
            @PathVariable Long id) {
        return excursionService.publish(id, userId);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Удалить экскурсию")
    public void delete(
            @Parameter(hidden = true) @RequestAttribute("userId") Long userId,
            @PathVariable Long id) {
        excursionService.delete(id, userId);
    }
}

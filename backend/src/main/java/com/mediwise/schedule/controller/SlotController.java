package com.mediwise.schedule.controller;

import com.mediwise.auth.model.User;
import com.mediwise.common.response.ApiResponse;
import com.mediwise.schedule.dto.SlotLockResponse;
import com.mediwise.schedule.dto.SlotResponse;
import com.mediwise.schedule.service.SlotService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Scheduling", description = "Slot availability, locking, releasing")
public class SlotController {

    private final SlotService slotService;

    @GetMapping("/doctors/{doctorId}/slots")
    @Operation(summary = "Get available slots for a doctor on a given date")
    public ResponseEntity<ApiResponse<List<SlotResponse>>> getSlots(
            @PathVariable UUID doctorId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(ApiResponse.success(slotService.getAvailableSlots(doctorId, date)));
    }

    @PostMapping("/slots/{slotId}/lock")
    @Operation(summary = "Lock a slot (5-min reservation) before booking")
    public ResponseEntity<ApiResponse<SlotLockResponse>> lockSlot(
            @PathVariable UUID slotId,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(ApiResponse.success(slotService.lockSlot(slotId, currentUser)));
    }

    @DeleteMapping("/slots/{slotId}/lock")
    @Operation(summary = "Release a slot lock (on back-press or cancel)")
    public ResponseEntity<ApiResponse<Void>> releaseSlot(
            @PathVariable UUID slotId,
            @AuthenticationPrincipal User currentUser) {
        slotService.releaseSlot(slotId, currentUser);
        return ResponseEntity.ok(ApiResponse.message("Slot released"));
    }
}

package com.smartmoldguard.control.interfaces.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Builder;
import lombok.Data;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/v1/control/spaces")
@Tag(name = "Space Management", description = "空间管理接口")
public class SpaceController {

    @GetMapping
    @Operation(summary = "获取空间列表")
    public ResponseEntity<SpaceListDto> getSpaceList(@RequestParam(required = false) Long parentId) {
        // Mock Data
        List<SpaceDto> spaces = new ArrayList<>();
        if (parentId == null) {
            spaces.add(SpaceDto.builder().id(1L).name("金南家园").type("小区").build());
        } else if (parentId == 1L) {
            spaces.add(SpaceDto.builder().id(2L).name("1号楼").type("楼栋").parentId(1L).build());
        }
        
        return ResponseEntity.ok(SpaceListDto.builder()
                .list(spaces)
                .total(spaces.size())
                .build());
    }

    @PostMapping
    @Operation(summary = "创建空间")
    public ResponseEntity<SpaceDto> createSpace(@RequestBody SpaceCreateRequest request) {
        return ResponseEntity.ok(SpaceDto.builder()
                .id(System.currentTimeMillis())
                .name(request.getName())
                .type(request.getType())
                .parentId(request.getParentId())
                .build());
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除空间")
    public ResponseEntity<Void> deleteSpace(@PathVariable Long id) {
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{spaceId}/devices/{deviceId}")
    @Operation(summary = "关联设备")
    public ResponseEntity<Void> associateDevice(@PathVariable Long spaceId, @PathVariable Long deviceId) {
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{spaceId}/devices/{deviceId}")
    @Operation(summary = "移除设备关联")
    public ResponseEntity<Void> removeDeviceAssociation(@PathVariable Long spaceId, @PathVariable Long deviceId) {
        return ResponseEntity.ok().build();
    }

    @Data
    @Builder
    public static class SpaceListDto {
        private List<SpaceDto> list;
        private int total;
    }

    @Data
    @Builder
    public static class SpaceDto {
        private Long id;
        private String name;
        private String type;
        private Long parentId;
        private List<SpaceDto> children;
    }

    @Data
    public static class SpaceCreateRequest {
        private String name;
        private String type;
        private Long parentId;
        private String function;
    }
}

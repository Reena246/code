package com.demo.accesscontrolsystem.controller;

import com.demo.accesscontrolsystem.dto.ValidateRequest;
import com.demo.accesscontrolsystem.dto.ValidateResponse;
import com.demo.accesscontrolsystem.service.AccessService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AccessController {

    private final AccessService accessService;

    @PostMapping("/validate")
    public ValidateResponse validateCard(@RequestBody ValidateRequest request) {
        return accessService.validateCard(request);
    }

    @GetMapping("/server-heartbeat")
    public String heartbeat(@RequestParam String deviceId) {
        return "Server online, device: " + deviceId;
    }
}

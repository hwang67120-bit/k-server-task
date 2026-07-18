package com.example.kservertask.point.controller;

import com.example.kservertask.point.request.PointChargeRequest;
import com.example.kservertask.point.response.PointChargeResponse;
import com.example.kservertask.point.service.PointChargeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/points/charges")
@RequiredArgsConstructor
public class PointChargeController {

    private final PointChargeService pointChargeService;

    @PostMapping
    public PointChargeResponse chargePoint(
            @Valid @RequestBody PointChargeRequest request
    ) {
        return pointChargeService.chargePoint(request);
    }
}

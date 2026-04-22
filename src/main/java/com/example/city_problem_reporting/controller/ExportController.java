package com.example.city_problem_reporting.controller;

import com.example.city_problem_reporting.model.User;
import com.example.city_problem_reporting.repository.UserRepository;
import com.example.city_problem_reporting.service.PdfExportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.security.Principal;

@RestController
@RequestMapping("/api/export")
@SecurityRequirement(name = "basicAuth")
public class ExportController {

    private final PdfExportService pdfExportService;
    private final UserRepository userRepository;

    public ExportController(PdfExportService pdfExportService, UserRepository userRepository) {
        this.pdfExportService = pdfExportService;
        this.userRepository = userRepository;
    }

    @GetMapping("/my-reports/pdf")
    @Operation(summary = "Export my reports as PDF")
    public ResponseEntity<byte[]> exportMyReports(Principal principal) {
        User user = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));

        byte[] pdf = pdfExportService.exportUserReports(user.getId(), user.getUsername());

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"cityfix-reports.pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }
}
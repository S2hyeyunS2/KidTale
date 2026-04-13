package com.hyeyuns2.kidtale.external.sweetbook.controller;

import com.hyeyuns2.kidtale.common.response.ApiResponse;
import com.hyeyuns2.kidtale.external.sweetbook.client.SweetBookClient;
import com.hyeyuns2.kidtale.external.sweetbook.dto.response.BookSpec;
import com.hyeyuns2.kidtale.external.sweetbook.dto.response.Template;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/admin/sweetbook")
public class SweetBookAdminController {

    private final SweetBookClient sweetBookClient;

    public SweetBookAdminController(SweetBookClient sweetBookClient) {
        this.sweetBookClient = sweetBookClient;
    }

    @GetMapping("/templates")
    public ResponseEntity<ApiResponse<List<Template>>> getTemplates(
            @RequestParam(name = "bookSpecUid", required = false) String bookSpecUid,
            @RequestParam(name = "templateKind", required = false) String templateKind
    ) {
        log.debug("[SweetBookAdmin] GET /templates. bookSpecUid={}, templateKind={}", bookSpecUid, templateKind);
        List<Template> templates = sweetBookClient.getTemplates(bookSpecUid, templateKind);
        return ResponseEntity.ok(ApiResponse.ok(templates));
    }

    @GetMapping("/book-specs")
    public ResponseEntity<ApiResponse<List<BookSpec>>> getBookSpecs() {
        log.debug("[SweetBookAdmin] GET /book-specs");
        List<BookSpec> bookSpecs = sweetBookClient.getBookSpecs();
        return ResponseEntity.ok(ApiResponse.ok(bookSpecs));
    }
}

package com.hyeyuns2.kidtale.book.controller;

import com.hyeyuns2.kidtale.book.dto.request.BookCreateRequest;
import com.hyeyuns2.kidtale.book.dto.response.BookCreateResponse;
import com.hyeyuns2.kidtale.book.service.BookService;
import com.hyeyuns2.kidtale.common.response.ApiResponse;
import com.hyeyuns2.kidtale.external.sweetbook.dto.response.Template;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/books")
public class BookController {

    private final BookService bookService;

    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    /**
     * 사용할 수 있는 템플릿 목록을 조회한다.
     * 프론트엔드에서 사용자가 표지/내지 스타일을 선택할 때 사용.
     *
     * @param templateKind "cover" 또는 "content"
     */
    @GetMapping("/templates")
    public ResponseEntity<ApiResponse<List<Template>>> getTemplates(
            @RequestParam(name = "templateKind", required = false) String templateKind
    ) {
        log.debug("[BookController] GET /api/books/templates. templateKind={}", templateKind);
        List<Template> templates = bookService.getTemplates(templateKind);
        return ResponseEntity.ok(ApiResponse.ok(templates));
    }

    /**
     * 사용자가 고른 표지/내지 템플릿으로 책을 생성한다.
     */
    @PostMapping
    public ResponseEntity<ApiResponse<BookCreateResponse>> createBook(
            @Valid @RequestBody BookCreateRequest request
    ) {
        log.debug("[BookController] POST /api/books. storyId={}", request.storyId());
        BookCreateResponse response = bookService.createBook(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.ok("책이 생성되었습니다.", response));
    }
}

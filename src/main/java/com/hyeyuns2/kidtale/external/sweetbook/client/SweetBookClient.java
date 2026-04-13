package com.hyeyuns2.kidtale.external.sweetbook.client;

import com.hyeyuns2.kidtale.common.exception.ErrorCode;
import com.hyeyuns2.kidtale.common.exception.KidTaleException;
import com.hyeyuns2.kidtale.external.sweetbook.dto.request.CreateBookRequest;
import com.hyeyuns2.kidtale.external.sweetbook.dto.request.CreateContentRequest;
import com.hyeyuns2.kidtale.external.sweetbook.dto.request.CreateCoverRequest;
import com.hyeyuns2.kidtale.external.sweetbook.dto.request.CreateOrderRequest;
import com.hyeyuns2.kidtale.external.sweetbook.dto.response.BookSpec;
import com.hyeyuns2.kidtale.external.sweetbook.dto.response.CreateBookData;
import com.hyeyuns2.kidtale.external.sweetbook.dto.response.CreateOrderData;
import com.hyeyuns2.kidtale.external.sweetbook.dto.response.SweetBookApiResponse;
import com.hyeyuns2.kidtale.external.sweetbook.dto.response.Template;
import com.hyeyuns2.kidtale.external.sweetbook.dto.response.TemplateListData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
public class SweetBookClient {

    private final WebClient webClient;

    public SweetBookClient(@Qualifier("sweetBookWebClient") WebClient webClient) {
        this.webClient = webClient;
    }

    public String createBook(CreateBookRequest request) {
        try {
            SweetBookApiResponse<CreateBookData> response = webClient.post()
                    .uri("/books")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<SweetBookApiResponse<CreateBookData>>() {})
                    .blockOptional()
                    .orElseThrow(() -> new KidTaleException(ErrorCode.SWEETBOOK_API_ERROR));
            return response.data().bookUid();
        } catch (WebClientResponseException e) {
            log.error("[SweetBookClient] POST /books failed. status={}, body={}",
                    e.getStatusCode(), e.getResponseBodyAsString(), e);
            throw new KidTaleException(ErrorCode.SWEETBOOK_API_ERROR);
        } catch (KidTaleException e) {
            throw e;
        } catch (Exception e) {
            log.error("[SweetBookClient] POST /books failed.", e);
            throw new KidTaleException(ErrorCode.SWEETBOOK_API_ERROR);
        }
    }

    public void createCover(String bookId, CreateCoverRequest request) {
        MultipartBodyBuilder builder = buildMultipart(
                request.templateUid(), request.parameters(), request.images()
        );
        try {
            webClient.post()
                    .uri("/books/{id}/cover", bookId)
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(BodyInserters.fromMultipartData(builder.build()))
                    .retrieve()
                    .toBodilessEntity()
                    .block();
        } catch (WebClientResponseException e) {
            log.error("[SweetBookClient] POST /books/{}/cover failed. status={}, body={}",
                    bookId, e.getStatusCode(), e.getResponseBodyAsString(), e);
            throw new KidTaleException(ErrorCode.SWEETBOOK_API_ERROR);
        } catch (Exception e) {
            log.error("[SweetBookClient] POST /books/{}/cover failed.", bookId, e);
            throw new KidTaleException(ErrorCode.SWEETBOOK_API_ERROR);
        }
    }

    public void createContents(String bookId, CreateContentRequest request) {
        MultipartBodyBuilder builder = buildMultipart(
                request.templateUid(), request.parameters(), request.images()
        );
        try {
            webClient.post()
                    .uri(uriBuilder -> {
                        var b = uriBuilder.path("/books/{id}/contents");
                        if (request.breakBefore() != null) {
                            b = b.queryParam("breakBefore", request.breakBefore());
                        }
                        return b.build(bookId);
                    })
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(BodyInserters.fromMultipartData(builder.build()))
                    .retrieve()
                    .toBodilessEntity()
                    .block();
        } catch (WebClientResponseException e) {
            log.error("[SweetBookClient] POST /books/{}/contents failed. status={}, body={}",
                    bookId, e.getStatusCode(), e.getResponseBodyAsString(), e);
            throw new KidTaleException(ErrorCode.SWEETBOOK_API_ERROR);
        } catch (Exception e) {
            log.error("[SweetBookClient] POST /books/{}/contents failed.", bookId, e);
            throw new KidTaleException(ErrorCode.SWEETBOOK_API_ERROR);
        }
    }

    public void finalizeBook(String bookId) {
        try {
            webClient.post()
                    .uri("/books/{id}/finalization", bookId)
                    .retrieve()
                    .toBodilessEntity()
                    .block();
        } catch (WebClientResponseException e) {
            log.error("[SweetBookClient] POST /books/{}/finalization failed. status={}, body={}",
                    bookId, e.getStatusCode(), e.getResponseBodyAsString(), e);
            throw new KidTaleException(ErrorCode.SWEETBOOK_API_ERROR);
        } catch (Exception e) {
            log.error("[SweetBookClient] POST /books/{}/finalization failed.", bookId, e);
            throw new KidTaleException(ErrorCode.SWEETBOOK_API_ERROR);
        }
    }

    public String createOrder(CreateOrderRequest request) {
        try {
            SweetBookApiResponse<CreateOrderData> response = webClient.post()
                    .uri("/orders")
                    .header("Idempotency-Key", UUID.randomUUID().toString())
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<SweetBookApiResponse<CreateOrderData>>() {})
                    .blockOptional()
                    .orElseThrow(() -> new KidTaleException(ErrorCode.SWEETBOOK_API_ERROR));
            return response.data().orderUid();
        } catch (WebClientResponseException e) {
            log.error("[SweetBookClient] POST /orders failed. status={}, body={}",
                    e.getStatusCode(), e.getResponseBodyAsString(), e);
            throw new KidTaleException(ErrorCode.SWEETBOOK_API_ERROR);
        } catch (KidTaleException e) {
            throw e;
        } catch (Exception e) {
            log.error("[SweetBookClient] POST /orders failed.", e);
            throw new KidTaleException(ErrorCode.SWEETBOOK_API_ERROR);
        }
    }

    public List<Template> getTemplates(String bookSpecUid, String templateKind) {
        try {
            SweetBookApiResponse<TemplateListData> response = webClient.get()
                    .uri(uriBuilder -> {
                        var b = uriBuilder.path("/templates");
                        if (bookSpecUid != null) b = b.queryParam("bookSpecUid", bookSpecUid);
                        if (templateKind != null) b = b.queryParam("templateKind", templateKind);
                        return b.build();
                    })
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<SweetBookApiResponse<TemplateListData>>() {})
                    .blockOptional()
                    .orElseThrow(() -> new KidTaleException(ErrorCode.SWEETBOOK_API_ERROR));
            return response.data().templates();
        } catch (WebClientResponseException e) {
            log.error("[SweetBookClient] GET /templates failed. status={}, body={}",
                    e.getStatusCode(), e.getResponseBodyAsString(), e);
            throw new KidTaleException(ErrorCode.SWEETBOOK_API_ERROR);
        } catch (KidTaleException e) {
            throw e;
        } catch (Exception e) {
            log.error("[SweetBookClient] GET /templates failed.", e);
            throw new KidTaleException(ErrorCode.SWEETBOOK_API_ERROR);
        }
    }

    public List<BookSpec> getBookSpecs() {
        try {
            SweetBookApiResponse<List<BookSpec>> response = webClient.get()
                    .uri("/book-specs")
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<SweetBookApiResponse<List<BookSpec>>>() {})
                    .blockOptional()
                    .orElseThrow(() -> new KidTaleException(ErrorCode.SWEETBOOK_API_ERROR));
            return response.data();
        } catch (WebClientResponseException e) {
            log.error("[SweetBookClient] GET /book-specs failed. status={}, body={}",
                    e.getStatusCode(), e.getResponseBodyAsString(), e);
            throw new KidTaleException(ErrorCode.SWEETBOOK_API_ERROR);
        } catch (KidTaleException e) {
            throw e;
        } catch (Exception e) {
            log.error("[SweetBookClient] GET /book-specs failed.", e);
            throw new KidTaleException(ErrorCode.SWEETBOOK_API_ERROR);
        }
    }

    private MultipartBodyBuilder buildMultipart(String templateUid, String parameters, Map<String, byte[]> images) {
        MultipartBodyBuilder builder = new MultipartBodyBuilder();
        builder.part("templateUid", templateUid);
        if (parameters != null) {
            builder.part("parameters", parameters);
        }
        if (images != null) {
            images.forEach((fieldName, bytes) ->
                    builder.part(fieldName, new ByteArrayResource(bytes))
                            .filename(fieldName)
                            .contentType(MediaType.APPLICATION_OCTET_STREAM)
            );
        }
        return builder;
    }
}

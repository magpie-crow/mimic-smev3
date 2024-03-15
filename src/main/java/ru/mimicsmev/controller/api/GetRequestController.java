package ru.mimicsmev.controller.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.mimicsmev.dao.GetRequestDAO;
import ru.mimicsmev.dao.PostApiModel;

@Slf4j
@Controller
@RequestMapping("/api/get_request")
@Tag(name = "GetRequest", description = "API для GetRequest запросов")
public class GetRequestController {
    private final GetRequestDAO getRequestDAO;

    public GetRequestController(GetRequestDAO getRequestDAO) {
        this.getRequestDAO = getRequestDAO;
    }

    @GetMapping(value = "/list")
    @Operation(summary = "Просмотр списка сообщений get_request")
    public ResponseEntity<Page<?>> viewGetRequestList(@Parameter(description = "колличество элеметов")
                                                      @RequestParam(value = "page_size", defaultValue = "10") int pageSize,
                                                      @Parameter(description = "станица")
                                                      @RequestParam(value = "page_number", defaultValue = "0") int pageNumber
    ) {
        return ResponseEntity.ok(getRequestDAO.getList(pageSize, pageNumber));
    }

    @GetMapping(value = "/{id}")
    @Operation(summary = "Просмотр сообщения get_request")
    public ResponseEntity<?> viewGetRequest(@Parameter(description = "id записи") @PathVariable(name = "id") long id) {
        return ResponseEntity.ok(getRequestDAO.get(id));
    }

    @PostMapping(value = "/create")
    @Operation(summary = "Создать сообщение get_request")
    public ResponseEntity<?> createGetRequest(@Parameter(description = "запрос на создание")
                                              @RequestBody PostApiModel model) {
        try {
            return ResponseEntity.ok(getRequestDAO.addGetRequest(model));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }

    }
}

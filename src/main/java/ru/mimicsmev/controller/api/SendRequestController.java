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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.mimicsmev.dao.SendRequestDao;

@Slf4j
@Controller
@RequestMapping("/api/send_request")
@Tag(name = "SendRequest", description = "API для SendRequest запросов")
public class SendRequestController {
    private final SendRequestDao sendRequestDao;

    public SendRequestController(SendRequestDao sendRequestDao) {

        this.sendRequestDao = sendRequestDao;
    }

    @GetMapping(value = "/list")
    @Operation(summary = "Просмотр списка сообщений send_request")
    public ResponseEntity<Page<?>> viewGetRequestList(@Parameter(description = "колличество элеметов")
                                                      @RequestParam(value = "page_size", defaultValue = "10") int pageSize,
                                                      @Parameter(description = "станица")
                                                      @RequestParam(value = "page_number", defaultValue = "0") int pageNumber
    ) {
        return ResponseEntity.ok(sendRequestDao.getList(pageSize, pageNumber));
    }

    @GetMapping(value = "/{id}")
    @Operation(summary = "Просмотр сообщения send_request")
    public ResponseEntity<?> viewGetRequest(@Parameter(description = "id записи") @PathVariable(name = "id") long id) {
        return ResponseEntity.ok(sendRequestDao.get(id));
    }

}

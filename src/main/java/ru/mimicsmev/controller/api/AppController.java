package ru.mimicsmev.controller.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import ru.mimicsmev.dao.VsListDao;
import ru.mimicsmev.exception.AppException;

@Controller
@Slf4j
@RequestMapping("/")
public class AppController {
    private final VsListDao vsListDao;

    public AppController(VsListDao vsListDao) {
        this.vsListDao = vsListDao;
    }

    @RequestMapping (value = {"/", "/{x:[\\w\\-]+}", "/{x:^(?!api$).*$}/*/{y:[\\w\\-]+}","/error"}, method = RequestMethod.GET)
    public String homePage() {
        return "index";
    }

    @GetMapping(value = "/api/vs/list")
    @Operation(summary = "Просмотр списка ВС обмена")
    public ResponseEntity<?> getVsList(@Parameter(description = "колличество элеметов")
                                       @RequestParam(value = "page_size", defaultValue = "10") int pageSize,
                                       @Parameter(description = "станица")
                                       @RequestParam(value = "page_number", defaultValue = "0") int pageNumber) {
        return ResponseEntity.ok().body(vsListDao.getList(pageSize, pageNumber));
    }

    @PostMapping(value = "/api/vs/add_va")
    @Operation(summary = "Добавление ВС обмена")
    public ResponseEntity<?> addNewVs(@Parameter(description = "Root Tag") @RequestParam(name = "root_tag", required = true) String rootTag,
                                      @Parameter(description = "Мнемоника отправителья/получателя") @RequestParam(name = "mnemonic", required = true) String mnemonic,
                                      @Parameter(description = "описание для мнемоники") @RequestParam(name = "mnemonicDesc", required = false) String mnemonicDesc,
                                      @Parameter(description = "описание ВС") @RequestParam(name = "vsDesc", required = false) String vsDesc) {
        try {
            return ResponseEntity.ok().body(vsListDao.addVs(rootTag, mnemonic, mnemonicDesc, vsDesc).getId());
        } catch (AppException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}

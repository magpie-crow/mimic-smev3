package ru.mimicsmev.dao;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class GetApiModel<T> {

    @Schema(description = "Массив mtom вложений")
    private List<Attach> attachments;
    @Schema(description = "Сущъность")
    private T entity;

    @Getter
    @Setter
    static class Attach {
        @Schema(description = "Имя файла mtom вложения")
        private String fileName;
        @Schema(description = "Base64 строка массива байт mtom вложения")
        private String base64ByteArray;
        @Schema(description = "строка xml mtom вложения")
        private String attachRow;
    }
}

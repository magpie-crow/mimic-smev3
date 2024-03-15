package ru.mimicsmev.dao;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class PostApiModel {
    @Schema(description = "Значение RootElementLocalName", requiredMode = Schema.RequiredMode.REQUIRED)
    private String rootTag;
    @Schema(description = "MessageID сообщения", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String msgId;
    @Schema(description = "Original MessageID сообщения", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String originalMsgId;
    @Schema(description = "Base64 строка xml primary content", requiredMode = Schema.RequiredMode.REQUIRED)
    private String content;
    @Schema(description = "Массив mtom вложений", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private List<Attach> attachments;

    @Getter
    @Setter
    public static class Attach {
        @Schema(description = "Имя файла mtom вложения", requiredMode = Schema.RequiredMode.REQUIRED)
        private String fileName;
        @Schema(description = "Base64 строка массива байт файла mtom вложения", requiredMode = Schema.RequiredMode.REQUIRED)
        private String base64ByteArray;
        @Schema(description = "Base64 строка массива байт строки mtom вложения", requiredMode = Schema.RequiredMode.REQUIRED)
        private String base64String;
    }
}

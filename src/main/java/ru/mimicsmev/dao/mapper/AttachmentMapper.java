package ru.mimicsmev.dao.mapper;

import jakarta.activation.DataHandler;
import jakarta.activation.DataSource;
import jakarta.mail.util.ByteArrayDataSource;
import lombok.SneakyThrows;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import org.springframework.util.StringUtils;
import ru.mimicsmev.dao.File;
import ru.mimicsmev.dao.PostApiModel;
import ru.mimicsmev.dao.entity.ReqAttachment;
import ru.mimicsmev.dao.entity.ReqType;
import ru.mimicsmev.exception.AppException;
import ru.mimicsmev.service.SignatureService;
import v1.AttachmentContentType;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static ru.mimicsmev.utils.Utils.createZipArchive;
import static ru.mimicsmev.utils.Utils.trimValue;

@Mapper
public interface AttachmentMapper {
    AttachmentMapper INSTANCE = Mappers.getMapper(AttachmentMapper.class);

    @Mapping(target = "attachRow", ignore = true)
    @Mapping(target = "createTimestamp", ignore = true)
    @Mapping(target = "attachName", expression = "java(attachmentContentType.getId())")
    @Mapping(target = "attachBlob", expression = "java(getByteFromDataHandler(attachmentContentType.getContent()))")
    @Mapping(target = "refId", source = "refId")
    @Mapping(target = "reqType", source = "reqType")
    @Mapping(target = "id", ignore = true)
    ReqAttachment map(AttachmentContentType attachmentContentType, Long refId, ReqType reqType);

    @Mapping(target = "id", expression = "java(reqAttachment.getAttachName() + \".zip\")")
    @Mapping(target = "content", expression = "java(createDataHandler(reqAttachment.getAttachBlob()))")
    AttachmentContentType map(ReqAttachment reqAttachment);

    default DataHandler createDataHandler(byte[] bos) {
        DataSource source = new ByteArrayDataSource(bos, "application/zip");
        return new DataHandler(source);
    }

    @SneakyThrows
    default byte[] getByteFromDataHandler(DataHandler d) {
        return d.getInputStream().readAllBytes();
    }

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createTimestamp", ignore = true)
    @Mapping(target = "attachName", expression = "java(ru.mimicsmev.utils.Utils.trimValue(attach.getFileName()))")
    @Mapping(target = "refId", source = "refId")
    @Mapping(target = "reqType", source = "reqType")
    @Mapping(target = "attachRow", expression = "java(base64StringDecode(attach.getBase64String()))")
    @Mapping(target = "attachBlob", expression = "java(createBlob(attach.getBase64String(), attach.getBase64ByteArray(), attach.getFileName()))")
    ReqAttachment map(PostApiModel.Attach attach, Long refId, ReqType reqType);
    @SneakyThrows
    default String base64StringDecode(String string) {
        try {
            if (StringUtils.hasLength(trimValue(string))) {
                return new String(Base64.getDecoder().decode(string), StandardCharsets.UTF_8);
            } else {
                return null;
            }
        } catch (Exception e) {
            throw new AppException("Unable convert base64String to String", e);
        }
    }
    @SneakyThrows
    default byte[] base64ByteArrayDecode(String string) {
        try {
            if (StringUtils.hasLength(string)) {
                return Base64.getDecoder().decode(string);
            } else {
                return null;
            }
        } catch (Exception e) {
            throw new AppException("Unable convert base64String to String", e);
        }
    }
    @SneakyThrows
    default byte[] createBlob(String base64String, String base64Blob, String fileName) {
        if (StringUtils.hasLength(trimValue(base64Blob))) {
            return base64ByteArrayDecode(trimValue(base64Blob));
        } else if (StringUtils.hasLength(trimValue(base64String))) {
            return createAttachBlobFromRow(base64String, fileName);
        } else {
            throw new AppException(String.format("attachment '%s' hase empty body", fileName));
        }
    }
    @SneakyThrows
    default byte[] createAttachBlobFromRow(String base64String, String fileName) {
        String attachRow = base64StringDecode(base64String);
        if (StringUtils.hasLength(attachRow)) {
            byte[] fileSignature = SignatureService.sigAttach(attachRow.getBytes());
            File fileAttach = File.builder().fileName(fileName + ".xml").body(attachRow.getBytes()).build();
            File sigFileAttach = File.builder().fileName(fileName + ".sig").body(fileSignature).build();
            return createZipArchive(fileAttach, sigFileAttach);
        } else {
            return null;
        }
    }
}

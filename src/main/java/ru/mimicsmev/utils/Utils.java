package ru.mimicsmev.utils;

import ru.mimicsmev.dao.File;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class Utils {

    public static byte[] createZipArchive(File... files) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ZipOutputStream zipOut = new ZipOutputStream(outputStream);
        for (File file : files) {
            ZipEntry zipEntry = new ZipEntry(file.getFileName());
            zipOut.putNextEntry(zipEntry);

            zipOut.write(file.getBody(), 0, file.getBody().length);
        }
        zipOut.close();
        outputStream.close();
        return outputStream.toByteArray();
    }

    public static byte[] appendFileInZipFile(byte[] zip, byte[] file, String filename) throws IOException {
        ByteArrayInputStream arrayInputStream = new ByteArrayInputStream(zip);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ZipInputStream zipInputStream = new ZipInputStream(arrayInputStream);
        ZipOutputStream zipOutputStream = new ZipOutputStream(outputStream);

        ZipEntry zipEntryInput = zipInputStream.getNextEntry();
        byte[] buffer = new byte[1024];
        while (zipEntryInput != null) {
            ZipEntry zipEntry = new ZipEntry(zipEntryInput.getName());
            zipOutputStream.putNextEntry(zipEntry);
            int len;
            while ((len = zipInputStream.read(buffer)) > 0) {
                zipOutputStream.write(buffer);
            }
            zipEntryInput = zipInputStream.getNextEntry();
        }

        ZipEntry zipEntry = new ZipEntry(filename);
        zipOutputStream.putNextEntry(zipEntry);
        zipOutputStream.write(file, 0, file.length);

        zipOutputStream.close();
        outputStream.close();
        return outputStream.toByteArray();
    }

    public static String trimValue(String value) {
        return Objects.nonNull(value) ? value.trim() : null;
    }
}

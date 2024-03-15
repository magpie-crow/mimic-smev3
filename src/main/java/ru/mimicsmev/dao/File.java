package ru.mimicsmev.dao;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class File {
    String fileName;
    byte[] body;
}

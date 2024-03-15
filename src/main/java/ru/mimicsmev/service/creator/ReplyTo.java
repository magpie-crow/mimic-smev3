package ru.mimicsmev.service.creator;

import lombok.Builder;

@Builder
public class ReplyTo {
    private int sid;
    private String mid;
    private int eol;
    private String slc;
    private String mnm;
}

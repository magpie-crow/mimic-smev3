package ru.mimicsmev.dao.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "req_attachments")
public class ReqAttachment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "ref_id")
    private Long refId;

    @Column(name = "req_type")
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    private ReqType reqType;

    @Column(name = "attach_blob")
    private byte[] attachBlob;

    @Column(name = "attach_row")
    private String attachRow;

    @Column(name = "attach_name", length = Integer.MAX_VALUE)
    private String attachName;

    @Column(name = "create_timestamp")
    @CreationTimestamp
    private LocalDateTime createTimestamp;

}
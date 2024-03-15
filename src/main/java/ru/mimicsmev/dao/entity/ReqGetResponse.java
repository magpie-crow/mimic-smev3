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
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "req_get_response")
public class ReqGetResponse {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "ref_id")
    private Long refId;

    @Column(name = "create_timestamp")
    @CreationTimestamp
    private LocalDateTime createTimestamp;

    @Column(name = "root_tag", length = Integer.MAX_VALUE)
    private String rootTag;

    @Column(name = "msg_id", length = Integer.MAX_VALUE)
    private String msgId;

    @Column(name = "original_msg_id", length = Integer.MAX_VALUE)
    private String originalMsgId;

    @Column(name = "content", length = Integer.MAX_VALUE)
    private String content;

    @Column(name = "update")
    @UpdateTimestamp
    private LocalDateTime update;

    @Column(name = "req_row", length = Integer.MAX_VALUE)
    private String reqRow;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    private ReqStatus reqStatus;
}
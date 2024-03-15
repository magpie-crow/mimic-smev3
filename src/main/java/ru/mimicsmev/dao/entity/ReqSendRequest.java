package ru.mimicsmev.dao.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "req_send_request")
public class ReqSendRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "create_timestamp")
    @CreationTimestamp
    private LocalDateTime createTimestamp;

    @Column(name = "root_tag", length = Integer.MAX_VALUE)
    private String rootTag;

    @Column(name = "msg_id", length = Integer.MAX_VALUE)
    private String msgId;

    @Column(name = "response_msg_id", length = Integer.MAX_VALUE)
    private String responseMsgId;

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
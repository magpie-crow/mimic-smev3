package ru.mimicsmev.dao.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Getter
@Setter
@Table(name = "vs_list")
@ToString
public class VsList {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    @Column(name = "root_tag")
    private String rootTag;
    @Column(name = "mnemonic")
    private String mnemonic;
    @Column(name = "mnemonic_desc")
    private String mnemonicDesc;
    @Column(name = "description")
    private String description;
}

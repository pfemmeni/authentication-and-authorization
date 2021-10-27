package com.example.demo;

import lombok.*;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@ToString
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table
public class SaltEntity {
    @Id
    String token;

    @Column(length=1000)
    String salt;

}

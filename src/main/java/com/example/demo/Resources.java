package com.example.demo;

import lombok.ToString;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.UUID;
@ToString
@Entity
@Table
@Setter
@Getter
@NoArgsConstructor
public class Resources {
    @Id
    String id;
    @Column
    Resource resource;
    @Column
    Rights rights;

    public Resources(String id, Resource resource, Rights rights) {
        this.id = id;
        this.resource = resource;
        this.rights = rights;
    }
}

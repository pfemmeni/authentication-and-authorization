package com.example.demo;

import lombok.*;
import lombok.experimental.FieldNameConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.*;
import java.util.List;
import java.util.stream.Collectors;

@Entity
@ToString
@Getter
@Setter
@NoArgsConstructor
@Table
public class PersonEntity {
    @Id
    String token;
    @Column
    String name;
    @Column
    String password;
    @OneToMany
    @Column
    List<Resources> resources;


    @ElementCollection
    List<Rights> rightsList;

    private static final Logger log = LoggerFactory.getLogger(PersonService.class);

    public void addResouces(Resources newResource) {
        resources.add(newResource);
    }

    public PersonEntity(String token, String name, String password) {
        this.token = token;
        this.name = name;
        this.password = password;
    }

    public List<Rights> getRightsForResource(Resource resource) throws UseException {
        List<Resources> re = resources.stream().filter(r -> r.getResource().equals(resource)).collect(Collectors.toList());
        if (re.isEmpty()) {
            throw new UseException(Activity.VERIFY_RIGHTS, UseExceptionType.RESOURCE_NOT_FOUND);
        }

        for (Resources r : re) {
            log.info(name + "____" + r.getRights());
            rightsList.add(r.getRights());
        }

        return rightsList;

    }
}

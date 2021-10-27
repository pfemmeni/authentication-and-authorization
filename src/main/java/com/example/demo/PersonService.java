package com.example.demo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class PersonService {
    @Autowired
    PersonRepository personRepository;
    @Autowired
    SaltRepository saltRepository;

    private static final Logger log = LoggerFactory.getLogger(PersonService.class);

    public String createPerson(String name, String password) {
        String token = UUID.randomUUID().toString();
        String salt = PasswordUtils.generateSalt(512).get();

        Person person = new Person(token, name, hashPassword(password, salt).get());
        Salt saltToSave = new Salt(token, salt);

        personRepository.save(person);
        saltRepository.save(saltToSave);
        return person.getToken();
    }

    public boolean verifyLogIn(String name, String password, String token) {
        String salt = saltRepository.getById(token).getSalt();

        String hashedPasswordToCheck = hashPassword(password, salt).get();
        Optional<Person> personToVerify = personRepository.findAll().stream()
                .filter(person -> person.getName().equals(name)
                        && person.getPassword().equals(hashedPasswordToCheck)
                        && person.getToken().equals(token)).findFirst();
        return personToVerify.isPresent();
    }

    Optional<String> hashPassword(String password, String salt) {
        return PasswordUtils.hashPassword(password, salt);
    }
}

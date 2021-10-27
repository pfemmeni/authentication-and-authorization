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

        PersonEntity personEntity = new PersonEntity(token, name, hashPassword(password, salt));
        SaltEntity saltEntityToSave = new SaltEntity(token, salt);

        personRepository.save(personEntity);
        saltRepository.save(saltEntityToSave);
        return personEntity.getToken();
    }

    public String verifyLogIn(String name, String password, String token) throws UseException {
        String salt = saltRepository.getById(token).getSalt();
        String hashedPasswordToVerify = hashPassword(password, salt);

        PersonEntity personEntityToVerify = personRepository.findById(token)
                .orElseThrow(() -> new UseException(Activity.VERIFY_PERSON, UseExceptionType.PERSON_NOT_FOUND));

        if (personEntityToVerify.getName().equals(name) && !personEntityToVerify.getPassword().equals(hashedPasswordToVerify)) {
            throw new UseException(Activity.VERIFY_PERSON, UseExceptionType.WRONG_PASSWORD);
        }
        return personEntityToVerify.getToken();
    }

   String hashPassword(String password, String salt) {
        return PasswordUtils.hashPassword(password, salt).get();
    }

    public boolean verifyToken(String token){
        personRepository.getById(token);
        return false;

    }
}

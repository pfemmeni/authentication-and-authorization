package com.example.demo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class PersonService {
    @Autowired
    PersonRepository personRepository;
    @Autowired
    SaltRepository saltRepository;
    @Autowired
    ResourceRepository resourceRepository;

    private static final Logger log = LoggerFactory.getLogger(PersonService.class);

    public PersonEntity createPerson(String name, String password) {
        String token = UUID.randomUUID().toString();
        String salt = PasswordUtils.generateSalt(512).get();

        PersonEntity personEntity = new PersonEntity(token, name, hashPassword(password, salt));
        SaltEntity saltEntityToSave = new SaltEntity(token, salt);

        personRepository.save(personEntity);
        saltRepository.save(saltEntityToSave);
        return personEntity;
    }

    public String verifyLogIn(String name, String password, String token) throws UseException {
        PersonEntity personEntityToVerify = personRepository.findById(token)
                .orElseThrow(() -> new UseException(Activity.VERIFY_PERSON, UseExceptionType.PERSON_NOT_FOUND));

        String salt = saltRepository.getById(token).getSalt();
        String hashedPasswordToVerify = hashPassword(password, salt);


        if (personEntityToVerify.getName().equals(name) && !personEntityToVerify.getPassword().equals(hashedPasswordToVerify)) {
            throw new UseException(Activity.VERIFY_PASSWORD, UseExceptionType.WRONG_PASSWORD);
        }
        if (!personEntityToVerify.getName().equals(name) && personEntityToVerify.getPassword().equals(hashedPasswordToVerify)) {
            throw new UseException(Activity.VERIFY_USERNAME, UseExceptionType.WRONG_USERNAME);
        }
        return personEntityToVerify.getToken();
    }

    String hashPassword(String password, String salt) {
        return PasswordUtils.hashPassword(password, salt).get();
    }

    public boolean verifyToken(String token) {
        List<PersonEntity> persons = personRepository.findAll().stream()
                .filter(person -> person.getToken().equals(token))
                .collect(Collectors.toList());
        if (persons.size() == 0) {
            return false;
        }
        return true;
    }

    public void addResourceAndRights(String token, Resource resource, Rights rights){
        PersonEntity person = personRepository.getById(token);
        Resources resources = new Resources(UUID.randomUUID().toString(), resource,rights);
        resourceRepository.save(resources);
        person.addResouces(resources);
        personRepository.save(person);
    }

    public List<Rights> getRightsForResourceByToken(String token, Resource resource) throws UseException {
        return personRepository.getById(token).getRightsForResource(resource);

    }
}

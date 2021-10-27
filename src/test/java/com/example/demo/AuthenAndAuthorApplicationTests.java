package com.example.demo;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class AuthenAndAuthorApplicationTests {

    @Autowired
    PersonService personService;

    @Autowired
    PersonRepository personRepository;

    @Test
    void createPerson() {
        //When
        String annaToken = personService.createPerson("anna", "losen");
        String beritToken = personService.createPerson("berit", "123456");
        String kalleToken = personService.createPerson("kalle", "password");

        //Then
        assertEquals(3, personRepository.findAll().size());
        assertNotNull(annaToken);
        assertNotNull(beritToken);
        assertNotNull(kalleToken);
    }

    @Test
    void verifyLogInSuccess() {
        //Given
        String annaToken = personService.createPerson("anna", "losen");
        String beritToken = personService.createPerson("berit", "123456");
        String kalleToken = personService.createPerson("kalle", "password");

        //When
        boolean anna = personService.verifyLogIn("anna", "losen", annaToken);
        boolean berit = personService.verifyLogIn("berit", "123456", beritToken);
        boolean kalle = personService.verifyLogIn("kalle", "password", kalleToken);
        Person annaPerson = personRepository.getById(annaToken);

        //Then
        assertTrue(anna);
        assertTrue(berit);
        assertTrue(kalle);
        assertNotEquals("losen", annaPerson.getPassword());
    }


}

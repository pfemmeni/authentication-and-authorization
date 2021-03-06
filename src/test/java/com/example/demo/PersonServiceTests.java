package com.example.demo;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class PersonServiceTests {


    @Autowired
    PersonService personService;
    @Autowired
    PersonRepository personRepository;
    @Autowired
    SaltRepository saltRepository;
    @Autowired
    ResourceRepository resourceRepository;

    String annaToken;
    String beritToken;
    String kalleToken;

    @BeforeEach
    void setUp() {
        annaToken = personService.createPerson("anna", "losen", UUID.randomUUID().toString()).getToken();
        beritToken = personService.createPerson("berit", "123456", UUID.randomUUID().toString()).getToken();
        kalleToken = personService.createPerson("kalle", "password", UUID.randomUUID().toString()).getToken();

    }

    @AfterEach
    void tearDown() {
        personRepository.deleteAll();
    }

    @Test
    void createPerson() {
        //Then
        assertEquals(3, personRepository.findAll().size());
        assertNotNull(annaToken);
        assertNotNull(beritToken);
        assertNotNull(kalleToken);
    }

    @Test
    void verifyLogInSuccess() throws UseException {
        //When
        String anna = personService.verifyLogIn("anna", "losen", annaToken);
        String berit = personService.verifyLogIn("berit", "123456", beritToken);
        String kalle = personService.verifyLogIn("kalle", "password", kalleToken);

        //Then
        assertEquals(annaToken, anna);
        assertEquals(beritToken, berit);
        assertEquals(kalleToken, kalle);
    }

    @Test
    void verifyLogInFail() throws UseException {
        //When
        UseException annaException = assertThrows(UseException.class, () -> {
            personService.verifyLogIn("anna", "losen", UUID.randomUUID().toString());
        });
        UseException beritException = assertThrows(UseException.class, () -> {
            personService.verifyLogIn("berit", "1234567", beritToken);
        });
        UseException kalleException = assertThrows(UseException.class, () -> {
            personService.verifyLogIn("kallee", "password", kalleToken);
        });

        //Then
        assertThat(annaException.getUserExceptionType(), is(UseExceptionType.PERSON_NOT_FOUND));
        assertThat(annaException.getActivity(), is(Activity.VERIFY_PERSON));
        assertThat(beritException.getUserExceptionType(), is(UseExceptionType.WRONG_PASSWORD));
        assertThat(beritException.getActivity(), is(Activity.VERIFY_PASSWORD));
        assertThat(kalleException.getUserExceptionType(), is(UseExceptionType.WRONG_USERNAME));
        assertThat(kalleException.getActivity(), is(Activity.VERIFY_USERNAME));
    }

    @Test
    void verifyTokenIsValidSuccess() {
        //When
        boolean anna = personService.verifyToken(annaToken);
        boolean berit = personService.verifyToken(beritToken);
        boolean kalle = personService.verifyToken(kalleToken);

        //Then
        assertTrue(anna);
        assertTrue(berit);
        assertTrue(kalle);
    }

    @Test
    void verifyTokenIsValidFail() {
        //When
        boolean anna = personService.verifyToken(UUID.randomUUID().toString());
        boolean berit = personService.verifyToken(UUID.randomUUID().toString());
        boolean kalle = personService.verifyToken(UUID.randomUUID().toString());

        //Then
        assertFalse(anna);
        assertFalse(berit);
        assertFalse(kalle);
    }

    @Test
    void verifyResourcesSuccess() throws UseException {
        //Given
        personService.addResourceAndRights(annaToken, Resource.ACCOUNT, Rights.READ);
        personService.addResourceAndRights(beritToken, Resource.ACCOUNT, Rights.READ);
        personService.addResourceAndRights(beritToken, Resource.ACCOUNT, Rights.WRITE);
        personService.addResourceAndRights(kalleToken, Resource.PROVISION_CALC, Rights.EXECUTE);

        //When
        List<Rights> annasRights = personService.getRightsForResourceByToken(annaToken, Resource.ACCOUNT);
        List<Rights> beritsRights = personService.getRightsForResourceByToken(beritToken, Resource.ACCOUNT);
        List<Rights> kalleRights = personService.getRightsForResourceByToken(kalleToken, Resource.PROVISION_CALC);

        //Then
        assertEquals(List.of(Rights.READ), annasRights);
        assertEquals(List.of(Rights.READ, Rights.WRITE), beritsRights);
        assertEquals(List.of(Rights.EXECUTE), kalleRights);
    }

    @Test
    void verifyResourcesFail() throws UseException {
        //Given
        personService.addResourceAndRights(beritToken, Resource.ACCOUNT, Rights.READ);
        personService.addResourceAndRights(beritToken, Resource.ACCOUNT, Rights.WRITE);


        //When
        UseException beritException = assertThrows(UseException.class, () -> {
            personService.getRightsForResourceByToken(beritToken, Resource.PROVISION_CALC);
        });
        UseException kalleException = assertThrows(UseException.class, () -> {
            personService.getRightsForResourceByToken(kalleToken, Resource.PROVISION_CALC);
        });


        //Then
        assertEquals(Activity.VERIFY_RIGHTS, beritException.getActivity());
        assertEquals(UseExceptionType.RESOURCE_NOT_FOUND, kalleException.getUserExceptionType());
    }

}

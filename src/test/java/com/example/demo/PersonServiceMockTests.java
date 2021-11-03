package com.example.demo;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyObject;
import static org.mockito.Mockito.when;

@SpringBootTest
class PersonServiceMockTests {


    @Autowired
    PersonService personService;

    @Autowired
    SaltRepository saltRepository;

    @Autowired
    ResourceRepository resourceRepository;

    @MockBean
    PersonRepository personRepository;


    String annaToken;
    String beritToken;
    String kalleToken;
    PersonEntity anna;
    PersonEntity berit;
    PersonEntity kalle;


    @BeforeEach
    void setUp() {
        anna = new PersonEntity(UUID.randomUUID().toString(), "anna", "losen");
        berit = new PersonEntity(UUID.randomUUID().toString(), "berit", "123456");
        kalle = new PersonEntity(UUID.randomUUID().toString(), "kalle", "password");

        when(personRepository.save(new PersonEntity(UUID.randomUUID().toString(), "anna", "losen"))).thenReturn(anna);
        when(personRepository.save(new PersonEntity(UUID.randomUUID().toString(), "berit", "123456"))).thenReturn(berit);
        when(personRepository.save(new PersonEntity(UUID.randomUUID().toString(), "kalle", "password"))).thenReturn(kalle);

        anna = personService.createPerson("anna", "losen");
        berit = personService.createPerson("berit", "123456");
        kalle = personService.createPerson("kalle", "password");

      /*  annaToken = anna.getToken();
        beritToken = berit.getToken();
        kalleToken = kalle.getToken();
*/
    }

    @Test
    void createPerson() {
        when(personRepository.findAll()).thenReturn(List.of(anna, kalle, berit));
        //Then
        assertEquals(3, personRepository.findAll().size());
        assertNotNull(anna.getToken());
        assertNotNull(berit.getToken());
        assertNotNull(kalle.getToken());

    }

    @Test
    @Transactional
    void verifyLogInSuccess() throws UseException {
        //Given
        when(personRepository.findById(anna.getToken())).thenReturn(Optional.ofNullable(anna));
        when(personRepository.findById(berit.getToken())).thenReturn(Optional.ofNullable(berit));
        when(personRepository.findById(kalle.getToken())).thenReturn(Optional.ofNullable(kalle));

        //When
        String annaToken = personService.verifyLogIn("anna", "losen", anna.getToken());
        String beritToken = personService.verifyLogIn("berit", "123456", berit.getToken());
        String kalleToken = personService.verifyLogIn("kalle", "password", kalle.getToken());

        //Then
        assertEquals(anna.getToken(),annaToken);
        assertEquals(berit.getToken(), beritToken);
        assertEquals(kalle.getToken(), kalleToken);
    }

    @Test
    void verifyLogInFail() throws UseException {
        //Given
        when(personRepository.findById(anna.getToken())).thenReturn(Optional.ofNullable(null));
        when(personRepository.findById(berit.getToken())).thenReturn(Optional.ofNullable(berit));
        when(personRepository.findById(kalle.getToken())).thenReturn(Optional.ofNullable(kalle));

        //When
        UseException annaException = assertThrows(UseException.class, () -> {
            personService.verifyLogIn("anna", "losen", UUID.randomUUID().toString());
        });
        UseException beritException = assertThrows(UseException.class, () -> {
            personService.verifyLogIn("berit", "1234567", berit.getToken());
        });
        UseException kalleException = assertThrows(UseException.class, () -> {
            personService.verifyLogIn("kallee", "password", kalle.getToken());
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
        //Given
        when(personRepository.findAll()).thenReturn(List.of(anna, kalle, berit));

        //When
        boolean annaVerified = personService.verifyToken(anna.getToken());
        boolean beritVerified = personService.verifyToken(berit.getToken());
        boolean kalleVerified = personService.verifyToken(kalle.getToken());

        //Then
        assertTrue(annaVerified);
        assertTrue(beritVerified);
        assertTrue(kalleVerified);
    }

    @Test
    void verifyTokenIsValidFail() {
        //Given
        when(personRepository.findAll()).thenReturn(List.of(anna, berit, kalle));

        //When
        boolean annaVerified = personService.verifyToken(UUID.randomUUID().toString());
        boolean beritVerified = personService.verifyToken(UUID.randomUUID().toString());
        boolean kalleVerified = personService.verifyToken(UUID.randomUUID().toString());

        //Then
        assertFalse(annaVerified);
        assertFalse(beritVerified);
        assertFalse(kalleVerified);
    }

    @Test
    void verifyResourcesSuccess() throws UseException {
        //Given
        when(personRepository.getById(anna.getToken())).thenReturn(anna);
        when(personRepository.getById(berit.getToken())).thenReturn(berit);
        when(personRepository.getById(kalle.getToken())).thenReturn(kalle);

        PersonEntity annaWithResources = new PersonEntity(anna.getToken(),
                anna.getName(), anna.getPassword(), List.of(new Resources(UUID.randomUUID().toString(), Resource.ACCOUNT, Rights.READ)));
        when(personRepository.save(anna)).thenReturn(annaWithResources);
        personService.addResourceAndRights(anna.getToken(), Resource.ACCOUNT, Rights.READ);


        PersonEntity beritWithResources = new PersonEntity(berit.getToken(),
                berit.getName(), berit.getPassword(), List.of(new Resources(UUID.randomUUID().toString(), Resource.ACCOUNT, Rights.READ),
                new Resources(UUID.randomUUID().toString(), Resource.ACCOUNT, Rights.WRITE)));
        when(personRepository.save(berit)).thenReturn(beritWithResources);
        personService.addResourceAndRights(berit.getToken(), Resource.ACCOUNT, Rights.READ);
        personService.addResourceAndRights(berit.getToken(), Resource.ACCOUNT, Rights.WRITE);

        PersonEntity kalleWithResources = new PersonEntity(kalle.getToken(),
                kalle.getName(), kalle.getPassword(),
                List.of(new Resources(UUID.randomUUID().toString(), Resource.PROVISION_CALC, Rights.EXECUTE)));
        when(personRepository.save(kalle)).thenReturn(kalleWithResources);
        personService.addResourceAndRights(kalle.getToken(), Resource.PROVISION_CALC, Rights.EXECUTE);

        //When
        List<Rights> annasRights = personService.getRightsForResourceByToken(anna.getToken(), Resource.ACCOUNT);
        List<Rights> beritsRights = personService.getRightsForResourceByToken(berit.getToken(), Resource.ACCOUNT);
        List<Rights> kallesRights = personService.getRightsForResourceByToken(kalle.getToken(), Resource.PROVISION_CALC);

        //Then
        assertEquals(List.of(Rights.READ), annasRights);
        assertEquals(List.of(Rights.READ, Rights.WRITE), beritsRights);
        assertEquals(List.of(Rights.EXECUTE), kallesRights);
    }

    @Test
    void verifyResourcesFail() throws UseException {
        //Given
        when(personRepository.getById(berit.getToken())).thenReturn(berit);
        when(personRepository.getById(kalle.getToken())).thenReturn(kalle);

        PersonEntity beritWithResources = new PersonEntity(berit.getToken(),
                berit.getName(),
                berit.getPassword(),
                List.of(new Resources(UUID.randomUUID().toString(), Resource.ACCOUNT, Rights.READ)));
        when(personRepository.save(berit)).thenReturn(beritWithResources);
        personService.addResourceAndRights(berit.getToken(), Resource.ACCOUNT, Rights.READ);

        PersonEntity kalleWithResources = new PersonEntity(kalle.getToken(),
                kalle.getName(), kalle.getPassword(),
                List.of(new Resources(UUID.randomUUID().toString(), Resource.ACCOUNT, Rights.WRITE)));
        when(personRepository.save(kalle)).thenReturn(kalleWithResources);
        personService.addResourceAndRights(kalle.getToken(), Resource.ACCOUNT, Rights.WRITE);


        //When
        UseException beritException = assertThrows(UseException.class, () -> {
            personService.getRightsForResourceByToken(berit.getToken(), Resource.PROVISION_CALC);
        });
        UseException kalleException = assertThrows(UseException.class, () -> {
            personService.getRightsForResourceByToken(kalle.getToken(), Resource.PROVISION_CALC);
        });


        //Then
        assertEquals(Activity.VERIFY_RIGHTS, beritException.getActivity());
        assertEquals(UseExceptionType.RESOURCE_NOT_FOUND, kalleException.getUserExceptionType());
    }

}

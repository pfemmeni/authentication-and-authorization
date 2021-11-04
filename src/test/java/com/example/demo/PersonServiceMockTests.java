package com.example.demo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.arguments;
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

    PersonEntity anna;
    PersonEntity berit;
    PersonEntity kalle;


    @BeforeEach
    void setUp() {
        anna = new PersonEntity("1", "anna", "losen");
        berit = new PersonEntity("2", "berit", "123456");
        kalle = new PersonEntity("3", "kalle", "password");

        when(personRepository.save(new PersonEntity("1", "anna", "losen"))).thenReturn(anna);
        when(personRepository.save(new PersonEntity("2", "berit", "123456"))).thenReturn(berit);
        when(personRepository.save(new PersonEntity("3", "kalle", "password"))).thenReturn(kalle);

        anna = personService.createPerson("anna", "losen", "1");
        berit = personService.createPerson("berit", "123456", "2");
        kalle = personService.createPerson("kalle", "password", "3");
    }


    @ParameterizedTest
    @MethodSource
    void verifyLogInSuccess(String token, String name, String password) throws UseException {
        //Given
        when(personRepository.findById("1")).thenReturn(Optional.ofNullable(anna));
        when(personRepository.findById("2")).thenReturn(Optional.ofNullable(berit));
        when(personRepository.findById("3")).thenReturn(Optional.ofNullable(kalle));


        //When
        String personToken = personService.verifyLogIn(name, password, token);

        //Then
        if (name.equals("anna")) {
            assertEquals(anna.getToken(), personToken);
        }
        if (name.equals("berit")) {
            assertEquals(berit.getToken(), personToken);
        }
        if (name.equals("kalle")) {
            assertEquals(kalle.getToken(), personToken);
        }

    }


    @ParameterizedTest
    @MethodSource
    void verifyLogInFail(String token, String name, String password) throws UseException {
        //Given
        when(personRepository.findById("1")).thenReturn(Optional.ofNullable(null));
        when(personRepository.findById("2")).thenReturn(Optional.ofNullable(berit));
        when(personRepository.findById("3")).thenReturn(Optional.ofNullable(kalle));

        //When
        UseException personException = assertThrows(UseException.class, () -> {
            personService.verifyLogIn(name, password, token);
        });


        //Then
        if (name.equals("anna")) {
            assertThat(personException.getUserExceptionType(), is(UseExceptionType.PERSON_NOT_FOUND));
            assertThat(personException.getActivity(), is(Activity.VERIFY_PERSON));
        }
        if (name.equals("berit")) {
            assertThat(personException.getUserExceptionType(), is(UseExceptionType.WRONG_PASSWORD));
            assertThat(personException.getActivity(), is(Activity.VERIFY_PASSWORD));
        }
        if (name.equals("kallee")) {
            assertThat(personException.getUserExceptionType(), is(UseExceptionType.WRONG_USERNAME));
            assertThat(personException.getActivity(), is(Activity.VERIFY_USERNAME));
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"1", "2", "3"})
    void verifyTokenIsValidSuccess(String token) {
        //Given
        when(personRepository.findAll()).thenReturn(List.of(anna, kalle, berit));

        //Then
        assertTrue(personService.verifyToken(token));
    }

    @ParameterizedTest
    @ValueSource(strings = {"11", "22", "33"})
    void verifyTokenIsValidFail(String token) {
        //Given
        when(personRepository.findAll()).thenReturn(List.of(anna, berit, kalle));

        //Then
        assertFalse(personService.verifyToken(token));
    }


    @ParameterizedTest
    @MethodSource
    void verifyResourcesSuccess(PersonEntity personToVerify, Resource resource, List<Rights> rights) throws UseException {
        //Given
        when(personRepository.getById("1")).thenReturn(anna);
        when(personRepository.getById("2")).thenReturn(berit);
        when(personRepository.getById("3")).thenReturn(kalle);

        if (personToVerify.getName().equals("anna")) {
            when(personRepository.save(anna)).thenReturn(personToVerify);
            personService.addResourceAndRights(anna.getToken(), Resource.ACCOUNT, Rights.READ);

        }

        if (personToVerify.getName().equals("berit")) {
            when(personRepository.save(berit)).thenReturn(personToVerify);
            personService.addResourceAndRights(berit.getToken(), Resource.ACCOUNT, Rights.READ);
            personService.addResourceAndRights(berit.getToken(), Resource.ACCOUNT, Rights.WRITE);
        }

        if (personToVerify.getName().equals("kalle")) {
            when(personRepository.save(kalle)).thenReturn(personToVerify);
            personService.addResourceAndRights(kalle.getToken(), Resource.PROVISION_CALC, Rights.EXECUTE);
        }

        //When
        List<Rights> personRights = personService.getRightsForResourceByToken(personToVerify.getToken(), resource);

        //Then
        assertEquals(rights, personRights);
    }

    static Stream<Arguments> verifyLogInSuccess() {
        return Stream.of(
                arguments("1", "anna", "losen"),
                arguments("2", "berit", "123456"),
                arguments("3", "kalle", "password")
        );
    }

    static Stream<Arguments> verifyLogInFail() {
        return Stream.of(
                arguments(UUID.randomUUID().toString(), "anna", "losen"),
                arguments("2", "berit", "1234567"),
                arguments("3", "kallee", "password")
        );
    }

    @ParameterizedTest
    @MethodSource
    void verifyResourcesFail(PersonEntity personToVerify, Resource resource, UseExceptionType type, Activity activity) throws UseException {
        //Given
        when(personRepository.getById(berit.getToken())).thenReturn(berit);
        when(personRepository.getById(kalle.getToken())).thenReturn(kalle);

        if (personToVerify.getName().equals("berit")) {
            when(personRepository.save(berit)).thenReturn(personToVerify);
            personService.addResourceAndRights(berit.getToken(), Resource.ACCOUNT, Rights.READ);
        }

        if (personToVerify.getName().equals("kalle")) {
            when(personRepository.save(kalle)).thenReturn(personToVerify);
            personService.addResourceAndRights(kalle.getToken(), Resource.ACCOUNT, Rights.WRITE);
        }

        //When
        UseException personException = assertThrows(UseException.class, () -> {
            personService.getRightsForResourceByToken(personToVerify.token, resource);
        });


        //Then
        assertEquals(activity, personException.getActivity());
        assertEquals(type, personException.getUserExceptionType());
    }

    static Stream<Arguments> verifyResourcesFail() {
        return Stream.of(
                arguments(new PersonEntity(
                                "2",
                                "berit",
                                "123456",
                                List.of(new Resources(UUID.randomUUID().toString(), Resource.ACCOUNT, Rights.READ))),
                        Resource.PROVISION_CALC,
                        UseExceptionType.RESOURCE_NOT_FOUND,
                        Activity.VERIFY_RIGHTS),
                arguments(new PersonEntity(
                                "3",
                                "kalle",
                                "password",
                                List.of(new Resources(UUID.randomUUID().toString(), Resource.ACCOUNT, Rights.WRITE))),
                        Resource.PROVISION_CALC,
                        UseExceptionType.RESOURCE_NOT_FOUND,
                        Activity.VERIFY_RIGHTS)
        );
    }

    static Stream<Arguments> verifyResourcesSuccess() {
        return Stream.of(
                arguments(new PersonEntity(
                                "1",
                                "anna",
                                "losen",
                                List.of(new Resources(UUID.randomUUID().toString(), Resource.ACCOUNT, Rights.READ))),
                        Resource.ACCOUNT,
                        List.of(Rights.READ)),
                arguments(new PersonEntity(
                                "2",
                                "berit",
                                "123456",
                                List.of(new Resources(UUID.randomUUID().toString(), Resource.ACCOUNT, Rights.READ),
                                        new Resources(UUID.randomUUID().toString(), Resource.ACCOUNT, Rights.WRITE))),
                        Resource.ACCOUNT,
                        List.of(Rights.READ, Rights.WRITE)),
                arguments(new PersonEntity(
                                "3",
                                "kalle",
                                "password",
                                List.of(new Resources(UUID.randomUUID().toString(), Resource.PROVISION_CALC, Rights.EXECUTE))),
                        Resource.PROVISION_CALC,
                        List.of(Rights.EXECUTE))
        );
    }
}

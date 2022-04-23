package com.example.messengerbyesya;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.Mockito;


import com.example.messengerbyesya.model.User;
import com.example.messengerbyesya.services.Validation;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {

    private static List<User> users = new ArrayList<>();
    private static Validation validation;
    private static Validation mockValidation;

    @BeforeAll
    public static void setUp() {

        //validation = Mockito.mock(Validation.class);
        validation = new Validation();
        mockValidation = Mockito.mock(Validation.class);
        Mockito.when(mockValidation.isGoodPasswordLength(Mockito.anyString())).thenReturn(true);
        Mockito.when(mockValidation.isPasswordsEquals(Mockito.anyString(), Mockito.anyString())).thenReturn(true);

        User tempUser = new User();
        tempUser.setEmail("qwe@mail.com");
        users.add(tempUser);
        User tempUser1 = new User();
        tempUser1.setEmail("qwe1@mail.com");
        users.add(tempUser1);
        User tempUser2 = new User();
        tempUser2.setEmail("qwe2@mail.com");
        users.add(tempUser2);

        validation.setUsers(users);
        // Mockito.when(validation.getUsers()).thenReturn(users);


        // Validation.setup();
    }

    @Test
    public void testBasePath1() {
        Assertions.assertFalse(validation.fullValidation("qwe", "Aa123456", "Aa123456"));
    }

    @Test
    public void testBasePath2() {
        Assertions.assertFalse(validation.fullValidation("qwe@mail.com", "Aa123456", "Aa123456789"));
    }

    @Test
    public void testBasePath3() {
        Assertions.assertFalse(validation.fullValidation("qwe@mail.com", "123", "123"));
    }

    @Test
    public void testBasePath4() {
        Assertions.assertFalse(validation.fullValidation("qwe@mail.com", "ЫЫЫЫЫЫЫ", "ЫЫЫЫЫЫЫ"));
    }

    @Test
    public void testBasePath5() {
        Assertions.assertFalse(validation.fullValidation("qwe@mail.com", "qwerty", "qwerty"));
    }

    @Test
    public void testBasePath6() {
        Assertions.assertFalse(validation.fullValidation("qwe@mail.com", "QWERTY", "QWERTY"));
    }

    @Test
    public void testBasePath7() {
        Assertions.assertFalse(validation.fullValidation("qwe@mail.com", "123456", "123456"));
    }

    @Test
    public void testBasePath8() {
        Assertions.assertFalse(validation.fullValidation("qwe@mail.com", "@_@_@_@", "@_@_@_@"));
    }

    @Test
    public void testBasePath9() {
        Assertions.assertTrue(validation.fullValidation("qwe@mail.com", "Aa123__", "Aa123__"));
    }

    @Test
    public void testBlackBox1() {
        Assertions.assertFalse(validation.fullValidation(null, "Aa123__", "Aa123__"));
    }

    @Test
    public void testBlackBox2() {
        Assertions.assertFalse(validation.fullValidation("qwe", "Aa123__", "Aa123__"));
    }

    @Test
    public void testBlackBox3() {
        Assertions.assertFalse(validation.fullValidation("qwe@mail.com", null, "Aa123__"));
    }

    @Test
    public void testBlackBox4() {
        Assertions.assertFalse(validation.fullValidation("qwe@mail.com", "Aa123__", null));
    }

    @Test
    public void testBlackBox5() {
        Assertions.assertTrue(validation.fullValidation("qwe@mail.com", "Aa123__", "Aa123__"));
    }

    @Test
    public void testBlackBox6() {
        Assertions.assertFalse(validation.fullValidation("qwe@mail.com", "1", "1"));
    }

    @Test
    public void testBlackBox7() {
        Assertions.assertFalse(validation.fullValidation("qwe@mail.com", "Aa123__", "456789Aa"));
    }

    @Test
    public void testBlackBox8() {
        Assertions.assertFalse(validation.fullValidation("qwe@mail.com", "12345678", "12345678"));
    }

    @Test
    public void testBasePath10() {
        Assertions.assertFalse(validation.fullValidation("qweewq@mail.com", "Aa123__", "Aa123__"));
    }

    @Nested
    public class TestWithOtherUsersList {
        @BeforeEach
        public void setUp() {

            users.clear();
            validation.setUsers(users);

        }

        @Test
        public void testBasePath11() {
            Assertions.assertFalse(validation.fullValidation("qwe@mail.com", "Aa123__", "Aa123__"));
        }
    }


    @Test
    public void testBasePathForName1() {
        Assertions.assertFalse(validation.isNameValid(null));
    }

    @Test
    public void testBasePathForName2() {
        Assertions.assertFalse(validation.isNameValid("Какое-тоИмяВОдноСлово"));
    }

    @Test
    public void testBasePathForName3() {
        Assertions.assertFalse(validation.isNameValid("первая Буква"));
    }

    @Test
    public void testBasePathForName4() {
        Assertions.assertFalse(validation.isNameValid("Вторая буква"));
    }

    @Test
    public void testBasePathForName5() {
        Assertions.assertFalse(validation.isNameValid("English Имя"));
    }

    @Test
    public void testBasePathForName6() {
        Assertions.assertTrue(validation.isNameValid("Правильное Имя"));
    }

    @Test
    public void integrationTestWithMoq() {
        mockValidation = Mockito.spy(validation);
        Mockito.when(mockValidation.isGoodPasswordLength(Mockito.anyString())).thenReturn(true);
        Mockito.when(mockValidation.isPasswordsEquals(Mockito.anyString(), Mockito.anyString())).thenReturn(true);
        Assertions.assertTrue(mockValidation.fullValidation("qwe@mail.com", "Aa123456", "Aa123456"));
    }

    @Test
    public void integrationTestWithoutMoq() {
        Assertions.assertTrue(validation.fullValidation("qwe@mail.com", "Aa123456", "Aa123456"));
    }

    @AfterAll
    public static void clean() {

    }
}
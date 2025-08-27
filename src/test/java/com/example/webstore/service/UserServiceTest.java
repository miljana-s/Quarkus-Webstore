package com.example.webstore.service;

import com.example.webstore.dto.UserDTO;
import com.example.webstore.model.User;
import io.quarkus.narayana.jta.QuarkusTransaction;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;
import org.mindrot.jbcrypt.BCrypt;

import static org.assertj.core.api.Assertions.*;

@QuarkusTest
class UserServiceTest {

    @Inject
    UserService userService;

    @Test
    void loginAndGet_validCredentials_returnsUser() {
        User u = User.find("username", "user").firstResult();
        assertThat(u).isNotNull();
        assertThat(userService.loginAndGet("user", "1234")).isNotNull();
    }

    @Test
    void loginAndGet_wrongPassword_returnsNull() {
        assertThat(userService.loginAndGet("user", "badpass")).isNull();
    }

    @Test
    void register_success_returnsNullAndPersists() {
        String err = userService.register(
                "Ana","Anić","ana","secret123","ana@example.com",
                "123456","Street 1","City"
        );
        assertThat(err).isNull();
        User ana = User.find("username","ana").firstResult();
        assertThat(ana).isNotNull();
        assertThat(BCrypt.checkpw("secret123", ana.password)).isTrue();
    }

    @Test
    void register_duplicateUsername_detected() {
        String err = userService.register(
                "Marko","Marković","user","secret123","newmail@example.com",
                null,null,null
        );
        assertThat(err).isEqualTo("Username already exists! Try a different one.");
    }

    @Test
    void register_duplicateEmail_detected() {
        String err = userService.register(
                "Mara","Marić","mara","secret123","customer@example.com",
                null,null,null
        );
        assertThat(err).isEqualTo("Email already exists! Try a different one.");
    }

    @Test
    void getUserById_happyPath() {
        User u = User.find("username","user").firstResult();
        var dto = userService.getUserById(u.id);
        assertThat(dto).isNotNull();
        assertThat(dto.email).isEqualTo("customer@example.com");
    }

    @Test
    void getUserById_notFound_throws404() {
        assertThatThrownBy(() -> userService.getUserById(99999L))
                .hasMessageContaining("User not found");
    }

    @Test
    void updateUser_whenChanged_returnsTrue_andPersists() {
        String err = userService.register(
                "Temp","User","upduser","pwd","upd@example.com",
                "000","Addr","City"
        );
        assertThat(err).isNull();
        User u = User.find("username","upduser").firstResult();
        assertThat(u).isNotNull();

        UserDTO dto = new UserDTO();
        dto.id = u.id;
        dto.firstName = "Temp-new";
        dto.lastName  = u.lastName;
        dto.email     = u.email;
        dto.phone     = "555-000";
        dto.address   = "New addr";
        dto.city      = "New city";

        boolean updated = userService.updateUser(dto);
        assertThat(updated).isTrue();

        User reloaded = QuarkusTransaction.requiringNew().call(() -> User.<User>findById(u.id));
        assertThat(reloaded.firstName).isEqualTo("Temp-new");
        assertThat(reloaded.phone).isEqualTo("555-000");
    }

    @Test
    void updateUser_whenNoChanges_returnsFalse() {
        String err = userService.register(
                "No","Change","nochg","pwd","nochg@example.com",
                null,null,null
        );
        assertThat(err).isNull();
        User u = User.find("username","nochg").firstResult();
        assertThat(u).isNotNull();

        UserDTO dto = new UserDTO();
        dto.id        = u.id;
        dto.firstName = u.firstName;
        dto.lastName  = u.lastName;
        dto.email     = u.email;
        dto.phone     = u.phone;
        dto.address   = u.address;
        dto.city      = u.city;

        boolean updated = userService.updateUser(dto);
        assertThat(updated).isFalse();
    }

    @Test
    void updateUser_notFound_throws404() {
        UserDTO dto = new UserDTO();
        dto.id = 123456L;
        dto.firstName = "X";
        dto.lastName = "Y";
        dto.email = "x@y.com";
        assertThatThrownBy(() -> userService.updateUser(dto))
                .hasMessageContaining("User not found");
    }
}

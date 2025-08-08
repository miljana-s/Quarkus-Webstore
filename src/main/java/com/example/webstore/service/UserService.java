package com.example.webstore.service;

import com.example.webstore.dto.*;
import com.example.webstore.model.*;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import org.mindrot.jbcrypt.BCrypt;

import java.util.List;
import java.util.Map;

@ApplicationScoped
public class UserService {


    public User loginAndGet(String username, String password) {
        User user = User.find("username", username).firstResult();
        if (user == null) return null;
        if (!org.mindrot.jbcrypt.BCrypt.checkpw(password, user.password)) return null;
        return user;
    }



    @Transactional
    public boolean register(String firstName, String lastName, String username, String password,
                            String email, String phone, String address, String city) {
        if (User.find("username", username).firstResult() != null) {
            return false;
        }

        User newUser = new User();
        newUser.firstName = firstName;
        newUser.lastName = lastName;
        newUser.username = username;


        newUser.password = BCrypt.hashpw(password, BCrypt.gensalt());

        newUser.email = email;
        newUser.phone = phone;
        newUser.address = address;
        newUser.city = city;

        Role defaultRole = Role.find("name", "CUSTOMER").firstResult();
        newUser.role = defaultRole;

        newUser.persist();
        return true;
    }

    public UserDTO getUserById(Long id) {
        User user = User.findById(id);
        if (user == null) {
            throw new WebApplicationException("User not found", 404);
        }
        UserDTO dto = new UserDTO();
        dto.id = user.id;
        dto.firstName = user.firstName;
        dto.lastName = user.lastName;
        dto.email = user.email;
        dto.phone = user.phone;
        dto.address = user.address;
        dto.city = user.city;
        return dto;
    }

    @Transactional
    public boolean updateUser(UserDTO dto) {
        User user = User.findById(dto.id);
        if (user == null) {
            throw new WebApplicationException("User not found", 404);
        }

        boolean changed = !(user.firstName.equals(dto.firstName)
                && user.lastName.equals(dto.lastName)
                && user.email.equals(dto.email)
                && user.phone.equals(dto.phone)
                && user.address.equals(dto.address)
                && user.city.equals(dto.city));

        if (changed) {
            user.firstName = dto.firstName;
            user.lastName = dto.lastName;
            user.email = dto.email;
            user.phone = dto.phone;
            user.address = dto.address;
            user.city = dto.city;
            return true;
        }

        return false;
    }



}

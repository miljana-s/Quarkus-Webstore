package com.example.webstore.service;

import com.example.webstore.dto.*;
import com.example.webstore.model.*;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.WebApplicationException;
import org.hibernate.exception.ConstraintViolationException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import io.quarkus.narayana.jta.QuarkusTransaction;

import java.util.Objects;


@ApplicationScoped
public class UserService {

    @PersistenceContext
    EntityManager em;


    public User loginAndGet(String username, String password) {
        User user = User.find("username", username).firstResult();
        if (user == null) return null;
        if (!org.mindrot.jbcrypt.BCrypt.checkpw(password, user.password)) return null;
        return user;
    }

//    public String register(String firstName, String lastName, String username, String password,
//                           String email, String phone, String address, String city, Long roleId) {
//
//        final String hashed = BCrypt.hashpw(password, BCrypt.gensalt(8)); // već imaš
//
//        try {
//            return QuarkusTransaction.requiringNew().call(() -> {
//                User u = new User();
//                u.firstName = firstName;
//                u.lastName  = lastName;
//                u.username  = username;
//                u.password  = hashed;
//                u.email     = email;
//                u.phone     = phone;
//                u.address   = address;
//                u.city      = city;
//                u.role      = em.getReference(Role.class, roleId != null ? roleId : 1L);
//
//                try {
//                    u.persist(); // bez pre-check SELECT-ova
//                    return null; // OK
//                } catch (Exception e) {
//                    if (isUniqueViolation(e)) {
//                        return "Username or email already exists! Try a different one.";
//                    }
//                    return "Registration failed. Please try again.";
//                }
//            });
//        } catch (Exception outer) {
//            return "Registration failed. Please try again.";
//        }
//    }


    public String register(String firstName, String lastName, String username, String password,
                           String email, String phone, String address, String city) {

        try {
            return QuarkusTransaction.requiringNew().call(() -> {

                if (User.find("username", username).firstResult() != null) {
                    return "Username already exists! Try a different one.";
                }
                if (User.find("email", email).firstResult() != null) {
                    return "Email already exists! Try a different one.";
                }

                User u = new User();
                u.firstName = firstName;
                u.lastName  = lastName;
                u.username  = username;
                u.password  = org.mindrot.jbcrypt.BCrypt.hashpw(password, org.mindrot.jbcrypt.BCrypt.gensalt());
                u.email     = email;
                u.phone     = phone;
                u.address   = address;
                u.city      = city;

                Role defaultRole = Role.find("name", "CUSTOMER").firstResult();
                u.role = defaultRole;

                try {
                    u.persistAndFlush();
                    return null;
                } catch (Exception e) {

                    if (isUniqueViolation(e)) {
                        return "Username or email already exists! Try a different one.";
                    }
                    return "Registration failed. Please try again.";
                }
            });
        } catch (Exception outer) {

            return "Registration failed. Please try again.";
        }
    }


    private boolean isUniqueViolation(Throwable t) {
        while (t != null) {
            if (t instanceof ConstraintViolationException ||
                    t instanceof java.sql.SQLIntegrityConstraintViolationException) {
                return true;
            }
            t = t.getCause();
        }
        return false;
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

        boolean changed = false;

        if (!Objects.equals(user.firstName, dto.firstName)) { user.firstName = dto.firstName; changed = true; }
        if (!Objects.equals(user.lastName,  dto.lastName))  { user.lastName  = dto.lastName;  changed = true; }
        if (!Objects.equals(user.email,     dto.email))     { user.email     = dto.email;     changed = true; }
        if (!Objects.equals(user.phone,     dto.phone))     { user.phone     = dto.phone;     changed = true; }
        if (!Objects.equals(user.address,   dto.address))   { user.address   = dto.address;   changed = true; }
        if (!Objects.equals(user.city,      dto.city))      { user.city      = dto.city;      changed = true; }

        return changed;
    }



}

    package com.example.webstore.model;

    import io.quarkus.hibernate.orm.panache.PanacheEntity;
    import jakarta.persistence.Entity;

    @Entity
    public class Role extends PanacheEntity {
        public String name;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

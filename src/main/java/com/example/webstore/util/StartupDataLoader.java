package com.example.webstore.util;

import com.example.webstore.model.*;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;
import org.mindrot.jbcrypt.BCrypt;

import io.quarkus.runtime.StartupEvent;

@ApplicationScoped
public class StartupDataLoader {

    @Inject
    EntityManager em;

    private static final Logger LOGGER = Logger.getLogger(StartupDataLoader.class);

    @Transactional
    public void onStart(@Observes StartupEvent event) {
        LOGGER.info("➡️ Pokretanje StartupDataLoader-a...");

        // Dodavanje rola
        Role customerRole = new Role();
        customerRole.name = "CUSTOMER";
        em.persist(customerRole);

        Role sellerRole = new Role();
        sellerRole.name = "SELLER";
        em.persist(sellerRole);

        // Dodavanje kategorija
        for (CategoryEnum categoryName : CategoryEnum.values()) {
            Category category = new Category();
            category.name = categoryName;
            em.persist(category);
        }

        // Dodavanje korisnika
        User customer = new User();
        customer.firstName = "John";
        customer.lastName = "Doe";
        customer.email = "customer@example.com";
        customer.username = "user";
        customer.password = BCrypt.hashpw("1234", BCrypt.gensalt());
        customer.role = customerRole;
        em.persist(customer);

        User seller = new User();
        seller.firstName = "Jane";
        seller.lastName = "Smith";
        seller.email = "seller@example.com";
        seller.username = "seller";
        seller.password = BCrypt.hashpw("1234", BCrypt.gensalt());
        seller.role = sellerRole;
        em.persist(seller);

        // Dodavanje proizvoda
        Product product1 = new Product();
        product1.name = "Winter Jacket";
        product1.price = 120.0;
        product1.quantity = 5;
        product1.image = "https://m.media-amazon.com/images/I/81rntI+0XHL._AC_UX679_.jpg";
        product1.category = getCategoryByEnum(CategoryEnum.JACKETS);
        em.persist(product1);

        Product product2 = new Product();
        product2.name = "Black T-shirt";
        product2.price = 20.0;
        product2.quantity = 15;
        product2.image = "https://chriscross.in/cdn/shop/files/ChrisCrossBlackCottonT-Shirt.jpg?v=1740994605&width=1000";
        product2.category = getCategoryByEnum(CategoryEnum.TSHIRTS);
        em.persist(product2);


    }

    private Category getCategoryByEnum(CategoryEnum categoryEnum) {
        return em.createQuery("SELECT c FROM Category c WHERE c.name = :name", Category.class)
                .setParameter("name", categoryEnum)
                .getSingleResult();
    }
}

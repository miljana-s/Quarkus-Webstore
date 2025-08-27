package com.example.webstore.util;

import com.example.webstore.model.*;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.jboss.logging.Logger;
import org.mindrot.jbcrypt.BCrypt;

import io.quarkus.runtime.StartupEvent;

@ApplicationScoped
public class StartupDataLoader {

    @Inject EntityManager em;
    private static final Logger LOGGER = Logger.getLogger(StartupDataLoader.class);

    @Transactional
    public void onStart(@Observes StartupEvent event) {
        LOGGER.info("➡️ Pokretanje StartupDataLoader-a...");

        Role customerRole = getOrCreateRole("CUSTOMER");
        Role sellerRole   = getOrCreateRole("SELLER");

        for (CategoryEnum ce : CategoryEnum.values()) {
            getOrCreateCategory(ce);
        }

        getOrCreateUser("customer@example.com", "user", "John", "Doe", "1234", customerRole);
        getOrCreateUser("seller@example.com", "seller", "Jane", "Smith", "1234", sellerRole);

        getOrCreateProduct(
                "Engraved Bracelet", 120.0, 5,
                "https://thesilverstore.com.au/cdn/shop/files/Engraved-Bar-Bracelet.jpg?v=1713750492",
                CategoryEnum.BRACELETS
        );

        getOrCreateProduct(
                "Anklet Tropic", 20.0, 15,
                "https://www.puravidabracelets.com/cdn/shop/files/10BRAK1001_TRPCV5.jpg?v=1682957078&width=1600",
                CategoryEnum.ANKLETS
        );


        getOrCreateProduct("Gold Zircon Ring",          120.0, 5,  "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcRYpgGuzfk2J3bix3XrdlPvfs2k8Mtp6A7RNA&s",            CategoryEnum.RINGS);
        getOrCreateProduct("Small Hoop Earrings",        20.0, 15, "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQdCfDtc-lffVIzg8lCPkKPa202ZiizYrcT5A&s",        CategoryEnum.EARRINGS);
        getOrCreateProduct("Classic Chain Necklace",     89.5, 15, "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcRhg57am3zWRruPL3yQ5vC3blDO5g-4SHdQIg&s",         CategoryEnum.NECKLACES);
        getOrCreateProduct("Round Pendant",             250.0, 4,  "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQj-d3FcJyg8eSR4HrgiawxcXt1D-emVGTvJg&s",          CategoryEnum.PENDANTS);
        getOrCreateProduct("Simple Anklet",              45.0, 30, "https://starkle.in/cdn/shop/files/web13_6e8a67b5-acb5-4a6b-8582-1fabd6eade3f.png?v=1698491301&width=2048",           CategoryEnum.ANKLETS);
        getOrCreateProduct("Chain Bracelet",             35.0, 25, "https://media.tiffany.com/is/image/Tiffany/EcomBrowseM/-tiffany-hardweardouble-link-bracelet-75224958_1086707_ED_M.jpg?defaultImage=NoImageAvailableInternal&",         CategoryEnum.BRACELETS);

        getOrCreateProduct("Minimalist Silver Ring",    180.0, 8,  "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcT1c61ATpuFw2eI88-A47cIhlh-Dv00_Qiehg&s",       CategoryEnum.RINGS);
        getOrCreateProduct("Large Hoop Earrings",        18.0, 25, "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcTzMCg3p42YYTOBRV702Kipve75wX2gzizsNQ&s",   CategoryEnum.EARRINGS);
        getOrCreateProduct("Gold Pendant Necklace",      95.0,  20, "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcTvgpJb1mRAX_8Sr95xqsIXPGuBlzwdVYMeag&s",   CategoryEnum.NECKLACES);
        getOrCreateProduct("Heart Pendant",             220.0, 6,  "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcST1IiMQ20ZlZf5ufqcZDpuMwf5KAooBUbNEA&s",    CategoryEnum.PENDANTS);
        getOrCreateProduct("Gold Anklet",                50.0, 28, "https://www.truesilver.co.in/cdn/shop/files/AN129217S10ISL-1_3670b95c-b282-4030-84cf-ff00ceb31c02.jpg?v=1745659203",      CategoryEnum.ANKLETS);
        getOrCreateProduct("Bangle Bracelet",            37.0, 23, "https://static.bloom-boutique.co.uk/media/catalog/product/f/r/freshwater-pearl-and-chain-double-heart-bracelet-05.jpg?width=90&height=90&store=default&image-type=image",  CategoryEnum.BRACELETS);

        getOrCreateProduct("Diamond Solitaire Ring",     90.0, 12, "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcTX_9YpsNXDy8p0REhfq078HxUe6n8uoU_oZw&s",     CategoryEnum.RINGS);
        getOrCreateProduct("Stud Earrings",              22.0, 20, "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQIDyd1Y261NJUALcPezC2mcVxjGpTMQXgjhg&s",   CategoryEnum.EARRINGS);
        getOrCreateProduct("Sterling Silver Necklace",   70.0,  25, "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcTMa98ZbaqDBB6heVmFU-BVtN_wsSOeffU71Q&s", CategoryEnum.NECKLACES);
        getOrCreateProduct("Cross Pendant",             300.0, 3,  "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcTEs_h03jJ1esTbEh0NHyRDiG1V0BQJr4RW4g&s",    CategoryEnum.PENDANTS);
        getOrCreateProduct("Silver Anklet",              55.0, 22, "https://www.puravidabracelets.com/cdn/shop/files/10BRAK1001_TRPCV5.jpg?v=1682957078&width=1600",    CategoryEnum.ANKLETS);
        getOrCreateProduct("Cuff Bracelet",              40.0, 18, "https://thesilverstore.com.au/cdn/shop/files/Engraved-Bar-Bracelet.jpg?v=1713750492",    CategoryEnum.BRACELETS);

        getOrCreateProduct("Classic Engagement Ring",   110.0, 10, "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcRYpgGuzfk2J3bix3XrdlPvfs2k8Mtp6A7RNA&s",    CategoryEnum.RINGS);
        getOrCreateProduct("Drop Earrings",              19.0, 30, "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQdCfDtc-lffVIzg8lCPkKPa202ZiizYrcT5A&s",   CategoryEnum.EARRINGS);
        getOrCreateProduct("Pearl Strand Necklace",     120.0,  8,  "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcRhg57am3zWRruPL3yQ5vC3blDO5g-4SHdQIg&s",  CategoryEnum.NECKLACES);
        getOrCreateProduct("Initial Letter Pendant",    270.0, 5,  "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQj-d3FcJyg8eSR4HrgiawxcXt1D-emVGTvJg&s",  CategoryEnum.PENDANTS);
        getOrCreateProduct("Beaded Anklet",              60.0, 20, "https://starkle.in/cdn/shop/files/web13_6e8a67b5-acb5-4a6b-8582-1fabd6eade3f.png?v=1698491301&width=2048",    CategoryEnum.ANKLETS);
        getOrCreateProduct("Charm Bracelet",             38.0, 20, "https://media.tiffany.com/is/image/Tiffany/EcomBrowseM/-tiffany-hardweardouble-link-bracelet-75224958_1086707_ED_M.jpg?defaultImage=NoImageAvailableInternal&",   CategoryEnum.BRACELETS);

        getOrCreateProduct("Gemstone Halo Ring",         75.0, 15, "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcT1c61ATpuFw2eI88-A47cIhlh-Dv00_Qiehg&s", CategoryEnum.RINGS);
        getOrCreateProduct("Dangle Earrings",            21.0, 17, "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcTzMCg3p42YYTOBRV702Kipve75wX2gzizsNQ&s", CategoryEnum.EARRINGS);
        getOrCreateProduct("Minimalist Bar Necklace",    35.0,  18, "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcTvgpJb1mRAX_8Sr95xqsIXPGuBlzwdVYMeag&s",CategoryEnum.NECKLACES);
        getOrCreateProduct("Locket Pendant",            280.0, 4,  "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcST1IiMQ20ZlZf5ufqcZDpuMwf5KAooBUbNEA&s",           CategoryEnum.PENDANTS);
        getOrCreateProduct("Charm Anklet",               58.0, 18, "https://www.truesilver.co.in/cdn/shop/files/AN129217S10ISL-1_3670b95c-b282-4030-84cf-ff00ceb31c02.jpg?v=1745659203",     CategoryEnum.ANKLETS);
        getOrCreateProduct("Tennis Bracelet",            36.0, 22, "https://static.bloom-boutique.co.uk/media/catalog/product/f/r/freshwater-pearl-and-chain-double-heart-bracelet-05.jpg?width=90&height=90&store=default&image-type=image",  CategoryEnum.BRACELETS);

        getOrCreateProduct("Stacking Ring Set (3)",     130.0, 6,  "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcRYpgGuzfk2J3bix3XrdlPvfs2k8Mtp6A7RNA&s",   CategoryEnum.RINGS);
        getOrCreateProduct("Pearl Earrings",             25.0, 14, "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQdCfDtc-lffVIzg8lCPkKPa202ZiizYrcT5A&s",  CategoryEnum.EARRINGS);
        getOrCreateProduct("Choker Necklace",            60.0,  12, "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcTMa98ZbaqDBB6heVmFU-BVtN_wsSOeffU71Q&s", CategoryEnum.NECKLACES);
        getOrCreateProduct("Bar Pendant",               260.0, 7,  "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQj-d3FcJyg8eSR4HrgiawxcXt1D-emVGTvJg&s",      CategoryEnum.PENDANTS);
        getOrCreateProduct("Seashell Anklet",            42.0, 25, "https://www.puravidabracelets.com/cdn/shop/files/10BRAK1001_TRPCV5.jpg?v=1682957078&width=1600",  CategoryEnum.ANKLETS);
        getOrCreateProduct("Beaded Bracelet",            42.0, 16, "https://thesilverstore.com.au/cdn/shop/files/Engraved-Bar-Bracelet.jpg?v=1713750492",  CategoryEnum.BRACELETS);

        getOrCreateProduct("Opal Stone Ring",            85.0, 20, "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcTX_9YpsNXDy8p0REhfq078HxUe6n8uoU_oZw&s",     CategoryEnum.RINGS);
        getOrCreateProduct("Gold-Plated Earrings",       27.0, 10, "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQIDyd1Y261NJUALcPezC2mcVxjGpTMQXgjhg&s",   CategoryEnum.EARRINGS);
        getOrCreateProduct("Layered Necklace Set",       55.0,  14, "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcTvgpJb1mRAX_8Sr95xqsIXPGuBlzwdVYMeag&s",CategoryEnum.NECKLACES);
        getOrCreateProduct("Teardrop Pendant",          320.0, 2,  "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcTEs_h03jJ1esTbEh0NHyRDiG1V0BQJr4RW4g&s", CategoryEnum.PENDANTS);
        getOrCreateProduct("Minimalist Anklet",          53.0, 17, "https://starkle.in/cdn/shop/files/web13_6e8a67b5-acb5-4a6b-8582-1fabd6eade3f.png?v=1698491301&width=2048",CategoryEnum.ANKLETS);
        getOrCreateProduct("Gold Bracelet",              45.0, 14, "https://static.bloom-boutique.co.uk/media/catalog/product/f/r/freshwater-pearl-and-chain-double-heart-bracelet-05.jpg?width=90&height=90&store=default&image-type=image",    CategoryEnum.BRACELETS);

        getOrCreateProduct("Heart-Shaped Ring",          95.0, 18, "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcT1c61ATpuFw2eI88-A47cIhlh-Dv00_Qiehg&s",  CategoryEnum.RINGS);
        getOrCreateProduct("Sterling Silver Earrings",   24.0, 22, "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQdCfDtc-lffVIzg8lCPkKPa202ZiizYrcT5A&s", CategoryEnum.EARRINGS);
        getOrCreateProduct("Gemstone Pendant Necklace",  40.0,  16, "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcRhg57am3zWRruPL3yQ5vC3blDO5g-4SHdQIg&s",CategoryEnum.NECKLACES);
        getOrCreateProduct("Gemstone Pendant",          230.0, 6,  "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcST1IiMQ20ZlZf5ufqcZDpuMwf5KAooBUbNEA&s", CategoryEnum.PENDANTS);
        getOrCreateProduct("Boho Anklet",                59.0, 16, "https://www.truesilver.co.in/cdn/shop/files/AN129217S10ISL-1_3670b95c-b282-4030-84cf-ff00ceb31c02.jpg?v=1745659203",      CategoryEnum.ANKLETS);
        getOrCreateProduct("Sterling Silver Bracelet",   39.0, 18, "https://media.tiffany.com/is/image/Tiffany/EcomBrowseM/-tiffany-hardweardouble-link-bracelet-75224958_1086707_ED_M.jpg?defaultImage=NoImageAvailableInternal&",  CategoryEnum.BRACELETS);

    }


    private Role getOrCreateRole(String name) {
        var list = em.createQuery("select r from Role r where r.name = :n", Role.class)
                .setParameter("n", name).setMaxResults(1).getResultList();
        if (!list.isEmpty()) return list.get(0);
        Role r = new Role(); r.name = name; em.persist(r); return r;
    }

    private Category getOrCreateCategory(CategoryEnum categoryEnum) {
        var list = em.createQuery("select c from Category c where c.name = :name", Category.class)
                .setParameter("name", categoryEnum).setMaxResults(1).getResultList();
        if (!list.isEmpty()) return list.get(0);
        Category c = new Category(); c.name = categoryEnum; em.persist(c); return c;
    }

    private User getOrCreateUser(String email, String username, String first, String last, String rawPwd, Role role) {
        var list = em.createQuery("select u from User u where u.email = :e or u.username = :u", User.class)
                .setParameter("e", email).setParameter("u", username)
                .setMaxResults(1).getResultList();
        if (!list.isEmpty()) return list.get(0);
        User user = new User();
        user.firstName = first;
        user.lastName  = last;
        user.email     = email;
        user.username  = username;
        user.password  = BCrypt.hashpw(rawPwd, BCrypt.gensalt());
        user.role      = role;
        em.persist(user);
        return user;
    }

    private Product getOrCreateProduct(String name, double price, int qty, String imageUrl, CategoryEnum cat) {
        var list = em.createQuery("select p from Product p where p.name = :n", Product.class)
                .setParameter("n", name).setMaxResults(1).getResultList();
        if (!list.isEmpty()) return list.get(0);

        Product p = new Product();
        p.name = name;
        p.price = price;
        p.quantity = qty;
        p.image = imageUrl;
        p.category = getOrCreateCategory(cat);
        em.persist(p);
        return p;
    }


    private Category getCategoryByEnum(CategoryEnum categoryEnum) {
        var list = em.createQuery("select c from Category c where c.name = :name", Category.class)
                .setParameter("name", categoryEnum)
                .setMaxResults(1).getResultList();
        return list.isEmpty() ? null : list.get(0);
    }
}



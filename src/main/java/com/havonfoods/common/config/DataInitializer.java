package com.havonfoods.common.config;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.havonfoods.catalog.entity.BatchInventory;
import com.havonfoods.catalog.entity.Category;
import com.havonfoods.catalog.entity.Product;
import com.havonfoods.catalog.entity.ProductVariant;
import com.havonfoods.catalog.repository.BatchInventoryRepository;
import com.havonfoods.catalog.repository.CategoryRepository;
import com.havonfoods.catalog.repository.ProductRepository;
import com.havonfoods.catalog.repository.ProductVariantRepository;
import com.havonfoods.user.entity.Role;
import com.havonfoods.user.entity.User;
import com.havonfoods.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner{
	
	private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final ProductVariantRepository variantRepository;
    private final BatchInventoryRepository batchRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        seedAdminUser();
        seedCatalogData();
    }

    private void seedAdminUser() {
        if (!userRepository.existsByEmail("admin@havonfoods.com")) {
            User admin = User.builder()
                    .fullName("Havon Admin")
                    .email("admin@havonfoods.com")
                    .phoneNumber("9999999999")
                    .passwordHash(passwordEncoder.encode("Admin@Havon2026"))
                    .role(Role.ROLE_ADMIN)
                    .active(true)
                    .emailVerified(true)
                    .phoneVerified(true)
                    .build();
            userRepository.save(admin);
            log.info("Default Admin User seeded: admin@havonfoods.com / Admin@Havon2026");
        }
    }

    private void seedCatalogData() {
        if (categoryRepository.count() > 0) {
            return;
        }

        log.info("Seeding Havon Foods initial catalog and inventory...");

        // 1. Categories
        Category pickles = Category.builder()
                .name("Traditional Pickles")
                .slug("traditional-pickles")
                .description("Authentic homemade style Andhra and South Indian pickles prepared with cold-pressed oils.")
                .active(true)
                .build();

        Category chutneys = Category.builder()
                .name("Gourmet Chutneys & Podis")
                .slug("gourmet-chutneys-podis")
                .description("Slow-cooked traditional meal accompaniments and dry spice blends.")
                .active(true)
                .build();

        categoryRepository.saveAll(List.of(pickles, chutneys));

        // 2. Product: Avakaya Mango Pickle
        Product mangoPickle = Product.builder()
                .category(pickles)
                .name("Classic Cut Mango Pickle (Avakaya)")
                .slug("classic-cut-mango-pickle-avakaya")
                .description("Made with raw hand-picked Ramkela mangoes, crushed Guntur mustard seeds, and pure sesame oil.")
                .ingredients("Raw Mango, Cold-pressed Sesame Oil, Mustard, Red Chilli Powder, Salt, Fenugreek, Turmeric, Asafoetida")
                .spiceLevel("SPICY")
                .isVeg(true)
                .active(true)
                .build();
        productRepository.save(mangoPickle);

        // 3. Variants for Mango Pickle (250g and 500g Glass Jars)
        ProductVariant mango250g = ProductVariant.builder()
                .product(mangoPickle)
                .sku("HVN-MNG-250G")
                .weightInGrams(250)
                .packagingType("Glass Jar")
                .mrp(new BigDecimal("180.00"))
                .sellingPrice(new BigDecimal("160.00"))
                .active(true)
                .build();

        ProductVariant mango500g = ProductVariant.builder()
                .product(mangoPickle)
                .sku("HVN-MNG-500G")
                .weightInGrams(500)
                .packagingType("Glass Jar")
                .mrp(new BigDecimal("340.00"))
                .sellingPrice(new BigDecimal("299.00"))
                .active(true)
                .build();

        variantRepository.saveAll(List.of(mango250g, mango500g));

        // 4. Batches for Variants (FEFO Inventory)
        LocalDate today = LocalDate.now();

        BatchInventory batch250g = BatchInventory.builder()
                .variant(mango250g)
                .batchNumber("B2026-SEP-M250")
                .manufacturingDate(today.minusDays(5))
                .expiryDate(today.plusMonths(9))
                .quantityAvailable(60)
                .quantityReserved(0)
                .build();

        BatchInventory batch500g = BatchInventory.builder()
                .variant(mango500g)
                .batchNumber("B2026-SEP-M500")
                .manufacturingDate(today.minusDays(5))
                .expiryDate(today.plusMonths(9))
                .quantityAvailable(40)
                .quantityReserved(0)
                .build();

        batchRepository.saveAll(List.of(batch250g, batch500g));

        log.info("Catalog seeding complete: 2 Categories, 1 Product, 2 Variants, 2 Batches.");
    }
}

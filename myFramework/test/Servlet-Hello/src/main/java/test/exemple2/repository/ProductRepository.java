package test.example2.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import test.example2.entity.Product;

/**
 * Spring Data JPA génère l'implémentation automatiquement (CRUD complet :
 * save, findAll, findById, deleteById, ...).
 */
public interface ProductRepository extends JpaRepository<Product, Long> {
}

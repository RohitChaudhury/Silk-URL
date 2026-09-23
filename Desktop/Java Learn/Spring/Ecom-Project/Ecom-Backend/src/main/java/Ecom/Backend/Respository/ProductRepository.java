package Ecom.Backend.Respository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import Ecom.Backend.Model.Product;

@Repository
public interface ProductRepository extends JpaRepository<Product, Integer> {
}
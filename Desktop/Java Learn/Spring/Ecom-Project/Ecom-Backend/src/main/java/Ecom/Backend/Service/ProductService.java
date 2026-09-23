package Ecom.Backend.Service;

import java.util.List;

// import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import Ecom.Backend.Respository.ProductRepository;
import Ecom.Backend.Model.Product;

@Service
public class ProductService {
    private ProductRepository product;

    public ProductService(ProductRepository product) {
        this.product = product;
    }

    // method to fetch all the products in the table
    public List<Product> getProducts() {
        return product.findAll();
    }

    // method to add new Product in table
    public Product addProduct(Product newProduct) {
        return product.save(newProduct);
    }
}

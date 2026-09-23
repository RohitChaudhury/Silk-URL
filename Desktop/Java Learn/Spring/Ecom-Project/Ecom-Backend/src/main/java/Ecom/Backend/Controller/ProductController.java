package Ecom.Backend.Controller;

//import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import Ecom.Backend.Service.ProductService;
import Ecom.Backend.Model.Product;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:5173/") // local-host port origin of the react hosted application
public class ProductController {
    private ProductService prService;

    public ProductController(ProductService prService) {
        this.prService = prService;
    }

    // method to validate all the fields of a product object in a post request
    protected String validateFields(Product product) {
        if ((product.getName() == "" || product.getName() == null))
            return "Name";
        else if (product.getDesc() == "" || product.getDesc() == null)
            return "Desc";
        else if (product.getBrand() == "" || product.getBrand() == null)
            return "Brand";
        else if (product.getCategory() == "" || product.getCategory() == null)
            return "Category";
        else if (product.getPrice() <= 0.0)
            return "Price";
        else if (product.getQuantity() <= 0)
            return "Quantity";
        else if (product.isAvailable() == false)
            return "Available";
        else if (product.getReleaseDate() == null)
            return "ReleaseDate";
        return "Correct";
    }

    // Basic greeting on requesting the api root path
    @GetMapping("/")
    public String index() {
        return "Welcome to the Ecom-Project";
    }

    // method to get all the products
    @GetMapping("/products")
    public List<Product> getProducts() {
        return prService.getProducts();
    }

    // method to add a new produc
    @PostMapping("/add/product")
    public String addProduct(@RequestBody Product product) {
        if (this.validateFields(product) == "Name")
            return "Please Enter Name of the Product";
        if (this.validateFields(product) == "Desc")
            return "Please Enter Description of the Product";
        if (this.validateFields(product) == "Brand")
            return "Please Enter Brand of the Product";
        if (this.validateFields(product) == "Category")
            return "Please Enter Category of the Product";
        if (this.validateFields(product) == "Price")
            return "Please Enter Price of the Product";
        if (this.validateFields(product) == "Quantity")
            return "Please Enter Quantity of the Product";
        if (this.validateFields(product) == "Available")
            return "Availability of the Product can't be false";
        if (this.validateFields(product) == "ReleaseDate")
            return "Please Enter ReleaseDate of the Product";

        Product newProduct = prService.addProduct(product);
        return newProduct != null ? "New Product Added Successfully!" : "Failed to Add the Product!";
    }
}
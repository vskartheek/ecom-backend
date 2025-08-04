package com.ecommerce.project.service;
import com.ecommerce.project.exceptions.APIException;
import com.ecommerce.project.exceptions.ResourceNotFoundException;
import com.ecommerce.project.model.Cart;
import com.ecommerce.project.model.CartItem;
import com.ecommerce.project.model.Category;
import com.ecommerce.project.model.Product;
import com.ecommerce.project.payload.CartDTO;
import com.ecommerce.project.payload.ProductDTO;
import com.ecommerce.project.payload.ProductResponse;
import com.ecommerce.project.repositories.CartRepository;
import com.ecommerce.project.repositories.CategoryRepository;
import com.ecommerce.project.repositories.ProductRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
public class ProductServiceImpl implements ProductService{

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private FileService fileService;

    @Value("${project.image}")
    private String path;
    @Autowired
    private CartRepository cartRepository;
    @Autowired
    private CartService cartService;


    @Override
    public ProductDTO addProduct(Long categoryId, ProductDTO productDTO) {
        Category category=categoryRepository.findById(categoryId)
                .orElseThrow(()->
                        new ResourceNotFoundException("category","categoryId",categoryId));
        Product product=modelMapper.map(productDTO,Product.class);
        List<Product> products=category.getProducts();
        boolean isProductNotPresent=true;
        for (Product value : products) {
            if (value.getProductName().equals(product.getProductName())) {
                isProductNotPresent = false;
                break;
            }
        }
        if(isProductNotPresent) {
            product.setImage("default.img");
            product.setCategory(category);
            double specialPrice = product.getPrice() - ((product.getDiscount() / 100) * product.getPrice());
            product.setSpecialPrice(specialPrice);
            Product savedProduct = productRepository.save(product);
            return modelMapper.map(savedProduct, ProductDTO.class);
        }else{
            throw new APIException("Product already exists");
        }

    }

    @Override
    public ProductResponse getAllProducts(Integer pageNumber, Integer pageSize, String sortBy, String sortOrder) {
        Sort sortByAndOrder= sortOrder.equalsIgnoreCase("asc")?Sort.by(sortBy).ascending():Sort.by(sortBy).descending();
        Pageable pageDetails= PageRequest.of(pageNumber,pageSize,sortByAndOrder);
        Page<Product> pagedProducts=productRepository.findAll(pageDetails);
        List<Product> products=pagedProducts.getContent();
        if(products.isEmpty()){
            throw new APIException("No Products Exists!");
        }
        List<ProductDTO> productDTOS=products.stream().map((product)->modelMapper.map(product,ProductDTO.class)).toList();
        ProductResponse productResponse=new ProductResponse();
        productResponse.setContent(productDTOS);
        productResponse.setPageNumber(pagedProducts.getNumber());
        productResponse.setPageSize(pagedProducts.getSize());
        productResponse.setTotalPages(pagedProducts.getTotalPages());
        productResponse.setTotalElements(pagedProducts.getTotalElements());
        productResponse.setLastPage(pagedProducts.isLast());
        return productResponse;
    }

    @Override
    public ProductResponse searchByCategory(Long categoryId, Integer pageNumber, Integer pageSize, String sortBy, String sortOrder) {
        Category category=categoryRepository.findById(categoryId)
                .orElseThrow(()->
                        new ResourceNotFoundException("category","categoryId",categoryId));
        Sort sortByAndOrder= sortOrder.equalsIgnoreCase("asc")?Sort.by(sortBy).ascending():Sort.by(sortBy).descending();
        Pageable pageDetails= PageRequest.of(pageNumber,pageSize,sortByAndOrder);
        Page<Product> pagedProducts=productRepository.findByCategoryOrderByPriceAsc(category,pageDetails);
        List<Product> products=pagedProducts.getContent();
        if(products.isEmpty()){
            throw new APIException("No products exists with category "+category.getCategoryName());
        }
        List<ProductDTO> productDTOS=products.stream().map(product -> modelMapper.map(product,ProductDTO.class)).toList();
        ProductResponse productResponse=new ProductResponse();
        productResponse.setContent(productDTOS);
        productResponse.setPageNumber(pagedProducts.getNumber());
        productResponse.setPageSize(pagedProducts.getSize());
        productResponse.setTotalPages(pagedProducts.getTotalPages());
        productResponse.setTotalElements(pagedProducts.getTotalElements());
        productResponse.setLastPage(pagedProducts.isLast());
        return productResponse;
    }

    @Override
    public ProductResponse searchByKeyword(String keyword,Integer pageNumber, Integer pageSize, String sortBy, String sortOrder) {

        Sort sortByAndOrder= sortOrder.equalsIgnoreCase("asc")?Sort.by(sortBy).ascending():Sort.by(sortBy).descending();
        Pageable pageDetails= PageRequest.of(pageNumber,pageSize,sortByAndOrder);
        Page<Product> pagedProducts=productRepository.findByProductNameLikeIgnoreCase("%"+keyword+"%",pageDetails);
        List<Product> products=pagedProducts.getContent();
        List<ProductDTO> productDTOS=products.stream()
                 .map(product -> modelMapper.map(product,ProductDTO.class)).toList();
        if(products.isEmpty()){
            throw new APIException("Product not found with keyword "+keyword);
        }
        ProductResponse productResponse=new ProductResponse();
        productResponse.setContent(productDTOS);
        productResponse.setPageNumber(pagedProducts.getNumber());
        productResponse.setPageSize(pagedProducts.getSize());
        productResponse.setTotalPages(pagedProducts.getTotalPages());
        productResponse.setTotalElements(pagedProducts.getTotalElements());
        productResponse.setLastPage(pagedProducts.isLast());
        return productResponse;
    }

    @Override
    public ProductDTO updateProduct(ProductDTO productDTO, Long productId) {
        Product productToBeUpdated=productRepository.findById(productId)
                .orElseThrow(()->
                        new ResourceNotFoundException("product","productId",productId));
        productToBeUpdated.setProductName(productDTO.getProductName());
        productToBeUpdated.setDescription(productDTO.getDescription());
        productToBeUpdated.setQuantity(productDTO.getQuantity());
        productToBeUpdated.setPrice(productDTO.getPrice());
        productToBeUpdated.setDiscount(productDTO.getDiscount());
        double specialPrice=productDTO.getPrice()-((productDTO.getPrice()/100)*productDTO.getPrice());
        productToBeUpdated.setSpecialPrice(specialPrice);
        Product product=productRepository.save(productToBeUpdated);
        List<Cart> carts=cartRepository.findCartsByProductId(productId);
        List<CartDTO> cartDTOS=carts.stream().map(cart->{
            CartDTO cartDTO=modelMapper.map(cart,CartDTO.class);
            List<ProductDTO> productDTOS=cart.getCartItems().stream().map(
                    p->{
                        ProductDTO productDTO1=modelMapper.map(p.getProduct(),ProductDTO.class);
                        return productDTO1;
                    }
            ).toList();
        cartDTO.setProducts(productDTOS);
        return cartDTO;
        }).toList();
        cartDTOS.forEach(cart-> cartService.updateProductInCarts(cart.getCartId(),productId));
        return modelMapper.map(product,ProductDTO.class);
    }

    @Override
    public ProductDTO deleteProduct(Long productId) {
        Product product=productRepository.findById(productId)
                .orElseThrow(()->new ResourceNotFoundException("product","product Id",productId));
        List<Cart> carts=cartRepository.findCartsByProductId(productId);
        carts.forEach(cart->cartService.deleteProductFromCart(cart.getCartId(),productId));
        productRepository.deleteById(productId);
        return modelMapper.map(product,ProductDTO.class);
    }

    @Override
    public ProductDTO updateProductImage(Long productId, MultipartFile image) throws IOException {
        //get product from DB
        Product productFromDb=productRepository.findById(productId)
                .orElseThrow(()->
                        new ResourceNotFoundException("product","productId",productId));
        //upload image to server
        //get the file name of uploaded image
        String fileName=fileService.uploadImage(path,image);
        //updating the new file name to the product
        productFromDb.setImage(fileName);
        //save updated product
        Product updatedProduct=productRepository.save(productFromDb);
        //return DTO after mapping product to DTO
        return modelMapper.map(updatedProduct,ProductDTO.class);

    }


}

package com.ecommerce.project.service;

import com.ecommerce.project.exceptions.APIException;
import com.ecommerce.project.exceptions.ResourceNotFoundException;
import com.ecommerce.project.model.Cart;
import com.ecommerce.project.model.CartItem;
import com.ecommerce.project.model.Product;
import com.ecommerce.project.payload.CartDTO;
import com.ecommerce.project.payload.ProductDTO;
import com.ecommerce.project.repositories.CartItemRepository;
import com.ecommerce.project.repositories.CartRepository;
import com.ecommerce.project.repositories.ProductRepository;
import com.ecommerce.project.util.AuthUtil;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;


@Service
public class CartServiceImpl implements CartService{
    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private AuthUtil authUtil;
    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private ModelMapper modelMapper;


    @Override
    public CartDTO addProductToCart(Long productId, Integer quantity) {
        // find existing cart or create one
        Cart cart=createCart();
        // retrieve product details
        Product product=productRepository.findById(productId)
                .orElseThrow(()->new ResourceNotFoundException("product","productId",productId));
        //perform validation
        CartItem cartItem=cartItemRepository.findCartItemByProductIdAndCartId(
                cart.getCartId(),
                productId
        );
        if(cartItem !=null){
            throw new APIException("Product "+product.getProductName()+" already exists in the cart");
        }
        if(product.getQuantity()==0){
            throw new APIException(product.getProductName()+" is not available");
        }

        if(product.getQuantity()<quantity){
            throw new APIException("please make an order of the "+product.getProductName()
            +" less than or equal to the quantity "+product.getQuantity()+".");
        }


        //create cart item

        CartItem newCartItem=new CartItem();
        newCartItem.setProduct(product);
        newCartItem.setCart(cart);
        newCartItem.setQuantity(quantity);
        newCartItem.setDiscount(product.getDiscount());
        newCartItem.setProductPrice(product.getSpecialPrice());

        //save cart item
        cartItemRepository.save(newCartItem);
        product.setQuantity(product.getQuantity());
        cart.setTotalPrice(cart.getTotalPrice()+(product.getSpecialPrice()*quantity));
        cart.getCartItems().add(newCartItem);
        cartRepository.save(cart);

        Cart updatedCart = cartRepository.findById(cart.getCartId())
                .orElseThrow(() -> new ResourceNotFoundException("cart", "cartId", cart.getCartId()));
        CartDTO cartDTO=modelMapper.map(updatedCart,CartDTO.class);
        //return updated cart

        List<CartItem> cartItems=updatedCart.getCartItems();
        Stream<ProductDTO> productDTOStream=cartItems.stream().map(
                item->{
                    ProductDTO map=modelMapper.map(item.getProduct(),ProductDTO.class);
                    map.setQuantity(item.getQuantity());
                    return map;
                }
        );
        cartDTO.setProducts(productDTOStream.toList());
        return cartDTO;
    }

    @Override
    public List<CartDTO> getAllCarts() {
        List<Cart> carts=cartRepository.findAll();
        if(carts.size()==0){
            throw new APIException("No carts Exist");
        }
        Stream<CartDTO> cartDTOS=carts.stream().map(
                cart->{
                    CartDTO cartDTO=modelMapper.map(cart, CartDTO.class);
                    List<ProductDTO> products=cart.getCartItems().stream()
                                    .map(cartItem -> {
                                        ProductDTO productDTO=modelMapper.map(cartItem.getProduct(),ProductDTO.class);
                                        productDTO.setQuantity(cartItem.getQuantity());
                                        return productDTO;
                                    }).toList();
                    cartDTO.setProducts(products);
                    return cartDTO;
                }
        );
        return cartDTOS.toList();
    }

    @Override
    public CartDTO getCart(String emailId, Long cartId) {
        Cart cart=cartRepository.findCartByEmailAndCartId(emailId,cartId);
        if(cart==null){
            throw new APIException("Cart  with mail "+emailId+" cartId "+cartId);
        }
        CartDTO cartDTO=modelMapper.map(cart,CartDTO.class);
        cart.getCartItems().forEach(c->c.getProduct().setQuantity(c.getQuantity()));
        List<CartItem> cartItems=cart.getCartItems();
        List<ProductDTO> productDTOS=cartItems.stream().map(
                item->{
                    ProductDTO productDTO=modelMapper.map(item.getProduct(),ProductDTO.class);
                    return productDTO;
                }
        ).toList();
        cartDTO.setProducts(productDTOS);
        return cartDTO;
    }

    @Override
    @Transactional
    public CartDTO updateProductQuantityInCart(Long productId, Integer quantity) {

        String email=authUtil.loggedInEmail();
        Cart userCart=cartRepository.findCartByEmail(email);
        Long cartId=userCart.getCartId();
        Cart cart=cartRepository.findById(cartId)
                .orElseThrow(()->new ResourceNotFoundException("cart","cartId",cartId));
        Product product=productRepository.findById(productId)
                .orElseThrow(()->new ResourceNotFoundException("product","productId",productId));

        if(product.getQuantity()==0){
            throw new APIException(product.getProductName()+" is not available");
        }

        if(product.getQuantity()<quantity){
            throw new APIException("please make an order of the "+product.getProductName()
                    +" less than or equal to the quantity "+product.getQuantity()+".");
        }

        CartItem cartItem=cartItemRepository.findCartItemByProductIdAndCartId(productId,cartId);
        if(cartItem==null)
            throw new APIException("Product "+product.getProductName()+" not available in cart");

        int newQuantity=cartItem.getQuantity()+quantity;

        if(newQuantity<0){
            throw new APIException("Resulting quantity cannot be negative");
        }
        if(newQuantity==0){
            deleteProductFromCart(cartId,productId);
        }else {
            cartItem.setProductPrice(product.getSpecialPrice());
            cartItem.setQuantity(cartItem.getQuantity() + quantity);
            cartItem.setDiscount(cartItem.getDiscount());
            cart.setTotalPrice(cart.getTotalPrice() + (cartItem.getProductPrice() * quantity));
            cartRepository.save(cart);
        }
        CartItem updatedCartItem=cartItemRepository.save(cartItem);
        if(updatedCartItem.getQuantity()==0){
            cartItemRepository.deleteById(updatedCartItem.getCartItemId());
        }

        CartDTO cartDTO=modelMapper.map(cart,CartDTO.class);
        List<CartItem> cartItems=cart.getCartItems();
        Stream<ProductDTO> productStream=cartItems.stream().map(
                item->{
                    ProductDTO prd=modelMapper.map(item.getProduct(),ProductDTO.class);
                    prd.setQuantity(item.getQuantity());
                    return prd;
                }
        );
        cartDTO.setProducts(productStream.toList());
        return cartDTO;
    }

    @Transactional
    @Override
    public String deleteProductFromCart(Long cartId, Long productId) {
        Cart cart=cartRepository.findById(cartId)
                .orElseThrow(()->new ResourceNotFoundException("cart","cartId",cartId));
        CartItem cartItem=cartItemRepository.findCartItemByProductIdAndCartId(cartId,productId);

        if (cartItem==null)
            throw new ResourceNotFoundException("product","productId",productId);
        cart.setTotalPrice(cart.getTotalPrice()-
                (cartItem.getProductPrice()*cartItem.getQuantity()));
        cartItemRepository.deleteCartItemByProductIdAndCartId(productId,cartId);

        return "Product "+cartItem.getProduct().getProductName()+" is Deleted from cart";

    }

    @Override
    public void updateProductInCarts(Long cartId, Long productId) {
        Cart cart=cartRepository.findById(cartId)
                .orElseThrow(()->new ResourceNotFoundException("cart","cartId",cartId));
        Product product=productRepository.findById(productId)
                .orElseThrow(()->new ResourceNotFoundException("product","productId",productId));
        CartItem cartItem=cartItemRepository.findCartItemByProductIdAndCartId(productId,cartId);
        if(cartItem==null){
            throw new APIException("Product "+product.getProductName()+" doesn't  exist in the cart !!");
        }
        double cartPrice=cart.getTotalPrice()-(cartItem.getProductPrice()*cartItem.getQuantity());
        cartItem.setProductPrice(product.getSpecialPrice());
        cart.setTotalPrice(cartPrice);
        cart.setTotalPrice(cartPrice+(cartItem.getProductPrice()*cartItem.getQuantity()));
        cartItem=cartItemRepository.save(cartItem);
    }

    private Cart createCart(){
        Cart userCart=cartRepository.findCartByEmail(authUtil.loggedInEmail());
        if(userCart!=null){
            return userCart;
        }
        Cart cart=new Cart();
        cart.setTotalPrice(0.00);
        cart.setUser(authUtil.loggedInUser());
        Cart newCart=cartRepository.save(cart);
        return newCart;
    }
}

package com.example.demo.order;

import com.example.demo.domain.member.entity.Member;
import com.example.demo.domain.member.repository.MemberRepository;
import com.example.demo.domain.order.controller.CartRegisterRequest;
import com.example.demo.domain.order.entity.ProductCart;
import com.example.demo.domain.order.entity.items.ItemCategoryType;
import com.example.demo.domain.order.entity.items.ProductItem;
import com.example.demo.domain.order.repository.ProductCartRepository;
import com.example.demo.domain.order.repository.ProductItemRepository;
import com.example.demo.domain.products.entity.Product;
import com.example.demo.domain.products.repository.ProductsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Optional;

import static java.util.Objects.requireNonNull;

@SpringBootTest
@AutoConfigureMockMvc
public class CartTest {
    @Autowired
    private ProductCartRepository productCartRepository;

    @Autowired
    private ProductItemRepository productItemRepository;

    @Autowired
    private ProductsRepository productsRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Mock
    private ProductCartRepository mockProductCartRepository;

    @Mock
    private ProductItemRepository mockProductItemRepository;

    @Mock
    private ProductsRepository mockPproductsRepository;

    @Mock
    private MemberRepository mockMemberRepository;


    @BeforeEach
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }
    @Test
    private Member 멤버_아이디_확인(Long memberId){
        Optional<Member> maybeMember =
                memberRepository.findByMemberId(memberId);

        Member member = null;
        if(maybeMember.isPresent()) {
            return member = maybeMember.get();
        }

        System.out.println("존재하지 않은 회원입니다.");
        return null;
    }

    @Test
    private Product 상품_아이디_확인(Long productId ){

        // product 찾기
        Optional<Product> maybeProduct =
                productsRepository.findById(productId);
        if(maybeProduct.isPresent()){
            System.out.println("Product "+maybeProduct.get().getProductId()+" 번의 상품이 존재합니다.");

            return maybeProduct.get();
        }

        System.out.println("해당 Product 상품이 없습니다.");
        return null;
    }

    @Test
    public void 상품_카테고리_분류(){

        CartRegisterRequest item = new CartRegisterRequest(
                ItemCategoryType.PRODUCT,2L, 20, 1L
        );

//        Product testProduct =
//                new Product("단호박 샐러드", 6000L, "상상도 못하게 비싸군요!");
//        productsRepository.save(testProduct);
        Product testProduct =
                new Product("고구마 샐러드", 4000L, "정말 싸군요!");
        productsRepository.save(testProduct);

        Member member = requireNonNull(멤버_아이디_확인(item.getMemberId()));
        Product requestProduct = requireNonNull(상품_아이디_확인(item.getItemId()));

        // 회원이 보낸 장바구니 상품을 분류
        if (item.getItemCategoryType() == ItemCategoryType.PRODUCT) {
            // 카트가 존재하는지 확인
            Optional<ProductCart> memberCart =
                    productCartRepository.findByMember_memberId(member.getMemberId());

            if(memberCart.isEmpty()){
                새료운_카트생성(member, item, requestProduct);
            }
            System.out.println(member.getNickname()+" 님의 product 카트는 이미 생성되어 있습니다.");
            기존카트_아이템_추가(memberCart.get(), item, requestProduct);
        }

    }
    @Test
    private void 새료운_카트생성(Member member, CartRegisterRequest item, Product requestProduct ){

        ProductCart firstCart = ProductCart.builder().member(member).build();

        productCartRepository.save(firstCart);
        System.out.println(member.getNickname()+" 님의 product 카트를 생성하였습니다.");

        ProductItem newProductItem = item.toProductItem(requestProduct, firstCart);
        productItemRepository.save(newProductItem);

        System.out.println(member.getNickname()+" 님의 product 카트에 첫 상품을 추가하였습니다.");
    }

    @Test
    private void 기존카트_아이템_추가(ProductCart memberCart, CartRegisterRequest item, Product requestProduct) {

        // 회원의 카트와 상품의 아이디로 product item 찾기
        Optional<ProductItem> maybeProductItem =
                productItemRepository.findByProduct_productIdAndProductCart_Id(memberCart.getId(), item.getItemId());

        // product item 있다 = 카트에 해당 item 있다. = 수량 추가
        if (maybeProductItem.isPresent()) {
            ProductItem productItem = maybeProductItem.get();

            productItem.setQuantity(productItem.getQuantity() + item.getQuantity());
            productItemRepository.save(productItem);
            System.out.println(requestProduct.getTitle() + " 상품의 수량을 카트에 추가하였습니다.");

        } else {
        // product item 없다 = 카트에 없는 새로운 상품 = 카트에 담기
            ProductItem newProductItem = item.toProductItem(requestProduct, memberCart);

            productItemRepository.save(newProductItem);
            System.out.println(requestProduct.getTitle() + " 상품을 카트에 추가하였습니다.");
        }
    }

}
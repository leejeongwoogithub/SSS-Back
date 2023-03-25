package com.example.demo.domain.selfSalad.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@NoArgsConstructor
@Data
@AllArgsConstructor
public class Ingredient {
    /**
     * 재료분류(육류) > 재료(닭고기)

     * 재료 클래스
        * 재료 식별자(번호), 재료 이름(닭고기), 재료 사진(닭고기 사진), 재료 convert, 재료 수량

     * 재료 수량 클래스 : 최대/최소 gram-개

     * 재료 convert : 재료분류 이름(육류), 재료 단위(g)

     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long ingredientId;

    @ManyToOne(fetch = FetchType.LAZY, targetEntity = Category.class ,cascade = CascadeType.ALL)
    @JoinColumn(name="category_id")
    private Category category;

    @Column(nullable = false)
    private String name;

    @OneToOne(targetEntity = ImageResource.class ,cascade = CascadeType.ALL)
    private ImageResource imageResource;

    @OneToOne(targetEntity = Amount.class ,cascade = CascadeType.ALL)
    private Amount amount;


}

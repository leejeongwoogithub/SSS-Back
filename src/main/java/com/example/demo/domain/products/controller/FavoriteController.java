package com.example.demo.domain.products.controller;

import com.example.demo.domain.products.entity.Favorite;
import com.example.demo.domain.products.service.FavoriteService;
import com.example.demo.domain.products.service.request.FavoriteInfoRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/products/favorite")
@RequiredArgsConstructor
public class FavoriteController {

    final private FavoriteService favoriteService;

    @PostMapping("/changeLike")
    public Boolean changeLike(@RequestBody FavoriteInfoRequest request) {
        log.info("changeLike(): " + request);
        return favoriteService.changeLike(request);
    }

    @PostMapping("/likeStatus")
    public Boolean likeStatus(@RequestBody FavoriteInfoRequest request) {
        log.info("likeStatus(): " + request);
        return favoriteService.likeStatus(request);
    }

    @PostMapping("/myFavorite")
    public List<Favorite> favoriteList(@PathVariable("memberId") Long memberId) {
        return favoriteService.favoriteList(memberId);
    }
}
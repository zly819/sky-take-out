package com.sky.controller.user;

import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.ShoppingCart;
import com.sky.result.Result;
import com.sky.service.ShoppingCartService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author ZLY
 * @version 1.0
 */
@Api(tags = "购物车相关接口")
@Slf4j
@RequestMapping("/user/shoppingCart")
@RestController
public class ShoppingCartController {

    @Autowired
    private ShoppingCartService shoppingCartService;

    /**
     * 添加购物车
     * @param dto
     * @return
     */
    @ApiOperation("添加购物车")
    @PostMapping("/add")
    public Result addCart(@RequestBody ShoppingCartDTO dto){
        log.info("添加购物车：{}",dto);
        shoppingCartService.addCart(dto);
        return Result.success();
    }

    /**
     * 查看购物车
     * @return
     */
    @ApiOperation("查看购物车")
    @GetMapping("/list")
    public Result list(){
        log.info("查看购物车");
        List<ShoppingCart> list = shoppingCartService.list();
        return Result.success(list);
    }
}

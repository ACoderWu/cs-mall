package com.cskaoyan.gateway.controller.shopping;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.mall.commons.result.ResponseData;
import com.mall.commons.result.ResponseUtil;
import com.mall.shopping.ICartService;
import com.mall.shopping.dto.*;
import com.mall.user.intercepter.TokenIntercepter;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.dubbo.config.annotation.Reference;
import org.checkerframework.checker.units.qual.C;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

/**
 * @author ACoderWu
 * @version 1.0
 * @date 2020/5/12 星期二
 */

@RestController
@RequestMapping("/shopping")
@Api(tags = "CartController", description = "购物车控制层")
public class CartController {
    @Reference(timeout = 3000,check = false)
    ICartService iCartService;

    @GetMapping("/carts")
    @ApiOperation("获取购物车列表")
    public ResponseData<Object> getCartListById() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        assert attributes != null;
        HttpServletRequest servletRequest = attributes.getRequest();
        String userInfo = (String) servletRequest.getAttribute(TokenIntercepter.USER_INFO_KEY);
        JSONObject object = JSON.parseObject(userInfo);
        Long uid = Long.parseLong(object.get("uid").toString());
        CartListByIdRequest request = new CartListByIdRequest();
        request.setUserId(uid);
        CartListByIdResponse response = iCartService.getCartListById(request);
        return new ResponseUtil<>().setData(response.getCartProductDtos());
    }

    @PostMapping("/carts")
    @ApiOperation("添加商品到购物车")
    public ResponseData addToCart(@RequestBody AddCartRequest request) {
        AddCartResponse response = iCartService.addToCart(request);
        return new ResponseUtil<>().setData(response);
    }

    @PutMapping("/carts")
    @ApiOperation("更新购物车中商品的数量和选择状态")
    public ResponseData updateCart(@RequestBody UpdateCartNumRequest request) {
        UpdateCartNumResponse response = iCartService.updateCartNum(request);
        return new ResponseUtil<>().setData(response);
    }

    @PutMapping("/items")
    @ApiOperation("选择购物车中的所有商品")
    public ResponseData checkAllItem(@RequestBody CheckAllItemRequest request) {
        CheckAllItemResponse response = iCartService.checkAllCartItem(request);
        return new ResponseUtil<>().setData(response);
    }

    @DeleteMapping("/items/{uid}")
    @ApiOperation("删除购物车中选中的商品")
    public ResponseData deleteCartItem(@PathVariable Long uid) {
        DeleteCheckedItemRequest request = new DeleteCheckedItemRequest();
        request.setUserId(uid);
        DeleteCheckedItemResposne response = iCartService.deleteCheckedItem(request);
        return new ResponseUtil<>().setData(response);
    }

    @DeleteMapping("/carts/{uid}/{pid}")
    @ApiOperation("删除购物车中指定的商品")
    public ResponseData deleteCheckedItem(@PathVariable Long uid, @PathVariable Long pid) {
        DeleteCartItemRequest request = new DeleteCartItemRequest();
        request.setUserId(uid);
        request.setItemId(pid);
        DeleteCartItemResponse response = iCartService.deleteCartItem(request);
        return new ResponseUtil<>().setData(response);
    }

    @DeleteMapping("/cart/clear")
    @ApiOperation("购物车清空缓存（删除已下单项）")
    public ResponseData clearCartItem(@RequestBody ClearCartItemRequest request) {
        ClearCartItemResponse response = iCartService.clearCartItemByUserID(request);
        return new ResponseUtil<>().setData(response);
    }
}

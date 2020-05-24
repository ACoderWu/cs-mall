package com.mall.shopping.services;

import com.mall.shopping.ICartService;
import com.mall.shopping.constants.ShoppingRetCode;
import com.mall.shopping.dal.entitys.Item;
import com.mall.shopping.dal.persistence.ItemMapper;
import com.mall.shopping.dto.*;
import org.apache.dubbo.config.annotation.Service;
import org.redisson.api.RBucket;
import org.redisson.api.RList;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * @author ACoderWu
 * @version 1.0
 * @date 2020/5/12 星期二
 */

@Component
@Service
public class ICartServiceImpl implements ICartService {

    @Autowired
    private RedissonClient redissonClient;

    @Autowired
    private ItemMapper itemMapper;

    /**
     * 获得购物车商品列表
     *
     * @param request
     * @return
     */
    @Override
    public CartListByIdResponse getCartListById(CartListByIdRequest request) {
        List<CartProductDto> cartProductDtoList = getCartRedisList(request.getUserId());

        // 如果还没有购物车则新建
        if (cartProductDtoList == null) {
            String cartUserId = cartRedisKey(request.getUserId());
            RBucket<List<CartProductDto>> bucket = redissonClient.getBucket(cartUserId);
            cartProductDtoList = new ArrayList<>();
            bucket.set(cartProductDtoList);
        }

        CartListByIdResponse response = new CartListByIdResponse();
        response.setCartProductDtos(cartProductDtoList);

        return response;
    }

    /**
     * 添加商品到购物车
     *
     * @param request
     * @return
     */
    @Override
    public AddCartResponse addToCart(AddCartRequest request) {

        // 查询商品详情
        Item item = new Item();
        item.setId(request.getProductId());
        item = itemMapper.selectByPrimaryKey(item);

        //item 信息转换为 CartProductDto 信息
        CartProductDto cartProductDto = new CartProductDto();
        cartProductDto.setProductId(item.getId());
        cartProductDto.setChecked("true");
        cartProductDto.setLimitNum(Long.valueOf(item.getLimitNum()));
        cartProductDto.setProductName(item.getTitle());
        cartProductDto.setSalePrice(item.getPrice());
        cartProductDto.setProductImg(item.getImageBig());
        cartProductDto.setProductNum(Long.valueOf(request.getProductNum()));

        // 从 Redis 获取 当前用户的购物车，若没有购物车则新建
        RList<CartProductDto> cartProductDtoList = getCartRedisList(request.getUserId());
        if (cartProductDtoList == null) {
            String cartUserId = cartRedisKey(request.getUserId());
            RBucket<List<CartProductDto>> bucket = redissonClient.getBucket(cartUserId);
            bucket.set(new ArrayList<>());
            cartProductDtoList = redissonClient.getList(cartUserId);
        }

        // 添加商品到购物车
        boolean hasAdd = false;
        for (int index = 0; index < cartProductDtoList.size(); index++) {
            CartProductDto dto = cartProductDtoList.get(index);
            if (dto.getProductId().equals(cartProductDto.getProductId())) {
                Long lastProductNum = dto.getProductNum();
                dto.setProductNum(lastProductNum + cartProductDto.getProductNum());
                cartProductDtoList.set(index, dto);
                hasAdd = true;
                break;
            }
        }
        if (!hasAdd) {
            cartProductDtoList.add(cartProductDto);
        }

        return null;
    }

    /**
     * 更新购物车中商品的数量和状态
     *
     * @param request
     * @return
     */
    @Override
    public UpdateCartNumResponse updateCartNum(UpdateCartNumRequest request) {
        RList<CartProductDto> cartProductDtoList = getCartRedisList(request.getUserId());
        for (int index = 0; index < cartProductDtoList.size(); index++) {
            CartProductDto dto = cartProductDtoList.get(index);
            if (request.getProductId().equals(dto.getProductId())) {
                dto.setProductNum(request.getProductNum());
                dto.setChecked(request.getChecked());
                cartProductDtoList.set(index, dto);
            }
        }
        return null;
    }

    /**
     * 选择购物车中的所有商品
     *
     * @param request
     * @return
     */
    @Override
    public CheckAllItemResponse checkAllCartItem(CheckAllItemRequest request) {
        RList<CartProductDto> cartProductDtoList = getCartRedisList(request.getUserId());
        for (int index = 0; index < cartProductDtoList.size(); index++) {
            CartProductDto dto = cartProductDtoList.get(index);
            dto.setChecked(request.getChecked());
            cartProductDtoList.set(index, dto);
        }
        return null;
    }

    /**
     * 删除购物车中的商品
     *
     * @param request
     * @return
     */
    @Override
    public DeleteCartItemResponse deleteCartItem(DeleteCartItemRequest request) {
        RList<CartProductDto> cartProductDtoList = getCartRedisList(request.getUserId());
        if (request.getItemId() != null) {
            for (CartProductDto cartProductDto : cartProductDtoList) {
                if (cartProductDto.getProductId().equals(request.getItemId())) {
                    cartProductDtoList.remove(cartProductDto);
                }
            }
        } else {
            cartProductDtoList.clear();
        }
        return null;
    }

    /**
     * 删除选中的商品
     *
     * @param request
     * @return
     */
    @Override
    public DeleteCheckedItemResposne deleteCheckedItem(DeleteCheckedItemRequest request) {
        RList<CartProductDto> cartProductDtoList = getCartRedisList(request.getUserId());
        cartProductDtoList.removeIf(cartProduct -> "true".equals(cartProduct.getChecked()));
        return null;
    }

    /**
     * 清空指定用户的购物车缓存(用户下完订单之后清理）
     *
     * @param request
     * @return
     */
    @Override
    public ClearCartItemResponse clearCartItemByUserID(ClearCartItemRequest request) {
        ClearCartItemResponse response = new ClearCartItemResponse();
        List<Long> productIds = request.getProductIds();
        RList<CartProductDto> cartProductDtoList = getCartRedisList(request.getUserId());
        for (CartProductDto cartProductDto : cartProductDtoList) {
            for (Long pid : productIds) {
                if (pid.equals(cartProductDto.getProductId())) {
                    cartProductDtoList.remove(cartProductDto);
                    productIds.remove(pid);
                    break;
                }
            }
        }
        response.setCode(ShoppingRetCode.SUCCESS.getCode());
        response.setMsg(ShoppingRetCode.SUCCESS.getMessage());
        return response;
    }

    /**
     * 获取购物车List
     *
     * @param userId 传入用户id
     * @return
     */
    private RList<CartProductDto> getCartRedisList(Long userId) {
        String cartUserId = cartRedisKey(userId);
        return redissonClient.getList(cartUserId);
    }

    private String cartRedisKey(Long userId) {
        return "cart" + userId;
    }
}

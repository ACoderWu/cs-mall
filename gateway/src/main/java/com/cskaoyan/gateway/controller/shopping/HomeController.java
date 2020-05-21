package com.cskaoyan.gateway.controller.shopping;

import com.mall.commons.result.ResponseData;
import com.mall.commons.result.ResponseUtil;
import com.mall.shopping.IContentService;
import com.mall.shopping.IHomeService;
import com.mall.shopping.constants.ShoppingRetCode;
import com.mall.shopping.dto.HomePageResponse;
import com.mall.shopping.dto.NavListResponse;
import com.mall.user.annotation.Anoymous;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.dubbo.config.annotation.Reference;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author dengww
 * @create 2020/5/12 13:42
 */
@RestController
@RequestMapping("/shopping")
@Api(tags = "HomeController",description = "导航控制层")
public class HomeController {

    @Reference(timeout = 3000,check = false)
    IContentService contentService;

    @Reference(timeout = 3000,check = false)
    IHomeService iHomeService;

    @Anoymous
    @GetMapping("/navigation")
    @ApiOperation("导航")
    public ResponseData navigation(){
        NavListResponse response = contentService.queryNavList();
        if (response.getCode().equals(ShoppingRetCode.SUCCESS.getCode())){
            return new ResponseUtil().setData(response.getPannelContentDtos());
        }
        return new ResponseUtil().setErrorMsg(response.getMsg());
    }

    @Anoymous
    @GetMapping("/homepage")
    @ApiOperation("主页")
    public ResponseData homepage(){
        HomePageResponse response = iHomeService.homepage();
        if (response.getCode().equals(ShoppingRetCode.SUCCESS.getCode())){
            return new ResponseUtil().setData(response.getPanelContentItemDtos());
        }
        return new ResponseUtil().setErrorMsg(response.getMsg());
    }
}

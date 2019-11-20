package com.binron.wechatpay.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test")
public class TestController{
    @GetMapping("/hello1")
    public Object test1(){

        return "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n" +
                "<response>\n" +
                "\t<status>success</status>\n" +
                "<code>00</ code >\n" +
                "<desc>交易成功</desc>\n" +
                "<amount>29718</amount>  //交易总金额\n" +
                "<areaCode>330000</areaCode>  //省域代码\n" +
                "<bizOrderId>37088</bizOrderId>  //充值平台方流水号\n" +
                "<carrierType>3</carrierType>  //运营商类别\n" +
                "<itemFacePrice>30000</itemFacePrice>   //商品面值单位为厘\n" +
                "<itemId>136</itemId>   //商品编号\n" +
                "<itemName>全国移动话费3元</itemName>\n" +
                "<price>29718</price>  //商品单价\n" +
                "<serialno>123333</ serialno>   //合作方流水号\n" +
                "</response>";
    }
}

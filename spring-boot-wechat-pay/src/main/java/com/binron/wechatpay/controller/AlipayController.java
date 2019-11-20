package com.binron.wechatpay.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.AlipayTradePrecreateRequest;
import com.alipay.api.response.AlipayTradePrecreateResponse;
import com.binron.wechatpay.config.AlipayConfig;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * 订单接口
 */
@RestController
@RequestMapping("/api/aliPay/order")
public class AlipayController {
    @Autowired
    AlipayConfig alipayConfig;
    /**
     * 用户点击购买下单接口
     */
    @GetMapping("buy")
    public void saveOrder(HttpServletRequest request,
                          HttpServletResponse response) throws Exception {

        //调用支付宝预下单接口生成二维码
        AlipayClient alipayClient = new DefaultAlipayClient("https://openapi.alipay.com/gateway.do", "APP_ID, APP_PRIVATE_KEY", "json", "CHARSET", "ALIPAY_PUBLIC_KEY", "RSA2"); //获得初始化的AlipayClient
        AlipayTradePrecreateRequest requestAli = new AlipayTradePrecreateRequest();//创建API对应的request类
        //生成参数，用于调用预下单接口
        SortedMap<String,String> params = new TreeMap<>();
        params.put("out_trade_no","");//商户订单号，需要保证不重复
        params.put("total_amount", "");//订单金额
        params.put("subject", "");//订单标题
        params.put("store_id",alipayConfig.getStoreId());//商户门店编号
        params.put("timeout_express", alipayConfig.getTimeOutExpress());//交易超时时间
        requestAli.setBizContent(JSON.toJSONString(params));//
        AlipayTradePrecreateResponse responseAli = alipayClient.execute(requestAli);
        JSONObject jsonObject=JSONObject.parseObject(responseAli.getBody());

        String codeUrl=jsonObject.getJSONObject("alipay_trade_precreate_response").getString("qr_code");

        //3、通过google工具生成二维码供用户扫码支付
        try{
            //3、1生成二维码配置
            Map<EncodeHintType,Object> hints =  new HashMap<>();

            //3、2设置纠错等级
            hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L);

            //3、3编码类型
            hints.put(EncodeHintType.CHARACTER_SET,"UTF-8");

            BitMatrix bitMatrix = new MultiFormatWriter().encode(codeUrl, BarcodeFormat.QR_CODE,400,400,hints);
            OutputStream out =  response.getOutputStream();

            MatrixToImageWriter.writeToStream(bitMatrix,"png",out);

        }catch (Exception e){
            e.printStackTrace();
        }
    }
}

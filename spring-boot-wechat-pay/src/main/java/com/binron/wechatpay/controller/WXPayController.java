package com.binron.wechatpay.controller;

import com.binron.wechatpay.config.WeChatConfig;
import com.binron.wechatpay.utils.CommonUtils;
import com.binron.wechatpay.utils.HttpUtils;
import com.binron.wechatpay.utils.WXPayUtil;
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
import java.util.*;

/**
 * 订单接口
 */
@RestController
@RequestMapping("/api/v1/order")
public class WXPayController {

    @Autowired
    private WeChatConfig weChatConfig;

    /**
     * 用户点击购买下单接口
     */
    @GetMapping("buy")
    public void saveOrder(HttpServletRequest request,
                          HttpServletResponse response) throws Exception {

        /**
         * 实际开发需要获取用户id和用户当前ip，这里临时写死的配置
         * String ip = IpUtils.getIpAddr(request);
         * int userId = request.getAttribute("user_id");
         */

        //1、保存订单同时返回codeUrl
        String codeUrl = unifiedOrder();
        if(codeUrl == null) {
            throw new  NullPointerException();
        }

        //2、通过google工具生成二维码供用户扫码支付
         try{
            //3、1生成二维码配置
            Map<EncodeHintType,Object> hints =  new HashMap<>();

            //3、2设置纠错等级
            hints.put(EncodeHintType.ERROR_CORRECTION,ErrorCorrectionLevel.L);

            //3、3编码类型
            hints.put(EncodeHintType.CHARACTER_SET,"UTF-8");

            BitMatrix bitMatrix = new MultiFormatWriter().encode(codeUrl,BarcodeFormat.QR_CODE,400,400,hints);
            OutputStream out =  response.getOutputStream();

            MatrixToImageWriter.writeToStream(bitMatrix,"png",out);

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private String unifiedOrder() throws Exception {


        //4.1、生成签名 按照开发文档需要按字典排序，所以用SortedMap
        SortedMap<String,String> params = new TreeMap<>();
        params.put("appid",weChatConfig.getAppId());         //公众账号ID
        params.put("mch_id", weChatConfig.getMchId());       //商户号
        params.put("nonce_str", CommonUtils.generateUUID()); //随机字符串
        params.put("body","");       // 商品描述
        params.put("out_trade_no","");//商户订单号,商户系统内部订单号，要求32个字符内，只能是数字、大小写字母_-|* 且在同一个商户号下唯一
        params.put("total_fee","");//标价金额	分
        params.put("spbill_create_ip","");//用户的客户端IP
        params.put("notify_url",weChatConfig.getPayCallbackUrl());  //通知地址
        params.put("trade_type","NATIVE"); //交易类型 JSAPI 公众号支付 NATIVE 扫码支付 APP APP支付

        //4.2、sign签名 具体规则:https://pay.weixin.qq.com/wiki/doc/api/native.php?chapter=4_3
        String sign = WXPayUtil.createSign(params, weChatConfig.getKey());
        params.put("sign",sign);

        //4.3、map转xml （ WXPayUtil工具类）
        String payXml = WXPayUtil.mapToXml(params);

        //4.4、回调微信的统一下单接口(HttpUtil工具类）
        String orderStr = HttpUtils.doPost(WeChatConfig.getUnifiedOrderUrl(),payXml,4000);
        if(null == orderStr) {
            return null;
        }
        //4.5、xml转map （WXPayUtil工具类）
        Map<String, String> unifiedOrderMap =  WXPayUtil.xmlToMap(orderStr);
        System.out.println(unifiedOrderMap.toString());

        //4.6、获取最终code_url
        if(unifiedOrderMap != null) {
            return unifiedOrderMap.get("code_url");
        }

        return null;
    }

}

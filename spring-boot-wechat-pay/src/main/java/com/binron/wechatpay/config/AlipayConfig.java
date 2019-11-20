package com.binron.wechatpay.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@Data
@PropertySource(value="classpath:application.properties")
public class AlipayConfig {
    @Value("${alipay.store_id}")
    private String storeId;
    @Value("${alipay.time_out_express}")
    private String timeOutExpress;

    public String getStoreId() {
        return storeId;
    }

    public void setStoreId(String storeId) {
        this.storeId = storeId;
    }

    public String getTimeOutExpress() {
        return timeOutExpress;
    }

    public void setTimeOutExpress(String timeOutExpress) {
        this.timeOutExpress = timeOutExpress;
    }
}

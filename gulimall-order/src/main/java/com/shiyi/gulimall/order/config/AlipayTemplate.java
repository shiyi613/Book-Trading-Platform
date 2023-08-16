package com.shiyi.gulimall.order.config;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.AlipayTradePagePayRequest;

import com.shiyi.gulimall.order.vo.PayVo;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "alipay")
@Component
@Data
public class AlipayTemplate {

    //在支付宝创建的应用的id
    private   String app_id;

    // 商户私钥，您的PKCS8格式RSA2私钥
    private  String merchant_private_key = "MIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQCCqgYj5864nokP2VWKW75pDFjB79dsDXAli1PE7r1yf0revr6itepoAFrT2h9Ow4J3iLpgCF/psOSIOtU+sc1+gLD73wZT2wEfTVhyXbJzeTo+CTrEFBVxwGTVaweOnnEWcsW5YWCuS6BowDxn0kDASREc4rB/LTbsQhYbWB8fu7d2MFLREyy9LMQ4W69dR9hkcbWVhEKQZ3OEThI8Rjh22UoCdYihaSapvTQNQhBDCvu4kWl3wkhVtNp2EQMRj0f4HiCtXvFPiO9qumRr9uU5hEVE1Jsf2L/pV6MVENKFsWDBYAk9oZ9QU0Okmwr/ufnDfyxeYQsrkyBxxcfkGDGdAgMBAAECggEAWtjYGBLN2fLwHQLgxKfxBZy+AWk+gDP/qzrp7VyIf5kddy1r4jlVVyYYAIABkIPCrFV2L9rixNpmqoktRr/cvn2/j/+KqLb2WssuGaYnsr2wzv0fzMCiAEriULhvsqtauV7nNnMMA2M1jl9KIG1A8/BZY5oBWQsYyr9QG4bzbN3Vwu/4qVU3wM0xYLAcATl5p8WAG6M0ZaVOvDY7bZzZkoMHhwMuTzw1N1hWlUZJFRU/4vvrPInH1y1guoHOqQJGu80hqex2dd2jxhxOmbPugtaoFXAkyjdFCbAKPij+gOJobemoVRnrXUXg049n2IqQHUoUVsSwTS0qX2Bva6auAQKBgQDfj6pVD5HhfdiP7Jol8w2bYhMODSDfTwsD9QMI0xT7Xy+We39MDoa/Tm5YrxAAhYNjMYrTnrSr9N/laoj4dNoJ67LxiI4+mEsODsxrfuJspCRyJoS4KWBS5a8pzQAtuyfqFTdVq1FU2nqhpKy27BStvv53B7jePosOHuo4jRlnwQKBgQCVn6JX0eZTcysoEkMREKoo3dVkAa2mMoIWRDHKXTOUlT/R6+rYvy/SARq1ffekJMePOOwZjDZuRQxrvH3A6F50GGuDVSR5k5xB2+9LVYrP5cl5z4JzBfQzEuN/zCo8kuOSjz9DSu1ULzT5L72iC/gJ2NNawbkos6ZlSmF+cJGg3QKBgQC5SKx3Cwxwwog14HZxdVuVqFb6kk7oRO5wPSRir+731w/tvDToqDFtZGd4AL9Wb1FRaMISWbqLucvGq+ERamc7SalpHTAnGLk70XACt4qZWAEMyne82i4HBm2CS3EWdcLC8wqFahNiLCDuzziF8mvBBEaKCFBjgZpl/aFJdUfVAQKBgFyuqc1khBwlNxJ3XXvwqrbt0wVD1OEiYFqSm5US7qSXXxUV4j+pDmIztOu/v+q5rbQWOooIxJ7BvtPtyqf4SKn5hn+v+gwlV6GkLzKnJnlpicgetlux0HbiuHGtcmxbQQTEHxf8Dc8b/uBspjb+2wKegS2y2pNP1T7hOEsuR8cxAoGBAJDX1/kZMiDE2YezzzyEz40SSWOwGqsCo75oawuuwXwdmDqg4UPFXGFz33bqHZdYHiLfp8dOLMU9WisRhR9kq31Cgbnro7SHOn2IgAgRrWxLhI0/Us+gAb5T+mCJm/lAbLL9NaA3isr+1m+B+h4ldkHrV7KEVu8+wIdaof2kEW4m";
    // 支付宝公钥,查看地址：https://openhome.alipay.com/platform/keyManage.htm 对应APPID下的支付宝公钥。
    private  String alipay_public_key = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAmaNyxF+CCmilaPIUo6TiPL/ZEGdQGg3v2eVPuNRGvWHHVkJY2wmPcZlt8IWPTw00EhfPItGOOgWEDPgSTk5Gkbx7388G4XS4HWBooQni5sOKrBIM/yPVgWdnOMSl1TouzfV3X0aIGuHSbNAyoaV0ZBBXnmT1fAXv6k/DDdaxJc4qU2wXUrTF36ymMjY7dFue34SG90NXV8FeR1l3ZhMRxZCUB/DKfJotUzAIYBpwcLMaakXZsJyNH1vL81LqV52QyqWCHdJgq0m3W6y1JBF0+Banz1BzrS4zD+0fKbLNtSgjS+ZQ03t1I0Hy0iC3zvxSAGdoC4Tzor2MZ0BTrAPQnQIDAQAB";
    // 服务器[异步通知]页面路径  需http://格式的完整路径，不能加?id=123这类自定义参数，必须外网可以正常访问
    // 支付宝会悄悄的给我们发送一个请求，告诉我们支付成功的信息
    private  String notify_url;

    // 页面跳转同步通知页面路径 需http://格式的完整路径，不能加?id=123这类自定义参数，必须外网可以正常访问
    //同步通知，支付成功，一般跳转到成功页
    private  String return_url;

    // 签名方式
    private  String sign_type = "RSA2";

    // 字符编码格式
    private  String charset = "utf-8";

    // 付款超时时间
    private String timeOut = "30m";

    // 支付宝网关； https://openapi.alipaydev.com/gateway.do
    private  String gatewayUrl;

    public  String pay(PayVo vo) throws AlipayApiException {

        //AlipayClient alipayClient = new DefaultAlipayClient(AlipayTemplate.gatewayUrl, AlipayTemplate.app_id, AlipayTemplate.merchant_private_key, "json", AlipayTemplate.charset, AlipayTemplate.alipay_public_key, AlipayTemplate.sign_type);
        //1、根据支付宝的配置生成一个支付客户端
        AlipayClient alipayClient = new DefaultAlipayClient(gatewayUrl,
                app_id, merchant_private_key, "json",
                charset, alipay_public_key, sign_type);

        //2、创建一个支付请求 //设置请求参数
        AlipayTradePagePayRequest alipayRequest = new AlipayTradePagePayRequest();
        alipayRequest.setReturnUrl(return_url);
        alipayRequest.setNotifyUrl(notify_url);

        //商户订单号，商户网站订单系统中唯一订单号，必填
        String out_trade_no = vo.getOut_trade_no();
        //付款金额，必填
        String total_amount = vo.getTotal_amount();
        //订单名称，必填
        String subject = vo.getSubject();
        //商品描述，可空
        String body = vo.getBody();

        alipayRequest.setBizContent("{\"out_trade_no\":\""+ out_trade_no +"\","
                + "\"total_amount\":\""+ total_amount +"\","
                + "\"subject\":\""+ subject +"\","
                + "\"body\":\""+ body +"\","
                + "\"body\":\""+ timeOut +"\","
                + "\"product_code\":\"FAST_INSTANT_TRADE_PAY\"}");

        String result = alipayClient.pageExecute(alipayRequest).getBody();

        //会收到支付宝的响应，响应的是一个页面，只要浏览器显示这个页面，就会自动来到支付宝的收银台页面
        System.out.println("支付宝的响应："+result);

        return result;

    }
}

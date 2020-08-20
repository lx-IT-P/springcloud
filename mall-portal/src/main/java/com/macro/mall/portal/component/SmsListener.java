package com.macro.mall.portal.component;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @Author: cuzz
 * @Date: 2018/11/16 15:39
 * @Description:
 */
@Component
public class SmsListener {
    private static Logger LOGGER =LoggerFactory.getLogger(SmsListener.class);
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "sms.sms.queue", durable = "true"),
            exchange = @Exchange(value = "mall.sms.exchange",
                    ignoreDeclarationExceptions = "true"),
            key = {"sms.verify.code"}))
    public void listenSms(Map<String, String> msg) throws Exception {
        if (msg == null || msg.size() <= 0) {
            // 放弃处理
            return;
        }
        String phone = msg.get("phone");
        String code = msg.get("code");

        if (StringUtils.isBlank(phone) || StringUtils.isBlank(code)) {
            // 放弃处理
            return;
        }
        LOGGER.info("消息服务 正在给手机号为{}发送短信，code: {}", phone, code);

    }
}

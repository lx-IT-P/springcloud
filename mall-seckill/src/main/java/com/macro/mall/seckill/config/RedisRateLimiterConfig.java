package com.macro.mall.seckill.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * @Author: liuxiang
 * @Date: 2020/5/20
 * @Description: 限流策略的配置类，此处有两种策略一种是根据请求参数中的用户名进行限流，另一种是根据访问IP进行限流；
 */
//@Configuration
public class RedisRateLimiterConfig implements KeyResolver{
      /*  @Bean
        KeyResolver userKeyResolver() {
            return exchange -> Mono.just(exchange.getRequest().getQueryParams().getFirst("username"));
        }*/

        /*@Bean
        @Primary
        public KeyResolver ipKeyResolver() {
            return exchange -> Mono.just(exchange.getRequest().getRemoteAddress().getHostName());
        }*/


    @Override
    public Mono<String> resolve(ServerWebExchange exchange) {
        return Mono.just(exchange.getRequest().getURI().getPath());
    }


    @Bean
    public RedisRateLimiterConfig uriKeyResolver() {
        return new RedisRateLimiterConfig();
    }

}

package com.primihub.gateway.filter;

import com.alibaba.fastjson.JSON;
import com.primihub.biz.entity.base.BaseParamEnum;
import com.primihub.biz.entity.base.BaseResultEntity;
import com.primihub.biz.entity.base.BaseResultEnum;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.UnsupportedEncodingException;
import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicReference;

public class GatewayFilterFactoryTool {

    private GatewayFilterFactoryTool() {
    }

    public static Mono<Void> writeFailureJsonToResponse(ServerWebExchange exchange,BaseResultEnum baseResultEnum,BaseParamEnum baseParamEnum){
        BaseResultEntity entity= BaseResultEntity.failure(baseResultEnum, baseParamEnum.getColumnName());
        return GatewayFilterFactoryTool.writeJsonToResponse(exchange,JSON.toJSON(entity).toString());
    }

    public static Mono<Void> writeFailureJsonToResponse(ServerWebExchange exchange,BaseResultEnum baseResultEnum){
        BaseResultEntity entity= BaseResultEntity.failure(baseResultEnum);
        return GatewayFilterFactoryTool.writeJsonToResponse(exchange,JSON.toJSON(entity).toString());
    }

    public static Mono<Void> writeFailureJsonToResponse(ServerWebExchange exchange,BaseResultEnum baseResultEnum,String error){
        BaseResultEntity entity= BaseResultEntity.failure(baseResultEnum,error);
        return GatewayFilterFactoryTool.writeJsonToResponse(exchange,JSON.toJSON(entity).toString());
    }

    public static Mono<Void> writeJsonToResponse(ServerWebExchange exchange,String json){
        HttpHeaders httpHeaders = exchange.getResponse().getHeaders();
        httpHeaders.add("Content-Type", "application/json; charset=UTF-8");
        httpHeaders.add("Cache-Control", "no-store, no-cache, must-revalidate, max-age=0");
        ServerHttpResponse response=exchange.getResponse();
        DataBuffer bodyDataBuffer = null;
        try {
            bodyDataBuffer = response.bufferFactory().wrap(json.getBytes("utf-8"));
        } catch (UnsupportedEncodingException e) {

        }
        return response.writeWith(Mono.just(bodyDataBuffer));
    }

    public static String getFromDataBufferToString(Flux<DataBuffer> content){
        AtomicReference<String> ref = new AtomicReference<>();
        content.subscribe(buffer -> {
            CharBuffer charBuffer = StandardCharsets.UTF_8.decode(buffer.asByteBuffer());
            DataBufferUtils.release(buffer);
            ref.set(charBuffer.toString());
        });
        return ref.get();
    }
}

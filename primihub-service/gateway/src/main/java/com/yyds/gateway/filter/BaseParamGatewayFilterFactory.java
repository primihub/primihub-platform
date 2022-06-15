package com.yyds.gateway.filter;

import com.alibaba.fastjson.JSON;
import com.yyds.biz.config.base.BaseConfiguration;
import com.yyds.biz.entity.base.BaseJsonParam;
import com.yyds.biz.entity.base.BaseParamEnum;
import com.yyds.biz.entity.base.BaseResultEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.ResolvableType;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.MediaType;
import org.springframework.http.codec.CodecConfigurer;
import org.springframework.http.codec.HttpMessageReader;
import org.springframework.http.codec.multipart.Part;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.server.HandlerStrategies;
import org.springframework.web.reactive.function.server.ServerRequest;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;

@Slf4j
@Order(10)
@Component
public class BaseParamGatewayFilterFactory extends AbstractGatewayFilterFactory {

    public static final String REQUEST_TIME_START="requestTimeStart";

    @Autowired
    private BaseConfiguration baseConfiguration;
    @Autowired
    private CodecConfigurer codecConfigurer;

    @Override
    public GatewayFilter apply(Object config) {
        return ((exchange, chain) -> {
            MediaType mediaType = exchange.getRequest().getHeaders().getContentType();
            if(mediaType==null){
                MultiValueMap<String, String> queryParams=exchange.getRequest().getQueryParams();

                String timestamp=queryParams.getFirst(BaseParamEnum.TIMESTAMP.getColumnName());
                if(timestamp==null||timestamp.trim().equals("")) {
                    return GatewayFilterFactoryTool.writeFailureJsonToResponse(exchange,BaseResultEnum.LACK_OF_PARAM,BaseParamEnum.TIMESTAMP);
                }
                String nonce=queryParams.getFirst(BaseParamEnum.NONCE.getColumnName());
                if(nonce==null||nonce.trim().equals("")) {
                    return GatewayFilterFactoryTool.writeFailureJsonToResponse(exchange,BaseResultEnum.LACK_OF_PARAM,BaseParamEnum.NONCE);
                }
                String currentRawPath=exchange.getRequest().getURI().getRawPath();
                String token=queryParams.getFirst(BaseParamEnum.TOKEN.getColumnName());
                if(!(baseConfiguration.getTokenValidateUriBlackList()!=null
                        && baseConfiguration.getTokenValidateUriBlackList().contains(currentRawPath))
                        &&(token==null||token.trim().equals(""))){
                    return GatewayFilterFactoryTool.writeFailureJsonToResponse(exchange,BaseResultEnum.LACK_OF_PARAM,BaseParamEnum.TOKEN);
                }
                String sign=queryParams.getFirst(BaseParamEnum.SIGN.getColumnName());
                if((baseConfiguration.getNeedSignUriList()!=null
                        && baseConfiguration.getNeedSignUriList().contains(currentRawPath))
                        &&(sign==null||sign.trim().equals(""))){
                    return GatewayFilterFactoryTool.writeFailureJsonToResponse(exchange,BaseResultEnum.LACK_OF_PARAM,BaseParamEnum.SIGN);
                }

                exchange.getAttributes().put(BaseParamEnum.TIMESTAMP.getColumnName(),timestamp);
                exchange.getAttributes().put(BaseParamEnum.NONCE.getColumnName(),nonce);
                if(token!=null)exchange.getAttributes().put(BaseParamEnum.TOKEN.getColumnName(),token);
                if(sign!=null)exchange.getAttributes().put(BaseParamEnum.SIGN.getColumnName(),sign);
                exchange.getAttributes().put(REQUEST_TIME_START,System.currentTimeMillis());
                return chain.filter(exchange).then(
                        Mono.fromRunnable(()->{
                            Long requestTimeStart=exchange.getAttribute(REQUEST_TIME_START);
                            if(requestTimeStart!=null){
                                StringBuilder sb=new StringBuilder(exchange.getRequest().getURI().getRawPath())
                                        .append(":")
                                        .append(System.currentTimeMillis() -requestTimeStart)
                                        .append("ms");
                                log.info(sb.toString());
                            }
                        })
                );
            }
            return DataBufferUtils.join(exchange.getRequest().getBody())
                    .flatMap(dataBuffer -> {
                        DataBufferUtils.retain(dataBuffer);
                        Flux<DataBuffer> cachedFlux = Flux
                                .defer(() -> Flux.just(dataBuffer.slice(0, dataBuffer.readableByteCount())));
                        ServerHttpRequest mutatedRequest = new ServerHttpRequestDecorator(exchange.getRequest()) {
                            @Override
                            public Flux<DataBuffer> getBody() {
                                return cachedFlux;
                            }
                        };

                        ResolvableType resolvableType;
                        if (MediaType.MULTIPART_FORM_DATA.isCompatibleWith(mediaType)) {
                            resolvableType = ResolvableType.forClassWithGenerics(MultiValueMap.class, String.class, Part.class);
                        } else if (MediaType.APPLICATION_JSON.isCompatibleWith(mediaType)) {
                            resolvableType = ResolvableType.forClass(String.class);
                        } else {
                            resolvableType = ResolvableType.forClass(MultiValueMap.class);
                        }
                        return HandlerStrategies.builder().codecs((c) -> c.defaultCodecs().maxInMemorySize(10*1024*1024)).build().messageReaders()
//                        return HandlerStrategies.withDefaults().messageReaders()
                                .stream()
                                .filter(reader -> reader.canRead(resolvableType, exchange.getRequest().getHeaders().getContentType())).findFirst()
                                .orElseThrow(() -> new IllegalStateException("no suitable HttpMessageReader."))
                                .readMono(resolvableType, mutatedRequest, Collections.emptyMap())
                                .flatMap(resolvedBody -> {
                                    if (MediaType.MULTIPART_FORM_DATA.isCompatibleWith(mediaType)) {
                                        Part timeStampPart = (Part) ((MultiValueMap) resolvedBody).getFirst(BaseParamEnum.TIMESTAMP.getColumnName());
                                        if(timeStampPart==null) {
                                            return GatewayFilterFactoryTool.writeFailureJsonToResponse(exchange,BaseResultEnum.LACK_OF_PARAM,BaseParamEnum.TIMESTAMP);
                                        }
                                        String timestamp = GatewayFilterFactoryTool.getFromDataBufferToString(timeStampPart.content());
                                        if(timestamp==null||timestamp.equals("")){
                                            return GatewayFilterFactoryTool.writeFailureJsonToResponse(exchange,BaseResultEnum.LACK_OF_PARAM,BaseParamEnum.TIMESTAMP);
                                        }

                                        Part noncePart = (Part) ((MultiValueMap) resolvedBody).getFirst(BaseParamEnum.NONCE.getColumnName());
                                        if(noncePart==null) {
                                            return GatewayFilterFactoryTool.writeFailureJsonToResponse(exchange,BaseResultEnum.LACK_OF_PARAM,BaseParamEnum.NONCE);
                                        }
                                        String nonce = GatewayFilterFactoryTool.getFromDataBufferToString(noncePart.content());
                                        if(nonce==null||nonce.equals("")){
                                            return GatewayFilterFactoryTool.writeFailureJsonToResponse(exchange,BaseResultEnum.LACK_OF_PARAM,BaseParamEnum.NONCE);
                                        }

                                        String token=null;
                                        Part tokenPart = (Part) ((MultiValueMap) resolvedBody).getFirst(BaseParamEnum.TOKEN.getColumnName());
                                        if(tokenPart!=null) {
                                            token = GatewayFilterFactoryTool.getFromDataBufferToString(tokenPart.content());
                                            if(token==null||token.equals("")){
                                                return GatewayFilterFactoryTool.writeFailureJsonToResponse(exchange,BaseResultEnum.LACK_OF_PARAM,BaseParamEnum.TOKEN);
                                            }
                                        }
                                        String currentRawPath=exchange.getRequest().getURI().getRawPath();
                                        if(!(baseConfiguration.getTokenValidateUriBlackList()!=null
                                                && baseConfiguration.getTokenValidateUriBlackList().contains(currentRawPath))
                                                &&(token==null||token.trim().equals(""))){
                                            return GatewayFilterFactoryTool.writeFailureJsonToResponse(exchange,BaseResultEnum.LACK_OF_PARAM,BaseParamEnum.TOKEN);
                                        }

                                        String sign=null;
                                        Part signPart = (Part) ((MultiValueMap) resolvedBody).getFirst(BaseParamEnum.SIGN.getColumnName());
                                        if(signPart!=null) {
                                            sign = GatewayFilterFactoryTool.getFromDataBufferToString(signPart.content());
                                            if(sign==null||sign.equals("")){
                                                return GatewayFilterFactoryTool.writeFailureJsonToResponse(exchange,BaseResultEnum.LACK_OF_PARAM,BaseParamEnum.SIGN);
                                            }
                                        }
                                        if((baseConfiguration.getNeedSignUriList()!=null
                                                && baseConfiguration.getNeedSignUriList().contains(currentRawPath))
                                                &&(sign==null||sign.trim().equals(""))){
                                            return GatewayFilterFactoryTool.writeFailureJsonToResponse(exchange,BaseResultEnum.LACK_OF_PARAM,BaseParamEnum.SIGN);
                                        }

                                        exchange.getAttributes().put(BaseParamEnum.TIMESTAMP.getColumnName(),timestamp);
                                        exchange.getAttributes().put(BaseParamEnum.NONCE.getColumnName(),nonce);
                                        if(token!=null)exchange.getAttributes().put(BaseParamEnum.TOKEN.getColumnName(),token);
                                        if(sign!=null)exchange.getAttributes().put(BaseParamEnum.SIGN.getColumnName(),sign);
                                    } else if (MediaType.APPLICATION_JSON.isCompatibleWith(mediaType)) {
                                        BaseJsonParam baseJsonParam=JSON.parseObject(resolvedBody.toString(),BaseJsonParam.class);
                                        if(baseJsonParam.getTimestamp()==null||baseJsonParam.getTimestamp().equals("")){
                                            return GatewayFilterFactoryTool.writeFailureJsonToResponse(exchange,BaseResultEnum.LACK_OF_PARAM,BaseParamEnum.TIMESTAMP);
                                        }
                                        if(baseJsonParam.getNonce()==null||baseJsonParam.getNonce().equals("")){
                                            return GatewayFilterFactoryTool.writeFailureJsonToResponse(exchange,BaseResultEnum.LACK_OF_PARAM,BaseParamEnum.NONCE);
                                        }
                                        String currentRawPath=exchange.getRequest().getURI().getRawPath();
                                        if(!(baseConfiguration.getTokenValidateUriBlackList()!=null
                                                && baseConfiguration.getTokenValidateUriBlackList().contains(currentRawPath))
                                                &&(baseJsonParam.getToken()==null||baseJsonParam.getToken().trim().equals(""))){
                                            return GatewayFilterFactoryTool.writeFailureJsonToResponse(exchange, BaseResultEnum.LACK_OF_PARAM, BaseParamEnum.TOKEN);
                                        }
                                        if((baseConfiguration.getNeedSignUriList()!=null
                                                && baseConfiguration.getNeedSignUriList().contains(currentRawPath))
                                                &&(baseJsonParam.getSign()==null||baseJsonParam.getSign().trim().equals(""))){
                                            return GatewayFilterFactoryTool.writeFailureJsonToResponse(exchange, BaseResultEnum.LACK_OF_PARAM, BaseParamEnum.SIGN);
                                        }

                                        exchange.getAttributes().put(BaseParamEnum.TIMESTAMP.getColumnName(),baseJsonParam.getTimestamp());
                                        exchange.getAttributes().put(BaseParamEnum.NONCE.getColumnName(),baseJsonParam.getNonce());
                                        if(baseJsonParam.getToken()!=null)exchange.getAttributes().put(BaseParamEnum.TOKEN.getColumnName(),baseJsonParam.getToken());
                                        if(baseJsonParam.getSign()!=null)exchange.getAttributes().put(BaseParamEnum.SIGN.getColumnName(),baseJsonParam.getSign());
                                    } else{
                                        MultiValueMap<String, String> queryParams=exchange.getRequest().getQueryParams();

                                        String timestamp=queryParams.getFirst(BaseParamEnum.TIMESTAMP.getColumnName());
                                        if(timestamp==null||timestamp.trim().equals("")) {
                                            timestamp=((MultiValueMap<String,String>) resolvedBody).getFirst(BaseParamEnum.TIMESTAMP.getColumnName());
                                            if(timestamp==null||timestamp.trim().equals("")) {
                                                return GatewayFilterFactoryTool.writeFailureJsonToResponse(exchange, BaseResultEnum.LACK_OF_PARAM, BaseParamEnum.TIMESTAMP);
                                            }
                                        }

                                        String nonce=queryParams.getFirst(BaseParamEnum.NONCE.getColumnName());
                                        if(nonce==null||nonce.trim().equals("")) {
                                            nonce=((MultiValueMap<String,String>) resolvedBody).getFirst(BaseParamEnum.NONCE.getColumnName());
                                            if(nonce==null||nonce.trim().equals("")) {
                                                return GatewayFilterFactoryTool.writeFailureJsonToResponse(exchange, BaseResultEnum.LACK_OF_PARAM, BaseParamEnum.NONCE);
                                            }
                                        }

                                        String currentRawPath=exchange.getRequest().getURI().getRawPath();
                                        String token=queryParams.getFirst(BaseParamEnum.TOKEN.getColumnName());
                                        if(!(baseConfiguration.getTokenValidateUriBlackList()!=null
                                                && baseConfiguration.getTokenValidateUriBlackList().contains(currentRawPath))
                                                &&(token==null||token.trim().equals(""))){
                                            token=((MultiValueMap<String,String>) resolvedBody).getFirst(BaseParamEnum.TOKEN.getColumnName());
                                            if(token==null||token.trim().equals("")){
                                                return GatewayFilterFactoryTool.writeFailureJsonToResponse(exchange, BaseResultEnum.LACK_OF_PARAM, BaseParamEnum.TOKEN);
                                            }
                                        }

                                        String sign=queryParams.getFirst(BaseParamEnum.SIGN.getColumnName());
                                        if((baseConfiguration.getNeedSignUriList()!=null
                                                && baseConfiguration.getNeedSignUriList().contains(currentRawPath))
                                                &&(sign==null||sign.trim().equals(""))){
                                            sign=((MultiValueMap<String,String>) resolvedBody).getFirst(BaseParamEnum.SIGN.getColumnName());
                                            if(sign==null||sign.trim().equals("")) {
                                                return GatewayFilterFactoryTool.writeFailureJsonToResponse(exchange, BaseResultEnum.LACK_OF_PARAM, BaseParamEnum.SIGN);
                                            }
                                        }

                                        exchange.getAttributes().put(BaseParamEnum.TIMESTAMP.getColumnName(),timestamp);
                                        exchange.getAttributes().put(BaseParamEnum.NONCE.getColumnName(),nonce);
                                        if(token!=null)exchange.getAttributes().put(BaseParamEnum.TOKEN.getColumnName(),token);
                                        if(sign!=null)exchange.getAttributes().put(BaseParamEnum.SIGN.getColumnName(),sign);
                                    }

                                    exchange.getAttributes().put(REQUEST_TIME_START,System.currentTimeMillis());
                                    return chain.filter(exchange.mutate().request(mutatedRequest).build()).then(
                                            Mono.fromRunnable(()->{
                                                Long requestTimeStart=exchange.getAttribute(REQUEST_TIME_START);
                                                if(requestTimeStart!=null){
                                                    StringBuilder sb=new StringBuilder(exchange.getRequest().getURI().getRawPath())
                                                            .append(":")
                                                            .append(System.currentTimeMillis() -requestTimeStart)
                                                            .append("ms");
                                                    log.info(sb.toString());
                                                }
                                            })
                                    );
                                });
                    });
        });

    }
}

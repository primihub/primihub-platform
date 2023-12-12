package com.primihub.gateway.filter;

import com.primihub.biz.config.base.BaseConfiguration;
import com.primihub.biz.entity.base.BaseParamEnum;
import com.primihub.biz.entity.base.BaseResultEnum;
import com.primihub.biz.entity.sys.vo.SysAuthNodeVO;
import com.primihub.biz.entity.sys.vo.SysUserListVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import reactor.core.publisher.Mono;

import java.io.File;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Order(200)
@Component
public class SwaggerGatewayFilterFactory extends AbstractGatewayFilterFactory {

    @Autowired
    private BaseConfiguration baseConfiguration;

    @Value("${openSwagger:false}")
    private boolean openSwagger;
    public static final String REQUEST_TIME_START="swaggerRequestTimeStart";
    public static final AntPathMatcher MATCHER = new AntPathMatcher(File.separator);
    public static final String[] PATTERN_URLS = new String[]{"/shareData/**","/v2/**"};

    public boolean checkUri(String requestURI){
        for (String url : PATTERN_URLS) {
            if(MATCHER.match(url,requestURI)){
                return true;
            }
        }
        return false;
    }

    @Override
    public GatewayFilter apply(Object config) {
        return (((exchange, chain) -> {
            if (!openSwagger){
                return GatewayFilterFactoryTool.writeFailureJsonToResponse(exchange,BaseResultEnum.NO_AUTH,"swagger不可用");
            }
            exchange.getAttributes().put(REQUEST_TIME_START,System.currentTimeMillis());
            String currentRawPath=exchange.getRequest().getURI().getRawPath();
            log.info(currentRawPath);
            if (checkUri(currentRawPath)){
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
        }));
    }
}

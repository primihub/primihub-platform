package com.primihub.gateway.filter;

import com.primihub.biz.entity.base.BaseParamEnum;
import com.primihub.biz.entity.base.BaseResultEnum;
import com.primihub.biz.entity.sys.vo.SysAuthNodeVO;
import com.primihub.biz.entity.sys.vo.SysUserListVO;
import com.primihub.biz.repository.primaryredis.sys.SysUserPrimaryRedisRepository;
import com.primihub.biz.service.sys.SysAuthService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.annotation.Order;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Order(11)
@Component
public class SysAuthGatewayFilterFactory extends AbstractGatewayFilterFactory {

    @Autowired
    private SysUserPrimaryRedisRepository sysUserPrimaryRedisRepository;
    @Autowired
    private SysAuthService sysAuthService;

    @Override
    public GatewayFilter apply(Object config) {
        return (((exchange, chain) -> {
            String token=exchange.getAttribute(BaseParamEnum.TOKEN.getColumnName());
            if(token==null||token.equals(""))
                return chain.filter(exchange).then();

            SysUserListVO sysUserListVO=sysUserPrimaryRedisRepository.findUserLoginStatus(token);
            if(sysUserListVO==null)
                return GatewayFilterFactoryTool.writeFailureJsonToResponse(exchange, BaseResultEnum.TOKEN_INVALIDATION);
            sysUserPrimaryRedisRepository.expireUserLoginStatus(token,sysUserListVO.getUserId());

            String rawPath=exchange.getRequest().getURI().getRawPath();
            Map<String, SysAuthNodeVO> mapping=sysAuthService.getSysAuthUrlMapping();
            SysAuthNodeVO sysAuthNodeVO=mapping.get(rawPath);
            if(sysAuthNodeVO!=null){
                if(!sysUserListVO.getAuthIdList().contains(sysAuthNodeVO.getAuthId().toString()))
                    return GatewayFilterFactoryTool.writeFailureJsonToResponse(exchange, BaseResultEnum.NO_AUTH);
            }

            String userIdStr=sysUserListVO.getUserId().toString();
            ServerHttpRequest newRequest = exchange.getRequest().mutate()
                    .header("userId", userIdStr)
                    .header("token", token)
                    .build();
            return chain.filter(exchange.mutate().request(newRequest).build());
        }));
    }
}

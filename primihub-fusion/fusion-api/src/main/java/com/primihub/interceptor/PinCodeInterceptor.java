package com.primihub.interceptor;

import com.alibaba.fastjson.JSON;
import com.primihub.entity.base.BaseResultEntity;
import com.primihub.entity.base.BaseResultEnum;
import com.primihub.entity.fusion.po.FusionOrgan;
import com.primihub.repository.FusionRepository;
import com.primihub.util.SignUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class PinCodeInterceptor implements HandlerInterceptor {

    @Autowired
    private FusionRepository fusionRepository;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        String globalId = request.getParameter("globalId");
        String pinCode = request.getParameter("pinCode");
        if (globalId==null|| "".equals(globalId)||pinCode==null|| "".equals(pinCode)) {
            response.setContentType(MediaType.APPLICATION_JSON.toString());
            response.setCharacterEncoding("utf-8");
            response.getWriter().println(JSON.toJSONString(BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"globalId,pinCode")));
            return false;
        }
        FusionOrgan fusionOrgan=fusionRepository.getFusionOrganByGlobalId(globalId);
        if(fusionOrgan==null||fusionOrgan.getId()==null){
            response.setContentType(MediaType.APPLICATION_JSON.toString());
            response.setCharacterEncoding("utf-8");
            response.getWriter().println(JSON.toJSONString(BaseResultEntity.failure(BaseResultEnum.FAILURE,"没有找到这个id")));
            return false;
        }

        String pinCodeMd5=SignUtil.getMD5ValueUpperCaseByDefaultEncode(pinCode);
        if(!pinCodeMd5.equals(fusionOrgan.getPinCodeMd())){
            response.setContentType(MediaType.APPLICATION_JSON.toString());
            response.setCharacterEncoding("utf-8");
            response.getWriter().println(JSON.toJSONString(BaseResultEntity.failure(BaseResultEnum.FAILURE,"pinCode不正确")));
            return false;
        }

        return true;
    }

}

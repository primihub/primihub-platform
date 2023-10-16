package com.primihub.biz.filter;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.primihub.biz.config.base.OrganConfiguration;
import com.primihub.biz.entity.base.BaseResultEntity;
import com.primihub.biz.entity.base.BaseResultEnum;
import com.primihub.biz.util.crypt.CryptUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.IOException;

@Slf4j
//@Component
@WebFilter(filterName = "shareFilter", urlPatterns = {"/shareData/*"})
public class ShareFilter implements Filter {

    @Autowired
    private OrganConfiguration organConfiguration;
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        Filter.super.init(filterConfig);
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest)request;
        if (httpRequest.getParameterMap().containsKey("ignore")){
            filterChain.doFilter(request, response);
        }else {
            String bodytxt;
            try {
                bodytxt = getBodytxt(request);
                if (StringUtils.isBlank(bodytxt)){
                    writeJsonToResponse(response,"解析内容空或同步内容为空");
                    return;
                }
                String privateKey = organConfiguration.getSysLocalOrganInfo().getPrivateKey();
                if (StringUtils.isNotBlank(privateKey)){
                    String shareData = CryptUtil.multipartDecrypt(bodytxt,privateKey);
                    ModifyBodyHttpServletRequestWrapper httpServletRequestWrapper = new ModifyBodyHttpServletRequestWrapper((HttpServletRequest)request, shareData);
                    filterChain.doFilter(httpServletRequestWrapper, response);
                }
            }catch (IOException e){
                e.printStackTrace();
                writeJsonToResponse(response,"获取同步内容失败:"+e.getMessage());
            }catch (Exception e) {
                e.printStackTrace();
                writeJsonToResponse(response,"解密内容失败:"+e.getMessage());
            }
        }
    }

    @Override
    public void destroy() {
        Filter.super.destroy();
    }

    private void writeJsonToResponse(ServletResponse response, String message) throws IOException {
        response.setContentType(MediaType.APPLICATION_JSON.toString());
        response.setCharacterEncoding("utf-8");
        response.getWriter().println(JSON.toJSONString(BaseResultEntity.failure(BaseResultEnum.DECRYPTION_FAILED,message)));
    }

    private String getBodytxt(ServletRequest request) throws IOException {
        BufferedReader br = request.getReader();
        String str, wholeStr = "";
        while((str = br.readLine()) != null){
            wholeStr += str;
        }
        return wholeStr;
    }
}

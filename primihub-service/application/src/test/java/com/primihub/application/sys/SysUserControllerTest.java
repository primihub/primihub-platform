package com.primihub.application.sys;

import com.alibaba.fastjson.JSON;
import com.primihub.biz.entity.base.BaseResultEntity;
import com.primihub.biz.util.crypt.CryptUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Map;

import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.requestParameters;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@AutoConfigureRestDocs(outputDir = "target/generated-snippets")
@RunWith(SpringRunner.class)
@SpringBootTest
public class SysUserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void testGetValidatePublicKey() throws Exception {
        this.mockMvc.perform(get("/common/getValidatePublicKey"))
                .andExpect(status().isOk())
                .andDo(document("getValidatePublicKey",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(
                        ),
                        requestParameters(
                        ),
                        responseFields(
                                fieldWithPath("code").description("返回码"),
                                fieldWithPath("msg").description("返回码描述"),
                                fieldWithPath("result").description("返回码结果"),
                                fieldWithPath("result.publicKey").description("公钥"),
                                fieldWithPath("result.publicKeyName").description("公钥key"),
                                fieldWithPath("extra").description("额外信息")
                        )
                ));
    }


    @Test
    public void testLogin() throws Exception {
        MvcResult mvcResult=this.mockMvc.perform(get("/common/getValidatePublicKey")).andReturn();
        MockHttpServletResponse response =mvcResult.getResponse();
        String result=response.getContentAsString();
        BaseResultEntity<Map<String,String>> baseResultEntity=JSON.parseObject(result, BaseResultEntity.class);

        String rsaPassword= CryptUtil.encryptRsaWithPublicKey( "123456",baseResultEntity.getResult().get("publicKey"));
        String publicKeyName=baseResultEntity.getResult().get("publicKeyName");
        String userAccount="admin";

        MvcResult loginMvcResult=this.mockMvc.perform(post("/user/login")
                .param("userAccount",userAccount)
                .param("userPassword",rsaPassword)
                .param("validateKeyName",publicKeyName))
                .andExpect(status().isOk())
                .andDo(document("login",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(
                        ),
                        requestParameters(
                                parameterWithName("userAccount").description("用户账户"),
                                parameterWithName("userPassword").description("用户密码 用rsa公钥加密后的"),
                                parameterWithName("validateKeyName").description("加密key的名称")
                        ),
                        responseFields(
                                fieldWithPath("code").description("返回码"),
                                fieldWithPath("msg").description("返回码描述"),
                                fieldWithPath("result").description("返回码结果"),
//                                fieldWithPath("result.roleAuthRootList").description("根节点集合"),
//                                fieldWithPath("result.roleAuthRootList[].authId").description("权限id"),
//                                fieldWithPath("result.roleAuthRootList[].authName").description("权限名称"),
//                                fieldWithPath("result.roleAuthRootList[].authType").description("权限类型（1.菜单 2.列表 3.按钮）"),
//                                fieldWithPath("result.roleAuthRootList[].authCode").description("权限代码"),
//                                fieldWithPath("result.roleAuthRootList[].dataAuthCode").description("数据权限代码"),
//                                fieldWithPath("result.roleAuthRootList[].authIndex").description("顺序"),
//                                fieldWithPath("result.roleAuthRootList[].authDepth").description("深度"),
//                                fieldWithPath("result.roleAuthRootList[].isShow").description("是否展示"),
//                                fieldWithPath("result.roleAuthRootList[].isEditable").description("是否可编辑"),
//                                fieldWithPath("result.roleAuthRootList[].isDel").description("是否删除"),
//                                fieldWithPath("result.roleAuthRootList[].pauthId").description("父id"),
//                                fieldWithPath("result.roleAuthRootList[].rauthId").description("根id"),
//                                fieldWithPath("result.roleAuthRootList[].fullPath").description("完整路径"),
//                                fieldWithPath("result.roleAuthRootList[].authUrl").description("过滤路径"),
//                                fieldWithPath("result.roleAuthRootList[].ctime").description("创建时间"),
//                                fieldWithPath("result.roleAuthRootList[].utime").description("更新时间"),
//                                fieldWithPath("result.roleAuthRootList[].children").optional().description("子节点"),
//                                fieldWithPath("result.roleAuthRootList[].isGrant").description("是否已授权"),
//
//                                fieldWithPath("result.roleAuthRootList[].children[].authId").optional().description("权限id"),
//                                fieldWithPath("result.roleAuthRootList[].children[].authName").optional().description("权限名称"),
//                                fieldWithPath("result.roleAuthRootList[].children[].authType").optional().description("权限类型（1.菜单 2.列表 3.按钮 4.链接）"),
//                                fieldWithPath("result.roleAuthRootList[].children[].authCode").optional().description("权限代码"),
//                                fieldWithPath("result.roleAuthRootList[].children[].dataAuthCode").optional().description("数据权限代码"),
//                                fieldWithPath("result.roleAuthRootList[].children[].authIndex").optional().description("顺序"),
//                                fieldWithPath("result.roleAuthRootList[].children[].authDepth").optional().description("深度"),
//                                fieldWithPath("result.roleAuthRootList[].children[].isShow").optional().description("是否展示"),
//                                fieldWithPath("result.roleAuthRootList[].children[].isEditable").optional().description("是否可编辑"),
//                                fieldWithPath("result.roleAuthRootList[].children[].isDel").optional().description("是否删除"),
//                                fieldWithPath("result.roleAuthRootList[].children[].pauthId").optional().description("父id"),
//                                fieldWithPath("result.roleAuthRootList[].children[].rauthId").optional().description("根id"),
//                                fieldWithPath("result.roleAuthRootList[].children[].fullPath").description("完整路径"),
//                                fieldWithPath("result.roleAuthRootList[].children[].authUrl").description("过滤路径"),
//                                fieldWithPath("result.roleAuthRootList[].children[].ctime").optional().description("创建时间"),
//                                fieldWithPath("result.roleAuthRootList[].children[].utime").optional().description("更新时间"),
//                                fieldWithPath("result.roleAuthRootList[].children[].children").optional().description("子节点"),
//                                fieldWithPath("result.roleAuthRootList[].children[].isGrant").optional().description("是否已授权"),
//
//                                fieldWithPath("result.roleAuthRootList[].children[].children[].authId").optional().description("权限id"),
//                                fieldWithPath("result.roleAuthRootList[].children[].children[].authName").optional().description("权限名称"),
//                                fieldWithPath("result.roleAuthRootList[].children[].children[].authType").optional().description("权限类型（1.菜单 2.列表 3.按钮 4.链接）"),
//                                fieldWithPath("result.roleAuthRootList[].children[].children[].authCode").optional().description("权限代码"),
//                                fieldWithPath("result.roleAuthRootList[].children[].children[].dataAuthCode").optional().description("数据权限代码"),
//                                fieldWithPath("result.roleAuthRootList[].children[].children[].authIndex").optional().description("顺序"),
//                                fieldWithPath("result.roleAuthRootList[].children[].children[].authDepth").optional().description("深度"),
//                                fieldWithPath("result.roleAuthRootList[].children[].children[].isShow").optional().description("是否展示"),
//                                fieldWithPath("result.roleAuthRootList[].children[].children[].isEditable").optional().description("是否可编辑"),
//                                fieldWithPath("result.roleAuthRootList[].children[].children[].isDel").optional().description("是否删除"),
//                                fieldWithPath("result.roleAuthRootList[].children[].children[].pauthId").optional().description("父id"),
//                                fieldWithPath("result.roleAuthRootList[].children[].children[].rauthId").optional().description("根id"),
//                                fieldWithPath("result.roleAuthRootList[].children[].children[].fullPath").description("完整路径"),
//                                fieldWithPath("result.roleAuthRootList[].children[].children[].authUrl").description("过滤路径"),
//                                fieldWithPath("result.roleAuthRootList[].children[].children[].ctime").optional().description("创建时间"),
//                                fieldWithPath("result.roleAuthRootList[].children[].children[].utime").optional().description("更新时间"),
//                                fieldWithPath("result.roleAuthRootList[].children[].children[].children").optional().description("子节点"),
//                                fieldWithPath("result.roleAuthRootList[].children[].children[].isGrant").optional().description("是否授权"),
                                fieldWithPath("result.grantAuthRootList").description("已授权权限集合"),
                                fieldWithPath("result.grantAuthRootList[].authId").description("权限id"),
                                fieldWithPath("result.grantAuthRootList[].authName").description("权限名称"),
                                fieldWithPath("result.grantAuthRootList[].authType").description("权限类型（1.菜单 2.列表 3.按钮）"),
                                fieldWithPath("result.grantAuthRootList[].authCode").description("权限代码"),
                                fieldWithPath("result.grantAuthRootList[].dataAuthCode").description("数据权限代码"),
                                fieldWithPath("result.grantAuthRootList[].authIndex").description("顺序"),
                                fieldWithPath("result.grantAuthRootList[].authDepth").description("深度"),
                                fieldWithPath("result.grantAuthRootList[].isShow").description("是否展示"),
                                fieldWithPath("result.grantAuthRootList[].isEditable").description("是否可编辑"),
                                fieldWithPath("result.grantAuthRootList[].isDel").description("是否删除"),
                                fieldWithPath("result.grantAuthRootList[].pauthId").description("父id"),
                                fieldWithPath("result.grantAuthRootList[].rauthId").description("根id"),
                                fieldWithPath("result.grantAuthRootList[].fullPath").description("完整路径"),
                                fieldWithPath("result.grantAuthRootList[].authUrl").description("过滤路径"),
                                fieldWithPath("result.grantAuthRootList[].ctime").description("创建时间"),
                                fieldWithPath("result.grantAuthRootList[].utime").description("更新时间"),
                                fieldWithPath("result.grantAuthRootList[].children").optional().description("子节点"),
                                fieldWithPath("result.grantAuthRootList[].isGrant").description("是否已授权"),
                                fieldWithPath("result.sysUser").description("用户信息"),
                                fieldWithPath("result.sysUser.userId").description("用户id"),
                                fieldWithPath("result.sysUser.userAccount").description("用户账户"),
                                fieldWithPath("result.sysUser.userName").description("用户昵称"),
                                fieldWithPath("result.sysUser.roleIdList").description("角色id集合"),
                                fieldWithPath("result.sysUser.roleIdListDesc").description("角色id集合描述"),
                                fieldWithPath("result.sysUser.organIdList").description("机构id集合"),
                                fieldWithPath("result.sysUser.organIdListDesc").description("机构id集合描述"),
                                fieldWithPath("result.sysUser.rorganIdList").description("根机构id集合"),
                                fieldWithPath("result.sysUser.rorganIdListDesc").description("根机构id集合描述"),
                                fieldWithPath("result.sysUser.authIdList").description("权限id集合"),
                                fieldWithPath("result.sysUser.isForbid").description("是否禁用"),
                                fieldWithPath("result.sysUser.isEditable").description("是否可编辑"),
                                fieldWithPath("result.sysUser.ctime").description("创建时间"),
                                fieldWithPath("result.sysUser.cTime").description("创建时间"),

                                fieldWithPath("result.token").description("登录令牌"),

                                fieldWithPath("extra").description("额外信息")
                        )
                )).andReturn();
        System.out.println(loginMvcResult.getResponse().getContentAsString());
    }

    @Test
    public void testLogout() throws Exception {
        this.mockMvc.perform(get("/user/logout"))
                .andExpect(status().isOk())
                .andDo(document("logout",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(
                        ),
                        requestParameters(
                        ),
                        responseFields(
                                fieldWithPath("code").description("返回码"),
                                fieldWithPath("msg").description("返回码描述"),
                                fieldWithPath("result").description("返回码结果"),
                                fieldWithPath("extra").description("额外信息")
                        )
                ));
    }

    @Test
    public void testSaveOrUpdateUser() throws Exception {
        this.mockMvc.perform(post("/user/saveOrUpdateUser")
                .param("userId","")
                .param("userName","测试用户姓名")
                .param("userAccount","account"+System.currentTimeMillis())
                .param("roleIdList","")
                .param("organIdList","")
                .param("rOrganIdList","")
                .param("isForbid","0"))
                .andExpect(status().isOk())
                .andDo(document("saveOrUpdateUser",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(
                        ),
                        requestParameters(
                                parameterWithName("userId").description("用户id(为空时新增，不为空修改)"),
                                parameterWithName("userName").description("用户昵称"),
                                parameterWithName("userAccount").description("用户账户 只有新增时才有效"),
                                parameterWithName("roleIdList").description("添加的角色id集合 逗号分割 非必填"),
                                parameterWithName("organIdList").description("添加的机构id集合 逗号分割 非必填"),
                                parameterWithName("rOrganIdList").description("与organIdList对应，添加的机构根id集合 逗号分割 非必填"),
                                parameterWithName("isForbid").description("是否禁用")
                        ),
                        responseFields(
                                fieldWithPath("code").description("返回码"),
                                fieldWithPath("msg").description("返回码描述"),
                                fieldWithPath("result").description("返回码结果"),
                                fieldWithPath("result.sysUser").description("用户信息"),
                                fieldWithPath("result.sysUser.userId").description("用户id"),
                                fieldWithPath("result.sysUser.userAccount").description("用户账户"),
                                fieldWithPath("result.sysUser.userPassword").description(""),
                                fieldWithPath("result.sysUser.userName").description("用户昵称"),
                                fieldWithPath("result.sysUser.roleIdList").description("角色id集合"),
                                fieldWithPath("result.sysUser.organIdList").description("机构id集合"),
                                fieldWithPath("result.sysUser.rorganIdList").description("根机构id集合"),
                                fieldWithPath("result.sysUser.isForbid").description("是否禁用"),
                                fieldWithPath("result.sysUser.isEditable").description("是否可编辑"),
                                fieldWithPath("result.sysUser.isDel").description("是否删除"),
                                fieldWithPath("result.sysUser.ctime").description("创建时间"),
                                fieldWithPath("result.sysUser.utime").description("更新时间"),
                                fieldWithPath("extra").description("额外信息")
                        )
                ));
    }

    @Test
    public void testDeleteSysUser() throws Exception {
        this.mockMvc.perform(get("/user/deleteSysUser")
                .param("userId","999"))
                .andExpect(status().isOk())
                .andDo(document("deleteSysUser",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(
                        ),
                        requestParameters(
                                parameterWithName("userId").description("用户id")
                        ),
                        responseFields(
                                fieldWithPath("code").description("返回码"),
                                fieldWithPath("msg").description("返回码描述"),
                                fieldWithPath("result").description("返回码结果"),
                                fieldWithPath("extra").description("额外信息")
                        )
                ));
    }

    @Test
    public void testFindUserPage() throws Exception {
        this.mockMvc.perform(get("/user/findUserPage")
                .param("userName","")
                .param("roleId","")
                .param("organId","")
                .param("rOrganId","")
                .param("pageNum","1")
                .param("pageSize","10"))
                .andExpect(status().isOk())
                .andDo(document("findUserPage",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(
                        ),
                        requestParameters(
                                parameterWithName("userName").description("用户昵称 非必填"),
                                parameterWithName("roleId").description("角色id 非必填"),
                                parameterWithName("organId").description("机构id 非必填"),
                                parameterWithName("rOrganId").description("根机构id 非必填"),
                                parameterWithName("pageNum").description("页数 非必填 默认为1"),
                                parameterWithName("pageSize").description("每页条数 非必填 默认为10")
                        ),
                        responseFields(
                                fieldWithPath("code").description("返回码"),
                                fieldWithPath("msg").description("返回码描述"),
                                fieldWithPath("result").description("返回码结果"),
                                fieldWithPath("result.sysUserList").description("用户列表"),
                                fieldWithPath("result.sysUserList[].userId").description("用户id"),
                                fieldWithPath("result.sysUserList[].userAccount").description("用户账户"),
                                fieldWithPath("result.sysUserList[].userName").description("用户昵称"),
                                fieldWithPath("result.sysUserList[].roleIdList").description("角色id集合"),
                                fieldWithPath("result.sysUserList[].roleIdListDesc").description("角色id集合描述"),
                                fieldWithPath("result.sysUserList[].organIdList").description("机构id集合"),
                                fieldWithPath("result.sysUserList[].organIdListDesc").description("机构id集合描述"),
                                fieldWithPath("result.sysUserList[].rorganIdList").description("根机构id集合"),
                                fieldWithPath("result.sysUserList[].rorganIdListDesc").description("根机构id集合描述"),
                                fieldWithPath("result.sysUserList[].authIdList").description("权限id集合"),
                                fieldWithPath("result.sysUserList[].isForbid").description("是否禁用"),
                                fieldWithPath("result.sysUserList[].isEditable").description("是否可编辑"),
                                fieldWithPath("result.sysUserList[].ctime").description("创建时间"),
                                fieldWithPath("result.sysUserList[].cTime").description("创建时间"),
                                fieldWithPath("result.pageParam").description("分页参数"),
                                fieldWithPath("result.pageParam.pageNum").description("页数"),
                                fieldWithPath("result.pageParam.pageSize").description("每页条数"),
                                fieldWithPath("result.pageParam.pageIndex").description("分页索引"),
                                fieldWithPath("result.pageParam.itemTotalCount").description("总数"),
                                fieldWithPath("result.pageParam.pageCount").description("页总数"),
                                fieldWithPath("result.pageParam.isLoadMore").description("是否加载下一页"),
                                fieldWithPath("extra").description("额外信息")
                        )
                ));
    }

    @Test
    public void testInitPassword() throws Exception {
        this.mockMvc.perform(get("/user/initPassword")
                .param("userId","1"))
                .andExpect(status().isOk())
                .andDo(document("initPassword",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(
                        ),
                        requestParameters(
                                parameterWithName("userId").description("用户id")
                        ),
                        responseFields(
                                fieldWithPath("code").description("返回码"),
                                fieldWithPath("msg").description("返回码描述"),
                                fieldWithPath("result").description("返回码结果"),
                                fieldWithPath("extra").description("额外信息")
                        )
                ));
    }

    @Test
    public void testUpdatePassword() throws Exception {
        MvcResult mvcResult=this.mockMvc.perform(get("/common/getValidatePublicKey")).andReturn();
        MockHttpServletResponse response =mvcResult.getResponse();
        String result=response.getContentAsString();
        BaseResultEntity<Map<String,String>> baseResultEntity=JSON.parseObject(result, BaseResultEntity.class);
        String password = "123456,liweihua";
        String rsaPassword= CryptUtil.encryptRsaWithPublicKey( password,baseResultEntity.getResult().get("publicKey"));
        String publicKeyName=baseResultEntity.getResult().get("publicKeyName");
        this.mockMvc.perform(post("/user/updatePassword")
                .param("validateKeyName",publicKeyName)
                .param("password",rsaPassword)
                .header("userId","1"))
                .andExpect(status().isOk())
                .andDo(document("updatePassword",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(
                                headerWithName("userId").description("用户id (前端不用传参)")
                        ),
                        requestParameters(
                                parameterWithName("validateKeyName").description("加密key的名称"),
                                parameterWithName("password").description("新旧密码 格式：旧密码,新密码")
                        ),
                        responseFields(
                                fieldWithPath("code").description("返回码"),
                                fieldWithPath("msg").description("返回码描述"),
                                fieldWithPath("result").description("返回码结果"),
                                fieldWithPath("extra").description("额外信息")
                        )
                ));
    }

}

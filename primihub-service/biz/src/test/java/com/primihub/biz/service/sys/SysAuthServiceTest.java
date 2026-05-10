package com.primihub.biz.service.sys;

import com.primihub.biz.entity.base.BaseResultEntity;
import com.primihub.biz.entity.sys.param.AlterAuthNodeStatusParam;
import com.primihub.biz.entity.sys.param.CreateAuthNodeParam;
import com.primihub.biz.entity.sys.po.SysAuth;
import com.primihub.biz.entity.sys.vo.SysAuthNodeVO;
import com.primihub.biz.repository.primarydb.sys.SysAuthPrimarydbRepository;
import com.primihub.biz.repository.primarydb.sys.SysRolePrimarydbRepository;
import com.primihub.biz.repository.primaryredis.sys.SysAuthPrimaryRedisRepository;
import com.primihub.biz.repository.secondarydb.sys.SysAuthSecondarydbRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.doAnswer;

@ExtendWith(MockitoExtension.class)
class SysAuthServiceTest {

    @Mock private SysAuthPrimarydbRepository primaryRepo;
    @Mock private SysAuthSecondarydbRepository secondaryRepo;
    @Mock private SysAuthPrimaryRedisRepository redisRepo;
    @Mock private SysRolePrimarydbRepository roleRepo;

    @InjectMocks private SysAuthService authService;

    @Captor private ArgumentCaptor<SysAuth> sysAuthCaptor;

    private CreateAuthNodeParam rootParam;
    private CreateAuthNodeParam childParam;
    private AlterAuthNodeStatusParam alterParam;

    @BeforeEach
    void setUp() {
        rootParam = new CreateAuthNodeParam();
        rootParam.setPAuthId(0L);
        rootParam.setAuthName("Root Menu");
        rootParam.setAuthCode("ROOT");
        rootParam.setAuthType(1);
        rootParam.setAuthUrl("/root");

        childParam = new CreateAuthNodeParam();
        childParam.setPAuthId(1L);
        childParam.setAuthName("Child Menu");
        childParam.setAuthCode("CHILD");
        childParam.setAuthType(2);
        childParam.setAuthUrl("/child");

        alterParam = new AlterAuthNodeStatusParam();
        alterParam.setAuthId(1L);
        alterParam.setAuthName("Updated");
    }

    @Test
    void createAuthNode_shouldCreateRootNode() {
        doAnswer(invocation -> {
            SysAuth arg = invocation.getArgument(0);
            arg.setAuthId(100L);
            return null;
        }).when(primaryRepo).insertSysAuth(any());

        BaseResultEntity result = authService.createAuthNode(rootParam);

        assertThat(result.getCode()).isZero();
        verify(primaryRepo).insertSysAuth(sysAuthCaptor.capture());
        SysAuth saved = sysAuthCaptor.getValue();
        assertThat(saved.getPAuthId()).isZero();
        assertThat(saved.getRAuthId()).isZero();
        assertThat(saved.getAuthDepth()).isZero();
        assertThat(saved.getIsEditable()).isEqualTo(1);
        assertThat(saved.getIsDel()).isZero();
        verify(primaryRepo).updateRAuthIdAndFullPath(anyLong(), anyLong(), anyString());
        verify(redisRepo).deleteSysAuthForBfs();
    }

    @Test
    void createAuthNode_shouldCreateChildNode_withParentLookup() {
        SysAuth parentAuth = new SysAuth();
        parentAuth.setAuthId(1L);
        parentAuth.setRAuthId(1L);
        parentAuth.setAuthDepth(0);
        parentAuth.setFullPath("1");
        given(secondaryRepo.selectSysAuthByAuthId(1L)).willReturn(parentAuth);

        doAnswer(invocation -> {
            SysAuth arg = invocation.getArgument(0);
            arg.setAuthId(200L);
            return null;
        }).when(primaryRepo).insertSysAuth(any());

        BaseResultEntity result = authService.createAuthNode(childParam);

        assertThat(result.getCode()).isZero();
        verify(primaryRepo).insertSysAuth(sysAuthCaptor.capture());
        SysAuth saved = sysAuthCaptor.getValue();
        assertThat(saved.getPAuthId()).isEqualTo(1L);
        assertThat(saved.getRAuthId()).isEqualTo(1L);
        assertThat(saved.getAuthDepth()).isEqualTo(1);
    }

    @Test
    void alterAuthNodeStatus_shouldUpdate_whenNodeExistsAndEditable() {
        SysAuth existing = new SysAuth();
        existing.setAuthId(1L);
        existing.setIsEditable(1);
        given(secondaryRepo.selectSysAuthByAuthId(1L)).willReturn(existing);

        BaseResultEntity result = authService.alterAuthNodeStatus(alterParam);

        assertThat(result.getCode()).isZero();
        verify(primaryRepo).updateSysAuthExplicit(alterParam);
        verify(redisRepo).deleteSysAuthForBfs();
    }

    @Test
    void alterAuthNodeStatus_shouldFail_whenNodeNotFound() {
        given(secondaryRepo.selectSysAuthByAuthId(99L)).willReturn(null);
        alterParam.setAuthId(99L);

        BaseResultEntity result = authService.alterAuthNodeStatus(alterParam);

        assertThat(result.getCode()).isEqualTo(104);
    }

    @Test
    void alterAuthNodeStatus_shouldFail_whenNodeNotEditable() {
        SysAuth existing = new SysAuth();
        existing.setAuthId(1L);
        existing.setIsEditable(0);
        given(secondaryRepo.selectSysAuthByAuthId(1L)).willReturn(existing);

        BaseResultEntity result = authService.alterAuthNodeStatus(alterParam);

        assertThat(result.getCode()).isEqualTo(104);
    }

    @Test
    void deleteAuthNode_shouldDelete_whenExistsAndEditable() {
        SysAuth existing = new SysAuth();
        existing.setAuthId(1L);
        existing.setIsEditable(1);
        given(secondaryRepo.selectSysAuthByAuthId(1L)).willReturn(existing);

        BaseResultEntity result = authService.deleteAuthNode(1L);

        assertThat(result.getCode()).isZero();
        verify(primaryRepo).deleteSysAuth(1L);
        verify(redisRepo).deleteSysAuthForBfs();
    }

    @Test
    void deleteAuthNode_shouldFail_whenNotEditable() {
        SysAuth existing = new SysAuth();
        existing.setAuthId(1L);
        existing.setIsEditable(0);
        given(secondaryRepo.selectSysAuthByAuthId(1L)).willReturn(existing);

        BaseResultEntity result = authService.deleteAuthNode(1L);

        assertThat(result.getCode()).isEqualTo(105);
    }

    @Test
    void deleteAuthNode_shouldFail_whenNotFound() {
        given(secondaryRepo.selectSysAuthByAuthId(99L)).willReturn(null);

        BaseResultEntity result = authService.deleteAuthNode(99L);

        assertThat(result.getCode()).isEqualTo(105);
    }

    @Test
    void getAuthTree_shouldReturnTreeFromFlatList() {
        SysAuthNodeVO root = new SysAuthNodeVO();
        root.setAuthId(1L);
        root.setPAuthId(0L);
        root.setAuthName("Root");

        SysAuthNodeVO child = new SysAuthNodeVO();
        child.setAuthId(2L);
        child.setPAuthId(1L);
        child.setAuthName("Child");

        given(redisRepo.getSysAuthForBFS()).willReturn(Arrays.asList(root, child));

        List<SysAuthNodeVO> tree = authService.getSysAuthTree(new HashSet<>());

        assertThat(tree).hasSize(1);
        assertThat(tree.get(0).getAuthName()).isEqualTo("Root");
        assertThat(tree.get(0).getChildren()).hasSize(1);
        assertThat(tree.get(0).getChildren().get(0).getAuthName()).isEqualTo("Child");
    }

    @Test
    void getSysAuthForBfs_shouldReturnFromRedis_whenAvailable() {
        SysAuthNodeVO node = new SysAuthNodeVO();
        node.setAuthId(1L);
        node.setPAuthId(0L);
        given(redisRepo.getSysAuthForBFS()).willReturn(Collections.singletonList(node));

        List<SysAuthNodeVO> result = authService.getSysAuthForBfs();

        assertThat(result).hasSize(1);
    }

    @Test
    void getSysAuthForBfs_shouldFallbackToDb_whenRedisEmpty() {
        given(redisRepo.getSysAuthForBFS()).willReturn(null);
        given(secondaryRepo.selectAllSysAuthForBFS()).willReturn(Collections.emptyList());

        List<SysAuthNodeVO> result = authService.getSysAuthForBfs();

        assertThat(result).isEmpty();
        verify(redisRepo).setSysAuthForBFS(anyList());
    }

    @Test
    void getSysAuthUrlMapping_shouldMapUrlsToNodes() {
        SysAuthNodeVO node1 = new SysAuthNodeVO();
        node1.setAuthId(1L);
        node1.setAuthUrl("/path1");
        node1.setPAuthId(0L);

        SysAuthNodeVO node2 = new SysAuthNodeVO();
        node2.setAuthId(2L);
        node2.setAuthUrl("/path2");
        node2.setPAuthId(1L);

        given(redisRepo.getSysAuthForBFS()).willReturn(Arrays.asList(node1, node2));

        Map<String, SysAuthNodeVO> mapping = authService.getSysAuthUrlMapping();

        assertThat(mapping).hasSize(2);
        assertThat(mapping.get("/path1").getAuthId()).isEqualTo(1L);
        assertThat(mapping.get("/path2").getAuthId()).isEqualTo(2L);
    }

    @Test
    void getAuthTree_shouldMarkGrantedNodes() {
        SysAuthNodeVO node = new SysAuthNodeVO();
        node.setAuthId(1L);
        node.setPAuthId(0L);
        given(redisRepo.getSysAuthForBFS()).willReturn(Collections.singletonList(node));

        Set<Long> granted = new HashSet<>(Collections.singletonList(1L));
        List<SysAuthNodeVO> tree = authService.getSysAuthTree(granted);

        assertThat(tree.get(0).getIsGrant()).isEqualTo(1);
    }

    @Test
    void generateAllAuth_shouldCreateFullAuthTree() {
        final long[] counter = {1L};
        doAnswer(invocation -> {
            SysAuth arg = invocation.getArgument(0);
            arg.setAuthId(counter[0]++);
            return null;
        }).when(primaryRepo).insertSysAuth(any());

        BaseResultEntity result = authService.generateAllAuth();

        assertThat(result.getCode()).isZero();
    }
}

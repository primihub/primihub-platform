package com.primihub.biz.service.sys;

import com.anji.captcha.model.common.ResponseModel;
import com.anji.captcha.service.CaptchaService;
import com.primihub.biz.config.base.BaseConfiguration;
import com.primihub.biz.config.base.OrganConfiguration;
import com.primihub.biz.constant.RedisKeyConstant;
import com.primihub.biz.entity.base.BaseResultEntity;
import com.primihub.biz.entity.sys.param.*;
import com.primihub.biz.entity.sys.po.SysRole;
import com.primihub.biz.entity.sys.po.SysUser;
import com.primihub.biz.entity.sys.vo.SysAuthNodeVO;
import com.primihub.biz.entity.sys.vo.SysUserListVO;
import com.primihub.biz.repository.primarydb.sys.SysUserPrimarydbRepository;
import com.primihub.biz.repository.primaryredis.sys.SysCommonPrimaryRedisRepository;
import com.primihub.biz.repository.primaryredis.sys.SysUserPrimaryRedisRepository;
import com.primihub.biz.repository.secondarydb.sys.SysRoleSecondarydbRepository;
import com.primihub.biz.repository.secondarydb.sys.SysUserSecondarydbRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.web.client.RestTemplate;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class SysUserServiceTest {

    @Mock private SysUserPrimarydbRepository primaryRepo;
    @Mock private SysUserSecondarydbRepository secondaryRepo;
    @Mock private SysRoleSecondarydbRepository roleSecondaryRepo;
    @Mock private SysCommonPrimaryRedisRepository commonRedisRepo;
    @Mock private SysUserPrimaryRedisRepository userRedisRepo;
    @Mock private SysAuthService authService;
    @Mock private BaseConfiguration baseConfig;
    @Mock private OrganConfiguration organConfig;
    @Mock private RestTemplate restTemplate;
    @Mock private CaptchaService captchaService;
    @InjectMocks private SysUserService userService;

    private SysUser createTestUser() {
        SysUser user = new SysUser();
        user.setUserId(1L);
        user.setUserAccount("admin");
        user.setUserName("Admin");
        user.setUserPassword("e10adc3949ba59abbe56e057f20f883e");
        user.setRoleIdList("1");
        user.setIsEditable(1);
        user.setIsForbid(0);
        return user;
    }

    // === Login ===

    @Test
    void login_shouldSucceed_withoutRsa() {
        SysUser sysUser = createTestUser();
        sysUser.setUserPassword("e10adc3949ba59abbe56e057f20f883e");
        given(secondaryRepo.selectUserByUserAccount("admin")).willReturn(sysUser);
        given(userRedisRepo.loginVerificationNumber(1L)).willReturn(0L);
        given(baseConfig.getDefaultPasswordVector()).willReturn("");
        given(roleSecondaryRepo.selectRaByBatchRoleId(anySet())).willReturn(Collections.emptySet());
        given(authService.getSysAuthForBfs()).willReturn(Collections.emptyList());
        given(organConfig.getSysLocalOrganName()).willReturn("MyOrgan");
        given(organConfig.getSysLocalOrganId()).willReturn("myOrgan");
        given(roleSecondaryRepo.selectSysRoleByBatchRoleId(anySet())).willReturn(Collections.emptyList());
        doNothing().when(userRedisRepo).updateUserLoginStatus(anyString(), any());
        doNothing().when(userRedisRepo).deleteLoginErrorRecordNumber(anyLong());
        doNothing().when(primaryRepo).updateUserIp(anyString(), anyLong());

        LoginParam param = new LoginParam();
        param.setUserAccount("admin");
        param.setUserPassword("123456");

        BaseResultEntity result = userService.login(param, "127.0.0.1");

        assertThat(result.getCode()).isZero();
    }

    @Test
    void login_shouldFail_whenAccountNotFound() {
        given(secondaryRepo.selectUserByUserAccount("unknown")).willReturn(null);

        LoginParam param = new LoginParam();
        param.setUserAccount("unknown");

        BaseResultEntity result = userService.login(param, null);

        assertThat(result.getCode()).isEqualTo(108);
    }

    @Test
    void login_shouldFail_whenRestricted() {
        SysUser sysUser = createTestUser();
        given(secondaryRepo.selectUserByUserAccount("admin")).willReturn(sysUser);
        given(userRedisRepo.loginVerificationNumber(1L)).willReturn(12L);

        LoginParam param = new LoginParam();
        param.setUserAccount("admin");

        BaseResultEntity result = userService.login(param, null);

        assertThat(result.getCode()).isEqualTo(119);
    }

    @Test
    void login_shouldFail_wrongPassword() {
        SysUser sysUser = createTestUser();
        given(secondaryRepo.selectUserByUserAccount("admin")).willReturn(sysUser);
        given(userRedisRepo.loginVerificationNumber(1L)).willReturn(0L);
        given(baseConfig.getDefaultPasswordVector()).willReturn("vector");

        LoginParam param = new LoginParam();
        param.setUserAccount("admin");
        param.setUserPassword("wrong");

        BaseResultEntity result = userService.login(param, null);

        assertThat(result.getCode()).isEqualTo(109);
        verify(userRedisRepo).loginErrorRecordNumber(1L);
    }

    @Test
    void login_shouldRequireValidatorKey_whenExceedsThreshold() {
        SysUser sysUser = createTestUser();
        given(secondaryRepo.selectUserByUserAccount("admin")).willReturn(sysUser);
        given(userRedisRepo.loginVerificationNumber(1L)).willReturn(4L);
        given(commonRedisRepo.getRsaKey("key1")).willReturn("testPrivateKey");
        given(baseConfig.getDefaultPasswordVector()).willReturn("");

        LoginParam param = new LoginParam();
        param.setUserAccount("admin");
        param.setUserPassword("123456");
        param.setValidateKeyName("key1");

        BaseResultEntity result = userService.login(param, null);

        assertThat(result.getCode()).isEqualTo(121);
    }

    // === saveOrUpdateUser ===

    @Test
    void saveOrUpdateUser_shouldCreate() {
        given(baseConfig.getDefaultPasswordVector()).willReturn("vector");
        given(baseConfig.getDefaultPassword()).willReturn("123456");
        given(secondaryRepo.isExistUserAccount("newuser")).willReturn(false);
        doAnswer(i -> {
            SysUser u = i.getArgument(0);
            u.setUserId(99L);
            return null;
        }).when(primaryRepo).insertSysUser(any());

        SaveOrUpdateUserParam param = new SaveOrUpdateUserParam();
        param.setUserAccount("newuser");
        param.setUserName("New User");

        BaseResultEntity result = userService.saveOrUpdateUser(param);

        assertThat(result.getCode()).isZero();
        verify(primaryRepo).insertSysUser(any());
    }

    @Test
    void saveOrUpdateUser_shouldFail_whenDuplicateAccount() {
        given(secondaryRepo.isExistUserAccount("existing")).willReturn(true);

        SaveOrUpdateUserParam param = new SaveOrUpdateUserParam();
        param.setUserAccount("existing");
        param.setUserName("User");

        BaseResultEntity result = userService.saveOrUpdateUser(param);

        assertThat(result.getCode()).isEqualTo(106);
    }

    @Test
    void saveOrUpdateUser_shouldUpdate() {
        SysUser existing = createTestUser();
        given(secondaryRepo.selectSysUserByUserId(1L)).willReturn(existing);
        given(secondaryRepo.isExistUserAccount(anyString())).willReturn(false);

        SaveOrUpdateUserParam param = new SaveOrUpdateUserParam();
        param.setUserId(1L);
        param.setUserName("Updated Name");

        BaseResultEntity result = userService.saveOrUpdateUser(param);

        assertThat(result.getCode()).isZero();
    }

    // === deleteSysUser ===

    @Test
    void deleteSysUser_shouldSucceed() {
        SysUser user = createTestUser();
        given(secondaryRepo.selectSysUserByUserId(1L)).willReturn(user);

        BaseResultEntity result = userService.deleteSysUser(1L);

        assertThat(result.getCode()).isZero();
        verify(primaryRepo).updateSysUserDelStatus(1L);
    }

    @Test
    void deleteSysUser_shouldFail_whenNotFound() {
        given(secondaryRepo.selectSysUserByUserId(99L)).willReturn(null);

        BaseResultEntity result = userService.deleteSysUser(99L);

        assertThat(result.getCode()).isEqualTo(105);
    }

    // === freeze / unfreeze ===

    @Test
    void freezeUser_shouldSucceed() {
        SysUser user = createTestUser();
        given(secondaryRepo.selectSysUserByUserId(1L)).willReturn(user);

        BaseResultEntity result = userService.freezeUser(1L);

        assertThat(result.getCode()).isZero();
        verify(primaryRepo).updateUserForbidStatus(1L, 1);
    }

    @Test
    void freezeUser_shouldFail_whenAlreadyFrozen() {
        SysUser user = createTestUser();
        user.setIsForbid(1);
        given(secondaryRepo.selectSysUserByUserId(1L)).willReturn(user);

        BaseResultEntity result = userService.freezeUser(1L);

        assertThat(result.getCode()).isEqualTo(104);
    }

    @Test
    void unfreezeUser_shouldSucceed() {
        SysUser user = createTestUser();
        user.setIsForbid(1);
        given(secondaryRepo.selectSysUserByUserId(1L)).willReturn(user);

        BaseResultEntity result = userService.unfreezeUser(1L);

        assertThat(result.getCode()).isZero();
        verify(primaryRepo).updateUserForbidStatus(1L, 0);
    }

    @Test
    void unfreezeUser_shouldFail_whenNotFrozen() {
        SysUser user = createTestUser();
        user.setIsForbid(0);
        given(secondaryRepo.selectSysUserByUserId(1L)).willReturn(user);

        BaseResultEntity result = userService.unfreezeUser(1L);

        assertThat(result.getCode()).isEqualTo(104);
    }

    // === batch freeze/unfreeze ===

    @Test
    void batchFreezeUser_shouldFreezeNonFrozen() {
        SysUser user1 = createTestUser();
        user1.setUserId(1L);
        user1.setIsForbid(0);
        SysUser user2 = createTestUser();
        user2.setUserId(2L);
        user2.setIsForbid(1);
        given(secondaryRepo.selectSysUserByUserId(1L)).willReturn(user1);
        given(secondaryRepo.selectSysUserByUserId(2L)).willReturn(user2);

        BaseResultEntity result = userService.batchFreezeUser(Arrays.asList(1L, 2L));

        assertThat(result.getCode()).isZero();
        verify(primaryRepo).updateUserForbidStatus(1L, 1);
        verify(primaryRepo, never()).updateUserForbidStatus(2L, 1);
    }

    @Test
    void batchUnfreezeUser_shouldUnfreezeFrozen() {
        SysUser user1 = createTestUser();
        user1.setUserId(1L);
        user1.setIsForbid(1);
        SysUser user2 = createTestUser();
        user2.setUserId(2L);
        user2.setIsForbid(0);
        given(secondaryRepo.selectSysUserByUserId(1L)).willReturn(user1);
        given(secondaryRepo.selectSysUserByUserId(2L)).willReturn(user2);

        BaseResultEntity result = userService.batchUnfreezeUser(Arrays.asList(1L, 2L));

        assertThat(result.getCode()).isZero();
        verify(primaryRepo).updateUserForbidStatus(1L, 0);
        verify(primaryRepo, never()).updateUserForbidStatus(2L, 0);
    }

    // === findUserPage ===

    @Test
    void findUserPage_shouldReturnPaginated() {
        SysUserListVO vo = new SysUserListVO();
        vo.setUserId(1L);
        vo.setRoleIdList("1");
        vo.setUserName("admin");
        given(secondaryRepo.selectSysUserListByParam(anyMap())).willReturn(Collections.singletonList(vo));
        given(secondaryRepo.selectSysUserListCountByParam(anyMap())).willReturn(1L);
        SysRole role = new SysRole();
        role.setRoleId(1L);
        role.setRoleName("Admin");
        given(roleSecondaryRepo.selectSysRoleByBatchRoleId(anySet())).willReturn(Collections.singletonList(role));

        FindUserPageParam param = new FindUserPageParam();
        BaseResultEntity result = userService.findUserPage(param, 1, 10);

        assertThat(result.getCode()).isZero();
    }

    // === forgetPassword ===

    @Test
    void forgetPassword_shouldSucceed() {
        SysUser user = createTestUser();
        given(secondaryRepo.selectUserByUserAccount("admin")).willReturn(user);
        given(baseConfig.getDefaultPasswordVector()).willReturn("vector");

        ForgetPasswordParam param = new ForgetPasswordParam();
        param.setUserAccount("admin");
        param.setPassword("newpass");

        BaseResultEntity result = userService.forgetPassword(param);

        assertThat(result.getCode()).isZero();
        verify(primaryRepo).updateSysUserExplicit(anyMap());
    }

    // === changeUserAccount ===

    @Test
    void changeUserAccount_shouldSucceed() {
        SysUser user = createTestUser();
        given(secondaryRepo.selectSysUserByUserId(1L)).willReturn(user);
        given(commonRedisRepo.lock(anyString())).willReturn(true);
        given(secondaryRepo.selectUserByUserAccount("newaccount")).willReturn(null);

        SaveOrUpdateUserParam param = new SaveOrUpdateUserParam();
        param.setUserId(1L);
        param.setUserAccount("newaccount");

        BaseResultEntity result = userService.changeUserAccount(param);

        assertThat(result.getCode()).isZero();
        verify(primaryRepo).updateUserAccount("newaccount", 1L);
        verify(commonRedisRepo).unlock(anyString());
    }

    @Test
    void changeUserAccount_shouldFail_whenSameAccount() {
        SysUser user = createTestUser();
        given(secondaryRepo.selectSysUserByUserId(1L)).willReturn(user);

        SaveOrUpdateUserParam param = new SaveOrUpdateUserParam();
        param.setUserId(1L);
        param.setUserAccount("admin");

        BaseResultEntity result = userService.changeUserAccount(param);

        assertThat(result.getCode()).isEqualTo(104);
    }

    // === logout ===

    @Test
    void logout_shouldClearToken() {
        BaseResultEntity result = userService.logout("testToken", 1L);

        assertThat(result.getCode()).isZero();
        verify(userRedisRepo).deleteUserLoginStatus("testToken", 1L);
    }

    // === initPassword ===

    @Test
    void initPassword_shouldReset() {
        given(baseConfig.getDefaultPasswordVector()).willReturn("vector");
        given(baseConfig.getDefaultPassword()).willReturn("123456");

        BaseResultEntity result = userService.initPassword(1L);

        assertThat(result.getCode()).isZero();
        verify(primaryRepo).updateSysUserExplicit(anyMap());
    }

    // === getSysUserMap ===

    @Test
    void getSysUserMap_shouldReturnEmpty_whenNullInput() {
        Map<Long, SysUser> result = userService.getSysUserMap(null);

        assertThat(result).isEmpty();
    }

    @Test
    void getSysUser_shouldReturnUser() {
        SysUser user = createTestUser();
        given(secondaryRepo.selectSysUserByUserId(1L)).willReturn(user);

        SysUser result = userService.getSysUserById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getUserId()).isEqualTo(1L);
    }

    @Test
    void getSysUserMap_shouldReturnPopulatedMap() {
        SysUser user = createTestUser();
        given(secondaryRepo.selectSysUserByUserIdSet(anySet())).willReturn(Collections.singletonList(user));

        Map<Long, SysUser> result = userService.getSysUserMap(new HashSet<>(Collections.singletonList(1L)));

        assertThat(result).hasSize(1);
        assertThat(result.get(1L).getUserName()).isEqualTo("Admin");
    }

    @Test
    void forgetPassword_shouldFail_whenAccountNotFound() {
        given(secondaryRepo.selectUserByUserAccount("unknown")).willReturn(null);

        ForgetPasswordParam param = new ForgetPasswordParam();
        param.setUserAccount("unknown");

        BaseResultEntity result = userService.forgetPassword(param);

        assertThat(result.getCode()).isEqualTo(108);
    }

    @Test
    void changeUserAccount_shouldFail_whenLocked() {
        SysUser user = createTestUser();
        given(secondaryRepo.selectSysUserByUserId(1L)).willReturn(user);
        given(commonRedisRepo.lock(anyString())).willReturn(false);

        SaveOrUpdateUserParam param = new SaveOrUpdateUserParam();
        param.setUserId(1L);
        param.setUserAccount("newaccount");

        BaseResultEntity result = userService.changeUserAccount(param);

        assertThat(result.getCode()).isEqualTo(112);
    }

    @Test
    void changeUserAccount_shouldFail_whenAccountExists() {
        SysUser user = createTestUser();
        given(secondaryRepo.selectSysUserByUserId(1L)).willReturn(user);
        given(commonRedisRepo.lock(anyString())).willReturn(true);
        given(secondaryRepo.selectUserByUserAccount("existing")).willReturn(new SysUser());

        SaveOrUpdateUserParam param = new SaveOrUpdateUserParam();
        param.setUserId(1L);
        param.setUserAccount("existing");

        BaseResultEntity result = userService.changeUserAccount(param);

        assertThat(result.getCode()).isEqualTo(104);
        verify(commonRedisRepo).unlock(anyString());
    }

    @Test
    void relieveUserAccount_shouldSucceed() {
        SysUser user = createTestUser();
        user.setRegisterType(4);
        given(secondaryRepo.selectSysUserByUserId(1L)).willReturn(user);
        given(commonRedisRepo.lock(anyString())).willReturn(true);
        given(secondaryRepo.selectUserByUserAccount(anyString())).willReturn(null);

        BaseResultEntity result = userService.relieveUserAccount(1L);

        assertThat(result.getCode()).isZero();
        verify(primaryRepo).updateUserAccount(anyString(), eq(1L));
    }

    @Test
    void relieveUserAccount_shouldFail_whenWrongRegisterType() {
        SysUser user = createTestUser();
        user.setRegisterType(1);
        given(secondaryRepo.selectSysUserByUserId(1L)).willReturn(user);

        BaseResultEntity result = userService.relieveUserAccount(1L);

        assertThat(result.getCode()).isEqualTo(104);
    }

    @Test
    void findUserByAccount_shouldReturnUser() {
        SysUser user = createTestUser();
        given(secondaryRepo.selectUserByUserAccount("admin")).willReturn(user);

        BaseResultEntity result = userService.findUserByAccount("admin");

        assertThat(result.getCode()).isZero();
    }

    @Test
    void validateVerificationCode_shouldReturnTrue_whenMatch() {
        String key = "verification:code_1_admin";
        given(commonRedisRepo.getKey(key)).willReturn("123456");

        boolean result = userService.validateVerificationCode(1, "admin", "123456");

        assertThat(result).isTrue();
    }

    @Test
    void validateVerificationCode_shouldReturnFalse_whenExpired() {
        String key = "verification:code_1_admin";
        given(commonRedisRepo.getKey(key)).willReturn(null);

        boolean result = userService.validateVerificationCode(1, "admin", "123456");

        assertThat(result).isFalse();
    }

    @Test
    void updatePassword_shouldFail_whenInvalidKey() {
        given(commonRedisRepo.getRsaKey("badKey")).willReturn(null);

        BaseResultEntity result = userService.updatePassword(1L, "encrypted", "badKey");

        assertThat(result.getCode()).isEqualTo(107);
    }

    @Test
    void updatePassword_shouldFail_whenDecryptFails() {
        given(commonRedisRepo.getRsaKey("key1")).willReturn("privateKey");

        BaseResultEntity result = userService.updatePassword(99L, "not-encrypted", "key1");

        assertThat(result.getCode()).isEqualTo(-1);
    }

    @Test
    void getSysUser_shouldReturnNull_whenIdZero() {
        SysUser result = userService.getSysUserById(0L);

        assertThat(result).isNull();
    }
}

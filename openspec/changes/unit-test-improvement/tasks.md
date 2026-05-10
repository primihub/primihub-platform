# 单元测试 — Tasks

## Phase 1: 基础设施
- [ ] POM 添加 AssertJ 依赖
- [ ] 创建 `src/test/java` 目录结构
- [ ] 验证 `mvn test` 可执行

## Phase 2: Tier 1 — 纯逻辑 (40 tests)
### 2a: Enum 测试
- [ ] TaskStateEnumTest
- [ ] TaskTypeEnumTest
- [ ] ModelStateEnumTest
- [ ] BaseResultEnumTest
- [ ] DataResourceAuthTypeTest
- [ ] ResourceStateEnumTest
- [ ] DataFusionCopyEnumTest
- [ ] SourceEnumTest

### 2b: Convert 测试
- [ ] DataPsiConvertTest
- [ ] DataProjectConvertTest
- [ ] DataTaskConvertTest
- [ ] DataModelConvertTest
- [ ] DataResourceConvertTest
- [ ] DataSourceConvertTest
- [ ] DataReasoningConvertTest
- [ ] SysBaseConvertTest

### 2c: Util 测试
- [ ] CryptUtilTest
- [ ] SignUtilTest
- [ ] DateUtilTest
- [ ] SnowflakeIdTest

### 2d: Entity 测试
- [ ] BaseResultEntityTest

## Phase 3: Tier 2 — Service Mock (100 tests)
- [ ] SysAuthServiceTest
- [ ] SysUserServiceTest (login)
- [ ] WhitelistServiceTest
- [ ] LogManagementServiceTest
- [ ] DataTaskServiceTest
- [ ] DataPsiServiceTest
- [ ] DataProjectServiceTest
- [ ] DataResourceServiceTest
- [ ] FederatedLearningServiceTest
- [ ] SysRoleServiceTest
- [ ] SysOrganServiceTest
- [ ] TenantServiceTest
- [ ] Node*ServiceTest

## Phase 4: Tier 3 — Controller (50 tests)
- [ ] AuthControllerTest
- [ ] UserControllerTest
- [ ] PsiControllerTest
- [ ] PirControllerTest
- [ ] FederatedLearningControllerTest
- [ ] TaskControllerTest
- [ ] ResourceControllerTest
- [ ] ProjectControllerTest
- [ ] WhitelistControllerTest
- [ ] RoleControllerTest
- [ ] OrganControllerTest

## Phase 5: Tier 4 — Repository (30 tests)
- [ ] SysUserRepositoryTest
- [ ] SysAuthRepositoryTest
- [ ] DataPsiRepositoryTest
- [ ] DataTaskRepositoryTest
- [ ] DataResourceRepositoryTest
- [ ] WhitelistRepositoryTest
- [ ] DataProjectRepositoryTest

## Phase 6: CI Integration
- [ ] Maven surefire config
- [ ] Test reports
- [ ] Coverage threshold

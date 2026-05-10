# 单元测试 — Design

## 技术选型

| 层次 | 框架 | 说明 |
|------|------|------|
| 测试引擎 | JUnit 5 (Jupiter) | Spring Boot 2.3.x 自带 |
| Mock | Mockito 3.x | `@Mock` / `@InjectMocks` |
| 断言 | AssertJ 3.x | 流畅 API |
| Controller 测试 | Spring `@WebMvcTest` | MockMvc |
| Repository 测试 | `@MybatisTest` | H2 内存数据库 |

## 测试模式

### Tier 1 — 纯逻辑无依赖

```java
class DataPsiConvertTest {
    @Test
    void dataPsiReqConvertPo_shouldMapAllFields() {
        DataPsiReq req = new DataPsiReq();
        // set fields...

        DataPsi result = DataPsiConvert.DataPsiReqConvertPo(req);

        assertThat(result.getOwnOrganId()).isEqualTo(req.getOwnOrganId());
        // assert all fields...
    }
}
```

### Tier 2 — Service with Mockito

```java
@ExtendWith(MockitoExtension.class)
class SysAuthServiceTest {
    @Mock SysAuthPrimarydbRepository primaryRepo;
    @Mock SysAuthSecondarydbRepository secondaryRepo;
    @InjectMocks SysAuthService service;

    @Test
    void createAuthNode_shouldSetProperParent() {
        // given
        CreateAuthNodeParam param = ...;
        when(secondaryRepo.selectSysAuthByAuthId(any())).thenReturn(parentAuth);

        // when
        BaseResultEntity result = service.createAuthNode(param);

        // then
        assertThat(result.getCode()).isZero();
    }
}
```

## 测试约定

1. 测试类命名: `{TargetClass}Test`
2. 方法命名: `{methodName}_{scenario}_{expected}`
3. 使用 Given/When/Then 三段式
4. 一个测试方法只测一个行为

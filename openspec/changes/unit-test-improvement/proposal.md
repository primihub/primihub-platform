# 完善单元测试 — Proposal

## 为什么

PrimiHub Platform 代码库有 ~39K LOC (440 Java 文件)，**零 Java 单元测试**。现有测试仅为 Python E2E 测试，需要完整平台部署，慢且覆盖面窄。

## 范围

4 层递进策略：

| Tier | 内容 | 测试数 | 预估人天 |
|------|------|--------|---------|
| T1 | 纯逻辑 (Convert/Util/Enum/Entity) | ~40 | 2-3 |
| T2 | Service + Mock | ~100 | 5-7 |
| T3 | Controller @WebMvcTest | ~50 | 3-4 |
| T4 | Repository @MybatisTest | ~30 | 2-3 |

## 影响

- 所有模块均增加 `src/test/` 目录
- `primihub-service` parent POM 增加 AssertJ 依赖管理
- 零运行时改动，纯新增测试文件

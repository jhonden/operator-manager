# 开发规范文档目录

本目录包含项目的所有开发规范文档，每个文档负责不同的领域。

---

## 文档列表

| 文档名称 | 职责 | 适用场景 |
|---------|------|---------|
| [项目总览](../CLAUDE.md) | 项目整体介绍、目录结构、技术栈、关键文件 | 新会话开始，快速了解项目 |
| [编码规范](./development-conventions.md) | 后端/前端编码规范、测试规范、安全规范 | 编写代码时参考 |
| [服务管理规范](./service-management.md) | 服务启动/停止、验证规范 | 启动停止服务时参考 |
| [代码提交流程](./code-submission-workflow.md) | 代码验证、提交流程、Git 提交规范 | 提交代码时参考 |
| [前后端协同开发规范](./frontend-backend-collaboration.md) | 前后端协同开发流程、测试规范、最佳实践 | 开发前后端功能时参考 |
| [需求设计工作流程](./requirements-design-workflow.md) | 需求讨论、方案设计、确认归档流程 | 进行需求设计时参考 |
| [项目关键约束](./project-constraints.md) | 技术栈约束、功能范围约束、执行前确认 | 关键决策前参考 |

---

## 文档使用流程

### 新会话启动时
1. 阅读 [项目总览](../CLAUDE.md)
2. 阅读 [编码规范](./development-conventions.md)
3. 阅读 [项目关键约束](./project-constraints.md)

### 编码时
- 后端开发：参考 [编码规范](./development-conventions.md) 的后端部分
- 前端开发：参考 [编码规范](./development-conventions.md) 的前端部分
- 测试开发：参考 [编码规范](./development-conventions.md) 的测试部分

### 服务管理时
- 启动/停止服务：参考 [服务管理规范](./service-management.md)

### 提交代码时
1. 验证代码：参考 [代码提交流程](./code-submission-workflow.md)
2. 前后端协同功能：参考 [前后端协同开发规范](./frontend-backend-collaboration.md)
3. 确认后提交

### 需求设计时
1. 参考 [需求设计工作流程](./requirements-design-workflow.md)
2. 关键决策时参考 [项目关键约束](./project-constraints.md)

---

**文档维护者**: Claude AI Assistant
**最后更新**: 2026-02-27

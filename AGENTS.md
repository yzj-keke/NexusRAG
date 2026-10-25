# NexusRAG 仓库说明

## 仓库结构

NexusRAG 采用前后端分离的项目结构：

```text
NexusRAG/
├── nexus-rag-server/    # 后端项目
└── nexus-rag-web/       # 前端项目
```

- `nexus-rag-server/`：基于 Spring AI Alibaba 构建的企业知识库 RAG 后端服务。
- `nexus-rag-web/`：前端项目，在后端基础功能完成后创建。

前后端代码分别维护在各自的项目目录中，不得混放。

## 开发顺序

当前优先实现 `nexus-rag-server/`，完成后端基础功能后再开始 `nexus-rag-web/`。未明确进入前端开发阶段前，不创建或修改前端项目。

## 指令适用范围

- 本文件适用于整个 NexusRAG 仓库。
- 修改后端项目时，同时遵循 `nexus-rag-server/AGENTS.md`。
- 修改前端项目时，同时遵循 `nexus-rag-web/AGENTS.md`。
- 子项目的技术栈、模块划分、依赖关系和核心流程记录在各自目录下的 `AGENTS.md` 中，不在本文件重复维护。

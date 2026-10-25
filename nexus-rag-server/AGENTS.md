# NexusRAG 后端项目说明

## 项目定位

NexusRAG 是一个基于 Spring AI Alibaba 构建的企业知识库 RAG 项目，为企业内部运营及其他业务人员提供业务文档入库和知识问答能力。项目最终包含基础 RAG、分块优化、Query 改写、混合检索和检索结果重排，并以可独立部署、可开源使用的完整项目为交付形态。

## 技术栈

| 类型 | 选型 |
| --- | --- |
| 开发语言 | Java 17 |
| 构建工具 | Maven 3.9.9 |
| 应用框架 | Spring Boot 3.5.8 |
| AI 框架 | Spring AI Alibaba 1.1.2.0、Spring AI 1.1.2 |
| 对话模型 | `qwen3.7-flash` |
| 向量模型 | `qwen3.7-text-embedding-flash`，向量维度 1024 |
| 文档解析 | Apache Tika |
| 原始文件存储 | MinIO |
| 向量存储 | Milvus |
| 支持的文档格式 | PDF、DOC/DOCX、TXT、Markdown |

## 敏感配置

- API Key、Access Key、Secret Key、密码等敏感信息不得写入代码、配置文件、测试数据或项目文档，也不得提交到 Git。
- 本地开发时，敏感信息统一通过系统环境变量或 IDE 启动环境变量提供。
- `application.yml` 只能引用环境变量，例如 `${AI_DASHSCOPE_API_KEY}`，不得填写真实值。
- 日志、异常信息和接口响应中不得输出敏感配置。
- 当前阶段不创建 `.env` 文件。后续如果使用 `.env` 管理本地环境变量，必须将其加入 `.gitignore`，不得提交。
- 如需向开源使用者说明所需的环境变量，可以提供不包含真实值的 `.env.example`。

## 项目文档

- 后端详细设计文档统一维护在 `docs/` 目录。
- `AGENTS.md` 只记录长期有效的项目约束，不记录临时任务进度和未确认方案。
- 第一阶段的已确认背景、讨论结论和待确认事项维护在 `docs/第一阶段基础RAG方案.md`；最终实施方案和验收标准在后续确认后补充。
- 设计结论确认后同步更新对应文档；未确认内容必须明确标记为待确认，不得当作最终方案实现。
- 不提前创建没有实际内容的空文档。

## 实现约束

- 本项目遵循 Controller → Pipeline → 阶段 Service → 基础设施 Service 的调用关系，不机械套用 DDD，也不在没有实际需要时新增 Manager、Mapper、Adapter、Core 等层次或 Maven 模块。
- 不提前创建空包、空接口、空实现、占位类或 `.gitkeep`；包结构随实际代码逐步创建。
- 未明确进入第二阶段前，只实现第一阶段基础 RAG，不提前实现分块优化、Query 改写、混合检索和检索结果重排。
- 新增依赖、配置和代码必须放入职责对应的模块，不得破坏现有模块依赖方向。

## 模块划分

后端采用单体多模块架构。当前只运行一个 Spring Boot 服务，由 `nexus-rag-bootstrap` 统一启动，其余模块以普通 JAR 形式被加载。

```text
nexus-rag-server/
├── pom.xml
│   # Maven 父工程，统一管理模块、依赖版本和构建插件
│
├── nexus-rag-ingestion/
│   └── src/main/java/com/nexusrag/ingestion/
│       ├── pipeline/
│       │   # 定义并实现完整的离线文档入库流程
│       └── service/
│           │   # 定义文本分块等离线业务阶段的 Service 接口
│           └── impl/
│               # 文本分块等离线业务阶段的实现
│
├── nexus-rag-query/
│   └── src/main/java/com/nexusrag/query/
│       ├── pipeline/
│       │   # 定义并实现完整的在线检索问答流程
│       └── service/
│           │   # 定义检索编排、上下文组装等在线业务阶段的 Service 接口
│           └── impl/
│               # 检索编排、上下文组装等在线业务阶段的实现
│
├── nexus-rag-infrastructure/
│   └── src/main/java/com/nexusrag/infrastructure/
│       ├── storage/
│       │   # 原始文档存储接口及 MinIO 实现
│       ├── parser/
│       │   # 文档解析接口及 Apache Tika 实现
│       ├── vector/
│       │   # 向量写入、检索接口及 Milvus 实现
│       ├── llm/
│       │   # 向量模型、对话模型接口及阿里模型实现
│       └── config/
│           # MinIO、Milvus 和模型等基础设施配置
│
└── nexus-rag-bootstrap/
    ├── src/main/java/com/nexusrag/
    │   ├── NexusRagApplication.java
    │   │   # 唯一的 Spring Boot 启动入口
    │   └── bootstrap/
    │       ├── controller/
    │       │   # 文档上传和在线问答 HTTP 接口
    │       ├── request/
    │       │   # HTTP 请求参数
    │       ├── response/
    │       │   # HTTP 响应数据和统一响应结构
    │       ├── config/
    │       │   # 应用公共配置和模块装配
    │       ├── exception/
    │       │   # 全局异常处理
    │       └── common/
    │           # 确实被多个入口共用的常量和简单工具
    └── src/main/resources/
        └── application.yml
            # 应用、模型、MinIO 和 Milvus 配置
```

### `nexus-rag-ingestion`

负责离线文档入库业务。

- `pipeline` 包包含 `OfflinePipeline` 接口和 `OfflinePipelineImpl` 实现。接口定义完整离线流程的统一入口，实现类只负责确定各处理阶段及其执行顺序。
- `service` 包定义文本分块等属于离线业务自身的阶段 Service 接口。
- `service.impl` 包存放文本分块等离线业务阶段的实现，例如第一阶段的基础文本分块实现。
- `OfflinePipelineImpl` 面向基础设施模块提供的文件存储、文档解析、向量化和向量写入 Service 接口开发，不直接依赖具体实现类。

### `nexus-rag-query`

负责在线检索问答业务。

- `pipeline` 包包含 `OnlinePipeline` 接口和 `OnlinePipelineImpl` 实现。接口定义完整在线流程的统一入口，实现类只负责确定各处理阶段及其执行顺序。
- `service` 包定义检索编排、上下文组装等属于在线业务自身的阶段 Service 接口。
- `service.impl` 包存放检索编排、上下文组装等在线业务阶段的实现。
- `OnlinePipelineImpl` 面向基础设施模块提供的向量化、向量检索和回答生成 Service 接口开发，不直接依赖具体实现类。
- 第二阶段的 Query 改写、混合检索、结果合并和检索结果重排均属于本模块的检索链路，不新增 Maven 模块；未进入第二阶段前不创建空接口和空实现。

### `nexus-rag-infrastructure`

作为最底层的基础设施模块，定义并实现离线和在线流程需要的外部基础能力。

- `storage`：定义原始文档存储 Service 接口，并提供 MinIO 实现。
- `parser`：定义文档解析 Service 接口，并提供 Apache Tika 实现。
- `vector`：定义向量写入和检索 Service 接口，并提供 Milvus 实现。
- `llm`：定义向量化和回答生成 Service 接口，并提供 `qwen3.7-text-embedding-flash`、`qwen3.7-flash` 的调用实现。
- `config`：维护 MinIO、Milvus 和模型客户端等基础设施配置。
- 本模块只负责外部基础能力的接口定义和技术实现，不编排离线或在线业务流程。

### `nexus-rag-bootstrap`

负责 HTTP 接口、公共 Web 能力、应用配置、模块装配和应用启动。

- 文档上传 Controller 只依赖 `OfflinePipeline` 接口。
- 在线问答 Controller 只依赖 `OnlinePipeline` 接口。
- Controller 不直接调用基础设施 Service 或具体技术实现。
- 本模块不实现离线入库和在线问答的业务流程。

### Pipeline 与阶段 Service

Pipeline 是负责完整流程编排的应用 Service，采用接口与实现分离的方式，不使用抽象类或模板方法模式。

```text
Controller ──依赖──→ Pipeline 接口
Pipeline 实现 ──实现──→ Pipeline 接口
Pipeline 实现 ──依赖──→ 阶段 Service 接口
Service 实现 ──实现──→ 阶段 Service 接口
```

- Pipeline 接口只定义整条流程的统一入口。
- Pipeline 实现确定流程包含哪些阶段以及各阶段的执行顺序，不包含外部组件的具体调用代码。
- 业务模块中的 Service 接口定义文本分块、检索编排、上下文组装等业务处理能力。
- 基础设施模块中的 Service 接口定义文件存储、文档解析、向量化、向量读写和模型生成等外部基础能力。
- Service 实现完成该阶段的业务处理或外部组件调用。
- Service 接口以 `Service` 结尾，不使用 `I` 前缀；具体实现根据职责或所用技术命名。

### 模块依赖关系

以下结构表示上层 Maven 模块依赖下层 Maven 模块：

```text
                         nexus-rag-bootstrap
                                  │
                    ┌─────────────┴─────────────┐
                    ▼                           ▼
        nexus-rag-ingestion             nexus-rag-query
                    │                           │
                    └─────────────┬─────────────┘
                                  ▼
                   nexus-rag-infrastructure
```

依赖约束：

- `nexus-rag-ingestion` 与 `nexus-rag-query` 互不依赖。
- `nexus-rag-ingestion`、`nexus-rag-query` 只依赖 `nexus-rag-infrastructure` 中的 Service 接口，不直接依赖具体实现类。
- `nexus-rag-infrastructure` 不依赖 `nexus-rag-ingestion` 或 `nexus-rag-query`。
- 任何业务模块都不依赖 `nexus-rag-bootstrap`。
- 模块依赖始终保持自上而下，不允许形成循环依赖。
- 当前由 `nexus-rag-bootstrap` 组装并启动一个服务；未来需要拆分时，分别为离线和在线业务增加独立启动模块，并按服务需要引入或拆分基础设施能力，不重写其 Pipeline 和核心业务代码。

## 核心流程

### 离线文档入库

第一阶段：

```text
用户上传文档
→ 系统生成文档唯一标识
→ 保存原始文档
→ 提取文档文本内容
→ 将文本切分为多个片段
→ 将文本片段转换为向量
→ 保存文本片段、向量和来源信息
→ 返回文档入库结果
```

第一阶段同步完成整条入库流程，文档唯一标识使用 UUID v4，不引入 MySQL、MQ、异步任务和处理状态查询。

第二阶段保持整体流程不变，完成文本分块优化。新的分块方式通过替换文本分块阶段的 Service 实现接入，不改变 `OfflinePipeline` 对外入口和 Maven 模块关系。

### 在线检索问答

第一阶段：

```text
用户提交问题
→ 将问题转换为向量
→ 检索与问题相关的文本片段
→ 将用户问题和检索内容组装为上下文
→ 调用大模型生成回答
→ 向用户返回回答
```

第二阶段：

```text
用户提交问题
→ 对问题进行改写或补充
→ 同时进行语义检索和关键词检索
→ 合并检索结果
→ 对候选内容重新排序
→ 选择相关度最高的内容组装上下文
→ 调用大模型生成回答
→ 向用户返回回答
```

第二阶段必须实现 Query 改写、混合检索和检索结果重排。优化在在线模块内部完成，不改变 `OnlinePipeline` 对外入口和 Maven 模块关系。

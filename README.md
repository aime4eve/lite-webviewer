# Nexus-Lite 知识预览系统

## 项目介绍

Nexus-Lite 是一个轻量级的知识预览系统，旨在为用户提供高效、便捷的文档预览服务。系统能够自动扫描指定目录下的文档，生成结构化索引，并支持多种文件格式的在线预览，帮助用户快速获取文档内容，提高知识管理效率。

### 核心价值
- 统一的文档预览平台，支持多种文件格式
- 高效的文档扫描和索引机制
- 结构化的文档内容呈现
- 简洁易用的API接口
- 科技风格的现代化UI设计

## 功能说明

### 1. 文件扫描功能
- 自动扫描指定目录下的文档文件
- 支持定时扫描和手动触发扫描
- 智能检测文件变化，增量更新索引
- 支持跳过黑名单目录（如.git、node_modules等）
- 安全的符号链接处理

### 2. 多格式文档预览
- PDF文件：直接预览
- DOCX文件：转换为HTML格式预览
- Markdown文件：转换为HTML格式预览，支持代码高亮
- CSV文件：转换为HTML表格预览
- HTML文件：安全清理后预览
- SVG文件：安全清理后预览
- XLSX文件：转换为HTML表格预览

### 3. 文档结构化
- 生成TOON结构（Tree of Organizational Nodes）
- 支持文档内部导航
- 提供文档元数据信息

### 4. 全文检索（规划中）
- 基于Elasticsearch的全文检索
- 支持文档内容、文件名、元数据的检索
- 支持精确匹配、模糊匹配、短语匹配
- 中文和英文分词支持

### 5. 高性能缓存
- 使用Caffeine缓存提升预览性能
- 缓存预览内容和索引信息
- 智能缓存失效策略

## 技术栈

### 后端技术栈
- **核心框架**：Spring Boot 3.2.0
- **编程语言**：Java 17
- **数据库**：H2 2.2.224（文件索引和元数据存储）
- **ORM框架**：Spring Data JPA 3.2.0
- **缓存**：Caffeine 3.1.8
- **文档处理**：
  - PDFBox 3.0.1（PDF文件处理）
  - Apache POI 5.2.5（Office文件处理）
  - Mammoth 1.11.0（DOCX转HTML）
  - Flexmark 0.64.8（Markdown处理）
  - OpenCSV 5.8（CSV文件处理）
  - Tika 2.9.1（内容检测）
- **构建工具**：Maven 3.8.6

### 前端技术栈
- **框架**：React 18.2.0
- **UI组件库**：Ant Design 5.12.0
- **构建工具**：Vite 5.0.8
- **状态管理**：Zustand 4.4.7
- **文档处理**：
  - pdfjs-dist 3.11.174（PDF预览）
  - react-markdown 9.0.1（Markdown渲染）
  - react-syntax-highlighter 15.5.0（代码高亮）
  - mermaid 11.12.1（图表渲染）
  - dompurify 3.0.7（HTML清理）

## 集成说明

### RESTful API接口

#### 文档扫描相关接口

1. **触发扫描**
   - URL: `/api/index/scan`
   - Method: `POST`
   - 描述: 手动触发文件扫描
   - 响应: 扫描任务ID和状态

2. **获取扫描状态**
   - URL: `/api/index/scan/status/{taskId}`
   - Method: `GET`
   - 描述: 获取扫描任务的执行状态
   - 响应: 扫描任务状态信息

#### 文档索引相关接口

1. **获取文件索引**
   - URL: `/api/index/files`
   - Method: `GET`
   - 描述: 获取所有文件的索引信息
   - 响应: 文件索引列表

2. **获取目录树**
   - URL: `/api/index/directory-tree`
   - Method: `GET`
   - 描述: 获取目录树结构
   - 响应: 目录树JSON结构

#### 文档预览相关接口

1. **生成预览**
   - URL: `/api/documents/preview/{filePath}`
   - Method: `GET`
   - 描述: 生成并获取文件预览内容
   - 响应: 预览内容（根据文件类型返回不同格式）

2. **获取文档元数据**
   - URL: `/api/documents/metadata/{filePath}`
   - Method: `GET`
   - 描述: 获取文档的元数据信息
   - 响应: 文档元数据JSON

3. **获取文档TOON结构**
   - URL: `/api/documents/toon/{filePath}`
   - Method: `GET`
   - 描述: 获取文档的TOON结构
   - 响应: TOON结构JSON

### 集成示例

#### Java集成示例

```java
// 使用RestTemplate调用API
RestTemplate restTemplate = new RestTemplate();

// 触发扫描
String scanUrl = "http://localhost:8080/api/index/scan";
ResponseEntity<Map> scanResponse = restTemplate.postForEntity(scanUrl, null, Map.class);
String taskId = (String) scanResponse.getBody().get("taskId");

// 获取文件预览
String previewUrl = "http://localhost:8080/api/documents/preview/{filePath}";
Map<String, String> params = Collections.singletonMap("filePath", "/path/to/document.pdf");
ResponseEntity<byte[]> previewResponse = restTemplate.getForEntity(previewUrl, byte[].class, params);
```

#### JavaScript集成示例

```javascript
// 使用fetch API调用

// 触发扫描
async function triggerScan() {
  const response = await fetch('http://localhost:8080/api/index/scan', {
    method: 'POST'
  });
  const data = await response.json();
  return data.taskId;
}

// 获取文件预览
async function getFilePreview(filePath) {
  const response = await fetch(`http://localhost:8080/api/documents/preview/${encodeURIComponent(filePath)}`);
  const blob = await response.blob();
  return URL.createObjectURL(blob);
}
```

## 安装部署

### 开发环境设置

#### 前提条件
- JDK 17 或更高版本
- Maven 3.8.6 或更高版本
- Node.js 18.x 或更高版本
- npm 8.x 或更高版本

#### 快速启动开发环境

项目提供了一键启动脚本，可以同时启动前端开发服务器和后端Spring Boot应用：

```bash
# 赋予脚本执行权限
chmod +x start-dev.sh

# 运行启动脚本
./start-dev.sh
```

脚本会自动：
1. 检查必要的命令是否安装
2. 启动前端开发服务器（端口：5173）
3. 启动后端Spring Boot应用（端口：8080）

#### 手动启动

如果需要分别启动前后端：

**启动后端：**

```bash
# 在项目根目录
mvn spring-boot:run
```

**启动前端：**

```bash
# 进入前端目录
cd frontend

# 安装依赖
npm install

# 启动开发服务器
npm run dev
```

### 生产环境部署

#### 构建项目

```bash
# 运行构建脚本
chmod +x build.sh
./build.sh
```

或者手动构建：

```bash
# 构建前端
cd frontend
npm install
npm run build
cd ..

# 构建后端
mvn clean package -DskipTests
```

#### 部署后端应用

```bash
# 运行打包后的jar文件
java -jar target/lite-webviewer-0.1.0.jar
```

#### 配置说明

主要配置文件：`src/main/resources/application.properties`

关键配置项：

```properties
# 扫描目录配置
nexus.scan.root-dir=./data
nexus.scan.blacklist-dir=.git,node_modules,.svn,.DS_Store

# 定时扫描配置
nexus.scan.cron=0 0 */6 * * *  # 每6小时扫描一次

# 缓存配置
spring.cache.caffeine.spec=maximumSize=1000,expireAfterWrite=1h

# 服务器配置
server.port=8080
server.servlet.context-path=/

# H2数据库配置
spring.datasource.url=jdbc:h2:file:./data/index.db
spring.datasource.driverClassName=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
spring.jpa.hibernate.ddl-auto=update
spring.h2.console.enabled=true
spring.h2.console.path=/h2-console
```

## 协同开发

### 开发规范

#### 代码风格
- **后端**：遵循Google Java Style Guide
- **前端**：遵循ESLint配置，使用Prettier保持代码风格一致

#### 提交规范

使用语义化提交信息（Semantic Commit Messages）：

```
<type>[optional scope]: <description>

[optional body]

[optional footer(s)]
```

类型说明：
- `feat`: 新增功能
- `fix`: 修复bug
- `docs`: 文档更新
- `style`: 代码风格调整（不影响功能）
- `refactor`: 代码重构（不新增功能，不修复bug）
- `perf`: 性能优化
- `test`: 测试相关
- `chore`: 构建过程或辅助工具变动

#### 分支管理

- `main`: 主分支，稳定版本
- `develop`: 开发分支，集成所有功能开发
- `feature/*`: 功能开发分支
- `bugfix/*`: Bug修复分支
- `release/*`: 版本发布分支

### 开发流程

1. **创建分支**：从`develop`分支创建功能分支
   ```bash
   git checkout develop
   git pull
   git checkout -b feature/your-feature-name
   ```

2. **开发功能**：完成功能开发和单元测试

3. **提交代码**：使用规范的提交信息
   ```bash
   git add .
   git commit -m "feat: 描述你的功能"
   ```

4. **推送分支**：
   ```bash
   git push origin feature/your-feature-name
   ```

5. **创建Merge Request**：提交MR到`develop`分支

6. **代码审查**：团队成员进行代码审查

7. **合并代码**：审查通过后合并到`develop`分支

### 版本管理

- 使用语义化版本号（Semantic Versioning）：`MAJOR.MINOR.PATCH`
- 当有不兼容的API变更时，增加`MAJOR`版本
- 当增加向下兼容的新功能时，增加`MINOR`版本
- 当修复向下兼容的bug时，增加`PATCH`版本

### 测试规范

- 单元测试覆盖率目标：≥80%
- 关键功能必须编写集成测试
- 提交代码前确保所有测试通过

```bash
# 运行后端测试
mvn test

# 运行前端测试
cd frontend
npm test
```

## 问题反馈

如遇到问题或有建议，请通过以下方式反馈：

- 提交Issue：在项目仓库中创建新的Issue
- 联系维护者：[维护者邮箱或联系方式]

## 许可证

[此处将放置项目许可证信息]

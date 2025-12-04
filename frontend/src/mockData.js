// 模拟文件树数据
export const mockFiles = [
  'docs/README.md',
  'docs/architecture.md',
  'docs/api.md',
  'docs/changelog.md',
  'src/App.jsx',
  'src/main.jsx',
  'src/components/EnhancedMermaidBlock.jsx',
  'src/components/MarkdownPreview.jsx',
  'src/components/PreviewPane.jsx',
  'src/components/FileTree.jsx',
  'src/components/ErrorBoundary.jsx',
  'src/App.css',
  'src/index.css',
  'package.json',
  'vite.config.js',
  'README.md'
];

// 模拟Mermaid图表数据
export const mockMarkdownContent = `# Mermaid 图表演示

这是一个Mermaid图表的演示文档。

## 流程图

\`\`\`mermaid
flowchart TD
    A[开始] --> B{条件A}
    B -->|是| C[结果1]
    B -->|否| D[结果2]
    C --> E[结束]
    D --> E
\`\`\`

## 序列图

\`\`\`mermaid
sequenceDiagram
    participant Alice
    participant Bob
    Alice->>Bob: 你好，Bob！
    Bob-->>Alice: 你好，Alice！
    Alice->>Bob: 我需要你的帮助
    Bob-->>Alice: 当然，我很乐意帮助你
\`\`\`

## 甘特图

\`\`\`mermaid
gantt
    dateFormat  YYYY-MM-DD
    title 项目计划
    section 设计
    需求分析     :a1, 2023-01-01, 30d
    界面设计     :after a1, 20d
    架构设计     : 20d
    section 开发
    前端开发     :2023-02-01, 40d
    后端开发     :2023-02-15, 40d
    测试         :2023-03-20, 20d
    section 部署
    上线准备     :2023-04-01, 10d
    正式上线     :2023-04-11, 2d
\`\`\`

## 类图

\`\`\`mermaid
classDiagram
    class Animal {
        +String name
        +eat()
        +sleep()
    }
    class Dog {
        +bark()
    }
    class Cat {
        +meow()
    }
    Animal <|-- Dog
    Animal <|-- Cat
\`\`\`

## 状态图

\`\`\`mermaid
stateDiagram
    [*] --> 空闲
    空闲 --> 运行 : 启动
    运行 --> 暂停 : 暂停
    暂停 --> 运行 : 继续
    运行 --> 停止 : 停止
    暂停 --> 停止 : 停止
    停止 --> 空闲 : 重置
\`\`\`
`;

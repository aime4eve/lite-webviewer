import React, { useEffect, useRef } from 'react';
import ReactMarkdown from 'react-markdown';
import remarkGfm from 'remark-gfm';
import rehypeRaw from 'rehype-raw';
import rehypeSlug from 'rehype-slug';
import rehypeAutolinkHeadings from 'rehype-autolink-headings';
import { Prism as SyntaxHighlighter } from 'react-syntax-highlighter';
import { tomorrow } from 'react-syntax-highlighter/dist/esm/styles/prism';
import EnhancedMermaidBlock from './EnhancedMermaidBlock';
// 移除react-window导入，因为它在当前版本中存在导入问题
// import { FixedSizeList as List } from 'react-window';

/**
 * Markdown预览核心组件
 * 支持完整的Markdown语法、GFM扩展、代码高亮、Mermaid图表渲染等功能
 */
const MarkdownPreview = ({ content, searchKeywords, onScroll, className }) => {
  const previewRef = useRef(null);

  // 高亮关键词函数
  const highlightKeywords = (text) => {
    if (!searchKeywords || !text || typeof text !== 'string') {
      return text;
    }
    
    const keywords = searchKeywords.trim().split(/\s+/).filter(k => k.length > 0);
    if (keywords.length === 0) {
      return text;
    }
    
    const pattern = new RegExp(`(${keywords.map(keyword => keyword.replace(/[.*+?^${}()|[\]\\]/g, '\\$&')).join('|')})`, 'gi');
    return text.replace(pattern, '<em class="highlight">$1</em>');
  };

  // 自定义文本高亮组件
  const HighlightText = ({ children }) => {
    if (typeof children === 'string') {
      const highlighted = highlightKeywords(children);
      return <span dangerouslySetInnerHTML={{ __html: highlighted }} />;
    }
    return children;
  };

  // 监听滚动事件，用于双向同步滚动
  useEffect(() => {
    const handleScroll = (e) => {
      if (onScroll) {
        const scrollTop = e.target.scrollTop;
        const scrollHeight = e.target.scrollHeight;
        const clientHeight = e.target.clientHeight;
        const scrollPercentage = scrollTop / (scrollHeight - clientHeight);
        onScroll(scrollPercentage);
      }
    };

    const previewElement = previewRef.current;
    if (previewElement) {
      previewElement.addEventListener('scroll', handleScroll);
      return () => previewElement.removeEventListener('scroll', handleScroll);
    }
  }, [onScroll]);

  // 滚动到指定位置，用于双向同步滚动
  const scrollToPosition = (scrollPercentage) => {
    const previewElement = previewRef.current;
    if (previewElement) {
      const scrollHeight = previewElement.scrollHeight;
      const clientHeight = previewElement.clientHeight;
      const scrollTop = scrollPercentage * (scrollHeight - clientHeight);
      previewElement.scrollTop = scrollTop;
    }
  };

  // 暴露滚动方法给父组件
  useEffect(() => {
    if (previewRef.current && typeof onScroll === 'function') {
      // 将滚动方法暴露给父组件，以便父组件可以控制滚动
      previewRef.current.scrollToPosition = scrollToPosition;
    }
  }, [onScroll]);

  return (
    <div 
      ref={previewRef} 
      className={`markdown-preview ${className || ''}`}
      style={{
        padding: '20px',
        height: '100%',
        overflowY: 'auto',
        // 添加响应式样式
        fontSize: '16px',
        lineHeight: '1.6',
        maxWidth: '100%',
        margin: '0 auto',
        boxSizing: 'border-box'
      }}
    >
      <ReactMarkdown
        remarkPlugins={[remarkGfm]}
        rehypePlugins={[
          rehypeRaw,
          rehypeSlug,
          [rehypeAutolinkHeadings, {
            behavior: 'wrap',
            properties: {
              className: 'heading-link',
              ariaLabel: 'Link to this heading'
            }
          }]
        ]}
        components={{
          code({ inline, className, value, ...props }) {
            const match = /language-(\w+)/.exec(className || '');
            if (!inline && match && match[1] === 'mermaid') {
              return <EnhancedMermaidBlock chart={value} />;
            }
            return !inline && match ? (
              <SyntaxHighlighter
                style={tomorrow}
                language={match[1]}
                PreTag="div"
                showLineNumbers={true}
                lineNumberStyle={{
                  textAlign: 'right',
                  paddingRight: '15px',
                  userSelect: 'none',
                  opacity: 0.7
                }}
                {...props}
              >
                {value.replace(/\n$/, '')}
              </SyntaxHighlighter>
            ) : (
              <code className={className} {...props}>
                {value}
              </code>
            );
          },
          // 应用高亮到所有文本内容
          p: ({ children, ...props }) => <p {...props}><HighlightText>{children}</HighlightText></p>,
          h1: ({ children, ...props }) => <h1 {...props}><HighlightText>{children}</HighlightText></h1>,
          h2: ({ children, ...props }) => <h2 {...props}><HighlightText>{children}</HighlightText></h2>,
          h3: ({ children, ...props }) => <h3 {...props}><HighlightText>{children}</HighlightText></h3>,
          h4: ({ children, ...props }) => <h4 {...props}><HighlightText>{children}</HighlightText></h4>,
          h5: ({ children, ...props }) => <h5 {...props}><HighlightText>{children}</HighlightText></h5>,
          h6: ({ children, ...props }) => <h6 {...props}><HighlightText>{children}</HighlightText></h6>,
          li: ({ children, ...props }) => <li {...props}><HighlightText>{children}</HighlightText></li>,
          span: ({ children, ...props }) => <span {...props}><HighlightText>{children}</HighlightText></span>,
          strong: ({ children, ...props }) => <strong {...props}><HighlightText>{children}</HighlightText></strong>,
          em: ({ children, ...props }) => <em {...props}><HighlightText>{children}</HighlightText></em>,
          // 为链接添加target="_blank"和rel="noopener noreferrer"
          a: ({ children, href, ...props }) => (
            <a 
              href={href} 
              target="_blank" 
              rel="noopener noreferrer" 
              {...props}
            >
              {children}
            </a>
          )
        }}
      >
        {content}
      </ReactMarkdown>
    </div>
  );
};

export default MarkdownPreview;

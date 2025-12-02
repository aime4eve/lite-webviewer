import React, { useState, useEffect, useCallback, useRef } from 'react';
import { Spin, Empty, Alert, Typography, Switch } from 'antd';
import ReactMarkdown from 'react-markdown';
import remarkGfm from 'remark-gfm';
import rehypeRaw from 'rehype-raw';
import mermaid from 'mermaid';
import { Prism as SyntaxHighlighter } from 'react-syntax-highlighter';
import { tomorrow } from 'react-syntax-highlighter/dist/esm/styles/prism';

const { Title, Text } = Typography;

// 独立的Mermaid组件 - 避免样式和逻辑冲突
const EnhancedMermaidBlock = ({ chart }) => {
  // 简化状态管理
  const [svg, setSvg] = useState('');
  const [isRendering, setIsRendering] = useState(true);
  const containerRef = useRef(null);
  
  // 直接使用内联样式控制所有UI元素，确保样式正确应用
  useEffect(() => {
    // 配置Mermaid实例
    mermaid.initialize({
      startOnLoad: false,
      theme: 'default',
      logLevel: 3,
      securityLevel: 'loose',
      flowchart: {
        useMaxWidth: true,
        htmlLabels: true
      },
      sequence: {
        showSequenceNumbers: false
      },
      gantt: {
        useWidth: 100
      }
    });
    
    // 渲染函数
    const renderMermaidChart = async () => {
      try {
        setIsRendering(true);
        // 创建唯一ID
        const id = `mermaid-chart-${Date.now()}`;
        // 直接渲染图表
        const { svg: renderedSvg } = await mermaid.render(id, chart);
        
        // 确保SVG包含必要的类和样式
        const processedSvg = renderedSvg
          .replace('<svg', '<svg class="mermaid-svg"')
          .replace('viewBox', 'preserveAspectRatio="xMidYMid meet" viewBox');
          
        setSvg(processedSvg);
      } catch (error) {
        console.error('Mermaid rendering error:', error);
        // 错误显示
        setSvg(`<div class="mermaid-error" style="
          padding: 20px;
          color: #dc3545;
          background: #f8d7da;
          border: 1px solid #f5c6cb;
          border-radius: 8px;
          font-family: Arial, sans-serif;
          font-size: 14px;
          text-align: center;
        ">图表渲染失败: ${error.message}</div>`);
      } finally {
        setIsRendering(false);
      }
    };
    
    renderMermaidChart();
  }, [chart]);
  
  // 添加简单的交互样式控制
  const [showControls, setShowControls] = useState(true);
  const [scale, setScale] = useState(1);
  
  const zoomIn = () => setScale(s => Math.min(s * 1.1, 2));
  const zoomOut = () => setScale(s => Math.max(s * 0.9, 0.5));
  const resetZoom = () => setScale(1);
  
  // 基本容器样式 - 确保应用
  const containerStyle = {
    position: 'relative',
    backgroundColor: '#ffffff',
    border: '2px solid #e2e8f0',
    borderRadius: '12px',
    padding: '20px',
    margin: '20px 0',
    boxShadow: '0 4px 6px rgba(0, 0, 0, 0.1)',
    minHeight: '300px',
    overflow: 'auto',
    transition: 'all 0.3s ease'
  };
  
  // 控件样式
  const controlsStyle = {
    position: 'absolute',
    top: '10px',
    right: '10px',
    display: 'flex',
    gap: '6px',
    zIndex: 10,
    backgroundColor: 'rgba(255, 255, 255, 0.95)',
    padding: '8px',
    borderRadius: '8px',
    boxShadow: '0 2px 4px rgba(0, 0, 0, 0.1)',
    border: '1px solid #e2e8f0'
  };
  
  // 按钮样式
  const buttonStyle = {
    backgroundColor: '#64748b',
    border: 'none',
    borderRadius: '4px',
    padding: '6px 8px',
    cursor: 'pointer',
    color: 'white',
    fontSize: '12px',
    minWidth: '32px',
    height: '32px',
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
    transition: 'all 0.2s ease'
  };
  
  // 图表样式
  const chartStyle = {
    width: '100%',
    height: 'auto',
    display: 'flex',
    justifyContent: 'center',
    alignItems: 'center',
    transform: `scale(${scale})`,
    transformOrigin: 'center center',
    transition: 'transform 0.3s ease',
    padding: '20px 0',
    minHeight: '250px'
  };
  
  return (
    <div ref={containerRef} style={containerStyle}>
      {/* 控制按钮 */}
      {showControls && (
        <div style={controlsStyle}>
          <button 
            style={{ ...buttonStyle, backgroundColor: '#2563eb' }}
            onClick={zoomIn}
            title="放大"
          >
            +
          </button>
          <button 
            style={{ ...buttonStyle, backgroundColor: '#64748b' }}
            onClick={zoomOut}
            title="缩小"
          >
            -
          </button>
          <button 
            style={{ ...buttonStyle, backgroundColor: '#10b981' }}
            onClick={resetZoom}
            title="重置"
          >
            ⟳
          </button>
        </div>
      )}
      
      {/* 图表容器 */}
      <div 
        style={chartStyle}
        dangerouslySetInnerHTML={{ __html: isRendering ? 
          '<div style="text-align: center; padding: 40px; color: #64748b;">图表加载中...</div>' : 
          svg 
        }}
      />
    </div>
  );
}

// Preview component for different file types
const PreviewPane = ({ filePath, loading: propLoading, error: propError, searchKeywords }) => {
  const [content, setContent] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [textOnly, setTextOnly] = useState(false);
  useEffect(() => { 
    mermaid.initialize({ 
      startOnLoad: false, 
      theme: 'default',
      fontFamily: 'Inter, sans-serif',
      securityLevel: 'loose',
      flowchart: {
        htmlLabels: true,
        curve: 'basis'
      },
      themeVariables: {
        primaryColor: '#2c3e50',
        primaryTextColor: '#2c3e50',
        primaryBorderColor: '#2c3e50',
        lineColor: '#95a5a6',
        secondaryColor: '#34495e',
        tertiaryColor: '#e74c3c'
      }
    }); 
  }, []);
  
  // Highlight keywords in text
  const highlightKeywords = (text) => {
    if (!searchKeywords || !text || typeof text !== 'string') {
      return text;
    }
    
    const keywords = searchKeywords.trim().split(/\s+/).filter(k => k.length > 0);
    if (keywords.length === 0) {
      return text;
    }
    
    // Create regex pattern for all keywords, case insensitive
    const pattern = new RegExp(`(${keywords.map(keyword => keyword.replace(/[.*+?^${}()|[\]\\]/g, '\\$&')).join('|')})`, 'gi');
    
    // Replace keywords with highlight tags
    return text.replace(pattern, '<em class="highlight">$1</em>');
  };
  
  const fetchPreview = useCallback(async () => {
    setLoading(true);
    setError(null);
    
    try {
      // 确保文件路径使用正确的格式 - 去除可能的前导斜杠，使路径相对于文档根目录
      let formattedFilePath = filePath;
      if (formattedFilePath && formattedFilePath.startsWith('/')) {
        formattedFilePath = formattedFilePath.substring(1);
      }
      
      const ext = (formattedFilePath || '').split('.').pop().toLowerCase();
      let previewContent = null;
      
      if (ext === 'html' || ext === 'htm') {
        const fsUrl = `/api/v1/fs/${encodeURIComponent(formattedFilePath)}`;
        if (textOnly) {
          const resp = await fetch(fsUrl);
          if (!resp.ok) throw new Error(`Failed to fetch HTML: ${resp.statusText}`);
          const htmlText = await resp.text();
          const plain = extractPlainText(htmlText);
          setContent(plain);
        } else {
          // Fetch HTML content, add highlights, then create Blob URL
          const resp = await fetch(fsUrl);
          if (!resp.ok) throw new Error(`Failed to fetch HTML: ${resp.statusText}`);
          let htmlText = await resp.text();
          
          // Add keyword highlights to HTML content
          if (searchKeywords) {
            htmlText = highlightHTML(htmlText);
          }
          
          // Create Blob URL for the highlighted HTML
          const blob = new Blob([htmlText], { type: 'text/html' });
          previewContent = URL.createObjectURL(blob);
          setContent(previewContent);
        }
        return;
      }
      
      // 确保使用正确的URL编码和文件路径格式
      const previewUrl = `/api/v1/document/preview?filePath=${encodeURIComponent(formattedFilePath)}`;
      const response = await fetch(previewUrl);
      
      if (!response.ok) {
        throw new Error(`Failed to fetch preview: ${response.statusText}`);
      }
      
      const contentType = response.headers.get('content-type');
      
      if (contentType?.includes('text/html')) {
        previewContent = await response.text();
      } else if (contentType?.includes('application/pdf')) {
        const blob = await response.blob();
        previewContent = URL.createObjectURL(blob);
      } else if (contentType?.includes('image/svg+xml')) {
        previewContent = await response.text();
      } else if (contentType?.includes('text/csv')) {
        previewContent = await response.text();
      } else if (contentType?.includes('text/markdown') || formattedFilePath.endsWith('.md')) {
        previewContent = await response.text();
      } else {
        previewContent = await response.text();
      }
      
      setContent(previewContent);
    } catch (err) {
      setError(`Failed to load preview: ${err.message}`);
    } finally {
      setLoading(false);
    }
  }, [filePath, textOnly, searchKeywords]);

  useEffect(() => {
    if (!filePath) {
      setContent(null);
      setError(null);
      return;
    }
    fetchPreview();
  }, [filePath, fetchPreview]);
  
  
  
  // Get file extension
  const getFileExtension = () => {
    if (!filePath) return '';
    return filePath.split('.').pop().toLowerCase();
  };
  
  // Render markdown content
  const renderMarkdown = (mdContent) => {
    
    // Custom component to apply highlighting to text content
    const HighlightText = ({ children }) => {
      if (typeof children === 'string') {
        const highlighted = highlightKeywords(children);
        return <span dangerouslySetInnerHTML={{ __html: highlighted }} />;
      }
      return children;
    };
    
    return (
      <div className="markdown-preview" style={{ padding: '20px' }}>
        <ReactMarkdown
          remarkPlugins={[remarkGfm]}
          rehypePlugins={[rehypeRaw]}
          components={{
            code({ inline, className, children, ...props }) {
              const match = /language-(\w+)/.exec(className || '');
              if (!inline && match && match[1] === 'mermaid') {
                return <EnhancedMermaidBlock chart={String(children)} />;
              }
              return !inline && match ? (
                <SyntaxHighlighter
                  style={tomorrow}
                  language={match[1]}
                  PreTag="div"
                  {...props}
                >
                  {String(children).replace(/\n$/, '')}
                </SyntaxHighlighter>
              ) : (
                <code className={className} {...props}>
                  {children}
                </code>
              );
            },
            // Apply highlighting to all text content
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
          }}
        >
          {mdContent}
        </ReactMarkdown>
      </div>
    );
  };
  
  // Render PDF content
  const renderPDF = (pdfUrl) => {
    return (
      <div className="pdf-preview" style={{ width: '100%', height: '80vh' }}>
        <iframe
          src={pdfUrl}
          width="100%"
          height="100%"
          title="PDF Preview"
          style={{ border: 'none' }}
        />
      </div>
    );
  };
  
  // Render CSV content as table
  const renderCSV = (csvContent) => {
    const lines = csvContent.split('\n').filter(line => line.trim());
    if (lines.length === 0) return <Empty description="CSV file is empty" />;
    
    const headers = lines[0].split(',').map(h => h.trim());
    const rows = lines.slice(1).map((line, index) => {
      const cells = line.split(',').map(cell => cell.trim());
      return (
        <tr key={index}>
          {cells.map((cell, cellIndex) => (
            <td key={cellIndex} style={{ padding: '8px', border: '1px solid #e8e8e8' }}>
              <div dangerouslySetInnerHTML={{ __html: highlightKeywords(cell) }} />
            </td>
          ))}
        </tr>
      );
    });
    
    return (
      <div className="csv-preview" style={{ padding: '20px', overflowX: 'auto' }}>
        <table style={{ borderCollapse: 'collapse', width: '100%', border: '1px solid #e8e8e8' }}>
          <thead>
            <tr style={{ backgroundColor: '#fafafa' }}>
              {headers.map((header, index) => (
                <th key={index} style={{ padding: '12px 8px', border: '1px solid #e8e8e8', textAlign: 'left', fontWeight: 'bold' }}>
                  <div dangerouslySetInnerHTML={{ __html: highlightKeywords(header) }} />
                </th>
              ))}
            </tr>
          </thead>
          <tbody>
            {rows}
          </tbody>
        </table>
      </div>
    );
  };
  
  // Render SVG content
  const renderSVG = (svgContent) => {
    return (
      <div className="svg-preview" style={{ padding: '20px', textAlign: 'center' }}>
        <div dangerouslySetInnerHTML={{ __html: svgContent }} />
      </div>
    );
  };
  
  // Highlight keywords in HTML content
  const highlightHTML = (html) => {
    if (!searchKeywords || !html || typeof html !== 'string') {
      return html;
    }
    
    const keywords = searchKeywords.trim().split(/\s+/).filter(k => k.length > 0);
    if (keywords.length === 0) {
      return html;
    }
    
    // Create a temporary element to parse HTML
    const tempDiv = document.createElement('div');
    tempDiv.innerHTML = html;
    
    // Recursively highlight text nodes
    const highlightTextNodes = (node) => {
      if (node.nodeType === Node.TEXT_NODE) {
        const text = node.textContent;
        const highlightedText = highlightKeywords(text);
        if (highlightedText !== text) {
          const temp = document.createElement('div');
          temp.innerHTML = highlightedText;
          while (temp.firstChild) {
            node.parentNode.insertBefore(temp.firstChild, node);
          }
          node.parentNode.removeChild(node);
        }
      } else if (node.nodeType === Node.ELEMENT_NODE && node.tagName !== 'SCRIPT' && node.tagName !== 'STYLE' && node.tagName !== 'CODE' && node.tagName !== 'PRE') {
        // Don't process script, style, code, or pre tags
        // Use while loop instead of for loop to avoid skipping nodes when replacing
        let i = 0;
        while (i < node.childNodes.length) {
          highlightTextNodes(node.childNodes[i]);
          i++;
        }
      }
    };
    
    highlightTextNodes(tempDiv);
    
    // Get the modified HTML content
    let highlightedHtml = tempDiv.innerHTML;
    
    // Add highlight CSS style to the HTML content so it works in iframe
    const highlightStyle = `
      <style>
        .highlight {
          background: rgba(0,229,255,0.12);
          color: #00E5FF;
          padding: 0 2px;
          border-radius: 2px;
        }
      </style>
    `;
    
    // Insert the style into the HTML head or at the beginning
    if (highlightedHtml.includes('<head>')) {
      highlightedHtml = highlightedHtml.replace('<head>', `<head>${highlightStyle}`);
    } else {
      highlightedHtml = highlightStyle + highlightedHtml;
    }
    
    return highlightedHtml;
  };

  // Render HTML content
  const HTMLPreview = ({ html }) => {
    const needsAssets = false;
    const containerRef = React.useRef(null);
    
    // Apply highlighting to HTML content
    const highlightedHtml = highlightHTML(html);
    
    useEffect(() => {
      if (needsAssets) return;
      const el = containerRef.current;
      if (!el) return;
      const blocks = el.querySelectorAll('pre code.language-mermaid, code.language-mermaid');
      blocks.forEach((codeEl) => {
        const chart = codeEl.textContent || '';
        const wrapper = document.createElement('div');
        wrapper.className = 'mermaid';
        codeEl.parentElement.replaceWith(wrapper);
        mermaid.render(`mmd-${Math.random().toString(36).slice(2)}`, chart).then(({ svg }) => {
          wrapper.innerHTML = svg;
        }).catch(() => {
          wrapper.innerHTML = '<pre>Mermaid 渲染失败</pre>';
        });
      });
    }, [html, needsAssets]);
    if (needsAssets) {
      return <div className="html-preview" ref={containerRef} dangerouslySetInnerHTML={{ __html: highlightedHtml }} />;
    }
    return <div ref={containerRef} dangerouslySetInnerHTML={{ __html: highlightedHtml }} />;
  };

  const extractPlainText = (html) => {
    try {
      const doc = new DOMParser().parseFromString(html, 'text/html');
      const bodyText = doc.body && doc.body.innerText ? doc.body.innerText : '';
      return bodyText || (doc.documentElement && doc.documentElement.textContent) || html.replace(/<[^>]+>/g, ' ');
    } catch (e) {
      return html.replace(/<[^>]+>/g, ' ');
    }
  };

  const renderHTML = (htmlContent) => {
    return (
      <div className="html-preview" style={{ padding: '20px' }}>
        <div className="table-wrap">
          <HTMLPreview html={htmlContent} />
        </div>
      </div>
    );
  };

  const renderHTMLPage = (url) => {
    return (
      <div className="html-preview" style={{ width: '100%', height: '80vh' }}>
        <iframe
          src={url}
          width="100%"
          height="100%"
          title="HTML Preview"
          style={{ border: 'none' }}
        />
      </div>
    );
  };
  
  // Render plain text
  const renderText = (textContent) => {
    const highlightedContent = highlightKeywords(textContent);
    return (
      <div className="text-preview" style={{ padding: '20px', backgroundColor: '#f0f0f0' }}>
        <pre style={{ margin: 0, whiteSpace: 'pre-wrap', wordBreak: 'break-word', color: '#000' }} dangerouslySetInnerHTML={{ __html: highlightedContent }} />
      </div>
    );
  };
  
  // Determine which renderer to use based on file extension and content type
  const renderContent = () => {
    if (!content) return null;
    
    const ext = getFileExtension();
    
    switch (ext) {
      case 'md':
        if (typeof content === 'string' && content.trim().startsWith('<')) {
          return renderHTML(content);
        }
        return renderMarkdown(content);
      case 'pdf':
        return renderPDF(content);
      case 'csv':
        if (typeof content === 'string' && content.trim().startsWith('<')) {
          return renderHTML(content);
        }
        return renderCSV(content);
      case 'svg':
        return renderSVG(content);
      case 'docx':
      case 'doc':
        return renderHTML(content);
      case 'xlsx':
        return renderHTML(content);
      case 'html':
      case 'htm':
        return textOnly ? renderText(content) : renderHTMLPage(content);
      default:
        // Check if content is HTML
        if (typeof content === 'string' && content.startsWith('<')) {
          return renderHTML(content);
        }
        return renderText(content);
    }
  };
  
  // Show loading state
  if (propLoading || loading) {
    return (
      <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '80vh' }}>
        <Spin size="large">
          <div style={{ marginTop: 8 }}>正在生成预览...</div>
        </Spin>
      </div>
    );
  }
  
  // Show error state
  if (propError || error) {
    return (
      <div style={{ padding: '20px' }}>
        <Alert
          message="预览失败"
          description={propError || error}
          type="error"
          showIcon
        />
      </div>
    );
  }
  
  // Show empty state when no file is selected
  if (!filePath || !content) {
    return (
      <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '80vh' }}>
        <Empty description="请从左侧选择一个文件进行预览" />
      </div>
    );
  }
  
  return (
    <div className="preview-pane" style={{ height: 'calc(100vh - 72px)', width: '100%', display: 'flex', flexDirection: 'column' }}>
      <div style={{ padding: '20px', borderBottom: '1px solid #e8e8e8', flex: '0 0 auto', width: '100%' }}>
        <Title level={4} style={{ margin: 0 }}>
          {filePath.split('/').pop()}
        </Title>
        <Text type="secondary">{filePath}</Text>
        {(['html','htm'].includes(getFileExtension())) && (
          <div style={{ float: 'right' }}>
            <Switch checked={textOnly} onChange={setTextOnly} />
            <span style={{ marginLeft: 8 }}>纯文本模式</span>
          </div>
        )}
      </div>
      <div style={{ padding: '20px', overflowY: 'auto', flex: '1 1 auto', width: '100%', overflowX: 'hidden' }}>
        {renderContent()}
      </div>
    </div>
  );
};

export default PreviewPane;

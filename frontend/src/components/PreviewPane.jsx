import React, { useState, useEffect, useCallback, useRef } from 'react';
import { Spin, Empty, Alert, Typography, Switch } from 'antd';
import ReactMarkdown from 'react-markdown';
import remarkGfm from 'remark-gfm';
import rehypeRaw from 'rehype-raw';
import { Prism as SyntaxHighlighter } from 'react-syntax-highlighter';
import { tomorrow } from 'react-syntax-highlighter/dist/esm/styles/prism';
import mermaid from 'mermaid';

const { Title, Text } = Typography;

// 增强的Mermaid图表组件 - 包含样式增强和交互功能
const EnhancedMermaidBlock = ({ chart }) => {
  // 状态管理
  const [svg, setSvg] = useState('');
  const [isRendering, setIsRendering] = useState(true);
  const [showControls, setShowControls] = useState(true);
  const [scale, setScale] = useState(1);
  const [isDragging, setIsDragging] = useState(false);
  const [dragStart, setDragStart] = useState({ x: 0, y: 0 });
  const [position, setPosition] = useState({ x: 0, y: 0 });
  const [activeNode, setActiveNode] = useState(null);
  const containerRef = useRef(null);
  const chartRef = useRef(null);
  
  // 动态加载和渲染Mermaid图表
  useEffect(() => {
    const renderMermaidChart = async () => {
      try {
        setIsRendering(true);
        
        // 动态导入Mermaid库
        const mermaidModule = await import('mermaid');
        const mermaid = mermaidModule.default;
        
        // 配置Mermaid实例
        mermaid.initialize({
          startOnLoad: false,
          theme: 'dark',
          securityLevel: 'loose',
          flowchart: {
            useMaxWidth: false,
            htmlLabels: true,
            curve: 'basis'
          }
        });
        
        const id = `mermaid-chart-${Date.now()}`;
        const { svg: renderedSvg } = await mermaid.render(id, chart);
        
        // 增强SVG样式和交互功能
        const processedSvg = renderedSvg
          .replace('<svg', '<svg class="mermaid-svg-enhanced"')
          .replace('viewBox', 'preserveAspectRatio="xMidYMid meet" viewBox')
          .replace(/<style>/, '<style>\n.mermaid-svg-enhanced { filter: drop-shadow(0 4px 6px rgba(0, 0, 0, 0.1)); cursor: pointer; }')
          .replace(/<style>/, '<style>\n.mermaid-svg-enhanced .node rect { transition: all 0.3s ease; cursor: pointer; }')
          .replace(/<style>/, '<style>\n.mermaid-svg-enhanced .node rect:hover { filter: brightness(1.1); transform: scale(1.02); box-shadow: 0 0 0 2px #3b82f6; }')
          .replace(/<style>/, '<style>\n.mermaid-svg-enhanced .node rect.active { filter: brightness(1.2); transform: scale(1.05); box-shadow: 0 0 0 3px #ef4444; }')
          .replace(/<style>/, '<style>\n.mermaid-svg-enhanced .edgePath path { transition: all 0.3s ease; }')
          .replace(/<style>/, '<style>\n.mermaid-svg-enhanced .edgePath:hover path { stroke-width: 3px; stroke: #ef4444; }')
          .replace(/<style>/, '<style>\n.mermaid-svg-enhanced .label { cursor: pointer; transition: all 0.3s ease; }')
          .replace(/<style>/, '<style>\n.mermaid-svg-enhanced .label:hover { filter: brightness(1.1); transform: scale(1.05); }');
          
        setSvg(processedSvg);
      } catch (error) {
        console.error('Mermaid rendering error:', error);
        setSvg(`<div class="mermaid-error" style="
          padding: 30px;
          color: #dc3545;
          background: linear-gradient(135deg, #f8d7da 0%, #f5c6cb 100%);
          border: 2px solid #f5c6cb;
          border-radius: 12px;
          font-family: Inter, sans-serif;
          font-size: 14px;
          text-align: center;
          box-shadow: 0 4px 6px rgba(220, 53, 69, 0.1);
          cursor: default;
        ">图表渲染失败: ${error.message}</div>`);
      } finally {
        setIsRendering(false);
      }
    };
    
    renderMermaidChart();
  }, [chart]);
  
  // 缩放控制功能
  const zoomIn = () => setScale(s => Math.min(s * 1.2, 3));
  const zoomOut = () => setScale(s => Math.max(s * 0.8, 0.3));
  const resetZoom = () => {
    setScale(1);
    setPosition({ x: 0, y: 0 });
  };
  
  // 拖拽功能
   const handleMouseDown = (e) => {
     if (e.button !== 0) return; // 只响应左键
     setIsDragging(true);
     setDragStart({
       x: e.clientX - position.x,
       y: e.clientY - position.y
     });
     e.currentTarget.style.cursor = 'grabbing';
   };
   
   const handleMouseMove = (e) => {
     if (!isDragging) return;
     setPosition({
       x: e.clientX - dragStart.x,
       y: e.clientY - dragStart.y
     });
   };
   
   const handleMouseUp = () => {
     setIsDragging(false);
     if (chartRef.current) {
       chartRef.current.style.cursor = 'grab';
     }
   };
   
   // 节点点击事件处理
   const handleNodeClick = (e) => {
     if (isDragging) return; // 拖拽时不触发点击事件
     
     const target = e.target;
     const nodeRect = target.closest('.node rect');
     const label = target.closest('.label');
     
     if (nodeRect || label) {
       // 移除之前激活的节点
       const activeElements = chartRef.current?.querySelectorAll('.active');
       activeElements?.forEach(el => el.classList.remove('active'));
       
       // 激活当前节点
       const node = nodeRect || label;
       node.classList.add('active');
       setActiveNode(node);
       
       // 添加点击动画效果
       node.style.transition = 'all 0.2s ease';
       setTimeout(() => {
         node.style.transition = 'all 0.3s ease';
       }, 200);
     }
   };
   
   // 双击重置视图
   const handleDoubleClick = () => {
     resetZoom();
   };
   
   // 添加鼠标悬停效果
   const handleMouseEnter = (e) => {
     if (chartRef.current) {
       chartRef.current.style.cursor = 'grab';
     }
   };
   
   const handleMouseLeave = (e) => {
     if (chartRef.current && !isDragging) {
       chartRef.current.style.cursor = 'default';
     }
   };
  
  // 添加键盘快捷键
  useEffect(() => {
    const handleKeyDown = (e) => {
      if (e.ctrlKey || e.metaKey) {
        switch (e.key) {
          case '=':
          case '+':
            e.preventDefault();
            zoomIn();
            break;
          case '-':
            e.preventDefault();
            zoomOut();
            break;
          case '0':
            e.preventDefault();
            resetZoom();
            break;
        }
      }
    };
    
    document.addEventListener('keydown', handleKeyDown);
    return () => document.removeEventListener('keydown', handleKeyDown);
  }, []);
  
  // 增强的容器样式
  const containerStyle = {
    position: 'relative',
    background: 'linear-gradient(135deg, #f8fafc 0%, #f1f5f9 100%)',
    border: '2px solid #e2e8f0',
    borderRadius: '16px',
    padding: '24px',
    margin: '24px 0',
    boxShadow: '0 10px 25px rgba(0, 0, 0, 0.1), 0 5px 10px rgba(0, 0, 0, 0.05)',
    minHeight: '350px',
    overflow: 'hidden',
    transition: 'all 0.4s cubic-bezier(0.4, 0, 0.2, 1)',
    backdropFilter: 'blur(10px)'
  };
  
  // 增强的控件样式
  const controlsStyle = {
    position: 'absolute',
    top: '12px',
    right: '12px',
    display: 'flex',
    gap: '8px',
    zIndex: 20,
    background: 'rgba(255, 255, 255, 0.95)',
    backdropFilter: 'blur(10px)',
    padding: '10px',
    borderRadius: '12px',
    boxShadow: '0 4px 12px rgba(0, 0, 0, 0.15)',
    border: '1px solid rgba(226, 232, 240, 0.8)',
    transition: 'all 0.3s ease'
  };
  
  // 增强的按钮样式
  const buttonStyle = {
    background: 'linear-gradient(135deg, #3b82f6 0%, #1d4ed8 100%)',
    border: 'none',
    borderRadius: '8px',
    padding: '8px 12px',
    cursor: 'pointer',
    color: 'white',
    fontSize: '14px',
    fontWeight: '600',
    minWidth: '36px',
    height: '36px',
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
    transition: 'all 0.3s ease',
    boxShadow: '0 2px 4px rgba(59, 130, 246, 0.3)'
  };
  
  // 增强的图表样式
  const chartStyle = {
    width: '100%',
    height: 'auto',
    display: 'flex',
    justifyContent: 'center',
    alignItems: 'center',
    transform: `scale(${scale}) translate(${position.x}px, ${position.y}px)`,
    transformOrigin: 'center center',
    transition: isDragging ? 'none' : 'transform 0.3s cubic-bezier(0.4, 0, 0.2, 1)',
    padding: '30px 0',
    minHeight: '300px',
    cursor: isDragging ? 'grabbing' : 'grab'
  };
  
  // 缩放指示器样式
  const scaleIndicatorStyle = {
    position: 'absolute',
    bottom: '12px',
    left: '12px',
    background: 'rgba(0, 0, 0, 0.7)',
    color: 'white',
    padding: '6px 12px',
    borderRadius: '20px',
    fontSize: '12px',
    fontWeight: '500',
    zIndex: 10
  };
  
  return (
    <div ref={containerRef} style={containerStyle}>
      {/* 控制按钮 */}
      {showControls && (
        <div style={controlsStyle}>
          <button 
            style={{ ...buttonStyle, background: 'linear-gradient(135deg, #10b981 0%, #047857 100%)' }}
            onClick={zoomIn}
            title="放大 (Ctrl+)"
            onMouseEnter={(e) => e.currentTarget.style.transform = 'scale(1.05)'}
            onMouseLeave={(e) => e.currentTarget.style.transform = 'scale(1)'}
          >
            <span style={{ fontSize: '16px', fontWeight: 'bold' }}>+</span>
          </button>
          <button 
            style={{ ...buttonStyle, background: 'linear-gradient(135deg, #f59e0b 0%, #d97706 100%)' }}
            onClick={zoomOut}
            title="缩小 (Ctrl-)"
            onMouseEnter={(e) => e.currentTarget.style.transform = 'scale(1.05)'}
            onMouseLeave={(e) => e.currentTarget.style.transform = 'scale(1)'}
          >
            <span style={{ fontSize: '16px', fontWeight: 'bold' }}>-</span>
          </button>
          <button 
            style={{ ...buttonStyle, background: 'linear-gradient(135deg, #ef4444 0%, #dc2626 100%)' }}
            onClick={resetZoom}
            title="重置 (Ctrl+0)"
            onMouseEnter={(e) => e.currentTarget.style.transform = 'scale(1.05)'}
            onMouseLeave={(e) => e.currentTarget.style.transform = 'scale(1)'}
          >
            <span style={{ fontSize: '14px' }}>⟳</span>
          </button>
        </div>
      )}
      
      {/* 缩放指示器 */}
      <div style={scaleIndicatorStyle}>
        缩放: {Math.round(scale * 100)}%
      </div>
      
      {/* 图表容器 */}
       <div 
         ref={chartRef}
         style={chartStyle}
         onMouseDown={handleMouseDown}
         onMouseMove={handleMouseMove}
         onMouseUp={handleMouseUp}
         onMouseLeave={handleMouseLeave}
         onDoubleClick={handleDoubleClick}
         onClick={handleNodeClick}
         onMouseEnter={handleMouseEnter}
         dangerouslySetInnerHTML={{ __html: isRendering ? 
           '<div style="text-align: center; padding: 60px; color: #64748b; font-size: 16px;">' +
           '<div style="margin-bottom: 16px;">🔄</div>' +
           '图表加载中...</div>' : 
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

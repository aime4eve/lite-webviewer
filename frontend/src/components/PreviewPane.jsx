import React, { useState, useEffect, useCallback, useRef, useMemo } from 'react';
import { Spin, Empty, Alert, Typography, Switch } from 'antd';
import MarkdownPreview from './MarkdownPreview';
import EnhancedMermaidBlock from './EnhancedMermaidBlock';
import { createRoot } from 'react-dom/client';
import { mockMarkdownContent } from '../mockData';

const { Title, Text } = Typography;

// Preview component for different file types
const PreviewPane = ({ filePath, loading: propLoading, error: propError, searchKeywords }) => {
  const [content, setContent] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [textOnly, setTextOnly] = useState(false);
  
  // Highlight keywords in text
  const highlightKeywords = useCallback((text) => {
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
  }, [searchKeywords]);
  
  // Highlight keywords in HTML content
  const highlightHTML = useCallback((html) => {
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
  }, [searchKeywords, highlightKeywords]);

  // Extract plain text from HTML
  const extractPlainText = useCallback((html) => {
    try {
      const doc = new DOMParser().parseFromString(html, 'text/html');
      const bodyText = doc.body && doc.body.innerText ? doc.body.innerText : '';
      return bodyText || (doc.documentElement && doc.documentElement.textContent) || html.replace(/<[^>]+>/g, ' ');
    } catch (e) {
      return html.replace(/<[^>]+>/g, ' ');
    }
  }, []);

  // 预览内容缓存，避免重复请求相同的内容
  const previewCacheRef = useRef({
    filePath: null,
    textOnly: null,
    searchKeywords: null,
    content: null
  });
  
  // 存储上一个内容引用，用于清理blob URL
  const prevContentRef = useRef(null);
  // 跟踪当前正在使用的blob URLs，避免过早清理
  const activeBlobUrlsRef = useRef(new Set());
  
  const fetchPreview = useCallback(async () => {
    // 检查缓存，如果请求参数相同，直接使用缓存的内容
    if (previewCacheRef.current.filePath === filePath &&
        previewCacheRef.current.textOnly === textOnly &&
        previewCacheRef.current.searchKeywords === searchKeywords &&
        previewCacheRef.current.content) {
      setContent(previewCacheRef.current.content);
      return;
    }
    
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
          if (!resp.ok) throw new Error(`获取HTML文件失败: ${resp.statusText}`);
          const htmlText = await resp.text();
          const plain = extractPlainText(htmlText);
          setContent(plain);
          // 更新缓存
          previewCacheRef.current = {
            filePath,
            textOnly,
            searchKeywords,
            content: plain
          };
        } else {
          try {
            // Fetch HTML content, add highlights, then create Blob URL
            const resp = await fetch(fsUrl);
            if (!resp.ok) throw new Error(`获取HTML文件失败: ${resp.statusText}`);
            let htmlText = await resp.text();
            
            // Add keyword highlights to HTML content
            if (searchKeywords) {
              htmlText = highlightHTML(htmlText);
            }
            
            // Create Blob URL for the highlighted HTML
            const blob = new Blob([htmlText], { type: 'text/html' });
            previewContent = URL.createObjectURL(blob);
            
            // 记录活动的blob URL
            activeBlobUrlsRef.current.add(previewContent);
            
            setContent(previewContent);
            // 更新缓存
            previewCacheRef.current = {
              filePath,
              textOnly,
              searchKeywords,
              content: previewContent,
              blobObject: blob // 可选：存储原始blob对象以避免重复创建
            };
          } catch (err) {
            console.error('Failed to create HTML preview:', err);
            // 提供降级方案 - 尝试使用纯文本模式作为后备
            try {
              const resp = await fetch(fsUrl);
              if (resp.ok) {
                const htmlText = await resp.text();
                const plain = extractPlainText(htmlText);
                setContent(plain);
                previewCacheRef.current = {
                  filePath,
                  textOnly: true, // 标记为已切换到纯文本模式
                  searchKeywords,
                  content: plain
                };
                console.log('Fallback to plain text preview for HTML');
              }
            } catch (fallbackErr) {
              throw err; // 如果降级也失败，抛出原始错误
            }
          }
        }
        return;
      }
      
      // 确保使用正确的URL编码和文件路径格式
      const previewUrl = `/api/v1/document/preview?filePath=${encodeURIComponent(formattedFilePath)}`;
      const response = await fetch(previewUrl);
      
      if (!response.ok) {
        throw new Error(`获取预览失败: ${response.statusText}`);
      }
      
      const contentType = response.headers.get('content-type');
      
      if (contentType?.includes('text/html')) {
        previewContent = await response.text();
      } else if (contentType?.includes('application/pdf')) {
        const blob = await response.blob();
        previewContent = URL.createObjectURL(blob);
        // 记录活动的PDF blob URL
        activeBlobUrlsRef.current.add(previewContent);
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
      // 更新缓存
      previewCacheRef.current = {
        filePath,
        textOnly,
        searchKeywords,
        content: previewContent
      };
    } catch (err) {
      let errorMessage = err.message;
      // 检查是否是连接错误
      if (err.message.includes('Failed to fetch') || err.message.includes('ECONNREFUSED') || err.message.includes('获取失败')) {
        errorMessage = '无法连接到后端服务，正在使用模拟数据';
        // 使用模拟数据
        const ext = (filePath || '').split('.').pop().toLowerCase();
        if (ext === 'md' || ext === 'markdown') {
          const mockContent = mockMarkdownContent;
          setContent(mockContent);
          // 更新缓存
          previewCacheRef.current = {
            filePath,
            textOnly,
            searchKeywords,
            content: mockContent
          };
        } else {
          setError(errorMessage);
        }
      } else {
        setError(errorMessage);
      }
    } finally {
      setLoading(false);
    }
  }, [filePath, textOnly, searchKeywords, highlightHTML, extractPlainText]);

  // 清理blob URL的辅助函数
  const revokeBlobUrlIfNeeded = useCallback((url) => {
    if (url && typeof url === 'string' && url.startsWith('blob:')) {
      // 检查URL是否仍在活动使用中
      if (activeBlobUrlsRef.current.has(url)) {
        // 延迟清理，确保组件有足够时间卸载
        setTimeout(() => {
          try {
            URL.revokeObjectURL(url);
            // 从活动集合中移除
            activeBlobUrlsRef.current.delete(url);
          } catch (e) {
            console.warn('Failed to revoke blob URL:', e);
          }
        }, 1000); // 1秒延迟
      } else {
        // 直接清理不在活动集合中的URL
        try {
          URL.revokeObjectURL(url);
          activeBlobUrlsRef.current.delete(url);
        } catch (e) {
          console.warn('Failed to revoke blob URL:', e);
        }
      }
    }
  }, []);
  
  useEffect(() => {
    if (!filePath) {
      setContent(null);
      setError(null);
      return;
    }
    fetchPreview();
  }, [filePath, fetchPreview]);
  
  // 监听内容变化，清理之前的blob URL
  useEffect(() => {
    // 清理之前的blob URL
    if (prevContentRef.current && prevContentRef.current !== content) {
      // 标记为不再活动
      activeBlobUrlsRef.current.delete(prevContentRef.current);
      // 延迟清理，给组件卸载留出时间
      setTimeout(() => {
        revokeBlobUrlIfNeeded(prevContentRef.current);
      }, 500);
    }
    // 更新之前的内容引用
    prevContentRef.current = content;
  }, [content, revokeBlobUrlIfNeeded]);
  
  // 组件卸载时清理所有blob URLs
  useEffect(() => {
    return () => {
      // 清理当前内容的blob URL
      if (content && typeof content === 'string' && content.startsWith('blob:')) {
        try {
          URL.revokeObjectURL(content);
        } catch (e) {
          console.warn('Failed to revoke content blob URL on unmount:', e);
        }
      }
      
      // 清理缓存中的blob URL
      if (previewCacheRef.current?.content && 
          typeof previewCacheRef.current.content === 'string' && 
          previewCacheRef.current.content.startsWith('blob:')) {
        try {
          URL.revokeObjectURL(previewCacheRef.current.content);
        } catch (e) {
          console.warn('Failed to revoke cached blob URL on unmount:', e);
        }
      }
      
      // 清理所有活动的blob URLs
      activeBlobUrlsRef.current.forEach(url => {
        try {
          URL.revokeObjectURL(url);
        } catch (e) {
          console.warn('Failed to revoke active blob URL on unmount:', e);
        }
      });
      
      // 清空活动URL集合
      activeBlobUrlsRef.current.clear();
    };
  }, []);
  
  
  
  // Get file extension
  const getFileExtension = useCallback(() => {
    if (!filePath) return '';
    return filePath.split('.').pop().toLowerCase();
  }, [filePath]);
  
  // Render markdown content
  const renderMarkdown = useCallback((mdContent) => {
    return <MarkdownPreview content={mdContent} searchKeywords={searchKeywords} />;
  }, [searchKeywords]);
  
  // Render PDF content
  const renderPDF = useCallback((pdfUrl) => {
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
  }, []);
  
  // Render CSV content as table
  const renderCSV = useCallback((csvContent) => {
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
  }, [highlightKeywords]);
  
  // Render SVG content
  const renderSVG = useCallback((svgContent) => {
    return (
      <div className="svg-preview" style={{ padding: '20px', textAlign: 'center' }}>
        <div dangerouslySetInnerHTML={{ __html: svgContent }} />
      </div>
    );
  }, []);
  
  // HTMLPreview组件使用React.memo优化，减少不必要的重渲染
  const HTMLPreview = React.memo(({ html }) => {
    const needsAssets = false;
    const containerRef = React.useRef(null);
    const iframeRef = React.useRef(null);
    const blobUrlRef = React.useRef(null);
    
    // Apply highlighting to HTML content
    const highlightedHtml = useMemo(() => {
      // 直接使用传入的html，已经在外部处理过高亮
      return html;
    }, [html]);
    
    useEffect(() => {
      if (needsAssets) return;
      
      const el = containerRef.current;
      if (!el) return;
      
      // 使用requestAnimationFrame优化渲染时序，减少闪烁
      const renderMermaidBlocks = () => {
        const blocks = el.querySelectorAll('pre code.language-mermaid, code.language-mermaid');
        blocks.forEach((codeEl) => {
          const chart = codeEl.textContent || '';
          const wrapper = document.createElement('div');
          wrapper.className = 'enhanced-mermaid-wrapper';
          wrapper.style.width = '100%';
          wrapper.style.height = 'auto';
          
          // Create a unique ID for this wrapper
          const wrapperId = `mermaid-wrapper-${Math.random().toString(36).slice(2)}`;
          wrapper.id = wrapperId;
          
          codeEl.parentElement.replaceWith(wrapper);
          
          // Create a new div for React component mounting
          const reactMountDiv = document.createElement('div');
          reactMountDiv.id = `react-mount-${wrapperId}`;
          wrapper.appendChild(reactMountDiv);
          
          // Create a root for each mermaid block
          const root = createRoot(reactMountDiv);
          // Render the EnhancedMermaidBlock component
          root.render(
            React.createElement(EnhancedMermaidBlock, { chart })
          );
        });
      };
      
      // 使用requestAnimationFrame确保HTML渲染完成后再处理Mermaid图表
      requestAnimationFrame(() => {
        renderMermaidBlocks();
      });
    }, [html, needsAssets]);
    
    if (needsAssets) {
      return <div className="html-preview" ref={containerRef} dangerouslySetInnerHTML={{ __html: highlightedHtml }} />;
    }
    
    return (
      <div ref={containerRef} className="html-preview">
        <div dangerouslySetInnerHTML={{ __html: highlightedHtml }} />
      </div>
    );
  });

  const renderHTML = useCallback((htmlContent) => {
    // 处理HTML内容，区分blob URL和直接HTML
    const isBlobUrl = typeof htmlContent === 'string' && htmlContent.startsWith('blob:');
    
    if (isBlobUrl) {
      return (
        <div className="html-preview" style={{ padding: '20px', height: 'calc(100vh - 150px)' }}>
          <div className="table-wrap" style={{ height: '100%', overflow: 'auto' }}>
            <iframe
              src={htmlContent}
              style={{ width: '100%', height: '100%', border: 'none' }}
              title="HTML Preview"
              onError={(e) => {
                console.error('Error loading HTML preview:', e);
                // 尝试重新加载
                setTimeout(() => {
                  if (e.target) {
                    e.target.src = htmlContent;
                  }
                }, 300);
              }}
            />
          </div>
        </div>
      );
    } else {
      // 直接HTML内容
      return (
        <div className="html-preview" style={{ padding: '20px' }}>
          <div className="table-wrap">
            <HTMLPreview html={htmlContent} />
          </div>
        </div>
      );
    }
  }, []);


  // Render plain text
  const renderText = useCallback((textContent) => {
    const highlightedContent = highlightKeywords(textContent);
    return (
      <div className="text-preview" style={{ padding: '20px', backgroundColor: '#f0f0f0' }}>
        <pre style={{ margin: 0, whiteSpace: 'pre-wrap', wordBreak: 'break-word', color: '#000' }} dangerouslySetInnerHTML={{ __html: highlightedContent }} />
      </div>
    );
  }, [highlightKeywords]);
  
  // 优化渲染内容，使用useMemo缓存渲染结果，避免重复渲染
  const contentToRender = useMemo(() => {
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
        return textOnly ? renderText(content) : renderHTML(content);
      default:
        // Check if content is HTML
        if (typeof content === 'string' && content.startsWith('<')) {
          return renderHTML(content);
        }
        return renderText(content);
    }
  }, [content, textOnly, getFileExtension, renderMarkdown, renderHTML, renderPDF, renderCSV, renderSVG, renderText]);
  
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
        {contentToRender}
      </div>
    </div>
  );
};

export default PreviewPane;

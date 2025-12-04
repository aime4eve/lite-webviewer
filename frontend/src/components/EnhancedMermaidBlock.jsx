import React, { useState, useEffect, useRef, useCallback, memo } from 'react';
import { createPortal } from 'react-dom';
import { ExpandAltOutlined, ZoomInOutlined, ZoomOutOutlined, ReloadOutlined, ArrowLeftOutlined } from '@ant-design/icons';
import { Button, Tooltip, Spin } from 'antd';

// 模块级缓存，存储已初始化的mermaid实例和主题信息
const mermaidCache = {
  instance: null,
  lastTheme: null
};

// 静态SVG内容组件，使用memo避免每次拖拽都重新渲染DOM
const StaticSvgContent = memo(({ svg, onNodeClick, onDoubleClick }) => (
  <div 
    dangerouslySetInnerHTML={{ __html: svg }} 
    style={{ 
      width: '100%', 
      height: '100%',
      display: 'flex',
      justifyContent: 'center',
      alignItems: 'center'
    }}
    onClick={onNodeClick}
    onDoubleClick={onDoubleClick}
  />
));

// 全屏视图组件，提取到外部以避免不必要的重新渲染
const FullscreenOverlay = memo(({ 
  svg, 
  scale, 
  position, 
  isDragging, 
  onClose, 
  onZoomIn, 
  onZoomOut, 
  onReset,
  onMouseDown,
  onMouseMove,
  onMouseUp,
  onWheel,
  onTouchStart,
  onTouchMove,
  onTouchEnd,
  onNodeClick,
  onDoubleClick
}) => {
  return createPortal(
    <div 
      className="mermaid-fullscreen-overlay"
      style={{
        position: 'fixed',
        top: 0,
        left: 0,
        width: '100vw',
        height: '100vh',
        backgroundColor: '#0B1020',
        zIndex: 2147483647, // Max z-index
        display: 'flex',
        justifyContent: 'center',
        alignItems: 'center',
        pointerEvents: 'all', // Capture all pointer events
        isolation: 'isolate', // Create new stacking context
        touchAction: 'none', // Disable browser touch actions
        overflow: 'hidden',
        // 阻止选择和文本选择
        userSelect: 'none',
        WebkitUserSelect: 'none'
      }}
      // Stop all event propagation at the overlay level
      onWheel={(e) => {
        e.preventDefault();
        e.stopPropagation();
        onWheel(e);
      }}
      onMouseMove={(e) => {
        e.preventDefault(); // Prevent default behavior
        e.stopPropagation();
        onMouseMove(e);
      }}
      onMouseDown={(e) => {
        e.preventDefault(); // Prevent default behavior
        e.stopPropagation();
        onMouseDown(e); // Call the handler to enable dragging
      }}
      onMouseUp={(e) => {
        e.preventDefault(); // Prevent default behavior
        e.stopPropagation();
        onMouseUp(e);
      }}
      onClick={(e) => {
        e.preventDefault(); // Prevent default behavior
        e.stopPropagation();
      }}
      onDoubleClick={(e) => {
        e.preventDefault(); // Prevent default behavior
        e.stopPropagation();
      }}
      onContextMenu={(e) => {
        e.preventDefault();
        e.stopPropagation();
      }}
      onTouchStart={(e) => {
        e.preventDefault(); // Prevent default behavior
        e.stopPropagation();
        onTouchStart(e);
      }}
      onTouchMove={(e) => {
        e.preventDefault(); // Prevent scrolling
        e.stopPropagation();
        onTouchMove(e);
      }}
      onTouchEnd={(e) => {
        e.preventDefault(); // Prevent default behavior
        e.stopPropagation();
        onTouchEnd(e);
      }}
      onMouseEnter={(e) => {
        e.preventDefault();
        e.stopPropagation();
      }}
      onMouseLeave={(e) => {
        e.preventDefault();
        e.stopPropagation();
      }}
      onDragStart={(e) => {
        e.preventDefault();
        e.stopPropagation();
      }}
      onDrop={(e) => {
        e.preventDefault();
        e.stopPropagation();
      }}
    >
      <div 
        className="mermaid-fullscreen-container"
        style={{
          width: '100%',
          height: '100%',
          backgroundColor: '#0B1020',
          position: 'relative',
          overflow: 'hidden',
          display: 'flex',
          flexDirection: 'column',
          cursor: isDragging ? 'grabbing' : 'grab',
          pointerEvents: 'auto',
          isolation: 'isolate'
        }}
        onMouseDown={(e) => {
          e.stopPropagation();
          onMouseDown(e);
        }}
        onMouseMove={(e) => {
          e.stopPropagation();
          onMouseMove(e);
        }}
        onMouseUp={(e) => {
          e.stopPropagation();
          onMouseUp(e);
        }}
        onMouseLeave={(e) => {
          e.stopPropagation();
          onMouseUp(e);
        }}
        onWheel={(e) => {
          e.stopPropagation();
          onWheel(e);
        }}
        onTouchStart={(e) => {
          e.stopPropagation();
          onTouchStart(e);
        }}
        onTouchMove={(e) => {
          e.stopPropagation();
          onTouchMove(e);
        }}
        onTouchEnd={(e) => {
          e.stopPropagation();
          onTouchEnd(e);
        }}
      >
        {/* 控制栏 */}
        <div 
          style={{
            position: 'absolute',
            bottom: '24px',
            left: '50%',
            transform: 'translateX(-50%)',
            display: 'flex',
            gap: '12px',
            padding: '8px 16px',
            background: 'rgba(15, 20, 41, 0.8)',
            backdropFilter: 'blur(12px)',
            borderRadius: '24px',
            border: '1px solid rgba(0, 229, 255, 0.2)',
            zIndex: 100,
            boxShadow: '0 4px 12px rgba(0, 0, 0, 0.2)',
            pointerEvents: 'auto',
            isolation: 'isolate'
          }}
          onMouseDown={(e) => e.stopPropagation()}
          onClick={(e) => e.stopPropagation()}
          onMouseUp={(e) => e.stopPropagation()}
          onWheel={(e) => e.stopPropagation()}
        >
           <Tooltip title="缩小">
            <Button 
              type="text" 
              icon={<ZoomOutOutlined />} 
              onClick={(e) => { e.stopPropagation(); onZoomOut(); }}
              style={{ color: '#DCE3F0' }}
            />
          </Tooltip>
          <Tooltip title="重置">
            <Button 
              type="text" 
              icon={<ReloadOutlined />} 
              onClick={(e) => { e.stopPropagation(); onReset(); }}
              style={{ color: '#DCE3F0' }}
            />
          </Tooltip>
          <Tooltip title="放大">
            <Button 
              type="text" 
              icon={<ZoomInOutlined />} 
              onClick={(e) => { e.stopPropagation(); onZoomIn(); }}
              style={{ color: '#DCE3F0' }}
            />
          </Tooltip>
          <div style={{ width: '1px', background: 'rgba(255, 255, 255, 0.2)', margin: '0 4px' }} />
          <Button 
            type="text" 
            icon={<ArrowLeftOutlined />} 
            onClick={(e) => { e.stopPropagation(); onClose(); }}
            style={{ color: '#DCE3F0' }}
          >
            返回
          </Button>
        </div>

        {/* 图表内容 */}
        <div 
          className="mermaid-fullscreen-content"
          style={{
            width: '100%',
            height: '100%',
            display: 'flex',
            justifyContent: 'center',
            alignItems: 'center',
            overflow: 'hidden',
            pointerEvents: 'all',
            isolation: 'isolate',
            userSelect: 'none',
            WebkitUserSelect: 'none',
            touchAction: 'none'
          }}
          onWheel={(e) => {
            e.stopPropagation();
            e.preventDefault();
            onWheel(e);
          }}
          onMouseMove={(e) => {
            e.stopPropagation();
            e.preventDefault();
            onMouseMove(e);
          }}
          onMouseDown={(e) => {
            e.stopPropagation();
            e.preventDefault();
            onMouseDown(e);
          }}
          onMouseUp={(e) => {
            e.stopPropagation();
            e.preventDefault();
            onMouseUp(e);
          }}
          onContextMenu={(e) => {
            e.stopPropagation();
            e.preventDefault();
          }}
        >
          {/* 包装层用于应用变换，分离静态内容 */}
          <div style={{
            transform: `scale(${scale})`,
            transformOrigin: 'center center',
            transition: isDragging ? 'none' : 'transform 0.3s cubic-bezier(0.4, 0, 0.2, 1)',
            cursor: isDragging ? 'grabbing' : 'grab',
            width: '100%',
            height: '100%',
            display: 'flex',
            justifyContent: 'center',
            alignItems: 'center',
            willChange: isDragging ? 'transform' : 'auto',
            // 确保这一层也不允许文本选择
            userSelect: 'none',
            WebkitUserSelect: 'none'
          }}>
            {/* 应用位移的内部容器 */}
            <div style={{
              transform: `translate(${position.x}px, ${position.y}px)`,
              width: '100%',
              height: '100%',
              display: 'flex',
              justifyContent: 'center',
              alignItems: 'center'
            }}>
              <StaticSvgContent 
                svg={svg} 
                onNodeClick={onNodeClick} 
                onDoubleClick={onDoubleClick} 
              />
            </div>
          </div>
        </div>
      </div>
    </div>,
    document.body
  );
});

/**
 * 增强的Mermaid图表组件
 * 包含样式增强和交互功能，支持缩放、拖拽、节点交互等
 */
const EnhancedMermaidBlock = ({ chart }) => {
  // 状态管理
  const [svg, setSvg] = useState('');
  const [isRendering, setIsRendering] = useState(true);
  const [scale, setScale] = useState(1);
  const [isDragging, setIsDragging] = useState(false);
  const [position, setPosition] = useState({ x: 0, y: 0 });
  const [isFullscreen, setIsFullscreen] = useState(false);
  const containerRef = useRef(null);
  const chartRef = useRef(null);
  const fullscreenContainerRef = useRef(null);
  
  // 拖拽动画帧请求ID
  const dragFrameRef = useRef(null);
  // 拖拽状态
  const dragStateRef = useRef(null);
  // 触摸状态
  const touchStateRef = useRef({
    lastDistance: null,
    startTouches: []
  });
  
  // 图表渲染缓存，避免重复渲染相同内容的图表
  const renderCache = useRef({
    chart: null,
    theme: null,
    svg: null
  });

  // 全屏状态下的状态
  const [fullscreenScale, setFullscreenScale] = useState(1);
  const [fullscreenPosition, setFullscreenPosition] = useState({ x: 0, y: 0 });
  
  // 渲染Mermaid图表的函数，支持主题检测，使用useCallback包裹避免依赖项变化
  const renderMermaidChart = useCallback(async () => {
    try {
      setIsRendering(true);
      
      // 配置Mermaid实例，支持主题跟随系统
      const isDarkMode = document.documentElement.classList.contains('dark');
      const currentTheme = isDarkMode ? 'dark' : 'light';
      
      // 检查缓存，如果图表内容和主题都没有变化，直接使用缓存的SVG
      if (renderCache.current.chart === chart && 
          renderCache.current.theme === currentTheme && 
          renderCache.current.svg) {
        setSvg(renderCache.current.svg);
        setIsRendering(false);
        return;
      }
      
      let mermaid;
      
      // 检查缓存中是否已有mermaid实例
      if (!mermaidCache.instance) {
        // 动态导入Mermaid库
        const mermaidModule = await import('mermaid');
        mermaid = mermaidModule.default;
        
        // 初始化mermaid实例
        mermaid.initialize({
          startOnLoad: false,
          theme: 'base',
          themeVariables: {
            fontFamily: "'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif",
            fontSize: '14px',
            primaryColor: 'rgba(0, 229, 255, 0.1)',
            primaryTextColor: '#DCE3F0',
            primaryBorderColor: 'rgba(0, 229, 255, 0.15)',
            lineColor: 'rgba(0, 229, 255, 0.3)',
            secondaryColor: 'rgba(0, 245, 160, 0.1)',
            tertiaryColor: '#0F1429',
            mainBkg: 'rgba(0, 229, 255, 0.05)',
            nodeBorder: 'rgba(0, 229, 255, 0.15)',
            clusterBkg: 'rgba(15, 20, 41, 0.5)',
            clusterBorder: 'rgba(0, 229, 255, 0.15)',
            defaultLinkColor: 'rgba(0, 229, 255, 0.3)',
            titleColor: '#DCE3F0',
            edgeLabelBackground: '#0B1020',
            actorBorder: 'rgba(0, 229, 255, 0.15)',
            actorBkg: 'rgba(0, 229, 255, 0.05)',
            actorTextColor: '#DCE3F0',
            actorLineColor: 'rgba(0, 229, 255, 0.3)',
            signalColor: 'rgba(0, 229, 255, 0.3)',
            signalTextColor: '#DCE3F0',
            labelBoxBkgColor: '#0B1020',
            labelBoxBorderColor: 'rgba(0, 229, 255, 0.15)',
            labelTextColor: '#DCE3F0',
            loopTextColor: '#DCE3F0',
            noteBorderColor: 'rgba(0, 229, 255, 0.15)',
            noteBkgColor: 'rgba(0, 229, 255, 0.05)',
            noteTextColor: '#DCE3F0',
            activationBorderColor: 'rgba(0, 229, 255, 0.15)',
            activationBkgColor: 'rgba(0, 229, 255, 0.1)',
            sequenceNumberColor: '#DCE3F0'
          },
          securityLevel: 'loose',
          logLevel: 5, // 静默日志输出
          flowchart: {
            useMaxWidth: false,
            htmlLabels: true,
            curve: 'basis',
            padding: 15
          },
          sequence: {
            showSequenceNumbers: true,
            actorMargin: 50,
            boxMargin: 10,
            boxTextMargin: 5,
            noteMargin: 10,
            messageMargin: 35
          },
          gantt: {
            axisFormat: '%Y-%m-%d',
            barHeight: 20,
            barGap: 4,
            topPadding: 50,
            leftPadding: 75,
            gridLineStartPadding: 35,
            fontSize: 11,
            numberSectionStyles: 4,
            axisFontSize: 11
          }
        });
        
        // 缓存实例和主题
        mermaidCache.instance = mermaid;
        mermaidCache.lastTheme = currentTheme;
      } else {
        // 使用缓存的实例
        mermaid = mermaidCache.instance;
        
        // 只有在主题变化时才重新初始化
        if (mermaidCache.lastTheme !== currentTheme) {
          mermaid.initialize({
            theme: 'base',
            themeVariables: {
              fontFamily: "'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif",
              fontSize: '14px',
              primaryColor: 'rgba(0, 229, 255, 0.1)',
              primaryTextColor: '#DCE3F0',
              primaryBorderColor: 'rgba(0, 229, 255, 0.15)',
              lineColor: 'rgba(0, 229, 255, 0.3)',
              secondaryColor: 'rgba(0, 245, 160, 0.1)',
              tertiaryColor: '#0F1429',
              mainBkg: 'rgba(0, 229, 255, 0.05)',
              nodeBorder: 'rgba(0, 229, 255, 0.15)',
              clusterBkg: 'rgba(15, 20, 41, 0.5)',
              clusterBorder: 'rgba(0, 229, 255, 0.15)',
              defaultLinkColor: 'rgba(0, 229, 255, 0.3)',
              titleColor: '#DCE3F0',
              edgeLabelBackground: '#0B1020',
              actorBorder: 'rgba(0, 229, 255, 0.15)',
              actorBkg: 'rgba(0, 229, 255, 0.05)',
              actorTextColor: '#DCE3F0',
              actorLineColor: 'rgba(0, 229, 255, 0.3)',
              signalColor: 'rgba(0, 229, 255, 0.3)',
              signalTextColor: '#DCE3F0',
              labelBoxBkgColor: '#0B1020',
              labelBoxBorderColor: 'rgba(0, 229, 255, 0.15)',
              labelTextColor: '#DCE3F0',
              loopTextColor: '#DCE3F0',
              noteBorderColor: 'rgba(0, 229, 255, 0.15)',
              noteBkgColor: 'rgba(0, 229, 255, 0.05)',
              noteTextColor: '#DCE3F0',
              activationBorderColor: 'rgba(0, 229, 255, 0.15)',
              activationBkgColor: 'rgba(0, 229, 255, 0.1)',
              sequenceNumberColor: '#DCE3F0'
            }
          });
          mermaidCache.lastTheme = currentTheme;
        }
      }
      
      const id = `mermaid-chart-${Date.now()}`;
      const { svg: renderedSvg } = await mermaid.render(id, chart);
      
      // 增强SVG样式和交互功能
      const processedSvg = renderedSvg
        .replace('<svg', '<svg class="mermaid-svg-enhanced"')
        .replace('viewBox', 'preserveAspectRatio="xMidYMid meet" viewBox')
        // 基础样式增强
        .replace(/<style>/, `<style>
          .mermaid-svg-enhanced { 
            font-family: 'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif !important;
            filter: drop-shadow(0 4px 6px rgba(0, 0, 0, 0.1)); 
            cursor: pointer; 
          }
        `)
        // 节点样式增强 - 统一使用文档配色
        .replace(/<style>/, `<style>
          .mermaid-svg-enhanced .node rect,
          .mermaid-svg-enhanced .node circle,
          .mermaid-svg-enhanced .node polygon,
          .mermaid-svg-enhanced .node path { 
            transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1); 
            cursor: pointer;
            stroke-width: 1.5px !important;
            rx: 4px !important; 
            ry: 4px !important;
          }
        `)
        // 交互效果增强
        .replace(/<style>/, `<style>
          .mermaid-svg-enhanced .node:hover rect,
          .mermaid-svg-enhanced .node:hover circle,
          .mermaid-svg-enhanced .node:hover polygon,
          .mermaid-svg-enhanced .node:hover path { 
            filter: brightness(1.2); 
            transform: translateY(-1px);
            stroke: #00E5FF !important;
            stroke-width: 2px !important;
            box-shadow: 0 0 15px rgba(0, 229, 255, 0.3);
          }
        `)
        // 连线样式增强
        .replace(/<style>/, `<style>
          .mermaid-svg-enhanced .edgePath path { 
            transition: all 0.3s ease;
            stroke-width: 1.5px !important;
            opacity: 0.8;
          }
          .mermaid-svg-enhanced .edgePath:hover path { 
            stroke-width: 2.5px !important; 
            stroke: #00E5FF !important;
            opacity: 1;
            filter: drop-shadow(0 0 3px rgba(0, 229, 255, 0.5));
          }
        `)
        // 文本样式增强
        .replace(/<style>/, `<style>
          .mermaid-svg-enhanced text {
            font-family: 'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif !important;
          }
          .mermaid-svg-enhanced .label { 
            cursor: pointer; 
            transition: all 0.3s ease; 
            font-weight: 500;
          }
          .mermaid-svg-enhanced .label:hover { 
            filter: brightness(1.2); 
            transform: scale(1.02);
            text-shadow: 0 0 8px rgba(0, 229, 255, 0.4);
          }
        `);
          
      // 更新缓存
      renderCache.current = {
        chart,
        theme: currentTheme,
        svg: processedSvg
      };
      
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
  }, [chart]);
  
  // 初始渲染和图表内容变化时重新渲染
  useEffect(() => {
    renderMermaidChart();
  }, [chart, renderMermaidChart]);
  
  // 监听主题变化，重新渲染图表
  useEffect(() => {
    // 保存当前主题状态
    let currentTheme = document.documentElement.classList.contains('dark') ? 'dark' : 'light';
    
    // 创建一个观察器，监听文档根元素的类名变化
    const observer = new MutationObserver((mutations) => {
      mutations.forEach((mutation) => {
        if (mutation.type === 'attributes' && mutation.attributeName === 'class') {
          // 检查主题是否实际变化
          const newTheme = document.documentElement.classList.contains('dark') ? 'dark' : 'light';
          if (newTheme !== currentTheme) {
            currentTheme = newTheme;
            // 主题发生变化，重新渲染图表
            renderMermaidChart();
          }
        }
      });
    });
    
    // 开始观察文档根元素的类名变化
    observer.observe(document.documentElement, {
      attributes: true
    });
    
    // 清理函数
    return () => {
      observer.disconnect();
    };
  }, [renderMermaidChart]);
  
  // 缩放控制功能
  const zoomIn = useCallback(() => {
    console.log('[Mermaid] Zoom In clicked');
    if (isFullscreen) {
      setFullscreenScale(s => {
        const newScale = Math.min(s * 1.2, 4);
        console.log('[Mermaid] Fullscreen scale:', newScale);
        return newScale;
      });
    } else {
      setScale(s => Math.min(s * 1.2, 4)); // 最大400%
    }
  }, [isFullscreen]);

  const zoomOut = useCallback(() => {
    console.log('[Mermaid] Zoom Out clicked');
    if (isFullscreen) {
      setFullscreenScale(s => {
        const newScale = Math.max(s * 0.8, 0.25);
        console.log('[Mermaid] Fullscreen scale:', newScale);
        return newScale;
      });
    } else {
      setScale(s => Math.max(s * 0.8, 0.25)); // 最小25%
    }
  }, [isFullscreen]);

  const resetZoom = useCallback(() => {
    console.log('[Mermaid] Reset Zoom clicked');
    if (isFullscreen) {
      setFullscreenScale(1);
      setFullscreenPosition({ x: 0, y: 0 });
    } else {
      setScale(1);
      setPosition({ x: 0, y: 0 });
    }
  }, [isFullscreen]);
  
  // 切换全屏
  const toggleFullscreen = useCallback(() => {
    console.log('[Mermaid] Toggle Fullscreen clicked. Current state:', !isFullscreen);
    if (!isFullscreen) {
      setFullscreenScale(1);
      setFullscreenPosition({ x: 0, y: 0 });
      // 添加body类名，防止底层滚动
      document.body.classList.add('mermaid-fullscreen-active');
    } else {
      // 移除body类名
      document.body.classList.remove('mermaid-fullscreen-active');
    }
    setIsFullscreen(!isFullscreen);
  }, [isFullscreen]);

  // 监听ESC键关闭全屏
  useEffect(() => {
    const handleEsc = (e) => {
      if (e.key === 'Escape' && isFullscreen) {
        e.preventDefault();
        e.stopPropagation();
        setIsFullscreen(false);
        document.body.classList.remove('mermaid-fullscreen-active');
      }
    };
    
    // 添加事件监听器，使用capture阶段确保优先处理
    window.addEventListener('keydown', handleEsc, true);
    
    return () => {
      window.removeEventListener('keydown', handleEsc, true);
    };
  }, [isFullscreen]);
  
  // 拖拽动画帧处理函数
  const animateDrag = () => {
    if (dragStateRef.current && isDragging) {
      const dx = dragStateRef.current.clientX - dragStateRef.current.dragStartX;
      const dy = dragStateRef.current.clientY - dragStateRef.current.dragStartY;
      
      // 边界限制
      let boundedX = dx;
      let boundedY = dy;

      if (isFullscreen) {
        // 全屏模式下的边界限制
        // 允许拖动范围是视口的一半
        const limitX = window.innerWidth * 0.8; 
        const limitY = window.innerHeight * 0.8;
        boundedX = Math.max(-limitX, Math.min(limitX, dx));
        boundedY = Math.max(-limitY, Math.min(limitY, dy));

        setFullscreenPosition({ x: boundedX, y: boundedY });
      } else {
        // 普通模式下的边界限制
        if (containerRef.current) {
          const containerRect = containerRef.current.getBoundingClientRect();
          const limitX = containerRect.width * 0.8;
          const limitY = containerRect.height * 0.8;
          
          boundedX = Math.max(-limitX, Math.min(limitX, dx));
          boundedY = Math.max(-limitY, Math.min(limitY, dy));
          
          setPosition({ x: boundedX, y: boundedY });
        }
      }
      
      // 请求下一帧
      dragFrameRef.current = requestAnimationFrame(animateDrag);
    }
  };
  
  // 鼠标滚轮缩放功能
  const handleWheel = (e) => {
    e.preventDefault();
    // 如果按下Ctrl键，则进行缩放
    if (e.ctrlKey) {
      const delta = e.deltaY > 0 ? 0.9 : 1.1; // 缩放因子
      if (isFullscreen) {
        setFullscreenScale(s => Math.min(Math.max(s * delta, 0.25), 4));
      } else {
        setScale(s => Math.min(Math.max(s * delta, 0.25), 4)); // 限制在25%-400%之间
      }
    } 
    // 移除滚轮平移功能，仅保留缩放
  };


  // 触摸事件处理
  const handleTouchStart = (e) => {
    if (isFullscreen) {
      e.stopPropagation();
    }
    if (e.touches.length === 1) {
      // 单指拖动
      const touch = e.touches[0];
      setIsDragging(true);
      const currentPos = isFullscreen ? fullscreenPosition : position;
      const startX = touch.clientX - currentPos.x;
      const startY = touch.clientY - currentPos.y;
      
      dragStateRef.current = {
        dragStartX: startX,
        dragStartY: startY,
        clientX: touch.clientX,
        clientY: touch.clientY
      };
    } else if (e.touches.length === 2) {
      // 双指缩放
      const dx = e.touches[0].clientX - e.touches[1].clientX;
      const dy = e.touches[0].clientY - e.touches[1].clientY;
      const distance = Math.sqrt(dx * dx + dy * dy);
      
      touchStateRef.current = {
        lastDistance: distance,
        startTouches: [...e.touches]
      };
    }
  };

  const handleTouchMove = (e) => {
    if (isFullscreen) {
      e.stopPropagation();
      e.preventDefault(); // 全屏模式下总是阻止默认行为
    }
    
    if (e.touches.length === 1 && isDragging) {
      // 单指拖动
      e.preventDefault(); // 防止页面滚动
      const touch = e.touches[0];
      
      dragStateRef.current = {
        ...dragStateRef.current,
        clientX: touch.clientX,
        clientY: touch.clientY
      };
      
      if (!dragFrameRef.current) {
        dragFrameRef.current = requestAnimationFrame(animateDrag);
      }
    } else if (e.touches.length === 2) {
      // 双指缩放
      e.preventDefault();
      const dx = e.touches[0].clientX - e.touches[1].clientX;
      const dy = e.touches[0].clientY - e.touches[1].clientY;
      const distance = Math.sqrt(dx * dx + dy * dy);
      
      if (touchStateRef.current.lastDistance) {
        const scaleDelta = distance / touchStateRef.current.lastDistance;
        // 平滑缩放因子
        const smoothScale = 1 + (scaleDelta - 1) * 0.5;
        
        if (isFullscreen) {
          setFullscreenScale(s => Math.min(Math.max(s * smoothScale, 0.25), 4));
        } else {
          setScale(s => Math.min(Math.max(s * smoothScale, 0.25), 4));
        }
      }
      
      touchStateRef.current.lastDistance = distance;
    }
  };

  const handleTouchEnd = (e) => {
    if (isFullscreen) {
      e.stopPropagation();
    }
    setIsDragging(false);
    touchStateRef.current = {
      lastDistance: null,
      startTouches: []
    };
    
    if (dragFrameRef.current) {
      cancelAnimationFrame(dragFrameRef.current);
      dragFrameRef.current = null;
    }
    
    dragStateRef.current = null;
  };

  // 响应式调整：监听窗口大小变化，自动调整图表大小
  useEffect(() => {
    const handleResize = () => {
      if (isFullscreen) {
        // 全屏模式下，重新计算最佳尺寸
        // 获取容器尺寸
        const containerWidth = window.innerWidth;
        // ... existing code ...
        setFullscreenPosition({ x: 0, y: 0 });
        
        // 根据屏幕尺寸调整初始缩放比例
        if (containerWidth < 768) {
          // 移动端
          setFullscreenScale(0.8);
        } else {
          // 桌面端
          setFullscreenScale(1);
        }
        return;
      }

      if (containerRef.current) {
        // 移动端自适应缩放
        if (window.innerWidth < 768) {
          // 移动端默认缩放比例稍微小一点，确保内容完整显示
          setScale(0.8);
        } else {
          setScale(1);
        }
      }
    };

    window.addEventListener('resize', handleResize);
    // 初始化调用一次
    handleResize();

    return () => {
      window.removeEventListener('resize', handleResize);
    };
  }, [isFullscreen]);
  
  // 拖拽功能
   const handleMouseDown = useCallback((e) => {
     if (isFullscreen) {
       e.stopPropagation();
     }
     if (e.button !== 0) return; // 只响应左键
     setIsDragging(true);
     const currentPos = isFullscreen ? fullscreenPosition : position;
     const startX = e.clientX - currentPos.x;
     const startY = e.clientY - currentPos.y;
     
     // 初始化拖拽状态
     dragStateRef.current = {
       dragStartX: startX,
       dragStartY: startY,
       clientX: e.clientX,
       clientY: e.clientY
     };
     
     if (isFullscreen) {
       // Don't manipulate DOM directly if possible, rely on React state
       // if (fullscreenContainerRef.current) fullscreenContainerRef.current.style.cursor = 'grabbing';
     } else {
       if (chartRef.current) chartRef.current.style.cursor = 'grabbing';
     }
   }, [isFullscreen, fullscreenPosition, position]);
   
   const handleMouseMove = useCallback((e) => {
     if (isFullscreen) {
       e.stopPropagation();
     }
     if (!isDragging) return;
     
     // 更新拖拽状态
     dragStateRef.current = {
       ...dragStateRef.current,
       clientX: e.clientX,
       clientY: e.clientY
     };
     
     // 如果没有正在进行的动画帧请求，发起请求
     if (!dragFrameRef.current) {
       dragFrameRef.current = requestAnimationFrame(animateDrag);
     }
   }, [isFullscreen, isDragging]); // Remove animateDrag dependency as it uses refs
   
   const handleMouseUp = useCallback((e) => {
     if (isFullscreen && e) {
       e.stopPropagation();
     }
     setIsDragging(false);
     
     // 取消动画帧请求
     if (dragFrameRef.current) {
       cancelAnimationFrame(dragFrameRef.current);
       dragFrameRef.current = null;
     }
     
     dragStateRef.current = null;
     
     if (isFullscreen) {
       // if (fullscreenContainerRef.current) fullscreenContainerRef.current.style.cursor = 'grab';
     } else {
       if (chartRef.current) chartRef.current.style.cursor = 'grab';
     }
   }, [isFullscreen]);
   
   // 节点点击事件处理
   const handleNodeClick = useCallback((e) => {
     if (isFullscreen) {
       e.stopPropagation();
       e.preventDefault(); // 阻止默认行为
     }
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
       
       // 添加点击动画效果
       node.style.transition = 'all 0.2s ease';
       setTimeout(() => {
         node.style.transition = 'all 0.3s ease';
       }, 200);
     }
   }, [isFullscreen, isDragging]);
   
   // 双击重置视图
   const handleDoubleClick = useCallback((e) => {
     if (isFullscreen && e) {
       e.stopPropagation();
     }
     resetZoom();
   }, [isFullscreen, resetZoom]);
   
   // 添加鼠标悬停效果
  const handleMouseEnter = useCallback(() => {
    if (chartRef.current) {
      chartRef.current.style.cursor = 'grab';
    }
  }, []);
  
  const handleMouseLeave = useCallback(() => {
    if (isDragging) {
      setIsDragging(false);
      
      // 取消动画帧请求
      if (dragFrameRef.current) {
        cancelAnimationFrame(dragFrameRef.current);
        dragFrameRef.current = null;
      }
      
      dragStateRef.current = null;
    }
    
    if (chartRef.current) {
      chartRef.current.style.cursor = 'default';
    }
  }, [isDragging]);
  
  // 组件卸载时清理
  useEffect(() => {
    return () => {
      // 取消任何正在进行的动画帧请求
      if (dragFrameRef.current) {
        cancelAnimationFrame(dragFrameRef.current);
        dragFrameRef.current = null;
      }
    };
  }, []);
  
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
  
  // 增强的容器样式，适配深色主题
  const containerStyle = {
    position: 'relative',
    background: 'rgba(15, 20, 41, 0.85)',
    border: '2px solid rgba(0, 229, 255, 0.15)',
    borderRadius: '16px',
    padding: '24px',
    margin: '24px 0',
    boxShadow: '0 10px 25px rgba(0, 0, 0, 0.3), 0 5px 10px rgba(0, 0, 0, 0.1)',
    minHeight: '350px',
    overflow: 'hidden',
    transition: 'all 0.4s cubic-bezier(0.4, 0, 0.2, 1)',
    backdropFilter: 'blur(4px)'
  };
  
  // 增强的控件样式，适配深色主题
  const controlsStyle = {
    position: 'absolute',
    top: '12px',
    right: '12px',
    display: 'flex',
    gap: '8px',
    zIndex: 20,
    background: 'rgba(15, 20, 41, 0.95)',
    backdropFilter: 'blur(10px)',
    padding: '10px',
    borderRadius: '12px',
    boxShadow: '0 4px 12px rgba(0, 229, 255, 0.2)',
    border: '1px solid rgba(0, 229, 255, 0.3)',
    transition: 'all 0.3s ease',
    opacity: 0.9
  };
  
  // 增强的按钮样式，适配深色主题
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
    boxShadow: '0 2px 8px rgba(59, 130, 246, 0.4)'
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
    <>
      <div ref={containerRef} style={containerStyle}>
        {/* 顶部控制按钮 */}
        <div style={controlsStyle}>
          <Tooltip title="全屏查看">
            <button 
              style={{ ...buttonStyle, background: 'linear-gradient(135deg, #3b82f6 0%, #1d4ed8 100%)' }}
              onClick={toggleFullscreen}
              onMouseEnter={(e) => e.currentTarget.style.transform = 'scale(1.05)'}
              onMouseLeave={(e) => e.currentTarget.style.transform = 'scale(1)'}
            >
              <ExpandAltOutlined style={{ fontSize: '16px' }} />
            </button>
          </Tooltip>
        </div>
        
        {/* 缩放指示器 */}
        <div style={scaleIndicatorStyle}>
          缩放: {Math.round(scale * 100)}%
        </div>
        
        {/* 图表容器 */}
         <div 
           ref={chartRef}
           style={{
             ...chartStyle,
             transform: `scale(${scale}) translate(${position.x}px, ${position.y}px) translateZ(0)`, // Add translateZ(0)
             willChange: 'transform', // Optimization hint
             backfaceVisibility: 'hidden' // Optimization hint
           }}
           onMouseDown={handleMouseDown}
           onMouseMove={handleMouseMove}
           onMouseUp={handleMouseUp}
           onMouseLeave={handleMouseLeave}
           onTouchStart={handleTouchStart}
           onTouchMove={handleTouchMove}
           onTouchEnd={handleTouchEnd}
           onDoubleClick={handleDoubleClick}
           onClick={handleNodeClick}
           onMouseEnter={handleMouseEnter}
           onWheel={handleWheel}
           dangerouslySetInnerHTML={{ __html: isRendering ? 
             '<div style="display: flex; justify-content: center; align-items: center; height: 300px; color: #64748b;">' +
             '<div class="ant-spin ant-spin-spinning"><span class="ant-spin-dot ant-spin-dot-spin"><i class="ant-spin-dot-item"></i><i class="ant-spin-dot-item"></i><i class="ant-spin-dot-item"></i><i class="ant-spin-dot-item"></i></span></div>' +
             '<span style="margin-left: 10px;">图表加载中...</span></div>' : 
             svg 
           }}
         />
      </div>
      
      {/* 全屏浮窗 */}
      {isFullscreen && <FullscreenOverlay
        svg={svg}
        scale={fullscreenScale}
        position={fullscreenPosition}
        isDragging={isDragging}
        onClose={toggleFullscreen}
        onZoomIn={zoomIn}
        onZoomOut={zoomOut}
        onReset={resetZoom}
        onMouseDown={handleMouseDown}
        onMouseMove={handleMouseMove}
        onMouseUp={handleMouseUp}
        onWheel={handleWheel}
        onTouchStart={handleTouchStart}
        onTouchMove={handleTouchMove}
        onTouchEnd={handleTouchEnd}
        onNodeClick={handleNodeClick}
        onDoubleClick={handleDoubleClick}
      />}
    </>
  );
};

export default memo(EnhancedMermaidBlock);

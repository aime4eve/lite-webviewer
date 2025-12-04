import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import './index.css'
import { ConfigProvider, theme, App as AntdApp, notification } from 'antd'
import App from './App.jsx'

// 立即执行的日志拦截器，确保在任何其他代码之前执行
(function() {
  // 保存原始console方法
  const originalLog = console.log;
  const originalDebug = console.debug;
  const originalInfo = console.info;
  const originalWarn = console.warn;
  const originalError = console.error;
  const originalFatal = console.fatal || console.error;
  const originalTrace = console.trace;
  
  // WebAssembly日志检测函数
  const isWebAssemblyLog = (args) => {
    return args.length === 1 && 
      typeof args[0] === 'string' && 
      /^\[0xc00[0-9a-fA-F]+\s+0xc00[0-9a-fA-F]+\]$/.test(args[0]);
  };
  
  // 替换所有console方法
  console.log = function(...args) {
    if (!isWebAssemblyLog(args)) {
      originalLog.apply(console, args);
    }
  };
  
  console.debug = function(...args) {
    if (!isWebAssemblyLog(args)) {
      originalDebug.apply(console, args);
    }
  };
  
  console.info = function(...args) {
    if (!isWebAssemblyLog(args)) {
      originalInfo.apply(console, args);
    }
  };
  
  console.warn = function(...args) {
    if (!isWebAssemblyLog(args)) {
      originalWarn.apply(console, args);
    }
  };
  
  console.error = function(...args) {
    if (!isWebAssemblyLog(args)) {
      originalError.apply(console, args);
    }
  };
  
  if (console.fatal) {
    console.fatal = function(...args) {
      if (!isWebAssemblyLog(args)) {
        originalFatal.apply(console, args);
      }
    };
  }
  
  console.trace = function(...args) {
    if (!isWebAssemblyLog(args)) {
      originalTrace.apply(console, args);
    }
  };
})();

notification.config({ placement: 'bottomRight', duration: 2 })

createRoot(document.getElementById('root')).render(
  <StrictMode>
    <ConfigProvider
      theme={{
        algorithm: theme.darkAlgorithm,
        token: {
          colorPrimary: '#00E5FF',
          colorSuccess: '#00F5A0',
          colorText: '#DCE3F0',
          colorBgBase: '#0B1020',
          borderRadius: 10,
        },
      }}
    >
      <AntdApp>
        <App />
      </AntdApp>
    </ConfigProvider>
  </StrictMode>,
)

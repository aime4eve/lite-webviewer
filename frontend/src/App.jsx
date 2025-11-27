import { useState, useEffect, useRef, useCallback } from 'react';
import { Layout, Input, Button, App as AntdApp, Space } from 'antd';
import { SearchOutlined, ReloadOutlined, MenuFoldOutlined, MenuUnfoldOutlined, BarsOutlined } from '@ant-design/icons';
import './App.css';
import FileTree from './components/FileTree';
import PreviewPane from './components/PreviewPane';
import { useAppStore } from './store';
import { NOTIFY_KEYS, notifyTreeLoaded } from './shared/notification';

const { Header, Sider, Content } = Layout;
const { Search } = Input;

function App() {
  const { message, notification } = AntdApp.useApp();
  const [collapsed, setCollapsed] = useState(false);
  const [searchText, setSearchText] = useState('');
  const scanAttempted = useRef(false);
  
  const {
    files,
    setFiles,
    selectedFile,
    setSelectedFile,
    loading,
    setLoading,
    error,
    setError
  } = useAppStore();

  const containerRef = useRef(null);

  const handleScan = useCallback(async (force = false) => {
    setLoading(true);
    setError(null);
    try {
      const response = await fetch(`/api/v1/document/scan?force=${force}`, {
        method: 'POST'
      });
      if (!response.ok) {
        throw new Error('Scan failed');
      }
      const result = await response.text();
      notification.success({ key: NOTIFY_KEYS.scanSuccess, message: '扫描完成', description: result, placement: 'bottomRight', duration: 3 });

      const treeResp = await fetch('/api/v1/index/json');
      if (!treeResp.ok) {
        throw new Error('Failed to fetch file tree');
      }
      const data = await treeResp.json();
      const filePaths = data.items
        .filter(item => item.type === 'file')
        .map(item => item.path);
      setFiles(filePaths);
      notifyTreeLoaded(notification, filePaths);
    } catch (err) {
      setError(err.message);
      notification.error({ key: NOTIFY_KEYS.scanFail, message: '扫描失败', description: err.message, placement: 'bottomRight', duration: 3 });
    } finally {
      setLoading(false);
    }
  }, [message, setLoading, setError, setFiles]);

  const fetchFileTree = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const response = await fetch('/api/v1/index/json');
      if (!response.ok) {
        throw new Error('Failed to fetch file tree');
      }
      const data = await response.json();

      const filePaths = data.items
        .filter(item => item.type === 'file')
        .map(item => item.path);
      setFiles(filePaths);
      notifyTreeLoaded(notification, filePaths, { onForceScan: () => handleScan(true) });
    } catch (err) {
      if (!scanAttempted.current) {
        scanAttempted.current = true;
        try {
          const scanResp = await fetch('/api/v1/document/scan?force=true', { method: 'POST' });
          if (scanResp.ok) {
            const treeResp = await fetch('/api/v1/index/json');
            if (treeResp.ok) {
              const data2 = await treeResp.json();
              const filePaths2 = data2.items
                .filter(item => item.type === 'file')
                .map(item => item.path);
              setFiles(filePaths2);
              notifyTreeLoaded(notification, filePaths2);
              return;
            }
          }
        } catch (e) {
          // ignore scan fallback error and continue to show fetch error
        }
      }
      notification.error({ key: NOTIFY_KEYS.treeLoadFail, message: '文件加载树失败', description: err.message, placement: 'bottomRight', duration: 3 });
    } finally {
      setLoading(false);
    }
  }, [message, setLoading, setError, setFiles]);

  useEffect(() => {
    fetchFileTree();
  }, [fetchFileTree]);

  return (
    <Layout style={{ minHeight: '100vh' }} className="app-root">
      <Header className="header">
        <div className="logo">Nexus-Lite 知识预览系统</div>
        <div className="header-actions">
          <Button
            type="primary"
            icon={<ReloadOutlined />}
            onClick={() => handleScan(false)}
            loading={loading}
          >
            扫描
          </Button>
          <Button
            danger
            icon={<ReloadOutlined />}
            onClick={() => handleScan(true)}
            loading={loading}
          >
            强制扫描
          </Button>
        </div>
      </Header>
      <Layout>
        <Sider
          collapsible
          collapsed={collapsed}
          onCollapse={setCollapsed}
          width={300}
          className="sider"
          breakpoint="lg"
          collapsedWidth={0}
          zeroWidthTriggerStyle={{ display: 'none' }}
        >
          <div className="search-container" style={{ padding: 16 }}>
            <Space.Compact style={{ width: '100%' }}>
              <Input
                placeholder="搜索文件"
                allowClear
                size="small"
                value={searchText}
                onChange={(e) => setSearchText(e.target.value)}
              />
              <Button size="small" icon={<SearchOutlined />} />
              <Button
                size="small"
                aria-label={collapsed ? '展开侧边栏' : '折叠侧边栏'}
                icon={collapsed ? <MenuUnfoldOutlined /> : <MenuFoldOutlined />}
                onClick={() => setCollapsed((c) => !c)}
              />
            </Space.Compact>
          </div>
          <FileTree
            files={files}
            searchText={searchText}
            onFileSelect={setSelectedFile}
            loading={loading}
          />
        </Sider>
        {collapsed && (
          <Button
            className="sider-float-trigger"
            type="default"
            size="small"
            icon={<BarsOutlined />}
            aria-label="展开侧边栏"
            onClick={() => setCollapsed(false)}
          />
        )}
        <Content className="content" ref={containerRef}>
          <div className="content-layout">
            <div className="left-content" style={{ width: '100%' }}>
              <PreviewPane filePath={selectedFile} loading={loading} error={error} />
            </div>
          </div>
        </Content>
      </Layout>
    </Layout>
  );
}

export default App;

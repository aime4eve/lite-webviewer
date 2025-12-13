import { useState, useEffect, useRef, useCallback } from 'react';
import { Layout, Input, Button, App as AntdApp, Space, Select, Divider } from 'antd';
import DOMPurify from 'dompurify';
import { SearchOutlined, ReloadOutlined, MenuFoldOutlined, MenuUnfoldOutlined, BarsOutlined } from '@ant-design/icons';
import './App.css';
import FileTree from './components/FileTree';
import PreviewPane from './components/PreviewPane';
import { useAppStore } from './store';
import { NOTIFY_KEYS, notifyTreeLoaded } from './shared/notification';
import { mockFiles } from './mockData';

const { Header, Sider, Content } = Layout;
const { Search } = Input;

function App() {
  const { notification } = AntdApp.useApp();
  const [collapsed, setCollapsed] = useState(false);
  const [searchText, setSearchText] = useState('');
  const [searchResults, setSearchResults] = useState([]);
  const [searching, setSearching] = useState(false);
  const [typeFilters, setTypeFilters] = useState([]);
  const [localResults, setLocalResults] = useState([]); // 存储本地文件名搜索结果
  const [searchMode, setSearchMode] = useState('local'); // 'local' 或 'es'
  const scanAttempted = useRef(false);
  
  // 根目录配置状态
  const [rootDirs, setRootDirs] = useState('');
  const [isEditing, setIsEditing] = useState(false);
  const [tempRootDirs, setTempRootDirs] = useState('');
  
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

  const handleScan = useCallback(async () => {
    setLoading(true);
    setError(null);
    
    try {
      const response = await fetch(`/api/v1/document/force-scan`, {
        method: 'POST'
      });
      if (!response.ok) {
        throw new Error('扫描失败');
      }
      const result = await response.text();
      notification.success({ key: NOTIFY_KEYS.scanSuccess, message: '扫描完成', description: result, placement: 'bottomRight', duration: 3 });

      const treeResp = await fetch('/api/v1/index/json');
      if (!treeResp.ok) {
        throw new Error('获取文件树失败');
      }
      const data = await treeResp.json();
      const filePaths = data.items
        .filter(item => item.type === 'file')
        .map(item => item.path);
      setFiles(filePaths);
      notifyTreeLoaded(notification, filePaths);
    } catch (err) {
      let errorMessage = err.message;
      // 检查是否是连接错误
      if (err.message.includes('Failed to fetch') || err.message.includes('ECONNREFUSED')) {
        errorMessage = '无法连接到后端服务，请确保后端服务正在运行';
      }
      setError(errorMessage);
      notification.error({ key: NOTIFY_KEYS.scanFail, message: '操作失败', description: errorMessage, placement: 'bottomRight', duration: 3 });
    } finally {
      setLoading(false);
    }
  }, [setLoading, setError, setFiles, notification]);

  // 获取根目录配置
  const fetchRootDirs = useCallback(async () => {
    try {
      const response = await fetch('/api/v1/config/root-dirs');
      if (response.ok) {
        const data = await response.text();
        setRootDirs(data);
        setTempRootDirs(data);
      }
    } catch (err) {
      console.error('Failed to fetch root dirs:', err);
      notification.error({ message: '获取根目录配置失败', description: err.message, placement: 'bottomRight', duration: 3 });
    }
  }, [notification]);

  // 更新根目录配置
  const handleUpdateRootDirs = useCallback(async () => {
    try {
      const response = await fetch('/api/v1/config/root-dirs', {
        method: 'PUT',
        headers: {
          'Content-Type': 'text/plain'
        },
        body: tempRootDirs
      });
      if (response.ok) {
        setRootDirs(tempRootDirs);
        setIsEditing(false);
        notification.success({ 
          message: '配置更新成功', 
          description: '根目录配置已更新，将自动触发扫描', 
          placement: 'bottomRight', 
          duration: 3 
        });
        // 自动触发扫描
        handleScan();
      }
    } catch (err) {
      console.error('Failed to update root dirs:', err);
      notification.error({ 
        message: '配置更新失败', 
        description: err.message, 
        placement: 'bottomRight', 
        duration: 3 
      });
    }
  }, [tempRootDirs, handleScan, notification]);

  const fetchFileTree = useCallback(async () => {
        setLoading(true);
        setError(null);
        try {
            const response = await fetch('/api/v1/index/json');
            if (!response.ok) {
                throw new Error('获取文件树失败');
            }
            const data = await response.json();

            const filePaths = data.items
                .filter(item => item.type === 'file')
                .map(item => item.path);
            setFiles(filePaths);
            notifyTreeLoaded(notification, filePaths, { onForceScan: () => handleScan(true) });
        } catch (err) {
            let errorMessage = err.message;
            // 检查是否是连接错误
            if (err.message.includes('Failed to fetch') || err.message.includes('ECONNREFUSED') || err.message.includes('获取失败')) {
                errorMessage = '无法连接到后端服务，正在使用模拟数据';
                // 使用模拟数据
                setFiles(mockFiles);
                notifyTreeLoaded(notification, mockFiles, { onForceScan: () => handleScan(true) });
                notification.warning({ key: NOTIFY_KEYS.treeLoadFail, message: '文件加载失败', description: errorMessage, placement: 'bottomRight', duration: 3 });
            } else {
                // 如果是其他错误，显示错误信息
                notification.error({ key: NOTIFY_KEYS.treeLoadFail, message: '文件加载失败', description: errorMessage, placement: 'bottomRight', duration: 3 });
            }
            
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
        } finally {
            setLoading(false);
        }
    }, [setLoading, setError, setFiles, handleScan, notification]);

  // 处理搜索输入，进行本地文件名匹配
  const handleSearchInput = (e) => {
    const value = e.target.value;
    setSearchText(value);
    setSearchMode('local');
    
    if (!value.trim()) {
      setLocalResults([]);
      return;
    }
    
    // 从files数组中筛选文件名包含搜索文本的文件
    const searchTerm = value.toLowerCase();
    const filteredResults = files
      .filter(filePath => {
        const fileName = filePath.split('/').pop();
        return fileName.toLowerCase().includes(searchTerm);
      })
      .map(filePath => {
        const fileName = filePath.split('/').pop();
        const parentDir = filePath.split('/').slice(0, -1).join('/');
        return {
          filePath,
          fileName,
          parentDir,
          snippet: `文件名匹配: ${fileName}`
        };
      });
    
    setLocalResults(filteredResults);
  };

  const runSearch = useCallback(async () => {
    const q = (searchText || '').trim();
    if (!q) {
      setSearchResults([]);
      return;
    }
    setSearching(true);
    setSearchMode('es');
    try {
      // 简化搜索参数，只包含关键字和文件类型
      let searchEndpoint = '/api/v1/search/advanced';
      let bodyParams = {
        keyword: q,
        fileTypes: typeFilters
      };
      
      const body = JSON.stringify(bodyParams);
      // 移除limit参数限制，确保显示所有搜索结果
      const resp = await fetch(`${searchEndpoint}`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body,
      });
      if (!resp.ok) throw new Error('搜索失败');
      const data = await resp.json();
      setSearchResults(Array.isArray(data) ? data : []);
    } catch (err) {
      notification.error({ message: '搜索失败', description: err.message });
    } finally {
      setSearching(false);
    }
  }, [searchText, typeFilters, notification]);

  useEffect(() => {
    fetchRootDirs();
    fetchFileTree();
  }, [fetchRootDirs, fetchFileTree]);

  return (
    <Layout style={{ minHeight: '100vh' }} className="app-root">
      <Header className="header">
        <div className="logo">知识库智能检索系统</div>
        <div className="header-actions">
          {/* 根目录配置 */}
          <Space style={{ marginRight: 16 }}>
            {isEditing ? (
              <>
                <Input
                  size="small"
                  value={tempRootDirs}
                  onChange={(e) => setTempRootDirs(e.target.value)}
                  placeholder="请输入扫描根目录"
                  style={{ width: 250 }}
                />
                <Space>
                  <Button size="small" onClick={handleUpdateRootDirs}>保存</Button>
                  <Button size="small" onClick={() => {
                    setIsEditing(false);
                    setTempRootDirs(rootDirs);
                  }}>取消</Button>
                </Space>
              </>
            ) : (
              <Space>
                <span style={{ color: '#fff', marginRight: 8 }}>根目录: {rootDirs}</span>
                <Button size="small" onClick={() => setIsEditing(true)}>修改</Button>
              </Space>
            )}
          </Space>
          
          {/* 强制扫描按钮 */}
          <Button
            danger
            icon={<ReloadOutlined />}
            onClick={handleScan}
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
                onChange={handleSearchInput}
                onPressEnter={runSearch}
              />
              <Button size="small" icon={<SearchOutlined />} loading={loading || searching} onClick={runSearch} />
              <Button
                size="small"
                aria-label={collapsed ? '展开侧边栏' : '折叠侧边栏'}
                icon={collapsed ? <MenuUnfoldOutlined /> : <MenuFoldOutlined />}
                onClick={() => setCollapsed((c) => !c)}
              />
            </Space.Compact>
            <Divider style={{ margin: '8px 0' }} />
            <Select
              mode="multiple"
              allowClear
              size="small"
              placeholder="文件类型筛选"
              style={{ width: '100%' }}
              value={typeFilters}
              onChange={setTypeFilters}
              options={[
                { label: 'MD', value: 'MD' },
                { label: 'DOCX', value: 'DOCX' },
                { label: 'PDF', value: 'PDF' },
                { label: 'CSV', value: 'CSV' },
                { label: 'HTML', value: 'HTML' },
                { label: 'XLSX', value: 'XLSX' },
              ]}
            />
          </div>
          {((searchMode === 'local' && localResults.length > 0) || (searchMode === 'es' && searchResults.length > 0)) ? (
            <div style={{ padding: 8 }}>
              {(searchMode === 'local' && localResults.length > 0) && (
                <>
                  <div style={{ padding: 8, fontWeight: 'bold' }}>文件名匹配结果:</div>
                  {localResults.map((r) => (
                    <div
                      key={r.filePath}
                      className="search-item"
                      style={{ padding: 8, cursor: 'pointer' }}
                      onClick={() => setSelectedFile(r.filePath)}
                    >
                      <div>
                        <span>{r.fileName}</span>
                      </div>
                      {r.snippet ? (
                        <div
                          style={{ opacity: 0.8, fontSize: 12, marginTop: 4 }}
                          dangerouslySetInnerHTML={{ __html: DOMPurify.sanitize(r.snippet) }}
                        />
                      ) : null}
                      <div style={{ opacity: 0.6, fontSize: 12 }}>{r.parentDir}</div>
                    </div>
                  ))}
                </>
              )}
              {(searchMode === 'es' && searchResults.length > 0) && (
                <>
                  <div style={{ padding: 8, fontWeight: 'bold' }}>全文检索结果:</div>
                  {searchResults.map((r) => (
                    <div
                      key={r.filePath}
                      className="search-item"
                      style={{ padding: 8, cursor: 'pointer' }}
                      onClick={() => setSelectedFile(r.filePath)}
                    >
                      <div>
                        <span>{r.fileName}</span>
                      </div>
                      {r.snippet ? (
                        <div
                          style={{ opacity: 0.8, fontSize: 12, marginTop: 4 }}
                          dangerouslySetInnerHTML={{ __html: DOMPurify.sanitize(r.snippet) }}
                        />
                      ) : null}
                      <div style={{ opacity: 0.6, fontSize: 12 }}>{r.parentDir}</div>
                    </div>
                  ))}
                </>
              )}
            </div>
          ) : (
            <FileTree
              files={files}
              searchText={searchText}
              onFileSelect={setSelectedFile}
              loading={loading}
            />
          )}
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
              <PreviewPane filePath={selectedFile} loading={loading} error={error} searchKeywords={searchText} />
            </div>
          </div>
        </Content>
      </Layout>
    </Layout>
  );
}

export default App;

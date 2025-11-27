import React, { useState } from 'react';
import { Tree, Spin, Empty } from 'antd';
import { 
  FileOutlined, 
  FolderOutlined, 
  FolderOpenOutlined,
  FileTextOutlined,
  FilePdfOutlined,
  FileExcelOutlined,
  FileImageOutlined,
  FileWordOutlined
} from '@ant-design/icons';

const { DirectoryTree } = Tree;

// Map file extensions to icons
const getFileIcon = (fileName) => {
  const ext = fileName.split('.').pop().toLowerCase();
  
  switch (ext) {
    case 'md':
      return <FileTextOutlined />;
    case 'pdf':
      return <FilePdfOutlined />;
    case 'csv':
    case 'xls':
    case 'xlsx':
      return <FileExcelOutlined />;
    case 'svg':
    case 'png':
    case 'jpg':
    case 'jpeg':
      return <FileImageOutlined />;
    case 'docx':
    case 'doc':
      return <FileWordOutlined />;
    default:
      return <FileOutlined />;
  }
};

// Convert flat file list to tree structure
const buildTreeFromFiles = (files = []) => {
  const tree = [];
  const pathMap = {};
  
  // First pass: create all nodes
  files.forEach(file => {
    const parts = file.split('/');
    let currentPath = '';
    
    parts.forEach((part, index) => {
      if (!part) return;
      
      currentPath += (currentPath ? '/' : '') + part;
      const isLeaf = index === parts.length - 1;
      
      if (!pathMap[currentPath]) {
        pathMap[currentPath] = {
          title: part,
          key: currentPath,
          isLeaf,
          icon: isLeaf ? getFileIcon(part) : <FolderOutlined />,
          children: [],
        };
      }
    });
  });
  
  // Second pass: build the tree structure
  Object.keys(pathMap).forEach(path => {
    const node = pathMap[path];
    const parentPath = path.substring(0, path.lastIndexOf('/'));
    
    if (parentPath && pathMap[parentPath]) {
      pathMap[parentPath].children.push(node);
    } else {
      tree.push(node);
    }
  });
  
  // Sort nodes: directories first, then files
  const sortNodes = (nodes) => {
    return nodes.sort((a, b) => {
      if (a.isLeaf && !b.isLeaf) return 1;
      if (!a.isLeaf && b.isLeaf) return -1;
      return a.title.localeCompare(b.title);
    }).map(node => ({
      ...node,
      children: node.children ? sortNodes(node.children) : []
    }));
  };
  
  return sortNodes(tree);
};

// Filter tree nodes based on search text
const filterTree = (treeData, searchText) => {
  if (!searchText) return treeData;
  
  // eslint-disable-next-line no-unused-vars
  
  const traverse = (nodes) => {
    const result = [];
    
    nodes.forEach(node => {
      const matches = node.title.toLowerCase().includes(searchText.toLowerCase());
      const children = node.children ? traverse(node.children) : [];
      
      if (matches || children.length > 0) {
        result.push({
          ...node,
          children,
          // Expand matching nodes or nodes with matching children
          expanded: true,
        });
      }
    });
    
    return result;
  };
  
  return traverse(treeData);
};

const FileTree = ({ files, searchText, onFileSelect, loading }) => {
  const [expandedKeys, setExpandedKeys] = useState([]);
  const [selectedKeys, setSelectedKeys] = useState([]);
  
  // Build tree structure from files
  const treeData = buildTreeFromFiles(files);
  
  // Filter tree based on search text
  const filteredTreeData = filterTree(treeData, searchText);
  
  // Handle file selection
  const handleSelect = (keys, info) => {
    setSelectedKeys(keys);
    if (info.node.isLeaf) {
      onFileSelect(info.node.key);
    }
  };
  
  // Handle expand/collapse
  const handleExpand = (keys) => {
    setExpandedKeys(keys);
  };
  
  if (loading) {
    return (
      <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '100%', padding: '20px' }}>
        <Spin>
          <div style={{ marginTop: 8 }}>加载文件树中...</div>
        </Spin>
      </div>
    );
  }
  
  if (filteredTreeData.length === 0) {
    return (
      <Empty
        description={searchText ? "没有找到匹配的文件" : "文件树为空"}
        style={{ padding: '20px' }}
      />
    );
  }
  
  return (
    <DirectoryTree
      treeData={filteredTreeData}
      expandedKeys={expandedKeys}
      selectedKeys={selectedKeys}
      onExpand={handleExpand}
      onSelect={handleSelect}
      defaultExpandAll={false}
      showLine
      icon={(node) => (node.isLeaf ? getFileIcon(node.title) : (expandedKeys.includes(node.key) ? <FolderOpenOutlined /> : <FolderOutlined />))}
    />
  );
};

export default FileTree;

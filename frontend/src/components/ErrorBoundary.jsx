import React, { Component } from 'react';
import { Alert } from 'antd';

/**
 * 错误边界组件
 * 用于捕获和处理React组件渲染过程中的错误，防止单个组件错误导致整个应用崩溃
 */
class ErrorBoundary extends Component {
  constructor(props) {
    super(props);
    this.state = {
      hasError: false,
      error: null,
      errorInfo: null
    };
  }

  /**
   * 捕获子组件树中的JavaScript错误
   */
  static getDerivedStateFromError(error) {
    // 更新状态，下次渲染时显示备用UI
    return {
      hasError: true,
      error
    };
  }

  /**
   * 记录错误信息
   */
  componentDidCatch(error, errorInfo) {
    // 可以将错误信息发送到错误监控服务
    console.error('ErrorBoundary caught an error:', error, errorInfo);
    this.setState({
      errorInfo
    });
  }

  /**
   * 重置错误状态
   */
  resetError = () => {
    this.setState({
      hasError: false,
      error: null,
      errorInfo: null
    });
  };

  render() {
    if (this.state.hasError) {
      // 渲染备用UI
      return (
        <div style={{
          padding: '20px',
          backgroundColor: '#f5f5f5',
          borderRadius: '8px',
          marginTop: '20px',
          textAlign: 'center'
        }}>
          <Alert
            message="组件渲染错误"
            description={
              <div>
                <p style={{ marginBottom: '10px' }}>在渲染组件时发生了错误，以下是错误详情：</p>
                <pre style={{
                  backgroundColor: '#fff',
                  padding: '15px',
                  borderRadius: '4px',
                  border: '1px solid #d9d9d9',
                  overflow: 'auto',
                  textAlign: 'left',
                  fontSize: '13px',
                  maxHeight: '200px',
                  marginBottom: '15px'
                }}>
                  {this.state.error?.toString() || '未知错误'}
                  {this.state.errorInfo?.componentStack && (
                    <div style={{ marginTop: '10px' }}>
                      <strong>组件栈：</strong>
                      <br />
                      {this.state.errorInfo.componentStack}
                    </div>
                  )}
                </pre>
                <button
                  onClick={this.resetError}
                  style={{
                    padding: '8px 16px',
                    backgroundColor: '#1890ff',
                    color: '#fff',
                    border: 'none',
                    borderRadius: '4px',
                    cursor: 'pointer',
                    fontSize: '14px'
                  }}
                >
                  重新加载
                </button>
              </div>
            }
            type="error"
            showIcon
          />
        </div>
      );
    }

    // 如果没有错误，渲染子组件
    return this.props.children;
  }
}

export default ErrorBoundary;

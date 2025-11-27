import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import './index.css'
import { ConfigProvider, theme, App as AntdApp, notification } from 'antd'
import App from './App.jsx'

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

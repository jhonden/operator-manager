import { useState, useEffect } from 'react';
import { Layout as AntLayout, theme } from 'antd';
import { Outlet } from 'react-router-dom';
import { MenuFoldOutlined, MenuUnfoldOutlined } from '@ant-design/icons';
import Sidebar from './Sidebar';
import Header from './Header';

const { Header: AntHeader, Content, Sider } = AntLayout;

/**
 * Main Layout component
 */
const Layout = () => {
  const [collapsed, setCollapsed] = useState(false);

  return (
    <AntLayout style={{ minHeight: '100vh' }}>
      <Sider
        collapsible
        collapsed={collapsed}
        onCollapse={(collapsed) => setCollapsed(collapsed)}
        trigger={null}
        collapsedWidth={64}
        style={{
          overflow: 'auto',
          height: '100vh',
          position: 'fixed',
          left: 0,
          zIndex: 1000,
        }}
      >
        <Sidebar collapsed={collapsed} />
      </Sider>

      <AntLayout style={{ marginLeft: collapsed ? 64 : 200, transition: 'all 0.2s' }}>
        <Header
          collapsed={collapsed}
          onToggle={() => setCollapsed(!collapsed)}
          style={{
            position: 'sticky',
            top: 0,
            zIndex: 999,
            width: '100%',
          }}
        />
        <Content style={{ margin: '24px 16px', minHeight: 'calc(100vh - 64px)' }}>
          <div className="page-transition">
            <Outlet />
          </div>
        </Content>
      </AntLayout>
    </AntLayout>
  );
};

export default Layout;

import { Menu, Layout } from 'antd';
import {
  CodeOutlined,
  AppstoreOutlined,
  DatabaseOutlined,
} from '@ant-design/icons';
import { useNavigate, useLocation } from 'react-router-dom';

const { Sider } = Layout;

interface Props {
  collapsed: boolean;
}

/**
 * Sidebar navigation component
 */
const Sidebar: React.FC<Props> = ({ collapsed }) => {
  const navigate = useNavigate();
  const location = useLocation();

  const menuItems = [
    {
      key: 'operators-group',
      label: '算子',
      type: 'group' as const,
      children: [
        {
          key: '/operators',
          icon: <CodeOutlined />,
          label: '所有算子',
          onClick: () => navigate('/operators'),
        },
      ],
    },
    {
      key: 'packages-group',
      label: '算子包',
      type: 'group' as const,
      children: [
        {
          key: '/packages',
          icon: <AppstoreOutlined />,
          label: '所有算子包',
          onClick: () => navigate('/packages'),
        },
      ],
    },
    {
      key: 'libraries-group',
      label: '公共库',
      type: 'group' as const,
      children: [
        {
          key: '/libraries',
          icon: <DatabaseOutlined />,
          label: '常用公共库',
          onClick: () => navigate('/libraries'),
        },
      ],
    },
  ];

  // Get current selected key based on location
  const getSelectedKey = () => {
    const path = location.pathname;

    if (path.startsWith('/operators')) {
      return '/operators';
    }
    if (path.startsWith('/packages')) {
      return '/packages';
    }
    if (path.startsWith('/libraries')) {
      return '/libraries';
    }

    return path;
  };

  return (
    <Sider width={200} style={{ background: '#001529' }}>
      <div style={{
        height: '64px',
        display: 'flex',
        alignItems: 'center',
        borderBottom: '1px solid rgba(255,255,255,0.1)',
      }}>
        {!collapsed && (
          <h1 style={{
            color: 'white',
            fontSize: 18,
            fontWeight: 600,
            margin: 0,
          }}>
            算子管理器
          </h1>
        )}
      </div>

      <Menu
        theme="dark"
        mode="inline"
        selectedKeys={[getSelectedKey()]}
        items={menuItems}
        style={{ borderRight: 0 }}
      />
    </Sider>
  );
};

export default Sidebar;

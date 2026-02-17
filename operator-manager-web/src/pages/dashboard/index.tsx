import { Card, Row, Col, Statistic, Table, Tag } from 'antd';
import {
  CodeOutlined,
  AppstoreOutlined,
  ThunderboltOutlined,
  ShopOutlined,
} from '@ant-design/icons';
import { useEffect, useState } from 'react';

/**
 * Dashboard page component
 */
const DashboardPage: React.FC = () => {
  const [loading, setLoading] = useState(false);

  // TODO: Fetch actual data from API
  const stats = {
    totalOperators: 128,
    totalPackages: 45,
    runningTasks: 12,
    marketItems: 256,
  };

  const recentTasks = [
    {
      key: '1',
      id: 'Task-001',
      operator: 'Data Cleaning Operator',
      status: 'running',
      duration: '2m 15s',
    },
    {
      key: '2',
      id: 'Task-002',
      operator: 'Report Generator Package',
      status: 'success',
      duration: '1m 30s',
    },
    {
      key: '3',
      id: 'Task-003',
      operator: 'Data Loader',
      status: 'failed',
      duration: '0m 45s',
    },
    {
      key: '4',
      id: 'Task-004',
      operator: 'File Converter',
      status: 'success',
      duration: '0m 55s',
    },
    {
      key: '5',
      id: 'Task-005',
      operator: 'Data Validator',
      status: 'pending',
      duration: '-',
    },
  ];

  const taskColumns = [
    {
      title: 'Task ID',
      dataIndex: 'id',
      key: 'id',
    },
    {
      title: 'Operator/Package',
      dataIndex: 'operator',
      key: 'operator',
    },
    {
      title: 'Status',
      dataIndex: 'status',
      key: 'status',
      render: (status: string) => {
        const colorMap: Record<string, string> = {
          running: 'blue',
          success: 'green',
          failed: 'red',
          pending: 'gray',
        };
        const labelMap: Record<string, string> = {
          running: 'Running',
          success: 'Success',
          failed: 'Failed',
          pending: 'Pending',
        };
        return <Tag color={colorMap[status]}>{labelMap[status]}</Tag>;
      },
    },
    {
      title: 'Duration',
      dataIndex: 'duration',
      key: 'duration',
    },
  ];

  return (
    <div>
      <h2 style={{ marginBottom: 24 }}>Dashboard</h2>

      {/* Statistics Cards */}
      <Row gutter={16} style={{ marginBottom: 24 }}>
        <Col span={6}>
          <Card>
            <Statistic
              title="Total Operators"
              value={stats.totalOperators}
              prefix={<CodeOutlined />}
              valueStyle={{ color: '#1677ff' }}
            />
          </Card>
        </Col>
        <Col span={6}>
          <Card>
            <Statistic
              title="Total Packages"
              value={stats.totalPackages}
              prefix={<AppstoreOutlined />}
              valueStyle={{ color: '#52c41a' }}
            />
          </Card>
        </Col>
        <Col span={6}>
          <Card>
            <Statistic
              title="Running Tasks"
              value={stats.runningTasks}
              prefix={<ThunderboltOutlined />}
              valueStyle={{ color: '#faad14' }}
            />
          </Card>
        </Col>
        <Col span={6}>
          <Card>
            <Statistic
              title="Market Items"
              value={stats.marketItems}
              prefix={<ShopOutlined />}
              valueStyle={{ color: '#722ed1' }}
            />
          </Card>
        </Col>
      </Row>

      {/* Recent Tasks */}
      <Card title="Recent Tasks" bordered={false}>
        <Table
          columns={taskColumns}
          dataSource={recentTasks}
          loading={loading}
          pagination={false}
        />
      </Card>
    </div>
  );
};

export default DashboardPage;

import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import {
  Card,
  Descriptions,
  Button,
  Space,
  Tag,
  Tabs,
  Table,
  message,
  Popconfirm,
} from 'antd';
import {
  ArrowLeftOutlined,
  EditOutlined,
  DeleteOutlined,
  SettingOutlined,
  CodeOutlined,
  BookOutlined,
} from '@ant-design/icons';
import type { Operator } from '@/types';
import { DataFormatOptions, GeneratorOptions } from '@/types';
import { operatorApi } from '@/api/operator';
import BusinessLogicViewer from '@/components/editor/BusinessLogicViewer';

// Helper function to convert data format codes to labels
const formatDataFormat = (dataFormat?: string) => {
  if (!dataFormat) return '-';
  return dataFormat.split(',').map(code => {
    const option = DataFormatOptions.find(opt => opt.value === code);
    return option ? option.label : code;
  }).join(', ');
};

const { TabPane } = Tabs;

/**
 * Operator detail page
 */
const OperatorDetailPage: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const [operator, setOperator] = useState<Operator | null>(null);

  const fetchOperator = async () => {
    if (!id) return;
    try {
      const response = await operatorApi.getOperator(Number(id));
      if (response.data) {
        setOperator(response.data);
      }
    } catch (error: any) {
      message.error(error.message || 'Failed to fetch operator');
    }
  };

  useEffect(() => {
    fetchOperator();
  }, [id]);

  const handleDelete = async () => {
    if (!id) return;
    try {
      await operatorApi.deleteOperator(Number(id));
      message.success('Operator deleted successfully');
      navigate('/operators');
    } catch (error: any) {
      message.error(error.message || 'Failed to delete operator');
    }
  };

  const handlePublish = async () => {
    if (!id) return;
    try {
      await operatorApi.updateOperatorStatus(Number(id), 'PUBLISHED');
      message.success('Operator published successfully');
      fetchOperator();
    } catch (error: any) {
      message.error(error.message || 'Failed to publish operator');
    }
  };

  if (!operator) {
    return <div>Loading...</div>;
  }

  const parameterColumns = [
    {
      title: 'Name',
      dataIndex: 'name',
      key: 'name',
    },
    {
      title: 'Type',
      dataIndex: 'parameterType',
      key: 'parameterType',
      width: 120,
      render: (type: string) => <Tag>{type}</Tag>,
    },
    {
      title: 'Required',
      dataIndex: 'isRequired',
      key: 'isRequired',
      width: 100,
      render: (required: boolean) => (
        <Tag color={required ? 'red' : 'green'}>{required ? 'Yes' : 'No'}</Tag>
      ),
    },
    {
      title: 'Default Value',
      dataIndex: 'defaultValue',
      key: 'defaultValue',
      width: 150,
    },
    {
      title: 'Description',
      dataIndex: 'description',
      key: 'description',
    },
  ];


  const inputParameters = operator.parameters?.filter((p) => p.ioType === 'INPUT' || p.direction === 'INPUT') || [];
  const outputParameters = operator.parameters?.filter((p) => p.ioType === 'OUTPUT' || p.direction === 'OUTPUT') || [];

  return (
    <div style={{ padding: '24px' }}>
      {/* Header */}
      <Card
        title={
          <Space>
            <Button
              type="text"
              icon={<ArrowLeftOutlined />}
              onClick={() => navigate('/operators')}
            >
              Back
            </Button>
            <span>{operator.name}</span>
            <Tag color={operator.language === 'JAVA' ? 'blue' : 'green'}>
              {operator.language}
            </Tag>
            <Tag
              color={
                operator.status === 'PUBLISHED'
                  ? 'success'
                  : operator.status === 'ARCHIVED'
                  ? 'warning'
                  : 'default'
              }
            >
              {operator.status}
            </Tag>
          </Space>
        }
        extra={
          <Space>
            {operator.status === 'DRAFT' && (
              <Button type="primary" onClick={handlePublish}>
                Publish
              </Button>
            )}
            <Button
              icon={<EditOutlined />}
              onClick={() => navigate(`/operators/${operator.id}/edit`)}
            >
              Edit
            </Button>
            <Popconfirm
              title="Are you sure you want to delete this operator?"
              onConfirm={handleDelete}
              okText="Yes"
              cancelText="No"
            >
              <Button danger icon={<DeleteOutlined />}>
                Delete
              </Button>
            </Popconfirm>
          </Space>
        }
        style={{ marginBottom: 16 }}
      >
        <Descriptions column={2} bordered>
          <Descriptions.Item label="Name">{operator.name}</Descriptions.Item>
          <Descriptions.Item label="Language">
            <Tag color={operator.language === 'JAVA' ? 'blue' : 'green'}>
              {operator.language}
            </Tag>
          </Descriptions.Item>
          <Descriptions.Item label="Current Version">
            {operator.version || '-'}
          </Descriptions.Item>
          <Descriptions.Item label="Generator">
            {operator.generator ? (
              <Tag color={operator.generator === 'dynamic' ? 'blue' : 'green'}>
                {GeneratorOptions.find(opt => opt.value === operator.generator)?.label || operator.generator}
              </Tag>
            ) : '-'}
          </Descriptions.Item>
          <Descriptions.Item label="Operator Code" span={2}>
            <code style={{ padding: '2px 6px', background: '#f5f5f5', borderRadius: '3px' }}>
              {operator.operatorCode || '-'}
            </code>
          </Descriptions.Item>
          <Descriptions.Item label="Object Code" span={2}>
            <code style={{ padding: '2px 6px', background: '#f5f5f5', borderRadius: '3px' }}>
              {operator.objectCode || '-'}
            </code>
          </Descriptions.Item>
          <Descriptions.Item label="Data Format" span={2}>
            {formatDataFormat(operator.dataFormat)}
          </Descriptions.Item>
          <Descriptions.Item label="Description" span={2}>
            {operator.description || '-'}
          </Descriptions.Item>
          <Descriptions.Item label="Created By">
            {operator.createdBy || '-'}
          </Descriptions.Item>
          <Descriptions.Item label="Created At">
            {new Date(operator.createdAt).toLocaleString()}
          </Descriptions.Item>
          <Descriptions.Item label="Updated At">
            {new Date(operator.updatedAt).toLocaleString()}
          </Descriptions.Item>
        </Descriptions>
      </Card>

      {/* Tabs */}
      <Card>
        <Tabs defaultActiveKey="businessLogic">
          <TabPane tab={<span><BookOutlined /> Business Logic</span>} key="businessLogic">
            <BusinessLogicViewer value={operator.businessLogic} />
          </TabPane>

          <TabPane tab={<span><SettingOutlined /> Parameters</span>} key="parameters">
            <Tabs defaultActiveKey="input">
              <TabPane tab={`Input Parameters (${inputParameters.length})`} key="input">
                <Table
                  columns={parameterColumns}
                  dataSource={inputParameters}
                  rowKey="id"
                  pagination={false}
                />
              </TabPane>
              <TabPane tab={`Output Parameters (${outputParameters.length})`} key="output">
                <Table
                  columns={parameterColumns}
                  dataSource={outputParameters}
                  rowKey="id"
                  pagination={false}
                />
              </TabPane>
            </Tabs>
          </TabPane>

          <TabPane tab={<span><CodeOutlined /> Code</span>} key="code">
            <div
              style={{
                background: '#f5f5f5',
                padding: '16px',
                borderRadius: '4px',
                minHeight: '200px',
              }}
            >
              <pre style={{ margin: 0, whiteSpace: 'pre-wrap' }}>
                {operator.code || '// No code available'}
              </pre>
            </div>
          </TabPane>

        </Tabs>
      </Card>
    </div>
  );
};

export default OperatorDetailPage;

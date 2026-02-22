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
import type { LibraryDependencyResponse, LibraryType } from '@/types/library';
import { operatorApi } from '@/api/operator';
import BusinessLogicViewer from '@/components/editor/BusinessLogicViewer';
import { t } from '@/utils/i18n';

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
  const [libraries, setLibraries] = useState<LibraryDependencyResponse[]>([]);
  const [librariesLoading, setLibrariesLoading] = useState(false);

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

  const fetchOperatorLibraries = async () => {
    if (!id) return;
    setLibrariesLoading(true);
    try {
      console.log('[Operator Detail Page] Fetching operator libraries for operatorId:', id);
      const response = await operatorApi.getOperatorLibraries(Number(id));
      console.log('[Operator Detail Page] Fetch operator libraries response:', response);

      if (response.data) {
        console.log('[Operator Detail Page] Operator libraries fetched:', response.data);
        setLibraries(response.data);
      }
    } catch (error: any) {
      console.error('[Operator Detail Page] Error fetching operator libraries:', error);
      message.error(error.message || '获取公共库列表失败');
    } finally {
      setLibrariesLoading(false);
    }
  };

  useEffect(() => {
    fetchOperator();
    fetchOperatorLibraries();
  }, [id]);

  const handleDelete = async () => {
    if (!id) return;
    try {
      await operatorApi.deleteOperator(Number(id));
      message.success(t('message.operator.deletedSuccess'));
      navigate('/operators');
    } catch (error: any) {
      message.error(error.message || t('message.operator.deletedFailed'));
    }
  };

  const handlePublish = async () => {
    if (!id) return;
    try {
      await operatorApi.updateOperatorStatus(Number(id), 'PUBLISHED');
      message.success(t('message.operator.publishedSuccess'));
      fetchOperator();
    } catch (error: any) {
      message.error(error.message || t('message.operator.publishFailed'));
    }
  };

  if (!operator) {
    return <div>{t('common.loading')}</div>;
  }

  const parameterColumns = [
    {
      title: t('common.name'),
      dataIndex: 'name',
      key: 'name',
    },
    {
      title: t('parameter.type'),
      dataIndex: 'parameterType',
      key: 'parameterType',
      width: 120,
      render: (type: string) => <Tag>{type}</Tag>,
    },
    {
      title: t('parameter.required'),
      dataIndex: 'isRequired',
      key: 'isRequired',
      width: 100,
      render: (required: boolean) => (
        <Tag color={required ? 'red' : 'green'}>{required ? t('common.yes') : t('common.no')}</Tag>
      ),
    },
    {
      title: t('parameter.defaultValue'),
      dataIndex: 'defaultValue',
      key: 'defaultValue',
      width: 150,
    },
    {
      title: t('common.description'),
      dataIndex: 'description',
      key: 'description',
    },
  ];

  const libraryColumns = [
    {
      title: '公共库名称',
      dataIndex: 'libraryName',
      key: 'libraryName',
      width: 200,
    },
    {
      title: '类型',
      dataIndex: 'libraryType',
      key: 'libraryType',
      width: 120,
      render: (type: LibraryType) => {
        const colorMap: Record<string, string> = {
          CONSTANT: 'blue',
          METHOD: 'green',
          MODEL: 'purple',
          CUSTOM: 'orange',
        };
        return <Tag color={colorMap[type]}>{type}</Tag>;
      },
    },
    {
      title: '描述',
      dataIndex: 'libraryDescription',
      key: 'libraryDescription',
      ellipsis: true,
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
              {t('common.back')}
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
                {t('common.publish')}
              </Button>
            )}
            <Button
              icon={<EditOutlined />}
              onClick={() => navigate(`/operators/${operator.id}/edit`)}
            >
              {t('common.edit')}
            </Button>
            <Popconfirm
              title={t('message.operator.deleteConfirm')}
              onConfirm={handleDelete}
              okText={t('common.yes')}
              cancelText={t('common.no')}
            >
              <Button danger icon={<DeleteOutlined />}>
                {t('common.delete')}
              </Button>
            </Popconfirm>
          </Space>
        }
        style={{ marginBottom: 16 }}
      >
        <Descriptions column={2} bordered>
          <Descriptions.Item label={t('common.name')}>{operator.name}</Descriptions.Item>
          <Descriptions.Item label={t('operator.language')}>
            <Tag color={operator.language === 'JAVA' ? 'blue' : 'green'}>
              {operator.language}
            </Tag>
          </Descriptions.Item>
          <Descriptions.Item label={t('common.version')}>
            {operator.version || '-'}
          </Descriptions.Item>
          <Descriptions.Item label={t('operator.generator')}>
            {operator.generator ? (
              <Tag color={operator.generator === 'dynamic' ? 'blue' : 'green'}>
                {GeneratorOptions.find(opt => opt.value === operator.generator)?.label || operator.generator}
              </Tag>
            ) : '-'}
          </Descriptions.Item>
          <Descriptions.Item label={t('operator.operatorCode')} span={2}>
            <code style={{ padding: '2px 6px', background: '#f5f5f5', borderRadius: '3px' }}>
              {operator.operatorCode || '-'}
            </code>
          </Descriptions.Item>
          <Descriptions.Item label={t('operator.objectCode')} span={2}>
            <code style={{ padding: '2px 6px', background: '#f5f5f5', borderRadius: '3px' }}>
              {operator.objectCode || '-'}
            </code>
          </Descriptions.Item>
          <Descriptions.Item label={t('operator.dataFormat')} span={2}>
            {formatDataFormat(operator.dataFormat)}
          </Descriptions.Item>
          <Descriptions.Item label={t('common.description')} span={2}>
            {operator.description || '-'}
          </Descriptions.Item>
          <Descriptions.Item label={t('common.createdBy')}>
            {operator.createdBy || '-'}
          </Descriptions.Item>
          <Descriptions.Item label={t('common.createdAt')}>
            {new Date(operator.createdAt).toLocaleString()}
          </Descriptions.Item>
          <Descriptions.Item label={t('common.updatedAt')}>
            {new Date(operator.updatedAt).toLocaleString()}
          </Descriptions.Item>
        </Descriptions>
      </Card>

      {/* Tabs */}
      <Card>
        <Tabs defaultActiveKey="businessLogic">
          <TabPane tab={<span><BookOutlined /> {t('operator.businessLogic')}</span>} key="businessLogic">
            <BusinessLogicViewer value={operator.businessLogic} />
          </TabPane>

          <TabPane tab={<span><SettingOutlined /> {t('operator.parameters')}</span>} key="parameters">
            <Tabs defaultActiveKey="input">
              <TabPane tab={`${t('parameter.input')} (${inputParameters.length})`} key="input">
                <Table
                  columns={parameterColumns}
                  dataSource={inputParameters}
                  rowKey="id"
                  pagination={false}
                />
              </TabPane>
              <TabPane tab={`${t('parameter.output')} (${outputParameters.length})`} key="output">
                <Table
                  columns={parameterColumns}
                  dataSource={outputParameters}
                  rowKey="id"
                  pagination={false}
                />
              </TabPane>
            </Tabs>
          </TabPane>

          <TabPane tab={<span><CodeOutlined /> {t('operator.code')}</span>} key="code">
            <div
              style={{
                background: '#f5f5f5',
                padding: '16px',
                borderRadius: '4px',
                minHeight: '200px',
              }}
            >
              <pre style={{ margin: 0, whiteSpace: 'pre-wrap' }}>
                {operator.code || '//' + t('common.noData')}
              </pre>
            </div>
          </TabPane>

          <TabPane tab={<span><BookOutlined /> {t('operator.libraries')}</span>} key="libraries">
            <Table
              loading={librariesLoading}
              rowKey="id"
              columns={libraryColumns}
              dataSource={libraries}
              pagination={false}
            />
          </TabPane>

        </Tabs>
      </Card>
    </div>
  );
};

export default OperatorDetailPage;

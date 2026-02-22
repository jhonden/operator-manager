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
  Row,
  Col,
  Typography,
  Modal,
  Select,
  Input,
  Form,
} from 'antd';
import {
  ArrowLeftOutlined,
  EditOutlined,
  DeleteOutlined,
  AppstoreOutlined,
  PlusOutlined,
  ArrowUpOutlined,
  ArrowDownOutlined,
} from '@ant-design/icons';
import type { OperatorPackage, PackageOperator } from '@/types';
import { packageApi } from '@/api/package';
import { operatorApi } from '@/api/operator';
import { t } from '@/utils/i18n';

const { TabPane } = Tabs;
const { Text } = Typography;

/**
 * Operator package detail page
 */
const PackageDetailPage: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const [packageData, setPackageData] = useState<OperatorPackage | null>(null);
  const [operators, setOperators] = useState<PackageOperator[]>([]);
  const [availableOperators, setAvailableOperators] = useState<any[]>([]);
  const [addOperatorModalVisible, setAddOperatorModalVisible] = useState(false);
  const [addOperatorLoading, setAddOperatorLoading] = useState(false);
  const [selectedOperatorId, setSelectedOperatorId] = useState<number | undefined>(undefined);
  const [addForm] = Form.useForm();

  const fetchPackage = async () => {
    if (!id) return;
    try {
      const response = await packageApi.getPackage(Number(id));
      if (response.data) {
        setPackageData(response.data);
        setOperators(response.data.operators || []);
      }
    } catch (error: any) {
      message.error(error.message || '获取算子包失败');
    }
  };

  const fetchAvailableOperators = async () => {
    try {
      const response = await operatorApi.getAllOperators({ page: 0, size: 1000 });
      if (response.data) {
        // Get all operators that are not already in this package
        const existingOperatorIds = operators.map(op => op.operatorId);
        const available = response.data.content.filter(
          (op: any) => !existingOperatorIds.includes(op.id)
        );
        setAvailableOperators(available);
      }
    } catch (error: any) {
      message.error(error.message || '获取算子列表失败');
    }
  };

  const handleAddOperator = async () => {
    if (!id || !selectedOperatorId) {
      message.warning('请选择一个算子');
      return;
    }

    setAddOperatorLoading(true);
    try {
      await packageApi.addOperator(Number(id), {
        operatorId: selectedOperatorId!,
        versionId: 0,
        enabled: true,
        orderIndex: operators.length,
      });
      message.success('算子添加到包成功');
      setAddOperatorModalVisible(false);
      setSelectedOperatorId(undefined);
      addForm.resetFields();
      fetchPackage();
    } catch (error: any) {
      message.error(error.response?.data?.error || error.message || '添加算子到包失败');
    } finally {
      setAddOperatorLoading(false);
    }
  };

  const handleOpenAddModal = async () => {
    await fetchAvailableOperators();
    setAddOperatorModalVisible(true);
  };

  useEffect(() => {
    fetchPackage();
  }, [id]);

  const handleDelete = async () => {
    if (!id) return;
    try {
      await packageApi.deletePackage(Number(id));
      message.success('算子包删除成功');
      navigate('/packages');
    } catch (error: any) {
      message.error(error.message || '删除算子包失败');
    }
  };

  const handlePublish = async () => {
    if (!id) return;
    try {
      await packageApi.updatePackageStatus(Number(id), 'PUBLISHED');
      message.success('算子包发布成功');
      fetchPackage();
    } catch (error: any) {
      message.error(error.message || '发布算子包失败');
    }
  };

  const handleRemoveOperator = async (operatorId: number) => {
    if (!id) return;
    try {
      await packageApi.removeOperator(Number(id), operatorId);
      message.success('算子已从包中移除');
      fetchPackage();
    } catch (error: any) {
      message.error(error.message || '移除算子失败');
    }
  };

  const handleReorder = async (operatorId: number, direction: 'up' | 'down') => {
    if (!id) return;
    try {
      await packageApi.reorderOperators(Number(id), operatorId, direction);
      message.success('算子重排序成功');
      fetchPackage();
    } catch (error: any) {
      message.error(error.message || '重排序算子失败');
    }
  };

  if (!packageData) {
    return <div>{t('common.loading')}</div>;
  }

  const operatorColumns = [
    {
      title: '顺序',
      dataIndex: 'orderIndex',
      key: 'orderIndex',
      width: 80,
      render: (order: number) => (
        <Tag color="blue">#{order + 1}</Tag>
      ),
    },
    {
      title: '算子名称',
      dataIndex: 'operatorName',
      key: 'operatorName',
      render: (name: string, record: PackageOperator) => (
        <a onClick={() => navigate(`/operators/${record.operatorId}`)}>{name}</a>
      ),
    },
    {
      title: t('operator.language'),
      dataIndex: 'operatorLanguage',
      key: 'operatorLanguage',
      width: 100,
      render: (language: string) => {
        const color = language === 'JAVA' ? 'blue' : 'green';
        return <Tag color={color}>{language}</Tag>;
      },
    },
    {
      title: t('common.version'),
      dataIndex: 'versionNumber',
      key: 'versionNumber',
      width: 100,
    },
    {
      title: '状态',
      dataIndex: 'enabled',
      key: 'enabled',
      width: 100,
      render: (enabled: boolean) => (
        <Tag color={enabled ? 'success' : 'default'}>
          {enabled ? '已启用' : '已禁用'}
        </Tag>
      ),
    },
    {
      title: '备注',
      dataIndex: 'notes',
      key: 'notes',
      ellipsis: true,
    },
    {
      title: t('common.actions'),
      key: 'actions',
      width: 180,
      render: (_: any, record: PackageOperator, index: number) => (
        <Space size="small">
          <Button
            type="link"
            size="small"
            icon={<ArrowUpOutlined />}
            onClick={() => handleReorder(record.operatorId, 'up')}
            disabled={index === 0}
          >
            上移
          </Button>
          <Button
            type="link"
            size="small"
            icon={<ArrowDownOutlined />}
            onClick={() => handleReorder(record.operatorId, 'down')}
            disabled={index === operators.length - 1}
          >
            下移
          </Button>
          <Popconfirm
            title="确定要移除此算子吗？"
            onConfirm={() => handleRemoveOperator(record.operatorId)}
            okText={t('common.yes')}
            cancelText={t('common.no')}
          >
            <Button type="link" size="small" danger>
              移除
            </Button>
          </Popconfirm>
        </Space>
      ),
    },
  ];

  return (
    <div style={{ padding: '24px' }}>
      {/* Header */}
      <Card
        title={
          <Space>
            <Button
              type="text"
              icon={<ArrowLeftOutlined />}
              onClick={() => navigate('/packages')}
            >
              {t('common.back')}
            </Button>
            {packageData.icon ? (
              <img
                src={packageData.icon}
                alt=""
                style={{ width: 32, height: 32, borderRadius: 4 }}
              />
            ) : (
              <AppstoreOutlined style={{ fontSize: 24 }} />
            )}
            <span>{packageData.name}</span>
            <Tag
              color={
                packageData.status === 'PUBLISHED'
                  ? 'success'
                  : packageData.status === 'ARCHIVED'
                  ? 'warning'
                  : 'default'
              }
            >
              {packageData.status}
            </Tag>
            {packageData.featured && (
              <Tag color="gold">精选</Tag>
            )}
          </Space>
        }
        extra={
          <Space>
            {packageData.status === 'DRAFT' && (
              <Button type="primary" onClick={handlePublish}>
                {t('common.publish')}
              </Button>
            )}
            <Button
              icon={<EditOutlined />}
              onClick={() => navigate(`/packages/${packageData.id}/edit`)}
            >
              {t('common.edit')}
            </Button>
            <Popconfirm
              title="确定要删除此算子包吗？"
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
        <Row gutter={16}>
          <Col span={16}>
            <Descriptions column={1} bordered>
              <Descriptions.Item label="算子包名称">
                {packageData.name}
              </Descriptions.Item>
              <Descriptions.Item label="业务场景">
                {packageData.businessScenario}
              </Descriptions.Item>
              <Descriptions.Item label={t('common.description')}>
                {packageData.description || '-'}
              </Descriptions.Item>
            </Descriptions>
          </Col>
          <Col span={8}>
            <Card size="small" title="统计信息">
              <Space direction="vertical" style={{ width: '100%' }}>
                <div>
                  <Text type="secondary">总算子数</Text>
                  <br />
                  <Text strong style={{ fontSize: 24 }}>
                    {packageData.operatorCount || 0}
                  </Text>
                </div>
                <div>
                  <Text type="secondary">下载次数</Text>
                  <br />
                  <Text strong style={{ fontSize: 24 }}>
                    {packageData.downloadsCount || 0}
                  </Text>
                </div>
                <div>
                  <Text type="secondary">状态</Text>
                  <br />
                  <Tag
                    color={
                      packageData.status === 'PUBLISHED'
                        ? 'success'
                        : packageData.status === 'ARCHIVED'
                        ? 'warning'
                        : 'default'
                    }
                    style={{ fontSize: 16 }}
                  >
                    {packageData.status}
                  </Tag>
                </div>
              </Space>
            </Card>
          </Col>
        </Row>
        <div style={{ marginTop: 16 }}>
          <Descriptions column={2} bordered size="small">
            <Descriptions.Item label={t('common.createdBy')}>
              {packageData.createdBy}
            </Descriptions.Item>
            <Descriptions.Item label={t('common.createdAt')}>
              {new Date(packageData.createdAt).toLocaleString()}
            </Descriptions.Item>
            <Descriptions.Item label={t('common.updatedAt')}>
              {new Date(packageData.updatedAt).toLocaleString()}
            </Descriptions.Item>
            <Descriptions.Item label={t('common.version')}>
              {packageData.version || '-'}
            </Descriptions.Item>
          </Descriptions>
        </div>
      </Card>

      {/* Tabs */}
      <Card>
        <Tabs defaultActiveKey="operators">
          <TabPane
            tab={
              <span>
                <AppstoreOutlined />
                Operators ({operators.length})
              </span>
            }
            key="operators"
          >
            <div style={{ marginBottom: 16 }}>
              <Button
                type="primary"
                icon={<PlusOutlined />}
                onClick={handleOpenAddModal}
              >
                添加算子
              </Button>
            </div>
            <Table
              columns={operatorColumns}
              dataSource={operators}
              rowKey="id"
              pagination={false}
            />
          </TabPane>

          <TabPane tab="数据流" key="dataflow">
            <div style={{ padding: '24px', textAlign: 'center' }}>
              <Text type="secondary">
                数据流可视化即将推出...
              </Text>
            </div>
          </TabPane>
        </Tabs>
      </Card>

      {/* Add Operator Modal */}
      <Modal
        title="添加算子到包"
        open={addOperatorModalVisible}
        onOk={handleAddOperator}
        onCancel={() => {
          setAddOperatorModalVisible(false);
          setSelectedOperatorId(undefined);
          addForm.resetFields();
        }}
        confirmLoading={addOperatorLoading}
        width={600}
      >
        <Form form={addForm} layout="vertical">
          <Form.Item
            label="选择算子"
            name="operatorId"
            rules={[{ required: true, message: '请选择一个算子' }]}
          >
            <Select
              placeholder="选择一个算子"
              showSearch
              filterOption={(input, option) =>
                (option?.label ?? '').toLowerCase().includes(input.toLowerCase())
              }
              options={availableOperators.map(op => ({
                label: op.name,
                value: op.id,
              }))}
              onChange={(value: number) => {
                setSelectedOperatorId(value);
                // Find the selected operator
                const operator = availableOperators.find(op => op.id === value);
                if (operator) {
                  // Use the operator's default version
                }
              }}
              value={selectedOperatorId}
            />
          </Form.Item>

          <Form.Item label="顺序索引">
            <Input
              type="number"
              value={operators.length}
              disabled
            />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
};

export default PackageDetailPage;

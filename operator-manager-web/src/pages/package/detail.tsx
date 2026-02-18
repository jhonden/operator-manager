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
  Switch,
} from 'antd';
import {
  ArrowLeftOutlined,
  EditOutlined,
  DeleteOutlined,
  PlayCircleOutlined,
  AppstoreOutlined,
  PlusOutlined,
  ArrowUpOutlined,
  ArrowDownOutlined,
} from '@ant-design/icons';
import type { OperatorPackage, PackageOperator } from '@/types';
import { packageApi } from '@/api/package';
import { operatorApi } from '@/api/operator';

const { TabPane } = Tabs;
const { Text } = Typography;

/**
 * Operator package detail page
 */
const PackageDetailPage: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const [loading, setLoading] = useState(false);
  const [packageData, setPackageData] = useState<OperatorPackage | null>(null);
  const [operators, setOperators] = useState<PackageOperator[]>([]);
  const [availableOperators, setAvailableOperators] = useState<any[]>([]);
  const [addOperatorModalVisible, setAddOperatorModalVisible] = useState(false);
  const [addOperatorLoading, setAddOperatorLoading] = useState(false);
  const [selectedOperatorId, setSelectedOperatorId] = useState<number | undefined>(undefined);
  const [addForm] = Form.useForm();

  const fetchPackage = async () => {
    if (!id) return;
    setLoading(true);
    try {
      const response = await packageApi.getPackage(Number(id));
      if (response.data) {
        setPackageData(response.data);
        setOperators(response.data.operators || []);
      }
    } catch (error: any) {
      message.error(error.message || 'Failed to fetch package');
    } finally {
      setLoading(false);
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
      message.error(error.message || 'Failed to fetch operators');
    }
  };

  const handleAddOperator = async () => {
    if (!id || !selectedOperatorId) {
      message.warning('Please select an operator');
      return;
    }

    setAddOperatorLoading(true);
    try {
      await packageApi.addOperator(Number(id), {
        operatorId: selectedOperatorId,
        enabled: true,
        orderIndex: operators.length,
      });
      message.success('Operator added to package successfully');
      setAddOperatorModalVisible(false);
      setSelectedOperatorId(undefined);
      addForm.resetFields();
      fetchPackage();
    } catch (error: any) {
      message.error(error.response?.data?.error || error.message || 'Failed to add operator to package');
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
      message.success('Package deleted successfully');
      navigate('/packages');
    } catch (error: any) {
      message.error(error.message || 'Failed to delete package');
    }
  };

  const handlePublish = async () => {
    if (!id) return;
    try {
      await packageApi.updatePackageStatus(Number(id), 'PUBLISHED');
      message.success('Package published successfully');
      fetchPackage();
    } catch (error: any) {
      message.error(error.message || 'Failed to publish package');
    }
  };

  const handleRemoveOperator = async (operatorId: number) => {
    if (!id) return;
    try {
      await packageApi.removeOperator(Number(id), operatorId);
      message.success('Operator removed from package');
      fetchPackage();
    } catch (error: any) {
      message.error(error.message || 'Failed to remove operator');
    }
  };

  const handleReorder = async (operatorId: number, direction: 'up' | 'down') => {
    if (!id) return;
    try {
      await packageApi.reorderOperators(Number(id), operatorId, direction);
      message.success('Operator reordered successfully');
      fetchPackage();
    } catch (error: any) {
      message.error(error.message || 'Failed to reorder operator');
    }
  };

  if (!packageData) {
    return <div>Loading...</div>;
  }

  const operatorColumns = [
    {
      title: 'Order',
      dataIndex: 'orderIndex',
      key: 'orderIndex',
      width: 80,
      render: (order: number) => (
        <Tag color="blue">#{order + 1}</Tag>
      ),
    },
    {
      title: 'Operator Name',
      dataIndex: 'operatorName',
      key: 'operatorName',
      render: (name: string, record: PackageOperator) => (
        <a onClick={() => navigate(`/operators/${record.operatorId}`)}>{name}</a>
      ),
    },
    {
      title: 'Language',
      dataIndex: 'operatorLanguage',
      key: 'operatorLanguage',
      width: 100,
      render: (language: string) => {
        const color = language === 'JAVA' ? 'blue' : 'green';
        return <Tag color={color}>{language}</Tag>;
      },
    },
    {
      title: 'Version',
      dataIndex: 'versionNumber',
      key: 'versionNumber',
      width: 100,
    },
    {
      title: 'Status',
      dataIndex: 'enabled',
      key: 'enabled',
      width: 100,
      render: (enabled: boolean) => (
        <Tag color={enabled ? 'success' : 'default'}>
          {enabled ? 'Enabled' : 'Disabled'}
        </Tag>
      ),
    },
    {
      title: 'Notes',
      dataIndex: 'notes',
      key: 'notes',
      ellipsis: true,
    },
    {
      title: 'Actions',
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
            Up
          </Button>
          <Button
            type="link"
            size="small"
            icon={<ArrowDownOutlined />}
            onClick={() => handleReorder(record.operatorId, 'down')}
            disabled={index === operators.length - 1}
          >
            Down
          </Button>
          <Popconfirm
            title="Are you sure you want to remove this operator?"
            onConfirm={() => handleRemoveOperator(record.operatorId)}
            okText="Yes"
            cancelText="No"
          >
            <Button type="link" size="small" danger>
              Remove
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
              Back
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
              <Tag color="gold">Featured</Tag>
            )}
          </Space>
        }
        extra={
          <Space>
            {packageData.status === 'DRAFT' && (
              <Button type="primary" onClick={handlePublish}>
                Publish
              </Button>
            )}
            <Button
              icon={<EditOutlined />}
              onClick={() => navigate(`/packages/${packageData.id}/edit`)}
            >
              Edit
            </Button>
            <Popconfirm
              title="Are you sure you want to delete this package?"
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
        <Row gutter={16}>
          <Col span={16}>
            <Descriptions column={1} bordered>
              <Descriptions.Item label="Package Name">
                {packageData.name}
              </Descriptions.Item>
              <Descriptions.Item label="Business Scenario">
                {packageData.businessScenario}
              </Descriptions.Item>
              <Descriptions.Item label="Description">
                {packageData.description || '-'}
              </Descriptions.Item>
              <Descriptions.Item label="Tags">
                {packageData.tags?.map((tag) => (
                  <Tag key={tag}>{tag}</Tag>
                )) || '-'}
              </Descriptions.Item>
            </Descriptions>
          </Col>
          <Col span={8}>
            <Card size="small" title="Statistics">
              <Space direction="vertical" style={{ width: '100%' }}>
                <div>
                  <Text type="secondary">Total Operators</Text>
                  <br />
                  <Text strong style={{ fontSize: 24 }}>
                    {packageData.operatorCount || 0}
                  </Text>
                </div>
                <div>
                  <Text type="secondary">Downloads</Text>
                  <br />
                  <Text strong style={{ fontSize: 24 }}>
                    {packageData.downloadsCount || 0}
                  </Text>
                </div>
                <div>
                  <Text type="secondary">Status</Text>
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
            <Descriptions.Item label="Created By">
              {packageData.createdBy}
            </Descriptions.Item>
            <Descriptions.Item label="Created At">
              {new Date(packageData.createdAt).toLocaleString()}
            </Descriptions.Item>
            <Descriptions.Item label="Updated At">
              {new Date(packageData.updatedAt).toLocaleString()}
            </Descriptions.Item>
            <Descriptions.Item label="Version">
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
                Add Operator
              </Button>
            </div>
            <Table
              columns={operatorColumns}
              dataSource={operators}
              rowKey="id"
              pagination={false}
            />
          </TabPane>

          <TabPane tab="Data Flow" key="dataflow">
            <div style={{ padding: '24px', textAlign: 'center' }}>
              <Text type="secondary">
                Data flow visualization coming soon...
              </Text>
            </div>
          </TabPane>
        </Tabs>
      </Card>

      {/* Add Operator Modal */}
      <Modal
        title="Add Operator to Package"
        open={addOperatorModalVisible}
        onOk={handleAddOperator}
        onCancel={() => {
          setAddOperatorModalVisible(false);
          setSelectedOperatorId(undefined);
          setSelectedVersionId(undefined);
          addForm.resetFields();
        }}
        confirmLoading={addOperatorLoading}
        width={600}
      >
        <Form form={addForm} layout="vertical">
          <Form.Item
            label="Select Operator"
            name="operatorId"
            rules={[{ required: true, message: 'Please select an operator' }]}
          >
            <Select
              placeholder="Select an operator"
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

          <Form.Item label="Order Index">
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

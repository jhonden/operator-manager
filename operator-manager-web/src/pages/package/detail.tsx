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
  Collapse,
} from 'antd';
import {
  ArrowLeftOutlined,
  EditOutlined,
  DeleteOutlined,
  AppstoreOutlined,
  PlusOutlined,
  ArrowUpOutlined,
  ArrowDownOutlined,
  FolderOutlined,
  FileOutlined,
  SettingOutlined,
  ReloadOutlined,
  ToolOutlined,
} from '@ant-design/icons';
import type {
  OperatorPackage,
  PackageOperator,
  PackagePathConfigResponse,
  PackagePreviewResponse,
  PackagePreviewTreeNode,
} from '@/types';
import type {
  LibraryPathConfigResponse,
  OperatorPathConfigResponse,
} from '@/types/library';
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

  // 公共库和打包配置相关状态
  const [activeTab, setActiveTab] = useState('operators');
  const [commonLibraries, setCommonLibraries] = useState<any[]>([]);
  const [pathConfig, setPathConfig] = useState<PackagePathConfigResponse | null>(null);
  const [preview, setPreview] = useState<PackagePreviewResponse | null>(null);
  const [pathConfigModalVisible, setPathConfigModalVisible] = useState(false);
  const [batchConfigModalVisible, setBatchConfigModalVisible] = useState(false);
  const [currentEditItem, setCurrentEditItem] = useState<{
    type: 'operator' | 'library';
    id: number;
    name: string;
  } | null>(null);
  const [batchConfigType, setBatchConfigType] = useState<'operator' | 'library' | null>(null);
  const [pathConfigForm] = Form.useForm();

  // 折叠状态控制
  const [libraryCardCollapsed, setLibraryCardCollapsed] = useState(false);
  const [operatorCardCollapsed, setOperatorCardCollapsed] = useState(false);

  const fetchPackage = async () => {
    if (!id) return;
    try {
      const response = await packageApi.getPackage(Number(id));
      if (response.data) {
        setPackageData(response.data);
        setOperators(response.data.operators || []);
        setCommonLibraries(response.data.commonLibraries || []);
      }
    } catch (error: any) {
      message.error(error.message || '获取算子包失败');
    }
  };

  const fetchPathConfig = async () => {
    if (!id) return;
    try {
      const response = await packageApi.getPackagePathConfig(Number(id));
      if (response.data) {
        setPathConfig(response.data);
      }
    } catch (error: any) {
      message.error(error.message || '获取打包配置失败');
    }
  };

  const fetchPreview = async (template: 'legacy' | 'modern' | 'custom' = 'legacy') => {
    if (!id) return;
    try {
      const response = await packageApi.generatePreview(Number(id), template);
      if (response.data) {
        setPreview(response.data);
      }
    } catch (error: any) {
      message.error(error.message || '获取打包预览失败');
    }
  };

  const handleTabChange = (key: string) => {
    setActiveTab(key);
    if (key === 'config') {
      fetchPathConfig();
      fetchPreview();
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

  // 打包配置相关处理函数
  const handleEditPathConfig = (type: 'operator' | 'library', id: number, name: string) => {
    setCurrentEditItem({ type, id, name });
    setPathConfigModalVisible(true);
  };

  const handleSavePathConfig = async () => {
    if (!id || !currentEditItem) return;

    const values = pathConfigForm.getFieldsValue();
    try {
      if (currentEditItem.type === 'operator') {
        await packageApi.updateOperatorPathConfig(Number(id), currentEditItem.id, {
          operatorId: currentEditItem.id,
          useCustomPath: values.useCustomPath,
          customPackagePath: values.customPackagePath,
        });
        message.success('算子路径配置已更新');
      } else {
        await packageApi.updateLibraryPathConfig(Number(id), currentEditItem.id, {
          libraryId: currentEditItem.id,
          useCustomPath: values.useCustomPath,
          customPackagePath: values.customPackagePath,
        });
        message.success('公共库路径配置已更新');
      }
      setPathConfigModalVisible(false);
      pathConfigForm.resetFields();
      fetchPathConfig();
      fetchPreview();
    } catch (error: any) {
      message.error(error.message || '保存路径配置失败');
    }
  };

  const handleBatchConfig = (type: 'operator' | 'library') => {
    setBatchConfigType(type);
    setBatchConfigModalVisible(true);
  };

  const handleSaveBatchConfig = async () => {
    if (!id || !batchConfigType) return;

    try {
      if (batchConfigType === 'operator') {
        const operatorIds = pathConfig?.operatorConfigs?.map(c => c.operatorId) || [];
        await packageApi.batchUpdateOperatorPathConfig(Number(id), {
          useRecommendedPath: true,
          operatorIds,
        });
        message.success('算子路径配置已批量更新');
      } else {
        const libraryIds = pathConfig?.libraryConfigs?.map(c => c.libraryId) || [];
        await packageApi.batchUpdateLibraryPathConfig(Number(id), {
          useRecommendedPath: true,
          libraryIds,
        });
        message.success('公共库路径配置已批量更新');
      }
      setBatchConfigModalVisible(false);
      fetchPathConfig();
      fetchPreview();
    } catch (error: any) {
      message.error(error.message || '批量更新路径配置失败');
    }
  };

  // 渲染路径预览树
  const renderPreviewTree = (node: PackagePreviewTreeNode, level = 0) => {
    const indent = level * 24;
    return (
      <div key={node.path} style={{ paddingLeft: indent }}>
        <span style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
          {node.type === 'directory' ? (
            <FolderOutlined style={{ color: '#1890ff' }} />
          ) : (
            <FileOutlined style={{ color: '#52c41a' }} />
          )}
          <span>{node.path.split('/').pop()}</span>
          {node.source && (
            <Tag style={{ marginLeft: 8 }} color="blue">
              {node.source.type === 'operator' ? '算子' : '库'}: {node.source.name}
            </Tag>
          )}
        </span>
        {node.children && node.children.map(child => renderPreviewTree(child, level + 1))}
      </div>
    );
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
        <Tabs activeKey={activeTab} onChange={handleTabChange} defaultActiveKey="operators">
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

          {/* 公共库配置标签页 */}
          <TabPane
            tab={
              <span>
                <ToolOutlined />
                公共库配置 ({commonLibraries.length})
              </span>
            }
            key="libraries"
          >
            <div style={{ marginBottom: 16 }}>
              <Space direction="vertical">
                <Text type="secondary">
                  公共库从算子自动同步，无需手动管理。可在「打包配置」标签页中配置打包路径。
                </Text>
              </Space>
            </div>
            {commonLibraries.length === 0 ? (
              <div style={{ padding: '40px', textAlign: 'center' }}>
                <Text type="secondary">暂无公共库</Text>
              </div>
            ) : (
              <Table
                dataSource={commonLibraries}
                rowKey="libraryId"
                pagination={false}
                columns={[
                  {
                    title: '公共库名称',
                    dataIndex: 'libraryName',
                    key: 'libraryName',
                  },
                  {
                    title: '类型',
                    dataIndex: 'libraryType',
                    key: 'libraryType',
                    width: 100,
                    render: (type: string) => {
                      const typeMap: Record<string, { color: string; label: string }> = {
                        CONSTANT: { color: 'blue', label: '常量' },
                        METHOD: { color: 'green', label: '方法' },
                        MODEL: { color: 'orange', label: '模型' },
                        CUSTOM: { color: 'purple', label: '自定义' },
                      };
                      const config = typeMap[type] || { color: 'default', label: type };
                      return <Tag color={config.color}>{config.label}</Tag>;
                    },
                  },
                  {
                    title: '版本',
                    dataIndex: 'version',
                    key: 'version',
                    width: 80,
                  },
                  {
                    title: '描述',
                    dataIndex: 'description',
                    key: 'description',
                    ellipsis: true,
                  },
                  {
                    title: '关联算子数',
                    dataIndex: 'relatedOperators',
                    key: 'relatedOperators',
                    width: 120,
                    render: (count?: number) => (
                      <Tag color="cyan">{count || 0} 个算子</Tag>
                    ),
                  },
                ]}
              />
            )}
          </TabPane>

          {/* 打包配置标签页 */}
          <TabPane
            tab={
              <span>
                <SettingOutlined />
                打包配置
              </span>
            }
            key="config"
          >
            <Space direction="vertical" style={{ width: '100%' }} size="large">
              {/* 打包模板选择 */}
              <Card size="small" title="打包模板">
                <Row gutter={16} align="middle">
                  <Col span={8}>
                    <Text>当前模板：</Text>
                    <Tag style={{ marginLeft: 8 }} color={pathConfig?.packageTemplate === 'legacy' ? 'blue' : pathConfig?.packageTemplate === 'modern' ? 'green' : 'orange'}>
                      {pathConfig?.packageTemplate?.toUpperCase()}
                    </Tag>
                  </Col>
                  <Col span={16} style={{ textAlign: 'right' }}>
                    <Button icon={<ReloadOutlined />} onClick={() => fetchPreview()}>
                      刷新预览
                    </Button>
                  </Col>
                </Row>
              </Card>

              {/* 打包预览 */}
              <Card size="small" title="打包结构预览">
                {preview ? (
                  <div style={{ maxHeight: 400, overflowY: 'auto', background: '#fafafa', padding: 16 }}>
                    <div style={{ marginBottom: 16 }}>
                      <Text strong>{preview.packageName}/</Text>
                    </div>
                    {preview.structure.map(node => renderPreviewTree(node))}
                    {preview.conflicts && preview.conflicts.length > 0 && (
                      <div style={{ marginTop: 16 }}>
                        <Text type="danger">
                          ⚠️ 检测到 {preview.conflicts.length} 个冲突：
                        </Text>
                        <ul style={{ marginTop: 8 }}>
                          {preview.conflicts.map((conflict, index) => (
                            <li key={index}>
                              <Text type="danger">{conflict.message}</Text>
                            </li>
                          ))}
                        </ul>
                      </div>
                    )}
                    {preview.warnings && preview.warnings.length > 0 && (
                      <div style={{ marginTop: 16 }}>
                        <Text type="warning">
                          ⚠️ 检测到 {preview.warnings.length} 个警告：
                        </Text>
                        <ul style={{ marginTop: 8 }}>
                          {preview.warnings.map((warning, index) => (
                            <li key={index}>
                              <Text type="warning">{warning}</Text>
                            </li>
                          ))}
                        </ul>
                      </div>
                    )}
                  </div>
                ) : (
                  <div style={{ padding: '40px', textAlign: 'center' }}>
                    <Text type="secondary">加载预览中...</Text>
                  </div>
                )}
              </Card>

              {/* 公共库路径配置 */}
              <Collapse
                size="small"
                activeKey={libraryCardCollapsed ? [] : ['library']}
                onChange={(keys) => {
                  setLibraryCardCollapsed(keys.length === 0);
                }}
                items={[
                  {
                    key: 'library',
                    label: (
                      <Space style={{ width: '100%', justifyContent: 'space-between' }}>
                        <span>公共库打包路径</span>
                        <Button
                          type="link"
                          size="small"
                          onClick={(e) => {
                            e.stopPropagation();
                            handleBatchConfig('library');
                          }}
                        >
                          批量配置
                        </Button>
                      </Space>
                    ),
                    children: (
                      <div style={{ marginTop: 8 }}>
                        {pathConfig?.libraryConfigs && pathConfig.libraryConfigs.length > 0 ? (
                          <Table
                            dataSource={pathConfig.libraryConfigs}
                            rowKey="libraryId"
                            pagination={false}
                            size="small"
                            columns={[
                              {
                                title: '公共库',
                                dataIndex: 'libraryName',
                                key: 'libraryName',
                                width: 200,
                              },
                              {
                                title: '类型',
                                dataIndex: 'libraryType',
                                key: 'libraryType',
                                width: 100,
                                render: (type: string) => {
                                  const typeMap: Record<string, { color: string; label: string }> = {
                                    CONSTANT: { color: 'blue', label: '常量' },
                                    METHOD: { color: 'green', label: '方法' },
                                    MODEL: { color: 'orange', label: '模型' },
                                    CUSTOM: { color: 'purple', label: '自定义' },
                                  };
                                  const config = typeMap[type] || { color: 'default', label: type };
                                  return <Tag color={config.color}>{config.label}</Tag>;
                                },
                              },
                              {
                                title: '版本',
                                dataIndex: 'version',
                                key: 'version',
                                width: 80,
                              },
                              {
                                title: '推荐路径',
                                dataIndex: 'recommendedPath',
                                key: 'recommendedPath',
                                ellipsis: true,
                                width: 250,
                                render: (path: string) => <Text ellipsis style={{ color: '#52c41a' }}>{path}</Text>,
                              },
                              {
                                title: '当前路径',
                                dataIndex: 'currentPath',
                                key: 'currentPath',
                                ellipsis: true,
                                render: (path: string, record: LibraryPathConfigResponse) => (
                                  <Space>
                                    <Text
                                      ellipsis
                                      style={{
                                        color: record.useCustomPath ? '#1890ff' : '#52c41a',
                                        maxWidth: 250,
                                      }}
                                    >
                                      {path}
                                    </Text>
                                    {!record.useCustomPath && <Tag color="success">默认</Tag>}
                                    <Button
                                      type="link"
                                      size="small"
                                      icon={<EditOutlined />}
                                      onClick={() => handleEditPathConfig('library', record.libraryId, record.libraryName)}
                                    >
                                      编辑
                                    </Button>
                                  </Space>
                                ),
                              },
                            ]}
                          />
                        ) : (
                          <div style={{ padding: '20px', textAlign: 'center' }}>
                            <Text type="secondary">暂无公共库</Text>
                          </div>
                        )}
                      </div>
                    ),
                  },
                ]}
              />

              {/* 算子路径配置 */}
              <Collapse
                size="small"
                activeKey={operatorCardCollapsed ? [] : ['operator']}
                onChange={(keys) => {
                  setOperatorCardCollapsed(keys.length === 0);
                }}
                items={[
                  {
                    key: 'operator',
                    label: (
                      <Space style={{ width: '100%', justifyContent: 'space-between' }}>
                        <span>算子打包路径</span>
                        <Button
                          type="link"
                          size="small"
                          onClick={(e) => {
                            e.stopPropagation();
                            handleBatchConfig('operator');
                          }}
                        >
                          批量配置
                        </Button>
                      </Space>
                    ),
                    children: (
                      <div style={{ marginTop: 8 }}>
                        {pathConfig?.operatorConfigs && pathConfig.operatorConfigs.length > 0 ? (
                          <Table
                            dataSource={pathConfig.operatorConfigs}
                            rowKey="operatorId"
                            pagination={false}
                            size="small"
                            columns={[
                              {
                                title: '算子',
                                dataIndex: 'operatorName',
                                key: 'operatorName',
                                width: 200,
                              },
                              {
                                title: '推荐路径',
                                dataIndex: 'recommendedPath',
                                key: 'recommendedPath',
                                ellipsis: true,
                                width: 250,
                                render: (path: string) => <Text ellipsis style={{ color: '#52c41a' }}>{path}</Text>,
                              },
                              {
                                title: '当前路径',
                                dataIndex: 'currentPath',
                                key: 'currentPath',
                                ellipsis: true,
                                render: (path: string, record: OperatorPathConfigResponse) => (
                                  <Space>
                                    <Text
                                      ellipsis
                                      style={{
                                        color: record.useCustomPath ? '#1890ff' : '#52c41a',
                                        maxWidth: 250,
                                      }}
                                    >
                                      {path}
                                    </Text>
                                    {!record.useCustomPath && <Tag color="success">默认</Tag>}
                                    <Button
                                      type="link"
                                      size="small"
                                      icon={<EditOutlined />}
                                      onClick={() => handleEditPathConfig('operator', record.operatorId, record.operatorName)}
                                    >
                                      编辑
                                    </Button>
                                  </Space>
                                ),
                              },
                            ]}
                          />
                        ) : (
                          <div style={{ padding: '20px', textAlign: 'center' }}>
                            <Text type="secondary">暂无算子</Text>
                          </div>
                        )}
                      </div>
                    ),
                  },
                ]}
              />
            </Space>
          </TabPane>
        </Tabs>
      </Card>

      {/* 路径编辑弹窗 */}
      <Modal
        title={`编辑路径${currentEditItem ? ` - ${currentEditItem.name}` : ''}`}
        open={pathConfigModalVisible}
        onOk={handleSavePathConfig}
        onCancel={() => {
          setPathConfigModalVisible(false);
          setCurrentEditItem(null);
          pathConfigForm.resetFields();
        }}
        width={600}
      >
        <Form form={pathConfigForm} layout="vertical">
          <Form.Item name="useCustomPath" label="使用自定义路径" valuePropName="checked">
            <Switch />
          </Form.Item>
          <Form.Item
            name="customPackagePath"
            label={
              <span>
                自定义路径（支持变量：$&#123;libraryName&#125;、$&#123;fileName&#125;、$&#123;operatorCode&#125; 等）
                <Text type="secondary" style={{ marginLeft: 8, fontSize: 12 }}>
                  支持的变量：$&#123;libraryName&#125;（库名）、$&#123;fileName&#125;（文件名）、$&#123;operatorCode&#125;（算子编码）、$&#123;packageName&#125;（包名）
                </Text>
              </span>
            }
          >
            <Input placeholder="例如：lib/$&#123;fileName&#125;" />
          </Form.Item>
        </Form>
      </Modal>

      {/* 批量配置弹窗 */}
      <Modal
        title={`批量使用推荐路径${batchConfigType === 'operator' ? '（算子）' : '（公共库）'}`}
        open={batchConfigModalVisible}
        onOk={handleSaveBatchConfig}
        onCancel={() => {
          setBatchConfigModalVisible(false);
          setBatchConfigType(null);
        }}
      >
        <p>
          确定要将所有{batchConfigType === 'operator' ? '算子' : '公共库'}的路径重置为推荐路径吗？
        </p>
      </Modal>

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

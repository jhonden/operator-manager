import { useState, useEffect, useMemo } from 'react';
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
  InputNumber,
  Form,
  Switch,
  Collapse,
  Tree,
  Alert,
} from 'antd';
import {
  ArrowLeftOutlined,
  EditOutlined,
  DeleteOutlined,
  AppstoreOutlined,
  PlusOutlined,
  DownloadOutlined,
  FolderOutlined,
  SettingOutlined,
  ReloadOutlined,
  ToolOutlined,
  FolderOpenOutlined,
  FileTextOutlined,
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
  const [selectedOperatorIds, setSelectedOperatorIds] = useState<number[]>([]);
  const [searchKeyword, setSearchKeyword] = useState<string>('');
  const [languageFilter, setLanguageFilter] = useState<string | undefined>(undefined);
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

  // 执行顺序编辑相关状态
  const [selectedRowKeys, setSelectedRowKeys] = useState<React.Key[]>([]);
  const [selectedOperators, setSelectedOperators] = useState<PackageOperator[]>([]);
  const [editOrderModalVisible, setEditOrderModalVisible] = useState(false);
  const [batchOrderModalVisible, setBatchOrderModalVisible] = useState(false);
  const [currentEditOperator, setCurrentEditOperator] = useState<PackageOperator | null>(null);
  const [orderForm] = Form.useForm();

  // 批量移除算子相关状态
  const [batchRemoveModalVisible, setBatchRemoveModalVisible] = useState(false);
  const [removeForm] = Form.useForm();

  // 折叠状态控制
  const [libraryCardCollapsed, setLibraryCardCollapsed] = useState(false);
  const [operatorCardCollapsed, setOperatorCardCollapsed] = useState(false);

  // 筛选可用算子
  const filteredAvailableOperators = useMemo(() => {
    return availableOperators.filter((op: any) => {
      const matchKeyword = !searchKeyword ||
        op.name.toLowerCase().includes(searchKeyword.toLowerCase());
      const matchLanguage = !languageFilter || op.language === languageFilter;
      return matchKeyword && matchLanguage;
    });
  }, [availableOperators, searchKeyword, languageFilter]);

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

  const handleDownloadPackage = async () => {
    if (!id) return;

    if (!packageData || !packageData.name) {
      return;
    }

    try {
      await packageApi.downloadPackage(Number(id), packageData?.name || 'package');
    } catch (error: any) {
      console.error('[handleDownloadPackage] 下载失败:', error);
      message.error(error.message || '下载算子包失败');
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
    if (!id || selectedOperatorIds.length === 0) {
      message.warning('请选择至少一个算子');
      return;
    }

    setAddOperatorLoading(true);
    try {
      const values = addForm.getFieldsValue();
      const response = await packageApi.batchAddOperators(Number(id), {
        operatorIds: selectedOperatorIds,
        orderIndex: values.orderIndex || 1,
        enabled: true,
      });

      if (response.data) {
        const { successCount, failedCount, failedOperators } = response.data;
        if (failedCount > 0) {
          message.warning(
            `成功添加 ${successCount} 个算子，${failedCount} 个失败`
          );
          console.log('[Package Page] Failed operators:', failedOperators);
        } else {
          message.success(`成功添加 ${successCount} 个算子到包`);
        }
      }

      handleCloseAddModal();
      fetchPackage();
    } catch (error: any) {
      console.error('[Package Page] Batch add operators failed:', error);
      message.error(error.response?.data?.error || error.message || '批量添加算子失败');
    } finally {
      setAddOperatorLoading(false);
    }
  };

  const handleCloseAddModal = () => {
    setAddOperatorModalVisible(false);
    setSelectedOperatorIds([]);
    setSearchKeyword('');
    setLanguageFilter(undefined);
    addForm.resetFields();
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

  // 编辑单个算子的执行顺序
  const handleEditOrderIndex = (operator: PackageOperator) => {
    setCurrentEditOperator(operator);
    orderForm.setFieldsValue({
      orderIndex: operator.orderIndex,
    });
    setEditOrderModalVisible(true);
  };

  const handleSaveOrderIndex = async () => {
    if (!id || !currentEditOperator) return;

    try {
      const values = orderForm.getFieldsValue();
      await packageApi.updatePackageOperator(Number(id), currentEditOperator.id, {
        orderIndex: values.orderIndex,
      });
      message.success('执行顺序更新成功');
      setEditOrderModalVisible(false);
      setCurrentEditOperator(null);
      orderForm.resetFields();
      fetchPackage();
    } catch (error: any) {
      message.error(error.message || '更新执行顺序失败');
    }
  };

  // 批量编辑执行顺序
  const handleBatchOrderIndex = () => {
    if (selectedOperators.length === 0) {
      message.warning('请先选择算子');
      return;
    }
    orderForm.setFieldsValue({
      orderIndex: 1,
    });
    setBatchOrderModalVisible(true);
  };

  const handleSaveBatchOrderIndex = async () => {
    if (!id || selectedOperators.length === 0) return;

    try {
      const values = orderForm.getFieldsValue();
      await packageApi.batchUpdateOperatorOrderIndex(Number(id), {
        orderIndex: values.orderIndex,
        packageOperatorIds: selectedOperators.map(op => op.id),
      });
      message.success(`成功更新 ${selectedOperators.length} 个算子的执行顺序`);
      setBatchOrderModalVisible(false);
      setSelectedRowKeys([]);
      setSelectedOperators([]);
      orderForm.resetFields();
      fetchPackage();
    } catch (error: any) {
      message.error(error.message || '批量更新执行顺序失败');
    }
  };

  // 行选择处理
  const handleRowSelectionChange = (selectedRowKeys: React.Key[], selectedRows: PackageOperator[]) => {
    setSelectedRowKeys(selectedRowKeys);
    setSelectedOperators(selectedRows);
  };

  // 批量移除算子
  const handleBatchRemove = async () => {
    if (!id || selectedRowKeys.length === 0) {
      message.warning('请选择至少一个算子');
      return;
    }

    setBatchRemoveModalVisible(false);
    removeForm.resetFields();
  };

  const handleConfirmBatchRemove = async () => {
    if (!id || selectedRowKeys.length === 0) return;

    const values = removeForm.getFieldsValue();
    const reason = values.reason || '批量移除';

    try {
      await packageApi.batchRemoveOperators(
        Number(id),
        selectedRowKeys as number[],
        reason
      );

      message.success(`成功移除 ${selectedRowKeys.length} 个算子`);

      // 清空选择
      setSelectedRowKeys([]);
      setSelectedOperators([]);
      setBatchRemoveModalVisible(false);
      removeForm.resetFields();

      // 刷新算子列表
      fetchPackage();
    } catch (error: any) {
      console.error('[Package Page] Batch remove operators failed:', error);
      message.error(error.response?.data?.error || error.message || '批量移除算子失败');
    }
  };

  const handleCancelBatchRemove = () => {
    setBatchRemoveModalVisible(false);
    removeForm.resetFields();
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

  // 将 PackagePreviewTreeNode 转换为 Tree 组件的数据格式
  const convertToTreeData = (nodes: PackagePreviewTreeNode[]): any[] => {
    return nodes.map(node => {
      const fileName = node.path.split('/').pop() || node.path;

      return {
        title: (
          <Space style={{ flex: 1 }}>
            {node.source && (
              <Tag style={{ marginLeft: 0, fontSize: 11 }} color={
                node.source.type === 'operator' ? 'blue' :
                node.source.type === 'metadata' ? 'orange' :
                'purple'
              }>
                {node.source.type === 'operator' ? '算子' :
                 node.source.type === 'metadata' ? '元' :
                 '库'}
              </Tag>
            )}
            <span style={{ fontSize: 13 }}>{fileName}</span>
          </Space>
        ),
        key: node.path,
        icon: node.type === 'directory'
          ? <FolderOutlined style={{ color: '#1890ff' }} />
          : <FileTextOutlined style={{ color: '#52c41a', fontSize: 12 }} />,
        children: node.children ? convertToTreeData(node.children) : undefined,
      };
    });
  };

  // 自定义 Tree 节点的图标渲染
  const getIcon = (node: any) => {
    if (!node) return <FileTextOutlined />;

    if (node.expanded) {
      return <FolderOpenOutlined style={{ color: '#1890ff' }} />;
    }

    return node.icon || <FileTextOutlined style={{ color: '#52c41a', fontSize: 12 }} />;
  };

  if (!packageData) {
    return <div>{t('common.loading')}</div>;
  }

  const operatorColumns = [
    {
      title: '执行顺序',
      dataIndex: 'orderIndex',
      key: 'orderIndex',
      width: 100,
      render: (order: number, record: PackageOperator) => (
        <Button
          type="link"
          size="small"
          onClick={() => handleEditOrderIndex(record)}
        >
          {order}
        </Button>
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
      width: 120,
      render: (_: any, record: PackageOperator) => (
        <Space size="small">
          <Popconfirm
            title="确定要移除此算子吗？"
            onConfirm={() => handleRemoveOperator(record.id)}
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
              <Button
                onClick={handleBatchOrderIndex}
                disabled={selectedOperators.length === 0}
              >
                批量设置顺序
              </Button>
              <Button
                danger
                disabled={selectedRowKeys.length === 0}
                onClick={() => setBatchRemoveModalVisible(true)}
              >
                批量移除（{selectedRowKeys.length}）
              </Button>
            </div>
            <Table
              columns={operatorColumns}
              dataSource={operators}
              rowKey="id"
              pagination={false}
              rowSelection={{
                selectedRowKeys,
                onChange: handleRowSelectionChange,
              }}
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
                    <Space>
                      <Button icon={<ReloadOutlined />} onClick={() => fetchPreview()}>
                        刷新预览
                      </Button>
                      <Button
                        type="primary"
                        icon={<DownloadOutlined />}
                        onClick={handleDownloadPackage}
                      >
                        打包下载
                      </Button>
                    </Space>
                  </Col>
                </Row>
              </Card>

              {/* 打包预览 */}
              <Card size="small" title="打包结构预览">
                {preview ? (
                  <div>
                    {/* 冲突提示 */}
                    {preview.conflicts && preview.conflicts.length > 0 && (
                      <Alert
                        type="error"
                        message={`检测到 ${preview.conflicts.length} 个冲突`}
                        description={
                          <ul style={{ marginTop: 8, marginBottom: 0, paddingLeft: 20 }}>
                            {preview.conflicts.map((conflict, index) => (
                              <li key={index}>{conflict.message}</li>
                            ))}
                          </ul>
                        }
                        style={{ marginBottom: 16 }}
                        showIcon
                      />
                    )}

                    {/* 警告提示 */}
                    {preview.warnings && preview.warnings.length > 0 && (
                      <Alert
                        type="warning"
                        message={`检测到 ${preview.warnings.length} 个警告`}
                        description={
                          <ul style={{ marginTop: 8, marginBottom: 0, paddingLeft: 20 }}>
                            {preview.warnings.map((warning, index) => (
                              <li key={index}>{warning}</li>
                            ))}
                          </ul>
                        }
                        style={{ marginBottom: 16 }}
                        showIcon
                      />
                    )}

                    {/* 文件树 */}
                    <div style={{
                      maxHeight: 400,
                      overflowY: 'auto',
                      background: '#fafafa',
                      padding: 16,
                      borderRadius: 4,
                      border: '1px solid #f0f0f0'
                    }}>
                      <Tree
                        showIcon
                        defaultExpandAll
                        icon={getIcon}
                        treeData={convertToTreeData(preview.structure)}
                        style={{ background: 'transparent' }}
                      />
                    </div>
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
                          disabled={pathConfig?.packageTemplate === 'legacy'}
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
                                      disabled={pathConfig?.packageTemplate === 'legacy'}
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
                          disabled={pathConfig?.packageTemplate === 'legacy'}
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
                                      disabled={pathConfig?.packageTemplate === 'legacy'}
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
        onCancel={handleCloseAddModal}
        confirmLoading={addOperatorLoading}
        width={800}
        footer={[
          <Button key="cancel" onClick={handleCloseAddModal}>取消</Button>,
          <Button
            key="submit"
            type="primary"
            loading={addOperatorLoading}
            onClick={handleAddOperator}
            disabled={selectedOperatorIds.length === 0}
          >
            确认添加（{selectedOperatorIds.length}）
          </Button>,
        ]}
      >
        <Form form={addForm} layout="vertical">
          {/* 执行顺序 - 顶部固定 */}
          <Form.Item
            label="执行顺序"
            name="orderIndex"
            rules={[{ required: true, message: '请输入执行顺序' }, { type: 'number', min: 1, message: '执行顺序必须大于等于1' }]}
            initialValue={1}
          >
            <InputNumber
              min={1}
              placeholder="输入执行顺序（1-N，相同顺序可并行执行）"
              style={{ width: '100%' }}
            />
          </Form.Item>

          {/* 已选数量 - 顶部固定 */}
          <div style={{ marginBottom: 16 }}>
            <Text type="secondary">已选择 {selectedOperatorIds.length} 个算子</Text>
          </div>

          {/* 搜索和筛选 */}
          <Space direction="vertical" style={{ width: '100%' }} size="middle">
            <Input.Search
              placeholder="搜索算子名称"
              allowClear
              value={searchKeyword}
              onChange={(e) => setSearchKeyword(e.target.value)}
            />
            <Select
              placeholder="筛选编程语言"
              allowClear
              style={{ width: 200 }}
              value={languageFilter}
              onChange={(value) => setLanguageFilter(value)}
            >
              <Select.Option value="JAVA">Java</Select.Option>
              <Select.Option value="GROOVY">Groovy</Select.Option>
            </Select>
          </Space>

          {/* 算子列表 - 可滚动区域 */}
          <div style={{ marginTop: 16 }}>
            <Table
              columns={[
                {
                  title: '算子名称',
                  dataIndex: 'name',
                  key: 'name',
                },
                {
                  title: '编程语言',
                  dataIndex: 'language',
                  key: 'language',
                  width: 100,
                  render: (language: string) => {
                    const color = language === 'JAVA' ? 'blue' : 'green';
                    return <Tag color={color}>{language}</Tag>;
                  },
                },
                {
                  title: '描述',
                  dataIndex: 'description',
                  key: 'description',
                  ellipsis: true,
                },
              ]}
              dataSource={filteredAvailableOperators}
              rowKey="id"
              rowSelection={{
                selectedRowKeys: selectedOperatorIds,
                onChange: (selectedKeys) => setSelectedOperatorIds(selectedKeys as number[]),
              }}
              pagination={false}
              scroll={{ y: 300 }}
              size="small"
            />
          </div>
        </Form>
      </Modal>

      {/* 批量移除算子弹窗 */}
      <Modal
        title="批量移除算子"
        open={batchRemoveModalVisible}
        onOk={handleConfirmBatchRemove}
        onCancel={handleCancelBatchRemove}
        okText="确认移除"
        cancelText="取消"
        width={500}
      >
        <div>
          <p>确定要批量移除选中的 {selectedRowKeys.length} 个算子吗？</p>
          {selectedOperators.length > 0 && (
            <div style={{ marginTop: 12 }}>
              <Text type="secondary">即将移除的算子：</Text>
              <ul style={{ marginTop: 8, paddingLeft: 20 }}>
                {selectedOperators.map((op) => (
                  <li key={op.id}>
                    {op.operatorName} ({op.operatorLanguage})
                  </li>
                ))}
              </ul>
            </div>
          )}
        </div>
      </Modal>

      {/* 编辑执行顺序弹窗 */}
      <Modal
        title="编辑执行顺序"
        open={editOrderModalVisible}
        onOk={handleSaveOrderIndex}
        onCancel={() => {
          setEditOrderModalVisible(false);
          setCurrentEditOperator(null);
          orderForm.resetFields();
        }}
        width={500}
      >
        <Form form={orderForm} layout="vertical">
          <Form.Item
            label="执行顺序"
            name="orderIndex"
            rules={[{ required: true, message: '请输入执行顺序' }, { type: 'number', min: 1, message: '执行顺序必须大于等于1' }]}
          >
            <InputNumber
              type="number"
              min={1}
              placeholder="输入执行顺序（1-N，相同顺序可并行执行）"
              style={{ width: '100%' }}
            />
          </Form.Item>
          <Form.Item>
            <Text type="secondary" style={{ fontSize: 12 }}>
              相同执行顺序的算子将并行执行，不同执行顺序的算子按顺序依次执行
            </Text>
          </Form.Item>
        </Form>
      </Modal>

      {/* 批量设置顺序弹窗 */}
      <Modal
        title={`批量设置执行顺序（${selectedOperators.length} 个算子）`}
        open={batchOrderModalVisible}
        onOk={handleSaveBatchOrderIndex}
        onCancel={() => {
          setBatchOrderModalVisible(false);
          orderForm.resetFields();
        }}
        width={500}
      >
        <Form form={orderForm} layout="vertical">
          <Form.Item
            label="执行顺序"
            name="orderIndex"
            rules={[{ required: true, message: '请输入执行顺序' }, { type: 'number', min: 1, message: '执行顺序必须大于等于1' }]}
            initialValue={1}
          >
            <InputNumber
              type="number"
              min={1}
              placeholder="输入执行顺序（1-N，相同顺序可并行执行）"
              style={{ width: '100%' }}
            />
          </Form.Item>
          <Form.Item>
            <Text type="secondary" style={{ fontSize: 12 }}>
              将选中的 {selectedOperators.length} 个算子设置为相同的执行顺序
            </Text>
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
};

export default PackageDetailPage;

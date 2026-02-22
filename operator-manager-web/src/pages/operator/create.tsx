import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import {
  Form,
  Input,
  Button,
  Card,
  Space,
  message,
  Steps,
  Tabs,
  Tag,
  Select,
  Table,
  Popconfirm,
  Checkbox,
} from 'antd';
import { ArrowLeftOutlined, SaveOutlined, SendOutlined, PlusOutlined, DeleteOutlined } from '@ant-design/icons';
import CodeEditor from '@/components/code/CodeEditor';
import ParameterForm from '@/components/operator/ParameterForm';
import BusinessLogicEditor from '@/components/editor/BusinessLogicEditor';
import LibrarySelectorModal from '@/components/library/LibrarySelectorModal';
import { operatorApi } from '@/api/operator';
import type { LibraryDependencyResponse } from '@/types/library';
import type { LibraryResponse, LibraryType } from '@/types/library';
import { DataFormatOptions, GeneratorOptions } from '@/types';
import { t } from '@/utils/i18n';

const { Step } = Steps;
const { TabPane } = Tabs;
const { Option } = Select;
const { TextArea } = Input;
const { Group: CheckboxGroup } = Checkbox;

/**
 * Operator create/edit page
 */
const OperatorCreatePage: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const [form] = Form.useForm();
  const [loading, setLoading] = useState(false);
  const [currentStep, setCurrentStep] = useState(0);
  const [code, setCode] = useState('');
  const [language, setLanguage] = useState<'java' | 'groovy'>('java');

  // 公共库依赖状态
  const [libraries, setLibraries] = useState<LibraryDependencyResponse[]>([]);
  const [libraryModalVisible, setLibraryModalVisible] = useState(false);
  const [librariesLoading, setLibrariesLoading] = useState(false);

  const isEdit = !!id;

  // 选择公共库 Modal 回调
  const handleLibrarySelect = async (library: LibraryResponse) => {
    if (!id) {
      message.error('无法添加公共库依赖：算子ID不存在');
      return;
    }

    try {
      console.log('[Operator Page] Adding library dependency, operatorId:', id, 'libraryId:', library.id);
      const response = await operatorApi.addLibraryDependency(Number(id), {
        libraryId: library.id
      });

      if (response.success) {
        message.success('公共库依赖添加成功');
        await fetchOperatorLibraries();
      }
    } catch (error: any) {
      console.error('[Operator Page] Error adding library dependency:', error);
      message.error(error.message || '添加公共库依赖失败');
    }
  };

  // 关闭选择公共库 Modal
  const handleLibraryModalCancel = () => {
    setLibraryModalVisible(false);
  };

  useEffect(() => {
    if (id) {
      fetchOperator();
      fetchOperatorLibraries();
    }
  }, [id]);

  const fetchOperator = async () => {
    setLoading(true);
    try {
      const response = await operatorApi.getOperator(Number(id));
      if (response.data) {
        const op = response.data;
        setLanguage(op.language.toLowerCase() as 'java' | 'groovy');
        setCode(op.code || '');

        // Split parameters by ioType (backend uses ioType, frontend uses direction)
        const inputParams = (op.parameters || [])
          .filter((p) => p.ioType === 'INPUT' || p.direction === 'INPUT')
          .map((p: any) => ({
            ...p,
            direction: 'INPUT',
            required: p.isRequired ?? p.required,
            type: p.parameterType || p.type,
          }));
        const outputParams = (op.parameters || [])
          .filter((p) => p.ioType === 'OUTPUT' || p.direction === 'OUTPUT')
          .map((p: any) => ({
            ...p,
            direction: 'OUTPUT',
            required: p.isRequired ?? p.required,
            type: p.parameterType || p.type,
          }));

        form.setFieldsValue({
          name: op.name,
          description: op.description,
          language: op.language,
          status: op.status,
          version: op.version,
          operatorCode: op.operatorCode,
          objectCode: op.objectCode,
          dataFormat: op.dataFormat ? op.dataFormat.split(',') : [],
          generator: op.generator,
          businessLogic: op.businessLogic,
          inputParameters: inputParams,
          outputParameters: outputParams,
        });

        console.log('[Operator Page] Form values set, businessLogic:', form.getFieldsValue(['businessLogic']));
      }
    } catch (error: any) {
      message.error(error.message || '获取算子失败');
    } finally {
      setLoading(false);
    }
  };

  // 获取算子依赖的公共库列表
  const fetchOperatorLibraries = async () => {
    setLibrariesLoading(true);
    try {
      console.log('[Operator Page] Fetching operator libraries for operatorId:', id);
      const response = await operatorApi.getOperatorLibraries(Number(id));
      console.log('[Operator Page] Fetch operator libraries response:', response);

      if (response.data) {
        console.log('[Operator Page] Operator libraries fetched:', response.data);
        setLibraries(response.data);
      }
    } catch (error: any) {
      console.error('[Operator Page] Error fetching operator libraries:', error);
      message.error(error.message || '获取公共库列表失败');
    } finally {
      setLibrariesLoading(false);
    }
  };

  // 移除公共库依赖
  const handleRemoveLibrary = async (libraryId: number) => {
    try {
      console.log('[Operator Page] Removing library dependency, operatorId:', id, 'libraryId:', libraryId);
      const response = await operatorApi.removeLibraryDependency(Number(id), libraryId);

      if (response.success) {
        message.success('公共库依赖移除成功');
        await fetchOperatorLibraries();
      }
    } catch (error: any) {
      console.error('[Operator Page] Error removing library dependency:', error);
      message.error(error.message || '移除公共库依赖失败');
    }
  };

  const handleSubmit = async (publish = false) => {
    try {
      // For publish: get all fields; for draft: only validate current step
      let values;
      if (publish) {
        // Publish：获取所有字段（包括未填写的）
        values = form.getFieldsValue(true); // true to get all fields including undefined
      } else {
        // Save Draft：只验证当前步骤的必填字段
        values = await form.validateFields();
      }

      console.log('[Operator Page] Form values:', values);
      console.log('[Operator Page] Code:', code);
      console.log('[Operator Page] Business logic from form:', values.businessLogic);

      // Convert dataFormat array to comma-separated string
      const dataFormat = Array.isArray(values.dataFormat)
        ? values.dataFormat.join(',')
        : values.dataFormat;

      // Merge inputParameters and outputParameters into a single parameters array
      const inputParams = (values.inputParameters || []).map((p: any) => ({
        ...p,
        ioType: 'INPUT',
        isRequired: p.required ?? false,
        parameterType: p.type ?? p.parameterType,
      }));
      const outputParams = (values.outputParameters || []).map((p: any) => ({
        ...p,
        ioType: 'OUTPUT',
        isRequired: p.required ?? false,
        parameterType: p.type ?? p.parameterType,
      }));

      const operatorData = {
        ...values,
        parameters: [...inputParams, ...outputParams],
        code,
        dataFormat,
        status: publish ? 'PUBLISHED' : 'DRAFT',
      };

      // Remove inputParameters and outputParameters from data sent to backend
      delete operatorData.inputParameters;
      delete operatorData.outputParameters;

      // Ensure businessLogic is properly handled
      if (operatorData.businessLogic === undefined || operatorData.businessLogic === null) {
        operatorData.businessLogic = null; // Explicitly set to null if undefined
      }

      console.log('[Operator Page] Operator data to send:', operatorData);
      console.log('[Operator Page] Business logic in operatorData:', operatorData.businessLogic);

      if (isEdit) {
        await operatorApi.updateOperator(Number(id), operatorData);
        message.success('算子更新成功');
      } else {
        const response = await operatorApi.createOperator(operatorData);
        message.success('算子创建成功');
        if (response.data) {
          navigate(`/operators/${response.data.id}`);
        }
      }
    } catch (error: any) {
      console.error('[Operator Page] Error saving operator:', error);
      message.error(error.message || '保存算子失败');
    }
  };

  const steps = [
    {
      title: '基本信息',
      description: '名称、描述、语言',
    },
    {
      title: '业务逻辑',
      description: 'Markdown 业务规则',
    },
    {
      title: '参数',
      description: '输入和输出参数',
    },
    {
      title: '代码',
      description: '实现代码',
    },
    {
      title: '公共库依赖',
      description: '选择依赖的公共库',
    },
  ];

  return (
    <div style={{ padding: '24px' }}>
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
            <span>{isEdit ? '编辑算子' : '创建算子'}</span>
          </Space>
        }
        extra={
          <Space>
            <Button
              icon={<SaveOutlined />}
              onClick={() => handleSubmit(false)}
              loading={loading}
            >
              {t('common.saveDraft')}
            </Button>
            <Button
              type="primary"
              icon={<SendOutlined />}
              onClick={() => handleSubmit(true)}
              loading={loading}
            >
              {isEdit ? t('common.updateAndPublish') : t('common.createAndPublish')}
            </Button>
          </Space>
        }
      >
        <Steps
          current={currentStep}
          onChange={(current) => setCurrentStep(current)}
          style={{ marginBottom: 32 }}
        >
          {steps.map((step, index) => (
            <Step
              key={index}
              title={step.title}
              description={step.description}
            />
          ))}
        </Steps>
        <Form
          form={form}
          layout="vertical"
          initialValues={{
            language: 'JAVA',
            status: 'DRAFT',
          }}
        >
          {/* Step 1: Basic Info */}
          {currentStep === 0 && (
            <Card title="基本信息" style={{ marginBottom: 16 }}>
              <Form.Item
                label="Operator Name"
                name="name"
                rules={[
                  { required: true, message: 'Please input operator name' },
                  { min: 3, message: 'Name must be at least 3 characters' },
                  ]}
              >
                <Input placeholder="e.g., Data Cleaning Operator" />
              </Form.Item>
              <Form.Item
                label="Description"
                name="description"
                rules={[{ required: true, message: 'Please input description' }]}
              >
                <TextArea
                  rows={4}
                  placeholder="Describe what this operator does..."
                />
              </Form.Item>
              <Form.Item
                label="Operator Code"
                name="operatorCode"
                rules={[
                  { required: true, message: 'Please input operator code' },
                  {
                    pattern: /^[a-zA-Z_][a-zA-Z0-9_]{0,63}$/,
                    message: 'Operator code must start with a letter or underscore, followed by letters, numbers, or underscores, 1-64 characters',
                  },
                ]}
                tooltip="Unique identifier for the operator. Must start with a letter or underscore, followed by letters, numbers, or underscores."
              >
                <Input placeholder="e.g., data_cleaning_operator" />
              </Form.Item>
              <Form.Item
                label="Object Code"
                name="objectCode"
                rules={[
                  { required: true, message: 'Please input object code' },
                  {
                    pattern: /^[a-zA-Z_][a-zA-Z0-9_]{0,63}$/,
                    message: 'Object code must start with a letter or underscore, followed by letters, numbers, or underscores, 1-64 characters',
                  },
                ]}
                tooltip="Code of the data object that this operator outputs. Same format as operator code."
              >
                <Input placeholder="e.g., cleaned_data" />
              </Form.Item>
              <Form.Item
                label="Data Format"
                name="dataFormat"
                rules={[{ required: true, message: 'Please select at least one data format' }]}
                tooltip="Selects raw data formats this operator can process"
              >
                <CheckboxGroup options={DataFormatOptions} />
              </Form.Item>
              <Form.Item
                label="Generator"
                name="generator"
                tooltip="Specify if this operator is dynamically generated or statically built-in"
              >
                <Select placeholder="Select generator type">
                  {GeneratorOptions.map((opt) => (
                    <Option key={opt.value} value={opt.value}>
                      {opt.label}
                    </Option>
                  ))}
                </Select>
              </Form.Item>
              <Form.Item
                label="Programming Language"
                name="language"
                rules={[{ required: true, message: 'Please select language' }]}
              >
                <Select
                  onChange={(value) =>
                    setLanguage(value.toLowerCase() as 'java' | 'groovy')
                  }
                >
                  <Option value="JAVA">Java</Option>
                  <Option value="GROOVY">Groovy</Option>
                </Select>
              </Form.Item>
              <Form.Item
                label="Version"
                name="version"
                rules={[{ required: true, message: 'Please input version' }]}
                initialValue="1.0.0"
              >
                <Input placeholder="e.g., 1.0.0" />
              </Form.Item>
            </Card>
          )}
          {/* Step 2: Business Logic */}
          {currentStep === 1 && (
            <Card title="业务逻辑" style={{ marginBottom: 16 }}>
              <Form.Item
                name="businessLogic"
                tooltip="使用 Markdown 描述算子的业务规则，支持 Mermaid 绘制流程图、序列图、甘特图等"
              >
                <BusinessLogicEditor />
              </Form.Item>
            </Card>
          )}
          {/* Step 3: Parameters */}
          {currentStep === 2 && (
            <Tabs defaultActiveKey="input">
              <TabPane tab={`${t('parameter.input')} Parameters`} key="input">
                <ParameterForm direction="INPUT" />
              </TabPane>
              <TabPane tab={`${t('parameter.output')} Parameters`} key="output">
                <ParameterForm direction="OUTPUT" />
              </TabPane>
            </Tabs>
          )}
          {/* Step 4: Code */}
          {currentStep === 3 && (
            <Card title={t('operator.code')}>
              <div style={{ marginBottom: 16 }}>
                <Tag color={language === 'java' ? 'blue' : 'green'}>
                  {language.toUpperCase()}
                </Tag>
                <span style={{ marginLeft: 8 }}>
                  编写您的 {language === 'java' ? 'Java' : 'Groovy'} 代码
                </span>
              </div>
              <CodeEditor
                language={language}
                value={code}
                onChange={setCode}
                height={600}
              />
            </Card>
          )}
          {/* Step 5: 公共库依赖 */}
          {currentStep === 4 && (
            <Card
              title={
                <Space>
                  <span>公共库依赖</span>
                  <Button
                    type="primary"
                    icon={<PlusOutlined />}
                    onClick={() => setLibraryModalVisible(true)}
                  >
                    选择公共库
                  </Button>
                </Space>
              }
              style={{ marginBottom: 16 }}
            >
              <Table
                loading={librariesLoading}
                rowKey="id"
                columns={[
                  {
                    title: 'ID',
                    dataIndex: 'id',
                    key: 'id',
                    width: 80,
                  },
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
                    title: '版本',
                    dataIndex: 'version',
                    key: 'version',
                    width: 100,
                  },
                  {
                    title: '描述',
                    dataIndex: 'libraryDescription',
                    key: 'libraryDescription',
                    width: 300,
                    ellipsis: true,
                  },
                  {
                    title: '文件数',
                    dataIndex: 'fileCount',
                    key: 'fileCount',
                    width: 100,
                  },
                  {
                    title: '添加时间',
                    dataIndex: 'createdAt',
                    key: 'createdAt',
                    width: 180,
                  },
                  {
                    title: '操作',
                    key: 'action',
                    width: 120,
                    fixed: 'right',
                    render: (_, record: LibraryDependencyResponse) => (
                      <Space>
                        <Popconfirm
                          title="确认删除"
                          description="确定要移除该公共库依赖吗？"
                          onConfirm={() => handleRemoveLibrary(record.libraryId)}
                          okText="确定"
                          cancelText="取消"
                        >
                          <Button
                            type="link"
                            danger
                            icon={<DeleteOutlined />}
                          >
                            移除
                          </Button>
                        </Popconfirm>
                      </Space>
                    ),
                  },
                ]}
                dataSource={libraries}
                pagination={false}
              />
            </Card>
          )}
          {/* Navigation Buttons */}
          <div style={{ marginTop: 24, textAlign: 'right' }}>
            <Space>
              {currentStep > 0 && (
                <Button onClick={() => setCurrentStep(currentStep - 1)}>
                  {t('common.previous')}
                </Button>
              )}
              {currentStep < steps.length - 1 && currentStep !== 4 && (
                <Button
                  type="primary"
                  onClick={() => setCurrentStep(currentStep + 1)}
                >
                  {t('common.next')}
                </Button>
              )}
            </Space>
          </div>
        </Form>
      </Card>

      {/* 公共库选择弹窗 */}
      <LibrarySelectorModal
        visible={libraryModalVisible}
        onCancel={handleLibraryModalCancel}
        onLibrarySelect={handleLibrarySelect}
      />
    </div>
  );
};

export default OperatorCreatePage;

import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import {
  Form,
  Input,
  Button,
  Card,
  Select,
  Space,
  message,
  Steps,
  Tabs,
  Tag,
} from 'antd';
import { ArrowLeftOutlined, SaveOutlined, SendOutlined } from '@ant-design/icons';
import CodeEditor from '@/components/code/CodeEditor';
import ParameterForm from '@/components/operator/ParameterForm';
import { operatorApi } from '@/api/operator';

const { Step } = Steps;
const { TabPane } = Tabs;
const { Option } = Select;
const { TextArea } = Input;

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

  const isEdit = !!id;

  useEffect(() => {
    if (id) {
      fetchOperator();
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
          inputParameters: inputParams,
          outputParameters: outputParams,
        });
      }
    } catch (error: any) {
      message.error(error.message || 'Failed to fetch operator');
    } finally {
      setLoading(false);
    }
  };

  const handleSubmit = async (publish = false) => {
    try {
      // For draft, only get filled fields; for publish, validate all required fields
      let values;
      if (publish) {
        values = await form.validateFields();
      } else {
        // Get all fields without validation
        values = form.getFieldsValue(true); // true to get all fields including undefined
      }

      console.log('Form values:', values);
      console.log('Code:', code);

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
        status: publish ? 'PUBLISHED' : 'DRAFT',
      };

      // Remove inputParameters and outputParameters from the data sent to backend
      delete operatorData.inputParameters;
      delete operatorData.outputParameters;

      console.log('Operator data to send:', operatorData);

      if (isEdit) {
        await operatorApi.updateOperator(Number(id), operatorData);
        message.success('Operator updated successfully');
      } else {
        const response = await operatorApi.createOperator(operatorData);
        message.success('Operator created successfully');
        if (response.data) {
          navigate(`/operators/${response.data.id}`);
        }
      }
    } catch (error: any) {
      console.error('Error saving operator:', error);
      message.error(error.message || 'Failed to save operator');
    }
  };

  const steps = [
    {
      title: 'Basic Info',
      description: 'Name, description, language',
    },
    {
      title: 'Parameters',
      description: 'Input and output parameters',
    },
    {
      title: 'Code',
      description: 'Implementation code',
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
              Back
            </Button>
            <span>{isEdit ? 'Edit Operator' : 'Create Operator'}</span>
          </Space>
        }
        extra={
          <Space>
            <Button
              icon={<SaveOutlined />}
              onClick={() => handleSubmit(false)}
              loading={loading}
            >
              Save Draft
            </Button>
            <Button
              type="primary"
              icon={<SendOutlined />}
              onClick={() => handleSubmit(true)}
              loading={loading}
            >
              {isEdit ? 'Update & Publish' : 'Create & Publish'}
            </Button>
          </Space>
        }
      >
        <Steps current={currentStep} style={{ marginBottom: 32 }}>
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
            <Card title="Basic Information" style={{ marginBottom: 16 }}>
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

          {/* Step 2: Parameters */}
          {currentStep === 1 && (
            <Tabs defaultActiveKey="input">
              <TabPane tab="Input Parameters" key="input">
                <ParameterForm direction="INPUT" />
              </TabPane>
              <TabPane tab="Output Parameters" key="output">
                <ParameterForm direction="OUTPUT" />
              </TabPane>
            </Tabs>
          )}

          {/* Step 3: Code */}
          {currentStep === 2 && (
            <Card title="Implementation Code">
              <div style={{ marginBottom: 16 }}>
                <Tag color={language === 'java' ? 'blue' : 'green'}>
                  {language.toUpperCase()}
                </Tag>
                <span style={{ marginLeft: 8 }}>
                  Write your {language === 'java' ? 'Java' : 'Groovy'} code here
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

          {/* Navigation Buttons */}
          <div style={{ marginTop: 24, textAlign: 'right' }}>
            <Space>
              {currentStep > 0 && (
                <Button onClick={() => setCurrentStep(currentStep - 1)}>
                  Previous
                </Button>
              )}
              {currentStep < steps.length - 1 && (
                <Button
                  type="primary"
                  onClick={() => setCurrentStep(currentStep + 1)}
                >
                  Next
                </Button>
              )}
            </Space>
          </div>
        </Form>
      </Card>
    </div>
  );
};

export default OperatorCreatePage;

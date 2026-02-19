import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import {
  Form,
  Input,
  Button,
  Card,
  Space,
  message,
  Row,
  Col,
  Switch,
} from 'antd';
import {
  ArrowLeftOutlined,
  SaveOutlined,
  SendOutlined,
} from '@ant-design/icons';
import type { OperatorPackage } from '@/types';
import { packageApi } from '@/api/package';

/**
 * Operator package create/edit page
 */
const PackageCreatePage: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const [form] = Form.useForm();
  const [loading, setLoading] = useState(false);

  const isEdit = !!id;

  useEffect(() => {
    if (id) {
      fetchPackage();
    }
  }, [id]);

  const fetchPackage = async () => {
    setLoading(true);
    try {
      const response = await packageApi.getPackage(Number(id));
      if (response.data) {
        const pkg = response.data;
        setPackageData(pkg);

        form.setFieldsValue({
          name: pkg.name,
          description: pkg.description,
          businessScenario: pkg.businessScenario,
          icon: pkg.icon,
          status: pkg.status,
          version: pkg.version,
          isPublic: pkg.isPublic,
        });
      }
    } catch (error: any) {
      message.error(error.message || 'Failed to fetch package');
    } finally {
      setLoading(false);
    }
  };

  const handleSubmit = async (publish = false) => {
    try {
      const values = await form.validateFields();

      const packageRequest = {
        ...values,
        status: publish ? 'PUBLISHED' : 'DRAFT',
      };

      if (isEdit) {
        await packageApi.updatePackage(Number(id), packageRequest);
        message.success('Package updated successfully');
      } else {
        const response = await packageApi.createPackage(packageRequest);
        message.success('Package created successfully');
        if (response.data) {
          navigate(`/packages/${response.data.id}`);
        }
      }
    } catch (error: any) {
      message.error(error.message || 'Failed to save package');
    }
  };

  return (
    <div style={{ padding: '24px' }}>
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
            <span>{isEdit ? 'Edit Package' : 'Create Package'}</span>
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
        <Row gutter={24}>
          <Col span={16} offset={4}>
            <Form
              form={form}
              layout="vertical"
              initialValues={{
                status: 'DRAFT',
                isPublic: false,
              }}
            >
              <Form.Item
                label="Package Name"
                name="name"
                rules={[
                  { required: true, message: 'Please input package name' },
                  { min: 3, message: 'Name must be at least 3 characters' },
                ]}
              >
                <Input placeholder="e.g., Data ETL Package" />
              </Form.Item>

              <Form.Item
                label="Business Scenario"
                name="businessScenario"
                rules={[
                  { required: true, message: 'Please describe business scenario' },
                ]}
              >
                <Input placeholder="e.g., Complete data warehouse ETL workflow" />
              </Form.Item>

              <Form.Item
                label="Description"
                name="description"
                rules={[{ required: true, message: 'Please input description' }]}
              >
                <Input.TextArea
                  rows={4}
                  placeholder="Describe what this package does and business problem it solves..."
                />
              </Form.Item>

              <Form.Item label="Icon URL" name="icon">
                <Input placeholder="https://example.com/icon.png" />
              </Form.Item>

              <Form.Item
                label="Version"
                name="version"
                rules={[{ required: true, message: 'Please input version' }]}
                initialValue="1.0.0"
              >
                <Input placeholder="e.g., 1.0.0" />
              </Form.Item>

              <Form.Item
                label="Public"
                name="isPublic"
                valuePropName="checked"
              >
                <Switch />
              </Form.Item>
            </Form>
          </Col>
        </Row>
      </Card>
    </div>
  );
};

export default PackageCreatePage;

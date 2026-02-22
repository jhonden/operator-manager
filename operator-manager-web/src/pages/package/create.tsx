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
import { t } from '@/utils/i18n';

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
      message.error(error.message || '获取算子包失败');
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
        message.success('算子包更新成功');
      } else {
        const response = await packageApi.createPackage(packageRequest);
        message.success('算子包创建成功');
        if (response.data) {
          navigate(`/packages/${response.data.id}`);
        }
      }
    } catch (error: any) {
      message.error(error.message || '保存算子包失败');
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
              {t('common.back')}
            </Button>
            <span>{isEdit ? '编辑算子包' : '创建算子包'}</span>
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
              {isEdit ? '更新并发布' : '创建并发布'}
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
                label="算子包名称"
                name="name"
                rules={[
                  { required: true, message: '请输入算子包名称' },
                  { min: 3, message: '名称至少 3 个字符' },
                ]}
              >
                <Input placeholder="例如：数据 ETL 包" />
              </Form.Item>

              <Form.Item
                label="业务场景"
                name="businessScenario"
                rules={[
                  { required: true, message: '请描述业务场景' },
                ]}
              >
                <Input placeholder="例如：完整的数据仓库 ETL 工作流" />
              </Form.Item>

              <Form.Item
                label={t('common.description')}
                name="description"
                rules={[{ required: true, message: '请输入描述' }]}
              >
                <Input.TextArea
                  rows={4}
                  placeholder="描述此算子包的功能和解决的业务问题..."
                />
              </Form.Item>

              <Form.Item label="图标 URL" name="icon">
                <Input placeholder="https://example.com/icon.png" />
              </Form.Item>

              <Form.Item
                label={t('common.version')}
                name="version"
                rules={[{ required: true, message: '请输入版本号' }]}
                initialValue="1.0.0"
              >
                <Input placeholder="例如：1.0.0" />
              </Form.Item>

              <Form.Item
                label="公开"
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

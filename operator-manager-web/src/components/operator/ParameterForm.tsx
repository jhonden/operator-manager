import { Form, Input, Select, Button, Space, Card, Tag } from 'antd';
import { PlusOutlined, MinusCircleOutlined } from '@ant-design/icons';
import type { Parameter, ParameterType } from '@/types';

const parameterTypes: ParameterType[] = [
  'STRING',
  'INTEGER',
  'FLOAT',
  'BOOLEAN',
  'JSON',
  'FILE',
];

const directions = ['INPUT', 'OUTPUT'];

interface ParameterFormProps {
  direction?: 'INPUT' | 'OUTPUT';
  initialValues?: Parameter[];
}

/**
 * Parameter form component for operator configuration
 */
const ParameterForm: React.FC<ParameterFormProps> = ({
  direction = 'INPUT',
  initialValues = [],
}) => {
  return (
    <Form.List name={direction === 'INPUT' ? 'inputParameters' : 'outputParameters'}>
      {(fields, { add, remove }) => (
        <div>
          {fields.map((field, index) => (
            <Card
              key={field.key}
              size="small"
              style={{ marginBottom: 16 }}
              extra={
                <Button
                  type="text"
                  danger
                  icon={<MinusCircleOutlined />}
                  onClick={() => remove(field.name)}
                >
                  Remove
                </Button>
              }
            >
              <Form.Item
                {...field}
                label="Parameter Name"
                name={[field.name, 'name']}
                rules={[{ required: true, message: 'Please input parameter name' }]}
              >
                <Input placeholder="Parameter name" />
              </Form.Item>

              <Form.Item
                {...field}
                label="Type"
                name={[field.name, 'type']}
                rules={[{ required: true, message: 'Please select parameter type' }]}
              >
                <Select placeholder="Select parameter type">
                  {parameterTypes.map((type) => (
                    <Select.Option key={type} value={type}>
                      {type}
                    </Select.Option>
                  ))}
                </Select>
              </Form.Item>

              <Form.Item
                {...field}
                label="Description"
                name={[field.name, 'description']}
              >
                <Input.TextArea
                  rows={2}
                  placeholder="Parameter description"
                />
              </Form.Item>

              <Form.Item
                {...field}
                label="Required"
                name={[field.name, 'required']}
                valuePropName="checked"
                initialValue={false}
              >
                <Select>
                  <Select.Option value={true}>Yes</Select.Option>
                  <Select.Option value={false}>No</Select.Option>
                </Select>
              </Form.Item>

              <Form.Item
                {...field}
                label="Default Value"
                name={[field.name, 'defaultValue']}
              >
                <Input placeholder="Default value (optional)" />
              </Form.Item>

              <Form.Item
                {...field}
                label="Validation Rules (JSON)"
                name={[field.name, 'validationRules']}
              >
                <Input.TextArea
                  rows={3}
                  placeholder='{"min": 0, "max": 100}'
                />
              </Form.Item>
            </Card>
          ))}

          <Button
            type="dashed"
            onClick={() => add({ direction })}
            block
            icon={<PlusOutlined />}
          >
            Add {direction === 'INPUT' ? 'Input' : 'Output'} Parameter
          </Button>
        </div>
      )}
    </Form.List>
  );
};

export default ParameterForm;

import React from 'react';
import { Editor } from '@bytemd/react';
import gfm from '@bytemd/plugin-gfm';
import mermaid from '@bytemd/plugin-mermaid';
import 'bytemd/dist/index.css';
import './index.less';

/**
 * BusinessLogicEditor Props
 */
export interface BusinessLogicEditorProps {
  value?: string;
  onChange?: (value: string) => void;
  placeholder?: string;
  className?: string;
  style?: React.CSSProperties;
}

/**
 * BusinessLogicEditor - 业务逻辑编辑器组件
 *
 * 功能特性：
 * - 支持 Markdown 语法
 * - 支持 Mermaid 流程图、序列图、甘特图等
 * - 实时预览
 * - 支持代码块语法高亮
 *
 * 使用方式：
 * - 作为受控组件使用：<BusinessLogicEditor value={value} onChange={onChange} />
 * - 与 Ant Design Form.Item 配合使用：<Form.Item name="businessLogic"><BusinessLogicEditor /></Form.Item>
 *
 * @author Operator Manager Team
 * @version 1.0.0
 */
const BusinessLogicEditor: React.FC<BusinessLogicEditorProps> = ({
  value = '',
  onChange,
  placeholder,
  className,
  style,
}) => {
  // 配置插件
  const plugins = React.useMemo(() => [gfm(), mermaid()], []);

  const handleChange = (val: string) => {
    console.log('BusinessLogicEditor onChange triggered, value:', val);
    if (onChange) {
      onChange(val);
    }
  };

  return (
    <div className={`business-logic-editor ${className || ''}`} style={style}>
      <Editor
        value={value}
        plugins={plugins}
        onChange={handleChange}
        placeholder={placeholder || '使用 Markdown 描述业务规则，支持 Mermaid 绘制流程图...'}
      />
    </div>
  );
};

export default BusinessLogicEditor;

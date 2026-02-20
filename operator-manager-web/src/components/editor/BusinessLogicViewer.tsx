import React from 'react';
import { Viewer } from '@bytemd/react';
import gfm from '@bytemd/plugin-gfm';
import mermaid from '@bytemd/plugin-mermaid';
import 'bytemd/dist/index.css';
import { Empty } from 'antd';
import './index.less';

/**
 * BusinessLogicViewer Props
 */
interface BusinessLogicViewerProps {
  value?: string;
  className?: string;
  style?: React.CSSProperties;
  loading?: boolean;
}

/**
 * BusinessLogicViewer - 业务逻辑查看器组件
 *
 * 功能特性：
 * - 渲染 Markdown 内容
 * - 渲染 Mermaid 流程图、序列图等
 * - 代码块语法高亮
 * - 空值提示
 *
 * @author Operator Manager Team
 * @version 1.0.0
 */
const BusinessLogicViewer: React.FC<BusinessLogicViewerProps> = ({
  value,
  className,
  style,
  loading,
}) => {
  // 配置插件
  const plugins = React.useMemo(() => [gfm(), mermaid()], []);

  // 空值显示
  if (!value && !loading) {
    return (
      <div className={`business-logic-viewer-empty ${className || ''}`} style={style}>
        <Empty
          image={Empty.PRESENTED_IMAGE_SIMPLE}
          description="暂无业务逻辑"
        />
      </div>
    );
  }

  // 加载状态
  if (loading) {
    return (
      <div className={`business-logic-viewer-loading ${className || ''}`} style={style}>
        <Empty
          image={Empty.PRESENTED_IMAGE_SIMPLE}
          description="加载中..."
        />
      </div>
    );
  }

  return (
    <div className={`business-logic-viewer ${className || ''}`} style={style}>
      <Viewer value={value || ''} plugins={plugins} />
    </div>
  );
};

export default BusinessLogicViewer;

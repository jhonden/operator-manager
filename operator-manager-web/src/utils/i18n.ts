/**
 * i18n 文本管理工具
 *
 * 用途：
 * 1. 当前：统一管理界面中文文本，解决中英文混杂问题
 * 2. 未来：可平滑切换到 react-i18next 等完整国际化方案
 *
 * 使用方式：
 * import { t } from '@/utils/i18n';
 * <Button>{t('common.save')}</Button>
 */

/**
 * 中文文本映射表
 *
 * 格式：模块.功能.键名
 * 示例：operator.list.create, common.button.save
 */
const zhCN: Record<string, string> = {
  // ========== 通用 ==========
  'common.save': '保存',
  'common.saveDraft': '保存草稿',
  'common.publish': '发布',
  'common.updateAndPublish': '更新并发布',
  'common.createAndPublish': '创建并发布',
  'common.edit': '编辑',
  'common.delete': '删除',
  'common.confirm': '确认',
  'common.cancel': '取消',
  'common.yes': '是',
  'common.no': '否',
  'common.back': '返回',
  'common.search': '搜索',
  'common.reset': '重置',
  'common.refresh': '刷新',
  'common.loading': '加载中...',
  'common.create': '创建',
  'common.view': '查看',
  'common.actions': '操作',
  'common.id': 'ID',
  'common.name': '名称',
  'common.description': '描述',
  'common.version': '版本',
  'common.createdAt': '创建时间',
  'common.updatedAt': '更新时间',
  'common.createdBy': '创建人',
  'common.total': '总计',
  'common.items': '项',
  'common.next': '下一步',
  'common.previous': '上一步',
  'common.select': '选择',
  'common.remove': '移除',
  'common.add': '添加',
  'common.success': '成功',
  'common.failed': '失败',
  'common.noData': '暂无数据',

  // ========== 算子通用 ==========
  'operator.name': '算子名称',
  'operator.language': '编程语言',
  'operator.status': '状态',
  'operator.operatorCode': '算子编码',
  'operator.objectCode': '对象编码',
  'operator.dataFormat': '数据格式',
  'operator.generator': '生成方式',
  'operator.code': '代码',
  'operator.businessLogic': '业务逻辑',
  'operator.parameters': '参数',
  'operator.libraries': '代码库',

  // ========== 算子状态 ==========
  'operator.status.draft': '草稿',
  'operator.status.published': '已发布',
  'operator.status.archived': '已归档',

  // ========== 编程语言 ==========
  'language.java': 'Java',
  'language.groovy': 'Groovy',

  // ========== 生成方式 ==========
  'generator.static': '静态内置',
  'generator.dynamic': '动态生成',

  // ========== 参数 ==========
  'parameter.input': '输入参数',
  'parameter.output': '输出参数',
  'parameter.type': '类型',
  'parameter.required': '必填',
  'parameter.defaultValue': '默认值',

  // ========== 公共库 ==========
  'library.name': '公共库名称',
  'library.type': '类型',
  'library.version': '版本',
  'library.description': '描述',
  'library.fileCount': '文件数',
  'library.addTime': '添加时间',
  'library.dependencies': '公共库依赖',
  'library.selectLibrary': '选择公共库',

  // ========== 公共库类型 ==========
  'library.type.constant': '常量',
  'library.type.method': '方法',
  'library.type.model': '模型',
  'library.type.custom': '自定义',

  // ========== 提示信息 ==========
  'message.operator.deletedSuccess': '算子删除成功',
  'message.operator.deletedFailed': '算子删除失败',
  'message.operator.fetchFailed': '获取算子列表失败',
  'message.operator.savedSuccess': '算子保存成功',
  'message.operator.publishedSuccess': '算子发布成功',
  'message.operator.publishFailed': '算子发布失败',
  'message.operator.deleteConfirm': '确定要删除此算子吗？',
  'message.library.fetchFailed': '获取公共库列表失败',
  'message.library.addedSuccess': '公共库依赖添加成功',
  'message.library.addedFailed': '添加公共库依赖失败',
  'message.library.removedSuccess': '公共库依赖移除成功',
  'message.library.removedFailed': '移除公共库依赖失败',
  'message.library.removeConfirm': '确定要移除该公共库依赖吗？',

  // ========== 占位符 ==========
  'placeholder.operator.name': '请输入算子名称',
  'placeholder.operator.description': '请输入算子描述',
  'placeholder.operator.operatorCode': '请输入算子编码',
  'placeholder.operator.objectCode': '请输入对象编码',
  'placeholder.operator.version': '请输入版本号，如 1.0.0',
  'placeholder.searchOperators': '搜索算子...',
  'placeholder.selectLanguage': '选择语言',
  'placeholder.selectStatus': '选择状态',

  // ========== 校验提示 ==========
  'validation.operator.name.required': '请输入算子名称',
  'validation.operator.name.minLength': '算子名称至少 3 个字符',
  'validation.operator.description.required': '请输入算子描述',
  'validation.operator.operatorCode.required': '请输入算子编码',
  'validation.operator.operatorCode.pattern': '算子编码必须以字母或下划线开头，后跟字母、数字或下划线，长度 1-64',
  'validation.operator.objectCode.required': '请输入对象编码',
  'validation.operator.objectCode.pattern': '对象编码必须以字母或下划线开头，后跟字母、数字或下划线，长度 1-64',
  'tooltip.objectCode': '此算子输出的数据对象编码。格式同算子编码。',
  'validation.operator.objectCode.required': '请输入对象编码',
  'validation.operator.objectCode.pattern': '对象编码必须以字母或下划线开头，后跟字母、数字或下划线，长度 1-64',
  'validation.operator.version.required': '请输入版本号',
  'validation.operator.language.required': '请选择编程语言',
  'validation.operator.dataFormat.required': '请至少选择一种数据格式',

  // ========== 工具提示 ==========
  'tooltip.operatorCode': '算子的唯一标识符。必须以字母或下划线开头，后跟字母、数字或下划线。',
  'tooltip.objectCode': '此算子输出的数据对象编码。格式同算子编码。',
  'tooltip.dataFormat': '选择此算子可以处理的原始数据格式',
  'tooltip.generator': '指定此算子是动态生成还是静态内置',
  'tooltip.businessLogic': '使用 Markdown 描述算子的业务规则，支持 Mermaid 绘制流程图、序列图、甘特图等',
};

/**
 * 获取文本（当前语言：中文）
 *
 * @param key - 文本键名
 * @param fallback - 找不到时的默认文本（可选）
 * @returns 对应语言的文本
 *
 * @example
 * t('common.save') // 返回 '保存'
 * t('operator.name') // 返回 '算子名称'
 * t('unknown.key', '默认文本') // 返回 '默认文本'
 */
export function t(key: string, fallback?: string): string {
  const text = zhCN[key];

  if (text === undefined) {
    // 如果找不到翻译文本
    if (fallback !== undefined) {
      return fallback;
    }
    // 开发环境打印警告
    if (process.env.NODE_ENV === 'development') {
      console.warn(`[i18n] 找不到翻译文本: ${key}`);
    }
    return key;
  }

  return text;
}

/**
 * 获取带参数的文本（当前语言：中文）
 *
 * @param key - 文本键名
 * @param params - 参数对象
 * @param fallback - 找不到时的默认文本（可选）
 * @returns 对应语言的文本（参数已替换）
 *
 * @example
 * t('common.totalItems', { count: 10 }) // 未来可支持
 */
export function tWithParams(key: string, params: Record<string, any>, fallback?: string): string {
  let text = zhCN[key];

  if (text === undefined) {
    if (fallback !== undefined) {
      text = fallback;
    } else {
      if (process.env.NODE_ENV === 'development') {
        console.warn(`[i18n] 找不到翻译文本: ${key}`);
      }
      return key;
    }
  }

  // 替换参数占位符（如 {count}）
  Object.keys(params).forEach(param => {
    text = text!.replace(new RegExp(`\\{${param}\\}`, 'g'), String(params[param]));
  });

  return text;
}

/**
 * 设置语言（为将来国际化预留）
 *
 * @param language - 语言代码（如 'zh-CN', 'en-US'）
 */
export function setLanguage(language: string): void {
  // 未来实现：切换语言并持久化到 localStorage
  console.log('[i18n] 设置语言:', language);
}

/**
 * 获取当前语言（为将来国际化预留）
 *
 * @returns 当前语言代码
 */
export function getCurrentLanguage(): string {
  // 未来实现：从 localStorage 读取语言偏好
  return 'zh-CN';
}

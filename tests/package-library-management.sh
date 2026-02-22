#!/bin/bash

# 算子包公共库管理功能测试脚本
# 测试核心业务流程：算子添加公共库依赖后自动同步到算子包

# 基础配置
BASE_URL="http://localhost:8080/api/v1"
USERNAME="admin"
PASSWORD="admin123"

# 测试数据
TIMESTAMP=$(date +%s)
LIBRARY_NAME="TestLibrary_${TIMESTAMP}"
LIBRARY_VERSION="1.0"
LIBRARY_TYPE="METHOD"
OPERATOR_NAME="TestOperator_${TIMESTAMP}"
OPERATOR_CODE="test_operator_${TIMESTAMP}"
PACKAGE_NAME="TestPackage_${TIMESTAMP}"

# 全局变量
TOKEN=""
LIBRARY_ID=""
OPERATOR_ID=""
PACKAGE_ID=""

# 打印测试标题
print_header() {
    echo ""
    echo "======================================"
    echo "$1"
    echo "======================================"
}

# 登录并获取 Token
login() {
    print_header "1. 登录系统"

    RESPONSE=$(curl -s -X POST "${BASE_URL}/auth/login" \
        -H "Content-Type: application/json" \
        -d "{\"username\":\"${USERNAME}\",\"password\":\"${PASSWORD}\"}")

    echo "登录响应: $RESPONSE"

    TOKEN=$(echo "$RESPONSE" | grep -o '"accessToken":"[^"]*"' | sed 's/"accessToken":"//g')

    if [ -z "$TOKEN" ]; then
        echo "❌ 登录失败，无法获取 Token"
        exit 1
    fi

    echo "✅ 登录成功，Token: ${TOKEN:0:20}..."
}

# 创建公共库
create_library() {
    print_header "2. 创建公共库"

    RESPONSE=$(curl -s -X POST "${BASE_URL}/libraries" \
        -H "Content-Type: application/json" \
        -H "Authorization: Bearer $TOKEN" \
        -d "{\"name\":\"${LIBRARY_NAME}\",\"description\":\"测试用公共库\",\"version\":\"${LIBRARY_VERSION}\",\"category\":\"测试\",\"libraryType\":\"${LIBRARY_TYPE}\",\"files\":[{\"fileName\":\"TestUtils.groovy\",\"code\":\"public class TestUtils {\\n    public static String hello() {\\n        return \\\"Hello\\\";\\n    }\\n}\",\"orderIndex\":0}]}")

    echo "创建公共库响应: $RESPONSE"

    LIBRARY_ID=$(echo "$RESPONSE" | grep -o '"id":[0-9]*' | head -1 | sed 's/"id"://')

    if [ -z "$LIBRARY_ID" ]; then
        echo "❌ 创建公共库失败"
        exit 1
    fi

    echo "✅ 公共库创建成功，ID: $LIBRARY_ID"
}

# 创建算子
create_operator() {
    print_header "3. 创建算子"

    RESPONSE=$(curl -s -X POST "${BASE_URL}/operators" \
        -H "Content-Type: application/json" \
        -H "Authorization: Bearer $TOKEN" \
        -d "{\"name\":\"${OPERATOR_NAME}\",\"description\":\"测试算子\",\"operatorCode\":\"${OPERATOR_CODE}\",\"language\":\"GROOVY\",\"status\":\"DRAFT\",\"objectCode\":\"TestObject\"}")

    echo "创建算子响应: $RESPONSE"

    OPERATOR_ID=$(echo "$RESPONSE" | grep -o '"id":[0-9]*' | head -1 | sed 's/"id"://')

    if [ -z "$OPERATOR_ID" ]; then
        echo "❌ 创建算子失败"
        exit 1
    fi

    echo "✅ 算子创建成功，ID: $OPERATOR_ID"
}

# 算子添加公共库依赖
add_library_dependency() {
    print_header "4. 算子添加公共库依赖"

    RESPONSE=$(curl -s -X POST "${BASE_URL}/operators/${OPERATOR_ID}/library-dependencies" \
        -H "Content-Type: application/json" \
        -H "Authorization: Bearer $TOKEN" \
        -d "{\"libraryId\": ${LIBRARY_ID}}")

    echo "添加依赖响应: $RESPONSE"

    SUCCESS=$(echo "$RESPONSE" | grep -o '"success":[^,}]*' | sed 's/"success"://')

    if [ "$SUCCESS" != "true" ]; then
        echo "❌ 添加公共库依赖失败"
        exit 1
    fi

    echo "✅ 公共库依赖添加成功"
}

# 创建算子包
create_package() {
    print_header "5. 创建算子包"

    RESPONSE=$(curl -s -X POST "${BASE_URL}/packages" \
        -H "Content-Type: application/json" \
        -H "Authorization: Bearer $TOKEN" \
        -d "{\"name\":\"${PACKAGE_NAME}\",\"description\":\"测试算子包\",\"businessScenario\":\"数据处理\",\"version\":\"1.0\",\"tags\":[\"测试\"]}")

    echo "创建算子包响应: $RESPONSE"

    PACKAGE_ID=$(echo "$RESPONSE" | grep -o '"id":[0-9]*' | head -1 | sed 's/"id"://')

    if [ -z "$PACKAGE_ID" ]; then
        echo "❌ 创建算子包失败"
        exit 1
    fi

    echo "✅ 算子包创建成功，ID: $PACKAGE_ID"
}

# 算子包添加算子（触发自动同步）
add_operator_to_package() {
    print_header "6. 算子包添加算子（触发自动同步）"

    RESPONSE=$(curl -s -X POST "${BASE_URL}/packages/${PACKAGE_ID}/operators" \
        -H "Content-Type: application/json" \
        -H "Authorization: Bearer $TOKEN" \
        -d "{\"operatorId\": ${OPERATOR_ID}}")

    echo "添加算子响应: $RESPONSE"

    SUCCESS=$(echo "$RESPONSE" | grep -o '"success":[^,}]*' | sed 's/"success"://')

    if [ "$SUCCESS" != "true" ]; then
        echo "❌ 添加算子失败"
        exit 1
    fi

    echo "✅ 算子添加成功（应自动同步公共库）"

    # 等待自动同步完成
    sleep 2
}

# 获取算子包的公共库列表（验证自动同步）
get_package_libraries() {
    print_header "7. 获取算子包的公共库列表（验证自动同步）"

    RESPONSE=$(curl -s -X GET "${BASE_URL}/packages/${PACKAGE_ID}" \
        -H "Authorization: Bearer $TOKEN")

    echo "获取算子包详情响应: $RESPONSE"

    if echo "$RESPONSE" | grep -q "${LIBRARY_NAME}"; then
        echo "✅ 公共库已自动同步到算子包（符合预期）"
    else
        echo "❌ 公共库未自动同步到算子包（不符合预期）"
        exit 1
    fi
}

# 配置公共库路径
configure_library_path() {
    print_header "8. 配置公共库路径"

    RESPONSE=$(curl -s -X PUT "${BASE_URL}/packages/${PACKAGE_ID}/libraries/${LIBRARY_ID}/path-config" \
        -H "Content-Type: application/json" \
        -H "Authorization: Bearer $TOKEN" \
        -d "{\"useCustomPath\": true, \"customPackagePath\": \"lib/${LIBRARY_NAME}\"}")

    echo "配置路径响应: $RESPONSE"

    SUCCESS=$(echo "$RESPONSE" | grep -o '"success":[^,}]*' | sed 's/"success"://')

    if [ "$SUCCESS" != "true" ]; then
        echo "❌ 配置公共库路径失败"
        exit 1
    fi

    echo "✅ 公共库路径配置成功"
}

# 获取打包路径配置（验证路径配置）
get_path_config() {
    print_header "9. 获取打包路径配置（验证路径配置）"

    RESPONSE=$(curl -s -X GET "${BASE_URL}/packages/${PACKAGE_ID}/path-config" \
        -H "Authorization: Bearer $TOKEN")

    echo "获取路径配置响应: $RESPONSE"

    if echo "$RESPONSE" | grep -q "lib/${LIBRARY_NAME}"; then
        echo "✅ 公共库路径配置正确（符合预期）"
    else
        echo "❌ 公共库路径配置不正确（不符合预期）"
        exit 1
    fi
}

# 清理测试数据
cleanup() {
    print_header "10. 清理测试数据"

    # 删除算子包
    curl -s -X DELETE "${BASE_URL}/packages/${PACKAGE_ID}" \
        -H "Authorization: Bearer $TOKEN" > /dev/null

    echo "✅ 测试算子包已删除"

    # 删除算子
    curl -s -X DELETE "${BASE_URL}/operators/${OPERATOR_ID}" \
        -H "Authorization: Bearer $TOKEN" > /dev/null

    echo "✅ 测试算子已删除"

    # 删除公共库
    curl -s -X DELETE "${BASE_URL}/libraries/${LIBRARY_ID}" \
        -H "Authorization: Bearer $TOKEN" > /dev/null

    echo "✅ 测试公共库已删除"
}

# 主测试流程
main() {
    echo "开始算子包公共库管理功能测试..."

    # 登录
    login

    # 创建测试数据
    create_library
    create_operator
    add_library_dependency
    create_package

    # 测试自动同步功能
    add_operator_to_package
    get_package_libraries

    # 测试路径配置功能
    configure_library_path
    get_path_config

    # 清理
    cleanup

    echo ""
    echo "======================================"
    echo "✅ 所有测试通过！"
    echo "======================================"
}

# 执行主流程
main

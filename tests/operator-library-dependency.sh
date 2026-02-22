#!/bin/bash

# 算子公共库依赖测试脚本

# 基础配置
BASE_URL="http://localhost:8080/api/v1"
USERNAME="admin"
PASSWORD="admin123"

# 测试数据
LIBRARY_NAME="TestLibrary_$$"
LIBRARY_VERSION="1.0"
LIBRARY_TYPE="METHOD"
OPERATOR_NAME="TestOperatorForLib_$$"
OPERATOR_CODE="test_operator_lib_$$"

# 全局变量
TOKEN=""
LIBRARY_ID=""
OPERATOR_ID=""

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

# 获取算子依赖的公共库列表（应为空）
get_operator_libraries_empty() {
    print_header "4. 获取算子依赖的公共库列表（应为空）"

    RESPONSE=$(curl -s -X GET "${BASE_URL}/operators/${OPERATOR_ID}/library-dependencies" \
        -H "Authorization: Bearer $TOKEN")

    echo "获取公共库列表响应: $RESPONSE"

    if echo "$RESPONSE" | grep -q '"data":\[\]'; then
        echo "✅ 算子未依赖任何公共库（符合预期）"
    else
        echo "❌ 算子已有依赖的公共库（不符合预期）"
        exit 1
    fi
}

# 添加公共库依赖
add_library_dependency() {
    print_header "5. 添加公共库依赖"

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

# 获取算子依赖的公共库列表（应有 1 个）
get_operator_libraries_one() {
    print_header "6. 获取算子依赖的公共库列表（应有 1 个）"

    RESPONSE=$(curl -s -X GET "${BASE_URL}/operators/${OPERATOR_ID}/library-dependencies" \
        -H "Authorization: Bearer $TOKEN")

    echo "获取公共库列表响应: $RESPONSE"

    if echo "$RESPONSE" | grep -q "${LIBRARY_NAME}"; then
        echo "✅ 算子依赖了公共库 ${LIBRARY_NAME}（符合预期）"
    else
        echo "❌ 算子未依赖公共库（不符合预期）"
        exit 1
    fi
}

# 尝试重复添加公共库依赖（应失败）
add_duplicate_library_dependency() {
    print_header "7. 尝试重复添加公共库依赖（应失败）"

    RESPONSE=$(curl -s -X POST "${BASE_URL}/operators/${OPERATOR_ID}/library-dependencies" \
        -H "Content-Type: application/json" \
        -H "Authorization: Bearer $TOKEN" \
        -d "{\"libraryId\": ${LIBRARY_ID}}")

    echo "重复添加依赖响应: $RESPONSE"

    SUCCESS=$(echo "$RESPONSE" | grep -o '"success":[^,}]*' | sed 's/"success"://')

    if [ "$SUCCESS" != "true" ]; then
        echo "✅ 重复添加被拒绝（符合预期）"
    else
        echo "❌ 重复添加未被拒绝（不符合预期）"
        exit 1
    fi
}

# 移除公共库依赖
remove_library_dependency() {
    print_header "8. 移除公共库依赖"

    RESPONSE=$(curl -s -X DELETE "${BASE_URL}/operators/${OPERATOR_ID}/library-dependencies/${LIBRARY_ID}" \
        -H "Authorization: Bearer $TOKEN")

    echo "移除依赖响应: $RESPONSE"

    SUCCESS=$(echo "$RESPONSE" | grep -o '"success":[^,}]*' | sed 's/"success"://')

    if [ "$SUCCESS" != "true" ]; then
        echo "❌ 移除公共库依赖失败"
        exit 1
    fi

    echo "✅ 公共库依赖移除成功"
}

# 获取算子依赖的公共库列表（应为空）
get_operator_libraries_empty_again() {
    print_header "9. 获取算子依赖的公共库列表（应为空）"

    RESPONSE=$(curl -s -X GET "${BASE_URL}/operators/${OPERATOR_ID}/library-dependencies" \
        -H "Authorization: Bearer $TOKEN")

    echo "获取公共库列表响应: $RESPONSE"

    if echo "$RESPONSE" | grep -q '"data":\[\]'; then
        echo "✅ 算子未依赖任何公共库（符合预期）"
    else
        echo "❌ 算子仍有依赖的公共库（不符合预期）"
        exit 1
    fi
}

# 清理测试数据
cleanup() {
    print_header "10. 清理测试数据"

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
    echo "开始算子公共库依赖功能测试..."

    # 登录
    login

    # 创建测试数据
    create_library
    create_operator

    # 测试功能
    get_operator_libraries_empty
    add_library_dependency
    get_operator_libraries_one
    add_duplicate_library_dependency
    remove_library_dependency
    get_operator_libraries_empty_again

    # 清理
    cleanup

    echo ""
    echo "======================================"
    echo "✅ 所有测试通过！"
    echo "======================================"
}

# 执行主流程
main

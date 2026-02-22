#!/bin/bash

# 批量更新算子公共库依赖功能测试脚本
# 测试批量更新多个算子的公共库依赖

# 基础配置
BASE_URL="http://localhost:8080/api/v1"
USERNAME="admin"
PASSWORD="admin123"

# 测试数据
TIMESTAMP=$(date +%s)

# 全局变量
TOKEN=""

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

# 创建测试算子
create_operator() {
    local name=$1
    print_header "2. 创建算子: $name"

    RESPONSE=$(curl -s -X POST "${BASE_URL}/operators" \
        -H "Content-Type: application/json" \
        -H "Authorization: Bearer $TOKEN" \
        -d "{\"name\":\"${name}\",\"description\":\"测试算子\",\"operatorCode\":\"${name}\",\"objectCode\":\"TestObject\",\"language\":\"GROOVY\",\"status\":\"DRAFT\"}")

    echo "创建算子响应: $RESPONSE"

    local operator_id=$(echo "$RESPONSE" | grep -o '"id":[0-9]*' | head -1 | sed 's/"id"://')

    if [ -z "$operator_id" ]; then
        echo "❌ 创建算子失败"
        exit 1
    fi

    echo "✅ 算子创建成功，ID: $operator_id"
}

# 创建测试公共库
create_library() {
    local name=$1
    print_header "3. 创建公共库: $name"

    RESPONSE=$(curl -s -X POST "${BASE_URL}/libraries" \
        -H "Content-Type: application/json" \
        -H "Authorization: Bearer $TOKEN" \
        -d "{\"name\":\"${name}\",\"description\":\"测试用公共库\",\"version\":\"1.0\",\"category\":\"测试\",\"libraryType\":\"METHOD\",\"files\":[{\"fileName\":\"${name}.groovy\",\"code\":\"public class ${name} {\\n    public static String hello() {\\n        return \\\"Hello from ${name}\\\";\\n    }\\n}\",\"orderIndex\":0}]}")

    echo "创建公共库响应: $RESPONSE"

    local library_id=$(echo "$RESPONSE" | grep -o '"id":[0-9]*' | head -1 | sed 's/"id"://')

    if [ -z "$library_id" ]; then
        echo "❌ 创建公共库失败"
        exit 1
    fi

    echo "✅ 公共库创建成功，ID: $library_id"
}

# 为单个算子添加公共库依赖
add_library_dependency() {
    local operator_id=$1
    local library_id=$2
    print_header "4. 为算子 $operator_id 添加公共库依赖: $library_id"

    RESPONSE=$(curl -s -X POST "${BASE_URL}/operators/${operator_id}/library-dependencies" \
        -H "Content-Type: application/json" \
        -H "Authorization: Bearer $TOKEN" \
        -d "{\"libraryId\": ${library_id}}")

    echo "添加依赖响应: $RESPONSE"

    local success=$(echo "$RESPONSE" | grep -o '"success":[^,}]*' | sed 's/"success"://')

    if [ "$success" != "true" ]; then
        echo "❌ 添加公共库依赖失败"
        exit 1
    fi

    echo "✅ 公共库依赖添加成功"
}

# 查看算子当前依赖的公共库
get_operator_libraries() {
    local operator_id=$1
    print_header "5. 查看算子 $operator_id 的公共库依赖"

    RESPONSE=$(curl -s -X GET "${BASE_URL}/operators/${operator_id}/library-dependencies" \
        -H "Authorization: Bearer $TOKEN")

    echo "获取依赖响应: $RESPONSE"

    echo "✅ 查询成功"
}

# 批量更新算子公共库依赖
batch_update_libraries() {
    local operator_ids=$1
    local library_ids=$2
    print_header "6. 批量更新算子公共库依赖"

    REQUEST="{\"operatorIds\": ${operator_ids}, \"libraryIds\": ${library_ids}}"

    RESPONSE=$(curl -s -X POST "${BASE_URL}/operators/batch-library-dependencies" \
        -H "Content-Type: application/json" \
        -H "Authorization: Bearer $TOKEN" \
        -d "$REQUEST")

    echo "批量更新响应: $RESPONSE"

    local success=$(echo "$RESPONSE" | grep -o '"success":[^,}]*' | sed 's/"success"://')

    if [ "$success" != "true" ]; then
        echo "❌ 批量更新失败"
        exit 1
    fi

    echo "✅ 批量更新成功"
}

# 验证批量更新结果
verify_batch_update() {
    local operator_id=$1
    print_header "7. 验证算子 $operator_id 的公共库依赖"

    RESPONSE=$(curl -s -X GET "${BASE_URL}/operators/${operator_id}/library-dependencies" \
        -H "Authorization: Bearer $TOKEN")

    echo "验证响应: $RESPONSE"
    echo "✅ 验证成功"
}

# 清理测试数据
cleanup() {
    print_header "8. 清理测试数据"

    # 删除算子（会级联删除公共库依赖）
    if [ ! -z "$OPERATOR_1_ID" ]; then
        echo "删除算子 $OPERATOR_1_ID"
        curl -s -X DELETE "${BASE_URL}/operators/${OPERATOR_1_ID}" \
            -H "Authorization: Bearer $TOKEN" > /dev/null
    fi

    if [ ! -z "$OPERATOR_2_ID" ]; then
        echo "删除算子 $OPERATOR_2_ID"
        curl -s -X DELETE "${BASE_URL}/operators/${OPERATOR_2_ID}" \
            -H "Authorization: Bearer $TOKEN" > /dev/null
    fi

    if [ ! -z "$OPERATOR_3_ID" ]; then
        echo "删除算子 $OPERATOR_3_ID"
        curl -s -X DELETE "${BASE_URL}/operators/${OPERATOR_3_ID}" \
            -H "Authorization: Bearer $TOKEN" > /dev/null
    fi

    # 删除公共库
    if [ ! -z "$LIBRARY_1_ID" ]; then
        echo "删除公共库 $LIBRARY_1_ID"
        curl -s -X DELETE "${BASE_URL}/libraries/${LIBRARY_1_ID}" \
            -H "Authorization: Bearer $TOKEN" > /dev/null
    fi

    if [ ! -z "$LIBRARY_2_ID" ]; then
        echo "删除公共库 $LIBRARY_2_ID"
        curl -s -X DELETE "${BASE_URL}/libraries/${LIBRARY_2_ID}" \
            -H "Authorization: Bearer $TOKEN" > /dev/null
    fi

    echo "✅ 测试数据清理完成"
}

# 主测试流程
main() {
    echo "开始批量更新算子公共库依赖功能测试..."

    # 登录
    login

    # 创建测试数据
    OPERATOR_1_RESPONSE=$(curl -s -X POST "${BASE_URL}/operators" \
        -H "Content-Type: application/json" \
        -H "Authorization: Bearer $TOKEN" \
        -d "{\"name\":\"Operator_${TIMESTAMP}_1\",\"description\":\"测试算子\",\"operatorCode\":\"Operator_${TIMESTAMP}_1\",\"objectCode\":\"TestObject\",\"language\":\"GROOVY\",\"status\":\"DRAFT\"}")
    OPERATOR_1_ID=$(echo "$OPERATOR_1_RESPONSE" | grep -o '"id":[0-9]*' | head -1 | sed 's/"id"://')
    echo "✅ 创建算子 1, ID: $OPERATOR_1_ID"

    OPERATOR_2_RESPONSE=$(curl -s -X POST "${BASE_URL}/operators" \
        -H "Content-Type: application/json" \
        -H "Authorization: Bearer $TOKEN" \
        -d "{\"name\":\"Operator_${TIMESTAMP}_2\",\"description\":\"测试算子\",\"operatorCode\":\"Operator_${TIMESTAMP}_2\",\"objectCode\":\"TestObject\",\"language\":\"GROOVY\",\"status\":\"DRAFT\"}")
    OPERATOR_2_ID=$(echo "$OPERATOR_2_RESPONSE" | grep -o '"id":[0-9]*' | head -1 | sed 's/"id"://')
    echo "✅ 创建算子 2, ID: $OPERATOR_2_ID"

    OPERATOR_3_RESPONSE=$(curl -s -X POST "${BASE_URL}/operators" \
        -H "Content-Type: application/json" \
        -H "Authorization: Bearer $TOKEN" \
        -d "{\"name\":\"Operator_${TIMESTAMP}_3\",\"description\":\"测试算子\",\"operatorCode\":\"Operator_${TIMESTAMP}_3\",\"objectCode\":\"TestObject\",\"language\":\"GROOVY\",\"status\":\"DRAFT\"}")
    OPERATOR_3_ID=$(echo "$OPERATOR_3_RESPONSE" | grep -o '"id":[0-9]*' | head -1 | sed 's/"id"://')
    echo "✅ 创建算子 3, ID: $OPERATOR_3_ID"

    LIBRARY_1_RESPONSE=$(curl -s -X POST "${BASE_URL}/libraries" \
        -H "Content-Type: application/json" \
        -H "Authorization: Bearer $TOKEN" \
        -d "{\"name\":\"Library_${TIMESTAMP}_1\",\"description\":\"测试用公共库\",\"version\":\"1.0\",\"category\":\"测试\",\"libraryType\":\"METHOD\",\"files\":[{\"fileName\":\"Library_${TIMESTAMP}_1.groovy\",\"code\":\"public class Library_${TIMESTAMP}_1 {\\n    public static String hello() {\\n        return \\\"Hello from Library_${TIMESTAMP}_1\\\";\\n    }\\n}\",\"orderIndex\":0}]}")
    LIBRARY_1_ID=$(echo "$LIBRARY_1_RESPONSE" | grep -o '"id":[0-9]*' | head -1 | sed 's/"id"://')
    echo "✅ 创建公共库 1, ID: $LIBRARY_1_ID"

    LIBRARY_2_RESPONSE=$(curl -s -X POST "${BASE_URL}/libraries" \
        -H "Content-Type: application/json" \
        -H "Authorization: Bearer $TOKEN" \
        -d "{\"name\":\"Library_${TIMESTAMP}_2\",\"description\":\"测试用公共库\",\"version\":\"1.0\",\"category\":\"测试\",\"libraryType\":\"METHOD\",\"files\":[{\"fileName\":\"Library_${TIMESTAMP}_2.groovy\",\"code\":\"public class Library_${TIMESTAMP}_2 {\\n    public static String hello() {\\n        return \\\"Hello from Library_${TIMESTAMP}_2\\\";\\n    }\\n}\",\"orderIndex\":0}]}")
    LIBRARY_2_ID=$(echo "$LIBRARY_2_RESPONSE" | grep -o '"id":[0-9]*' | head -1 | sed 's/"id"://')
    echo "✅ 创建公共库 2, ID: $LIBRARY_2_ID"

    echo ""
    echo "测试数据准备完成："
    echo "  - 算子: $OPERATOR_1_ID, $OPERATOR_2_ID, $OPERATOR_3_ID"
    echo "  - 公共库: $LIBRARY_1_ID, $LIBRARY_2_ID"

    # 场景1: 为算子添加不同的公共库
    echo ""
    echo "=== 场景1: 初始添加依赖 ==="
    add_library_dependency "$OPERATOR_1_ID" "$LIBRARY_1_ID"
    add_library_dependency "$OPERATOR_2_ID" "$LIBRARY_2_ID"
    add_library_dependency "$OPERATOR_3_ID" "$LIBRARY_1_ID"

    # 查看初始依赖
    echo ""
    echo "=== 查看初始依赖 ==="
    get_operator_libraries "$OPERATOR_1_ID"
    get_operator_libraries "$OPERATOR_2_ID"
    get_operator_libraries "$OPERATOR_3_ID"

    # 场景2: 批量更新 - 统一使用库1和库2
    echo ""
    echo "=== 场景2: 批量更新 - 统一使用库1和库2 ==="
    batch_update_libraries "[$OPERATOR_1_ID, $OPERATOR_2_ID, $OPERATOR_3_ID]" "[$LIBRARY_1_ID, $LIBRARY_2_ID]"

    # 验证批量更新结果
    echo ""
    echo "=== 验证批量更新结果 ==="
    verify_batch_update "$OPERATOR_1_ID"
    verify_batch_update "$OPERATOR_2_ID"
    verify_batch_update "$OPERATOR_3_ID"

    # 场景3: 批量更新 - 只使用库1（测试移除功能）
    echo ""
    echo "=== 场景3: 批量更新 - 只使用库1（测试移除功能） ==="
    batch_update_libraries "[$OPERATOR_1_ID, $OPERATOR_2_ID, $OPERATOR_3_ID]" "[$LIBRARY_1_ID]"

    # 验证场景3结果
    echo ""
    echo "=== 验证场景3结果（所有算子应该只有库1） ==="
    verify_batch_update "$OPERATOR_1_ID"
    verify_batch_update "$OPERATOR_2_ID"
    verify_batch_update "$OPERATOR_3_ID"

    # 场景4: 批量更新 - 只使用库2（测试替换功能）
    echo ""
    echo "=== 场景4: 批量更新 - 只使用库2（测试替换功能） ==="
    batch_update_libraries "[$OPERATOR_1_ID, $OPERATOR_2_ID, $OPERATOR_3_ID]" "[$LIBRARY_2_ID]"

    # 验证场景4结果
    echo ""
    echo "=== 验证场景4结果（所有算子应该只有库2） ==="
    verify_batch_update "$OPERATOR_1_ID"
    verify_batch_update "$OPERATOR_2_ID"
    verify_batch_update "$OPERATOR_3_ID"

    # 清理
    cleanup

    echo ""
    echo "======================================"
    echo "✅ 所有测试通过！"
    echo "======================================"
}

# 执行主流程
main

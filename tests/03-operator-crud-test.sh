#!/bin/bash

# Operator CRUD tests
# @author Operator Manager Team

source "$(dirname "$0")/utils/logger.sh"
source "$(dirname "$0")/utils/assertions.sh"

# API Base URL
API_BASE="http://localhost:8080/api/v1"

# Test credentials
TEST_ADMIN="admin"
TEST_PASS="admin123"

# Test counters
TEST_NUM=0
PASS_COUNT=0
FAIL_COUNT=0

# Export IDs for tests
TOKEN_FILE="/tmp/test_auth_token.txt"
OPERATOR_ID_FILE="/tmp/test_operator_id.txt"

# Helper functions
get_admin_token() {
    local login_json="{\"username\": \"$TEST_ADMIN\", \"password\": \"$TEST_PASS\"}"
    local response=$(curl -s -X POST "$API_BASE/auth/login" \
        -H "Content-Type: application/json" \
        -d "$login_json")
    local token=$(echo "$response" | grep -o '"accessToken":"[^"]*"' | cut -d'"' -f4)
    echo "$token" > "$TOKEN_FILE"
    echo "$token"
}

# Test case: Create operator
test_create_operator() {
    TEST_NUM=$((TEST_NUM + 1))
    log_step "TEST 01: Create operator"

    local TOKEN=$(get_admin_token)

    local operator_json="{
        \"name\": \"Test Operator\",
        \"description\": \"Test operator description\",
        \"language\": \"PYTHON\",
        \"status\": \"DRAFT\",
        \"categoryId\": 1,
        \"code\": \"def test_function():\\n    return 'hello world'\",
        \"tags\": [\"test\", \"automation\"],
        \"isPublic\": false
    }"

    local response=$(curl -s -X POST "$API_BASE/operators" \
        -H "Content-Type: application/json" \
        -H "Authorization: Bearer $TOKEN" \
        -d "$operator_json")
    local success=$(echo "$response" | grep -o '"success":[a-z]*' | cut -d':' -f2)

    if [ "$success" = "true" ]; then
        local operator_id=$(echo "$response" | grep -o '"id":[0-9]*' | cut -d':' -f2 | head -1)
        local name=$(echo "$response" | grep -o '"name":"[^"]*"' | cut -d'"' -f4)
        local code=$(echo "$response" | grep -o '"code":"[^"]*"' | head -1 | cut -d'"' -f4)

        if [ "$name" = "Test Operator" ] && [ -n "$code" ]; then
            echo "$operator_id" > "$OPERATOR_ID_FILE"
            log_success "✓ Operator created successfully with ID: $operator_id"
            log_data "Code preview: ${code:0:50}..."
            PASS_COUNT=$((PASS_COUNT + 1))
        else
            log_error "✗ Operator created but response data invalid"
            FAIL_COUNT=$((FAIL_COUNT + 1))
        fi
    else
        log_error "✗ Operator creation failed"
        FAIL_COUNT=$((FAIL_COUNT + 1))
    fi
}

# Test case: Create operator with parameters
test_create_operator_with_parameters() {
    TEST_NUM=$((TEST_NUM + 1))
    log_step "TEST 02: Create operator with parameters"

    local TOKEN=$(get_admin_token)

    local operator_json="{
        \"name\": \"Test Operator With Params\",
        \"description\": \"Test operator with parameters\",
        \"language\": \"PYTHON\",
        \"status\": \"DRAFT\",
        \"categoryId\": 1,
        \"code\": \"def process(input_data):\\n    return input_data\",
        \"tags\": [\"test\", \"params\"],
        \"isPublic\": false,
        \"parameters\": [
            {
                \"name\": \"input_data\",
                \"description\": \"Input data parameter\",
                \"parameterType\": \"STRING\",
                \"ioType\": \"INPUT\",
                \"isRequired\": true,
                \"orderIndex\": 1
            },
            {
                \"name\": \"output_result\",
                \"description\": \"Output result parameter\",
                \"parameterType\": \"STRING\",
                \"ioType\": \"OUTPUT\",
                \"isRequired\": false,
                \"orderIndex\": 2
            }
        ]
    }"

    local response=$(curl -s -X POST "$API_BASE/operators" \
        -H "Content-Type: application/json" \
        -H "Authorization: Bearer $TOKEN" \
        -d "$operator_json")
    local success=$(echo "$response" | grep -o '"success":[a-z]*' | cut -d':' -f2)

    if [ "$success" = "true" ]; then
        local operator_id=$(echo "$response" | grep -o '"id":[0-9]*' | cut -d':' -f2 | head -1)
        echo "$operator_id" > "$OPERATOR_ID_FILE"

        # Check if parameters are returned
        local has_params=$(echo "$response" | grep -o '"parameters":\[' | wc -l)

        if [ "$has_params" -ge 1 ]; then
            log_success "✓ Operator with parameters created successfully"
            PASS_COUNT=$((PASS_COUNT + 1))
        else
            log_error "✗ Operator created but parameters not returned"
            FAIL_COUNT=$((FAIL_COUNT + 1))
        fi
    else
        log_error "✗ Operator with parameters creation failed"
        FAIL_COUNT=$((FAIL_COUNT + 1))
    fi
}

# Test case: Get operator by ID
test_get_operator() {
    TEST_NUM=$((TEST_NUM + 1))
    log_step "TEST 03: Get operator by ID"

    local TOKEN=$(get_admin_token)
    local operator_id=$(cat "$OPERATOR_ID_FILE" 2>/dev/null)

    if [ -z "$operator_id" ]; then
        log_error "✗ No operator ID available, skipping test"
        FAIL_COUNT=$((FAIL_COUNT + 1))
        return
    fi

    local response=$(curl -s -X GET "$API_BASE/operators/$operator_id" \
        -H "Authorization: Bearer $TOKEN")
    local success=$(echo "$response" | grep -o '"success":[a-z]*' | cut -d':' -f2)

    if [ "$success" = "true" ]; then
        local name=$(echo "$response" | grep -o '"name":"[^"]*"' | cut -d'"' -f4)
        if [ -n "$name" ]; then
            log_success "✓ Operator retrieved successfully: $name"
            PASS_COUNT=$((PASS_COUNT + 1))
        else
            log_error "✗ Operator retrieved but response data invalid"
            FAIL_COUNT=$((FAIL_COUNT + 1))
        fi
    else
        log_error "✗ Get operator failed"
        FAIL_COUNT=$((FAIL_COUNT + 1))
    fi
}

# Test case: Get operator and verify parameters
test_get_operator_with_parameters() {
    TEST_NUM=$((TEST_NUM + 1))
    log_step "TEST 04: Get operator and verify parameters"

    local TOKEN=$(get_admin_token)
    local operator_id=$(cat "$OPERATOR_ID_FILE" 2>/dev/null)

    if [ -z "$operator_id" ]; then
        log_error "✗ No operator ID available, skipping test"
        FAIL_COUNT=$((FAIL_COUNT + 1))
        return
    fi

    local response=$(curl -s -X GET "$API_BASE/operators/$operator_id" \
        -H "Authorization: Bearer $TOKEN")
    local success=$(echo "$response" | grep -o '"success":[a-z]*' | cut -d':' -f2)

    if [ "$success" = "true" ]; then
        # Check if parameters array exists and is not empty
        local has_params=$(echo "$response" | grep -o '"parameters":\[' | wc -l)

        if [ "$has_params" -ge 1 ]; then
            # Count parameters
            local param_count=$(echo "$response" | grep -o '"ioType":"[A-Z]*"' | wc -l)

            if [ "$param_count" -ge 1 ]; then
                log_success "✓ Operator retrieved with $param_count parameters"
                PASS_COUNT=$((PASS_COUNT + 1))
            else
                log_error "✗ Operator parameters array exists but is empty"
                FAIL_COUNT=$((FAIL_COUNT + 1))
            fi
        else
            log_error "✗ Operator retrieved but parameters not in response"
            FAIL_COUNT=$((FAIL_COUNT + 1))
        fi
    else
        log_error "✗ Get operator failed"
        FAIL_COUNT=$((FAIL_COUNT + 1))
    fi
}

# Test case: Update operator
test_update_operator() {
    TEST_NUM=$((TEST_NUM + 1))
    log_step "TEST 05: Update operator"

    local TOKEN=$(get_admin_token)
    local operator_id=$(cat "$OPERATOR_ID_FILE" 2>/dev/null)

    if [ -z "$operator_id" ]; then
        log_error "✗ No operator ID available, skipping test"
        FAIL_COUNT=$((FAIL_COUNT + 1))
        return
    fi

    local update_json="{
        \"name\": \"Updated Test Operator\",
        \"description\": \"Updated description\",
        \"language\": \"PYTHON\",
        \"status\": \"PUBLISHED\",
        \"categoryId\": 1,
        \"code\": \"def updated_function():\\n    return 'updated'\",
        \"tags\": [\"test\", \"updated\"],
        \"isPublic\": true
    }"

    local response=$(curl -s -X PUT "$API_BASE/operators/$operator_id" \
        -H "Content-Type: application/json" \
        -H "Authorization: Bearer $TOKEN" \
        -d "$update_json")
    local success=$(echo "$response" | grep -o '"success":[a-z]*' | cut -d':' -f2)

    if [ "$success" = "true" ]; then
        local name=$(echo "$response" | grep -o '"name":"[^"]*"' | cut -d'"' -f4)
        local status=$(echo "$response" | grep -o '"status":"[^"]*"' | cut -d'"' -f4)

        if [ "$name" = "Updated Test Operator" ] && [ "$status" = "PUBLISHED" ]; then
            log_success "✓ Operator updated successfully"
            PASS_COUNT=$((PASS_COUNT + 1))
        else
            log_error "✗ Operator updated but response data invalid"
            FAIL_COUNT=$((FAIL_COUNT + 1))
        fi
    else
        log_error "✗ Operator update failed"
        FAIL_COUNT=$((FAIL_COUNT + 1))
    fi
}

# Test case: List operators with pagination
test_list_operators() {
    TEST_NUM=$((TEST_NUM + 1))
    log_step "TEST 06: List operators with pagination"

    local TOKEN=$(get_admin_token)

    local response=$(curl -s -X GET "$API_BASE/operators?page=0&size=10" \
        -H "Authorization: Bearer $TOKEN")
    local success=$(echo "$response" | grep -o '"success":[a-z]*' | cut -d':' -f2)

    if [ "$success" = "true" ]; then
        local total=$(echo "$response" | grep -o '"totalElements":[0-9]*' | cut -d':' -f2 | head -1)

        if [ -n "$total" ] && [ "$total" -ge 0 ]; then
            log_success "✓ Operators listed successfully, total: $total"
            PASS_COUNT=$((PASS_COUNT + 1))
        else
            log_error "✗ Operators listed but pagination data invalid"
            FAIL_COUNT=$((FAIL_COUNT + 1))
        fi
    else
        log_error "✗ List operators failed"
        FAIL_COUNT=$((FAIL_COUNT + 1))
    fi
}

# Test case: Search operators by name
test_search_operators() {
    TEST_NUM=$((TEST_NUM + 1))
    log_step "TEST 07: Search operators by name"

    local TOKEN=$(get_admin_token)

    local response=$(curl -s -X GET "$API_BASE/operators/search?keyword=Test&status=DRAFT" \
        -H "Authorization: Bearer $TOKEN")
    local success=$(echo "$response" | grep -o '"success":[a-z]*' | cut -d':' -f2)

    if [ "$success" = "true" ]; then
        local total=$(echo "$response" | grep -o '"totalElements":[0-9]*' | cut -d':' -f2 | head -1)

        if [ -n "$total" ]; then
            log_success "✓ Operator search completed, found: $total"
            PASS_COUNT=$((PASS_COUNT + 1))
        else
            log_error "✗ Operator search completed but response invalid"
            FAIL_COUNT=$((FAIL_COUNT + 1))
        fi
    else
        log_error "✗ Operator search failed"
        FAIL_COUNT=$((FAIL_COUNT + 1))
    fi
}

# Test case: Delete operator
test_delete_operator() {
    TEST_NUM=$((TEST_NUM + 1))
    log_step "TEST 08: Delete operator"

    local TOKEN=$(get_admin_token)
    local operator_id=$(cat "$OPERATOR_ID_FILE" 2>/dev/null)

    if [ -z "$operator_id" ]; then
        log_error "✗ No operator ID available, skipping test"
        FAIL_COUNT=$((FAIL_COUNT + 1))
        return
    fi

    local response=$(curl -s -X DELETE "$API_BASE/operators/$operator_id" \
        -H "Authorization: Bearer $TOKEN")
    local success=$(echo "$response" | grep -o '"success":[a-z]*' | cut -d':' -f2)

    if [ "$success" = "true" ]; then
        # Verify deletion by trying to get the operator
        local get_response=$(curl -s -X GET "$API_BASE/operators/$operator_id" \
            -H "Authorization: Bearer $TOKEN")
        local get_success=$(echo "$get_response" | grep -o '"success":[a-z]*' | cut -d':' -f2)

        if [ "$get_success" = "false" ]; then
            log_success "✓ Operator deleted and verified"
            PASS_COUNT=$((PASS_COUNT + 1))
        else
            log_error "✗ Operator deletion returned success but operator still exists"
            FAIL_COUNT=$((FAIL_COUNT + 1))
        fi
    else
        log_error "✗ Operator deletion failed"
        log_data "Response: $response"
        FAIL_COUNT=$((FAIL_COUNT + 1))
    fi
}

# Main execution
main() {
    log_info "Starting operator CRUD tests"
    log_info "API Base: $API_BASE"

    # Execute tests
    test_create_operator
    test_create_operator_with_parameters
    test_get_operator
    test_get_operator_with_parameters
    test_update_operator
    test_list_operators
    test_search_operators
    test_delete_operator

    # Summary
    log_step "Operator CRUD tests completed"
    log_info "Passed: $PASS_COUNT"
    log_info "Failed: $FAIL_COUNT"
    log_info "Total: $((PASS_COUNT + FAIL_COUNT))"

    if [ "$FAIL_COUNT" -gt 0 ]; then
        exit 1
    else
        exit 0
    fi
}

# Run main function
main

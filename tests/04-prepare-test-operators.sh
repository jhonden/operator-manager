#!/bin/bash

# Prepare test operators for package testing
# @author Operator Manager Team

source "$(dirname "$0")/utils/logger.sh"

API_BASE="http://localhost:8080/api/v1"
TEST_ADMIN="admin"
TEST_PASS="admin123"

# Helper function to get admin token
get_admin_token() {
    local login_json="{\"username\": \"$TEST_ADMIN\", \"password\": \"$TEST_PASS\"}"
    local response=$(curl -s -X POST "$API_BASE/auth/login" \
        -H "Content-Type: application/json" \
        -d "$login_json")
    local token=$(echo "$response" | grep -o '"accessToken":"[^"]*"' | cut -d'"' -f4)
    echo "$token"
}

# Helper function to create operator
create_operator() {
    local token="$1"
    local name="$2"
    local description="$3"
    local code="$4"
    local parameters="$5"

    local operator_json="{
        \"name\": \"$name\",
        \"description\": \"$description\",
        \"language\": \"GROOVY\",
        \"status\": \"DRAFT\",
        \"categoryId\": 1,
        \"code\": \"$code\",
        \"tags\": [\"test\"],
        \"isPublic\": false,
        \"parameters\": $parameters
    }"

    local response=$(curl -s -X POST "$API_BASE/operators" \
        -H "Content-Type: application/json" \
        -H "Authorization: Bearer $token" \
        -d "$operator_json")

    local success=$(echo "$response" | grep -o '"success":[a-z]*' | cut -d':' -f2)

    if [ "$success" = "true" ]; then
        local operator_id=$(echo "$response" | grep -o '"id":[0-9]*' | cut -d':' -f2 | head -1)
        echo "$operator_id"
    else
        echo "ERROR"
        echo "$response"
    fi
}

# Main execution
log_info "Starting to create test operators..."

# Get admin token
TOKEN=$(get_admin_token)
if [ -z "$TOKEN" ]; then
    log_error "Failed to get admin token"
    exit 1
fi
log_info "Admin token obtained"

# Operator 1: String processing operator
OP1_CODE='def processText(inputText): return inputText.toUpperCase()'
OP1_ID=$(create_operator "$TOKEN" \
    "String Processing Operator" \
    "Processes string data and performs text transformations" \
    "$OP1_CODE" \
    "[]")

if [ "$OP1_ID" = "ERROR" ]; then
    log_error "Failed to create Operator 1"
else
    log_success "Operator 1 created with ID: $OP1_ID"
fi

# Operator 2: Data aggregation operator
OP2_PARAMS=$(cat <<'EOF'
[
    {
        "name": "data",
        "description": "Data sources",
        "parameterType": "OBJECT",
        "ioType": "INPUT",
        "isRequired": true,
        "orderIndex": 1
    }
]
EOF
)
OP2_CODE='def aggregateData(sources): result = {}; return result'
OP2_ID=$(create_operator "$TOKEN" \
    "Data Aggregation Operator" \
    "Aggregates data from multiple sources" \
    "$OP2_CODE" \
    "$OP2_PARAMS")

if [ "$OP2_ID" = "ERROR" ]; then
    log_error "Failed to create Operator 2"
else
    log_success "Operator 2 created with ID: $OP2_ID"
fi

# Operator 3: File processing operator
OP3_PARAMS=$(cat <<'EOF'
[
    {
        "name": "path",
        "description": "File path",
        "parameterType": "STRING",
        "ioType": "INPUT",
        "isRequired": true,
        "orderIndex": 1
    },
    {
        "name": "operation",
        "description": "Operation type",
        "parameterType": "STRING",
        "ioType": "INPUT",
        "isRequired": true,
        "orderIndex": 2
    },
    {
        "name": "result",
        "description": "Result message",
        "parameterType": "STRING",
        "ioType": "OUTPUT",
        "isRequired": true,
        "orderIndex": 3
    }
]
EOF
)
OP3_CODE='def processFile(filePath, operation): if operation == "read": return "Read file"; elif operation == "write": return "Write file"; else: return "Delete file"'
OP3_ID=$(create_operator "$TOKEN" \
    "File Processing Operator" \
    "Reads, transforms, and writes files" \
    "$OP3_CODE" \
    "$OP3_PARAMS")

if [ "$OP3_ID" = "ERROR" ]; then
    log_error "Failed to create Operator 3"
else
    log_success "Operator 3 created with ID: $OP3_ID"
fi

# Summary
log_info "=== Test Operators Created ==="
log_info "Operator 1 (String Processing): ID=$OP1_ID"
log_info "Operator 2 (Data Aggregation): ID=$OP2_ID"
log_info "Operator 3 (File Processing): ID=$OP3_ID"

log_success "All test operators prepared successfully!"

#!/bin/bash

# Authentication and Authorization tests
# @author Operator Manager Team

source "$(dirname "$0")/utils/logger.sh"
source "$(dirname "$0")/utils/assertions.sh"

# API Base URL
API_BASE="http://localhost:8080/api/v1"

# Test credentials
TEST_ADMIN="admin"
TEST_PASS="admin123"

# Test users
TEST_USER="testuser"
TEST_EMAIL="testuser@example.com"
TEST_USER_PASS="testpass123"

# Test counters
TEST_NUM=0
PASS_COUNT=0
FAIL_COUNT=0

# Export TOKEN for other tests
TOKEN_FILE="/tmp/test_auth_token.txt"

# Helper functions
login_and_get_token() {
    local username="$1"
    local password="$2"
    local login_json="{\"username\": \"$username\", \"password\": \"$password\"}"

    log_step "Authenticating: $username"

    local response=$(curl -s -X POST "$API_BASE/auth/login" \
        -H "Content-Type: application/json" \
        -d "$login_json")
    local success=$(echo "$response" | grep -o '"success":[a-z]*' | cut -d':' -f2)

    if [ "$success" = "true" ]; then
        local token=$(echo "$response" | grep -o '"accessToken":"[^"]*"' | cut -d'"' -f4)
        echo "$token" > "$TOKEN_FILE"
        log_success "Login successful"
        return 0
    else
        log_error "Login failed"
        return 1
    fi
}

# Test case: Valid credentials should get token and 200
test_auth_valid_credentials() {
    TEST_NUM=$((TEST_NUM + 1))
    log_step "TEST 01: Valid credentials ($TEST_ADMIN / $TEST_PASS)"

    local login_json="{\"username\": \"$TEST_ADMIN\", \"password\": \"$TEST_PASS\"}"
    local response=$(curl -s -X POST "$API_BASE/auth/login" \
        -H "Content-Type: application/json" \
        -d "$login_json")
    local success=$(echo "$response" | grep -o '"success":[a-z]*' | cut -d':' -f2)

    if [ "$success" = "true" ]; then
        local token=$(echo "$response" | grep -o '"accessToken":"[^"]*"' | cut -d'"' -f4)
        if [ -n "$token" ]; then
            echo "$token" > "$TOKEN_FILE"
            log_success "✓ Valid credentials login successful"
            PASS_COUNT=$((PASS_COUNT + 1))
        else
            log_error "✗ No token returned"
            FAIL_COUNT=$((FAIL_COUNT + 1))
        fi
    else
        log_error "✗ Valid credentials login failed"
        FAIL_COUNT=$((FAIL_COUNT + 1))
    fi
}

# Test case: Invalid username should get error
test_auth_invalid_username() {
    TEST_NUM=$((TEST_NUM + 1))
    log_step "TEST 02: Invalid username (empty)"

    local login_json="{\"username\": \"\", \"password\": \"$TEST_PASS\"}"
    local response=$(curl -s -X POST "$API_BASE/auth/login" \
        -H "Content-Type: application/json" \
        -d "$login_json")
    local success=$(echo "$response" | grep -o '"success":[a-z]*' | cut -d':' -f2)

    if [ "$success" = "false" ]; then
        log_success "✓ Invalid username returns error as expected"
        PASS_COUNT=$((PASS_COUNT + 1))
    else
        log_error "✗ Invalid username returned unexpected result"
        FAIL_COUNT=$((FAIL_COUNT + 1))
    fi
}

# Test case: Invalid password should get error
test_auth_invalid_password() {
    TEST_NUM=$((TEST_NUM + 1))
    log_step "TEST 03: Invalid password (empty)"

    local login_json="{\"username\": \"$TEST_ADMIN\", \"password\": \"\"}"
    local response=$(curl -s -X POST "$API_BASE/auth/login" \
        -H "Content-Type: application/json" \
        -d "$login_json")
    local success=$(echo "$response" | grep -o '"success":[a-z]*' | cut -d':' -f2)

    if [ "$success" = "false" ]; then
        log_success "✓ Invalid password returns error as expected"
        PASS_COUNT=$((PASS_COUNT + 1))
    else
        log_error "✗ Invalid password returned unexpected result"
        FAIL_COUNT=$((FAIL_COUNT + 1))
    fi
}

# Test case: Missing username should get error
test_auth_missing_username() {
    TEST_NUM=$((TEST_NUM + 1))
    log_step "TEST 04: Missing username"

    local login_json="{\"password\": \"$TEST_PASS\"}"
    local response=$(curl -s -X POST "$API_BASE/auth/login" \
        -H "Content-Type: application/json" \
        -d "$login_json")
    local success=$(echo "$response" | grep -o '"success":[a-z]*' | cut -d':' -f2)

    if [ "$success" = "false" ]; then
        log_success "✓ Missing username returns error as expected"
        PASS_COUNT=$((PASS_COUNT + 1))
    else
        log_error "✗ Missing username returned unexpected result"
        FAIL_COUNT=$((FAIL_COUNT + 1))
    fi
}

# Test case: Missing password should get error
test_auth_missing_password() {
    TEST_NUM=$((TEST_NUM + 1))
    log_step "TEST 05: Missing password"

    local login_json="{\"username\": \"$TEST_ADMIN\"}"
    local response=$(curl -s -X POST "$API_BASE/auth/login" \
        -H "Content-Type: application/json" \
        -d "$login_json")
    local success=$(echo "$response" | grep -o '"success":[a-z]*' | cut -d':' -f2)

    if [ "$success" = "false" ]; then
        log_success "✓ Missing password returns error as expected"
        PASS_COUNT=$((PASS_COUNT + 1))
    else
        log_error "✗ Missing password returned unexpected result"
        FAIL_COUNT=$((FAIL_COUNT + 1))
    fi
}

# Test case: Access protected endpoint without token should get 403
test_auth_protected_endpoint_no_token() {
    TEST_NUM=$((TEST_NUM + 1))
    log_step "TEST 06: Access protected endpoint without token"

    local http_code=$(curl -s -o /dev/null -w "%{http_code}" "$API_BASE/operators")

    if [ "$http_code" = "403" ] || [ "$http_code" = "401" ]; then
        log_success "✓ Protected endpoint without token returns $http_code"
        PASS_COUNT=$((PASS_COUNT + 1))
    else
        log_error "✗ Protected endpoint without token returned unexpected code: $http_code"
        FAIL_COUNT=$((FAIL_COUNT + 1))
    fi
}

# Test case: Access protected endpoint with valid token should get 200
test_auth_protected_endpoint_with_token() {
    TEST_NUM=$((TEST_NUM + 1))
    log_step "TEST 07: Access protected endpoint with valid token"

    local TOKEN=$(cat "$TOKEN_FILE" 2>/dev/null)

    if [ -z "$TOKEN" ]; then
        log_error "✗ No token available, skipping test"
        FAIL_COUNT=$((FAIL_COUNT + 1))
        return
    fi

    local response=$(curl -s -X GET "$API_BASE/operators" \
        -H "Authorization: Bearer $TOKEN")
    local success=$(echo "$response" | grep -o '"success":[a-z]*' | cut -d':' -f2)

    if [ "$success" = "true" ]; then
        log_success "✓ Protected endpoint with valid token returns success"
        PASS_COUNT=$((PASS_COUNT + 1))
    else
        log_error "✗ Protected endpoint with token returned error"
        FAIL_COUNT=$((FAIL_COUNT + 1))
    fi
}

# Test case: Invalid token should get 403
test_auth_invalid_token() {
    TEST_NUM=$((TEST_NUM + 1))
    log_step "TEST 08: Access with invalid token"

    local INVALID_TOKEN="invalid.token.here"

    local http_code=$(curl -s -o /dev/null -w "%{http_code}" "$API_BASE/operators" \
        -H "Authorization: Bearer $INVALID_TOKEN")

    if [ "$http_code" = "403" ] || [ "$http_code" = "401" ]; then
        log_success "✓ Invalid token returns $http_code as expected"
        PASS_COUNT=$((PASS_COUNT + 1))
    else
        log_error "✗ Invalid token returned unexpected code: $http_code"
        FAIL_COUNT=$((FAIL_COUNT + 1))
    fi
}

# Main execution
main() {
    log_info "Starting authentication tests"
    log_info "API Base: $API_BASE"

    # Execute tests
    test_auth_valid_credentials
    test_auth_invalid_username
    test_auth_invalid_password
    test_auth_missing_username
    test_auth_missing_password
    test_auth_protected_endpoint_no_token
    test_auth_protected_endpoint_with_token
    test_auth_invalid_token

    # Summary
    log_step "Authentication tests completed"
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

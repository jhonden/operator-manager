#!/bin/bash

# Prepare test data
# @author Operator Manager Team

# API Base URL
API_BASE="http://localhost:8080/api/v1"

# Test credentials
TEST_ADMIN="admin"
TEST_PASS="admin123"

# Test user
TEST_USER="testuser"
TEST_EMAIL="testuser@example.com"
TEST_USER_PASS="testpass123"

# Test category IDs
CATEGORY_DATA_PROCESSING=1
CATEGORY_ID=1
CATEGORY_ID_DATA_TRANSFORMATION=2

# Logging
source "$(dirname "$0")/utils/logger.sh"

# Get admin token
get_admin_token() {
    local login_json="{\"username\": \"$TEST_ADMIN\", \"password\": \"$TEST_PASS\"}"
    local response=$(curl -s -X POST "$API_BASE/auth/login" \
        -H "Content-Type: application/json" \
        -d "$login_json")
    local token=$(echo "$response" | grep -o '"accessToken":"[^"]*"' | cut -d'"' -f4)
    echo "$token"
}

# Functions
cleanup_database() {
    log_step "Cleaning database"
    local TOKEN=$(get_admin_token)
    local http_code=$(curl -s -o /dev/null -w "%{http_code}" -X DELETE "$API_BASE/operators/all" \
        -H "Authorization: Bearer $TOKEN")

    if [ "$http_code" = "200" ] || [ "$http_code" = "204" ]; then
        log_success "Database cleaned"
    else
        log_error "Failed to clean database: HTTP $http_code"
    fi
}

create_test_user() {
    log_step "Creating test user: $TEST_USER"
    local TOKEN=$(get_admin_token)

    local user_json="{
        \"username\": \"$TEST_USER\",
        \"email\": \"$TEST_EMAIL\",
        \"password\": \"$TEST_USER_PASS\",
        \"fullName\": \"Test User\",
        \"role\": \"USER\",
        \"status\": \"ACTIVE\"
    }"

    local response=$(curl -s -X POST "$API_BASE/users" \
        -H "Content-Type: application/json" \
        -H "Authorization: Bearer $TOKEN" \
        -d "$user_json")
    local success=$(echo "$response" | grep -o '"success":[a-z]*' | cut -d':' -f2)

    if [ "$success" = "true" ]; then
        local user_id=$(echo "$response" | grep -o '"id":[0-9]*' | cut -d':' -f2 | head -1)
        echo "$user_id" > /tmp/test_user_id.txt
        log_success "Test user created with ID: $user_id"
        return 0
    else
        log_error "Failed to create test user"
        return 1
    fi
}

delete_test_user() {
    log_step "Deleting test user: $TEST_USER"
    local TOKEN=$(get_admin_token)
    local user_id=$(cat /tmp/test_user_id.txt 2>/dev/null)

    if [ -z "$user_id" ]; then
        log_error "No test user ID found"
        return 1
    fi

    local http_code=$(curl -s -o /dev/null -w "%{http_code}" -X DELETE "$API_BASE/users/$user_id" \
        -H "Authorization: Bearer $TOKEN")

    if [ "$http_code" = "200" ] || [ "$http_code" = "204" ]; then
        log_success "Test user deleted: $user_id"
        rm -f /tmp/test_user_id.txt
        return 0
    else
        log_error "Failed to delete test user: HTTP $http_code"
        return 1
    fi
}

create_test_categories() {
    log_step "Creating test categories"
    local TOKEN=$(get_admin_token)

    # Delete existing test categories if they exist
    for id in $CATEGORY_ID $CATEGORY_ID_DATA_TRANSFORMATION; do
        local category="category_$id"
        curl -s -X DELETE "$API_BASE/categories/$category" \
            -H "Authorization: Bearer $TOKEN" 2>/dev/null
    done

    # Create test categories
    local categories_json="[
        {\"name\": \"Category $CATEGORY_ID\", \"description\": \"Test Category for $CATEGORY_ID\", \"parentId\": null}
    ]"

    local response=$(curl -s -X POST "$API_BASE/categories" \
        -H "Content-Type: application/json" \
        -H "Authorization: Bearer $TOKEN" \
        -d "$categories_json")
    local success=$(echo "$response" | grep -o '"success":[a-z]*' | cut -d':' -f2)

    if [ "$success" = "true" ]; then
        log_success "Test categories created"
        return 0
    else
        log_error "Failed to create test categories"
        return 1
    fi
}

# Export IDs for tests
echo "$CATEGORY_ID" > /tmp/test_category_id.txt
echo "$CATEGORY_ID_DATA_TRANSFORMATION" > /tmp/test_category_id_data_transformation.txt

# Main execution
main() {
    log_info "Starting test data preparation"

    cleanup_database
    create_test_user
    create_test_categories

    log_info "Test data preparation completed"
}

# Run main function
main

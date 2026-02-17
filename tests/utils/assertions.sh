#!/bin/bash

# Assertions for test cases
# @author Operator Manager Team

# Color codes
RED='\033[0m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

# Test result storage
TEST_STATUS="unknown"
TEST_ERROR_MSG=""
TEST_RESPONSE=""

# Assert functions
assert_eq() {
    local expected="$1"
    local actual="$2"
    local message="${3:-Expected $expected, got $actual}"

    if [ "$expected" != "$actual" ]; then
        log_error "$message"
        return 1
    fi
    return 0
}

assert_ne() {
    local not_expected="$1"
    local actual="$2"
    local message="${3:-Expected NOT $not_expected, got $actual}"

    if [ "$not_expected" = "$actual" ]; then
        log_error "$message"
        return 1
    fi
    return 0
}

assert_gt() {
    local value="$1"
    local threshold="$2"
    local message="${3:-Expected $value > $threshold}"

    if [ "$value" -le "$threshold" ]; then
        log_error "$message"
        return 1
    fi
    return 0
}

assert_ge() {
    local value="$1"
    local threshold="$2"
    local message="${3:-Expected $value >= $threshold}"

    if [ "$value" -lt "$threshold" ]; then
        log_error "$message"
        return 1
    fi
    return 0
}

assert_has_field() {
    local json="$1"
    local field="$2"
    local message="${3:-Response should contain $field}"

    if echo "$json" | jq -e ".$field" > /dev/null 2>&1; then
        return 0
    fi

    log_error "$message"
    TEST_STATUS="FAILED"
    TEST_ERROR_MSG="$message"
    return 1
}

assert_not_empty() {
    local value="$1"
    local message="${3:-Value should not be empty}"

    if [ -z "$value" ]; then
        log_error "$message"
        TEST_STATUS="FAILED"
        TEST_ERROR_MSG="$message"
        return 1
    fi
    return 0
}

# Helper function to exit with status
exit_with_status() {
    if [ "$TEST_STATUS" = "FAILED" ]; then
        exit 1
    else
        exit 0
    fi
}


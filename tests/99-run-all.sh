#!/bin/bash

# Master test runner
# Executes all test scripts in sequence and provides a comprehensive test report
# @author Operator Manager Team

# Color codes
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
NC='\033[0m'

# Test configuration
TESTS_DIR="$(dirname "$0")"
LOG_DIR="$TESTS_DIR/logs"
TIMESTAMP=$(date +"%Y%m%d_%H%M%S")
LOG_FILE="$LOG_DIR/test_run_$TIMESTAMP.log"

# Overall test results
TOTAL_PASS=0
TOTAL_FAIL=0
TOTAL_TESTS=0

# Test suite results (stored as plain text files for compatibility)
SUITE_RESULTS_DIR="$TESTS_DIR/.suite_results"
mkdir -p "$SUITE_RESULTS_DIR"

# Ensure log directory exists
mkdir -p "$LOG_DIR"

# Logging functions
print_header() {
    echo -e "${CYAN}========================================${NC}"
    echo -e "${CYAN}$1${NC}"
    echo -e "${CYAN}========================================${NC}"
}

print_success() {
    echo -e "${GREEN}✓ $1${NC}"
    echo "[SUCCESS] $1" >> "$LOG_FILE"
}

print_error() {
    echo -e "${RED}✗ $1${NC}"
    echo "[ERROR] $1" >> "$LOG_FILE"
}

print_info() {
    echo -e "${BLUE}ℹ $1${NC}"
    echo "[INFO] $1" >> "$LOG_FILE"
}

print_step() {
    echo -e "${YELLOW}▶ $1${NC}"
    echo "[STEP] $1" >> "$LOG_FILE"
}

# Store suite result
store_suite_result() {
    local suite_name="$1"
    local exit_code="$2"
    echo "$exit_code" > "$SUITE_RESULTS_DIR/${suite_name}.txt"
}

# Get suite result
get_suite_result() {
    local suite_name="$1"
    local result_file="$SUITE_RESULTS_DIR/${suite_name}.txt"
    if [ -f "$result_file" ]; then
        cat "$result_file"
    fi
}

# Run a single test suite
run_test_suite() {
    local test_name="$1"
    local test_script="$2"

    print_header "Running: $test_name"
    print_step "Script: $test_script"
    echo "" | tee -a "$LOG_FILE"

    # Make sure the script is executable
    chmod +x "$test_script"

    # Run the test script and capture output
    local start_time=$(date +%s)
    local exit_code=0

    if bash "$test_script" 2>&1 | tee -a "$LOG_FILE"; then
        local exit_code=${PIPESTATUS[0]}
    else
        local exit_code=$?
    fi

    local end_time=$(date +%s)
    local duration=$((end_time - start_time))

    echo "" | tee -a "$LOG_FILE"

    if [ $exit_code -eq 0 ]; then
        print_success "$test_name PASSED (${duration}s)"
        store_suite_result "$test_name" "0"
        TOTAL_PASS=$((TOTAL_PASS + 1))
    else
        print_error "$test_name FAILED (${duration}s)"
        store_suite_result "$test_name" "$exit_code"
        TOTAL_FAIL=$((TOTAL_FAIL + 1))
    fi

    TOTAL_TESTS=$((TOTAL_TESTS + 1))

    echo "" | tee -a "$LOG_FILE"
}

# Check if backend is running
check_backend() {
    print_step "Checking if backend is running..."
    local success=$(curl -s -X POST http://localhost:8080/api/v1/auth/login \
        -H 'Content-Type: application/json' \
        -d '{"username":"admin","password":"admin123"}' 2>/dev/null | grep -o '"success":[a-z]*' | cut -d':' -f2)

    if [ "$success" = "true" ]; then
        print_success "Backend is running"
        return 0
    else
        print_error "Backend is not running (login failed)"
        print_info "Please start the backend before running tests"
        return 1
    fi
}

# Print final summary
print_summary() {
    print_header "TEST SUMMARY"
    echo ""

    # Print suite-by-suite results
    echo -e "${CYAN}Test Suite Results:${NC}"
    for result_file in "$SUITE_RESULTS_DIR"/*.txt; do
        if [ -f "$result_file" ]; then
            local suite_name=$(basename "$result_file" .txt)
            local exit_code=$(cat "$result_file")
            if [ "$exit_code" = "0" ]; then
                echo -e "  ${GREEN}✓ $suite_name: PASSED${NC}"
            else
                echo -e "  ${RED}✗ $suite_name: FAILED (exit code: $exit_code)${NC}"
            fi
        fi
    done
    echo ""

    # Print overall statistics
    echo -e "${CYAN}Overall Statistics:${NC}"
    echo -e "  Total Test Suites: $TOTAL_TESTS"
    echo -e "  ${GREEN}Passed: $TOTAL_PASS${NC}"
    echo -e "  ${RED}Failed: $TOTAL_FAIL${NC}"
    echo ""

    # Print success rate
    if [ $TOTAL_TESTS -gt 0 ]; then
        local success_rate=$((TOTAL_PASS * 100 / TOTAL_TESTS))
        echo -e "${CYAN}Success Rate: $success_rate%${NC}"
    fi
    echo ""

    # Print log file location
    echo -e "${CYAN}Detailed logs: $LOG_FILE${NC}"
    echo ""

    # Print final result
    if [ $TOTAL_FAIL -eq 0 ]; then
        print_success "ALL TESTS PASSED! ✓"
        return 0
    else
        print_error "SOME TESTS FAILED! ✗"
        return 1
    fi
}

# Main execution
main() {
    print_header "OPERATOR MANAGER TEST SUITE"
    print_info "Started at: $(date)"
    print_info "Log file: $LOG_FILE"
    echo "" | tee -a "$LOG_FILE"

    # Check backend
    if ! check_backend; then
        print_error "Cannot proceed without backend running"
        exit 1
    fi

    echo "" | tee -a "$LOG_FILE"

    # Define test suites
    declare -a TEST_SUITES=(
        "Data Preparation:$TESTS_DIR/01-prepare-data.sh"
        "Authentication Tests:$TESTS_DIR/02-auth-test.sh"
        "Operator CRUD Tests:$TESTS_DIR/03-operator-crud-test.sh"
    )

    # Run all test suites
    for suite_entry in "${TEST_SUITES[@]}"; do
        local suite_name="${suite_entry%%:*}"
        local suite_script="${suite_entry##*:}"

        if [ -f "$suite_script" ]; then
            run_test_suite "$suite_name" "$suite_script"
        else
            print_error "Test script not found: $suite_script"
            TOTAL_FAIL=$((TOTAL_FAIL + 1))
        fi
    done

    # Print summary
    print_summary

    # Exit with appropriate code
    if [ $TOTAL_FAIL -eq 0 ]; then
        exit 0
    else
        exit 1
    fi
}

# Run main function
main

# Operator Manager Test Framework

Comprehensive automated testing suite for Operator Manager REST API using shell scripts.

## Overview

This test framework provides automated regression testing for all core REST API endpoints. It's designed to be run after each code change to catch regressions early and ensure system stability.

## Test Suites

### 1. Data Preparation (`01-prepare-data.sh`)
- Cleans database before tests
- Creates test user accounts
- Creates test categories
- Export IDs for use in subsequent tests

### 2. Authentication Tests (`02-auth-test.sh`)
- Valid credentials login
- Invalid username/password handling
- Missing username/password validation
- Protected endpoint access control
- Token validation (valid, invalid, expired)
- User creation and deletion

### 3. Operator CRUD Tests (`03-operator-crud-test.sh`)
- Create operator
- Create operator with parameters
- Get operator by ID
- Get operator with parameters verification
- Update operator
- List operators with pagination
- Search operators by name/status
- Delete operator

## Test Utilities

### Logger (`utils/logger.sh`)
Colored logging functions:
- `log_info()` - General information
- `log_success()` - Success messages with checkmark
- `log_error()` - Error messages
- `log_step()` - Step indicators
- `log_data()` - Data display

### Assertions (`utils/assertions.sh`)
Test assertion functions:
- `assert_eq()` - Equality check
- `assert_ne()` - Inequality check
- `assert_gt()` - Greater than check
- `assert_ge()` - Greater or equal check
- `assert_has_field()` - JSON field check
- `assert_not_empty()` - Non-empty check

## Quick Start

### 1. Start the Backend

```bash
cd /path/to/operator-manager/operator-api
mvn spring-boot:run
```

Wait for the application to start (you should see "Started OperatorApiApplication").

### 2. Run All Tests

```bash
cd /path/to/operator-manager/tests
./99-run-all.sh
```

### 3. Run Individual Test Suites

```bash
# Prepare test data
./01-prepare-data.sh

# Run authentication tests
./02-auth-test.sh

# Run operator CRUD tests
./03-operator-crud-test.sh
```

## Test Results

Test results are logged to:
- Console output (colored)
- Log file: `tests/logs/test_run_<timestamp>.log`

The master test runner (`99-run-all.sh`) provides:
- Test suite-by-suite results
- Overall statistics (passed/failed)
- Success rate percentage
- Log file location

## Example Output

```
========================================
Running: Authentication Tests
========================================
[STEP] Script: /path/to/tests/02-auth-test.sh

[STEP] TEST 01: Valid credentials (admin / admin123)
[SUCCESS] ✓ Valid credentials login successful

[STEP] TEST 02: Invalid username (empty)
[SUCCESS] ✓ Invalid username returns error as expected

...

========================================
TEST SUMMARY
========================================

Test Suite Results:
  ✓ Data Preparation: PASSED
  ✓ Authentication Tests: PASSED
  ✓ Operator CRUD Tests: PASSED

Overall Statistics:
  Total Test Suites: 3
  Passed: 3
  Failed: 0

Success Rate: 100%

Detailed logs: /path/to/tests/logs/test_run_20260216_143022.log

[SUCCESS] ALL TESTS PASSED! ✓
```

## Configuration

### API Endpoint

Default API base URL: `http://localhost:8080/api/v1`

To change, edit the `API_BASE` variable in individual test scripts.

### Test Credentials

Default admin credentials:
- Username: `admin`
- Password: `admin123`

These are defined in each test script.

## Adding New Tests

### 1. Create a new test script

```bash
#!/bin/bash

source "$(dirname "$0")/utils/logger.sh"
source "$(dirname "$0")/utils/assertions.sh"

API_BASE="http://localhost:8080/api/v1"

TEST_NUM=0
PASS_COUNT=0
FAIL_COUNT=0

test_your_feature() {
    TEST_NUM=$((TEST_NUM + 1))
    log_step "TEST 01: Your feature description"

    # Your test logic here
    # Use curl to call API
    # Use assertions to verify results

    if [ condition ]; then
        log_success "✓ Test passed"
        PASS_COUNT=$((PASS_COUNT + 1))
    else
        log_error "✗ Test failed"
        FAIL_COUNT=$((FAIL_COUNT + 1))
    fi
}

main() {
    log_info "Starting your tests"

    # Execute your test functions
    test_your_feature

    # Summary
    log_step "Tests completed"
    log_info "Passed: $PASS_COUNT"
    log_info "Failed: $FAIL_COUNT"

    if [ "$FAIL_COUNT" -gt 0 ]; then
        exit 1
    else
        exit 0
    fi
}

main
```

### 2. Add to master runner

Edit `99-run-all.sh` and add your test to the `TEST_SUITES` array:

```bash
declare -a TEST_SUITES=(
    "Data Preparation:$TESTS_DIR/01-prepare-data.sh"
    "Authentication Tests:$TESTS_DIR/02-auth-test.sh"
    "Operator CRUD Tests:$TESTS_DIR/03-operator-crud-test.sh"
    "Your New Test:$TESTS_DIR/04-your-test.sh"  # Add this line
)
```

### 3. Make executable

```bash
chmod +x /path/to/tests/04-your-test.sh
```

## Troubleshooting

### Backend not running
```
[ERROR] Backend is not running (HTTP 000)
```
Start the backend first: `mvn spring-boot:run` from `operator-api` directory.

### Permission denied
```
bash: ./99-run-all.sh: Permission denied
```
Run: `chmod +x ./99-run-all.sh`

### Test scripts not found
```
[ERROR] Test script not found: /path/to/tests/XX-test.sh
```
Check that the file exists and the path is correct in `99-run-all.sh`.

### Connection refused
```
[ERROR] Connection refused on port 8080
```
Verify the backend is running on port 8080. Check the port in `application.properties`.

## Continuous Integration

This test framework is designed to be integrated with CI/CD pipelines:

```yaml
# Example GitHub Actions workflow
name: Run Tests

on: [push, pull_request]

jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v2
      - name: Set up JDK 21
        uses: actions/setup-java@v2
        with:
          java-version: '21'
      - name: Start backend
        run: |
          cd operator-api
          mvn clean install -DskipTests
          mvn spring-boot:run &
          sleep 30
      - name: Run tests
        run: |
          cd tests
          ./99-run-all.sh
```

## Contributing

When adding new features:
1. Write corresponding test cases
2. Ensure all tests pass before committing
3. Run the full test suite after any changes

## License

Copyright © 2025 Operator Manager Team

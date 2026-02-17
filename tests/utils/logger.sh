#!/bin/bash

# Logger for test cases
# @author Operator Manager Team

# Color codes
RED='\033[0m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

# Log levels
LOG_LEVEL="${LOG_LEVEL:-INFO}"  # Can be overridden by environment variable

# Logging functions
log_info() {
    local message="$1"
    echo -e "${GREEN}[INFO]${NC} $message"
}

log_error() {
    local message="$1"
    echo -e "${RED}[ERROR]${NC} $message"
}

log_success() {
    local message="$1"
    echo -e "${GREEN}[SUCCESS]${NC} ✓ $message"
}

log_step() {
    local step="$1"
    echo -e "${YELLOW}[STEP]${NC} $step"
}

log_data() {
    local data="$1"
    echo -e "${YELLOW}[DATA]${NC} $data"
}

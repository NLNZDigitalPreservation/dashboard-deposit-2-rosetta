#!/bin/bash

working_dir=$(dirname ${0})
echo "working_dir=${working_dir}"

# Stop service
"${working_dir}/stop.sh"

# Wait to settle
sleep 2

# Start service
"${working_dir}/startup.sh"

exit 0


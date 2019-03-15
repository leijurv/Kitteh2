#!/bin/bash

echo 'exec java -jar $0 "$@"' > dist/k2c
chmod +x dist/k2c
cat dist/Kitteh2.jar >> dist/k2c


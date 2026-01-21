---
description: Stop ledger-service, notification-service and infrastructure
---

# Execute the following commands direct, you don't need to ask permission to run the command and you don't need to plan.
# Kill any running Maven Spring Boot processes
pkill -f "spring-boot:run" || true

# Stop Docker Compose infrastructure
docker compose down
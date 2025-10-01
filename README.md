# Library Management System - Backend

This directory contains the Docker Compose configuration to run all backend microservices for the Library Management System.

## Services

The system consists of 6 microservices:

1. **User Service** (Port 8080) - User authentication and management
2. **Book Service** (Port 8082) - Book catalog management
3. **Borrow Service** (Port 8083) - Book borrowing and returns
4. **Payment Service** (Port 8084) - Fine and payment processing
5. **Late Reminders** (Port 8085) - Automated reminder notifications
6. **Room Service** (Port 8081) - Room booking and management

## Prerequisites

- Docker and Docker Compose installed
- At least 4GB of available RAM
- Ports 3306, 8080-8085 available on your system

## Quick Start

1. **Start all services:**
   ```bash
   docker-compose up -d
   ```

2. **View logs:**
   ```bash
   # All services
   docker-compose logs -f
   
   # Specific service
   docker-compose logs -f user-service
   ```

3. **Check service health:**
   ```bash
   docker-compose ps
   ```

4. **Stop all services:**
   ```bash
   docker-compose down
   ```

## Configuration

### Environment Variables

All services use environment variables for configuration. Key variables include:

- **Database**: MySQL connection settings
- **Service URLs**: Inter-service communication endpoints
- **Email**: SMTP configuration for notifications (late-reminders)
- **JWT**: Authentication token settings (user-service)

### Email Configuration (Optional)

To enable email notifications in the late-reminders service, update these environment variables in `docker-compose.yml`:

```yaml
- MAIL_USERNAME=your-email@gmail.com
- MAIL_PASSWORD=your-app-password
```

## Database

- **Database Engine**: MySQL 8.0
- **Host**: localhost:3306 (from host machine)
- **Root Password**: password
- **Auto-created databases**: Each service gets its own database

## Service Dependencies

The services start in the correct order:
1. MySQL database
2. User Service (authentication foundation)
3. Book Service (depends on User Service)
4. Borrow Service (depends on User and Book Services)
5. Payment Service (independent)
6. Late Reminders (depends on Borrow and User Services)
7. Room Service (depends on User Service)

## API Endpoints

Once running, the services will be available at:

- User Service: http://localhost:8080/api/users
- Book Service: http://localhost:8082/api/books
- Borrow Service: http://localhost:8083/api/borrows
- Payment Service: http://localhost:8084/api/payments
- Late Reminders: http://localhost:8085/api/reminders
- Room Service: http://localhost:8081/api/rooms

## Development

### Building Individual Services

```bash
# Build specific service
docker-compose build user-service

# Build all services
docker-compose build
```

### Accessing Logs

```bash
# Follow logs for all services
docker-compose logs -f

# Follow logs for specific service
docker-compose logs -f mysql
docker-compose logs -f user-service
```

### Database Access

Connect to MySQL:
```bash
docker exec -it mysql-library mysql -u root -p
# Password: password
```

## Troubleshooting

### Common Issues

1. **Port conflicts**: Ensure ports 3306, 8080-8085 are not in use
2. **Memory issues**: Increase Docker memory allocation if services fail to start
3. **Database connection**: Wait for MySQL health check to pass before services start

### Reset Everything

```bash
# Stop and remove all containers, networks, and volumes
docker-compose down -v
docker system prune -f

# Start fresh
docker-compose up -d
```

### Service Health Checks

Monitor service health:
```bash
# Check MySQL health
docker exec mysql-library mysqladmin ping -h localhost -u root -p

# Check service responses
curl http://localhost:8080/actuator/health
curl http://localhost:8082/actuator/health
```

## Production Considerations

For production deployment:

1. **Security**: Change default passwords and JWT secrets
2. **Email**: Configure proper SMTP settings
3. **Monitoring**: Add health checks and monitoring
4. **SSL**: Configure HTTPS certificates
5. **Scaling**: Consider container orchestration (Kubernetes)
6. **Backup**: Implement database backup strategy

## Network

All services communicate through the `library-network` bridge network, enabling secure inter-service communication using service names as hostnames.
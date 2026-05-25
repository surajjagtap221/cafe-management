#  Cafe Management System on AWS EC2

Complete step-by-step deployment guide for running the Cafe Management System using:

- AWS EC2
- Amazon Linux 2
- Docker & Docker Compose
- Spring Boot Backend
- Angular Frontend
- MySQL 5.7
- Redis
- Nginx Reverse Proxy

---

#  Project Architecture

```text
   User Browser
       │
       ▼
┌─────────────────────┐
│  AWS EC2 Instance   │
│  Amazon Linux 2     │
└─────────────────────┘
       │
       ▼
┌─────────────────────┐
│ Angular + Nginx     │
│ cafe-app-frontend   │
│ Port: 80            │
└─────────────────────┘
       │
       ▼
┌─────────────────────┐
│ Spring Boot Backend │
│ cafe-app-backend    │
│ Port: 8081          │
└─────────────────────┘
       │
 ┌─────┴─────┐
 ▼           ▼
MySQL       Redis
3306        6379
```

---

#   Containers Used

| Container | Purpose | Port |
|---|---|---|
| cafe-app-frontend | Angular + Nginx | 80 |
| cafe-app-backend | Spring Boot API | 8081 |
| mysql | MySQL Database | 3306 |
| redis | Redis Cache | 6379 |

---

#   Phase 1 — AWS EC2 Setup

##   Launch EC2 Instance

### Recommended Configuration

| Setting | Value |
|---|---|
| AMI | Amazon Linux 2 |
| Instance Type | t2.medium |
| Storage | 20 GB |
| Security Group | cafe-sg |

>   Do NOT use t2.micro. Angular + Maven builds require more RAM.

---

##   Configure Security Group

Open only these ports:

| Type | Port | Purpose |
|---|---|---|
| SSH | 22 | SSH Access |
| HTTP | 80 | Web App Access |

---

##   Connect to EC2

### Mac / Linux

```bash
chmod 400 ~/Downloads/cafe-key.pem

ssh -i ~/Downloads/cafe-key.pem ec2-user@YOUR_EC2_PUBLIC_IP
```

### Windows PowerShell

```powershell
ssh -i C:\Users\YourName\Downloads\cafe-key.pem ec2-user@YOUR_EC2_PUBLIC_IP
```

---

#   Phase 2 — Install Docker

##   Install Docker

```bash
sudo yum update -y

sudo yum install docker -y

sudo systemctl start docker
sudo systemctl enable docker

sudo usermod -aG docker ec2-user
```

Logout and login again.

---

## Install Docker Compose

```bash
sudo curl -L "https://github.com/docker/compose/releases/latest/download/docker-compose-linux-x86_64" \
-o /usr/local/bin/docker-compose

sudo chmod +x /usr/local/bin/docker-compose
```

Verify:

```bash
docker --version
docker compose version
```

---

#   Phase 3 — Upload Project Files

##   Upload Files using SCP

### Upload Backend

```bash
scp -i ~/Downloads/cafe-key.pem \
Cafe-Management-System-BE-main.zip \
ec2-user@YOUR_EC2_PUBLIC_IP:~/
```

### Upload Frontend

```bash
scp -i ~/Downloads/cafe-key.pem \
Cafe-Management-System-FE-main.zip \
ec2-user@YOUR_EC2_PUBLIC_IP:~/
```

---

## Extract Files

```bash
sudo yum install unzip -y

mkdir -p ~/cafe-management
cd ~/cafe-management

unzip ~/Cafe-Management-System-BE-main.zip -d /tmp/
mv /tmp/Cafe-Management-System-BE-main backend

unzip ~/Cafe-Management-System-FE-main.zip -d /tmp/
mv /tmp/Cafe-Management-System-FE-main frontend
```

---

#   Phase 4 — Source Code Fixes

## Fix CafeConstants.java

File:

```text
backend/src/main/java/com/in/cafe/constants/CafeConstants.java
```

Replace:

```java
public static final String STORE_LOCATION = "/app/cafe-stored-files";
```

---

## Fix BillServiceImpl.java

```bash
sed -i 's|STORE_LOCATION + "\\\\" +|STORE_LOCATION + "/" +|g' \
~/cafe-management/backend/src/main/java/com/in/cafe/serviceImpl/BillServiceImpl.java
```

---

## Fix environment.ts

### environment.ts

```ts
export const environment = {
  production: false,
  apiUrl: ''
};
```

### environment.prod.ts

```ts
export const environment = {
  production: true,
  apiUrl: ''
};
```

---

## Fix Redis Host

```bash
sed -i 's/spring.data.redis.host=localhost/spring.data.redis.host=redis/' \
~/cafe-management/backend/src/main/resources/application.properties
```

---

#  Phase 5 — Docker Configuration

## Backend Dockerfile

Create:

```text
backend/Dockerfile
```

```dockerfile
FROM maven:3.9.6-eclipse-temurin-17 AS build

WORKDIR /app

COPY . .

RUN mvn clean package -DskipTests

FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

RUN mkdir -p /app/cafe-stored-files

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8081

ENTRYPOINT ["java", "-jar", "app.jar"]
```

---

## Frontend Dockerfile

Create:

```text
frontend/Dockerfile
```

```dockerfile
FROM node:16-alpine AS build

WORKDIR /app

COPY package*.json ./

RUN npm install --legacy-peer-deps

COPY . .

RUN npm run build -- --configuration=production

FROM nginx:alpine

COPY --from=build /app/dist/frontend /usr/share/nginx/html

COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
```

---

## Nginx Configuration

Create:

```text
frontend/nginx.conf
```

```nginx
server {
    listen 80;

    root /usr/share/nginx/html;
    index index.html;

    location / {
        try_files $uri $uri/ /index.html;
    }

    location ~ ^/(user|category|product|bill|dashboard|password)/ {

        proxy_pass http://cafe-app-backend:8081;

        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;

        client_max_body_size 10M;
    }
}
```

---

#  docker-compose.yml

Create:

```text
docker-compose.yml
```

```yaml
version: '3.8'

services:

  mysql:
    image: mysql:5.7
    container_name: mysql
    restart: always

    environment:
      MYSQL_ROOT_PASSWORD: ${DB_ROOT_PASS}
      MYSQL_DATABASE: cafe_db
      MYSQL_USER: ${DB_USER}
      MYSQL_PASSWORD: ${DB_PASS}

    volumes:
      - mysql_data:/var/lib/mysql

    networks:
      - cafe_network

  redis:
    image: redis:7-alpine
    container_name: redis
    restart: always

    networks:
      - cafe_network

  cafe-app-backend:
    build:
      context: ./backend

    container_name: cafe-app-backend

    restart: always

    environment:
      DB_URL: jdbc:mysql://mysql:3306/cafe_db?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true
      DB_USER: ${DB_USER}
      DB_PASS: ${DB_PASS}

      MAIL_USER: ${MAIL_USER}
      APP_PASS: ${APP_PASS}

      ALLOWED_ORIGIN: ${ALLOWED_ORIGIN}
      FRONTEND_URL: ${FRONTEND_URL}

    volumes:
      - cafe_bills:/app/cafe-stored-files

    depends_on:
      - mysql
      - redis

    networks:
      - cafe_network

  cafe-app-frontend:
    build:
      context: ./frontend

    container_name: cafe-app-frontend

    restart: always

    ports:
      - "80:80"

    depends_on:
      - cafe-app-backend

    networks:
      - cafe_network

networks:
  cafe_network:
    driver: bridge

volumes:
  mysql_data:
  cafe_bills:
```

---

#  Create .env File

Create:

```text
.env
```

```env
DB_ROOT_PASS=rootStrongPass123!
DB_USER=cafeuser
DB_PASS=cafePass456!

MAIL_USER=your.email@gmail.com
APP_PASS=your_gmail_app_password

ALLOWED_ORIGIN=http://YOUR_EC2_PUBLIC_IP
FRONTEND_URL=http://YOUR_EC2_PUBLIC_IP
```

---

#  Build & Run

## Build Containers

```bash
cd ~/cafe-management

docker compose up --build -d
```

---

## Check Running Containers

```bash
docker compose ps
```

Expected:

```text
cafe-app-backend    Up
cafe-app-frontend   Up
mysql               Up
redis               Up
```

---

## View Logs

```bash
docker compose logs -f
```

---

#   Access the Application

Open:

```text
http://YOUR_EC2_PUBLIC_IP
```

---

#  First-Time Admin Setup

## Open MySQL

```bash
docker exec -it mysql mysql -u cafeuser -pcafePass456! cafe_db
```

---

## Make User Admin

```sql
UPDATE user
SET role='admin', status='true'
WHERE email='your.email@gmail.com';
```

---

#  Useful Docker Commands

## Start Containers

```bash
docker compose up -d
```

---

## Stop Containers

```bash
docker compose down
```

---

## Restart Backend

```bash
docker compose restart cafe-app-backend
```

---

## View Logs

```bash
docker compose logs -f
```

---

## Clean Docker Space

```bash
docker system prune -f
```

---

#   Troubleshooting

## Backend Restarting

```bash
docker compose logs cafe-app-backend
```

---

## Port 80 Already Used

```bash
sudo ss -tlnp | grep :80
```

Stop Apache:

```bash
sudo systemctl stop httpd
sudo systemctl disable httpd
```

---

## Out of Memory

Create swap:

```bash
sudo dd if=/dev/zero of=/swapfile bs=128M count=16

sudo chmod 600 /swapfile

sudo mkswap /swapfile

sudo swapon /swapfile
```

---

#  Deployment Complete

Your Cafe Management System is now running on AWS EC2 using Docker.

Access it from:

```text
http://YOUR_EC2_PUBLIC_IP
```

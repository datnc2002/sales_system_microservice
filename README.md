Hệ thống E-Commerce Microservices được xây dựng dựa trên kiến trúc phân tán (Distributed Architecture), sử dụng Spring Boot cho Backend, React (Vite) cho Frontend và được thiết kế để triển khai toàn diện trên môi trường Kubernetes.

## Kiến trúc hệ thống
- **Frontend:** React.js (Vite)
- **API Gateway:** Spring Cloud Gateway (Xử lý định tuyến và xác thực JWT)
- **Microservices:** User, Product, Order, Inventory, Notification
- **Database:** PostgreSQL (Chia schema độc lập cho từng service)
- **Message Broker:** Apache Kafka & Zookeeper (Giao tiếp bất đồng bộ, xử lý Order/Inventory)
- **Caching:** Redis

---

## Hướng dẫn Triển khai (Step-by-Step)

### Yêu cầu hệ thống (Prerequisites)
- Đã cài đặt **Docker** để build image.
- Có tài khoản **Docker Hub** để push image.
- Có sẵn một cụm **Kubernetes** (VD: Kubeadm trên GCP VM, Minikube, hoặc GKE).
- Đã cài đặt **kubectl** và cấu hình kết nối tới cụm K8s.
- Node.js (để chạy script tạo dữ liệu mẫu).

---

### Bước 1: Clone mã nguồn
Tải mã nguồn từ Github về máy:
```bash
git clone https://github.com/datnc2002/sales_system_microservice.git
cd sales_system_microservice
```

---

### Bước 2: Build và Push Docker Images
Hệ thống sử dụng các Image Docker được tuỳ biến cho từng service. 
*(Lưu ý: Thay `congdat0703` bằng username Docker Hub của bạn).*

**1. Build & Push Frontend:**
```bash
docker build -t congdat0703/htpt-frontend:v2 ./frontend
docker push congdat0703/htpt-frontend:v2
```

**2. Build & Push Backend Services:**
Cần thực hiện build cho từng service (Ví dụ với `user-service` và `api-gateway`):
```bash
# API Gateway
docker build -t congdat0703/htpt-api-gateway:v1 ./api-gateway
docker push congdat0703/htpt-api-gateway:v1

# User Service
docker build -t congdat0703/htpt-user-service:v1 ./user-service
docker push congdat0703/htpt-user-service:v1

# Tương tự cho product-service, order-service, inventory-service, notification-service...
```
*Lưu ý: Hãy đảm bảo bạn đã vào các file yaml trong thư mục `k8s/` và cập nhật lại đường dẫn image thành tên repository của bạn trước khi sang bước tiếp theo.*

---

### Bước 3: Triển khai Infrastructure (Hạ tầng)
Hạ tầng bao gồm Database (Postgres), Queue (Kafka), và Cache (Redis). K8s sẽ chạy chúng trước để các Microservices có thể kết nối vào.

**1. Khởi tạo Namespace & Mật khẩu (Secrets)**
Hệ thống sử dụng K8s Secrets lấy trực tiếp từ file `.env` để bảo mật (không lưu hardcode mật khẩu hay IP lên Git). 
Bạn cần tạo file `.env` từ file `.env-example` mẫu có sẵn:
```bash
cp .env-example .env
```
Mở file `.env` vừa tạo và điền các thông số thực tế của bạn (IP của Node K8s, mật khẩu DB...). 
Sau đó, nạp toàn bộ cấu hình bảo mật vào K8s bằng lệnh sau:
```bash
kubectl apply -f k8s/namespace-and-secrets.yaml
kubectl create secret generic htpt-secrets --from-env-file=.env -n htpt --dry-run=client -o yaml | kubectl apply -f -
```

**2. Khởi chạy Postgres & Kafka & Redis**
```bash
kubectl apply -f k8s/infra/postgres.yaml
kubectl apply -f k8s/infra/kafka-zookeeper.yaml
kubectl apply -f k8s/infra/redis.yaml
```
*Ghi chú: Postgres đã được tích hợp sẵn script `init-databases.sh` thông qua ConfigMap để tự động tạo 5 database rỗng (user_db, product_db,...) khi khởi chạy lần đầu.*

---

### Bước 4: Triển khai Microservices & Frontend
Khi hạ tầng đã báo trạng thái `Running` (kiểm tra bằng `kubectl get pods -n htpt`), tiếp tục chạy các service:

```bash
# Deploy toàn bộ backend services & api-gateway
kubectl apply -f k8s/services/microservices.yaml
kubectl apply -f k8s/services/api-gateway.yaml

# Deploy frontend
kubectl apply -f k8s/frontend/frontend.yaml
```

---

### Bước 5: Cấu hình Firewall (Chỉ dành cho GCP/Cloud VM)
Do K8s sử dụng NodePort để Public dịch vụ ra ngoài mạng internet, cần yêu cầu Cloud Provider (ví dụ Google Cloud) mở các cổng Firewall tương ứng:

- **Mở port 30000 cho Frontend web:**
  ```bash
  gcloud compute firewall-rules create allow-htpt-frontend --allow=tcp:30000 --source-ranges=0.0.0.0/0
  ```
- **Mở port 30080 cho API Gateway (Bắt buộc để Login/Register):**
  ```bash
  gcloud compute firewall-rules create allow-htpt-gateway --allow=tcp:30080 --source-ranges=0.0.0.0/0
  ```
- **Mở port 30543 cho Postgres (Dành cho việc quản lý DB bằng DBeaver):**
  ```bash
  gcloud compute firewall-rules create allow-htpt-postgres --allow=tcp:30543 --source-ranges=0.0.0.0/0
  ```

---

### Bước 6: Nạp dữ liệu mẫu (Seeding)
Sau khi cụm khởi chạy hoàn tất, Database sẽ trống. Sử dụng script `seed.js` để tạo dữ liệu mặc định (Lưu ý: Mở file `seed.js` và cập nhật biến `apiBase` thành IP Server của bạn).

```bash
node seed.js
```
Script này sẽ tự động:
1. Đăng ký/Đăng nhập tài khoản admin.
2. Tạo danh mục sản phẩm (Category).
3. Thêm các Laptop mẫu (MacBook, Dell XPS,...).
4. Khởi tạo kho hàng (Inventory) bằng 50 cho mỗi sản phẩm.

---
- Để theo dõi log khi lỗi: `kubectl logs deployment/order-service -n htpt`
- Để kết nối và xem dòng chảy dữ liệu Database: Tải phần mềm **DBeaver**, kết nối vào IP máy chủ với port `30543`, bạn sẽ có thể nhìn thấy tồn kho bị trừ theo thời gian thực mỗi khi có Order mới nhờ vào kiến trúc giao tiếp Kafka Event-driven.

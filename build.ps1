Write-Host "Running Maven build..."
mvn clean package

# if ($LASTEXITCODE -ne 0) {
#     Write-Host "An error occurred."
#     exit 1
# }

Write-Host "Sucessful! Access http://localhost:9090/"

cd lms_project  # Chuyển vào thư mục lms_project

Write-Host "Building Docker images..."
docker-compose -f docker-compose.yml build

# if ($LASTEXITCODE -ne 0) {
#     Write-Host "Lỗi khi build Docker Compose."
#     exit 1
# }

Write-Host "Xóa các Docker images cũ..."
docker image prune -f

Write-Host "Running Docker containers..."
docker-compose -f docker-compose.yml up -d

# if ($LASTEXITCODE -ne 0) {
#     Write-Host "Lỗi khi khởi động Docker Compose."
#     exit 1
# }

# Delay 3s trước khi thông báo thành công
Start-Sleep -Seconds 3

Write-Host "Deploy thành công!"
Write-Host "Thành công!`nTruy cập http://localhost:9090/"

# Quay lại thư mục trước
cd ..

exit 0
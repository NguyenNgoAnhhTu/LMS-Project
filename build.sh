#!/bin/bash

#echo "Delete old container"
#cd lms_project || { echo "Thư mục lms_project không tồn tại."; exit 1; }
#docker-compose down
#
#echo "Quay lại folder chính"
#cd ../

echo "Running Maven build..."
mvn clean package

if [ $? -ne 0 ]; then
    echo "Lỗi khi build Maven. Kiểm tra lại cấu hình Maven hoặc mã nguồn."
    exit 1
fi

echo "Build Maven xong, bắt đầu build lại Docker..."

# Chuyển vào thư mục lms_project
cd lms_project || { echo "Thư mục lms_project không tồn tại."; exit 1; }

echo "Building Docker images..."
docker-compose -f docker-compose.yml build

if [ $? -ne 0 ]; then
    echo "Lỗi khi build Docker Compose."
    exit 1
fi

echo "Xóa các Docker images cũ..."
docker image prune -f

echo "Running Docker containers..."
docker-compose -f docker-compose.yml up -d

if [ $? -ne 0 ]; then
    echo "Lỗi khi khởi động Docker Compose."
    exit 1
fi

# Delay 3 giây trước khi thông báo thành công
sleep 3

echo "Deploy thành công!"
echo -e "Thành công!\nTruy cập http://localhost:9090/"

# Quay lại thư mục trước
cd ..

exit 0

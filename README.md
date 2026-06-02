
# Hướng Dẫn Biên Dịch Và Thực Thi Dự Án ChatSystem

Tài liệu này hướng dẫn chi tiết cách biên dịch (compile) mã nguồn Java, sao chép tài nguyên cấu hình và chạy ứng dụng (Server & Client) có sử dụng thư viện JDBC của bên thứ ba.

## Bước SetUp: Cài đặt Cơ sở dữ liệu

Dự án này sử dụng PostgreSQL. Vì lý do bảo mật, file cấu hình cơ sở dữ liệu đã được ẩn đi. Để ứng dụng có thể chạy được, bạn cần thực hiện các bước sau:

1. Clone dự án về máy.
2. Đi tới thư mục `ChatSystem/database/`.
3. Tìm file `db.properties.example` và copy/đổi tên nó thành `db.properties`.
4. Mở file `db.properties` và thay đổi thông tin `db.username` và `db.password` cho khớp với PostgreSQL trên máy của bạn.

> **Lưu ý:** File `db.properties` đã được thêm vào `.gitignore` để tránh việc bạn vô tình đẩy mật khẩu cá nhân lên repository.
---

## Bước 1: Chuẩn bị file danh sách mã nguồn (sources.txt)
Trước khi biên dịch, cần gom đường dẫn của tất cả các file `.java` vào một file text để trình biên dịch dễ dàng xử lý.

Mở terminal tại thư mục gốc của dự án và chạy lệnh sau:
```bash
find ChatSystem -name "*.java" > sources.txt

```

*(Lệnh này sẽ tự động quét toàn bộ thư mục `ChatSystem` và ghi tất cả đường dẫn file `.java` vào `sources.txt`)*.

---

## Bước 2: Biên dịch dự án (Compile)

Chúng ta sẽ biên dịch các file `.java` và đưa các file `.class` (kết quả) vào một thư mục riêng biệt mang tên `bin`.
Trong bước này, bắt buộc phải khai báo Classpath (`-cp`) trỏ tới thư viện PostgreSQL.

```bash
# 1. Tạo thư mục chứa file biên dịch (nếu chưa có)
mkdir -p bin

# 2. Biên dịch mã nguồn với classpath
javac -cp "ChatSystem/libs/postgresql-42.7.11.jar" -d bin @sources.txt

```

---

## Bước 3: Sao chép tài nguyên Database (Resources)

Trình biên dịch `javac` chỉ xử lý file `.java`. Vì Server cần đọc file `db.properties` bằng lệnh `getResourceAsStream`, bạn phải copy file này vào đúng cấu trúc thư mục bên trong `bin`.

```bash
# 1. Tạo thư mục database bên trong bin
mkdir -p bin/ChatSystem/database

# 2. Copy file db.properties sang
cp ChatSystem/database/db.properties bin/ChatSystem/database/

```

---

## Bước 4: Thực thi ứng dụng (Chạy trực tiếp không cần đóng gói)

Lúc này thư mục `bin` đã chứa đầy đủ code và cấu hình. Bạn có thể khởi động Server và Client trên hai terminal khác nhau.

> **Lưu ý quan trọng:** Classpath (`-cp`) lúc chạy phải bao gồm cả thư mục `bin` VÀ file `.jar` của thư viện PostgreSQL, được phân cách bằng dấu hai chấm (`:`).

**Khởi động Server:**

```bash
java -cp "bin:ChatSystem/libs/postgresql-42.7.11.jar" ChatSystem.server.ui.ServerFrame

```

**Khởi động Client:**
*(Mở một tab terminal mới và giữ nguyên thư mục gốc của dự án)*

```bash
java -cp "bin" ChatSystem.client.ui.MainFrame

```

*(Client hiện tại không trực tiếp gọi database qua JDBC nên không cần nạp thư viện PostgreSQL vào classpath, chỉ cần thư mục `bin` là đủ).*

---

## Bước 5 (Tùy chọn): Đóng gói thành file ServerApp.jar

Nếu bạn muốn đóng gói toàn bộ Server thành một tệp `.jar` để mang sang máy khác chạy, hãy làm tiếp các lệnh sau:

```bash
# 1. Di chuyển vào thư mục bin
cd bin

# 2. Nén toàn bộ file .class và db.properties thành file .jar
jar cvf ServerApp.jar .

# 3. Chạy Server từ file .jar (lưu ý đường dẫn tới thư viện lùi lại một cấp)
java -cp "ServerApp.jar:../ChatSystem/libs/postgresql-42.7.11.jar" ChatSystem.server.Server

```


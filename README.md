# QUIZ — Ứng dụng trắc nghiệm

Ứng dụng Android làm bài trắc nghiệm, chạy hoàn toàn offline, dữ liệu lưu bằng SQLite trên máy.

Bài tập lớn môn Lập trình Android.

---

## Tính năng

**Tài khoản**
- Đăng nhập / đăng ký, ghi nhớ phiên đăng nhập bằng `SharedPreferences`
- Mật khẩu lưu dưới dạng **SHA-256 kèm muối ngẫu nhiên riêng từng tài khoản**, không lưu mật khẩu gốc
- Đổi mật khẩu ngay trong ứng dụng
- Hai vai trò: **quản trị viên** (4 tab) và **học viên** (3 tab)

**Làm bài**
- Chọn chủ đề, mỗi câu 30 giây — đồng hồ đếm ngược tự nộp bài khi hết giờ
- Trộn ngẫu nhiên thứ tự câu hỏi **và** thứ tự 4 đáp án
- Thanh tiến độ, điều hướng câu trước / câu sau, lưu lại lựa chọn đã chọn
- Màn hình kết quả: điểm, xếp loại, số câu đúng / sai / bỏ trống, chi tiết từng câu tô màu xanh–đỏ

**Lịch sử & thống kê**
- Lịch sử và thống kê tách riêng theo từng tài khoản
- Tổng hợp bằng hàm SQL `COUNT` / `AVG` / `MAX` / `MIN` / `SUM` + `GROUP BY`
- Xuất báo cáo ra tập tin văn bản

**Quản trị** (chỉ vai trò quản trị viên)
- Thêm / sửa / xoá chủ đề và câu hỏi
- Nhập và xuất ngân hàng câu hỏi ra file `.txt` qua trình chọn file của hệ thống
- Xem danh sách tài khoản kèm số lần làm bài và điểm trung bình, xoá tài khoản

---

## Yêu cầu

| Mục | Giá trị |
|---|---|
| Android tối thiểu | 7.0 (API 24) |
| Biên dịch với | API 37 |
| Ngôn ngữ | Java |
| Gradle DSL | Groovy |
| Quyền truy cập | không xin quyền nào |

---

## Chạy thử

```bash
./gradlew assembleDebug
```

APK nằm ở `app/build/outputs/apk/debug/app-debug.apk`.

Nếu build bằng dòng lệnh trên Windows mà Gradle đòi tải JDK, trỏ `JAVA_HOME` về JBR của Android Studio:

```bash
export JAVA_HOME="C:\Program Files\Android\Android Studio\jbr"
```

### Tài khoản mặc định

Lần đầu mở ứng dụng, cơ sở dữ liệu tự tạo sẵn một tài khoản quản trị `admin` với mật khẩu `admin` (xem `CSDL.napNguoiDungMau`). **Nên đổi mật khẩu ngay** bằng nút ĐỔI MK.

Kèm theo là 18 câu hỏi mẫu thuộc 2 chủ đề: *Android cơ bản* và *Java cơ bản*.

---

## Cấu trúc mã nguồn

```
app/src/main/java/com/example/apptracnghiem/
├── CSDL.java              Toàn bộ thao tác SQLite: 4 bảng, băm mật khẩu, nâng cấp CSDL
├── PhienDangNhap.java     Lưu phiên đăng nhập bằng SharedPreferences
├── TapTin.java            Đọc / ghi file câu hỏi và báo cáo
│
├── DangNhapActivity.java  Màn hình đăng nhập (LAUNCHER)
├── DangKyActivity.java    Màn hình tạo tài khoản
├── MainActivity.java      Khung chính: thanh tài khoản + TabLayout + ViewPager
├── LamBaiActivity.java    Làm bài, đếm ngược, chấm điểm
├── KetQuaActivity.java    Kết quả và chi tiết từng câu
│
├── ChonDeFragment.java    Tab LÀM BÀI
├── LichSuFragment.java    Tab LỊCH SỬ
├── ThongKeFragment.java   Tab THỐNG KÊ
├── QuanTriFragment.java   Tab QUẢN TRỊ
│
├── VPAdapter.java         FragmentPagerAdapter cho ViewPager
└── ChiTietAdapter.java    ArrayAdapter tô màu dòng đúng / sai
```

---

## Cơ sở dữ liệu

File `tracnghiem.db`, gồm 4 bảng:

| Bảng | Nội dung |
|---|---|
| `nguoidung` | `tendangnhap` (duy nhất), `matkhau` (mã băm), `muoi`, `hoten`, `vaitro`, `ngaytao` |
| `chude` | Danh mục chủ đề |
| `cauhoi` | `chude`, `noidung`, `da1`–`da4`, `dapandung` |
| `lichsu` | `nguoidung`, `chude`, `socau`, `socaudung`, `diem`, `thoigian` |

Khi mở ứng dụng, `CSDL.capNhatBangLichSu()` tự kiểm tra bằng `PRAGMA table_info` và chạy `ALTER TABLE` nếu thiếu cột — nâng cấp từ bản cũ mà không mất dữ liệu.

---

## Định dạng file câu hỏi

Mỗi dòng một câu, các trường ngăn nhau bằng dấu `|`:

```
Chủ đề|Nội dung câu hỏi|Đáp án A|Đáp án B|Đáp án C|Đáp án D|Số thứ tự đáp án đúng (1-4)
```

Dòng bắt đầu bằng `#` là chú thích, dòng trống bị bỏ qua. Nội dung câu hỏi và đáp án không được chứa dấu `|`.

Ví dụ:

```
Android cơ bản|Phương thức nào được gọi đầu tiên khi một Activity được tạo?|onStart()|onCreate()|onResume()|main()|2
```

Nhập lại cùng một file nhiều lần không tạo câu trùng — ứng dụng đối chiếu theo cặp (chủ đề, nội dung).

package com.example.apptracnghiem;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.security.MessageDigest;
import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

/**
 * Quan ly co so du lieu SQLite cua ung dung trac nghiem.
 * Gom 4 bang:
 *   chude     - danh muc chu de
 *   cauhoi    - ngan hang cau hoi
 *   lichsu    - ket qua cac lan lam bai (gan voi tung nguoi dung)
 *   nguoidung - tai khoan dang nhap
 */
public class CSDL {

    public static final String TEN_DB = "tracnghiem.db";

    /** Mo CSDL, tao bang va nap du lieu mau neu chua co. */
    public static SQLiteDatabase mo(Context ctx) {
        SQLiteDatabase db = ctx.openOrCreateDatabase(TEN_DB, Context.MODE_PRIVATE, null);
        taoBang(db);
        capNhatBangLichSu(db);
        napCauHoiMau(db);
        dongBoChuDe(db);
        napNguoiDungMau(db);
        return db;
    }

    private static void taoBang(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS chude("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "ten TEXT UNIQUE)");

        db.execSQL("CREATE TABLE IF NOT EXISTS cauhoi("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "chude TEXT,"
                + "noidung TEXT,"
                + "da1 TEXT, da2 TEXT, da3 TEXT, da4 TEXT,"
                + "dapandung INTEGER)");

        db.execSQL("CREATE TABLE IF NOT EXISTS lichsu("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "nguoidung TEXT,"
                + "chude TEXT,"
                + "socau INTEGER,"
                + "socaudung INTEGER,"
                + "diem REAL,"
                + "thoigian TEXT)");

        db.execSQL("CREATE TABLE IF NOT EXISTS nguoidung("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "tendangnhap TEXT UNIQUE,"
                + "matkhau TEXT,"
                + "muoi TEXT,"
                + "hoten TEXT,"
                + "vaitro TEXT,"
                + "ngaytao TEXT)");
    }

    /** Bo sung vao bang chude moi chu de dang co cau hoi ma chua duoc khai bao. */
    private static void dongBoChuDe(SQLiteDatabase db) {
        db.execSQL("INSERT OR IGNORE INTO chude(ten) "
                + "SELECT DISTINCT chude FROM cauhoi WHERE chude IS NOT NULL AND chude <> ''");
    }

    // ===================== CHU DE =====================

    public static String[] layDanhSachChuDe(SQLiteDatabase db) {
        Cursor c = db.rawQuery("SELECT ten FROM chude ORDER BY ten", null);
        String[] ds = new String[c.getCount()];
        int i = 0;
        while (c.moveToNext()) ds[i++] = c.getString(0);
        c.close();
        return ds;
    }

    /** Them chu de moi. Tra ve false neu ten rong hoac da ton tai. */
    public static boolean themChuDe(SQLiteDatabase db, String ten) {
        ten = ten.trim();
        if (ten.isEmpty()) return false;

        Cursor c = db.rawQuery("SELECT COUNT(*) FROM chude WHERE ten = ?", new String[]{ten});
        boolean daCo = c.moveToFirst() && c.getInt(0) > 0;
        c.close();
        if (daCo) return false;

        ContentValues v = new ContentValues();
        v.put("ten", ten);
        return db.insert("chude", null, v) != -1;
    }

    /** Xoa chu de va toan bo cau hoi thuoc chu de do. Tra ve so cau hoi da xoa. */
    public static int xoaChuDe(SQLiteDatabase db, String ten) {
        int soCau = db.delete("cauhoi", "chude = ?", new String[]{ten});
        db.delete("chude", "ten = ?", new String[]{ten});
        return soCau;
    }

    // ===================== CAU HOI =====================

    public static int demCauHoi(SQLiteDatabase db) {
        Cursor c = db.rawQuery("SELECT COUNT(*) FROM cauhoi", null);
        int n = 0;
        if (c.moveToFirst()) n = c.getInt(0);
        c.close();
        return n;
    }

    public static int demCauHoi(SQLiteDatabase db, String chuDe) {
        Cursor c = db.rawQuery("SELECT COUNT(*) FROM cauhoi WHERE chude = ?",
                new String[]{chuDe});
        int n = 0;
        if (c.moveToFirst()) n = c.getInt(0);
        c.close();
        return n;
    }

    /** Kiem tra mot cau hoi (cung chu de, cung noi dung) da co trong CSDL chua. */
    public static boolean coCauHoi(SQLiteDatabase db, String chude, String noidung) {
        Cursor c = db.rawQuery("SELECT COUNT(*) FROM cauhoi WHERE chude = ? AND noidung = ?",
                new String[]{chude.trim(), noidung.trim()});
        boolean co = c.moveToFirst() && c.getInt(0) > 0;
        c.close();
        return co;
    }

    /** Them mot cau hoi. Tra ve true neu thanh cong. */
    public static boolean themCauHoi(SQLiteDatabase db, String chude, String noidung,
                                     String da1, String da2, String da3, String da4,
                                     int dapAnDung) {
        if (chude == null || chude.trim().isEmpty()) return false;
        if (noidung == null || noidung.trim().isEmpty()) return false;
        if (da1 == null || da2 == null || da3 == null || da4 == null) return false;
        if (da1.trim().isEmpty() || da2.trim().isEmpty()
                || da3.trim().isEmpty() || da4.trim().isEmpty()) return false;
        if (dapAnDung < 1 || dapAnDung > 4) return false;

        ContentValues v = new ContentValues();
        v.put("chude", chude.trim());
        v.put("noidung", noidung.trim());
        v.put("da1", da1.trim());
        v.put("da2", da2.trim());
        v.put("da3", da3.trim());
        v.put("da4", da4.trim());
        v.put("dapandung", dapAnDung);
        boolean ok = db.insert("cauhoi", null, v) != -1;
        if (ok) themChuDe(db, chude);   // chu de moi tu file se duoc tao luon
        return ok;
    }

    private static void them(SQLiteDatabase db, String chude, String noidung,
                             String da1, String da2, String da3, String da4, int dung) {
        themCauHoi(db, chude, noidung, da1, da2, da3, da4, dung);
    }

    /** Nap cau hoi mau, chi chay mot lan khi bang con rong. */
    private static void napCauHoiMau(SQLiteDatabase db) {
        if (demCauHoi(db) > 0) return;

        String android = "Android cơ bản";
        them(db, android, "Phương thức nào được gọi đầu tiên khi một Activity được tạo?",
                "onStart()", "onCreate()", "onResume()", "main()", 2);
        them(db, android, "Tập tin nào khai báo tất cả các Activity của ứng dụng?",
                "build.gradle", "strings.xml", "AndroidManifest.xml", "settings.gradle", 3);
        them(db, android, "Để xếp các View nằm ngang, LinearLayout dùng thuộc tính nào?",
                "android:orientation=horizontal", "android:gravity=center",
                "android:layout_weight=1", "android:orientation=vertical", 1);
        them(db, android, "Phương thức nào ánh xạ một View từ layout vào biến Java?",
                "getView()", "setContentView()", "findViewById()", "inflate()", 3);
        them(db, android, "Đối tượng nào dùng để chuyển sang một Activity khác?",
                "Bundle", "Intent", "Cursor", "Adapter", 2);
        them(db, android, "Lớp nào hiển thị một thông báo ngắn rồi tự biến mất?",
                "Toast", "AlertDialog", "Snackbar", "TextView", 1);
        them(db, android, "Trong LinearLayout, muốn chia tỉ lệ bề ngang thì layout_width đặt là gì?",
                "match_parent", "wrap_content", "0dp", "fill_parent", 3);
        them(db, android, "Thành phần nào cho phép chọn MỘT trong nhiều lựa chọn loại trừ nhau?",
                "CheckBox", "RadioGroup", "ListView", "Spinner", 2);
        them(db, android, "Phương thức nào đóng Activity hiện tại và quay về màn hình trước?",
                "close()", "exit()", "destroy()", "finish()", 4);
        them(db, android, "Lớp nào dùng để đổ dữ liệu từ ArrayList lên ListView?",
                "ArrayAdapter", "ContentValues", "LayoutInflater", "Intent", 1);

        String java = "Java cơ bản";
        them(db, java, "Từ khoá nào dùng để khai báo một hằng số trong Java?",
                "const", "static", "final", "readonly", 3);
        them(db, java, "Lớp nào dùng để tạo chuỗi có thể thay đổi nội dung?",
                "String", "StringBuilder", "Character", "Text", 2);
        them(db, java, "Phương thức nào chuyển một chuỗi thành số nguyên?",
                "Integer.parseInt()", "String.toInt()", "Number.parse()", "int.valueOf()", 1);
        them(db, java, "Cấu trúc nào chọn một trong nhiều trường hợp dựa theo giá trị?",
                "while", "for", "switch", "try", 3);
        them(db, java, "Lớp ArrayList nằm trong gói (package) nào?",
                "java.lang", "java.io", "java.text", "java.util", 4);
        them(db, java, "Từ khoá nào dùng để kế thừa một lớp khác?",
                "implements", "extends", "inherits", "super", 2);
        them(db, java, "Với hai số nguyên, phép tính 7 / 2 trong Java cho kết quả là gì?",
                "3.5", "4", "3", "Báo lỗi", 3);
        them(db, java, "Ngoại lệ nào xảy ra khi parseInt() nhận một chuỗi không phải số?",
                "NullPointerException", "IOException",
                "ArithmeticException", "NumberFormatException", 4);
    }

    // ===================== NGUOI DUNG =====================

    public static final String VAI_TRO_ADMIN = "admin";
    public static final String VAI_TRO_HOC_VIEN = "hocvien";

    /** Mot ban ghi nguoi dung. Khong bao gio chua mat khau goc. */
    public static class NguoiDung {
        public String ten;       // ten dang nhap
        public String hoTen;
        public String vaiTro;
        public int soLanLam;     // chi dung khi liet ke danh sach
        public double diemTB;
    }

    /** Lan dau chay: tao san mot tai khoan quan tri de co the vao ung dung. */
    private static void napNguoiDungMau(SQLiteDatabase db) {
        if (demNguoiDung(db) > 0) return;
        dangKy(db, "admin", "admin", "Quản trị viên", VAI_TRO_ADMIN);
    }

    /**
     * Bo sung cot nguoidung cho bang lichsu o nhung may da cai ban cu.
     * Cac lan lam bai cu duoc gan cho tai khoan admin de khong bi mat.
     */
    private static void capNhatBangLichSu(SQLiteDatabase db) {
        if (coCot(db, "lichsu", "nguoidung")) return;
        db.execSQL("ALTER TABLE lichsu ADD COLUMN nguoidung TEXT");
        db.execSQL("UPDATE lichsu SET nguoidung = 'admin' WHERE nguoidung IS NULL");
    }

    /** Kiem tra mot bang da co cot nay chua (dung lenh PRAGMA cua SQLite). */
    private static boolean coCot(SQLiteDatabase db, String bang, String cot) {
        Cursor c = db.rawQuery("PRAGMA table_info(" + bang + ")", null);
        boolean co = false;
        while (c.moveToNext()) {
            if (cot.equalsIgnoreCase(c.getString(c.getColumnIndexOrThrow("name")))) {
                co = true;
                break;
            }
        }
        c.close();
        return co;
    }

    public static int demNguoiDung(SQLiteDatabase db) {
        Cursor c = db.rawQuery("SELECT COUNT(*) FROM nguoidung", null);
        int n = 0;
        if (c.moveToFirst()) n = c.getInt(0);
        c.close();
        return n;
    }

    public static boolean coNguoiDung(SQLiteDatabase db, String ten) {
        Cursor c = db.rawQuery("SELECT COUNT(*) FROM nguoidung WHERE tendangnhap = ?",
                new String[]{chuan(ten)});
        boolean co = c.moveToFirst() && c.getInt(0) > 0;
        c.close();
        return co;
    }

    /** Tao tai khoan moi. Tra ve false neu thieu du lieu hoac ten da ton tai. */
    public static boolean dangKy(SQLiteDatabase db, String ten, String matKhau,
                                 String hoTen, String vaiTro) {
        ten = chuan(ten);
        if (ten.isEmpty() || matKhau == null || matKhau.isEmpty()) return false;
        if (coNguoiDung(db, ten)) return false;

        String muoi = taoMuoi();
        ContentValues v = new ContentValues();
        v.put("tendangnhap", ten);
        v.put("muoi", muoi);
        v.put("matkhau", bam(matKhau, muoi));
        v.put("hoten", (hoTen == null || hoTen.trim().isEmpty()) ? ten : hoTen.trim());
        v.put("vaitro", vaiTro);
        v.put("ngaytao", new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.US)
                .format(new Date()));
        return db.insert("nguoidung", null, v) != -1;
    }

    /** Doi chieu ten dang nhap va mat khau. Tra ve null neu khong dung. */
    public static NguoiDung dangNhap(SQLiteDatabase db, String ten, String matKhau) {
        if (ten == null || matKhau == null) return null;
        Cursor c = db.rawQuery("SELECT tendangnhap, hoten, vaitro, matkhau, muoi "
                        + "FROM nguoidung WHERE tendangnhap = ?",
                new String[]{chuan(ten)});
        NguoiDung nd = null;
        if (c.moveToFirst() && c.getString(3).equals(bam(matKhau, c.getString(4)))) {
            nd = new NguoiDung();
            nd.ten = c.getString(0);
            nd.hoTen = c.getString(1);
            nd.vaiTro = c.getString(2);
        }
        c.close();
        return nd;
    }

    /** Doi mat khau. Tra ve false neu mat khau hien tai khong dung. */
    public static boolean doiMatKhau(SQLiteDatabase db, String ten,
                                     String matKhauCu, String matKhauMoi) {
        if (matKhauMoi == null || matKhauMoi.isEmpty()) return false;
        if (dangNhap(db, ten, matKhauCu) == null) return false;

        String muoi = taoMuoi();
        ContentValues v = new ContentValues();
        v.put("muoi", muoi);
        v.put("matkhau", bam(matKhauMoi, muoi));
        return db.update("nguoidung", v, "tendangnhap = ?", new String[]{chuan(ten)}) > 0;
    }

    /**
     * Danh sach tai khoan kem so lan lam bai va diem trung binh.
     * LEFT JOIN de nguoi chua lam bai nao van xuat hien trong danh sach.
     */
    public static ArrayList<NguoiDung> layDanhSachNguoiDung(SQLiteDatabase db) {
        ArrayList<NguoiDung> ds = new ArrayList<>();
        Cursor c = db.rawQuery("SELECT n.tendangnhap, n.hoten, n.vaitro, "
                + "COUNT(l.id), IFNULL(AVG(l.diem), 0) "
                + "FROM nguoidung n LEFT JOIN lichsu l ON l.nguoidung = n.tendangnhap "
                + "GROUP BY n.tendangnhap ORDER BY n.vaitro, n.tendangnhap", null);
        while (c.moveToNext()) {
            NguoiDung nd = new NguoiDung();
            nd.ten = c.getString(0);
            nd.hoTen = c.getString(1);
            nd.vaiTro = c.getString(2);
            nd.soLanLam = c.getInt(3);
            nd.diemTB = c.getDouble(4);
            ds.add(nd);
        }
        c.close();
        return ds;
    }

    /** Xoa tai khoan va toan bo lich su lam bai cua no. Tra ve so lan lam bai da xoa. */
    public static int xoaNguoiDung(SQLiteDatabase db, String ten) {
        String t = chuan(ten);
        int soLan = db.delete("lichsu", "nguoidung = ?", new String[]{t});
        db.delete("nguoidung", "tendangnhap = ?", new String[]{t});
        return soLan;
    }

    private static String chuan(String ten) {
        return ten == null ? "" : ten.trim().toLowerCase(Locale.US);
    }

    // ----- Bam mat khau -----
    // CSDL khong luu mat khau goc, chi luu ma bam SHA-256 cua (muoi + mat khau).
    // "Muoi" la mot chuoi ngau nhien rieng cho tung tai khoan, nho do hai nguoi
    // dat cung mat khau van cho ra hai ma bam khac nhau.

    private static String taoMuoi() {
        byte[] b = new byte[8];
        new SecureRandom().nextBytes(b);
        return sangHex(b);
    }

    private static String bam(String matKhau, String muoi) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(muoi.getBytes("UTF-8"));
            return sangHex(md.digest(matKhau.getBytes("UTF-8")));
        } catch (Exception e) {
            return muoi + matKhau;   // khong xay ra tren Android
        }
    }

    private static String sangHex(byte[] b) {
        StringBuilder sb = new StringBuilder();
        for (byte x : b) sb.append(String.format(Locale.US, "%02x", x));
        return sb.toString();
    }
}

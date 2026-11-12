package com.example.apptracnghiem;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

/**
 * Man hinh dang nhap - diem vao cua ung dung.
 * Doi chieu ten dang nhap va mat khau voi bang nguoidung trong SQLite.
 */
public class DangNhapActivity extends AppCompatActivity {

    private EditText edtTen, edtMatKhau;
    private CheckBox cbHien, cbGhiNho;
    private TextView tvThongBao;
    private SQLiteDatabase db;
    private int soLanSai = 0;

    /** Nhan ten dang nhap vua tao tu man hinh dang ky de dien san vao form. */
    private final ActivityResultLauncher<Intent> moDangKy = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), kq -> {
                if (kq.getResultCode() != RESULT_OK || kq.getData() == null) return;
                String ten = kq.getData().getStringExtra("tendangnhap");
                if (ten == null) return;
                edtTen.setText(ten);
                edtMatKhau.setText("");
                edtMatKhau.requestFocus();
                baoTin("Đăng ký thành công! Hãy đăng nhập bằng tài khoản vừa tạo.", false);
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = CSDL.mo(this);

        // Lan truoc co tich "Ghi nho dang nhap" thi vao thang trang chinh
        if (PhienDangNhap.daDangNhap(this) && PhienDangNhap.ghiNho(this)) {
            vaoTrangChinh();
            return;
        }
        PhienDangNhap.xoa(this);   // phien cu khong ghi nho thi bo di

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_dang_nhap);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });

        edtTen = findViewById(R.id.edtTenDangNhap);
        edtMatKhau = findViewById(R.id.edtMatKhau);
        cbHien = findViewById(R.id.cbHienMatKhau);
        cbGhiNho = findViewById(R.id.cbGhiNho);
        tvThongBao = findViewById(R.id.tvThongBao);

        Button btnDangNhap = findViewById(R.id.btnDangNhap);
        Button btnDangKy = findViewById(R.id.btnDangKy);

        // Hien / an mat khau
        cbHien.setOnCheckedChangeListener((b, hien) -> {
            edtMatKhau.setTransformationMethod(hien
                    ? HideReturnsTransformationMethod.getInstance()
                    : PasswordTransformationMethod.getInstance());
            edtMatKhau.setSelection(edtMatKhau.getText().length());
        });

        btnDangNhap.setOnClickListener(v -> dangNhap());
        btnDangKy.setOnClickListener(v ->
                moDangKy.launch(new Intent(this, DangKyActivity.class)));

        // Lan dau mo ung dung thi nhac tai khoan quan tri co san
        if (CSDL.demNguoiDung(db) <= 1) {
            baoTin("Lần đầu sử dụng: đăng nhập admin / admin, hoặc bấm ĐĂNG KÝ.", false);
        }
    }

    private void dangNhap() {
        String ten = edtTen.getText().toString().trim();
        String matKhau = edtMatKhau.getText().toString();

        if (ten.isEmpty()) {
            edtTen.requestFocus();
            baoTin("Bạn chưa nhập tên đăng nhập", true);
            return;
        }
        if (matKhau.isEmpty()) {
            edtMatKhau.requestFocus();
            baoTin("Bạn chưa nhập mật khẩu", true);
            return;
        }

        CSDL.NguoiDung nd = CSDL.dangNhap(db, ten, matKhau);
        if (nd == null) {
            soLanSai++;
            baoTin("Tên đăng nhập hoặc mật khẩu không đúng (lần " + soLanSai + ")", true);
            edtMatKhau.setText("");
            edtMatKhau.requestFocus();
            if (soLanSai >= 5) hoiQuenMatKhau();
            return;
        }

        PhienDangNhap.luu(this, nd, cbGhiNho.isChecked());
        Toast.makeText(this, "Xin chào " + nd.hoTen + "!", Toast.LENGTH_SHORT).show();
        vaoTrangChinh();
    }

    private void hoiQuenMatKhau() {
        new AlertDialog.Builder(this)
                .setTitle("Bạn quên mật khẩu?")
                .setMessage("Đã sai " + soLanSai + " lần."
                        + "\n\nỨng dụng chạy hoàn toàn trên máy nên không có chức năng gửi mã"
                        + " khôi phục. Bạn có thể đăng ký một tài khoản mới, hoặc nhờ người"
                        + " quản trị xoá tài khoản cũ giúp bạn.")
                .setPositiveButton("Đăng ký tài khoản mới", (d, w) ->
                        moDangKy.launch(new Intent(this, DangKyActivity.class)))
                .setNegativeButton("Thử lại", null)
                .show();
    }

    /** Hien thong bao ngay duoi o mat khau: mau do neu la loi. */
    private void baoTin(String noiDung, boolean loi) {
        tvThongBao.setText(noiDung);
        tvThongBao.setTextColor(ContextCompat.getColor(this,
                loi ? R.color.do_sai : R.color.nhan_cyan));
        tvThongBao.setVisibility(TextView.VISIBLE);
    }

    private void vaoTrangChinh() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}

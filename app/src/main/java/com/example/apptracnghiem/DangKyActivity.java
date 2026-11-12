package com.example.apptracnghiem;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.Locale;

/**
 * Man hinh tao tai khoan moi. Tai khoan tao o day luon co vai tro "hocvien".
 * Khi thanh cong, tra ten dang nhap ve DangNhapActivity bang setResult().
 */
public class DangKyActivity extends AppCompatActivity {

    private EditText edtHoTen, edtTen, edtMatKhau, edtMatKhauLai;
    private TextView tvThongBao;
    private SQLiteDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_dang_ky);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });

        db = CSDL.mo(this);

        edtHoTen = findViewById(R.id.edtHoTen);
        edtTen = findViewById(R.id.edtTenDangNhapMoi);
        edtMatKhau = findViewById(R.id.edtMatKhauMoi);
        edtMatKhauLai = findViewById(R.id.edtMatKhauLai);
        tvThongBao = findViewById(R.id.tvThongBaoDangKy);

        Button btnTao = findViewById(R.id.btnTaoTaiKhoan);
        Button btnQuayLai = findViewById(R.id.btnQuayLai);

        btnTao.setOnClickListener(v -> taoTaiKhoan());
        btnQuayLai.setOnClickListener(v -> finish());
    }

    private void taoTaiKhoan() {
        String hoTen = edtHoTen.getText().toString().trim();
        String ten = edtTen.getText().toString().trim().toLowerCase(Locale.US);
        String matKhau = edtMatKhau.getText().toString();
        String matKhauLai = edtMatKhauLai.getText().toString();

        if (hoTen.isEmpty()) {
            edtHoTen.requestFocus();
            baoTin("Bạn chưa nhập họ tên", true);
            return;
        }
        if (ten.length() < 3) {
            edtTen.requestFocus();
            baoTin("Tên đăng nhập phải có ít nhất 3 ký tự", true);
            return;
        }
        if (!ten.matches("[a-z0-9_]+")) {
            edtTen.requestFocus();
            baoTin("Tên đăng nhập chỉ gồm chữ không dấu, số và dấu gạch dưới", true);
            return;
        }
        if (matKhau.length() < 4) {
            edtMatKhau.requestFocus();
            baoTin("Mật khẩu phải có ít nhất 4 ký tự", true);
            return;
        }
        if (!matKhau.equals(matKhauLai)) {
            edtMatKhauLai.requestFocus();
            edtMatKhauLai.setText("");
            baoTin("Hai lần nhập mật khẩu không giống nhau", true);
            return;
        }
        if (CSDL.coNguoiDung(db, ten)) {
            edtTen.requestFocus();
            baoTin("Tên đăng nhập \"" + ten + "\" đã có người dùng. Hãy chọn tên khác.", true);
            return;
        }

        if (!CSDL.dangKy(db, ten, matKhau, hoTen, CSDL.VAI_TRO_HOC_VIEN)) {
            baoTin("Không tạo được tài khoản. Hãy thử lại.", true);
            return;
        }

        Toast.makeText(this, "Đã tạo tài khoản " + ten, Toast.LENGTH_SHORT).show();

        Intent kq = new Intent();
        kq.putExtra("tendangnhap", ten);
        setResult(RESULT_OK, kq);
        finish();
    }

    private void baoTin(String noiDung, boolean loi) {
        tvThongBao.setText(noiDung);
        tvThongBao.setTextColor(ContextCompat.getColor(this,
                loi ? R.color.do_sai : R.color.nhan_cyan));
        tvThongBao.setVisibility(TextView.VISIBLE);
    }
}

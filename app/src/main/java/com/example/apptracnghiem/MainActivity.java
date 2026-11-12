package com.example.apptracnghiem;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;

import java.util.Locale;

/**
 * Khung chinh: thanh tai khoan + TabLayout + ViewPager.
 * Tab QUAN TRI chi hien voi nguoi dung co vai tro admin.
 */
public class MainActivity extends AppCompatActivity {

    private SQLiteDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Chua dang nhap (vi du vua dang xuat roi mo lai) thi ve man hinh dang nhap
        if (!PhienDangNhap.daDangNhap(this)) {
            startActivity(new Intent(this, DangNhapActivity.class));
            finish();
            return;
        }

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });

        db = CSDL.mo(this);
        boolean laAdmin = PhienDangNhap.laAdmin(this);

        hienThanhTaiKhoan(laAdmin);

        findViewById(R.id.btnDoiMatKhau).setOnClickListener(v -> hoiDoiMatKhau());
        findViewById(R.id.btnDangXuat).setOnClickListener(v -> hoiDangXuat());

        TabLayout tabLayout = findViewById(R.id.tablayout);
        ViewPager viewPager = findViewById(R.id.viewpager);

        tabLayout.setupWithViewPager(viewPager);

        VPAdapter vpAdapter = new VPAdapter(getSupportFragmentManager(),
                FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        vpAdapter.addFragment(new ChonDeFragment(), "LÀM BÀI");
        vpAdapter.addFragment(new LichSuFragment(), "LỊCH SỬ");
        vpAdapter.addFragment(new ThongKeFragment(), "THỐNG KÊ");
        if (laAdmin) vpAdapter.addFragment(new QuanTriFragment(), "QUẢN TRỊ");
        viewPager.setAdapter(vpAdapter);
    }

    /** Dien ten, vai tro va chu cai dau vao thanh tai khoan tren cung. */
    private void hienThanhTaiKhoan(boolean laAdmin) {
        String hoTen = PhienDangNhap.hoTen(this);
        String ten = PhienDangNhap.ten(this);

        TextView tvChuCai = findViewById(R.id.tvChuCai);
        TextView tvHoTen = findViewById(R.id.tvHoTen);
        TextView tvVaiTro = findViewById(R.id.tvVaiTro);

        tvChuCai.setText(hoTen.isEmpty()
                ? "?" : hoTen.substring(0, 1).toUpperCase(Locale.US));
        tvHoTen.setText(hoTen);
        tvVaiTro.setText((laAdmin ? "Quản trị viên" : "Học viên") + "  •  @" + ten);
    }

    // ===================== DOI MAT KHAU =====================

    private void hoiDoiMatKhau() {
        View form = getLayoutInflater().inflate(R.layout.dialog_doi_mat_khau, null);
        EditText edtCu = form.findViewById(R.id.edtMkCu);
        EditText edtMoi = form.findViewById(R.id.edtMkMoi);
        EditText edtLai = form.findViewById(R.id.edtMkLai);

        AlertDialog hop = new AlertDialog.Builder(this)
                .setTitle("Đổi mật khẩu")
                .setView(form)
                .setPositiveButton("Đổi mật khẩu", null)   // gan listener sau khi show()
                .setNegativeButton("Huỷ", null)
                .create();
        hop.show();

        // Gan listener sau show() de hop thoai khong tu dong dong khi nhap sai
        hop.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String cu = edtCu.getText().toString();
            String moi = edtMoi.getText().toString();
            String lai = edtLai.getText().toString();

            if (moi.length() < 4) {
                Toast.makeText(this, "Mật khẩu mới phải có ít nhất 4 ký tự",
                        Toast.LENGTH_SHORT).show();
                return;
            }
            if (!moi.equals(lai)) {
                Toast.makeText(this, "Hai lần nhập mật khẩu mới không giống nhau",
                        Toast.LENGTH_SHORT).show();
                return;
            }
            if (!CSDL.doiMatKhau(db, PhienDangNhap.ten(this), cu, moi)) {
                Toast.makeText(this, "Mật khẩu hiện tại không đúng",
                        Toast.LENGTH_SHORT).show();
                return;
            }
            Toast.makeText(this, "Đã đổi mật khẩu", Toast.LENGTH_SHORT).show();
            hop.dismiss();
        });
    }

    // ===================== DANG XUAT =====================

    private void hoiDangXuat() {
        new AlertDialog.Builder(this)
                .setTitle("Đăng xuất?")
                .setMessage("Bạn sẽ quay lại màn hình đăng nhập."
                        + "\nCâu hỏi và lịch sử làm bài vẫn được giữ nguyên.")
                .setPositiveButton("Đăng xuất", (d, w) -> {
                    PhienDangNhap.xoa(this);
                    Intent it = new Intent(this, DangNhapActivity.class);
                    it.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                            | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(it);
                    finish();
                })
                .setNegativeButton("Huỷ", null)
                .show();
    }
}

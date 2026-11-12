package com.example.apptracnghiem;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

public class LamBaiActivity extends AppCompatActivity {

    /** So giay lam bai cho moi cau hoi. */
    static final int GIAY_MOI_CAU = 30;

    /** Mot cau hoi gom noi dung, 4 dap an va so thu tu dap an dung (1..4). */
    private static class CauHoi {
        String noiDung;
        String[] dapAn = new String[4];
        int dapAnDung;
    }

    private TextView tvTienDo, tvThoiGian, tvCauHoi, tvDaTraLoi;
    private ProgressBar pbTienDo;
    private RadioGroup rgDapAn;
    private final RadioButton[] rb = new RadioButton[4];

    private SQLiteDatabase db;
    private String chuDe = "";
    private final ArrayList<CauHoi> danhSach = new ArrayList<>();
    private int[] traLoi;              // 0 = chua tra loi, 1..4 = dap an da chon
    private int viTri = 0;             // cau dang hien
    private boolean dangNap = false;   // chan listener khi dat trang thai bang code
    private boolean daNop = false;     // chan nop bai hai lan
    private CountDownTimer dongHo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_lam_bai);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });

        anhXa();

        // Nhan chu de tu MainActivity qua Bundle
        Bundle goi = getIntent().getBundleExtra("GoiDuLieu");
        if (goi != null) chuDe = goi.getString("chude", "");

        db = CSDL.mo(this);
        napCauHoi();

        if (danhSach.isEmpty()) {
            Toast.makeText(this, "Chủ đề này chưa có câu hỏi!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        traLoi = new int[danhSach.size()];
        hienCau(0);
        batDauDemNguoc(danhSach.size() * GIAY_MOI_CAU);

        rgDapAn.setOnCheckedChangeListener((group, checkedId) -> {
            if (dangNap) return;
            if (checkedId == R.id.rb1) traLoi[viTri] = 1;
            else if (checkedId == R.id.rb2) traLoi[viTri] = 2;
            else if (checkedId == R.id.rb3) traLoi[viTri] = 3;
            else if (checkedId == R.id.rb4) traLoi[viTri] = 4;
            capNhatTienDo();
        });

        Button btnTruoc = findViewById(R.id.btnTruoc);
        Button btnSau = findViewById(R.id.btnSau);
        Button btnNopBai = findViewById(R.id.btnNopBai);

        btnTruoc.setOnClickListener(v -> {
            if (viTri > 0) hienCau(viTri - 1);
            else Toast.makeText(this, "Đây là câu đầu tiên", Toast.LENGTH_SHORT).show();
        });

        btnSau.setOnClickListener(v -> {
            if (viTri < danhSach.size() - 1) hienCau(viTri + 1);
            else Toast.makeText(this, "Đây là câu cuối cùng", Toast.LENGTH_SHORT).show();
        });

        btnNopBai.setOnClickListener(v -> hoiNopBai());
    }

    private void anhXa() {
        tvTienDo = findViewById(R.id.tvTienDo);
        tvThoiGian = findViewById(R.id.tvThoiGian);
        tvCauHoi = findViewById(R.id.tvCauHoi);
        tvDaTraLoi = findViewById(R.id.tvDaTraLoi);
        pbTienDo = findViewById(R.id.pbTienDo);
        rgDapAn = findViewById(R.id.rgDapAn);
        rb[0] = findViewById(R.id.rb1);
        rb[1] = findViewById(R.id.rb2);
        rb[2] = findViewById(R.id.rb3);
        rb[3] = findViewById(R.id.rb4);
    }

    // ===================== DONG HO DEM NGUOC =====================

    private void batDauDemNguoc(int tongGiay) {
        dongHo = new CountDownTimer(tongGiay * 1000L, 1000) {
            @Override
            public void onTick(long conLai) {
                long giay = conLai / 1000;
                tvThoiGian.setText(String.format(Locale.US,
                        "Thời gian còn lại: %02d:%02d", giay / 60, giay % 60));
                // Con duoi 30 giay thi doi nen sang do de canh bao
                tvThoiGian.setBackgroundColor(ContextCompat.getColor(LamBaiActivity.this,
                        giay <= 30 ? R.color.do_saphut : R.color.vang_dongho));
            }

            @Override
            public void onFinish() {
                tvThoiGian.setText("Hết giờ!");
                Toast.makeText(LamBaiActivity.this,
                        "Hết giờ — bài làm được nộp tự động", Toast.LENGTH_LONG).show();
                chamDiem();
            }
        }.start();
    }

    private void dungDongHo() {
        if (dongHo != null) {
            dongHo.cancel();
            dongHo = null;
        }
    }

    @Override
    protected void onDestroy() {
        dungDongHo();
        super.onDestroy();
    }

    // ===================== NAP VA HIEN CAU HOI =====================

    /** Doc cau hoi cua chu de tu SQLite, xao tron thu tu cau va thu tu 4 dap an. */
    private void napCauHoi() {
        Cursor c = db.query("cauhoi", null, "chude = ?", new String[]{chuDe},
                null, null, null);
        while (c.moveToNext()) {
            CauHoi ch = new CauHoi();
            ch.noiDung = c.getString(c.getColumnIndexOrThrow("noidung"));
            ch.dapAn[0] = c.getString(c.getColumnIndexOrThrow("da1"));
            ch.dapAn[1] = c.getString(c.getColumnIndexOrThrow("da2"));
            ch.dapAn[2] = c.getString(c.getColumnIndexOrThrow("da3"));
            ch.dapAn[3] = c.getString(c.getColumnIndexOrThrow("da4"));
            ch.dapAnDung = c.getInt(c.getColumnIndexOrThrow("dapandung"));
            danhSach.add(ch);
        }
        c.close();

        Random rnd = new Random();
        Collections.shuffle(danhSach, rnd);          // doi thu tu cau hoi
        for (CauHoi ch : danhSach) tronDapAn(ch, rnd); // doi thu tu 4 dap an
    }

    /** Xao tron 4 dap an cua mot cau, cap nhat lai vi tri dap an dung. */
    private void tronDapAn(CauHoi ch, Random rnd) {
        Integer[] thuTu = {0, 1, 2, 3};
        Collections.shuffle(Arrays.asList(thuTu), rnd);

        String[] moi = new String[4];
        int dungMoi = ch.dapAnDung;
        for (int i = 0; i < 4; i++) {
            moi[i] = ch.dapAn[thuTu[i]];
            if (thuTu[i] == ch.dapAnDung - 1) dungMoi = i + 1;
        }
        ch.dapAn = moi;
        ch.dapAnDung = dungMoi;
    }

    private void hienCau(int i) {
        viTri = i;
        CauHoi ch = danhSach.get(i);

        tvTienDo.setText("Câu " + (i + 1) + " / " + danhSach.size() + "   —   " + chuDe);
        tvCauHoi.setText(ch.noiDung);
        for (int k = 0; k < 4; k++) {
            rb[k].setText((char) ('A' + k) + ". " + ch.dapAn[k]);
        }

        // Khoi phuc lua chon cu (neu co) ma khong kich hoat listener
        dangNap = true;
        rgDapAn.clearCheck();
        if (traLoi[i] >= 1 && traLoi[i] <= 4) {
            rb[traLoi[i] - 1].setChecked(true);
        }
        dangNap = false;
        capNhatTienDo();
    }

    /** Thanh tien do theo vi tri cau va so cau da tra loi. */
    private void capNhatTienDo() {
        int tong = danhSach.size();
        pbTienDo.setProgress((viTri + 1) * 100 / tong);
        int daLam = 0;
        for (int t : traLoi) if (t != 0) daLam++;
        tvDaTraLoi.setText("Đã trả lời " + daLam + " / " + tong + " câu");
    }

    // ===================== NOP BAI VA CHAM DIEM =====================

    private void hoiNopBai() {
        int chuaLam = 0;
        for (int t : traLoi) if (t == 0) chuaLam++;

        String thongBao = (chuaLam == 0)
                ? "Bạn đã trả lời hết. Nộp bài ngay?"
                : "Bạn còn " + chuaLam + " câu chưa trả lời.\nVẫn nộp bài?";

        new AlertDialog.Builder(this)
                .setTitle("Xác nhận nộp bài")
                .setMessage(thongBao)
                .setPositiveButton("Nộp bài", (d, w) -> chamDiem())
                .setNegativeButton("Làm tiếp", null)
                .show();
    }

    private void chamDiem() {
        if (daNop) return;   // tranh nop hai lan (bam nut dung luc het gio)
        daNop = true;
        dungDongHo();

        int soCau = danhSach.size();
        int soCauDung = 0;
        String[] chiTiet = new String[soCau];
        boolean[] dungSai = new boolean[soCau];

        for (int i = 0; i < soCau; i++) {
            CauHoi ch = danhSach.get(i);
            boolean dung = (traLoi[i] == ch.dapAnDung);
            dungSai[i] = dung;
            if (dung) soCauDung++;

            String daChon = (traLoi[i] == 0)
                    ? "chưa trả lời"
                    : (char) ('A' + traLoi[i] - 1) + "";
            String dapAnDung = (char) ('A' + ch.dapAnDung - 1) + ". " + ch.dapAn[ch.dapAnDung - 1];

            chiTiet[i] = "Câu " + (i + 1) + ": " + (dung ? "ĐÚNG" : "SAI")
                    + "\n" + ch.noiDung
                    + "\nBạn chọn: " + daChon
                    + "\nĐáp án đúng: " + dapAnDung;
        }

        double diem = soCauDung * 10.0 / soCau;

        // Luu ket qua vao bang lichsu
        ContentValues v = new ContentValues();
        v.put("nguoidung", PhienDangNhap.ten(this));
        v.put("chude", chuDe);
        v.put("socau", soCau);
        v.put("socaudung", soCauDung);
        v.put("diem", diem);
        v.put("thoigian", new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.US)
                .format(new Date()));
        db.insert("lichsu", null, v);

        // Chuyen ket qua sang man hinh KetQua bang Bundle
        Intent it = new Intent(LamBaiActivity.this, KetQuaActivity.class);
        Bundle goi = new Bundle();
        goi.putInt("socau", soCau);
        goi.putInt("socaudung", soCauDung);
        int boTrong = 0;
        for (int t : traLoi) if (t == 0) boTrong++;
        goi.putInt("socauboTrong", boTrong);
        goi.putDouble("diem", diem);
        goi.putStringArray("chitiet", chiTiet);
        goi.putBooleanArray("dungsai", dungSai);
        it.putExtra("GoiDuLieu", goi);
        startActivity(it);
        finish();
    }
}

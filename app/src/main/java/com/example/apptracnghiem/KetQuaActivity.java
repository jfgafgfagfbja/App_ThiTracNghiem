package com.example.apptracnghiem;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;

public class KetQuaActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_ket_qua);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });

        TextView tvDiem = findViewById(R.id.tvDiem);
        TextView tvTomTat = findViewById(R.id.tvTomTat);
        ListView lvChiTiet = findViewById(R.id.lvChiTiet);
        Button btnVeTrangChu = findViewById(R.id.btnVeTrangChu);
        TextView tvSoDung = findViewById(R.id.tvSoDung);
        TextView tvSoSai = findViewById(R.id.tvSoSai);
        TextView tvSoBoTrong = findViewById(R.id.tvSoBoTrong);
        ProgressBar pbTyLe = findViewById(R.id.pbTyLe);

        // Nhan ket qua tu LamBaiActivity qua Bundle
        Bundle goi = getIntent().getBundleExtra("GoiDuLieu");
        int soCau = 0, soCauDung = 0, soBoTrong = 0;
        double diem = 0;
        String[] chiTiet = new String[0];
        boolean[] dungSai = new boolean[0];
        if (goi != null) {
            soCau = goi.getInt("socau");
            soCauDung = goi.getInt("socaudung");
            soBoTrong = goi.getInt("socauboTrong");
            diem = goi.getDouble("diem");
            String[] ct = goi.getStringArray("chitiet");
            if (ct != null) chiTiet = ct;
            boolean[] ds = goi.getBooleanArray("dungsai");
            if (ds != null) dungSai = ds;
        }

        tvDiem.setText(String.format(Locale.US, "%.1f / 10", diem));
        tvTomTat.setText("Trả lời đúng " + soCauDung + " / " + soCau + " câu   •   "
                + xepLoai(diem));

        tvSoDung.setText(String.valueOf(soCauDung));
        tvSoSai.setText(String.valueOf(soCau - soCauDung - soBoTrong));
        tvSoBoTrong.setText(String.valueOf(soBoTrong));
        pbTyLe.setProgress(soCau == 0 ? 0 : soCauDung * 100 / soCau);

        ArrayList<String> ds = new ArrayList<>(Arrays.asList(chiTiet));
        lvChiTiet.setAdapter(new ChiTietAdapter(this, ds, dungSai));

        btnVeTrangChu.setOnClickListener(v -> finish());
    }

    private String xepLoai(double diem) {
        if (diem >= 9) return "Xuất sắc";
        if (diem >= 8) return "Giỏi";
        if (diem >= 6.5) return "Khá";
        if (diem >= 5) return "Trung bình";
        return "Chưa đạt";
    }
}

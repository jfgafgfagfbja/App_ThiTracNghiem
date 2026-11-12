package com.example.apptracnghiem;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import java.util.Locale;

/** Tab 1 - tong quan nhanh, chon chu de va bat dau lam bai. */
public class ChonDeFragment extends Fragment {

    private Spinner spChuDe;
    private TextView tvSoCau, tvThoiGianDuKien;
    private TextView tvSoChuDe, tvTongCau, tvSoLanLam, tvDiemTB, tvGanNhat;
    private View khoiGanNhat;

    private SQLiteDatabase db;
    private String chuDeDangChon = "";

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chon_de, container, false);

        spChuDe = view.findViewById(R.id.spChuDe);
        tvSoCau = view.findViewById(R.id.tvSoCau);
        tvThoiGianDuKien = view.findViewById(R.id.tvThoiGianDuKien);
        tvSoChuDe = view.findViewById(R.id.tvSoChuDe);
        tvTongCau = view.findViewById(R.id.tvTongCau);
        tvSoLanLam = view.findViewById(R.id.tvSoLanLam);
        tvDiemTB = view.findViewById(R.id.tvDiemTB);
        tvGanNhat = view.findViewById(R.id.tvGanNhat);
        khoiGanNhat = view.findViewById(R.id.khoiGanNhat);

        Button btnBatDau = view.findViewById(R.id.btnBatDau);
        Button btnThoat = view.findViewById(R.id.btnThoat);

        db = CSDL.mo(requireContext());

        spChuDe.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View v, int position, long id) {
                chuDeDangChon = parent.getItemAtPosition(position).toString();
                capNhatThongTinDe();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        btnBatDau.setOnClickListener(v -> {
            if (chuDeDangChon.isEmpty() || CSDL.demCauHoi(db, chuDeDangChon) == 0) {
                Toast.makeText(requireContext(),
                        "Chủ đề này chưa có câu hỏi — vào tab QUẢN TRỊ để thêm",
                        Toast.LENGTH_LONG).show();
                return;
            }
            Intent it = new Intent(requireContext(), LamBaiActivity.class);
            Bundle goi = new Bundle();
            goi.putString("chude", chuDeDangChon);
            it.putExtra("GoiDuLieu", goi);
            startActivity(it);
        });

        btnThoat.setOnClickListener(v -> new AlertDialog.Builder(requireContext())
                .setTitle("Xác nhận thoát")
                .setMessage("Bạn có chắc chắn muốn thoát ứng dụng?")
                .setPositiveButton("Có", (d, w) -> requireActivity().finishAffinity())
                .setNegativeButton("Không", null)
                .show());

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        napLai();
    }

    private void napLai() {
        if (db == null) return;

        // Spinner chu de (co the vua them/xoa o tab Quan tri)
        String[] ds = CSDL.layDanhSachChuDe(db);
        ArrayAdapter<String> ad = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, ds);
        ad.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spChuDe.setAdapter(ad);

        tvSoChuDe.setText(String.valueOf(ds.length));
        tvTongCau.setText(String.valueOf(CSDL.demCauHoi(db)));

        if (ds.length == 0) {
            chuDeDangChon = "";
            capNhatThongTinDe();
        }

        capNhatThongKeNhanh();
    }

    /** So cau va thoi gian du kien cua chu de dang chon. */
    private void capNhatThongTinDe() {
        if (chuDeDangChon.isEmpty()) {
            tvSoCau.setText("—");
            tvThoiGianDuKien.setText("—");
            return;
        }
        int n = CSDL.demCauHoi(db, chuDeDangChon);
        int giay = n * LamBaiActivity.GIAY_MOI_CAU;
        tvSoCau.setText(n + " câu");
        tvThoiGianDuKien.setText(String.format(Locale.US, "%02d:%02d", giay / 60, giay % 60));
    }

    /** So lan da lam, diem trung binh va lan lam gan nhat. */
    private void capNhatThongKeNhanh() {
        String ai = PhienDangNhap.ten(requireContext());
        Cursor c = db.rawQuery("SELECT COUNT(*), AVG(diem) FROM lichsu WHERE nguoidung = ?",
                new String[]{ai});
        int soLan = 0;
        if (c.moveToFirst()) {
            soLan = c.getInt(0);
            tvSoLanLam.setText(String.valueOf(soLan));
            tvDiemTB.setText(soLan > 0
                    ? String.format(Locale.US, "%.1f", c.getDouble(1)) : "—");
        }
        c.close();

        if (soLan == 0) {
            khoiGanNhat.setVisibility(View.GONE);
            return;
        }

        khoiGanNhat.setVisibility(View.VISIBLE);
        Cursor c2 = db.query("lichsu", null, "nguoidung = ?", new String[]{ai},
                null, null, "id DESC", "1");
        if (c2.moveToFirst()) {
            tvGanNhat.setText(String.format(Locale.US,
                    "%s%nĐúng %d/%d câu  •  %.1f điểm%n%s",
                    c2.getString(c2.getColumnIndexOrThrow("chude")),
                    c2.getInt(c2.getColumnIndexOrThrow("socaudung")),
                    c2.getInt(c2.getColumnIndexOrThrow("socau")),
                    c2.getDouble(c2.getColumnIndexOrThrow("diem")),
                    c2.getString(c2.getColumnIndexOrThrow("thoigian"))));
        }
        c2.close();
    }
}

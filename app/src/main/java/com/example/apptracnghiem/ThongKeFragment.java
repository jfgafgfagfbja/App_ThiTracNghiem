package com.example.apptracnghiem;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;

/** Tab 3 - thong ke ket qua bang cac ham tong hop SQL. */
public class ThongKeFragment extends Fragment {

    private SQLiteDatabase db;
    private TextView tvTongQuan;
    private ListView lv;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_thong_ke, container, false);

        tvTongQuan = view.findViewById(R.id.tvTongQuan);
        lv = view.findViewById(R.id.lvThongKe);

        lv.setEmptyView(view.findViewById(R.id.khoiTrongThongKe));
        db = CSDL.mo(requireContext());
        view.findViewById(R.id.btnXuatBaoCao).setOnClickListener(v -> xuatBaoCao());
        napLai();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        napLai();   // cap nhat sau khi vua lam xong mot bai
    }

    private void napLai() {
        if (db == null) return;

        String ai = PhienDangNhap.ten(requireContext());
        Cursor c = db.rawQuery(
                "SELECT COUNT(*), AVG(diem), MAX(diem), MIN(diem) FROM lichsu "
                        + "WHERE nguoidung = ?", new String[]{ai});
        if (c.moveToFirst() && c.getInt(0) > 0) {
            tvTongQuan.setText(String.format(Locale.US,
                    "Tổng số lần làm bài: %d%nĐiểm trung bình: %.1f%nĐiểm cao nhất: %.1f"
                            + "%nĐiểm thấp nhất: %.1f",
                    c.getInt(0), c.getDouble(1), c.getDouble(2), c.getDouble(3)));
        } else {
            tvTongQuan.setText("Chưa có lần làm bài nào. Hãy làm một bài để xem thống kê.");
        }
        c.close();

        ArrayList<String> dong = new ArrayList<>();
        Cursor c2 = db.rawQuery(
                "SELECT chude, COUNT(*), AVG(diem), MAX(diem), SUM(socaudung), SUM(socau) "
                        + "FROM lichsu WHERE nguoidung = ? "
                        + "GROUP BY chude ORDER BY chude", new String[]{ai});
        while (c2.moveToNext()) {
            int tongDung = c2.getInt(4);
            int tongCau = c2.getInt(5);
            double tyLe = tongCau == 0 ? 0 : tongDung * 100.0 / tongCau;
            dong.add(String.format(Locale.US,
                    "%s%nSố lần làm: %d   •   Điểm TB: %.1f   •   Cao nhất: %.1f"
                            + "%nTỷ lệ trả lời đúng: %d/%d câu (%.0f%%)",
                    c2.getString(0), c2.getInt(1), c2.getDouble(2), c2.getDouble(3),
                    tongDung, tongCau, tyLe));
        }
        c2.close();

        lv.setAdapter(new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_list_item_1, dong));
    }

    /** Ghi bao cao ra tap tin baocao.txt (kien thuc doc ghi file - chuong 5). */
    private void xuatBaoCao() {
        try {
            int n = TapTin.xuatBaoCao(requireContext(), db,
                    PhienDangNhap.ten(requireContext()));
            new AlertDialog.Builder(requireContext())
                    .setTitle("Đã xuất báo cáo")
                    .setMessage("Đã ghi " + n + " lần làm bài vào tập tin "
                            + TapTin.FILE_BAO_CAO + " trong thư mục files của ứng dụng.")
                    .setPositiveButton("OK", null)
                    .show();
        } catch (IOException e) {
            Toast.makeText(requireContext(), "Lỗi khi ghi tập tin: " + e.getMessage(),
                    Toast.LENGTH_LONG).show();
        }
    }
}

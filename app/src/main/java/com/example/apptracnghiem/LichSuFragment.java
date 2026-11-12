package com.example.apptracnghiem;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.Locale;

/** Tab 2 - lich su cac lan lam bai cua rieng nguoi dang dang nhap. */
public class LichSuFragment extends Fragment {

    private SQLiteDatabase db;
    private final ArrayList<String> dong = new ArrayList<>();
    private final ArrayList<Integer> maSo = new ArrayList<>();
    private ArrayAdapter<String> adapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_lich_su, container, false);

        ListView lv = view.findViewById(R.id.lvLichSu);
        Button btnXoaHet = view.findViewById(R.id.btnXoaHet);

        db = CSDL.mo(requireContext());
        adapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_list_item_1, dong);
        lv.setAdapter(adapter);
        lv.setEmptyView(view.findViewById(R.id.khoiTrongLichSu));
        napLai();

        lv.setOnItemLongClickListener((parent, v, position, id) -> {
            new AlertDialog.Builder(requireContext())
                    .setTitle("Xoá lần làm bài này?")
                    .setMessage(dong.get(position))
                    .setPositiveButton("Xoá", (d, w) -> {
                        db.delete("lichsu", "id = ?",
                                new String[]{String.valueOf(maSo.get(position))});
                        napLai();
                        Toast.makeText(requireContext(), "Đã xoá", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("Huỷ", null)
                    .show();
            return true;
        });

        btnXoaHet.setOnClickListener(v -> {
            if (dong.isEmpty()) {
                Toast.makeText(requireContext(), "Chưa có lịch sử nào",
                        Toast.LENGTH_SHORT).show();
                return;
            }
            new AlertDialog.Builder(requireContext())
                    .setTitle("Xoá tất cả lịch sử?")
                    .setMessage("Toàn bộ " + dong.size() + " lần làm bài của bạn sẽ bị xoá."
                            + "\nLịch sử của người dùng khác không bị ảnh hưởng.")
                    .setPositiveButton("Xoá hết", (d, w) -> {
                        db.delete("lichsu", "nguoidung = ?", new String[]{ai()});
                        napLai();
                        Toast.makeText(requireContext(), "Đã xoá tất cả",
                                Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("Huỷ", null)
                    .show();
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        napLai();   // cap nhat sau khi vua lam xong mot bai
    }

    /** Ten dang nhap cua nguoi dang dung ung dung. */
    private String ai() {
        return PhienDangNhap.ten(requireContext());
    }

    private void napLai() {
        if (db == null || adapter == null) return;
        dong.clear();
        maSo.clear();
        Cursor c = db.query("lichsu", null, "nguoidung = ?", new String[]{ai()},
                null, null, "id DESC");
        while (c.moveToNext()) {
            maSo.add(c.getInt(c.getColumnIndexOrThrow("id")));
            dong.add(String.format(Locale.US, "%s  •  %.1f điểm%nĐúng %d/%d câu  •  %s",
                    c.getString(c.getColumnIndexOrThrow("chude")),
                    c.getDouble(c.getColumnIndexOrThrow("diem")),
                    c.getInt(c.getColumnIndexOrThrow("socaudung")),
                    c.getInt(c.getColumnIndexOrThrow("socau")),
                    c.getString(c.getColumnIndexOrThrow("thoigian"))));
        }
        c.close();
        adapter.notifyDataSetChanged();
    }
}

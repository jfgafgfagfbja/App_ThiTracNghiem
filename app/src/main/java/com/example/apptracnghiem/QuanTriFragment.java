package com.example.apptracnghiem;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;

/** Tab 4 - them/sua/xoa chu de va cau hoi; nhap va xuat tap tin. */
public class QuanTriFragment extends Fragment {

    private Spinner spChuDe;
    private EditText edtChuDeMoi, edtNoiDung;
    private final EditText[] edtDa = new EditText[4];
    private RadioGroup rgDung;
    private final RadioButton[] rbD = new RadioButton[4];
    private ListView lvCauHoi;
    private TextView tvSoCau;

    private SQLiteDatabase db;
    private String chuDeDangChon = "";

    private final ArrayList<String> dong = new ArrayList<>();
    private final ArrayList<Integer> maSo = new ArrayList<>();
    private ArrayAdapter<String> adapter;

    /** Id cau hoi dang duoc chon de sua; -1 nghia la dang nhap cau moi. */
    private int idDangSua = -1;

    /** Bo chon tap tin de NHAP - nguoi dung tu chon file bat ky tren may. */
    private final ActivityResultLauncher<String[]> chonFileNhap =
            registerForActivityResult(new ActivityResultContracts.OpenDocument(), uri -> {
                if (uri == null) return;                 // nguoi dung bam Huy
                try (InputStream is = requireContext().getContentResolver().openInputStream(uri)) {
                    if (is == null) throw new IOException("Không mở được tập tin");
                    int[] kq = TapTin.nhapCauHoi(db, is);
                    napSpinnerChuDe(chuDeDangChon.isEmpty() ? null : chuDeDangChon);
                    napDanhSach();
                    new AlertDialog.Builder(requireContext())
                            .setTitle("Kết quả nhập tập tin")
                            .setMessage("Tên tập tin: " + tenTapTin(uri)
                                    + "\nThêm mới: " + kq[0] + " câu"
                                    + "\nĐã có, bỏ qua: " + kq[1] + " câu"
                                    + "\nSai định dạng: " + kq[2] + " dòng")
                            .setPositiveButton("OK", null)
                            .show();
                } catch (Exception e) {
                    Toast.makeText(requireContext(), "Lỗi khi đọc tập tin: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                }
            });

    /** Bo chon noi luu khi XUAT tap tin. */
    private final ActivityResultLauncher<String> chonNoiLuu =
            registerForActivityResult(
                    new ActivityResultContracts.CreateDocument("text/plain"), uri -> {
                        if (uri == null) return;
                        try (OutputStream os =
                                     requireContext().getContentResolver().openOutputStream(uri)) {
                            if (os == null) throw new IOException("Không ghi được tập tin");
                            int n = TapTin.xuatCauHoi(db, os);
                            new AlertDialog.Builder(requireContext())
                                    .setTitle("Đã xuất tập tin")
                                    .setMessage("Đã ghi " + n + " câu hỏi vào "
                                            + tenTapTin(uri))
                                    .setPositiveButton("OK", null)
                                    .show();
                        } catch (Exception e) {
                            Toast.makeText(requireContext(),
                                    "Lỗi khi ghi tập tin: " + e.getMessage(),
                                    Toast.LENGTH_LONG).show();
                        }
                    });

    /** Lay ten hien thi cua tap tin tu Uri. */
    private String tenTapTin(Uri uri) {
        String s = uri.getLastPathSegment();
        if (s == null) return "tập tin đã chọn";
        int i = s.lastIndexOf('/');
        return i >= 0 ? s.substring(i + 1) : s;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_quan_tri, container, false);

        anhXa(view);
        db = CSDL.mo(requireContext());

        adapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_list_item_1, dong);
        lvCauHoi.setAdapter(adapter);
        lvCauHoi.setEmptyView(view.findViewById(R.id.khoiTrongCauHoi));

        spChuDe.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View v, int position, long id) {
                chuDeDangChon = parent.getItemAtPosition(position).toString();
                napDanhSach();
                lamMoi();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        napSpinnerChuDe(null);

        lvCauHoi.setOnItemClickListener((parent, v, position, id) -> doLenForm(maSo.get(position)));

        view.findViewById(R.id.btnThemChuDe).setOnClickListener(v -> themChuDe());
        view.findViewById(R.id.btnXoaChuDe).setOnClickListener(v -> hoiXoaChuDe());
        view.findViewById(R.id.btnThem).setOnClickListener(v -> them());
        view.findViewById(R.id.btnSua).setOnClickListener(v -> sua());
        view.findViewById(R.id.btnXoa).setOnClickListener(v -> hoiXoa());
        view.findViewById(R.id.btnLamMoi).setOnClickListener(v -> lamMoi());
        view.findViewById(R.id.btnXuatFile).setOnClickListener(v -> xuatFile());
        view.findViewById(R.id.btnNhapFile).setOnClickListener(v -> hoiNhapFile());
        view.findViewById(R.id.btnNguoiDung).setOnClickListener(v -> quanLyNguoiDung());

        return view;
    }

    private void anhXa(View view) {
        spChuDe = view.findViewById(R.id.spChuDeQT);
        edtChuDeMoi = view.findViewById(R.id.edtChuDeMoi);
        edtNoiDung = view.findViewById(R.id.edtNoiDung);
        edtDa[0] = view.findViewById(R.id.edtDa1);
        edtDa[1] = view.findViewById(R.id.edtDa2);
        edtDa[2] = view.findViewById(R.id.edtDa3);
        edtDa[3] = view.findViewById(R.id.edtDa4);
        rgDung = view.findViewById(R.id.rgDung);
        rbD[0] = view.findViewById(R.id.rbD1);
        rbD[1] = view.findViewById(R.id.rbD2);
        rbD[2] = view.findViewById(R.id.rbD3);
        rbD[3] = view.findViewById(R.id.rbD4);
        lvCauHoi = view.findViewById(R.id.lvCauHoi);
        tvSoCau = view.findViewById(R.id.tvSoCauQT);
    }

    // ===================== CHU DE =====================

    private void napSpinnerChuDe(String chonTen) {
        String[] ds = CSDL.layDanhSachChuDe(db);
        ArrayAdapter<String> ad = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, ds);
        ad.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spChuDe.setAdapter(ad);

        if (ds.length == 0) {
            chuDeDangChon = "";
            dong.clear();
            maSo.clear();
            adapter.notifyDataSetChanged();
            tvSoCau.setText("Chưa có chủ đề nào — hãy thêm chủ đề mới");
            return;
        }
        int vt = (chonTen == null) ? 0 : Math.max(0, Arrays.asList(ds).indexOf(chonTen));
        spChuDe.setSelection(vt);
    }

    private void themChuDe() {
        String ten = edtChuDeMoi.getText().toString().trim();
        if (ten.isEmpty()) {
            Toast.makeText(requireContext(), "Hãy nhập tên chủ đề mới",
                    Toast.LENGTH_SHORT).show();
            return;
        }
        if (CSDL.themChuDe(db, ten)) {
            edtChuDeMoi.setText("");
            napSpinnerChuDe(ten);
            Toast.makeText(requireContext(), "Đã thêm chủ đề: " + ten,
                    Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(requireContext(), "Chủ đề này đã tồn tại",
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void hoiXoaChuDe() {
        if (chuDeDangChon.isEmpty()) {
            Toast.makeText(requireContext(), "Chưa có chủ đề nào để xoá",
                    Toast.LENGTH_SHORT).show();
            return;
        }
        int soCau = CSDL.demCauHoi(db, chuDeDangChon);
        new AlertDialog.Builder(requireContext())
                .setTitle("Xoá chủ đề?")
                .setMessage("Chủ đề \"" + chuDeDangChon + "\" và " + soCau
                        + " câu hỏi thuộc chủ đề này sẽ bị xoá vĩnh viễn.")
                .setPositiveButton("Xoá", (d, w) -> {
                    int n = CSDL.xoaChuDe(db, chuDeDangChon);
                    Toast.makeText(requireContext(), "Đã xoá chủ đề và " + n + " câu hỏi",
                            Toast.LENGTH_SHORT).show();
                    napSpinnerChuDe(null);
                    lamMoi();
                })
                .setNegativeButton("Huỷ", null)
                .show();
    }

    // ===================== NHAP / XUAT TAP TIN =====================

    private void xuatFile() {
        // Mo hop thoai cho nguoi dung chon noi luu (Downloads, Documents...)
        chonNoiLuu.launch(TapTin.FILE_CAU_HOI);
    }

    private void hoiNhapFile() {
        // Mo hop thoai cho nguoi dung chon tap tin .txt tren may
        chonFileNhap.launch(new String[]{"text/*", "application/octet-stream"});
    }

    // ===================== DANH SACH CAU HOI =====================

    private void napDanhSach() {
        dong.clear();
        maSo.clear();
        Cursor c = db.query("cauhoi", null, "chude = ?", new String[]{chuDeDangChon},
                null, null, "id");
        int stt = 1;
        while (c.moveToNext()) {
            maSo.add(c.getInt(c.getColumnIndexOrThrow("id")));
            dong.add(stt++ + ". " + c.getString(c.getColumnIndexOrThrow("noidung")));
        }
        c.close();
        adapter.notifyDataSetChanged();
        tvSoCau.setText("Danh sách câu hỏi: " + dong.size() + " câu (bấm để sửa)");
    }

    private void doLenForm(int id) {
        Cursor c = db.query("cauhoi", null, "id = ?", new String[]{String.valueOf(id)},
                null, null, null);
        if (c.moveToFirst()) {
            idDangSua = id;
            edtNoiDung.setText(c.getString(c.getColumnIndexOrThrow("noidung")));
            edtDa[0].setText(c.getString(c.getColumnIndexOrThrow("da1")));
            edtDa[1].setText(c.getString(c.getColumnIndexOrThrow("da2")));
            edtDa[2].setText(c.getString(c.getColumnIndexOrThrow("da3")));
            edtDa[3].setText(c.getString(c.getColumnIndexOrThrow("da4")));
            int dung = c.getInt(c.getColumnIndexOrThrow("dapandung"));
            if (dung >= 1 && dung <= 4) rbD[dung - 1].setChecked(true);
            Toast.makeText(requireContext(), "Đang sửa câu số " + id,
                    Toast.LENGTH_SHORT).show();
        }
        c.close();
    }

    private void lamMoi() {
        idDangSua = -1;
        edtNoiDung.setText("");
        for (EditText e : edtDa) e.setText("");
        rgDung.check(R.id.rbD1);
    }

    // ===================== THEM / SUA / XOA CAU HOI =====================

    private ContentValues docForm() {
        if (chuDeDangChon.isEmpty()) {
            Toast.makeText(requireContext(), "Hãy tạo một chủ đề trước",
                    Toast.LENGTH_SHORT).show();
            return null;
        }
        String noiDung = edtNoiDung.getText().toString().trim();
        String[] da = new String[4];
        for (int i = 0; i < 4; i++) da[i] = edtDa[i].getText().toString().trim();

        if (noiDung.isEmpty() || da[0].isEmpty() || da[1].isEmpty()
                || da[2].isEmpty() || da[3].isEmpty()) {
            Toast.makeText(requireContext(), "Vui lòng nhập nội dung và đủ 4 đáp án!",
                    Toast.LENGTH_SHORT).show();
            return null;
        }

        int dung = 1;
        int chon = rgDung.getCheckedRadioButtonId();
        if (chon == R.id.rbD2) dung = 2;
        else if (chon == R.id.rbD3) dung = 3;
        else if (chon == R.id.rbD4) dung = 4;

        ContentValues v = new ContentValues();
        v.put("chude", chuDeDangChon);
        v.put("noidung", noiDung);
        v.put("da1", da[0]);
        v.put("da2", da[1]);
        v.put("da3", da[2]);
        v.put("da4", da[3]);
        v.put("dapandung", dung);
        return v;
    }

    private void them() {
        ContentValues v = docForm();
        if (v == null) return;

        long kq = db.insert("cauhoi", null, v);
        if (kq == -1) {
            Toast.makeText(requireContext(), "Thêm câu hỏi thất bại!",
                    Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(requireContext(), "Đã thêm câu hỏi mới",
                    Toast.LENGTH_SHORT).show();
            napDanhSach();
            lamMoi();
        }
    }

    private void sua() {
        if (idDangSua == -1) {
            Toast.makeText(requireContext(), "Hãy bấm chọn một câu hỏi trong danh sách trước",
                    Toast.LENGTH_SHORT).show();
            return;
        }
        ContentValues v = docForm();
        if (v == null) return;

        int n = db.update("cauhoi", v, "id = ?", new String[]{String.valueOf(idDangSua)});
        Toast.makeText(requireContext(),
                n > 0 ? "Đã cập nhật câu hỏi" : "Không có câu hỏi nào được sửa",
                Toast.LENGTH_SHORT).show();
        napDanhSach();
        lamMoi();
    }

    private void hoiXoa() {
        if (idDangSua == -1) {
            Toast.makeText(requireContext(), "Hãy bấm chọn một câu hỏi trong danh sách trước",
                    Toast.LENGTH_SHORT).show();
            return;
        }
        new AlertDialog.Builder(requireContext())
                .setTitle("Xoá câu hỏi?")
                .setMessage(edtNoiDung.getText().toString())
                .setPositiveButton("Xoá", (d, w) -> {
                    int n = db.delete("cauhoi", "id = ?",
                            new String[]{String.valueOf(idDangSua)});
                    Toast.makeText(requireContext(), n > 0 ? "Đã xoá câu hỏi" : "Không xoá được",
                            Toast.LENGTH_SHORT).show();
                    napDanhSach();
                    lamMoi();
                })
                .setNegativeButton("Huỷ", null)
                .show();
    }

    // ===================== QUAN LY TAI KHOAN =====================

    /** Liet ke tat ca tai khoan; bam vao mot dong de xoa tai khoan do. */
    private void quanLyNguoiDung() {
        final java.util.ArrayList<CSDL.NguoiDung> ds = CSDL.layDanhSachNguoiDung(db);
        String[] dong = new String[ds.size()];
        for (int i = 0; i < ds.size(); i++) {
            CSDL.NguoiDung nd = ds.get(i);
            String diem = nd.soLanLam > 0
                    ? String.format(java.util.Locale.US, "  •  điểm TB %.1f", nd.diemTB)
                    : "";
            dong[i] = nd.hoTen + "  (@" + nd.ten + ")"
                    + (CSDL.VAI_TRO_ADMIN.equals(nd.vaiTro) ? "   [quản trị]" : "")
                    + "\n" + nd.soLanLam + " lần làm bài" + diem;
        }

        new AlertDialog.Builder(requireContext())
                .setTitle("Tài khoản (" + ds.size() + ")")
                .setItems(dong, (d, i) -> hoiXoaNguoiDung(ds.get(i)))
                .setNegativeButton("Đóng", null)
                .show();
    }

    private void hoiXoaNguoiDung(CSDL.NguoiDung nd) {
        if (nd.ten.equals(PhienDangNhap.ten(requireContext()))) {
            Toast.makeText(requireContext(),
                    "Không thể xoá tài khoản đang đăng nhập", Toast.LENGTH_LONG).show();
            return;
        }
        if (CSDL.VAI_TRO_ADMIN.equals(nd.vaiTro)) {
            Toast.makeText(requireContext(),
                    "Không xoá được tài khoản quản trị khác", Toast.LENGTH_LONG).show();
            return;
        }

        new AlertDialog.Builder(requireContext())
                .setTitle("Xoá tài khoản @" + nd.ten + "?")
                .setMessage("Tài khoản này và " + nd.soLanLam
                        + " lần làm bài của họ sẽ bị xoá."
                        + "\nNgân hàng câu hỏi không bị ảnh hưởng.")
                .setPositiveButton("Xoá", (d, w) -> {
                    int n = CSDL.xoaNguoiDung(db, nd.ten);
                    Toast.makeText(requireContext(),
                            "Đã xoá tài khoản và " + n + " lần làm bài",
                            Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Huỷ", null)
                .show();
    }
}

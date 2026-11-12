package com.example.apptracnghiem;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Locale;

/**
 * Doc / ghi tap tin (kien thuc chuong 5).
 *
 * Cac ham chinh lam viec truc tiep voi InputStream / OutputStream nen dung duoc
 * cho ca tap tin rieng cua ung dung lan tap tin nguoi dung tu chon ngoai bo nho.
 *
 * Dinh dang tap tin cau hoi - moi dong mot cau, cac truong cach nhau bang dau |
 *   chu de|noi dung cau hoi|dap an A|dap an B|dap an C|dap an D|so dap an dung (1-4)
 * Dong trong hoac dong bat dau bang # duoc bo qua.
 */
public class TapTin {

    public static final String FILE_CAU_HOI = "cauhoi.txt";
    public static final String FILE_BAO_CAO = "baocao.txt";

    private static final String NGAN = "\\|";   // dau phan cach khi tach chuoi

    // ===================== XUAT CAU HOI =====================

    /** Ghi ngan hang cau hoi ra mot luong bat ky. Tra ve so cau da ghi. */
    public static int xuatCauHoi(SQLiteDatabase db, OutputStream os) throws IOException {
        OutputStreamWriter writer = new OutputStreamWriter(os, "UTF-8");

        writer.write("# NGAN HANG CAU HOI - UNG DUNG TRAC NGHIEM\n");
        writer.write("# Moi dong mot cau hoi, cac truong cach nhau bang dau |\n");
        writer.write("# chu de|noi dung|dap an A|dap an B|dap an C|dap an D|dap an dung (1-4)\n");
        writer.write("# Luu y: khong dung ky tu | trong noi dung cau hoi hay dap an.\n\n");

        int dem = 0;
        Cursor c = db.query("cauhoi", null, null, null, null, null, "chude, id");
        while (c.moveToNext()) {
            writer.write(c.getString(c.getColumnIndexOrThrow("chude")) + "|"
                    + c.getString(c.getColumnIndexOrThrow("noidung")) + "|"
                    + c.getString(c.getColumnIndexOrThrow("da1")) + "|"
                    + c.getString(c.getColumnIndexOrThrow("da2")) + "|"
                    + c.getString(c.getColumnIndexOrThrow("da3")) + "|"
                    + c.getString(c.getColumnIndexOrThrow("da4")) + "|"
                    + c.getInt(c.getColumnIndexOrThrow("dapandung")) + "\n");
            dem++;
        }
        c.close();
        writer.flush();
        writer.close();
        return dem;
    }

    /** Ghi ra tap tin rieng cua ung dung (thu muc files). */
    public static int xuatCauHoi(Context ctx, SQLiteDatabase db) throws IOException {
        return xuatCauHoi(db, ctx.openFileOutput(FILE_CAU_HOI, Context.MODE_PRIVATE));
    }

    // ===================== NHAP CAU HOI =====================

    /**
     * Doc cau hoi tu mot luong bat ky va them vao CSDL.
     * Cau da co san (cung chu de + cung noi dung) se duoc bo qua.
     * Tra ve mang {so cau them moi, so cau da co bi bo qua, so dong bi loi}.
     */
    public static int[] nhapCauHoi(SQLiteDatabase db, InputStream is) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));

        int them = 0, trung = 0, loi = 0;
        String dong;
        while ((dong = reader.readLine()) != null) {
            dong = dong.trim();
            if (dong.isEmpty() || dong.startsWith("#")) continue;   // bo qua ghi chu

            String[] p = dong.split(NGAN, -1);
            if (p.length != 7) {
                loi++;
                continue;
            }

            int dapAnDung;
            try {
                dapAnDung = Integer.parseInt(p[6].trim());
            } catch (NumberFormatException e) {
                loi++;
                continue;
            }

            if (CSDL.coCauHoi(db, p[0], p[1])) {   // da co roi thi bo qua
                trung++;
                continue;
            }
            boolean ok = CSDL.themCauHoi(db, p[0], p[1], p[2], p[3], p[4], p[5], dapAnDung);
            if (ok) them++;
            else loi++;
        }
        reader.close();
        return new int[]{them, trung, loi};
    }

    /** Doc tu tap tin rieng cua ung dung (thu muc files). */
    public static int[] nhapCauHoi(Context ctx, SQLiteDatabase db) throws IOException {
        return nhapCauHoi(db, ctx.openFileInput(FILE_CAU_HOI));
    }

    // ===================== XUAT BAO CAO =====================

    /** Ghi bao cao lich su va thong ke ra mot luong. Tra ve so lan lam bai da ghi. */
    public static int xuatBaoCao(SQLiteDatabase db, OutputStream os, String nguoiDung)
            throws IOException {
        OutputStreamWriter writer = new OutputStreamWriter(os, "UTF-8");

        writer.write("=========================================\n");
        writer.write("   BAO CAO KET QUA - UNG DUNG TRAC NGHIEM\n");
        writer.write("=========================================\n");
        writer.write("Tai khoan: " + nguoiDung + "\n\n");

        String[] ai = new String[]{nguoiDung};
        Cursor c = db.rawQuery(
                "SELECT COUNT(*), AVG(diem), MAX(diem), MIN(diem) FROM lichsu "
                        + "WHERE nguoidung = ?", ai);
        if (c.moveToFirst() && c.getInt(0) > 0) {
            writer.write(String.format(Locale.US,
                    "Tong so lan lam bai : %d\nDiem trung binh     : %.1f\n"
                            + "Diem cao nhat       : %.1f\nDiem thap nhat      : %.1f\n\n",
                    c.getInt(0), c.getDouble(1), c.getDouble(2), c.getDouble(3)));
        } else {
            writer.write("Chua co lan lam bai nao.\n\n");
        }
        c.close();

        writer.write("--- THONG KE THEO CHU DE ---\n");
        Cursor c2 = db.rawQuery("SELECT chude, COUNT(*), AVG(diem), MAX(diem) "
                + "FROM lichsu WHERE nguoidung = ? "
                + "GROUP BY chude ORDER BY chude", ai);
        while (c2.moveToNext()) {
            writer.write(String.format(Locale.US,
                    "%s: %d lan, diem TB %.1f, cao nhat %.1f\n",
                    c2.getString(0), c2.getInt(1), c2.getDouble(2), c2.getDouble(3)));
        }
        c2.close();

        writer.write("\n--- CHI TIET CAC LAN LAM BAI ---\n");
        int dem = 0;
        Cursor c3 = db.query("lichsu", null, "nguoidung = ?", ai,
                null, null, "id DESC");
        while (c3.moveToNext()) {
            writer.write(String.format(Locale.US, "%s | %s | dung %d/%d cau | %.1f diem\n",
                    c3.getString(c3.getColumnIndexOrThrow("thoigian")),
                    c3.getString(c3.getColumnIndexOrThrow("chude")),
                    c3.getInt(c3.getColumnIndexOrThrow("socaudung")),
                    c3.getInt(c3.getColumnIndexOrThrow("socau")),
                    c3.getDouble(c3.getColumnIndexOrThrow("diem"))));
            dem++;
        }
        c3.close();

        writer.flush();
        writer.close();
        return dem;
    }

    /** Ghi bao cao ra tap tin rieng cua ung dung. */
    public static int xuatBaoCao(Context ctx, SQLiteDatabase db, String nguoiDung)
            throws IOException {
        return xuatBaoCao(db, ctx.openFileOutput(FILE_BAO_CAO, Context.MODE_PRIVATE),
                nguoiDung);
    }
}

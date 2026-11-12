package com.example.apptracnghiem;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Luu phien dang nhap bang SharedPreferences.
 *
 * SharedPreferences la noi luu cac gia tri nho (chuoi, so, boolean) theo tung cap
 * khoa - gia tri. Du lieu nam trong vung rieng cua ung dung va con nguyen ca khi
 * ung dung bi tat han, nen rat thich hop de ghi nho "ai dang dang nhap".
 *
 * Lop nay chi gom ham static, khong can tao doi tuong.
 */
public class PhienDangNhap {

    private static final String TEP = "phien_dang_nhap";

    private static final String K_TEN = "tendangnhap";
    private static final String K_HO_TEN = "hoten";
    private static final String K_VAI_TRO = "vaitro";
    private static final String K_GHI_NHO = "ghinho";

    private static SharedPreferences pref(Context ctx) {
        return ctx.getSharedPreferences(TEP, Context.MODE_PRIVATE);
    }

    /** Ghi lai thong tin nguoi dung vua dang nhap thanh cong. */
    public static void luu(Context ctx, CSDL.NguoiDung nd, boolean ghiNho) {
        pref(ctx).edit()
                .putString(K_TEN, nd.ten)
                .putString(K_HO_TEN, nd.hoTen)
                .putString(K_VAI_TRO, nd.vaiTro)
                .putBoolean(K_GHI_NHO, ghiNho)
                .apply();
    }

    /** Xoa phien - dung khi dang xuat. */
    public static void xoa(Context ctx) {
        pref(ctx).edit().clear().apply();
    }

    public static String ten(Context ctx) {
        return pref(ctx).getString(K_TEN, "");
    }

    public static String hoTen(Context ctx) {
        return pref(ctx).getString(K_HO_TEN, "");
    }

    public static String vaiTro(Context ctx) {
        return pref(ctx).getString(K_VAI_TRO, CSDL.VAI_TRO_HOC_VIEN);
    }

    public static boolean daDangNhap(Context ctx) {
        return !ten(ctx).isEmpty();
    }

    /** Nguoi dung co tich "Ghi nho dang nhap" hay khong. */
    public static boolean ghiNho(Context ctx) {
        return pref(ctx).getBoolean(K_GHI_NHO, false);
    }

    public static boolean laAdmin(Context ctx) {
        return CSDL.VAI_TRO_ADMIN.equals(vaiTro(ctx));
    }
}

package com.example.apptracnghiem;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;

/**
 * Adapter tu viet cho ListView chi tiet bai lam:
 * cau tra loi dung to nen xanh, cau sai to nen do.
 */
public class ChiTietAdapter extends ArrayAdapter<String> {

    private final boolean[] dungSai;

    public ChiTietAdapter(Context ctx, ArrayList<String> dong, boolean[] dungSai) {
        super(ctx, android.R.layout.simple_list_item_1, dong);
        this.dungSai = dungSai;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        View v = super.getView(position, convertView, parent);
        TextView tv = v.findViewById(android.R.id.text1);

        boolean dung = position < dungSai.length && dungSai[position];
        int nen = dung ? R.color.nen_dung : R.color.nen_sai;
        int chu = dung ? R.color.xanh_dung : R.color.do_sai;

        v.setBackgroundColor(ContextCompat.getColor(getContext(), nen));
        tv.setTextColor(ContextCompat.getColor(getContext(), chu));
        tv.setTextSize(14);
        return v;
    }
}

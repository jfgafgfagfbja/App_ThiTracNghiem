package com.example.apptracnghiem;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import java.util.ArrayList;

/** Adapter noi TabLayout voi cac Fragment (kien thuc bai 4.5). */
public class VPAdapter extends FragmentPagerAdapter {

    private final ArrayList<Fragment> danhSachFragment = new ArrayList<>();
    private final ArrayList<String> tieuDeTab = new ArrayList<>();

    public VPAdapter(@NonNull FragmentManager fm, int behavior) {
        super(fm, behavior);
    }

    public void addFragment(Fragment fragment, String tieuDe) {
        danhSachFragment.add(fragment);
        tieuDeTab.add(tieuDe);
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        return danhSachFragment.get(position);
    }

    @Override
    public int getCount() {
        return danhSachFragment.size();
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return tieuDeTab.get(position);
    }
}

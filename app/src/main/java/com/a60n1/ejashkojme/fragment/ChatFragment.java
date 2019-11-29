package com.a60n1.ejashkojme.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.a60n1.ejashkojme.R;
import com.google.android.material.tabs.TabLayout;

import java.util.Objects;

public class ChatFragment extends BaseFragment {
    private FragmentStatePagerAdapter mPagerAdapter;
    private ViewPager mViewPager;

    public ChatFragment() {
    }

    public static ChatFragment newInstance() {
        return new ChatFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat, container, false);

        // Create the adapter that will return a fragment for each section
        mPagerAdapter = new FragmentStatePagerAdapter(Objects.requireNonNull(getFragmentManager())) {
            private final Fragment[] mFragments = new Fragment[]{
                    new ConversationFragment(),
                    new FriendsFragment(),
            };
            private final String[] mFragmentNames = new String[]{
                    getString(R.string.heading_conversation),
                    getString(R.string.heading_friends)
            };

            @NonNull
            @Override
            public Fragment getItem(int position) {
                return mFragments[position];
            }

            @Override
            public int getCount() {
                return mFragments.length;
            }

            @Override
            public CharSequence getPageTitle(int position) {
                return mFragmentNames[position];
            }

            @Override
            public int getItemPosition(@NonNull Object object) {
                return POSITION_NONE;
            }
        };
        // Set up the ViewPager with the sections adapter.
        mViewPager = view.findViewById(R.id.chat_container);
        mViewPager.setAdapter(mPagerAdapter);
        TabLayout tabLayout = view.findViewById(R.id.chat_tabs);
        tabLayout.setupWithViewPager(mViewPager);

        return view;
    }

}

package com.a60n1.ejashkojme.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.a60n1.ejashkojme.R
import com.google.android.material.tabs.TabLayout

class ChatFragment : BaseFragment() {
    private var mPagerAdapter: FragmentStatePagerAdapter? = null
    private var mViewPager: ViewPager? = null
    @Suppress("RedundantOverride")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    @Suppress("DEPRECATION")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_chat, container, false)
        // Create the adapter that will return a fragment for each section
        mPagerAdapter = object : FragmentStatePagerAdapter(fragmentManager!!) {
            private val mFragments = arrayOf<Fragment>(
                    ConversationFragment(),
                    FriendsFragment())
            private val mFragmentNames = arrayOf(
                    getString(R.string.heading_conversation),
                    getString(R.string.heading_friends)
            )

            override fun getItem(position: Int): Fragment {
                return mFragments[position]
            }

            override fun getCount(): Int {
                return mFragments.size
            }

            override fun getPageTitle(position: Int): CharSequence? {
                return mFragmentNames[position]
            }

            override fun getItemPosition(`object`: Any): Int {
                return PagerAdapter.POSITION_NONE
            }
        }
        // Set up the ViewPager with the sections adapter.
        mViewPager = view.findViewById(R.id.chat_container)
        mViewPager!!.adapter = mPagerAdapter
        val tabLayout: TabLayout = view.findViewById(R.id.chat_tabs)
        tabLayout.setupWithViewPager(mViewPager)
        return view
    }

    companion object {
        fun newInstance(): ChatFragment {
            return ChatFragment()
        }
    }
}
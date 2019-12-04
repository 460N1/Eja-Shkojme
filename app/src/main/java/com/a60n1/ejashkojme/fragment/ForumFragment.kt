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
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.tabs.TabLayout

/**
 * Fragment to hold forum
 */
class ForumFragment : BaseFragment() {
    private var mPagerAdapter: FragmentStatePagerAdapter? = null
    private var mViewPager: ViewPager? = null
    @Suppress("RedundantOverride")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    @Suppress("DEPRECATION")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_forum, container, false)
        // krijimi i adapterit qe do kthej view per qdo seksion
        mPagerAdapter = object : FragmentStatePagerAdapter(fragmentManager!!) {
            private val mFragments = arrayOf<Fragment>(
                    RecentPostsFragment(),
                    MyPostsFragment(),
                    MyTopPostsFragment())
            private val mFragmentNames = arrayOf(
                    getString(R.string.heading_recent),
                    getString(R.string.heading_my_posts),
                    getString(R.string.heading_my_top_posts)
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
        // viewpager, nderlidhje me adapter
        mViewPager = view.findViewById(R.id.forum_container)
        mViewPager?.adapter = mPagerAdapter
        val tabLayout: TabLayout = view.findViewById(R.id.forum_tabs)
        tabLayout.setupWithViewPager(mViewPager)
        val fab: FloatingActionButton = view.findViewById(R.id.fab_new_post)
        fab.setOnClickListener { mainActivity?.onNewPostBtnClicked() }
        return view
    }

    companion object {
        fun newInstance(): ForumFragment {
            return ForumFragment()
        }
    }
}
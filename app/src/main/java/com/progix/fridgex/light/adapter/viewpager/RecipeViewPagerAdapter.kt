package com.progix.fridgex.light.adapter.viewpager

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.progix.fridgex.light.fragment.recipe.InfoFragment
import com.progix.fridgex.light.fragment.recipe.IngredsFragment
import com.progix.fridgex.light.fragment.recipe.RecipeFragment


class RecipeViewPagerAdapter(fragmentActivity: FragmentActivity) :
    FragmentStateAdapter(fragmentActivity) {
    override fun createFragment(position: Int): Fragment {
        when (position) {
            0 -> return IngredsFragment()
            1 -> return RecipeFragment()
            2 -> return InfoFragment()
        }
        return IngredsFragment()
    }

    override fun getItemCount(): Int {
        return 3
    }
}
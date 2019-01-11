package com.nearhuscarl.smack

import android.support.v4.widget.DrawerLayout
import android.app.Activity
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.widget.Toolbar
import android.view.View


class SmoothActionBarDrawerToggle(
        activity: Activity,
        drawerLayout: DrawerLayout,
        toolbar: Toolbar,
        openDrawerContentDescRes: Int,
        closeDrawerContentDescRes: Int)
    :
        ActionBarDrawerToggle(
                activity,
                drawerLayout,
                toolbar,
                openDrawerContentDescRes,
                closeDrawerContentDescRes)
{

    private var runnable: Runnable? = null

    override fun onDrawerOpened(drawerView: View) {
        super.onDrawerOpened(drawerView)
    }

    override fun onDrawerClosed(view: View) {
        super.onDrawerClosed(view)
    }

    override fun onDrawerStateChanged(newState: Int) {
        super.onDrawerStateChanged(newState)
        if (runnable != null && newState == DrawerLayout.STATE_IDLE) {
            runnable!!.run()
            runnable = null
        }
    }

    fun runWhenIdle(runnable: Runnable) {
        this.runnable = runnable
    }
}
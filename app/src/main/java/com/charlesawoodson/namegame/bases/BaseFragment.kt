package com.charlesawoodson.namegame.bases

import com.airbnb.mvrx.BaseMvRxFragment

open class BaseFragment : BaseMvRxFragment() {

    /**
     * Override this to handle any state changes from MvRxViewModels created through MvRx Fragment delegates.
     */
    override fun invalidate() {}
}
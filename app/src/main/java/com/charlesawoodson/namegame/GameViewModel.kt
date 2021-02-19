package com.charlesawoodson.namegame

import android.util.Log
import com.airbnb.mvrx.BaseMvRxViewModel
import com.airbnb.mvrx.MvRxState
import com.airbnb.mvrx.MvRxViewModelFactory
import com.airbnb.mvrx.ViewModelContext
import com.charlesawoodson.namegame.api.Profile
import com.charlesawoodson.namegame.api.WillowTreeService
import com.charlesawoodson.namegame.api.WillowTreeServiceFactory
import io.reactivex.schedulers.Schedulers

data class GameState(
    val profiles: List<Profile> = emptyList()
) : MvRxState

class GameViewModel(initialState: GameState, willowTreeApi: WillowTreeService) :
    BaseMvRxViewModel<GameState>(initialState, true) {

    init {
        getProfiles(willowTreeApi)
    }

    private fun getProfiles(willowTreeApi: WillowTreeService) {
        willowTreeApi.getProfiles()
            .subscribeOn(Schedulers.io())
            .subscribe(this::handleResponse, this::handleError)
            .disposeOnClear()
    }

    private fun handleResponse(profiles: List<Profile>) {
        setState {
            copy(profiles = profiles)
        }
    }

    private fun handleError(error: Throwable) {
        Log.d("Error", error.toString()) // todo: show error dialog
    }

    companion object : MvRxViewModelFactory<GameViewModel, GameState> {
        @JvmStatic
        override fun create(viewModelContext: ViewModelContext, state: GameState): GameViewModel {
            return GameViewModel(
                state,
                WillowTreeServiceFactory.willowTreeApi // todo: get with daggar
            )
        }
    }

}

package com.charlesawoodson.namegame

import com.airbnb.mvrx.*
import com.charlesawoodson.namegame.api.WillowTreeService
import com.charlesawoodson.namegame.api.WillowTreeServiceFactory
import com.charlesawoodson.namegame.api.model.Profile
import com.charlesawoodson.namegame.extensions.removeItem
import io.reactivex.schedulers.Schedulers

data class GameState(
    val profiles: Async<List<Profile>> = Loading(),
    val profilesPerRound: List<Profile> = emptyList(),
    val displayName: String = "Loading Data...",
    val roundCount: Int = 0,
    val correctCount: Int = 0,
    val incorrectCount: Int = 0,
    val totalTime: Long = 0L,
    val roundStartTime: Long = 0L
) : MvRxState

class GameViewModel(initialState: GameState, willowTreeApi: WillowTreeService) :
    BaseMvRxViewModel<GameState>(initialState, true) {

    private val availableProfiles = mutableSetOf<Profile>()
    var answerId: String = ""

    init {
        getProfiles(willowTreeApi)

        asyncSubscribe(GameState::profiles) { profiles ->
            availableProfiles.addAll(profiles)
        }
    }

    private fun getProfiles(willowTreeApi: WillowTreeService) {
        willowTreeApi.getProfiles()
            .subscribeOn(Schedulers.io())
            .subscribe(this::handleResponse, this::handleError)
            .disposeOnClear()
    }

    private fun handleResponse(profiles: List<Profile>) {
        setState {
            copy(profiles = Success(profiles))
        }
    }

    private fun handleError(error: Throwable) {
        setState {
            copy(profiles = Fail(error))
        }
    }

    fun startRound() {
        val profilesForRound = mutableListOf<Profile>()
        for (i in 0 until 6) {
            profilesForRound.add(availableProfiles.random())
        }

        val correctProfile = profilesForRound.random()
        availableProfiles.remove(correctProfile)

        val displayName = "${correctProfile.firstName} ${correctProfile.lastName}"

        answerId = correctProfile.id

        val roundStartTime = System.nanoTime()

        setState {
            copy(
                profilesPerRound = profilesForRound,
                displayName = displayName,
                roundStartTime = roundStartTime
            )
        }
    }

    fun incrementRound() {
        setState {
            copy(roundCount = roundCount.plus(1))
        }
    }

    fun incrementCorrect() {
        setState {
            copy(correctCount = correctCount.plus(1))
        }
    }

    fun incrementIncorrect() {
        setState {
            copy(incorrectCount = incorrectCount.plus(1))
        }
    }

    fun calculateTotalTime() {
        val elapsed = System.nanoTime()
        setState {
            copy(totalTime = totalTime.plus(elapsed - roundStartTime))
        }
    }

    fun removeProfile(position: Int) {
        setState {
            copy(profilesPerRound = profilesPerRound.removeItem(position))
        }
    }

    companion object : MvRxViewModelFactory<GameViewModel, GameState> {
        @JvmStatic
        override fun create(viewModelContext: ViewModelContext, state: GameState): GameViewModel {
            return GameViewModel(
                state,
                WillowTreeServiceFactory.willowTreeApi // todo: get with dagger
            )
        }
    }

}

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
    private var answerId: String = ""

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
        val picks = mutableListOf<Profile>()

        val pickFrom = mutableSetOf<Profile>().apply {
            addAll(availableProfiles)
        }

        for (i in 0 until pickFrom.size.coerceAtMost(6)) {
            pickFrom.random().also {
                pickFrom.remove(it)
                picks.add(it)
            }
        }

        val correctProfile = picks.random().also {
            availableProfiles.remove(it)
        }

        val displayName = "${correctProfile.firstName} ${correctProfile.lastName}"

        answerId = correctProfile.id

        val roundStartTime = System.nanoTime()

        setState {
            copy(
                profilesPerRound = picks,
                displayName = displayName,
                roundStartTime = roundStartTime
            )
        }
    }

    fun correctAnswer() {
        val elapsedTime = System.nanoTime()
        setState {
            copy(
                roundCount = roundCount.plus(1),
                correctCount = correctCount.plus(1),
                totalTime = totalTime.plus(elapsedTime - roundStartTime)
            )
        }
    }

    fun wrongAnswer(position: Int) {
        setState {
            copy(
                profilesPerRound = profilesPerRound.removeItem(position),
                incorrectCount = incorrectCount.plus(1)
            )
        }
    }

    fun getAnswerId() = answerId

    companion object : MvRxViewModelFactory<GameViewModel, GameState> {
        @JvmStatic
        override fun create(viewModelContext: ViewModelContext, state: GameState): GameViewModel {
            return GameViewModel(
                state,
                WillowTreeServiceFactory.willowTreeApi // todo: get with dagger repo pattern
            )
        }
    }

}

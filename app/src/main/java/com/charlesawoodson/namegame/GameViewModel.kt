package com.charlesawoodson.namegame

import androidx.preference.PreferenceManager
import com.airbnb.mvrx.*
import com.charlesawoodson.namegame.api.WillowTreeApiFactory
import com.charlesawoodson.namegame.api.model.Profile
import com.charlesawoodson.namegame.extensions.removeItem
import com.charlesawoodson.namegame.repositories.WillowTreeRepository
import io.reactivex.schedulers.Schedulers

data class GameState(
    val profiles: Async<List<Profile>> = Loading(),
    val profilesPerRound: List<Profile> = emptyList(),
    val displayName: String = "Loading Data...",
    val displayImageUrl: String = "",
    val roundCount: Int = 0,
    val correctCount: Int = 0,
    val incorrectCount: Int = 0,
    val totalTime: Long = 0L,
    val roundStartTime: Long = 0L,
    val hasAvailableProfiles: Boolean = true
) : MvRxState

class GameViewModel(
    initialState: GameState,
    willowTreeRepository: WillowTreeRepository,
    private val mattMode: Boolean,
    private val challengeMode: Boolean
) :
    BaseMvRxViewModel<GameState>(initialState, true) {

    private val availableProfiles = mutableSetOf<Profile>()
    private var answerId: String = ""

    init {
        getProfiles(willowTreeRepository)

        asyncSubscribe(GameState::profiles) { profiles ->
            if (mattMode)
                availableProfiles.addAll(profiles.filter { it.firstName == "Matt" || it.firstName == "Matthew" })
            else {
                availableProfiles.addAll(profiles)
            }
        }
    }

    private fun getProfiles(willowTreeRepository: WillowTreeRepository) {
        willowTreeRepository.getProfiles()
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
        val maxItems = if (challengeMode) PICK_SIZE * 2 else PICK_SIZE

        val pickFrom = mutableSetOf<Profile>().apply {
            addAll(availableProfiles)
        }

        val picks = mutableListOf<Profile>()

        for (i in 0 until pickFrom.size.coerceAtMost(maxItems)) {
            pickFrom.random().also {
                pickFrom.remove(it)
                picks.add(it)
            }
        }

        val correctProfile = picks.random().also {
            availableProfiles.remove(it)
        }

        val displayName = "${correctProfile.firstName} ${correctProfile.lastName}"
        val displayUrl = correctProfile.headshot.url

        answerId = correctProfile.id

        val roundStartTime = System.nanoTime()

        setState {
            copy(
                profilesPerRound = picks,
                displayName = displayName,
                roundStartTime = roundStartTime,
                displayImageUrl = displayUrl
            )
        }
    }

    fun correctAnswer() {
        val elapsedTime = System.nanoTime()
        setState {
            copy(
                roundCount = roundCount.plus(1),
                correctCount = correctCount.plus(1),
                totalTime = totalTime.plus(elapsedTime - roundStartTime),
                hasAvailableProfiles = availableProfiles.size > 0
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

    fun getAvailableSize() = availableProfiles.size

    companion object : MvRxViewModelFactory<GameViewModel, GameState> {

        private const val PICK_SIZE = 6

        @JvmStatic
        override fun create(viewModelContext: ViewModelContext, state: GameState): GameViewModel {

            val context = viewModelContext.activity

            val mattMode = PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(context.getString(R.string.matt_mode_pref), false)

            val challengeMode = PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(context.getString(R.string.challenge_mode_pref), false)

            return GameViewModel(
                state,
                WillowTreeRepository(WillowTreeApiFactory.willowTreeApi), // todo dagger
                mattMode,
                challengeMode
            )
        }
    }
}

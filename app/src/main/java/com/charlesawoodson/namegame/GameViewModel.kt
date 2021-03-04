package com.charlesawoodson.namegame

import androidx.preference.PreferenceManager
import com.airbnb.mvrx.*
import com.charlesawoodson.namegame.api.WillowTreeApiFactory
import com.charlesawoodson.namegame.api.model.Profile
import com.charlesawoodson.namegame.extensions.removeItem
import com.charlesawoodson.namegame.repositories.WillowTreeRepository
import io.reactivex.schedulers.Schedulers
import java.util.*

data class GameState(
    val profiles: Async<List<Profile>> = Uninitialized,
    val profilePicks: List<Profile> = emptyList(),
    val profileAnswer: Async<Profile> = Uninitialized,
    val roundCount: Int = 0,
    val correctCount: Int = 0,
    val incorrectCount: Int = 0,
    val totalTime: Long = 0L,
    val roundStartTime: Long = 0L,
    val hasAvailableProfiles: Async<Boolean> = Uninitialized,
    val roundStarted: Boolean = false
) : MvRxState

class GameViewModel(
    initialState: GameState,
    private val willowTreeRepository: WillowTreeRepository,
    private val mattMode: Boolean,
    private val challengeMode: Boolean,
    private val reverseMode: Boolean,
    private val hintMode: Boolean
) :
    BaseMvRxViewModel<GameState>(initialState, true) {

    private val availableProfiles = mutableSetOf<Profile>()
    private var pickedProfiles = mutableListOf<Profile>()
    private var answerId = ""
    private var roundStarted = false

    init {
        fetchData()

        asyncSubscribe(GameState::profiles) { profiles ->
            if (mattMode)
                availableProfiles.addAll(profiles.filter { it.firstName == MATT_NAME || it.firstName == MATTHEW_NAME })
            else {
                availableProfiles.addAll(profiles)
            }
        }

        selectSubscribe(GameState::profilePicks) { picks ->
            pickedProfiles = picks.toMutableList()
        }

        asyncSubscribe(GameState::profileAnswer) { answer ->
            answerId = answer.id
        }

        selectSubscribe(GameState::roundStarted) { started ->
            roundStarted = started
        }
    }

    fun fetchData() {
        willowTreeRepository.getProfiles()
            .subscribeOn(Schedulers.io())
            .subscribe(this::handleResponse, this::handleError)
            .disposeOnClear()

        setState {
            copy(profiles = Loading())
        }
    }

    private fun handleResponse(profiles: List<Profile>) {
        setState {
            copy(
                profiles = Success(profiles.filter {
                    it.headshot.url != "" && it.headshot.height != 0 && it.headshot.width != 0
                            && (it.firstName.isNotBlank() || it.lastName.isNotBlank())
                }),
                hasAvailableProfiles = Success(profiles.isNotEmpty())
            )
        }
    }

    private fun handleError(error: Throwable) {
        setState {
            copy(
                profiles = Fail(error),
                hasAvailableProfiles = Success(false)
            )
        }
    }

    fun startRound() {
        val maxItems = if (challengeMode) PICK_SIZE * CHALLENGE_MODE_MULTIPLE else PICK_SIZE

        val pickFrom = mutableSetOf<Profile>().apply {
            addAll(availableProfiles)
        }

        pickedProfiles.clear()

        for (i in 0 until pickFrom.size.coerceAtMost(maxItems)) {
            pickFrom.random().also {
                pickFrom.remove(it)
                pickedProfiles.add(it)
            }
        }

        val correctProfile = pickedProfiles.random().also {
            availableProfiles.remove(it)
        }

        val roundStartTime = System.nanoTime()

        if (hintMode) {
            task.cancel()
            task = Timer()
            removeRandomIncorrectItem(correctProfile)
        }

        setState {
            copy(
                profilePicks = pickedProfiles.toList(),
                profileAnswer = Success(correctProfile),
                roundStartTime = roundStartTime,
                roundStarted = true
            )
        }
    }

    private var task = Timer()

    private fun removeRandomIncorrectItem(answerProfile: Profile) {
        task.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                if (pickedProfiles.size > 1) {
                    pickedProfiles.remove(answerProfile)
                    val randomItem = pickedProfiles.random()
                    pickedProfiles.remove(randomItem)
                    pickedProfiles.add(answerProfile)

                    setState {
                        copy(profilePicks = pickedProfiles.toList())
                    }
                } else {
                    task.cancel()
                }
            }
        }, HINT_MODE_DELAY, HINT_MODE_DELAY)
    }

    fun correctAnswer() {
        val elapsedTime = System.nanoTime()
        setState {
            copy(
                roundCount = roundCount.plus(1),
                correctCount = correctCount.plus(1),
                totalTime = totalTime.plus(elapsedTime - roundStartTime),
                hasAvailableProfiles = Success(availableProfiles.size > 0),
                roundStarted = false,
                profilePicks = listOf(profileAnswer()!!) // todo: remove this
            )
        }
    }

    fun wrongAnswer(position: Int) {
        setState {
            copy(
                profilePicks = profilePicks.removeItem(position),
                incorrectCount = incorrectCount.plus(1)
            )
        }
    }

    fun getAvailableSize() = availableProfiles.size

    fun isReverseMode() = reverseMode

    fun getAnswerId() = answerId

    fun getRoundStarted() = roundStarted

    companion object : MvRxViewModelFactory<GameViewModel, GameState> {

        private const val PICK_SIZE = 6
        private const val CHALLENGE_MODE_MULTIPLE = 2
        private const val HINT_MODE_DELAY = 2000L
        private const val MATT_NAME = "Matt"
        private const val MATTHEW_NAME = "Matthew"

        @JvmStatic
        override fun create(viewModelContext: ViewModelContext, state: GameState): GameViewModel {

            val context = viewModelContext.activity

            val mattMode = PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(context.getString(R.string.matt_mode_pref), false)

            val challengeMode = PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(context.getString(R.string.challenge_mode_pref), false)

            val reverseMode = PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(context.getString(R.string.reverse_mode_pref), false)

            val hintMode = PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(context.getString(R.string.hint_mode_pref), false)

            return GameViewModel(
                state,
                WillowTreeRepository(WillowTreeApiFactory.willowTreeApi),
                mattMode,
                challengeMode,
                reverseMode,
                hintMode
            )
        }
    }
}

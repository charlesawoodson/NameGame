package com.charlesawoodson.namegame.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.airbnb.mvrx.Success
import com.airbnb.mvrx.activityViewModel
import com.airbnb.mvrx.fragmentViewModel
import com.bumptech.glide.Glide
import com.charlesawoodson.namegame.GameState
import com.charlesawoodson.namegame.GameViewModel
import com.charlesawoodson.namegame.R
import com.charlesawoodson.namegame.bases.BaseDialogFragment
import kotlinx.android.synthetic.main.fragment_game.*
import kotlinx.android.synthetic.main.fragment_statistics_dialog.*
import kotlinx.android.synthetic.main.fragment_statistics_dialog.answerNameTextView
import kotlinx.android.synthetic.main.fragment_statistics_dialog.answerProfileImageView

class StatisticsDialogFragment : BaseDialogFragment() {

    private val viewModel: GameViewModel by fragmentViewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel.selectSubscribe(GameState::correctCount) {
            correctCount.text = getString(R.string.correct_count, it)
        }

        viewModel.selectSubscribe(GameState::incorrectCount) {
            incorrectCount.text = getString(R.string.incorrect_count, it)
        }

        viewModel.selectSubscribe(GameState::totalTime, GameState::roundCount) { total, count ->
            val averageSecondsPerRound = ((total.toDouble() / 1_000_000_000) / count.toDouble())
            averageTime.text = getString(R.string.average_time, averageSecondsPerRound)

            roundCount.text = getString(R.string.rounds_played, count)
        }

        viewModel.selectSubscribe(GameState::gameOver) { gameOver ->
            lastCorrectAnswerContainer.isVisible = gameOver
            startButton.isGone = gameOver
            youWonTextView.isVisible = gameOver
        }

        viewModel.selectSubscribe(GameState::profiles) { profiles ->
            startButton.isClickable = profiles is Success
        }

        viewModel.selectSubscribe(GameState::lastCorrect) { profile ->
            lastCorrectAnswerContainer.isVisible = profile != null
            profile?.also {
                val circularProgressDrawable = CircularProgressDrawable(requireContext())
                circularProgressDrawable.start()

                Glide.with(requireContext())
                    .load("http:${profile.headshot.url}")
                    .placeholder(circularProgressDrawable)
                    .circleCrop()
                    .into(answerProfileImageView)

                answerNameTextView.text =
                    getString(R.string.answer_name, profile.firstName, profile.lastName)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_statistics_dialog, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        startButton.setOnClickListener {
            if (viewModel.getAvailableSize() > 0) {
                viewModel.startRound()
            }
            dismiss()
        }
    }
}
package com.charlesawoodson.namegame.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isGone
import androidx.preference.PreferenceManager
import com.airbnb.mvrx.parentFragmentViewModel
import com.charlesawoodson.namegame.GameState
import com.charlesawoodson.namegame.GameViewModel
import com.charlesawoodson.namegame.R
import com.charlesawoodson.namegame.bases.BaseDialogFragment
import kotlinx.android.synthetic.main.fragment_statistics_dialog.*

class StatisticsDialogFragment : BaseDialogFragment() {

    private val viewModel: GameViewModel by parentFragmentViewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isCancelable = false

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

        viewModel.selectSubscribe(GameState::hasAvailableProfiles) { hasProfiles ->
            if (hasProfiles) {
                startRoundButton.text = getString(R.string.start_round)
            } else {
                startRoundButton.text = getString(R.string.end_game)
            }
            youWonTextView.isGone = hasProfiles
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

        startRoundButton.setOnClickListener {
            if (viewModel.getAvailableSize() > 0) {
                viewModel.startRound()
            } else {
                requireActivity().onBackPressed()
            }
            dismiss()
        }
    }
}
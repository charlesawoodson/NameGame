package com.charlesawoodson.namegame.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
            correctCount.text = "Correct Count: $it"
        }

        viewModel.selectSubscribe(GameState::incorrectCount) {
            incorrectCount.text = "Incorrect Count: $it"
        }

        viewModel.selectSubscribe(GameState::totalTime, GameState::roundCount) { total, count ->
            val nanosToSeconds = (total.toDouble() / 1_000_000_000)
            averageTime.text =
                String.format("Average Time: %.2f", nanosToSeconds / count.toDouble())

            roundCount.text = "Rounds Played: $count"
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
        // requireDialog().window?.setWindowAnimations(R.style.DialogAnimation)

        startRoundButton.setOnClickListener {
            viewModel.startRound()
            dismiss()
        }
    }
}
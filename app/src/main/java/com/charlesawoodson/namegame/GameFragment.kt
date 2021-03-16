package com.charlesawoodson.namegame

import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.airbnb.mvrx.*
import com.bumptech.glide.Glide
import com.charlesawoodson.namegame.adapters.OnProfileItemClickListener
import com.charlesawoodson.namegame.adapters.ProfileAdapter
import com.charlesawoodson.namegame.adapters.ReverseModeAdapter
import com.charlesawoodson.namegame.bases.BaseFragment
import com.charlesawoodson.namegame.dialogs.StatisticsDialogFragment
import kotlinx.android.synthetic.main.fragment_game.*

class GameFragment : BaseFragment(), OnProfileItemClickListener {

    private val viewModel: GameViewModel by activityViewModel()

    private val adapter by lazy(mode = LazyThreadSafetyMode.NONE) {
        ProfileAdapter(this)
    }

    private val reverseModeAdapter by lazy(mode = LazyThreadSafetyMode.NONE) {
        ReverseModeAdapter(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel.selectSubscribe(
            GameState::profiles,
            GameState::roundStarted,
            GameState::gameOver
        ) { profiles, roundStarted, gameOver ->

            progressBar.isVisible = profiles is Loading
            messageTextView.isVisible = !roundStarted || gameOver
            retryButton.isVisible = profiles is Fail
            startRoundButton.isVisible = profiles is Success && !roundStarted && !gameOver
            playAgainButton.isVisible = gameOver

            messageContainer.isGone = roundStarted

            if (profiles is Fail) {
                messageTextView.text = getString(R.string.error_loading_data)
            }

            if (profiles is Loading) {
                messageTextView.text = getString(R.string.loading_data)
            }

            if (profiles is Success || gameOver) {
                messageTextView.text = getString(R.string.tap_when_ready)
            }
        }

        viewModel.selectSubscribe(GameState::profilePicks) { profiles ->
            if (viewModel.isReverseMode()) {
                reverseModeAdapter.updateData(profiles)
            } else {
                adapter.updateData(profiles)
            }
        }

        viewModel.selectSubscribe(GameState::profileAnswer) { profile ->
            answerViewContainer.isGone = profile is Uninitialized

            if (profile is Success) {
                if (viewModel.isReverseMode()) {
                    val circularProgressDrawable = CircularProgressDrawable(requireContext())
                    circularProgressDrawable.start()
                    Glide.with(requireContext())
                        .load(getString(R.string.http_url, profile().headshot.url))
                        .placeholder(circularProgressDrawable)
                        .circleCrop()
                        .into(answerProfileImageView)
                } else {
                    answerNameTextView.text =
                        getString(R.string.answer_name, profile().firstName, profile().lastName)
                }

                answerProfileImageView.isVisible = viewModel.isReverseMode()
                answerNameTextView.isGone = viewModel.isReverseMode()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        return inflater.inflate(R.layout.fragment_game, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        retryButton.setOnClickListener {
            viewModel.fetchData()
        }

        startRoundButton.setOnClickListener {
            viewModel.startRound()
        }

        playAgainButton.setOnClickListener {
            viewModel.fetchData()
        }

        val orientation =
            if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
                RecyclerView.VERTICAL
            } else {
                RecyclerView.HORIZONTAL
            }

        val spanCount =
            when {
                viewModel.isReverseMode() && orientation == RecyclerView.HORIZONTAL -> {
                    SPAN_COUNT_THREE
                }
                !viewModel.isReverseMode() && orientation == RecyclerView.VERTICAL -> {
                    SPAN_COUNT_THREE
                }
                else -> {
                    SPAN_COUNT_TWO
                }
            }

        profilesRecyclerView.layoutManager =
            GridLayoutManager(context, spanCount, orientation, false)

        profilesRecyclerView.adapter =
            if (viewModel.isReverseMode()) reverseModeAdapter else adapter
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        (activity as MainActivity).supportActionBar?.title = getString(R.string.app_name)
        (activity as MainActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)
        menu.findItem(R.id.action_begin_game).isVisible = false
        super.onPrepareOptionsMenu(menu)
    }

    override fun onProfileItemClicked(profileId: String, position: Int) {
        if (profileId == viewModel.getAnswerId()) {
            viewModel.correctAnswer()
            StatisticsDialogFragment().show(childFragmentManager, null)
        } else {
            viewModel.wrongAnswer(position)
        }
    }

    companion object {
        private const val SPAN_COUNT_THREE = 3
        private const val SPAN_COUNT_TWO = 2
    }
}

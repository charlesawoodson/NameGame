package com.charlesawoodson.namegame

import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.airbnb.mvrx.Loading
import com.airbnb.mvrx.fragmentViewModel
import com.airbnb.mvrx.withState
import com.bumptech.glide.Glide
import com.charlesawoodson.namegame.adapters.OnProfileItemClickListener
import com.charlesawoodson.namegame.adapters.ProfileAdapter
import com.charlesawoodson.namegame.adapters.ReverseModeAdapter
import com.charlesawoodson.namegame.bases.BaseFragment
import com.charlesawoodson.namegame.dialogs.StatisticsDialogFragment
import kotlinx.android.synthetic.main.fragment_game.*

class GameFragment : BaseFragment(), OnProfileItemClickListener {

    private val viewModel: GameViewModel by fragmentViewModel()

    private val adapter by lazy(mode = LazyThreadSafetyMode.NONE) {
        ProfileAdapter(this)
    }

    private val reverseModeAdapter by lazy(mode = LazyThreadSafetyMode.NONE) {
        ReverseModeAdapter(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel.selectSubscribe(GameState::profiles) { profiles ->
            progressBar.isVisible = (profiles is Loading)
            if (profiles is Loading) {
                StatisticsDialogFragment().show(childFragmentManager, null)
            }
        }

        viewModel.selectSubscribe(GameState::profilePicks) { profiles ->
            if (viewModel.isReverseMode()) {
                reverseModeAdapter.updateData(profiles)
            } else {
                adapter.updateData(profiles)
            }
        }

        viewModel.asyncSubscribe(GameState::profileAnswer) { profile ->

            if (viewModel.isReverseMode()) {
                val circularProgressDrawable = CircularProgressDrawable(requireContext())
                circularProgressDrawable.start()

                Glide.with(requireContext())
                    .load("http:${profile.headshot.url}")
                    .placeholder(circularProgressDrawable)
                    .circleCrop()
                    .into(answerProfileImageView)

            } else {
                answerNameTextView.text =
                    getString(R.string.answer_name, profile.firstName, profile.lastName)
            }

            answerProfileImageView.isVisible = viewModel.isReverseMode()
            answerNameTextView.isGone = viewModel.isReverseMode()
        }

        viewModel.selectSubscribe(GameState::errorLoading) { errorLoading ->
            if (errorLoading) errorTextView.setText(R.string.error_loading_data)
            errorTextView.isVisible = errorLoading
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_game, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val orientation =
            if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
                RecyclerView.VERTICAL
            } else {
                RecyclerView.HORIZONTAL
            }

        profilesRecyclerView.layoutManager =
            GridLayoutManager(context, 2, orientation, false)

        profilesRecyclerView.adapter =
            if (viewModel.isReverseMode()) reverseModeAdapter else adapter
    }

    override fun onProfileItemClicked(profileId: String, position: Int) {
        if (profileId == viewModel.getAnswerId()) {
            viewModel.correctAnswer()
            StatisticsDialogFragment().show(childFragmentManager, null)
        } else {
            viewModel.wrongAnswer(position)
        }
    }
}

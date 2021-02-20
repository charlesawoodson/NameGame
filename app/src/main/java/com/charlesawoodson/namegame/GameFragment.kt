package com.charlesawoodson.namegame

import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.mvrx.*
import com.charlesawoodson.namegame.adapters.ProfileAdapter
import com.charlesawoodson.namegame.bases.BaseFragment
import com.charlesawoodson.namegame.dialogs.StatisticsDialogFragment
import kotlinx.android.synthetic.main.fragment_game.*

class GameFragment : BaseFragment(), ProfileAdapter.OnProfileItemClickListener {

    private val viewModel: GameViewModel by fragmentViewModel()

    private val adapter by lazy(mode = LazyThreadSafetyMode.NONE) {
        ProfileAdapter(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel.selectSubscribe(GameState::profiles) { profiles ->
            progressBar.isVisible = (profiles is Loading)
            if (profiles is Loading) {
                StatisticsDialogFragment().show(childFragmentManager, null)
            }
        }

        viewModel.selectSubscribe(GameState::displayName) {
            nameTextView.text = it
        }

        viewModel.selectSubscribe(GameState::profilesPerRound) { profiles ->
            adapter.updateData(profiles)
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
        profilesRecyclerView.adapter = adapter
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

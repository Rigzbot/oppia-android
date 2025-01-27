package org.oppia.android.app.player.exploration

import android.content.Context
import android.os.Bundle
import org.oppia.android.app.fragment.FragmentComponentImpl
import org.oppia.android.app.fragment.InjectableFragment
import javax.inject.Inject

/**
 * ManagerFragment of [ExplorationFragment] that observes data provider that retrieve default story
 * text size.
 */
class ExplorationManagerFragment : InjectableFragment() {
  @Inject
  lateinit var explorationManagerFragmentPresenter: ExplorationManagerFragmentPresenter

  override fun onAttach(context: Context) {
    super.onAttach(context)
    (fragmentComponent as FragmentComponentImpl).inject(this)
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    val internalProfileId =
      arguments!!.getInt(
        ExplorationActivity.EXPLORATION_ACTIVITY_PROFILE_ID_ARGUMENT_KEY,
        /* defaultValue= */ -1
      )
    explorationManagerFragmentPresenter.handleCreate(internalProfileId)
  }
}

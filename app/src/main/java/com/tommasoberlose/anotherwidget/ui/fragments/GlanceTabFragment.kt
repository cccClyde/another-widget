package com.tommasoberlose.anotherwidget.ui.fragments

import android.Manifest
import android.app.Activity
import android.app.AlarmManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Canvas
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.common.api.ApiException
import com.google.android.material.card.MaterialCardView
import com.tommasoberlose.anotherwidget.R
import com.tommasoberlose.anotherwidget.components.CustomNotesDialog
import com.tommasoberlose.anotherwidget.components.GlanceSettingsDialog
import com.tommasoberlose.anotherwidget.databinding.FragmentGlanceSettingsBinding
import com.tommasoberlose.anotherwidget.global.Constants
import com.tommasoberlose.anotherwidget.global.Preferences
import com.tommasoberlose.anotherwidget.helpers.ActiveNotificationsHelper
import com.tommasoberlose.anotherwidget.helpers.AlarmHelper
import com.tommasoberlose.anotherwidget.helpers.GlanceProviderHelper
import com.tommasoberlose.anotherwidget.helpers.MediaPlayerHelper
import com.tommasoberlose.anotherwidget.models.GlanceProvider
import com.tommasoberlose.anotherwidget.receivers.ActivityDetectionReceiver
import com.tommasoberlose.anotherwidget.receivers.ActivityDetectionReceiver.Companion.FITNESS_OPTIONS
import com.tommasoberlose.anotherwidget.ui.activities.MainActivity
import com.tommasoberlose.anotherwidget.ui.viewmodels.MainViewModel
import com.tommasoberlose.anotherwidget.utils.checkGrantedPermission
import com.tommasoberlose.anotherwidget.utils.convertDpToPixel
import kotlinx.android.synthetic.main.fragment_glance_settings.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.idik.lib.slimadapter.SlimAdapter
import java.util.*


class GlanceTabFragment : Fragment() {

    companion object {
        fun newInstance() = GlanceTabFragment()
    }

    private var dialog: GlanceSettingsDialog? = null
    private lateinit var adapter: SlimAdapter
    private lateinit var viewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {

        viewModel = ViewModelProvider(activity as MainActivity).get(MainViewModel::class.java)
        val binding = DataBindingUtil.inflate<FragmentGlanceSettingsBinding>(inflater,
            R.layout.fragment_glance_settings,
            container,
            false)

        subscribeUi(binding, viewModel)

        binding.lifecycleOwner = this
        binding.viewModel = viewModel

        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        // List
        providers_list.setHasFixedSize(true)
        val mLayoutManager = LinearLayoutManager(context)
        providers_list.layoutManager = mLayoutManager

        adapter = SlimAdapter.create()
        adapter
            .register<GlanceProvider>(R.layout.glance_provider_item) { item, injector ->
                val provider = Constants.GlanceProviderId.from(item.id)!!
                injector
                    .text(R.id.title, item.title)
                    .with<ImageView>(R.id.icon) {
                        it.setImageDrawable(ContextCompat.getDrawable(requireContext(), item.icon))
                    }
                    .clicked(R.id.item) {
                        if (Preferences.showGlance) {
                            if (provider == Constants.GlanceProviderId.CUSTOM_INFO) {
                                CustomNotesDialog(requireContext()).show()
                            } else {
                                dialog = GlanceSettingsDialog(requireActivity(), provider) {
                                    adapter.notifyItemRangeChanged(0, adapter.data.size)
                                }
                                dialog?.setOnDismissListener {
                                    dialog = null
                                }
                                dialog?.show()
                            }
                        }
                    }
                when (provider) {
                    Constants.GlanceProviderId.PLAYING_SONG -> {
                        when {
                            ActiveNotificationsHelper.checkNotificationAccess(requireContext()) -> {
                                MediaPlayerHelper.updatePlayingMediaInfo(requireContext())
                                injector.visibility(R.id.error_icon, View.GONE)
                                injector.visibility(R.id.info_icon, View.VISIBLE)
                                injector.text(R.id.label,
                                    if (Preferences.showMusic) getString(R.string.settings_visible) else getString(
                                        R.string.settings_not_visible))
                            }
                            Preferences.showMusic -> {
                                injector.visibility(R.id.error_icon, View.VISIBLE)
                                injector.visibility(R.id.info_icon, View.GONE)
                                injector.text(R.id.label, getString(R.string.settings_not_visible))
                            }
                            else -> {
                                injector.visibility(R.id.error_icon, View.GONE)
                                injector.visibility(R.id.info_icon, View.VISIBLE)
                                injector.text(R.id.label, getString(R.string.settings_not_visible))
                            }
                        }
                    }
                    Constants.GlanceProviderId.NEXT_CLOCK_ALARM -> {
                        injector.text(R.id.label,
                            if (Preferences.showNextAlarm && !AlarmHelper.isAlarmProbablyWrong(
                                    requireContext())
                            ) getString(R.string.settings_visible) else getString(
                                R.string.settings_not_visible))
                        injector.visibility(R.id.error_icon,
                            if (Preferences.showNextAlarm && AlarmHelper.isAlarmProbablyWrong(
                                    requireContext())
                            ) View.VISIBLE else View.GONE)
                        injector.visibility(R.id.info_icon,
                            if (!(Preferences.showNextAlarm && AlarmHelper.isAlarmProbablyWrong(
                                    requireContext()))
                            ) View.VISIBLE else View.GONE)
                    }
                    Constants.GlanceProviderId.BATTERY_LEVEL_LOW -> {
                        injector.text(R.id.label,
                            if (Preferences.showBatteryCharging) getString(R.string.settings_visible) else getString(
                                R.string.settings_not_visible))
                        injector.visibility(R.id.error_icon, View.GONE)
                        injector.visibility(R.id.info_icon, View.VISIBLE)
                    }
                    Constants.GlanceProviderId.NOTIFICATIONS -> {
                        when {
                            ActiveNotificationsHelper.checkNotificationAccess(requireContext()) -> {
                                injector.visibility(R.id.error_icon, View.GONE)
                                injector.visibility(R.id.info_icon, View.VISIBLE)
                                injector.text(R.id.label,
                                    if (Preferences.showNotifications) getString(
                                        R.string.settings_visible) else getString(R.string.settings_not_visible))
                            }
                            Preferences.showNotifications -> {
                                injector.visibility(R.id.error_icon, View.VISIBLE)
                                injector.visibility(R.id.info_icon, View.GONE)
                                injector.text(R.id.label, getString(R.string.settings_not_visible))
                            }
                            else -> {
                                injector.visibility(R.id.error_icon, View.GONE)
                                injector.visibility(R.id.info_icon, View.VISIBLE)
                                injector.text(R.id.label, getString(R.string.settings_not_visible))
                            }
                        }
                    }
                    Constants.GlanceProviderId.GREETINGS -> {
                        injector.text(R.id.label,
                            if (Preferences.showGreetings) getString(R.string.settings_visible) else getString(
                                R.string.settings_not_visible))
                        injector.visibility(R.id.error_icon, View.GONE)
                        injector.visibility(R.id.info_icon, View.VISIBLE)
                    }
                    Constants.GlanceProviderId.CUSTOM_INFO -> {
                        injector.text(R.id.label,
                            if (Preferences.customNotes != "") getString(R.string.settings_visible) else getString(
                                R.string.settings_not_visible))
                        injector.visibility(R.id.error_icon, View.GONE)
                        injector.visibility(R.id.info_icon, View.VISIBLE)
                    }
                    Constants.GlanceProviderId.GOOGLE_FIT_STEPS -> {
                        val account: GoogleSignInAccount? = GoogleSignIn.getLastSignedInAccount(context)
                        if (GoogleSignIn.hasPermissions(account, FITNESS_OPTIONS) && (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q || activity?.checkGrantedPermission(
                                Manifest.permission.ACTIVITY_RECOGNITION) == true)
                        ) {
                            injector.text(R.id.label,
                                if (Preferences.showDailySteps) getString(R.string.settings_visible) else getString(
                                    R.string.settings_not_visible))
                            injector.visibility(R.id.error_icon, View.GONE)
                            injector.visibility(R.id.info_icon, View.VISIBLE)
                        } else if (Preferences.showDailySteps) {
                            ActivityDetectionReceiver.unregisterFence(requireContext())
                            injector.visibility(R.id.error_icon, View.VISIBLE)
                            injector.visibility(R.id.info_icon, View.GONE)
                            injector.text(R.id.label, getString(R.string.settings_not_visible))
                        } else {
                            ActivityDetectionReceiver.unregisterFence(requireContext())
                            injector.text(R.id.label, getString(R.string.settings_not_visible))
                            injector.visibility(R.id.error_icon, View.GONE)
                            injector.visibility(R.id.info_icon, View.VISIBLE)
                        }
                    }
                }
            }
            .attachTo(providers_list)

        val mIth = ItemTouchHelper(
            object : ItemTouchHelper.SimpleCallback(
                ItemTouchHelper.UP or ItemTouchHelper.DOWN,
                0
            ) {

                val list = GlanceProviderHelper.getGlanceProviders(requireContext())

                override fun onMove(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder,
                ): Boolean {
                    val fromPos = viewHolder.adapterPosition
                    val toPos = target.adapterPosition
                    // move item in `fromPos` to `toPos` in adapter.
                    adapter.notifyItemMoved(fromPos, toPos)
                    return true
                }

                override fun isItemViewSwipeEnabled(): Boolean {
                    return false
                }

                override fun onMoved(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder,
                    fromPos: Int,
                    target: RecyclerView.ViewHolder,
                    toPos: Int,
                    x: Int,
                    y: Int
                ) {
                    with(list[fromPos]) {
                        list[fromPos] = list[toPos]
                        list[toPos] = this
                    }
                    GlanceProviderHelper.saveGlanceProviderOrder(list)
                    Log.d("ciao", list.toString())
                    super.onMoved(recyclerView, viewHolder, fromPos, target, toPos, x, y)
                }

                override fun onChildDraw(
                    c: Canvas,
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder,
                    dX: Float,
                    dY: Float,
                    actionState: Int,
                    isCurrentlyActive: Boolean,
                ) {
                    val view = viewHolder.itemView as MaterialCardView
                    if (isCurrentlyActive) {
                        ViewCompat.setElevation(view, 2f.convertDpToPixel(requireContext()))
                        view.setCardBackgroundColor(ContextCompat.getColor(requireContext(),
                            R.color.colorPrimary))
                    } else {
                        ViewCompat.setElevation(view, 0f)
                        view.setCardBackgroundColor(ContextCompat.getColor(requireContext(),
                            R.color.colorPrimaryDark))
                    }

                    val topEdge = if ((view.top == 0 && dY < 0) || ((view.top + view.height >= recyclerView.height - 32f.convertDpToPixel(requireContext())) && dY > 0)) 0f else dY

                    super.onChildDraw(c,
                        recyclerView,
                        viewHolder,
                        dX,
                        topEdge,
                        actionState,
                        isCurrentlyActive)
                }

                override fun onSwiped(
                    viewHolder: RecyclerView.ViewHolder,
                    direction: Int,
                ) {
                    // remove from adapter
                }
            })

        mIth.attachToRecyclerView(providers_list)
        adapter.updateData(
            GlanceProviderHelper.getGlanceProviders(requireContext())
                .mapNotNull { GlanceProviderHelper.getGlanceProviderById(requireContext(), it) }
        )
        providers_list.isNestedScrollingEnabled = false

        setupListener()
    }

    private fun subscribeUi(
        binding: FragmentGlanceSettingsBinding,
        viewModel: MainViewModel,
    ) {
        binding.isGlanceVisible = Preferences.showGlance

        viewModel.showGlance.observe(viewLifecycleOwner, Observer {
            maintainScrollPosition {
                binding.isGlanceVisible = it
                show_glance_label.text =
                    if (it) getString(R.string.description_show_glance_visible) else getString(
                        R.string.description_show_glance_not_visible)
            }
        })
    }

    private fun setupListener() {
        action_show_glance.setOnClickListener {
            Preferences.showGlance = !Preferences.showGlance
        }

        show_glance_switch.setOnCheckedChangeListener { _, enabled: Boolean ->
            Preferences.showGlance = enabled
        }
    }

    private val nextAlarmChangeBroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            adapter.notifyItemRangeChanged(0, adapter.data.size)
        }
    }

    override fun onStart() {
        super.onStart()
        activity?.registerReceiver(nextAlarmChangeBroadcastReceiver,
            IntentFilter(AlarmManager.ACTION_NEXT_ALARM_CLOCK_CHANGED))
        if (dialog != null) {
            dialog?.show()
        }
    }

    override fun onStop() {
        activity?.unregisterReceiver(nextAlarmChangeBroadcastReceiver)
        super.onStop()
    }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?,
    ) {
        when (requestCode) {
            1 -> {
                if (resultCode == Activity.RESULT_OK) {
                    adapter.notifyItemRangeChanged(0, adapter.data.size)
                } else {
                    Preferences.showDailySteps = false
                }

                if (dialog != null) {
                    dialog?.show()
                }
            }
            2 -> {
                try {
                    val account: GoogleSignInAccount? = GoogleSignIn.getSignedInAccountFromIntent(
                        data).getResult(ApiException::class.java)
                    if (!GoogleSignIn.hasPermissions(account, FITNESS_OPTIONS)) {
                        GoogleSignIn.requestPermissions(
                            requireActivity(),
                            1,
                            account,
                            FITNESS_OPTIONS)
                    } else {
                        adapter.notifyItemRangeChanged(0, adapter.data.size)
                    }
                } catch (e: ApiException) {
                    e.printStackTrace()
                    Preferences.showDailySteps = false
                }

                if (dialog != null) {
                    dialog?.show()
                }
            }
        }
    }

    private fun maintainScrollPosition(callback: () -> Unit) {
        scrollView.isScrollable = false
        callback.invoke()
        lifecycleScope.launch {
            delay(200)
            scrollView.isScrollable = true
        }
    }

    override fun onResume() {
        super.onResume()
        adapter.notifyItemRangeChanged(0, adapter.data.size)
        if (dialog != null) {
            dialog?.show()
        }
    }
}

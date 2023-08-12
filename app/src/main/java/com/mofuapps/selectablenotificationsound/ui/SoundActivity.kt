package com.mofuapps.selectablenotificationsound.ui

import android.app.Activity
import android.content.Intent
import android.media.RingtoneManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.android.material.appbar.MaterialToolbar
import com.mofuapps.selectablenotificationsound.R
import xyz.aprildown.ultimateringtonepicker.RingtonePickerFragment
import xyz.aprildown.ultimateringtonepicker.UltimateRingtonePicker
import xyz.aprildown.ultimateringtonepicker.getParcelableArrayExtraCompat

class SoundActivity : AppCompatActivity(), UltimateRingtonePicker.RingtonePickerListener {



    private val settings = UltimateRingtonePicker.Settings(
        systemRingtonePicker = UltimateRingtonePicker.SystemRingtonePicker(
            customSection = UltimateRingtonePicker.SystemRingtonePicker.CustomSection(),
            defaultSection = UltimateRingtonePicker.SystemRingtonePicker.DefaultSection(),
            ringtoneTypes = listOf(
                //RingtoneManager.TYPE_RINGTONE,
                RingtoneManager.TYPE_NOTIFICATION,
                //RingtoneManager.TYPE_ALARM
            )
        ),
        deviceRingtonePicker = UltimateRingtonePicker.DeviceRingtonePicker(
            deviceRingtoneTypes = listOf(
                UltimateRingtonePicker.RingtoneCategoryType.All,
                UltimateRingtonePicker.RingtoneCategoryType.Artist,
                UltimateRingtonePicker.RingtoneCategoryType.Album,
                UltimateRingtonePicker.RingtoneCategoryType.Folder
            )
        )
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sound)

        if (savedInstanceState == null) {
            val fragment = settings.createFragment()
            supportFragmentManager.beginTransaction()
                .replace(xyz.aprildown.ultimateringtonepicker.R.id.layoutRingtonePicker, fragment, TAG_RINGTONE_PICKER)
                .setPrimaryNavigationFragment(fragment)
                .commit()
        }

        val appBar = findViewById<MaterialToolbar>(R.id.topAppBar)
        appBar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        appBar.menu.findItem(R.id.menu_sound_ok).setOnMenuItemClickListener {
            getRingtonePickerFragment().onSelectClick()
            true
        }
    }



    override fun onRingtonePicked(ringtones: List<UltimateRingtonePicker.RingtoneEntry>) {
        setResult(
            Activity.RESULT_OK,
            Intent().putExtra(EXTRA_RESULT, ringtones.toTypedArray())
        )
        finish()
    }

    private fun getRingtonePickerFragment(): RingtonePickerFragment {
        return supportFragmentManager.findFragmentByTag(TAG_RINGTONE_PICKER) as RingtonePickerFragment
    }

    companion object {
        private const val TAG_RINGTONE_PICKER = "ringtone_picker"
        private const val EXTRA_RESULT = "result"

        @JvmStatic
        fun getResult(intent: Intent?): List<UltimateRingtonePicker.RingtoneEntry> {
            if (intent == null) return emptyList()
            return intent
                .getParcelableArrayExtraCompat<UltimateRingtonePicker.RingtoneEntry>(
                    EXTRA_RESULT
                )
                .toList()
        }
    }
}
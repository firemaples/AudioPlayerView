package com.firemaples.audioplayerview

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.SeekBar
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        audioPlayerView.setOnClickListener {
            val state = audioPlayerView.getCurrentButtonState()

            var next = AudioPlayerView.ButtonState.values().indexOf(state) + 1

            if (next >= AudioPlayerView.ButtonState.values().size) {
                next = 0
            }

            audioPlayerView.updateButtonState(buttonState = AudioPlayerView.ButtonState.values()[next])
        }

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                audioPlayerView.updateProgress(progress / 100f)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }
        })

        seekBar2.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                audioPlayerView.updateProgressButtonDegree(progress.toFloat())
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }
        })

        sw_replyMode.setOnCheckedChangeListener { _, checked ->
            if (checked)
                audioPlayerView.updateRadius(0, 0, 18, 18)
            else
                audioPlayerView.updateRadius(18, 18, 18, 18)
        }
    }
}

package com.example.pomodorotimer

import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Bundle
import android.os.CountDownTimer
import androidx.appcompat.app.AppCompatActivity
import com.example.pomodorotimer.databinding.ActivityTimerBinding
import java.util.Locale

class TimerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTimerBinding
    private var timer: CountDownTimer? = null

    private var tempoFocoEmMillis: Long = 0
    private var tempoDescansoEmMillis: Long = 0
    private var estouEmFoco: Boolean = true
    private var ciclosCompletos: Int = 0
    private var metaPomodoros: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTimerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val minutosFoco = intent.getIntExtra("TEMPO_FOCO", 25)
        val minutosDescanso = intent.getIntExtra("TEMPO_DESCANSO", 5)
        metaPomodoros = intent.getIntExtra("META_POMODOROS", 0)

        tempoFocoEmMillis = minutosFoco * 60 * 1000L
        tempoDescansoEmMillis = minutosDescanso * 60 * 1000L

        atualizarTextoContador()
        iniciarCronometro(tempoFocoEmMillis)

        binding.btnVoltar.setOnClickListener {
            timer?.cancel()
            finish()
        }
    }

    private fun iniciarCronometro(tempoMillis: Long) {
        timer?.cancel()

        timer = object : CountDownTimer(tempoMillis, 1000) {

            override fun onTick(millisUntilFinished: Long) {
                atualizarTextoDoTimer(millisUntilFinished)
            }

            override fun onFinish() {
                tocarSomBlip()

                if (estouEmFoco) {
                    ciclosCompletos++
                    atualizarTextoContador()

                    if (metaPomodoros > 0 && ciclosCompletos >= metaPomodoros) {
                        timer?.cancel()
                        binding.tvStatus.text = "Meta Atingida! Parabéns!"
                        binding.tvStatus.setTextColor(android.graphics.Color.parseColor("#4CAF50"))
                        binding.tvTimer.text = "FIM"
                        return
                    }

                    estouEmFoco = false
                    binding.tvStatus.text = "Hora de Descansar!"
                    binding.tvStatus.setTextColor(android.graphics.Color.parseColor("#2196F3"))
                    iniciarCronometro(tempoDescansoEmMillis)
                } else {
                    estouEmFoco = true
                    binding.tvStatus.text = "Hora de Focar!"
                    binding.tvStatus.setTextColor(android.graphics.Color.parseColor("#4CAF50"))
                    iniciarCronometro(tempoFocoEmMillis)
                }
            }
        }.start()
    }

    private fun atualizarTextoContador() {
        if (metaPomodoros > 0) {
            binding.tvContador.text = "Pomodoros completados: $ciclosCompletos / $metaPomodoros"
        } else {
            binding.tvContador.text = "Pomodoros completados: $ciclosCompletos"
        }
    }

    private fun tocarSomBlip() {
        val mediaPlayer = MediaPlayer.create(this, R.raw.blip)
        mediaPlayer.setAudioAttributes(
            AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                .build()
        )
        mediaPlayer.start()
        mediaPlayer.setOnCompletionListener { mp ->
            mp.release()
        }
    }

    private fun atualizarTextoDoTimer(millis: Long) {
        val minutos = (millis / 1000) / 60
        val segundos = (millis / 1000) % 60
        val tempoFormatado = String.format(Locale.getDefault(), "%02d:%02d", minutos, segundos)
        binding.tvTimer.text = tempoFormatado
    }

    override fun onDestroy() {
        super.onDestroy()
        timer?.cancel()
    }
}
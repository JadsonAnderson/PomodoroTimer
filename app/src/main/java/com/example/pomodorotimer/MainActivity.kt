package com.example.pomodorotimer

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.pomodorotimer.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnIniciar.setOnClickListener {
            val focoTexto = binding.etTempoFoco.text.toString()
            val descansoTexto = binding.etTempoDescanso.text.toString()
            val metaTexto = binding.etMetaPomodoros.text.toString()

            val tempoFoco = if (focoTexto.isNotEmpty()) focoTexto.toInt() else 25
            val tempoDescanso = if (descansoTexto.isNotEmpty()) descansoTexto.toInt() else 5
            val metaPomodoros = if (metaTexto.isNotEmpty()) metaTexto.toInt() else 0

            val intent = Intent(this, TimerActivity::class.java)
            intent.putExtra("TEMPO_FOCO", tempoFoco)
            intent.putExtra("TEMPO_DESCANSO", tempoDescanso)
            intent.putExtra("META_POMODOROS", metaPomodoros)

            startActivity(intent)
        }
    }
}
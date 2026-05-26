package com.example.pomodorotimer

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.pomodorotimer.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    // View Binding: Cria uma variável para acessar os elementos do layout XML (como botões e textos).
    private lateinit var binding: ActivityMainBinding

    // Funciona como "ponto de entrada" da Activity, executando assim que o app abre.
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState) // Executa a lógica padrão de inicialização da superclasse.

        // Inicializa o View Binding "inflando" (desenhando) o layout XML correspondente na memória.
        binding = ActivityMainBinding.inflate(layoutInflater)

        // Define que o conteúdo visual desta Activity na tela do usuário será a raiz (root) do XML configurado.
        setContentView(binding.root)

        // Configura um escutador de cliques (Listener) para o botão "Iniciar Timer".
        binding.btnIniciar.setOnClickListener {

            // 1. CAPTURA DE DADOS: Lê o que o usuário digitou nos campos de texto e transforma em String.
            val focoTexto = binding.etTempoFoco.text.toString()
            val descansoTexto = binding.etTempoDescanso.text.toString()
            val metaTexto = binding.etMetaPomodoros.text.toString()

            /**
             * 2. VALIDAÇÃO DE DADOS (Programação Defensiva):
             * Verifica se os campos estão vazios. Se o usuário digitou algo, converte para Inteiro (.toInt()).
             * Se deixou vazio, o operador 'else' assume valores padrão seguros (25, 5 e 0) para o app não travar.
             */
            val tempoFoco = if (focoTexto.isNotEmpty()) focoTexto.toInt() else 25
            val tempoDescanso = if (descansoTexto.isNotEmpty()) descansoTexto.toInt() else 5
            val metaPomodoros = if (metaTexto.isNotEmpty()) metaTexto.toInt() else 0

            /**
             * 3. CRIAÇÃO DA INTENT:
             * Cria uma Intent Explícita. Ela serve como uma mensagem para o sistema operacional Android,
             * dizendo claramente que a intenção é sair desta tela atual (this) e abrir a tela do cronômetro (TimerActivity::class.java).
             */
            val intent = Intent(this, TimerActivity::class.java)

            /**
             * 4. EMPACOTAMENTO DE DADOS (Extras):
             * Anexa as variáveis validadas dentro da Intent usando um sistema de "Chave e Valor".
             * Essas etiquetas em maiúsculo ("TEMPO_FOCO", etc.) serão usadas pela TimerActivity para pescar os valores do outro lado.
             */
            intent.putExtra("TEMPO_FOCO", tempoFoco)
            intent.putExtra("TEMPO_DESCANSO", tempoDescanso)
            intent.putExtra("META_POMODOROS", metaPomodoros)

            /**
             * 5. NAVEGAÇÃO:
             * Envia a ordem final para o Android disparar a inicialização da TimerActivity, levando os dados junto.
             */
            startActivity(intent)
        }
    }
}
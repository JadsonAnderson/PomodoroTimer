package com.example.pomodorotimer

import android.media.AudioAttributes // Permite categorizar o tipo de áudio para o sistema operacional.
import android.media.MediaPlayer // Classe nativa para reprodução de arquivos de som.
import android.os.Bundle
import android.os.CountDownTimer // Classe do ecossistema Android usada para criar contagens regressivas.
import androidx.appcompat.app.AppCompatActivity
import com.example.pomodorotimer.databinding.ActivityTimerBinding
import java.util.Locale // Garante que a formatação do texto (ex: números) siga o padrão do idioma do aparelho.

class TimerActivity : AppCompatActivity() {

    // Instância do View Binding para acessar os componentes visuais do layout activity_timer.xml.
    private lateinit var binding: ActivityTimerBinding

    // Objeto do tipo CountDownTimer mantido globalmente para que possamos iniciá-lo, pausá-lo ou cancelá-lo a qualquer momento.
    private var timer: CountDownTimer? = null

    // Variáveis de controle de tempo convertidas para milissegundos (padrão exigido pelo CountDownTimer).
    private var tempoFocoEmMillis: Long = 0
    private var tempoDescansoEmMillis: Long = 0

    // Flag (etiqueta booleana) para rastrear o estado atual do timer: true = Foco, false = Descanso.
    private var estouEmFoco: Boolean = true

    // Contadores para monitorar o progresso do usuário em tempo real.
    private var ciclosCompletos: Int = 0
    private var metaPomodoros: Int = 0 // Armazena o teto máximo de ciclos definido pelo usuário.

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTimerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        /**
         * RECEBIMENTO DE PARÂMETROS (Intents):
         * Captura os valores numéricos empacotados pela MainActivity.
         * O segundo argumento (ex: 25, 5, 0) serve como um valor de fallback (segurança)
         * caso o Android perca a referência das chaves originais.
         */
        val minutosFoco = intent.getIntExtra("TEMPO_FOCO", 25)
        val minutosDescanso = intent.getIntExtra("TEMPO_DESCANSO", 5)
        metaPomodoros = intent.getIntExtra("META_POMODOROS", 0)

        // Conversão matemática simples: Minutos -> Segundos -> Milissegundos (multiplica por 60 e depois por 1000).
        tempoFocoEmMillis = minutosFoco * 60 * 1000L
        tempoDescansoEmMillis = minutosDescanso * 60 * 1000L

        // Atualiza o texto do contador na interface (Ex: "0" ou "0 / 4") antes do início.
        atualizarTextoContador()

        // Inicializa a execução da primeira rodada do cronômetro usando o tempo de foco.
        iniciarCronometro(tempoFocoEmMillis)

        // Configuração do botão "Parar e Voltar".
        binding.btnVoltar.setOnClickListener {
            timer?.cancel() // Interrompe imediatamente a thread do cronômetro para evitar vazamento de memória.
            finish() // Destrói a TimerActivity atual e remove-a da pilha, retornando à MainActivity.
        }
    }

    /**
     * FUNÇÃO PRINCIPAL DO CRONÔMETRO:
     * Recebe um período de tempo em milissegundos e gerencia a contagem regressiva.
     */
    private fun iniciarCronometro(tempoMillis: Long) {
        // Zera e limpa qualquer instância anterior do timer para prevenir bugs de cronômetros rodando em duplicidade.
        timer?.cancel()

        /**
         * Criação anônima de um CountDownTimer.
         * Parâmetro 1 (tempoMillis): Tempo total que o cronômetro deve rodar.
         * Parâmetro 2 (1000): Intervalo de pulso (de quanto em quanto tempo ele roda o método onTick). Aqui, 1000ms = 1 segundo.
         */
        timer = object : CountDownTimer(tempoMillis, 1000) {

            // Executado automaticamente a cada 1 segundo (pulso).
            override fun onTick(millisUntilFinished: Long) {
                // Atualiza o TextView do relógio com o tempo restante formatado.
                atualizarTextoDoTimer(millisUntilFinished)
            }

            // Executado de forma automática e assíncrona quando a contagem regressiva chega exatamente a 0.
            override fun onFinish() {
                tocarSomBlip() // Dispara o alerta sonoro para notificar o usuário.

                // MÁQUINA DE ESTADOS: Verifica onde o usuário estava quando o tempo esgotou.
                if (estouEmFoco) {
                    // Se o tempo acabou e ele estava em FOCO, significa que completou um ciclo com sucesso!.
                    ciclosCompletos++
                    atualizarTextoContador() // Atualiza o placar visual na tela

                    // VALIDAÇÃO DA META DEFINIDA:
                    // Se o usuário estabeleceu uma meta (metaPomodoros > 0) e ela foi atingida ou superada:
                    if (metaPomodoros > 0 && ciclosCompletos >= metaPomodoros) {
                        timer?.cancel() // Finaliza o fluxo do cronômetro por completo.
                        binding.tvStatus.text = "Meta Atingida! Parabéns!"
                        binding.tvStatus.setTextColor(android.graphics.Color.parseColor("#4CAF50")) // Altera a cor do texto para verde sucesso.
                        binding.tvTimer.text = "FIM"
                        return // Interrompe a execução aqui com um 'return abrupto' para não iniciar o descanso.
                    }

                    // Se a meta não foi atingida (ou for infinita), altera o estado para DESCANSO.
                    estouEmFoco = false
                    binding.tvStatus.text = "Hora de Descansar!"
                    binding.tvStatus.setTextColor(android.graphics.Color.parseColor("#2196F3")) // Muda para azul descritivo.
                    iniciarCronometro(tempoDescansoEmMillis) // Reinicia o motor do cronômetro com o tempo de descanso.
                } else {
                    // Se o tempo acabou e ele estava em DESCANSO, altera o estado de volta para FOCO.
                    estouEmFoco = true
                    binding.tvStatus.text = "Hora de Focar!"
                    binding.tvStatus.setTextColor(android.graphics.Color.parseColor("#4CAF50")) // Retorna para o verde de atividade.
                    iniciarCronometro(tempoFocoEmMillis) // Reinicia o motor do cronômetro com o tempo de foco.
                }
            }
        }.start() // O método .start() é o gatilho que inicia a execução imediata da contagem.
    }

    /**
     * Atualiza o placar de metas de acordo com o cenário:
     * Com meta definida exibe: "Pomodoros completados: X / Y"
     * Sem meta definida exibe de forma infinita: "Pomodoros completados: X".
     */
    private fun atualizarTextoContador() {
        if (metaPomodoros > 0) {
            binding.tvContador.text = "Pomodoros completados: $ciclosCompletos / $metaPomodoros"
        } else {
            binding.tvContador.text = "Pomodoros completados: $ciclosCompletos"
        }
    }

    /**
     * REPRODUÇÃO DE ÁUDIO ASSÍNCRONA E OTIMIZADA:
     */
    private fun tocarSomBlip() {
        // Instancia o player apontando diretamente para o arquivo 'blip' inserido na pasta res/raw.
        val mediaPlayer = MediaPlayer.create(this, R.raw.blip)

        // Informa ao Android as propriedades do áudio para que o sistema priorize a saída correta (mídia/notificação assistiva).
        mediaPlayer.setAudioAttributes(
            AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION) // Categoriza como efeito de interface.
                .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                .build()
        )

        mediaPlayer.start() // Inicia o som.

        // Ouvinte de encerramento: Fundamental para a gerência de memória do dispositivo.
        // Assim que o som de 1 ou 2 segundos termina de tocar, ele desaloca o objeto da memória do celular.
        mediaPlayer.setOnCompletionListener { mp ->
            mp.release() // Evita estouro de memória ram (Memory Leak) em sessões longas.
        }
    }

    /**
     * FORMATAÇÃO DE STRINGS:
     * Converte os milissegundos brutos em uma representação visual amigável do tipo "MM:SS".
     */
    private fun atualizarTextoDoTimer(millis: Long) {
        val minutos = (millis / 1000) / 60 // Converte o total de milissegundos restantes para minutos inteiros.
        val segundos = (millis / 1000) % 60 // Pega o resto da divisão para descobrir os segundos daquela fração.

        // Monta o padrão textual formatado. O "%02d" garante que números menores que 10 ganhem um zero à esquerda (Ex: 05).
        val tempoFormatado = String.format(Locale.getDefault(), "%02d:%02d", minutos, segundos)
        binding.tvTimer.text = tempoFormatado // Imprime o resultado no TextView correspondente.
    }

    /**
     * CICLO DE VIDA DO ANDROID (onDestroy):
     * Executado automaticamente se o usuário fechar o aplicativo de forma abrupta ou o sistema destruir a Activity.
     */
    override fun onDestroy() {
        super.onDestroy()
        timer?.cancel() // Garante que o timer pare em background se a tela for destruída, poupando bateria e processamento do celular.
    }
}
package com.example.adivinaadivinador

import android.media.MediaPlayer
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.get
import com.airbnb.lottie.LottieAnimationView
import com.github.javafaker.Faker
import com.google.android.flexbox.FlexboxLayout
import kotlin.random.Random

class MainActivity : AppCompatActivity() {
    private lateinit var txtPregunta: TextView
    private var respuesta: String = ""
    private lateinit var flexAlfabeto: FlexboxLayout
    private lateinit var flexResponse: FlexboxLayout
    private var indicesOcupados: ArrayList<Int> = arrayListOf()
    private var intentosPermitidos: Int = 0
    private var intentosHechos: Int = 0
    private lateinit var txtCantIntentos: TextView
    private lateinit var txtMsjIntentos: TextView
    private var finalizado: Boolean = false
    private lateinit var lottieResult: LottieAnimationView
    private lateinit var lotieAnimThinking: LottieAnimationView
    private lateinit var textMsjResultado: TextView
    private lateinit var txtMsjRespuestaCorrecta: TextView
    private lateinit var restartButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        installSplashScreen()

        setContentView(R.layout.activity_main)

        // Widgets
        txtPregunta = findViewById(R.id.txtPregunta)
        lotieAnimThinking = findViewById(R.id.animation_view_thik)
        flexResponse = findViewById(R.id.edt)
        flexAlfabeto = findViewById(R.id.flexboxLayout)
        txtCantIntentos = findViewById(R.id.txtCantIntentos)
        txtMsjIntentos = findViewById(R.id.txtMsjIntentos)
        lottieResult = findViewById(R.id.animation_view_resultado)
        textMsjResultado = findViewById(R.id.txtMsjResultado)
        txtMsjRespuestaCorrecta = findViewById(R.id.txtMsjRespuestaCorrecta)
        restartButton = findViewById(R.id.restartButton)

        respuesta = obtenerPalabraAleatoria().uppercase()
        intentosPermitidos = respuesta.length + 2
        txtCantIntentos.text = "$intentosHechos/$intentosPermitidos"

        val alfabeto = generarAlfabeto(respuesta)
        var alfabetoDesorden = desordenar(alfabeto)

        mostrarEspacioRespuesta(respuesta.length, flexResponse)
        mostrarAlfabeto(alfabetoDesorden.uppercase(), flexAlfabeto)

        restartButton.setOnClickListener {
            recreate()
        }
    }

    fun generarAlfabeto(semilla: String): String {
        val randomValues = List(5) { Random.nextInt(65, 90).toChar() }
        return "$semilla${randomValues.joinToString(separator = "")}"
    }

    fun desordenar(theWord: String): String {
        val theTempWord = theWord.toMutableList()
        for (item in 0..Random.nextInt(1, theTempWord.count() - 1)) {
            val indexA = Random.nextInt(theTempWord.count() - 1)
            val indexB = Random.nextInt(theTempWord.count() - 1)
            val temp = theTempWord[indexA]
            theTempWord[indexA] = theTempWord[indexB]
            theTempWord[indexB] = temp
        }
        return theTempWord.joinToString(separator = "")
    }

    fun obtenerPalabraAleatoria(): String {
        val faker = Faker()
        val palabra = faker.artist().name()
        return palabra.split(' ').get(0)
    }

    fun mostrarEspacioRespuesta(cantidad: Int, vista: FlexboxLayout) {
        for (letter in 1..cantidad) {
            val btnLetra = EditText(this)
            btnLetra.isEnabled = false
            val layoutParams = FlexboxLayout.LayoutParams(
                FlexboxLayout.LayoutParams.WRAP_CONTENT,
                FlexboxLayout.LayoutParams.WRAP_CONTENT
            )
            layoutParams.setMargins(5, 5, 5, 5)
            btnLetra.layoutParams = layoutParams
            vista.addView(btnLetra)
        }
    }

    fun mostrarAlfabeto(alfabeto: String, vista: FlexboxLayout) {
        for (letter in alfabeto) {
            val btnLetra = Button(this)
            btnLetra.text = letter.toString()
            btnLetra.textSize = 12f
            val layoutParams = FlexboxLayout.LayoutParams(
                FlexboxLayout.LayoutParams.WRAP_CONTENT,
                FlexboxLayout.LayoutParams.WRAP_CONTENT
            )
            layoutParams.setMargins(5, 5, 5, 5)
            btnLetra.layoutParams = layoutParams
            vista.addView(btnLetra)
            btnLetra.setOnClickListener {
                var mediaPlayer: MediaPlayer = MediaPlayer.create(this, R.raw.button_press)
                mediaPlayer.start()
                clickLetra(it as Button)
            }
        }
    }

    fun verificarResultado() {
        if (intentosHechos == intentosPermitidos || indicesOcupados.size == respuesta.length) {
            finalizado = true

            //si gano o perdió
            if (indicesOcupados.size == respuesta.length) {
                var mediaPlayer = MediaPlayer.create(this, R.raw.game_win)
                mediaPlayer.start()

                lottieResult.setAnimation(R.raw.success)
                textMsjResultado.text = "Has ganado, felicidades!"
            } else {
                var mediaPlayer = MediaPlayer.create(this, R.raw.game_over)
                mediaPlayer.start()
                lottieResult.setAnimation(R.raw.fail)
                textMsjResultado.text = "Has perdido!"
            }

            txtMsjRespuestaCorrecta.setText("La respuesta correcta era: $respuesta")

            //despues de configurar la vista ponerlas como visibles
            textMsjResultado.visibility = View.VISIBLE
            lottieResult.visibility = View.VISIBLE
            txtMsjRespuestaCorrecta.visibility = View.VISIBLE
            restartButton.visibility = View.VISIBLE

            //ocultar los que no se deben mostrar
            flexResponse.visibility = View.GONE
            txtCantIntentos.visibility = View.GONE
            flexAlfabeto.visibility = View.GONE
            txtMsjIntentos.visibility = View.GONE
            txtPregunta.visibility = View.GONE
            lotieAnimThinking.visibility = View.GONE
        }
    }

    fun clickLetra(btnClicked: Button) {
        if (!finalizado) {
            //obtener el indice de la letra seleccionada inicialmente
            var starIndex = 0
            var resIndex = respuesta.indexOf(btnClicked.text.toString())

            //si el indice ya fue ocupado entonces no tomar en cuenta los indices hacia atras
            while (indicesOcupados.contains(resIndex)) {
                starIndex = resIndex + 1
                resIndex = respuesta.indexOf(btnClicked.text.toString(), starIndex)
            }

            //si la respuesta contiene la letra seleccionada
            if (resIndex != -1) {
                val flexRow = flexResponse.get(resIndex) as EditText
                flexRow.setText(respuesta.get(resIndex).toString())
                indicesOcupados.add(resIndex)
                btnClicked.setBackgroundColor(android.graphics.Color.GREEN)
                btnClicked.isEnabled = false
                btnClicked.setTextColor(android.graphics.Color.WHITE)
            } else {
//                Toast.makeText(
//                    applicationContext, "Letra incorrecta!",
//                    Toast.LENGTH_SHORT
//                ).show()
                btnClicked.setBackgroundColor(android.graphics.Color.RED)
                btnClicked.isEnabled = false
                btnClicked.setTextColor(android.graphics.Color.WHITE)
            }
            intentosHechos++

            txtCantIntentos.text = "$intentosHechos/$intentosPermitidos"
            verificarResultado()
        }
    }
}
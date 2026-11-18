package com.seuapp.emuladorps2

import android.net.Uri
import android.os.Bundle
import android.system.Os
import android.system.OsConstants
import android.view.MotionEvent
import android.view.SurfaceView
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private var biosUri: Uri? = null
    private var romUri: Uri? = null

    private val selectBiosLauncher = registerForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let {
            biosUri = it
            Toast.makeText(this, "BIOS selecionada!", Toast.LENGTH_SHORT).show()
        }
    }

    private val selectRomLauncher = registerForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let {
            romUri = it
            Toast.makeText(this, "Jogo selecionado!", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val btnSelectBios = findViewById<Button>(R.id.btnSelectBios)
        val btnSelectRom = findViewById<Button>(R.id.btnSelectRom)
        val btnPlay = findViewById<Button>(R.id.btnPlay)
        val menuLayout = findViewById<LinearLayout>(R.id.menuLayout)
        val controlsOverlay = findViewById<RelativeLayout>(R.id.controlsOverlay)
        val gameSurface = findViewById<SurfaceView>(R.id.gameSurface)

        btnSelectBios.setOnClickListener {
            selectBiosLauncher.launch(arrayOf("*/*"))
        }

        btnSelectRom.setOnClickListener {
            selectRomLauncher.launch(arrayOf("*/*"))
        }

        btnPlay.setOnClickListener {
            if (biosUri == null || romUri == null) {
                Toast.makeText(this, "Selecione a BIOS e a ROM primeiro!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            menuLayout.visibility = View.GONE
            controlsOverlay.visibility = View.VISIBLE

            val biosFd = contentResolver.openFileDescriptor(biosUri!!, "r")?.detachFd() ?: -1
            val romFd = contentResolver.openFileDescriptor(romUri!!, "r")?.detachFd() ?: -1

            Toast.makeText(this, "Núcleo ARMSX2 carregado! Iniciando...", Toast.LENGTH_SHORT).show()
        }

        configurarBotao(findViewById(R.id.btnCross), BOTAO_X)
        configurarBotao(findViewById(R.id.btnCircle), BOTAO_O)
        configurarBotao(findViewById(R.id.btnDpadUp), BOTAO_CIMA)
    }

    private fun configurarBotao(botao: Button, codigoBotao: Int) {
        botao.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> { }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> { }
            }
            true
        }
    }

    companion object {
        const val BOTAO_X = 1
        const val BOTAO_O = 2
        const val BOTAO_CIMA = 3

        init {
            System.loadLibrary("c++_shared")

            val pageSize = Os.sysconf(OsConstants._SC_PAGESIZE)
            if (pageSize == 16384L) {
                System.loadLibrary("emucore_16k")
            } else {
                System.loadLibrary("emucore_4k")
            }
        }
    }
}

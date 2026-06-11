package com.damaris.suquillo.manoymente.data.hardware

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.os.Handler
import android.os.Looper
import android.util.Log
import java.io.IOException
import java.io.InputStream
import java.util.*

class LectorRfidManager(private val listener: RfidListener) {

    interface RfidListener {
        fun onTagLeido(tag: String)
        fun onConectado()
        fun onError(mensaje: String)
    }

    private val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    private var socket: BluetoothSocket? = null
    private var threadLectura: Thread? = null
    private val uuidSeguro: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")

    // Bandera para saber si nosotros cerramos el juego o si se cayó solo
    private var desconexionIntencional = false

    @SuppressLint("MissingPermission")
    fun conectar() {
        desconexionIntencional = false
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled) {
            listener.onError("Bluetooth no disponible o apagado")
            return
        }

        val dispositivosVinculados: Set<BluetoothDevice>? = bluetoothAdapter.bondedDevices
        val mesaDevice = dispositivosVinculados?.find { it.name == "Mesa_ManoYMente" }

        if (mesaDevice == null) {
            listener.onError("No se encontró la 'Mesa_ManoYMente'.")
            return
        }

        Thread {
            try {
                // Cerramos el socket anterior por si quedó colgado
                socket?.close()

                socket = mesaDevice.createRfcommSocketToServiceRecord(uuidSeguro)
                socket?.connect()

                Handler(Looper.getMainLooper()).post { listener.onConectado() }
                iniciarHiloLectura(socket?.inputStream)

            } catch (e: IOException) {
                Log.e("Bluetooth", "Error de conexión", e)
                manejarDesconexion()
            }
        }.start()
    }

    private fun iniciarHiloLectura(inputStream: InputStream?) {
        if (inputStream == null) return

        threadLectura = Thread {
            val buffer = ByteArray(1024)
            var bytes: Int

            while (!desconexionIntencional) {
                try {
                    bytes = inputStream.read(buffer)
                    val mensajeRecibido = String(buffer, 0, bytes).trim()

                    if (mensajeRecibido.isNotEmpty()) {
                        Handler(Looper.getMainLooper()).post {
                            listener.onTagLeido(mensajeRecibido)
                        }
                    }
                } catch (e: IOException) {
                    Log.e("Bluetooth", "Conexión perdida", e)
                    manejarDesconexion()
                    break
                }
            }
        }
        threadLectura?.start()
    }

    private fun manejarDesconexion() {
        if (!desconexionIntencional) {
            Handler(Looper.getMainLooper()).post {
                listener.onError("Conexión perdida. Reconectando...")
            }
            Handler(Looper.getMainLooper()).postDelayed({
                conectar()
            }, 3000)
        }
    }

    fun desconectar() {
        desconexionIntencional = true
        try {
            socket?.close()
            threadLectura?.interrupt()
        } catch (e: IOException) {
            Log.e("Bluetooth", "Error al cerrar", e)
        }
    }
}
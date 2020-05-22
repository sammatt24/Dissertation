package com.example.dissertation

import android.app.ProgressDialog
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.os.*
import android.speech.tts.TextToSpeech
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.dissertation.MainActivity.Companion.EXTRA_ADDRESS
import com.example.dissertation.MainActivity.Companion.EXTRA_PLAYERNO
import kotlinx.android.synthetic.main.activity_start_game.*
import java.util.*
import kotlin.collections.ArrayList
import org.jetbrains.anko.toast
import java.io.IOException

/**
 * The second class for the Snake and Ladders game. The activity is able to roll a virtual dice
 * and send the Bluetooth signals to the Bluetooth chip and move the servos on the board.
 *
 * @author Samuel Matthews
 * @since 14/05/2020
 */
class StartGameActivity : AppCompatActivity() {

    /**
     * Companion object for the Bluetooth connection and number of players used in this activity.
     */
    companion object {
        var myUUID : UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
        var bluetoothSocket : BluetoothSocket? = null
        lateinit var progressLog : ProgressDialog
        lateinit var bluetoothAdapter: BluetoothAdapter
        var isConnected : Boolean = false
        lateinit var btMacAddress : String
        lateinit var message : String
    }

    lateinit var mTextToSpeech: TextToSpeech
    private val rng = Random()
    private var playerCount = 0
    var playerPosition = ArrayList<Int>()
    var turnCounter = 0
    var winner : Boolean = false

    /**
     * onCreate that sets an onClickListener that rolls the dice and sets up the text-to-speach
     * within the class. The Bluetooth connection is also initialized in this function.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //Hides activity bar at top of application.
        try {
            this.supportActionBar!!.hide()
        } catch (e: NullPointerException) {
        }

        //Text to speech initializer.
        mTextToSpeech = TextToSpeech(applicationContext, TextToSpeech.OnInitListener { status ->
            if(status != TextToSpeech.ERROR) {
                mTextToSpeech.language = Locale.UK
            }
        })

        setContentView(R.layout.activity_start_game)

        //Intents from last activity (number of players and Bluetooth MAC address).
        message = intent.getStringExtra("player_number")
        btMacAddress = intent.getStringExtra("device_address")

        //Connect to Bluetooth device.
        ConnectToDevice(this).execute()

        playerCount = message.toInt()
        val imageDiceRoller = findViewById<ImageView>(R.id.imageDiceRoller) as ImageView
        val playerTurn = findViewById<TextView>(R.id.textViewPlayerTurn)
        val currentPosition = findViewById<TextView>(R.id.textViewCurrentPosition)

        //Sets up the amount of players in the player array.
        for (i in 0..playerCount - 1) {
           playerPosition.add(i, 1)
        }

        playerTurn.setText(getString(R.string.player_turn1))
        currentPosition.setText(getString(R.string.current_position))

        //Click listener for dice imageview.
        imageDiceRoller.setOnClickListener {
            rollDice()
        }
    }

    /**
     * Function that rolls dice and changes picture to dice number rolled.
     */
    fun rollDice() {
        var randomNumber = rng.nextInt(6) + 1

        //Rolls a dice based on the random number above.
        when (randomNumber) {
            1 -> {
                imageDiceRoller.setImageResource(R.drawable.dice1)
                nextTurn(1)
            }
            2 -> {
                imageDiceRoller.setImageResource(R.drawable.dice2)
                nextTurn(2)
            }
            3 -> {
                imageDiceRoller.setImageResource(R.drawable.dice3)
                nextTurn(3)
            }
            4 -> {
                imageDiceRoller.setImageResource(R.drawable.dice4)
                nextTurn(4)
            }
            5 -> {
                imageDiceRoller.setImageResource(R.drawable.dice5)
                nextTurn(5)
            }
            6 -> {
                imageDiceRoller.setImageResource(R.drawable.dice6)
                nextTurn(6)
            }
        }
    }

    /**
     * Main function for the game that sends commands to the Bluetooth, sends haptic vibration,
     * changes the player position, utilizes text-to-speech and changes the players turn.
     * @param diceRoll  the number rolled by the dice.
     */
    fun nextTurn(diceRoll: Int) {
        val playerTurn = findViewById<TextView>(R.id.textViewPlayerTurn)
        val currentPosition = findViewById<TextView>(R.id.textViewCurrentPosition)

        var playerString = ""

        //Vibrates the phone based on the roll.
        for (i in 1..diceRoll) {
            vibratePhone()
            Thread.sleep(250)
        }

        //Changes the player that rolled the dice's turn.
        playerPosition[turnCounter] = playerPosition[turnCounter] + diceRoll

        //When statement to check for snakes, ladders and the winning position. Text-to-speech is
        //utilized to tell the user that positions and the state of the position.
        when(playerPosition[turnCounter]) {
            2 -> {
                playerPosition[turnCounter] = 8
                toast("Player " + (turnCounter + 1) + "has hit 2. Climb a ladder " +
                        "and move to 8")
                mTextToSpeech.speak("Player " + (turnCounter + 1) + "has hit 2. Climb a " +
                        "ladder and move to 7", TextToSpeech.QUEUE_FLUSH, null)
            }
            7 -> {
                playerPosition[turnCounter] = 1
                toast("Player " + (turnCounter + 1) + "has hit 7. Slither down a snake " +
                        "and move to 1")
                mTextToSpeech.speak("Player " + (turnCounter + 1) + "has hit 7. Slither " +
                        "down a snake and move to 1", TextToSpeech.QUEUE_FLUSH, null)
            }
            10 -> {
                winner = true
                toast("Player " + (turnCounter + 1) + " HAS WON!!!")
                mTextToSpeech.speak("Player " + (turnCounter + 1) + " has won",
                    TextToSpeech.QUEUE_FLUSH, null)
            }
            in 11..15 -> {
                playerPosition[turnCounter] = 9
                toast("Player " + (turnCounter + 1) + " has rolled over the win, move to 9")
                mTextToSpeech.speak("Player " + (turnCounter + 1) + " has rolled over the " +
                        "win, move to 9", TextToSpeech.QUEUE_FLUSH, null)
            }
            else -> {
                toast("Player " + (turnCounter + 1) + " has thrown a " + diceRoll +
                        " and moved to " + playerPosition[turnCounter])
                mTextToSpeech.speak("Player " + (turnCounter + 1) + "has moved to " +
                        playerPosition[turnCounter].toString(), TextToSpeech.QUEUE_FLUSH, null)
            }
        }

        //Checks if the winning position is hit and disconnects the Bluetooth if this is true
        //and sends the player's position to the board.
        if (winner == true) {
            sendCommand("w")
            disconnect()
        } else {
            sendCommand(playerPosition[turnCounter].toString())
        }

        //Cycles the turnCounter per the amount of players input in the first activity.
        if (turnCounter == (playerCount - 1)) {
            turnCounter = 0
            playerString = "Player " + (turnCounter + 1) + " Turn"
        } else {
            turnCounter = turnCounter + 1
            playerString = "Player " + (turnCounter + 1) + " Turn"
        }

        playerTurn.setText(playerString)
        currentPosition.setText(playerPosition[turnCounter].toString())
    }

    /**
     * Function that vibrates the phone for 200 milliseconds.
     */
    fun vibratePhone() {
        val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (Build.VERSION.SDK_INT >= 26) {
            vibrator.vibrate(VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            vibrator.vibrate(200)
        }
    }

    /**
     * Function that sends the command to the Bluetooth
     * @param input     The number sent to the board to move the servos.
     */
    private fun sendCommand(input : String) {
        if (bluetoothSocket != null) {
            try {
                bluetoothSocket!!.outputStream.write(input.toByteArray())
            } catch (e : IOException) {
                e.printStackTrace()
                toast("NOT SENT")
            }
        }
    }

    /**
     * Function that disconnects the Bluetooth module from the Android Device.
     */
    private fun disconnect() {
        if (bluetoothSocket != null) {
            try {
                bluetoothSocket!!.close()
                bluetoothSocket = null
                isConnected = false
            } catch (e : IOException) {
                e.printStackTrace()
            }
        }
        finish()
    }

    /**
     * Class that runs a background task to connect the Bluetooth module to the Android device.
     * @param c     this, able to ask for Bluetooth permissions.
     */
    private class ConnectToDevice(c : Context) : AsyncTask<Void, Void, String>() {
        private var connectSuccess: Boolean = true
        private val context: Context

        init {
            this.context = c
        }

        override fun onPreExecute() {
            super.onPreExecute()
            progressLog = ProgressDialog.show(context, "Connecting...", "Please Wait")
        }

        override fun doInBackground(vararg p0: Void?) : String? {
            try {
                if (bluetoothSocket == null || !isConnected) {
                    bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
                    val device: BluetoothDevice = bluetoothAdapter.getRemoteDevice(btMacAddress)
                    bluetoothSocket = device.createInsecureRfcommSocketToServiceRecord(myUUID)
                    BluetoothAdapter.getDefaultAdapter().cancelDiscovery()
                    bluetoothSocket!!.connect()
                }
            } catch(e : IOException) {
                connectSuccess = false
                e.printStackTrace()
            }

            return null
        }

        override fun onPostExecute(result : String?) {
            super.onPostExecute(result)
            if (!connectSuccess) {
                Log.i("data", "Couldn't Connect")
            } else {
                isConnected = true
            }
            progressLog.dismiss()
        }
    }
}
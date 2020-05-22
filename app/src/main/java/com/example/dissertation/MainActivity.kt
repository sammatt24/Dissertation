package com.example.dissertation

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.toast


/**
 * The starting class for the Snake and Ladders game. The activity is able to input
 * a number from the user that will be used in te next activity for the number of players
 * in the game as well as search for Bluetooth devices to connect to.
 *
 * @author Samuel Matthews
 * @since 14/05/2020
 */
class MainActivity : AppCompatActivity() {

    //Variables used for Bluetooth connection.
    private var bluetoothAdapter: BluetoothAdapter? = null
    private lateinit var pairedDevices: Set<BluetoothDevice>
    private val REQUEST_ENABLE_BLUETOOTH = 1

    /**
     * Companion Object used for
     */
    companion object {
        const val EXTRA_ADDRESS : String = "device_address"
        const val EXTRA_PLAYERNO : String = "player_number"

    }

    /**
     * onCreate that initializes all variables and functions for the application.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //To hide task bar at top of application.
        try {
            this.supportActionBar!!.hide()
        } catch (e: NullPointerException) {
        }

        setContentView(R.layout.activity_main)

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

        //If statements to check if the Bluetooth Adapter is available
        if (bluetoothAdapter == null) {
            toast("This device does not support BlueTooth connection.")
            return
        }
        if (!bluetoothAdapter!!.isEnabled) {
            val enableBluetoothIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBluetoothIntent, REQUEST_ENABLE_BLUETOOTH)
        }

        // Click listener for refresh button.
        buttonRefreshBT.setOnClickListener{
            refreshBTList()
        }
    }

    /**
     * Function to refresh the list of Bluetooth devices and set a click listener on the recycler
     * view to connect to a Bluetooth device.
     */
    private fun refreshBTList() {
        pairedDevices = bluetoothAdapter!!.bondedDevices
        val btList : ArrayList<BluetoothDevice> = ArrayList()
        val playerNumberText = findViewById<EditText>(R.id.editTextNumberOfPlayers)
        val playerNumber = playerNumberText.text.toString()

        if (pairedDevices.isNotEmpty()) {
            for (btDevice: BluetoothDevice in pairedDevices) {
                btList.add(btDevice)
            }
        } else {
            toast("No Bluetooth Devices Found.")
        }

        val listAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, btList)
        listViewBT?.adapter = listAdapter
        listViewBT.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            val btDevice : BluetoothDevice = btList[position]
            val btMacAddress : String = btDevice.address

            if (playerNumberText.text.isEmpty()) {
                    toast("Please enter an amount of players.")
            } else {
                val intent = Intent(this, StartGameActivity::class.java).apply {
                    putExtra(EXTRA_PLAYERNO, playerNumber)
                    putExtra(EXTRA_ADDRESS, btMacAddress)
                }
                startActivity(intent)
            }
        }
    }

    /**
     * An onActivityResult to check that the Bluetooth Adapter and device are enabled.
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_ENABLE_BLUETOOTH) {
            if (resultCode == Activity.RESULT_OK) {
                if (bluetoothAdapter!!.isEnabled) {
                    toast("Bluetooth is enabled")
                } else {
                    toast("Bluetooth is not enabled")
                }
            } else if (resultCode == Activity.RESULT_CANCELED) {
                toast("Bluetooth enabling has been cancelled")
            }
        }
    }
}
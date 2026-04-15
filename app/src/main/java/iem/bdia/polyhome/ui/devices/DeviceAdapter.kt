package iem.bdia.polyhome.ui.devices

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.google.android.material.button.MaterialButton
import com.google.android.material.materialswitch.MaterialSwitch
import iem.bdia.polyhome.R
import iem.bdia.polyhome.domain.model.Device

class DeviceAdapter(
    private val context: Context,
    private val onCommandSend: (deviceId: String, command: String) -> Unit
) : BaseAdapter() {

    private val dataSource: ArrayList<Device> = arrayListOf()
    private val inflater: LayoutInflater = LayoutInflater.from(context)

    override fun getCount(): Int = dataSource.size
    override fun getItem(position: Int): Any = dataSource[position]
    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val rowView = convertView ?: inflater.inflate(R.layout.item_device, parent, false)
        val device = dataSource[position]

        val tvDeviceType = rowView.findViewById<TextView>(R.id.tvDeviceType)
        val tvDeviceId = rowView.findViewById<TextView>(R.id.tvDeviceId)
        val tvDeviceStatus = rowView.findViewById<TextView>(R.id.tvDeviceStatus)
        val switchAction = rowView.findViewById<MaterialSwitch>(R.id.switchAction)
        val btn3 = rowView.findViewById<MaterialButton>(R.id.btnAction3)


        tvDeviceType.text = when (device.type) {
            "light" -> "Lumière"
            "rolling shutter" -> "Volet"
            "garage door" -> "Garage"
            else -> device.type
        }

        tvDeviceId.text = "ID: ${device.id}"

        val statusText = when {
            device.opening != null -> {
                when (val percent = device.opening) {
                    0 -> "Fermé"
                    1 -> "Ouvert"
                    else -> "Ouverture: $percent"
                }
            }

            device.power != null -> {
                when (device.power) {
                    1 -> "Allumé"
                    else -> "Éteint"
                }
            }

            else -> "N/A"
        }
        tvDeviceStatus.text = statusText

        switchAction.visibility = View.GONE
        btn3.visibility = View.GONE

        switchAction.setOnCheckedChangeListener(null)
        val isCurrentlyOn = (device.power == 1) || (device.opening == 1)
        switchAction.isChecked = isCurrentlyOn

        device.availableCommands.forEach { command ->
            when (command) {
                "TURN ON", "OPEN", "TURN OFF", "CLOSE" -> {
                    switchAction.visibility = View.VISIBLE
                    switchAction.setOnCheckedChangeListener { _, isChecked ->
                        if (isChecked) {
                            val onCmd = if (device.type == "light") "TURN ON" else "OPEN"
                            onCommandSend(device.id, onCmd)
                        } else {
                            val offCmd = if (device.type == "light") "TURN OFF" else "CLOSE"
                            onCommandSend(device.id, offCmd)
                        }
                    }
                }

                "STOP" -> {
                    btn3.visibility = View.VISIBLE
                    btn3.text = "STOP"
                    btn3.isEnabled = true
                    btn3.alpha = 1.0f
                    btn3.setOnClickListener {
                        onCommandSend(device.id, command)
                    }
                }
            }
        }

        return rowView
    }

    fun updateDevices(newDevices: List<Device>) {
        dataSource.clear()
        dataSource.addAll(newDevices)
        notifyDataSetChanged()
    }
}

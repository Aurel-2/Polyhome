package iem.bdia.polyhome.ui.home

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.google.android.material.button.MaterialButton
import iem.bdia.polyhome.R
import iem.bdia.polyhome.domain.model.House

class HouseAdapter(
    private val context: Context,
    private val onDevicesClick: (House) -> Unit,
    private val onUsersClick: (House) -> Unit
) : BaseAdapter() {

    private val dataSource: ArrayList<House> = arrayListOf()
    private val inflater: LayoutInflater = LayoutInflater.from(context)
    fun submitList(newHouses: List<House>) {
        dataSource.clear()
        dataSource.addAll(newHouses)
        notifyDataSetChanged()
    }

    override fun getCount(): Int = dataSource.size

    override fun getItem(position: Int): Any = dataSource[position]

    override fun getItemId(position: Int): Long = dataSource[position].houseId.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val rowView = convertView ?: inflater.inflate(R.layout.item_house, parent, false)

        val house = dataSource[position]

        val tvHouseId: TextView = rowView.findViewById(R.id.tvHouseId)
        val tvOwnerStatus: TextView = rowView.findViewById(R.id.tvOwnerStatus)
        val btnViewDevices: MaterialButton = rowView.findViewById(R.id.btnViewDevices)

        tvHouseId.text = "Maison #${house.houseId}"
        tvOwnerStatus.text = if (house.owner) "Propriétaire" else "Invité"

        btnViewDevices.setOnClickListener { onDevicesClick(house) }

        return rowView
    }
}
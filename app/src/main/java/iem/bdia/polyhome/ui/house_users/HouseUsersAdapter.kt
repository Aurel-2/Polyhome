package iem.bdia.polyhome.ui.house_users

import android.content.Context
import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import androidx.core.graphics.toColorInt
import com.google.android.material.button.MaterialButton
import iem.bdia.polyhome.R
import iem.bdia.polyhome.domain.model.UnifiedUser

class HouseUsersAdapter(
    private val context: Context,
    private val onActionClick: (UnifiedUser) -> Unit
) : BaseAdapter() {

    private val dataSource: ArrayList<UnifiedUser> = arrayListOf()
    private val inflater = LayoutInflater.from(context)

    fun submitList(newList: List<UnifiedUser>) {
        dataSource.clear()
        dataSource.addAll(newList)
        notifyDataSetChanged()
    }

    override fun getCount(): Int = dataSource.size
    override fun getItem(position: Int): UnifiedUser = dataSource[position]
    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val rowView = convertView ?: inflater.inflate(R.layout.item_unified_user, parent, false)
        val item = dataSource[position]

        val tvUserLogin: TextView = rowView.findViewById(R.id.tvUserLogin)
        val tvRole: TextView = rowView.findViewById(R.id.tvRole)
        val btnAction: MaterialButton = rowView.findViewById(R.id.btnAction)

        tvUserLogin.text = item.login

        if (item.isMember) {
            tvRole.text = if (item.isOwner) "Propriétaire" else "Membre"
            btnAction.text = "Retirer"
            btnAction.backgroundTintList = ColorStateList.valueOf("#F44336".toColorInt())
            btnAction.visibility = if (item.isOwner) View.GONE else View.VISIBLE
        } else {
            tvRole.text = "Utilisateur"
            btnAction.text = "Accès"
            btnAction.backgroundTintList = ColorStateList.valueOf("#29B6F6".toColorInt())
            btnAction.visibility = View.VISIBLE
        }

        btnAction.setOnClickListener { onActionClick(item) }

        return rowView
    }
}
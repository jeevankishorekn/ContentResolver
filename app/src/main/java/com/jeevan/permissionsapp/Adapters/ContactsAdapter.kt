package com.jeevan.permissionsapp.Adapters

import android.content.Context
import android.view.*
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.jeevan.permissionsapp.ContactItem
import com.jeevan.permissionsapp.R

class ContactsAdapter(private val data1: List<ContactItem>, private val context: Context) :
    RecyclerView.Adapter<ContactsAdapter.ContactViewHolder>() {

    var position: Int = -1
    private var data = data1

    inner class ContactViewHolder(view: View) : RecyclerView.ViewHolder(view),
        View.OnCreateContextMenuListener {
        var contactName: TextView
        var contactNumber: TextView
        var contactCard: CardView

        init {
            contactName = view.findViewById(R.id.contactName)
            contactNumber = view.findViewById(R.id.contactNumber)
            contactCard = view.findViewById(R.id.contactItemCard)
            contactCard.setOnCreateContextMenuListener(this)
        }

        override fun onCreateContextMenu(
            menu: ContextMenu?,
            v: View?,
            menuInfo: ContextMenu.ContextMenuInfo?
        ) {
            MenuInflater(context).inflate(R.menu.context_menu, menu)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactViewHolder {
        return ContactViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.contact_item, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ContactViewHolder, position: Int) {
        holder.contactName.text = data[position].name
        holder.contactNumber.text = data[position].contactNumber
        holder.contactCard.setOnLongClickListener {
            this.position = position
            return@setOnLongClickListener false
        }
    }

    fun updateList(dataItem: List<ContactItem>) {
        data = dataItem
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = data.size
}
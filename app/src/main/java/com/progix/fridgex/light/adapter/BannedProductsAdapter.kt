package com.progix.fridgex.light.adapter

import android.content.Context
import android.database.Cursor
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils.loadAnimation
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.progix.fridgex.light.MainActivity
import com.progix.fridgex.light.MainActivity.Companion.imagesCat
import com.progix.fridgex.light.MainActivity.Companion.isMultiSelectOn
import com.progix.fridgex.light.MainActivity.Companion.mDb
import com.progix.fridgex.light.R
import com.progix.fridgex.light.custom.CustomSnackbar
import com.progix.fridgex.light.helper.ActionInterface


class BannedProductsAdapter(
    var context: Context,
    var bannedProductsList: ArrayList<Pair<String, String>>
) :
    RecyclerView.Adapter<BannedProductsAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View =
            LayoutInflater.from(parent.context).inflate(R.layout.item_product, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val name = bannedProductsList[position].first
        holder.star.visibility = View.GONE

        holder.name.text = name.replaceFirstChar(Char::uppercase)
        val cursor2: Cursor = mDb.rawQuery(
            "SELECT * FROM categories WHERE category = ?",
            listOf(bannedProductsList[position].second).toTypedArray()
        )
        cursor2.moveToFirst()
        holder.image.setImageResource(imagesCat[cursor2.getInt(0) - 1])

        val cursor: Cursor =
            mDb.rawQuery("SELECT * FROM products WHERE product = ?", listOf(name).toTypedArray())
        cursor.moveToFirst()

        holder.bind(cursor.getInt(0), position)
        cursor.close()
        cursor2.close()
        setAnimation(holder.itemView, position)

        (holder.itemView as MaterialCardView).isChecked = selectedIds.contains(name)
    }


    private fun popupMenus(
        view: View,
        id: Int,
        position: Int
    ) {
        val popupMenus = PopupMenu(context, view)
        inflatePopup(popupMenus)
        popupMenus.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.clear -> {
                    val tempValue = bannedProductsList[position]
                    mDb.execSQL(
                        "UPDATE products SET banned = 0 WHERE product = ?",
                        listOf(tempValue.first).toTypedArray()
                    )
                    bannedProductsList.remove(tempValue)
                    notifyItemRemoved(position)
                    CustomSnackbar(context)
                        .create(
                            55,
                            (context as MainActivity).findViewById(R.id.main_root),
                            context.getString(R.string.deletedFromBanned)
                        )
                        .setAction(context.getString(R.string.undo)) {
                            mDb.execSQL(
                                "UPDATE products SET banned = 1 WHERE product = ?",
                                listOf(tempValue.first).toTypedArray()
                            )
                            bannedProductsList.add(position, tempValue)
                            notifyItemInserted(position)
                        }
                        .show()
                    true
                }

                else -> true
            }

        }
        popupMenus.show()
        val popup = PopupMenu::class.java.getDeclaredField("mPopup")
        popup.isAccessible = true
        val menu = popup.get(popupMenus)
        menu.javaClass.getDeclaredMethod("setForceShowIcon", Boolean::class.java)
            .invoke(menu, true)
    }

    private fun inflatePopup(
        popupMenus: PopupMenu
    ) {
        popupMenus.menu.add(0, R.id.clear, 0, context.getString(R.string.deleteWord))
            ?.setIcon(R.drawable.ic_baseline_delete_24)
    }


    override fun getItemCount(): Int {
        return bannedProductsList.size
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.name)
        val image: ImageView = view.findViewById(R.id.image)
        val star: ImageView = view.findViewById(R.id.star)
        fun clearAnimation() {
            itemView.clearAnimation()
        }

        fun bind(
            id: Int,
            position: Int
        ) {
            itemView.setOnLongClickListener {
                if (!isMultiSelectOn) {
                    isMultiSelectOn = true
                    addIDIntoSelectedIds(position)
                }
                true
            }
            itemView.setOnClickListener {
                if (isMultiSelectOn) addIDIntoSelectedIds(position)
                else popupMenus(it, id, position)
            }
        }

    }

    private var actionInterface: ActionInterface? = null

    fun init(actionInterface: ActionInterface) {
        this.actionInterface = actionInterface
    }

    fun addIDIntoSelectedIds(position: Int) {
        val id = bannedProductsList[position].first
        if (selectedIds.contains(id)) {
            selectedIds.remove(id)
            selectedPositions.remove(position)
        } else {
            selectedIds.add(id)
            selectedPositions.add(position)
        }
        notifyItemChanged(position)
        if (selectedIds.size < 1) isMultiSelectOn = false
        actionInterface?.actionInterface(selectedIds.size)
    }

    val selectedIds: ArrayList<String> = ArrayList()

    var tempList: ArrayList<String>? = null

    var tempPositions: ArrayList<Int>? = null

    val selectedPositions: ArrayList<Int> = ArrayList()

    fun doSomeAction(modifier: String) {
        if (selectedIds.size < 1) return
        when (modifier) {
            "delete" -> {
                val delList: ArrayList<Pair<String, String>> = ArrayList()
                val indexes: ArrayList<Int> = ArrayList()
                for (i in tempPositions!!) {
                    delList.add(bannedProductsList[i])
                }
                for (i in 0 until tempList!!.size) {
                    val temp = tempList!![i]
                    mDb.execSQL(
                        "UPDATE products SET banned = 0 WHERE product = ?",
                        listOf(temp).toTypedArray()
                    )
                    val tempPos = bannedProductsList.indexOf(delList[i])
                    indexes.add(tempPos)
                    bannedProductsList.remove(delList[i])
                    notifyItemRemoved(tempPos)
                }
                CustomSnackbar(context)
                    .create(
                        55,
                        (context as MainActivity).findViewById(R.id.main_root),
                        context.getString(R.string.deletedFromBanned)
                    )
                    .setAction(context.getString(R.string.undo)) {
                        for (i in 0 until tempList!!.size) {
                            val temp = tempList!![i]
                            mDb.execSQL(
                                "UPDATE products SET banned = 1 WHERE product = ?",
                                listOf(temp).toTypedArray()
                            )
                            if (indexes[i] < bannedProductsList.size) bannedProductsList.add(
                                indexes[i],
                                delList[i]
                            )
                            else bannedProductsList.add(delList[i])
                        }
                        notifyDataSetChanged()
                    }
                    .show()
            }
        }
        isMultiSelectOn = false
    }

    private var lastPosition = -1
    private fun setAnimation(viewToAnimate: View, position: Int) {
        val animation: Animation =
            loadAnimation(context, R.anim.enter_fade_through)
        viewToAnimate.startAnimation(animation)
        lastPosition = position
    }

    override fun onFailedToRecycleView(holder: ViewHolder): Boolean {
        return true
    }

    override fun onViewDetachedFromWindow(holder: ViewHolder) {
        holder.clearAnimation()
    }


}
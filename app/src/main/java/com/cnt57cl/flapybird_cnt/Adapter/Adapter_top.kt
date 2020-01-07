package com.cnt57cl.flapybird_cnt.Adapter

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import com.cnt57cl.flapybird_cnt.R
import com.cnt57cl.flapybird_cnt.user
import com.facebook.internal.ImageRequest
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView

class Adapter_top (context: Context,arr_user:ArrayList<user>):BaseAdapter() {

    var context:Context?=null
    var inflater:LayoutInflater?=null
    var arr_user:ArrayList<user>?=null
    init
    {
        this.context=context
        this.arr_user=arr_user
        inflater= LayoutInflater.from(this.context)
    }

    @SuppressLint("ViewHolder", "SetTextI18n")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val font:Typeface = Typeface.createFromAsset(context!!.assets,"fonts/DancingScript-Bold.ttf")
           var v: View?=null
        v= this.inflater?.inflate(R.layout.row_rank,null,false)
        val img_avatar:CircleImageView=v!!.findViewById(R.id.img_avatar)
        val img_cup:ImageView=v.findViewById(R.id.img_cup)
        val tv_name:TextView =v.findViewById(R.id.tv_name)
        val tv_winnumber:TextView=v.findViewById(R.id.tv_winnumber)
        val u= arr_user?.get(position)
        val uri= ImageRequest.getProfilePictureUri(u?.id.toString(),120,120)
        Picasso.get().load(uri).error(R.drawable.ic_launcher_background).into(img_avatar)
        when(position)
        {
            0->
            {
                img_cup.setImageResource(R.drawable.gold)
            }
            1->{
                img_cup.setImageResource(R.drawable.bac)
            }
            2->{
                img_cup.setImageResource(R.drawable.dong)
            }
        }
        tv_name.typeface=font
        tv_winnumber.typeface=font
        tv_name.text="Name : ${u?.name}"
        tv_winnumber.text="Win : ${u?.winnumber}"
        return v

    }

    override fun getItem(position: Int): Any {

            return arr_user!![position]
    }

    override fun getItemId(position: Int): Long {
            return position.toLong()

    }

    override fun getCount(): Int {
        return arr_user!!.size

    }

}
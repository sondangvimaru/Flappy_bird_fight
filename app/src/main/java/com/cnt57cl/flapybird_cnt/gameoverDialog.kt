package com.cnt57cl.flapybird_cnt

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import android.view.Window

class gameoverDialog(context: Context,view: View) : Dialog(context) {


    var conn:Context?=null
    var view:View?=null
    init {



        this.conn=context
        this.view=view
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)



        requestWindowFeature(Window.FEATURE_NO_TITLE)

        window?.decorView?.background= ColorDrawable(Color.TRANSPARENT)

        setContentView(this.view!!)
    }



}
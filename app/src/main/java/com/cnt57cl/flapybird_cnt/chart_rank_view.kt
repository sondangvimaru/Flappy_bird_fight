package com.cnt57cl.flapybird_cnt

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.cnt57cl.flapybird_cnt.Adapter.Adapter_top
import com.kaopiz.kprogresshud.KProgressHUD
import kotlinx.android.synthetic.main.activity_chart_rank_view.*
import org.json.JSONArray
import org.json.JSONObject

class chart_rank_view : AppCompatActivity() {

    var ds_user:ArrayList<user>?=null
    var dialog:KProgressHUD?=null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chart_rank_view)

        init()
    }

    private fun init() {
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        window.decorView.systemUiVisibility= View.SYSTEM_UI_FLAG_FULLSCREEN
        window.statusBarColor= Color.TRANSPARENT
        ds_user= ArrayList()
        setdata()
    }

    private fun setdata() {

         dialog= KProgressHUD.create(this)
            .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
            .setLabel("Đang tải dữ liệu")
            .setCancellable(false)
            .setAnimationSpeed(2)
            .setDimAmount(0.5f)
            .show()
       val listenr: Response.Listener<JSONObject> = Response.Listener {
           response ->

           val arr:JSONArray=response.getJSONArray("data")
           for(i in 0.until(arr.length()))
           {
               val json:JSONObject = arr.getJSONObject(i)
               ds_user?.add(user(json.getLong("id"),json.getString("name"),json.getString("gioitinh"),json.getLong("winnumber")))

           }

           val a= Adapter_top(this,ds_user!!)
           list_rank.adapter=a
       }
        val request=object :JsonObjectRequest(Request.Method.GET,"https://serverflappybrid.000webhostapp.com/gettop.php",listenr, Response.ErrorListener {


        })
        {
            override fun onFinish() {
                dialog!!.dismiss()
                super.onFinish()
            }
        }



        val quence= Volley.newRequestQueue(this)
        quence.cache.clear()
        quence.add(request)
    }


}

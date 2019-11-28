package com.cnt57cl.flapybird_cnt

import android.graphics.Color
import android.icu.text.DecimalFormat
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.facebook.Profile
import com.facebook.internal.ImageRequest
import com.squareup.picasso.Picasso
import io.socket.emitter.Emitter
import kotlinx.android.synthetic.main.activity_gameplaying.*
import org.json.JSONException
import org.json.JSONObject
import pl.droidsonroids.gif.GifImageView
import java.util.concurrent.TimeUnit

class Gameplaying : AppCompatActivity(){


    var thread:Thread?=null
    var room:Long=0
    var id:Long?=null
    var my_point=0
    var result:String?=" "
    var enemy_point=0
    var id_enemy:Long=0
    var profilePictureUri: Uri?=null
    var profilePictureUri2: Uri?=null
    var countDownTimer:CountDownTimer?=null
    var time:Long=300000
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gameplaying)


       init()
        start_time()
        message.setOnClickListener(View.OnClickListener {
            message.visibility=View.GONE
            frame_fight.visibility=View.VISIBLE
            gameview_play.visibility=View.VISIBLE
        })


    }

    fun  start_time()
    {
        countDownTimer= object :CountDownTimer(time,1000)
        {

            @RequiresApi(Build.VERSION_CODES.N)
            override fun onTick(millisUntilFinished: Long) {
                time-=1000
                val phut= TimeUnit.MILLISECONDS.toMinutes(time)
                val giay= TimeUnit.MILLISECONDS.toSeconds(time-TimeUnit.MINUTES.toMillis(phut))

               val fm: DecimalFormat = DecimalFormat("00")
                tv_time.text= fm.format(phut)+":"+fm.format(giay)
        }
            override fun onFinish() {

               MainActivity.socket?.emit("time-up",gameview_play.source.toString()+" "+room.toString()+" "+enemy_point.toString()+" "+id.toString())

            }



        }


        countDownTimer!!.start()

    }


    fun setdata_for_enemy(url: String)
    {
        var u:user?=null

        val objectRequest =
            JsonObjectRequest(Request.Method.GET, url,
                Response.Listener { response ->
                    try {
                        val jsonArray = response.getJSONArray("data")
                        for (i in 0 until jsonArray.length()) {
                            val jsonObject = jsonArray.getJSONObject(i)

                            u= user(jsonObject.getString("id").toLong(),jsonObject.getString("name"),jsonObject.getString("gioitinh"),jsonObject.getString("winnumber").toLong())

                        }

                        setgender(u?.gioitinh!!.trim(),enemy_gender)
                        tv_enemy_name.text=u?.name

                        profilePictureUri2= ImageRequest.getProfilePictureUri(u?.id.toString(), 120, 120)
                        Picasso.get().load(profilePictureUri2).error(R.drawable.bird).into(img_enemy_avatar)
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }
                }, Response.ErrorListener { })

        val requestQueue = Volley.newRequestQueue(applicationContext)
        requestQueue.add(objectRequest)
    }
    fun setdata_for_me(url:String)
    {
        var u:user?=null

        val objectRequest =
            JsonObjectRequest(Request.Method.GET, url,
                Response.Listener { response ->
                    try {
                        val jsonArray = response.getJSONArray("data")
                        for (i in 0 until jsonArray.length()) {
                            val jsonObject = jsonArray.getJSONObject(i)

                            u= user(jsonObject.getString("id").toLong(),jsonObject.getString("name"),jsonObject.getString("gioitinh"),jsonObject.getString("winnumber").toLong())

                        }

                        setgender(u?.gioitinh!!.trim(),my_gender)
                        tv_my_name.text=u?.name

                        profilePictureUri= ImageRequest.getProfilePictureUri(u?.id.toString(), 120, 120)
                        Picasso.get().load(profilePictureUri).error(R.drawable.bird).into(img_myavatar)
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }
                }, Response.ErrorListener { })

        val requestQueue = Volley.newRequestQueue(applicationContext)
        requestQueue.add(objectRequest)
    }
    fun  setgender(gender:String,img:ImageView)
    {
        when(gender)
        {
            "male"->
            {

                img.setImageResource(R.drawable.male)
            }
            "female"->
            {

                img.setImageResource(R.drawable.female)
            }

        }

    }

    var Server_send_point= Emitter.Listener { args ->
    runOnUiThread {


        val jsonObject = args[0] as JSONObject
       enemy_point=jsonObject.getInt("source")
        Log.d("doi thu:",enemy_point.toString())

            tv_enemy_source.text=enemy_point.toString()



    }

    }

    var Server_send_time_up= Emitter.Listener { args ->
        runOnUiThread {


            val jsonObject = args[0] as JSONObject
            val result_time_up:String=jsonObject.getString("timeup")


            try {


                val over_dialog: gameoverDialog = gameoverDialog(this,getview(result_time_up))

                over_dialog.setCancelable(false)

                over_dialog.show()


            }catch (e:java.lang.Exception)
            {
                Log.d("loi_dialog",e.message.toString())
            }



        }

    }


    var Server_send_game_over= Emitter.Listener { args ->
        runOnUiThread {


            val jsonObject = args[0] as JSONObject
            result=jsonObject.getString("result")

            if(gameview_play.handel!=null)
            gameview_play.handel?.removeCallbacks(gameview_play.runnable!!)

            try {


                    val over_dialog: gameoverDialog = gameoverDialog(this,getview(result!!))

                    over_dialog.setCancelable(false)

                    over_dialog.show()


            }catch (e:java.lang.Exception)
            {
                Log.d("loi_dialog",e.message.toString())
            }
        }

    }
    fun getview(result: String):View
    {
        val v:View= LayoutInflater.from(this).inflate(R.layout.game_view_over,null,false)

        val img_over= v.findViewById<ImageView>(R.id.img_over)
        val img_cup= v.findViewById<GifImageView>(R.id.img_cup)
        val img_home= v.findViewById<ImageView>(R.id.btn_gotohome)


        when(result)
        {
            "win"->
            {
                img_over.setImageResource(R.drawable.yw)
                img_cup.setImageResource(R.drawable.wincup)


            }
            "lose"->
            {

                img_over.setImageResource(R.drawable.youlose)
                img_cup.setImageResource(R.drawable.lose)
            }
            "mid"->
            {
                img_over.setImageResource(R.drawable.tied)
                img_cup.setImageResource(R.drawable.wincup)

            }

        }
        img_home.setImageResource(R.drawable.home)

        img_home.setOnClickListener(View.OnClickListener {


           finish()

        })
        return v
    }
    fun init()
    {




            MainActivity.socket?.on("server-send-point",Server_send_point)
            MainActivity.socket?.on("server-send-result",Server_send_game_over)
            MainActivity.socket?.on("server-send-time-up",Server_send_time_up)
        val bd= intent.extras
        if(bd!=null)
        {
            room= bd.getLong("id_room")
            id=bd.getLong("id-user")
            id_enemy =bd.getLong("id-enemy")


        }
        if(id!=null)
        setdata_for_me("https://serverflappybrid.000webhostapp.com/getuser.php?id=${id}")
        else   setdata_for_me("https://serverflappybrid.000webhostapp.com/getuser.php?id=${Profile.getCurrentProfile().id}")
        setdata_for_enemy("https://serverflappybrid.000webhostapp.com/getuser.php?id=${(id_enemy)}")
        supportActionBar?.hide()
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )



        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                )
        window.statusBarColor = Color.TRANSPARENT
    startthread()
    }
    fun startthread()
    {
        thread= Thread(Runnable {

            while (true)
            {
                if(gameview_play.gameover)
                {
                    MainActivity.socket!!.emit("game-over",room.toString() +" "+id.toString())

                    break
                }else
                {

                    runOnUiThread {

                        tv_my_source.text = gameview_play.source.toString()
                     }


                    Thread(Runnable {




                        MainActivity.socket!!.emit("client-send-point",room.toString()+" "+gameview_play.source+" "+id.toString())


                    }).start()



                }
                Thread.sleep(1000)
            }

        })
        try {
            thread!!.start()
        }catch (e:Exception)
        {
            e.printStackTrace()

        }



    }

}

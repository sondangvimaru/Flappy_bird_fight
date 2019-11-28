package com.cnt57cl.flapybird_cnt

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.AuthFailureError
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.facebook.*
import com.facebook.Profile.getCurrentProfile
import com.facebook.internal.ImageRequest
import com.facebook.login.LoginResult
import io.socket.client.IO
import io.socket.client.Socket
import io.socket.emitter.Emitter
import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONException
import org.json.JSONObject
import java.util.*
import kotlin.random.Random


class MainActivity : AppCompatActivity(){


    companion object
    {
        var socket:Socket?=null

    }
    var room:Long?=null
    var seachingDialog:seaching_dialog?=null
    var id:Long?=null
    var profilePictureUri:Uri?=null
    var callbackManager: CallbackManager?=null
    var profileTracker:ProfileTracker?=null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        startLightsAnimation()
        FacebookSdk.fullyInitialize()
       FacebookSdk.setAutoLogAppEventsEnabled(true)

        FacebookSdk.isInitialized()


        init_socket()
        init()

    }

    override fun onRestart() {
        super.onRestart()

    }

    override fun onResume() {


        super.onResume()
    }
    var Server_send_room = Emitter.Listener { args ->
        runOnUiThread {
            val jsonObject = args[0] as JSONObject


            try {

                if(seachingDialog!!.isShowing)
                {
                    seachingDialog!!.dismiss()
                    seachingDialog= seaching_dialog(this,getview())
                    seachingDialog!!.setCancelable(false)
                }
                val arr_str=jsonObject.getString("room")
                val arr= arr_str.split(" ")

               room = arr[2].trim().toLong()
                Log.d("room_play:",room.toString())
                val intent = Intent(application,Gameplaying::class.java)
                intent.putExtra("id_room",room!!)
                intent.putExtra("id-user",id)
                intent.putExtra("id-enemy",arr[0].toLong())
                startActivity(intent)
            } catch (e: JSONException) {
                e.printStackTrace()
            }
        }
    }
    fun init_socket()
    {
        try
        {
            socket= IO.socket("http://192.168.1.7:3000/")

            socket!!.connect()

            socket!!.on("server-send-room",Server_send_room)


        }catch (e:Exception)
        {
            e.printStackTrace()
        }

    }
    fun isLoggedIn(): Boolean {
        val accessToken = AccessToken.getCurrentAccessToken()
        return accessToken != null
    }
    fun init()
    {

        seachingDialog= seaching_dialog(this,getview())
        seachingDialog!!.setCancelable(false)
        callbackManager = CallbackManager.Factory.create()
        supportActionBar?.hide()
        window.setFlags(
            WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION,
            WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION
        )


        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
               )
        window.statusBarColor = Color.TRANSPARENT



        login_button.setLoginText(resources.getString(R.string.login_text))
        login_button.setLogoutText(resources.getString(R.string.logout_text))








        login_button.setReadPermissions(Arrays.asList("public_profile", "email","user_gender"))

        try {
            id=getCurrentProfile().id.toLong()
            socket!!.emit(
                "user-online",
                id.toString()+ " anh${Random.nextInt(
                    100,
                    500
                )}" + " Nam"
            )
        }catch (e:Exception)
        {
            Log.d("null","nullcc")

        }


        loginfacebook()

    }
fun getview():View
{
    val view:View= LayoutInflater.from(this).inflate(R.layout.searching_game_view,null,false)
    val btn_cancel= view.findViewById<Button>(R.id.btn_cancel)
    btn_cancel.setOnClickListener(View.OnClickListener {


        if(seachingDialog!=null&& seachingDialog!!.isShowing)
        {
            seachingDialog?.dismiss()
        }
        socket?.emit("huy-tim-tran",id.toString())


    })
    return view
}
    fun  loginfacebook()
    {
        login_button.registerCallback(callbackManager, object : FacebookCallback<LoginResult> {








            override fun onSuccess(loginResult: LoginResult) {


                profileTracker = object :ProfileTracker()
                {
                    override fun onCurrentProfileChanged(
                        oldProfile: Profile?,
                        currentProfile: Profile?
                    ) {
                        this.stopTracking()
                        Profile.setCurrentProfile(currentProfile)
                    }

                }

                (profileTracker as ProfileTracker).startTracking()
                Log.d("datafb","data")
                try {
        val     request = GraphRequest.newMeRequest(
                    loginResult.accessToken,
                     object : GraphRequest.GraphJSONObjectCallback
                     {
                         override fun onCompleted(data: JSONObject?, response: GraphResponse?) {

                             start_game_button.isEnabled=true
                            id=data!!.getString("id").toLong()
                             socket!!.emit(
                                 "user-online",
                                 id.toString()+ " anh${Random.nextInt(
                                     100,
                                     500
                                 )}" + " Nam"
                             )
                            try
                            {
                           Log.d("inform",data.toString())
                                Log.d("inform",response?.jsonObject.toString())
                                var gender:String
                              gender=data.getString("gender")
                                insert_user_to_database(data.getString("id").toLong(),data.getString("name"),this@MainActivity,gender,0)

                            }catch (e:java.lang.Exception)
                            {

                                    Log.d("loi_insert",e.message.toString())
                            }


                         }

                     })

           val parameters =  Bundle()
            parameters.putString("fields","id,name,email,gender")
            request.parameters=parameters
            request.executeAsync()

                }catch (e: Exception)
                {

                    e.printStackTrace()
                }

            }

            override fun onCancel() {


            }

            override fun onError(error: FacebookException) {

                Log.d("errofb",error.message)
            }
        })
    }
    private fun startLightsAnimation() {
        val animator =
            ObjectAnimator.ofFloat(start_game_button_lights, "rotation", 0f, 360f)
        animator.interpolator = AccelerateDecelerateInterpolator()
        animator.duration = 6000
        animator.repeatCount = ValueAnimator.INFINITE
        start_game_button_lights.setLayerType(View.LAYER_TYPE_HARDWARE, null)
        animator.start()


    }
    fun insert_user_to_database(
        id: Long,
        user_name: String,
        context: Context?,
        gioitinh: String?,
        winnumber: Int
    ) {



        val luu: StringRequest = object : StringRequest(
            Method.POST,
            "https://serverflappybrid.000webhostapp.com/themuser.php",
            Response.Listener { },
            Response.ErrorListener { error -> Log.d("sondk", error.message) }
        ) {
            override fun getParams(): Map<String, String> {
                val list : HashMap<String, String>
                        = HashMap()
                    list.put("id",id.toString())
                    list.put("name",user_name)
                if(gioitinh==null || TextUtils.isEmpty(gioitinh))
                    list.put("gioitinh","male")
                else
                    list.put("gioitinh",gioitinh)
                    list.put("winnumber",winnumber.toString())
                return list
            }

            @Throws(AuthFailureError::class)
            override fun getHeaders(): Map<String, String> {
                return super.getHeaders()
            }
        }


        val requestQueue: RequestQueue = Volley.newRequestQueue(context)
        requestQueue.getCache().clear()
        requestQueue.add(luu)
    }
    private fun get_notifil() {
        val graphRequest = GraphRequest.newMeRequest(
            AccessToken.getCurrentAccessToken()
        ) { `object`, response ->
            try {
                if (response.jsonObject != null) {
                    val name = response.jsonObject.getString("name")
                 Log.d("login:",name)

                  var  img = response.jsonObject.getString("id")
                                        if (img == null) {
                        img = getCurrentProfile().id

                   profilePictureUri=  ImageRequest.getProfilePictureUri(img, 120, 120)
                    }
                }

                else if(`object`!=null)
                {

                    Log.d("gender",`object`.getString("gender"))
                }
                else {
                    Log.d("null","null")
                   val name = getCurrentProfile().name
//                    Log.d("login",name)
                    var img = getCurrentProfile().id
                    if (img == null) img = getCurrentProfile().id
                    profilePictureUri = ImageRequest.getProfilePictureUri(img, 120, 120)
                }
//                Picasso.get().load(profilePictureUri)
//                    .error(R.drawable.ic_launcher_background).into(image_avatar)
//
//                try {
//                    Handler(Looper.getMainLooper())
//                        .post {
//                            insert_user_to_database(
//                                img,
//                                name,
//                                this@MainActivity,
//                                profilePictureUri.toString(),
//                                0
//                            )
//                        }
//                } catch (e: java.lang.Exception) {
//                    Log.d("sondk", e.message)
//                }

            } catch (e: java.lang.Exception) {
                Log.d("sondk", e.message)
            }


        }
        val parameters = Bundle()
        parameters.putString(
            "fields",
            "picture.type(large),name,email,first_name,friendlist,members,gender"
        )
        graphRequest.parameters = parameters
        graphRequest.executeAsync()
    }
    fun startgame(view: View?) {


        try {

            if(!seachingDialog!!.isShowing)
            {
                seachingDialog?.show()
            }
            socket!!.emit("tim-tran",id.toString())

        }catch (e: Exception)
        {

        }


    }
    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)
        callbackManager!!.onActivityResult(requestCode, resultCode, data)
    }


    override fun onBackPressed() {

        socket?.emit("user-off",id.toString())
        super.onBackPressed()
    }

}

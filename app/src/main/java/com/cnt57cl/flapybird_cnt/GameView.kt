package com.cnt57cl.flapybird_cnt


import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.media.MediaPlayer
import android.os.Handler
import android.util.AttributeSet
import android.view.Display
import android.view.MotionEvent
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import kotlin.random.Random

class GameView(context: Context, att: AttributeSet?):View(context,att) {

    var handel:Handler?=null
    val update:Int=30
    var bm:Bitmap?=null
    var runnable:Runnable?=null
    var dis:Display?=null
    var point:Point?=null
    var dwith=0
    var dheight=0
    var rect:Rect?=null
    var brid:ArrayList<Bitmap>?=null
    var bird_frame=0
    var v=0
    var vitri=3
    var birdx=0
    var birdy=0
    var backgroudx=0
    var backgroudy=0
    var rectbgr:Rect?=null
    var ground:Bitmap?=null
    var pipe_upx=0
    var pipe_upheight=0
    var pipe_downx=0
    var pipe_downheight=0
    var pipe_up2:Bitmap?=null
    var pipe_down2:Bitmap?=null
    var pipe_up:Bitmap?=null
    var pipe_down:Bitmap?=null
    var check=false
    var check2=false
    var touch_music:MediaPlayer?=null
    var die_music:MediaPlayer?=null
    var pipe_up2_x=0
    var pipe_up2_height=0
    var pipe_down2_x=0
    var pipe_down2_height=0
    var pipe_up3:Bitmap?=null
    var pipe_down3:Bitmap?=null
    var check3=false
    var gameover=false
    var pipe_up3_x=0
    var pipe_up3_height=0
    var pipe_down3_x=0
    var pipe_down3_height=0
    var source:Int=0
    var item:Bitmap?=null
    var itemx:Int=0
    var musicpoint:MediaPlayer?=null
    var rocket_run:Bitmap?=null
    var x_rocketrun:Int=0
    var y_rocketrun:Int=0
    var showitem=false
    var itemishowing=false
    var run_rocket=false
    var my_id:Long=0
    var room_id:Long=0
    var run_rocket_count:Int=0
    var arr_bird1:IntArray= intArrayOf(R.drawable.upbrid,R.drawable.downbird)
    var arr_bird2:IntArray= intArrayOf(R.drawable.bluebirdupflap,R.drawable.bluebirddownflap)
    var arr_bird3:IntArray= intArrayOf(R.drawable.redbirdupflap,R.drawable.redbirddownflap)
    var bird_id=0
    init {

        runnable= Runnable {
           run {
                invalidate()
            }
        }

        handel=Handler()

        bm= BitmapFactory.decodeResource(resources,R.drawable.bgr_game)
        ground=BitmapFactory.decodeResource(resources,R.drawable.ground)
        pipe_up= BitmapFactory.decodeResource(resources,R.drawable.uppipe)
        pipe_down=BitmapFactory.decodeResource(resources,R.drawable.downpipe)
        item=BitmapFactory.decodeResource(resources,R.drawable.rocketicon)
        rocket_run= BitmapFactory.decodeResource(resources,R.drawable.rocket)


        pipe_up2=pipe_up
        pipe_down2=pipe_down
        pipe_up3=pipe_up
        pipe_down3=pipe_down
        dis=(context as AppCompatActivity).windowManager.defaultDisplay
        point= Point()
        dis?.getSize(point)
        dwith=point!!.x
        dheight=point!!.y
        rect= Rect(backgroudx,backgroudy,dwith,dheight)
        rectbgr=Rect((backgroudx+bm!!.width),backgroudy,dwith,dheight)
        itemx=dwith
        x_rocketrun=dwith



    }

    override fun isShown(): Boolean {

        brid= ArrayList()
        val bird1:Bitmap=getbirdup()

        val bird2:Bitmap=getbirdown()
        brid?.add(bird1)
        brid?.add(bird2)
        birdx=(dwith-brid?.get(0)!!.width)/2
        birdy=(dheight-brid?.get(0)!!.height)/2
        touch_music= MediaPlayer.create(context,R.raw.wing)
        die_music= MediaPlayer.create(context,R.raw.hit)
        musicpoint=MediaPlayer.create(context,R.raw.point)
        return super.isShown()

    }
    private fun getbirdown(): Bitmap {
        when(bird_id)
        {
            0-> return  BitmapFactory.decodeResource(resources,arr_bird1.get(1))
            1-> return  BitmapFactory.decodeResource(resources,arr_bird2.get(1))
            2-> return  BitmapFactory.decodeResource(resources,arr_bird3.get(1))
        }
        return BitmapFactory.decodeResource(resources,arr_bird1.get(1))
    }

    private fun getbirdup(): Bitmap {
        when(bird_id)
        {
            0-> return  BitmapFactory.decodeResource(resources,arr_bird1.get(0))
            1-> return  BitmapFactory.decodeResource(resources,arr_bird2.get(0))
            2-> return  BitmapFactory.decodeResource(resources,arr_bird3.get(0))
        }
        return BitmapFactory.decodeResource(resources,arr_bird1.get(0))
    }

    fun  setupdata_pipe2()
    {
        pipe_up2_x=width+width/2+pipe_up2!!.width
        pipe_up2_height=Random.nextInt(height/2+120,height-300)

        pipe_down2_x= width+width/2+pipe_up2!!.width
        pipe_down2_height= Random.nextInt(300,height/2-60)
        if(pipe_up2_height>pipe_down2_height) pipe_down2_height= Random.nextInt(pipe_up2_height/2,height/2-150)
        if(pipe_down2_height+10>=height/2) pipe_down2_height-=30
        if(pipe_up2_height-10<=height/2) pipe_down2_height+=30
        if(pipe_up3_x>pipe_upx  && (pipe_up3_x-pipe_upx+pipe_up!!.width)<=30)
        {
            pipe_up2_x+=150
            pipe_down2_x+=150
        }
        if(pipe_up3_x>pipe_up2_x && (pipe_up3_x-pipe_up2_x+pipe_up2!!.width)<=30)
        {
            pipe_up2_x+=150
            pipe_down2_x+=150
        }

    }
    fun  setupdata_pipe3()
    {
        pipe_up3_x=width*2+50
        pipe_up3_height=Random.nextInt(dheight/2+120,dheight-300)

        pipe_down3_x= width*2+50
        pipe_down3_height= Random.nextInt(300,height/2-60)
        if(pipe_up3_height>pipe_down3_height) pipe_down3_height= Random.nextInt(pipe_up3_height/2,height/2-150)
        if(pipe_down3_height+10>=height/2) pipe_down3_height-=30
        if(pipe_up2_x>pipe_upx  && (pipe_up2_x-pipe_upx+pipe_up!!.width)<=30)
        {
            pipe_up2_x+=150
            pipe_down2_x+=150
        }
        if(pipe_up2_x>pipe_up3_x && (pipe_up2_x-pipe_up3_x+pipe_up3!!.width)<=30)
        {
            pipe_up2_x+=150
            pipe_down2_x+=150
        }
    }
    fun setupdata_pipe()
    {

       pipe_upx=width-(pipe_up!!.width+10)
       pipe_upheight= Random.nextInt(height/2+80,height-300)

       pipe_downx= width-(pipe_down!!.width+10)
        pipe_downheight= Random.nextInt(300,height/2-60)
        if(pipe_upheight>pipe_downheight) pipe_downheight= Random.nextInt(pipe_upheight/2,height/2-150)
        if(pipe_downheight+10>=height/2) pipe_downheight-=30
        if(pipe_upheight-10<=height/2) pipe_downheight+=30
        if(pipe_upx>pipe_up2_x   && (pipe_upx-pipe_up2_x+pipe_up2!!.width)<=30)
        {
            pipe_upx+=150
            pipe_downx+=150
        }
        if(pipe_upx>pipe_up3_x   && (pipe_upx-pipe_up3_x+pipe_up3!!.width)<=30)
        {
            pipe_upx+=150
            pipe_downx+=150
        }
    }

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        if(isVisible&&!check3 )
        {
            check3=true
            setupdata_pipe3()
        }

        if(isVisible&&!check )
        { setupdata_pipe()
            check=true

        }
        if(isVisible&&!check2)
        {
            check2=true
            setupdata_pipe2()
        }
    if(pipe_upx+pipe_up!!.width<=0 && pipe_downx+pipe_down!!.width<=0)
    {

    check=false

    }
        if(pipe_up2_x+pipe_up2!!.width<=0 &&  pipe_down2_x+pipe_down2!!.width<=0)
        {
            check2=false

        }

        if(pipe_up3_x+pipe_up3!!.width<=0 &&  pipe_down3_x+pipe_down3!!.width<=0)
        {
           check3=false
        }
        pipe_upx-=15
        pipe_downx-=15
        pipe_up2_x-=15
        pipe_down2_x-=15
        pipe_up3_x-=15
        pipe_down3_x-=15
        rect= Rect(backgroudx,backgroudy,dwith,dheight)
        rectbgr=Rect((backgroudx+bm!!.width),backgroudy,dwith,dheight)
        if(rectbgr!!.left<=0)
        {
            backgroudx=0
            backgroudy=0
        }
        canvas?.drawBitmap(bm!!,null,rect!!,null)
        canvas?.drawBitmap(bm!!,null,rectbgr!!,null)
        backgroudx-=2
        canvas?.drawBitmap(pipe_up!!,null, Rect(pipe_upx,pipe_upheight,pipe_upx+pipe_up!!.width,height-190),null)
        canvas?.drawBitmap(pipe_down!!, null,Rect(pipe_downx,0,pipe_downx+pipe_down!!.width,pipe_downheight),Paint())
        canvas?.drawBitmap(pipe_up2!!,null, Rect(pipe_up2_x,pipe_up2_height,pipe_up2_x+pipe_up!!.width,height-190),null)
        canvas?.drawBitmap(pipe_down2!!, null,Rect(pipe_down2_x,0,pipe_down2_x+pipe_down!!.width,pipe_down2_height),Paint())
        canvas?.drawBitmap(pipe_up3!!,null, Rect(pipe_up3_x,pipe_up3_height,pipe_up3_x+pipe_up!!.width,height-190),null)
        canvas?.drawBitmap(pipe_down3!!, null,Rect(pipe_down3_x,0,pipe_down3_x+pipe_down!!.width,pipe_down3_height),Paint())
        canvas?.drawBitmap(ground!!,null,Rect(0,height-200,dwith,height),null)
        if(showitem)
        {

            if(eat_rocket())
            {

                itemishowing=false
                showitem=false
                itemx=dwith
                MainActivity.socket?.emit("eat-item","${room_id} ${my_id}")

            }else
            showitem(canvas)
        }
        if(run_rocket)
        {
            if(run_rocket_count<3)
                {

            run_rocket(canvas)
               if(rocket_die())
               {
                   handel!!.removeCallbacks(runnable!!)
                   die_music!!.start()
                   gameover=true
               }


            }
            else
            {
                run_rocket=false
                run_rocket_count=0
            }

        }


        if(bird_frame==0)
        {
            bird_frame=1
        }else bird_frame=0
        if(birdy<dheight-brid!!.get(0).height){
            v+=vitri
            birdy+=v

        }
        else
        {

            birdy= dheight-brid!!.get(0).height
        }


        canvas?.drawBitmap(brid!!.get(bird_frame),(birdx).toFloat(),(birdy).toFloat(),null)
        if(gameover())
        {

            die_music!!.start()
            gameover=true

           handel?.removeCallbacks(runnable!!)
        }
        if(point_up())
        {
            source++
            musicpoint!!.start()

        }
        handel?.postDelayed(runnable!!, update.toLong())

    }

    fun rocket_die():Boolean
    {
        if(birdx>=(x_rocketrun-100) && birdx<=(x_rocketrun+100) && birdy>=y_rocketrun&& birdy<=(y_rocketrun+100)) return  true
        return  false
    }
    fun eat_rocket():Boolean
    {
        if(birdx>= itemx && birdx<= (itemx+100) && birdy >=height/2 && birdy<=(height/2+100)) return  true
        return false

    }
    fun run_rocket(canvas: Canvas?)
    {

        canvas?.drawBitmap(rocket_run!!,null,Rect(x_rocketrun-100,y_rocketrun,x_rocketrun+100,y_rocketrun+100),null)
        x_rocketrun-=20
        y_rocketrun+=30

        if(x_rocketrun<=0 && y_rocketrun>=dheight)
        {
            run_rocket_count++
            x_rocketrun=dwith
            y_rocketrun=0
        }

    }
    fun showitem(canvas: Canvas?)
    {
        canvas?.drawBitmap(item!!,null, Rect(itemx,height/2,(itemx+100),height/2+100),null)
                itemx-=15


        if(itemx<=0) {
            itemishowing=false
            itemx=dwith
            showitem=false

        }

    }
    fun point_up():Boolean
    {

        if(!gameover && birdx>pipe_upx+pipe_up!!.width&& birdx-(pipe_upx+pipe_up!!.width)<=16
            &&birdx>pipe_downx+pipe_down!!.width&&birdx-(pipe_downx+pipe_down!!.width)<=16
            || !gameover && birdx>pipe_up2_x+pipe_up2!!.width&& birdx-(pipe_up2_x+pipe_up2!!.width)<=16
            &&birdx>pipe_down2_x+pipe_down2!!.width&&birdx-(pipe_down2_x+pipe_down2!!.width)<=16

            ||!gameover && birdx>pipe_up3_x+pipe_up3!!.width&& birdx-(pipe_up3_x+pipe_up3!!.width)<=16
            &&birdx>pipe_down3_x+pipe_down3!!.width&&birdx-(pipe_down3_x+pipe_down3!!.width)<=16
        )
            return  true

        return false



    }
    
    fun gameover():Boolean
    {
        if(birdx+brid!!.get(0).width>=pipe_upx&&birdx<=pipe_upx+pipe_up!!.width && birdy+brid!!.get(0).height>=pipe_upheight) {

        return true
        }
        else
            if(birdx +brid!!.get(0).width>=pipe_up2_x&&birdx<=pipe_up2_x+pipe_up2!!.width &&+brid!!.get(0).height>=pipe_up2_height) {

            return true
            }
            else

                if(birdx +brid!!.get(0).width>=pipe_up3_x&&birdx<=pipe_up3_x+pipe_up3!!.width && +brid!!.get(0).height>=pipe_up3_height)

                {
            return true
        }

        if(birdx+brid!!.get(0).width>=pipe_downx &&birdx<=pipe_downx+pipe_down!!.width && birdy<=pipe_downheight)
        {
            return true
        }
        else
        if(birdx+brid!!.get(0).width>=pipe_down2_x &&birdx<=pipe_down2_x+pipe_down!!.width && birdy<=pipe_down2_height)
        {
            return true
        }
        else
        if(birdx+brid!!.get(0).width>=pipe_down3_x &&birdx<=pipe_down3_x+pipe_down!!.width && birdy<=pipe_down3_height)
        {
            return true
        }

            if(birdy+brid!!.get(0).height>=height-200) return  true
         return  false



    }
    override fun onTouchEvent(event: MotionEvent?): Boolean {

        val action= event?.action
        when(action)
        {
            MotionEvent.ACTION_DOWN
                    ->
            {


                touch_music!!.start()
                v=-30
            }
        }
        return true
    }
}
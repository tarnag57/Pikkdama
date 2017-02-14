package com.tarnag.cardsdisplay1;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/**
 * Created by viktor on 2017. 02. 14..
 */

public class GamePanel extends SurfaceView implements Runnable{

    public static final int WIDTH = 1600;
    public static final int HEIGHT = 1200;

    Thread thread = null;
    boolean canDraw = false;

    Bitmap background;
    Canvas canvas;
    SurfaceHolder surfaceHolder;

    public GamePanel(Context context) {
        super(context);
        surfaceHolder = getHolder();
        background = BitmapFactory.decodeResource(getResources(), R.drawable.table_background);
    }

    @Override
    public void run() {
        while (canDraw) {

            //check whether the surface is valid
            if(!surfaceHolder.getSurface().isValid()) {
                continue;
            }

            canvas = surfaceHolder.lockCanvas();
            Bitmap scaled = Bitmap.createScaledBitmap(background, getWidth(), getHeight(), false);
            canvas.drawBitmap(scaled, 0, 0, null);
            surfaceHolder.unlockCanvasAndPost(canvas);
            Log.d("run", "finished");
            canDraw = false;
        }
    }

    public void pause() {
        canDraw = false;
        while(true) {
            try {
                thread.join();
                break;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        thread = null;
    }

    public void resume() {
        canDraw = true;
        thread = new Thread(this);
        thread.start();
    }
}

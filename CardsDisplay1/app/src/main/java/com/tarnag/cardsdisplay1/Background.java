package com.tarnag.cardsdisplay1;

import android.graphics.Bitmap;
import android.graphics.Canvas;

/**
 * Created by viktor on 2017. 02. 14..
 */

public class Background {

    private Bitmap image;

    Background(Bitmap res) {
        image = res;
    }

    public void update() {

    }

    public void draw(Canvas canvas) {
        canvas.drawBitmap(image, 0, 0, null);
    }

}

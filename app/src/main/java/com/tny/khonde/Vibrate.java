package com.tny.khonde;

import android.content.Context;
import android.os.Vibrator;

/**
 * Created by Thitiphat on 9/21/2015.
 */
public class Vibrate {
    Context context;
    public Vibrate(Context context){ this.context=context;}

    public void vibrate(){
        Vibrator v = (Vibrator) context.getSystemService(context.VIBRATOR_SERVICE);
        v.vibrate(10);
    }
}

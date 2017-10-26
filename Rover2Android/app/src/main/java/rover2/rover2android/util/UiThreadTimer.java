package rover2.rover2android.util;


import android.os.Handler;

/**
 * Created by publi on 10/21/2017.
 */

public class UiThreadTimer {

    private Runnable runnable;
    public UiThreadTimer(Runnable runnable)
    {
        this.runnable = runnable;
    }

    void onTimer(){
        this.runnable.run();
    }

    private Runnable runnableCode = null;
    private Handler handler = new Handler();

    void startDelayed(final int intervalMS, int delayMS) {
        runnableCode = new Runnable() {
            @Override
            public void run() {
                onTimer();
                handler.postDelayed(runnableCode, intervalMS);
            }
        };
        handler.postDelayed(runnableCode, delayMS);
    }

    public void start(final int intervalMS) {
        startDelayed(intervalMS, 0);
    }

    public void stop() {
        handler.removeCallbacks(runnableCode);
    }
}

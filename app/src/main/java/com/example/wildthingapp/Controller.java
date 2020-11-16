package com.example.wildthingapp;

import android.util.Log;
import android.view.InputDevice;
import android.view.MotionEvent;

public class Controller {
    static final public double JOYSTICK_THRESH = 0.1;
    float[] processJoystickInput(MotionEvent event, int historyPos) {
        InputDevice inputDevice = event.getDevice();

        // Calculate the horizontal distance to move by
        // using the input value from one of these physical controls:
        // the left control stick, hat axis, or the right control stick.
        float x = getCenteredAxis(event, inputDevice,
                MotionEvent.AXIS_Y, historyPos);
        if (x == 0) {
            x = getCenteredAxis(event, inputDevice,
                    MotionEvent.AXIS_Y, historyPos);
        }

        // Calculate the vertical distance to move by
        // using the input value from one of these physical controls:
        // the left control stick, hat switch, or the right control stick.
        float y = getCenteredAxis(event, inputDevice,
                MotionEvent.AXIS_RZ, historyPos);
        if (y == 0) {
            y = getCenteredAxis(event, inputDevice,
                    MotionEvent.AXIS_RZ, historyPos);
        }

        if(x < JOYSTICK_THRESH && x > -JOYSTICK_THRESH){
            x = 0;
        }
        if(y < JOYSTICK_THRESH && y > -JOYSTICK_THRESH){
            y = 0;
        }

        Log.d("values", "X " + x);
        Log.d("values", "Y " + y);
        return new float[]{formatControllerData(y), formatControllerData(x)};
    }
    private static float getCenteredAxis(MotionEvent event,
                                         InputDevice device, int axis, int historyPos) {
        final InputDevice.MotionRange range =
                device.getMotionRange(axis, event.getSource());

        // A joystick at rest does not always report an absolute position of
        // (0,0). Use the getFlat() method to determine the range of values
        // bounding the joystick axis center.
        if (range != null) {
            final float flat = range.getFlat();
            final float value =
                    historyPos < 0 ? event.getAxisValue(axis):
                            event.getHistoricalAxisValue(axis, historyPos);

            // Ignore axis values that are within the 'flat' region of the
            // joystick axis center.
            if (Math.abs(value) > flat) {
                return value;
            }
        }
        return 0;
    }

    private float formatControllerData(float per){
        if(per != 0) {
            float temp = (((10f * Math.abs(per)) / 5f - (1f / 5f)));
            Log.d("format", Float.toString(temp));
            if(per < 0f){
                return -temp;
            }
            return temp;
        }
        return 0f;
    }
}

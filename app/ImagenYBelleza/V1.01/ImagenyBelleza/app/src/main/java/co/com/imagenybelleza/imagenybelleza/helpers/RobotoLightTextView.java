package co.com.imagenybelleza.imagenybelleza.helpers;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * Created by Juan Camilo Villa Amaya on 1/12/2016.
 * <p>
 * Clase que crea un label personalizado con la fuente de Google Roboto Light
 * Los archivos de fuentes .ttf estan en la carpeta assets/fonts del proyecto,
 * desde ahi se referencian
 */

public class RobotoLightTextView extends TextView {

    public RobotoLightTextView(Context context) {
        super(context);
        Typeface face = Typeface.createFromAsset(context.getAssets(), "fonts/Roboto-Light.ttf");
        this.setTypeface(face);
    }

    public RobotoLightTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        Typeface face = Typeface.createFromAsset(context.getAssets(), "fonts/Roboto-Light.ttf");
        this.setTypeface(face);
    }

    public RobotoLightTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        Typeface face = Typeface.createFromAsset(context.getAssets(), "fonts/Roboto-Light.ttf");
        this.setTypeface(face);
    }

    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
    }

}

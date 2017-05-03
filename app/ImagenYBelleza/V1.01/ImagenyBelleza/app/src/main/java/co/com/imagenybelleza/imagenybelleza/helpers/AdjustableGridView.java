package co.com.imagenybelleza.imagenybelleza.helpers;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.GridView;

/**
 * Created by Juan Camilo Villa Amaya on 6/12/2016.
 * <p>
 * GridView que se ajusta al contenido de la pantalla, se llama desde los archivos de vista XML
 * y desde los archivos .java de las actividades
 */

public class AdjustableGridView extends GridView {

    public AdjustableGridView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AdjustableGridView(Context context) {
        super(context);
    }

    public AdjustableGridView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int expandSpec = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2,
                MeasureSpec.AT_MOST);
        super.onMeasure(widthMeasureSpec, expandSpec);
    }
}

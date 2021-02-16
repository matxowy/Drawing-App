package com.example.drawingappp

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_main.view.*

class DrawingView(context: Context, attrs: AttributeSet) : View(context, attrs) {

    private var mDrawPath : CustomPath ? = null //ściężka czyli linia tego co rysujemy
    private var mCanvasBitmap: Bitmap ? = null
    private var mDrawPaint: Paint ? = null
    private var mCanvasPaint: Paint ? = null
    private var mBrushSize: Float = 0.toFloat() //rozmiar pędzla
    private var color = Color.BLACK //kolor pędzla
    private var canvas: Canvas ?= null //tło po którym będziemy rysować
    private val mPaths = ArrayList<CustomPath>() //zmienna w której będziemy przechowywać to co narysowaliśmy
    private val mUndoPaths = ArrayList<CustomPath>() //zmienna do przechowywania ścieżek z mPaths by móc cofać
    private val mForwardPaths = ArrayList<CustomPath>() //zmienna do przechowywania ścieżek z mPaths by móc cofać cofnięcie
    private var howMuchUndo: Int = 0
    //private var erase: Boolean = false //zmienna by wiedzieć czy jest ustawiona gumka czy nie

    init {
        setUpDrawing()
    }

     fun onClickUndo(){
        if(mPaths.size > 0) { //jeżeli mamy co usuwać
            mUndoPaths.add(mPaths.removeAt(mPaths.size - 1)) //do listy dodajemy mPaths bez tej ostatniej ścieżki którą chcemy usunąć
            if(mForwardPaths.size > howMuchUndo) { //zabezpieczenie by nie wypadać poza listę przy rysowaniu nowych ścieżek po cofnięciu
                howMuchUndo++ //licznik cofnięć
            }
            invalidate() //wywołuje od nowa onDraw i rysuje na nowo ścieżki bez tej ostatnio usuniętej
        }
    }

    fun onClickForward(){
            if(mPaths.size > 0) { //jeżeli są jakieś ścieżki
                if(mForwardPaths[mForwardPaths.size - 1] != mPaths[mPaths.size - 1]){ //jeżeli ścieżka do przywrócenia nie jest jeszcze w mPahts czyli narysowana to

                    when (howMuchUndo) { //w zależności ile cofnięć jest wtedy
                        1 -> {
                            mPaths.add(mForwardPaths[mForwardPaths.size - 1]) //dodajemy do mPaths tą ścieżkę którą chcemy przywrócić
                            invalidate() //rysujemy od nowa ścieżki
                            howMuchUndo-- //zmniejszamy liczbę cofnięć
                        }
                        2 -> {
                            mPaths.add(mForwardPaths[mForwardPaths.size - 2])
                            invalidate()
                            howMuchUndo--
                        }
                        3 -> {
                            mPaths.add(mForwardPaths[mForwardPaths.size - 3])
                            invalidate()
                            howMuchUndo--
                        }
                        4 -> {
                            mPaths.add(mForwardPaths[mForwardPaths.size - 4])
                            invalidate()
                            howMuchUndo--
                        }
                        5 -> {
                            mPaths.add(mForwardPaths[mForwardPaths.size - 5])
                            invalidate()
                            howMuchUndo--
                        }
                    }

            }
        } else if(mPaths.size == 0){ //if potrzebny by móc przywracać ścieżki gdy cofniemy wszystkie ścieżki dotychczasowe 
                when (howMuchUndo) {
                    1 -> {
                        mPaths.add(mForwardPaths[mForwardPaths.size - 1])
                        invalidate()
                        howMuchUndo--
                    }
                    2 -> {
                        mPaths.add(mForwardPaths[mForwardPaths.size - 2])
                        invalidate()
                        howMuchUndo--
                    }
                    3 -> {
                        mPaths.add(mForwardPaths[mForwardPaths.size - 3])
                        invalidate()
                        howMuchUndo--
                    }
                    4 -> {
                        mPaths.add(mForwardPaths[mForwardPaths.size - 4])
                        invalidate()
                        howMuchUndo--
                    }
                    5 -> {
                        mPaths.add(mForwardPaths[mForwardPaths.size - 5])
                        invalidate()
                        howMuchUndo--
                    }
                }
            }
    }

    private fun setUpDrawing() {
        mDrawPaint = Paint() //zainicjowanie zmiennej by nie była nullem
        mDrawPath = CustomPath(color, mBrushSize) //ustawienie mDrawPath przy użyciu stworzonej metody CustomPath
        //mDrawPaint ma dużo wartości które trzeba ustawić od razu
        mDrawPaint!!.color = color //ustawienie koloru
        mDrawPaint!!.style = Paint.Style.STROKE //ustawienie stylu jako linii
        mDrawPaint!!.strokeJoin = Paint.Join.ROUND //ustawienie linii żeby była zaokrąglona
        mDrawPaint!!.strokeCap = Paint.Cap.ROUND //ustawienie linii żeby była zaokrąglona
        mCanvasPaint = Paint(Paint.DITHER_FLAG) //umożliwia kopiowanie bitów z jednej części do drugiej(blitting)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) { //nadpisanie metody
        super.onSizeChanged(w, h, oldw, oldh)
        mCanvasBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888) //tworzymy nasze pole do rysowania jako bitmape z szerokością w, wysokością h i z konfiguracją kolorów ARGB_8888
        canvas = Canvas(mCanvasBitmap!!) //ustawiamy nasze tło jako stworzoną wcześniej bitmape(!! używamy bo jest to null pierwotnie, ale już ustawiliśmy ją wcześniej w tej funkcji)
    }

    //zmiana Canvas na Canvas? jeżeli jest błąd
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawBitmap(mCanvasBitmap!!, 0f,0f,mCanvasPaint) //na naszym tle używamy funkcji drawBitmap i ustawiamy, że ma się zaczynać w górnym lewym rogu

        for(path in mPaths){ //for each bo dopóki rysujemy to będzie się rysowało w mPaths i przy każdej możliwej innej linii może ktoś chcieć inną grubość lub kolor pędzla więc zawsze ustawiamy to tutaj
            mDrawPaint!!.strokeWidth = path.brushThickness //ustawiamy grubość pędzla
            mDrawPaint!!.color = path!!.color //ustawiamy kolor
            canvas.drawPath(path,mDrawPaint!!) //ustawiamy ściężkę zależną od path, mDrawPaint
        }

        if(!mDrawPath!!.isEmpty){ //jeżeli ściężka nie jest pusta to
            mDrawPaint!!.strokeWidth = mDrawPath!!.brushThickness //ustawiamy grubość pędzla
            mDrawPaint!!.color = mDrawPath!!.color //ustawiamy kolor
            canvas.drawPath(mDrawPath!!,mDrawPaint!!) //ustawiamy ściężkę zależną od mDrawPath, mDrawPaint
        }
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        val touchX = event?.x
        val touchY = event?.y

        when(event?.action){
            MotionEvent.ACTION_DOWN -> { //gdy dotykamy ekranu to:
                mDrawPath!!.color = color //jaki ma być kolor tego co rysujemy
                mDrawPath!!.brushThickness = mBrushSize //jaka grubość tego co rysujemy

                mDrawPath!!.reset()
                if (touchX != null) {
                    if (touchY != null) {
                        mDrawPath!!.moveTo(touchX,touchY) //przesuwamy ściężkę tam gdzie zaznaczymy palcem
                    }
                }
            }
            MotionEvent.ACTION_MOVE -> { //gdy przesuwamy
                /*if(erase){
                    if (touchX != null) {
                        if (touchY != null) {
                            mDrawPath!!.lineTo(touchX, touchY)
                        }
                    }
                    mPaths.add(mDrawPath!!)
                    mDrawPath!!.reset()
                    if (touchX != null) {
                        if (touchY != null) {
                            mDrawPath!!.moveTo(touchX,touchY)
                        }
                    }
                }else {
                    if (touchY != null) {
                        if (touchX != null) {
                            mDrawPath!!.lineTo(touchX, touchY) //dodaje linie do zadeklarowanego x i y
                        }
                    }
                }*/

                if (touchY != null) {
                    if (touchX != null) {
                        mDrawPath!!.lineTo(touchX, touchY) //dodaje linie do zadeklarowanego x i y
                    }
                }

            }
            MotionEvent.ACTION_UP ->{ //gdy podniesiemy palec
                mPaths.add(mDrawPath!!) //dodajemy to co narysowaliśmy do arraylisty zdefiniowanej by zapisać na ekranie to co narysowaliśmy
                if(howMuchUndo > 0){
                    mForwardPaths.clear()
                    mForwardPaths.addAll(mPaths)
                    howMuchUndo = 1 //jeżeli przed narysowaniem było cofane to trzeba przypisać 1 licznikowi, dodać wszystkie ścieżki z mPaths żeby można było je przywracać i wyczyścić listę poprzednich ścieżek żeby starych ścieżek nie przywracać
                }
                mForwardPaths.add(mDrawPath!!) //dodajemy to co narysowaliśmy byśmy po cofnieciu mogli wrócić do tego

                mDrawPath = CustomPath(color, mBrushSize)
            }
            else -> return false //jeżeli jakiś inny event to zwracamy false
        }
        invalidate() //usuwa cały widok

        return true
    }

    fun setSizeForBrush(newSize: Float){
        mBrushSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, newSize, resources.displayMetrics) //ustawienie rozmiaru pędzla zależna od wielkości ekranu urządzenia
        mDrawPaint!!.strokeWidth = mBrushSize
    }

    fun setColor(newColor: String){
        color = Color.parseColor(newColor) //przypisujemy do zmiennej globalnej nowy kolor podany w argumencie funckji
        mDrawPaint!!.color = color //ustawiamy color do rysowania na color ustawiony z argumentu funkcji
    }


    /*fun startEraser(){
        mDrawPaint!!.alpha = 0
        color = Color.TRANSPARENT
        mDrawPaint!!.color = Color.TRANSPARENT
        mDrawPaint!!.style = Paint.Style.STROKE //ustawienie stylu jako linii
        mDrawPaint!!.strokeJoin = Paint.Join.ROUND //ustawienie linii żeby była zaokrąglona
        mDrawPaint!!.strokeCap = Paint.Cap.ROUND //ustawienie linii żeby była zaokrąglona
        mDrawPaint!!.maskFilter = null
        mDrawPaint!!.xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
        mDrawPaint!!.isAntiAlias = true
    }

    fun setErase(isErase: Boolean){
        erase = isErase

        if(erase){
            mDrawPaint!!.setXfermode(PorterDuffXfermode(PorterDuff.Mode.CLEAR)) //ustawiamy mDrawPaint na tryb gumki
        }else{
            mDrawPaint!!.setXfermode(null)
        }
    }*/

    internal inner class CustomPath(var color: Int, var brushThickness: Float) : Path() {

    }

}
package com.example.drawingappp

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.media.MediaScannerConnection
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.ImageButton
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.get
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.dialog_brush_size.*
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.lang.Exception

class MainActivity : AppCompatActivity() {

    private var mImageButtonCurrentPaint: ImageButton? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        drawing_view.setSizeForBrush(20.toFloat())

        mImageButtonCurrentPaint = ll_paint_colors[1] as ImageButton //wybieramy z linearlayoutu imagebutton na pierwszym miejscu czyli w naszym przypadku black by był wybrany na początek
        mImageButtonCurrentPaint!!.setImageDrawable(
                ContextCompat.getDrawable(this, R.drawable.pallet_pressed) //na tym naszym buttonie 1 czyli z czarnym kolorem zmieniamy src na pallet_pressed z drawable
        )

        ib_brush.setOnClickListener {
            showBrushSizeChooserDialog() //jak klikniemy na ib_brush to wywołujemy funkcję do pokazania dialogu
        }

        ib_gallery.setOnClickListener {
            if(isReadStorageAllowed()){ //sprawdzamy czy mamy już przyznane pozwolenie

                val pickPhotoIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI) //intent potrzebny do wybrania zdjęcia z galerii

                startActivityForResult(pickPhotoIntent, GALLERY) //dostajemy rezultat czy udało nam się wybrać zdjęcie czy nie

            }else{
                requestStoragePermission() //jeżeli nie mamy przyznanego pozwolenia wywołujemy funkcję by je dostać
            }
        }

        ib_undo.setOnClickListener {
            drawing_view.onClickUndo()
        }

        ib_save.setOnClickListener {
            if(isReadStorageAllowed()){
                BitmapAsyncTask(getBitmapFromView(fl_drawing_view_container)).execute() //przekazujemy do naszej klasy bitmapę przekonwertowaną metodą z view(z całego kontenera FrameLayout), wykonujemy tą klasę przez dodanie execute()
            }else{
                requestStoragePermission()
            }
        }

        ib_new.setOnClickListener {
            newProjectDialog()
        }

        ib_forward.setOnClickListener {
            drawing_view.onClickForward()
        }

        /*ib_erase.setOnClickListener {
            showEraserSizeChooserDialog()
            drawing_view.startEraser()
        }*/
    }

    private fun newProjectDialog(){
        val newProject = AlertDialog.Builder(this)
        newProject.setTitle("Nowy projekt")
        newProject.setMessage("Stworzyć nowy projekt?(stracisz swój dotychczasowy projekt)")
        newProject.setPositiveButton("Tak") { dialogInterface, which ->
            finish() //kończymy activity
            overridePendingTransition(0,0) //funkcja by nasze włączanie od nowa activity było bardziej płynne
            startActivity(Intent(this, MainActivity::class.java)) //odpalamy znowu ten sam intent by zresetować wszystko i dać użytkownikowi robić nowy projekt
            overridePendingTransition(0,0)
            dialogInterface.dismiss()
        }
        newProject.setNegativeButton("Anuluj") { dialogInterface, which ->
            dialogInterface.dismiss()
        }

        val newProjectDialog: AlertDialog = newProject.create()
        newProjectDialog.show()
    }

    private fun showBrushSizeChooserDialog(){ //funkcja do pokazania dialogu z wyborem rozmiaru pędzla
        val brushDialog = Dialog(this)
        brushDialog.setContentView(R.layout.dialog_brush_size) //ustawiamy wygląd dialogu na przygotowany w xml
        brushDialog.setTitle("Rozmiar pędzla: ") //tytuł dialogu

        val smallBtn = brushDialog.ib_small_brush
        smallBtn.setOnClickListener {
            //drawing_view.setErase(false) //usuwamy tryb gumki
            drawing_view.setSizeForBrush(10.toFloat()) //ustawia rozmiar pędzla na 10
            brushDialog.dismiss() // znika widok wyboru rozmiaru pędzla
        }

        val mediumBtn = brushDialog.ib_medium_brush
        mediumBtn.setOnClickListener {
            //drawing_view.setErase(false) //usuwamy tryb gumki
            drawing_view.setSizeForBrush(20.toFloat()) //ustawia rozmiar pędzla na 20
            brushDialog.dismiss() // znika widok wyboru rozmiaru pędzla
        }

        val largeBtn = brushDialog.ib_large_brush
        largeBtn.setOnClickListener {
            //drawing_view.setErase(false) //usuwamy tryb gumki
            drawing_view.setSizeForBrush(30.toFloat()) //ustawia rozmiar pędzla na 30
            brushDialog.dismiss() // znika widok wyboru rozmiaru pędzla
        }
        brushDialog.show() //żeby pokazywalo brushDialog
    }

    /*private fun showEraserSizeChooserDialog(){ //funkcja do pokazania dialogu z wyborem rozmiaru gumki
        val brushDialog = Dialog(this)
        brushDialog.setContentView(R.layout.dialog_brush_size) //ustawiamy wygląd dialogu na przygotowany w xml
        brushDialog.setTitle("Rozmiar gumki: ") //tytuł dialogu

        val smallBtn = brushDialog.ib_small_brush
        smallBtn.setOnClickListener {
            drawing_view.setErase(true) //ustawiamy tryb gumki
            drawing_view.setSizeForBrush(10.toFloat()) //ustawia rozmiar pędzla na 10
            brushDialog.dismiss() // znika widok wyboru rozmiaru pędzla
        }

        val mediumBtn = brushDialog.ib_medium_brush
        mediumBtn.setOnClickListener {
            drawing_view.setErase(true) //ustawiamy tryb gumki
            drawing_view.setSizeForBrush(20.toFloat()) //ustawia rozmiar pędzla na 20
            brushDialog.dismiss() // znika widok wyboru rozmiaru pędzla
        }

        val largeBtn = brushDialog.ib_large_brush
        largeBtn.setOnClickListener {
            drawing_view.setErase(true) //ustawiamy tryb gumki
            drawing_view.setSizeForBrush(30.toFloat()) //ustawia rozmiar pędzla na 30
            brushDialog.dismiss() // znika widok wyboru rozmiaru pędzla
        }
        brushDialog.show() //żeby pokazywalo brushDialog
    }*/

    fun paintClicked(view: View){
        //drawing_view.setErase(false) //usuwamy tryb gumki
        if(view != mImageButtonCurrentPaint){ //jeżeli kliknięty imagebutton nie jest wybranym już wcześniej buttonem to wtedy:
            val imageButton = view as ImageButton //przypisujemy do zmiennej ten aktualnie kliknięty imagebutton
            val colorTag = imageButton.tag.toString() //zapisujemy pod zmienną colorTag kolor aktualnie klikniętego buttonu z właściwości tag jako string(bo to kolor)
            drawing_view.setColor(colorTag)

            imageButton.setImageDrawable(
                    ContextCompat.getDrawable(this, R.drawable.pallet_pressed) //nowo wciśniety button ustawiamy na wygląd pressed
            )
            mImageButtonCurrentPaint!!.setImageDrawable(
                    ContextCompat.getDrawable(this, R.drawable.pallet_normal) //stary button który był wciśnięty ustawiamy na normalny wygląd
            )
            mImageButtonCurrentPaint = view //przypisujemy aktualnie kliknięty button z view do zmiennej
        }
    }

    //funkcja potrzebna do przyznania pozwolenia przez użytkownika
    private fun requestStoragePermission(){
        if(ActivityCompat.shouldShowRequestPermissionRationale(this,
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE).toString())){ //kod potrzebny gdy użytkownik odmówi pozwolenia dostępu, wtedy dostaje wiadomość po co są potrzebne te pozwolenia
            Toast.makeText(this, "Potrzebne pozwolenie by dodać tło!", Toast.LENGTH_SHORT).show()
        }

        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE), STORAGE_PERMISSION_CODE) //wywołuje okienko o pozwolenia
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == STORAGE_PERMISSION_CODE){
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){ //jeżeli użytkownik przydzielił nam pozwolenie
                Toast.makeText(this, "Pozwolenie przyznane! Teraz możesz odczytywać pliki z telefonu", Toast.LENGTH_LONG).show()
            }else{
                Toast.makeText(this, "Musisz zezwolić na odczyt plików w telefonie by aplikacja działała poprawnie", Toast.LENGTH_LONG).show() //jeżeli odmówione pozwolenie pokazuje toast
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == Activity.RESULT_OK){
            if(requestCode == GALLERY){
                try {
                    if(data!!.data != null){ //jeżeli data nie jest null to znaczy, że użytkownik wybrał coś
                        iv_background.visibility = View.VISIBLE //upewniamy się, że tło jest widzialne
                        iv_background.setImageURI(data.data) //ustawiamy tło na wybrane przez użytkownika przez funkcję setImageURI, ponieważ wybierając z galerii taki typ danych dostajemy(czyli dokładnie adres w pamięci telefonu (URI))
                    }else{
                        Toast.makeText(this, "Błąd podczas wgrywania zdjęcia. Zły format albo plik jest uszkodzony", Toast.LENGTH_SHORT)
                    }
                }catch (e: Exception){
                    e.printStackTrace()
                }
            }
        }
    }

    //funkcja umożliwiająca sprawdzenie czy pozwolenie na odczyt danych przyznane jest w dalszym ciągu(bo użytkownik mógł zabrać je w ustawieniach)
    private fun isReadStorageAllowed(): Boolean{
        val result = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)

        return result == PackageManager.PERMISSION_GRANTED //zwraca true jeżeli jest dalej przyznane pozwolenie
    }

    private fun getBitmapFromView(view: View): Bitmap{
        val returnedBitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888) //tworzymy bitmapę z naszego view. Z całej wysokości i szerokości z ustawieniami kolorów argb_8888
        val canvas = Canvas(returnedBitmap) //tworzymy nowy canvas na który dajemy bitmapę stworzoną
        val bgDrawable = view.background //zmienna do przechowania tła
        if (bgDrawable != null){
            bgDrawable.draw(canvas) //jeżeli nie prawda, że tło jest puste to rysujemy na canvas nasze tło
        }else{
            canvas.drawColor(Color.WHITE) //jeżeli tło jest puste to ustawiamy tło na podstawowe czyli białe
        }

        view.draw(canvas) //wszystko co ustaliliśmy na naszej zmiennej canvas rysujemy na view

        return returnedBitmap //zwracamy cały rysunek w bitmapie
    }

    //klasa do zapisywania zdjęcia w tle
    private inner class BitmapAsyncTask(val mBitmap: Bitmap): AsyncTask<Any, Void, String>(){

        private lateinit var mProgressDialog: Dialog

        override fun onPreExecute() {
            super.onPreExecute()
            showProgressDialog() //przed zapisywaniem zdjęcia pokazujemy dialog proszę czekać
        }

        override fun doInBackground(vararg params: Any?): String {

            var result = ""

            if(mBitmap != null){ //jeżeli dostaliśmy jakąś bitmapę w parametrze to
                try { //musimy użyć bloku try catch ponieważ będziemy działali na outputstreamie
                    val bytes = ByteArrayOutputStream() //zmienna potrzebna by przechować skompresowaną bitmapę
                    mBitmap.compress(Bitmap.CompressFormat.PNG, 90, bytes) //kompresujemy bitmapę na format png z jakością 90% i przesyłamy do bytes
                    val f = File(externalCacheDir!!.absoluteFile.toString() + File.separator
                            + "DrawingApp_" + System.currentTimeMillis() / 1000 + ".png") //przygotowujemy plik o różnej nazwie dla każdego zapisanego pliku dzięki użyciu millisekund

                    val fos = FileOutputStream(f) //tworzymy zmienną do której przekazujemy nasz plik
                    fos.write(bytes.toByteArray()) //to zmiennej przekazujemy naszą zmienną bytes z zapisanym skompresowanym naszym zdjęciem w niej
                    fos.close() //zawsze zamykamy strumienie
                    result = f.absolutePath //do zmiennej result przekazujemy ściężkę do pliku

                }catch (e: Exception){
                    result = ""
                    e.printStackTrace()
                }
            }

            return result

        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            cancelProgressDialog() //usuwamy progress dialog
            if(!result!!.isEmpty()){
                Toast.makeText(this@MainActivity, "Plik zapisany pomyślnie: $result", Toast.LENGTH_SHORT).show()
            }else{
                Toast.makeText(this@MainActivity, "Coś poszło nie tak z zapisem pliku", Toast.LENGTH_SHORT).show()
            }

            //funkcjonalność która umożliwia udostępnienie zapisanej pracy
            MediaScannerConnection.scanFile(this@MainActivity, arrayOf(result), null){
                path, uri -> val shareIntent = Intent()
                shareIntent.action = Intent.ACTION_SEND //ustawiamy akcję send na shareIntent
                shareIntent.putExtra(Intent.EXTRA_STREAM, uri) //wysyłamy do intentu extra_stream i uri
                shareIntent.type = "image/png" //ustawiamy typ

                startActivity(Intent.createChooser(shareIntent, "Udostępnij"))
            }
        }

        private fun showProgressDialog(){
            mProgressDialog = Dialog(this@MainActivity)
            mProgressDialog.setContentView(R.layout.dialog_custom_progress) //ustawiamy wygląd dialogu na zdefiniowany w pliku xml
            mProgressDialog.show() //wyświetlamy progress dialog
        }

        private fun cancelProgressDialog(){
            mProgressDialog.dismiss() //kończy wyswietlanie dialogu
        }

    }

    companion object{
        private const val STORAGE_PERMISSION_CODE = 1 //zmienna na potrzeby tego by wiedzieć jakie pozwolenie chcemy
        private const val GALLERY = 2 //zmienna potrzebna do wybrania zdjęcia w galerii
    }
}
package com.iesribera.tarea4_ortiz_sobrino;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity {

	//	private String DIRECTORY_PICTURES;
	private final int MAKE_PHOTO = 10;
	public final int SAVE_FILE = 11;
	private int SEEK_PROGRESS = 100;

	private String finalPath;
	private boolean toogleClicked = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		Button photoButton = findViewById(R.id.photoButton);

		photoButton.setOnClickListener(this::makePhoto);
		SeekBar seekBar = findViewById(R.id.seekBar);
		seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {

			}

			@Override public void onStopTrackingTouch(SeekBar seekBar) {
				if (seekBar.getProgress() == 0) {
					seekBar.setProgress(1);
				}
				SEEK_PROGRESS = seekBar.getProgress();

				if (finalPath == null)
					return;

				if (toogleClicked)
					decoder();
				else
					scaleBitmap();
			}
		});

		ToggleButton tb = findViewById(R.id.toggleButton);

		tb.setOnClickListener(v -> {
			toogleClicked = !toogleClicked;

			if (finalPath == null)
				return;

			if (toogleClicked)
				decoder();
			else
				scaleBitmap();
		});
		if (!hasPermissions(Collections.singletonList(Manifest.permission.CAMERA))) {
			askPermission(Manifest.permission.CAMERA, MAKE_PHOTO);
		}
		if (!hasPermissions(Collections.singletonList(Manifest.permission.WRITE_EXTERNAL_STORAGE))) {
			askPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, SAVE_FILE);
		}
	}

	private boolean hasPermissions(List<String> permissions) {
		boolean hasAllPermission = true;
		for (String permission : permissions) {
			if (ContextCompat.checkSelfPermission(this, permission)
					!= PackageManager.PERMISSION_GRANTED) {
				return !hasAllPermission;
			}
		}
		return hasAllPermission;
	}

	private void askPermission(String permission, int save_file) {
		if (ContextCompat.checkSelfPermission(this,
											  permission) != PackageManager.PERMISSION_GRANTED) {
			ActivityCompat.requestPermissions(this,
											  new String[]{
													  permission}, save_file);
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == MAKE_PHOTO && resultCode == RESULT_OK) {
			Toast.makeText(this, "La imagen se ha guardado correctamente: " + finalPath,
						   Toast.LENGTH_LONG).show();

			ImageView image = findViewById(R.id.imageView);
			if (toogleClicked)
				image.setImageBitmap(decoder());
			else
				image.setImageBitmap(scaleBitmap());
		} else {
			Toast.makeText(this, "Error al obtener imagen en la ruta: " + finalPath, Toast.LENGTH_LONG).show();
		}

	}

	private Bitmap decoder() {
		BitmapFactory.Options bmOptions = new BitmapFactory.Options();
		bmOptions.inSampleSize = 100 / SEEK_PROGRESS;
		Bitmap bm = BitmapFactory.decodeFile(finalPath, bmOptions);

		adjustImage(bm.getHeight(), bm.getWidth());

		return bm;
	}

	public Bitmap scaleBitmap() {
		Bitmap bitmap = BitmapFactory.decodeFile(finalPath, null);
		Bitmap scaledBitmap =
				Bitmap.createScaledBitmap(bitmap, bitmap.getWidth() * SEEK_PROGRESS / 100,
										  bitmap.getHeight() * SEEK_PROGRESS / 100,
										  false);

		adjustImage(scaledBitmap.getHeight(), scaledBitmap.getWidth());
		return scaledBitmap;
	}

	public void adjustImage(int height, int width) {
		ImageView image = findViewById(R.id.imageView);
		image.getLayoutParams().height = height;
		image.getLayoutParams().width = width;
		image.requestLayout();
	}

	public void makePhoto(View v) {
		if (hasPermissions(Arrays.asList(Manifest.permission.CAMERA,
										 Manifest.permission.MANAGE_EXTERNAL_STORAGE))) {
			Toast.makeText(this, "No se han aceptado los permisos necesarios para usar esta aplicación",
						   Toast.LENGTH_LONG).show();
			return;
		}
		StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
		StrictMode.setVmPolicy(builder.build());
		Intent hacerFotoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		if (hacerFotoIntent.resolveActivity(getPackageManager()) != null) {
			//Crear un fichero y pasárselo como extra a la actividad
			File photo = null;
			try {
				photo = createImage();
			} catch (IOException e) {
				//acción a realizar si no he podido crear el fichero
				e.printStackTrace();
			}
			if (photo != null) {
				hacerFotoIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photo));
				startActivityForResult(hacerFotoIntent, MAKE_PHOTO);
			}
		}

	}

	public File createImage() throws IOException {
		String tiempo = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
		String nombre_fichero = "JPEG_" + tiempo + "_";
		File directorio_almacenaje = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
		File image = File.createTempFile(nombre_fichero, ".jpg", directorio_almacenaje);
		finalPath = image.getAbsolutePath();
		return image;
	}
}
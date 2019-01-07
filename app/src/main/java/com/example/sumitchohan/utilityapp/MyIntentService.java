package com.example.sumitchohan.utilityapp;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;

import com.googlecode.tesseract.android.TessBaseAPI;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class MyIntentService extends IntentService {
    // TODO: Rename actions, choose action names that describe tasks that this
    // IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
    private static final String ACTION_FOO = "com.example.sumitchohan.utilityapp.action.FOO";
    private static final String ACTION_BAZ = "com.example.sumitchohan.utilityapp.action.BAZ";

    // TODO: Rename parameters
    private static final String EXTRA_PARAM1 = "com.example.sumitchohan.utilityapp.extra.PARAM1";
    private static final String EXTRA_PARAM2 = "com.example.sumitchohan.utilityapp.extra.PARAM2";

    public MyIntentService() {
        super("MyIntentService");
    }
    @Override
    protected void onHandleIntent(Intent intent) {

        String result = "";
        String showResults = "n";
        if (intent != null) {
            String action = intent.getExtras().getString("action");
            showResults = intent.getExtras().getString("show");
            if (action != null) {

                switch (action) {
                    case "ECHO":
                    {
                        String data = intent.getExtras().getString("data");
                        result=data;
                        showResults="y";
                    }
                    break;
                    case "READ":
                    {
                        String filepath = intent.getExtras().getString("filepath");
                        String fileContent = "";
                        try {
                            File f = new File(filepath);
                            byte[] bf = new byte[(int) f.length()];
                            new FileInputStream(f).read(bf);
                            fileContent = new String(bf, "UTF-8");
                            result = fileContent;
                        } catch (FileNotFoundException e) {
                            result = e.getMessage();
                        } catch (IOException e) {
                            result = e.getMessage();
                        }
                    }
                    break;

                    case "WRITE":
                    {
                        String filepath = intent.getExtras().getString("filepath");
                        String data = intent.getExtras().getString("data");

                        try {
                            writeUsingOutputStream(data, filepath);
                        } catch (Exception e) {
                            result = e.getMessage();
                        }

                        result = "write  done";
                    }
                    break;


                    case "CROP":
                    {
                        String filepath = intent.getExtras().getString("filepath");
                        String outputfilepath = intent.getExtras().getString("outputfilepath");
                        String x = intent.getExtras().getString("x");
                        String y = intent.getExtras().getString("y");
                        String w = intent.getExtras().getString("w");
                        String h = intent.getExtras().getString("h");
                        try {
                            Bitmap bitmap = BitmapFactory.decodeFile(filepath);
                            cropBitmapIntoJPEG(bitmap, Integer.parseInt(x), Integer.parseInt(y),
                                    Integer.parseInt(w), Integer.parseInt(h), outputfilepath);

                            result = "success";


                        } catch (Exception e) {
                            result = e.getMessage();
                        }

                    }
                    break;


                    case "FILE_SIZE":
                    {
                        String filepath = intent.getExtras().getString("filepath");
                        String outFilePath = intent.getExtras().getString("outFilePath");
                        try {
                            result =Long.toString( new File(filepath).length());
                            writeUsingOutputStream(result,outFilePath);

                        } catch (Exception e) {
                            result = e.getMessage();
                        }
                    }
                    break;
                    case "READ_IMAGE":
                    {
                        String imagePath = intent.getExtras().getString("imagePath");
                        String configPath = intent.getExtras().getString("configPath");
                        try
                        {
                            ocrImage(imagePath,configPath);
                        }
                        catch (Exception e)
                        {
                            result=e.getMessage();
                        }

                    }
                    break;

                    case "PROCESS_RECORD_FILE":
                    {
                        String imagePath = intent.getExtras().getString("recordFilePath");
                        String configPath = intent.getExtras().getString("recordFileSizePath");
                        try
                        {
                            ocrImage(imagePath,configPath);

                        }
                        catch (Exception e)
                        {
                            result=e.getMessage();
                        }

                    }
                    break;
                    default:
                        result = "Unhandled action!";

                }

            }
        }
        if (showResults != null && showResults == "y") {
            Intent intent1 = new Intent(this, DisplayMessageActivity.class);
            intent1.putExtra(MainActivity.EXTRA_MESSAGE, result);
            startActivity(intent1);
        }
        String completedFilePath = intent.getExtras().getString("completedFilePath");
        if (completedFilePath != null && completedFilePath != "") {
            try {
                writeUsingOutputStream("done", completedFilePath);
            } catch (Exception e) {
                result = e.getMessage();
            }
        }
    }
    private static void cropBitmapIntoJPEG(Bitmap inputBitmap, int x, int y, int width, int height, String outputFilePath) {
        Bitmap cropped = Bitmap.createBitmap(inputBitmap, x, y, width, height);


        File file = new File(outputFilePath);
        if (file.exists()) file.delete();
        try {
            FileOutputStream out = new FileOutputStream(file);
            cropped.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private static void ocrImage(String imagePath,String configFilePath) throws IOException
    {
        Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
        BufferedReader br = new BufferedReader(new FileReader (configFilePath));
        String line = null;
        while ((line = br.readLine()) != null)
        {
            String[] lineParts = line.split(",");
            if(lineParts.length>4)
            {
                Bitmap cropped = Bitmap.createBitmap(bitmap, Integer.parseInt(lineParts[1]), Integer.parseInt(lineParts[2]),
                        Integer.parseInt(lineParts[3]), Integer.parseInt(lineParts[4]));


                if(lineParts.length>5)
                {
                    for (int x = 0; x < cropped.getWidth(); x++) {
                        for (int y = 0; y < cropped.getHeight(); y++) {
                            int pixel=cropped.getPixel(x,y);
                            int redValue = Color.red(pixel);
                            int blueValue = Color.blue(pixel);
                            int greenValue = Color.green(pixel);
                            String[] colorParts=lineParts[5].split("_");
                            if(match(redValue,greenValue,blueValue,Integer.parseInt(colorParts[0])
                                    ,Integer.parseInt(colorParts[1])
                                    ,Integer.parseInt(colorParts[2]))) {
                                cropped.setPixel(x, y, Color.rgb(255,255,255));
                            }
                            else
                            {
                                cropped.setPixel(x, y, Color.rgb(0,0,0));
                            }
                        }
                    }
                }
                Matrix matrix = new Matrix();
                matrix.postRotate(270);
                Bitmap rotated=Bitmap.createBitmap(cropped, 0, 0, cropped.getWidth(), cropped.getHeight(),matrix,true);
                /*
                File file = new File(imagePath+lineParts[0]+"rotated.png");
                if (file.exists()) file.delete();
                try {
                    FileOutputStream out = new FileOutputStream(file);
                    rotated.compress(Bitmap.CompressFormat.JPEG, 100, out);
                    out.flush();
                    out.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                */
                TessBaseAPI tessTwo = new TessBaseAPI();
                tessTwo.init("/sdcard", "coc1");
                tessTwo.setPageSegMode(TessBaseAPI.PageSegMode.PSM_SINGLE_LINE);
                tessTwo.setImage(rotated);
                String recognizedText = tessTwo.getUTF8Text().replaceAll("[^0-9]", "");;
                tessTwo.end();
                writeUsingOutputStream(recognizedText,"/sdcard/coc/ocred_"+lineParts[0]+".txt");
            }

        }
    }
    private static boolean match(int r1, int g1, int b1, int r2, int g2, int b2)
    {
        int d=diff(r1,r2)+diff(g1,g2)+diff(b1,b2);
        if(d>40)
        {
            return false;
        }
        else
        {
            return true;
        }
    }
    private static int diff(int i1, int i2)
    {
        if(i1>i2)
        {
            return i1-i2;
        }
        else
        {
            return i2-i1;
        }
    }
    private static void writeUsingOutputStream(String data, String filepath) {
        OutputStream os = null;
        try {
            os = new FileOutputStream(new File(filepath));
            os.write(data.getBytes(), 0, data.length());
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                os.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
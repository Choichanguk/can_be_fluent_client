package com.example.canbefluent;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

//import sun.rmi.runtime.Log;

/**
 * 이미지의 사이즈를 커스텀마이징 할 수 있다.
 * 카메라 촬영일 경우 회전 각도를 가져와 이미지를 회전시킨다.
 */
public class ImageResizeUtils {
    private static final String TAG = "ImageResizeUtils";
    /**
     *
     * @param file   내가 변셩 시키고 싶은 파일
     * @param newFile   변형시킨 파일을 저장할 파일
     * @param newWidth  리사이징할 크기
     * @param isCamera  카메라에서 온 이미지인지 구분하는 변수
     */
    public static void resizeFile(File file, File newFile, int newWidth, Boolean isCamera) {
        Log.e(TAG, "resizeFile");
//        String TAG = "blackjin";

        Bitmap originalBm = null;
        Bitmap resizedBitmap = null;

        try {

            // file의 비트맵을 가져올 때, 옵션들을 달아준다.
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPurgeable = true;

            // file의 비트맵을 가져온다
            Log.e(TAG, "file path: " + file.getAbsolutePath());
            originalBm = BitmapFactory.decodeFile(file.getAbsolutePath(), options);

            if(isCamera) {

                // 카메라인 경우 이미지를 상황에 맞게 회전시킨다
                try {
                    //EXIF: 디지털 사진의 이미지 정보
                    // ExifInterface 클래스를 통해 만들어진 exif객체를 통해 file의 EXIF 정보를 가져올 수 있다.
                    //
                    ExifInterface exif = new ExifInterface(file.getAbsolutePath());


                    int exifOrientation = exif.getAttributeInt(
                            ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);

                    Log.e(TAG,"exifOrientation : " + exifOrientation);
                    // exifOrientationToDegrees메서드를 통해 오리지날 이미지의 degree 정보를 얻는다.
                    // 리턴값이 0일 땐 회전 불필요.
                    int exifDegree = exifOrientationToDegrees(exifOrientation);
                    Log.e(TAG,"exifDegree : " + exifDegree);

                    // 오리지날 이미지의 degree값에 따라 회전시켜준다.
                    originalBm = rotate(originalBm, exifDegree);

                } catch (IOException e) {
                    e.printStackTrace();
                }

            }

            if(originalBm == null) {
                Log.e(TAG,("파일 에러"));
                return;
            }

            int width = originalBm.getWidth();
            int height = originalBm.getHeight();

            float aspect, scaleWidth, scaleHeight;
            if(width > height) {
                if(width <= newWidth) return;

                aspect = (float) width / height;

                scaleWidth = newWidth;
                scaleHeight = scaleWidth / aspect;

            } else {

                if(height <= newWidth) return;

                aspect = (float) height / width;

                scaleHeight = newWidth;
                scaleWidth = scaleHeight / aspect;

            }

            // create a matrix for the manipulation
            Matrix matrix = new Matrix();

            // resize the bitmap
            // postScale: Postconcats the matrix with the specified scale.
            matrix.postScale(scaleWidth / width, scaleHeight / height);

            // recreate the new Bitmap
            resizedBitmap = Bitmap.createBitmap(originalBm, 0, 0, width, height, matrix, true);

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {

                resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 80, new FileOutputStream(newFile));

            } else {

                resizedBitmap.compress(Bitmap.CompressFormat.PNG, 80, new FileOutputStream(newFile));

            }


        } catch (FileNotFoundException e) {

            e.printStackTrace();

        } finally {

            if(originalBm != null){
                originalBm.recycle();
            }

            if (resizedBitmap != null){
                resizedBitmap.recycle();
            }
        }
    }

    /**
     * EXIF 정보를 회전각도로 변환하는 메서드
     *
     * @param exifOrientation EXIF 회전각
     * @return 실제 각도
     */
    public static int exifOrientationToDegrees(int exifOrientation)
    {
        Log.e(TAG, "exifOrientationToDegrees");
        if(exifOrientation == ExifInterface.ORIENTATION_ROTATE_90)
        {
            return 90;
        }
        else if(exifOrientation == ExifInterface.ORIENTATION_ROTATE_180)
        {
            return 180;
        }
        else if(exifOrientation == ExifInterface.ORIENTATION_ROTATE_270)
        {
            return 270;
        }
        return 0;
    }

    /**
     * 이미지를 회전시킵니다.
     *
     * @param bitmap 비트맵 이미지
     * @param degrees 회전 각도
     * @return 회전된 이미지
     */
    public static Bitmap rotate(Bitmap bitmap, int degrees)
    {
        Log.e(TAG, "rotate");
//        if(degrees != 0 && bitmap != null)
//        {
            Matrix m = new Matrix();
            m.setRotate(90, (float) bitmap.getWidth() / 2,
                    (float) bitmap.getHeight() / 2);

            try
            {
                Bitmap converted = Bitmap.createBitmap(bitmap, 0, 0,
                        bitmap.getWidth(), bitmap.getHeight(), m, true);
                if(bitmap != converted)
                {
                    bitmap.recycle();
                    bitmap = converted;
                }
            }
            catch(OutOfMemoryError ex)
            {
                // 메모리가 부족하여 회전을 시키지 못할 경우 그냥 원본을 반환합니다.
            }
//        }
        return bitmap;
    }
}

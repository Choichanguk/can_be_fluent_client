package com.example.canbefluent.ml_kit;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.net.Uri;
import android.util.Log;

import com.example.canbefluent.R;
import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceContour;
import com.google.mlkit.vision.face.FaceLandmark;

import java.io.FileNotFoundException;
import java.text.DecimalFormat;
import java.util.List;

/** Graphic instance for rendering face contours graphic overlay view. */
public class FaceContourGraphic2 extends GraphicOverlay.Graphic {
    private static final String TAG = "FaceContourGraphic2";

    private final Paint facePositionPaint;
//  private final Paint idPaint;
//  private final Paint boxPaint;

    private volatile Face face;
    Bitmap image;

    // sunglasses, beard
    String type;

    int width, height;



    public FaceContourGraphic2(GraphicOverlay overlay, Context context, String type) {
        super(overlay);
        Resources r = context.getResources();

        this.type = type;

        // 마스크 타입별로 다른 이미지를 불러온다.
        if(type.equals("sunglasses")){
            image = BitmapFactory.decodeResource(r, R.drawable.sunglasses2);
        }
        else if(type.equals("beard")){
            image = BitmapFactory.decodeResource(r, R.drawable.beard);
        }

        /**
         * 페인터 객체들을 초기화 시켜준다
         * 여기선, 얼굴박스, 얼굴 윤곽 위치, id 페인터 객체
         */
        facePositionPaint = new Paint();
    }

    /**
     * Updates the face instance from the detection of the most recent frame. Invalidates the relevant
     * portions of the overlay to trigger a redraw.
     */
    public void updateFace(Face face) {
//    Log.e(TAG, "updateFace");
        this.face = face;
        postInvalidate();
    }

    /** Draws the face annotations for position on the supplied canvas. */
    @Override
    public void draw(Canvas canvas) {
//    Log.e(TAG, "draw");
        Face face = this.face;
        if (face == null) {
            return;
        }

        // Draws a circle at the position of the detected face, with the face's track id below.
        float x = translateX(face.getBoundingBox().centerX());  //얼굴 박스의 가운데 좌표 x값
//    float y = translateY(face.getBoundingBox().centerY());  //얼굴 박스의 가운데 좌표 y값


        // Draws a bounding box around the face.
        float xOffset = scaleX(face.getBoundingBox().width() / 2.0f);
//    float yOffset = scaleY(face.getBoundingBox().height() / 2.0f);
        float left = x - xOffset;
//    float top = y - yOffset;
//    float right = x + xOffset;
//    float bottom = y + yOffset;

        List<FaceContour> contour = face.getAllContours();
        float image_ratio = image.getHeight()*1f / image.getWidth()*1f;  // 이미지 높이 : 넓이 비율

        if(type.equals("sunglasses")){

            int resize_height = (int) (face.getBoundingBox().width() * image_ratio); // 축소할 이미지 높이

            // 얼굴 크기에 맞게 이미지 축소
            Bitmap resizedBmp = Bitmap.createScaledBitmap(image, (int) (face.getBoundingBox().width()), (int) (resize_height), false);

            for (FaceContour faceContour : contour) {

                if(faceContour.getFaceContourType() == 12){
                    Log.e(TAG, "node bridge contour 개수: " + faceContour.getPoints().size());
                    for (int i = 0; i < faceContour.getPoints().size(); i++){
                        if(i == 0){
                            float py = translateY(faceContour.getPoints().get(i).y);
                            canvas.drawBitmap(resizedBmp, left , (py), null);
                        }
                    }

                }
            }
        }
        else if(type.equals("beard")){
//      float nose_botX;
            float nose_botY = 0;
            float left_cheekX = 0;
//      float left_cheekY;
            float right_cheekX = 0;
//      float right_cheekY;

            for (FaceContour faceContour : contour) {
                // 12: node bridge contour

                // 코 밑 부분 좌표 추출
                if(faceContour.getFaceContourType() == 13){
//          nose_botX = faceContour.getPoints().get(0).x;
                    nose_botY = faceContour.getPoints().get(0).y;
                }
                // 왼쪽 뺨 좌표 추출
                else if(faceContour.getFaceContourType() == 14){
                    left_cheekX = faceContour.getPoints().get(0).x;
//          left_cheekY = faceContour.getPoints().get(0).y;
                }
                // 오른쪽 뺨 추출
                else if(faceContour.getFaceContourType() == 15){
                    right_cheekX = faceContour.getPoints().get(0).x;
//          right_cheekY = faceContour.getPoints().get(0).y;
                }
            }

            // 턱수염 이미지 넓이
            if(right_cheekX !=  0 && left_cheekX != 0){
                int resize_width = (int)(right_cheekX - left_cheekX);
                int resize_height = (int)(resize_width * image_ratio);
                Bitmap resizedBmp = Bitmap.createScaledBitmap(image, (int) (resize_width), (int) (resize_height), false);
                canvas.drawBitmap(resizedBmp, left_cheekX, nose_botY, null);
            }
        }
    }

}

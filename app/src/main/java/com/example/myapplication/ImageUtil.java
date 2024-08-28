package com.example.myapplication;

import android.media.Image;

import androidx.annotation.NonNull;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import java.nio.ByteBuffer;
public class ImageUtil {
    public ImageUtil(Image image) {
        super();
        mImage = image;
        mRgba = new Mat();
        mGray = new Mat();
    }

    private Image mImage;
    private Mat mRgba;
    private Mat mGray;

    public Mat gray() {
        Image.Plane[] planes = mImage.getPlanes();
        int w = mImage.getWidth();
        int h = mImage.getHeight();
        assert (planes[0].getPixelStride() == 1);
        ByteBuffer y_plane = planes[0].getBuffer();
        int y_plane_step = planes[0].getRowStride();
        mGray = new Mat(h, w, CvType.CV_8UC1, y_plane, y_plane_step);
        return mGray;
    }

    public Mat rgba() {
        Image.Plane[] planes = mImage.getPlanes();
        int w = mImage.getWidth();
        int h = mImage.getHeight();
        int chromaPixelStride = planes[1].getPixelStride();
        if (chromaPixelStride == 2) { // Chroma channels are interleaved
            assert (planes[0].getPixelStride() == 1);
            assert (planes[2].getPixelStride() == 2);
            ByteBuffer y_plane = planes[0].getBuffer();
            int y_plane_step = planes[0].getRowStride();
            ByteBuffer uv_plane1 = planes[1].getBuffer();
            int uv_plane1_step = planes[1].getRowStride();
            ByteBuffer uv_plane2 = planes[2].getBuffer();
            int uv_plane2_step = planes[2].getRowStride();
            Mat y_mat = new Mat(h, w, CvType.CV_8UC1, y_plane, y_plane_step);
            Mat uv_mat1 = new Mat(h / 2, w / 2, CvType.CV_8UC2, uv_plane1, uv_plane1_step);
            Mat uv_mat2 = new Mat(h / 2, w / 2, CvType.CV_8UC2, uv_plane2, uv_plane2_step);
            long addr_diff = uv_mat2.dataAddr() - uv_mat1.dataAddr();
            if (addr_diff > 0) {
                assert (addr_diff == 1);
                Imgproc.cvtColorTwoPlane(y_mat, uv_mat1, mRgba, Imgproc.COLOR_YUV2RGBA_NV12);
            } else {
                assert (addr_diff == -1);
                Imgproc.cvtColorTwoPlane(y_mat, uv_mat2, mRgba, Imgproc.COLOR_YUV2RGBA_NV21);
            }
            return mRgba;
        } else { // Chroma channels are not interleaved
            byte[] yuv_bytes = new byte[w * (h + h / 2)];
            ByteBuffer y_plane = planes[0].getBuffer();
            ByteBuffer u_plane = planes[1].getBuffer();
            ByteBuffer v_plane = planes[2].getBuffer();

            int yuv_bytes_offset = 0;

            int y_plane_step = planes[0].getRowStride();
            if (y_plane_step == w) {
                y_plane.get(yuv_bytes, 0, w * h);
                yuv_bytes_offset = w * h;
            } else {
                int padding = y_plane_step - w;
                for (int i = 0; i < h; i++) {
                    y_plane.get(yuv_bytes, yuv_bytes_offset, w);
                    yuv_bytes_offset += w;
                    if (i < h - 1) {
                        y_plane.position(y_plane.position() + padding);
                    }
                }
                assert (yuv_bytes_offset == w * h);
            }

            int chromaRowStride = planes[1].getRowStride();
            int chromaRowPadding = chromaRowStride - w / 2;

            if (chromaRowPadding == 0) {
                // When the row stride of the chroma channels equals their width, we can copy
                // the entire channels in one go
                u_plane.get(yuv_bytes, yuv_bytes_offset, w * h / 4);
                yuv_bytes_offset += w * h / 4;
                v_plane.get(yuv_bytes, yuv_bytes_offset, w * h / 4);
            } else {
                // When not equal, we need to copy the channels row by row
                for (int i = 0; i < h / 2; i++) {
                    u_plane.get(yuv_bytes, yuv_bytes_offset, w / 2);
                    yuv_bytes_offset += w / 2;
                    if (i < h / 2 - 1) {
                        u_plane.position(u_plane.position() + chromaRowPadding);
                    }
                }
                for (int i = 0; i < h / 2; i++) {
                    v_plane.get(yuv_bytes, yuv_bytes_offset, w / 2);
                    yuv_bytes_offset += w / 2;
                    if (i < h / 2 - 1) {
                        v_plane.position(v_plane.position() + chromaRowPadding);
                    }
                }
            }

            Mat yuv_mat = new Mat(h + h / 2, w, CvType.CV_8UC1);
            yuv_mat.put(0, 0, yuv_bytes);
            Imgproc.cvtColor(yuv_mat, mRgba, Imgproc.COLOR_YUV2RGBA_I420, 4);
            return mRgba;
        }
    }


    public void rotation(int rotation, Mat mat) {
        //  旋转90°
        if (rotation == 90) {
            Core.transpose(mat, mat); // 将图像逆时针旋转90°，然后再关于x轴对称
            Core.flip(mat, mat, 1); // 然后再绕Y轴旋转180° （顺时针）
        } else if (rotation == 180) {
            Core.flip(mat, mat, 0); //将图片绕X轴旋转180°（顺时针）
            Core.flip(mat, mat, 1); //将图片绕Y轴旋转180°（顺时针）
        } else if (rotation == 270) {
            Core.transpose(mat, mat); // 将图像逆时针旋转90°，然后再关于x轴对称
            Core.flip(mat, mat, 0); // //将图片绕X轴旋转180°（顺时针）
        }
    }

    public void release() {
        mRgba.release();
        mGray.release();
    }

  public static    Mat convertYUVtoMat(@NonNull Image img) {
        byte[] nv21;

        ByteBuffer yBuffer = img.getPlanes()[0].getBuffer();
        ByteBuffer uBuffer = img.getPlanes()[1].getBuffer();
        ByteBuffer vBuffer = img.getPlanes()[2].getBuffer();

        int ySize = yBuffer.remaining();
        int uSize = uBuffer.remaining();
        int vSize = vBuffer.remaining();

        nv21 = new byte[ySize + uSize + vSize];

        yBuffer.get(nv21, 0, ySize);
        vBuffer.get(nv21, ySize, vSize);
        uBuffer.get(nv21, ySize + vSize, uSize);

        Mat yuv = new Mat(img.getHeight() + img.getHeight()/2, img.getWidth(), CvType.CV_8UC1);
        yuv.put(0, 0, nv21);
        Mat rgb = new Mat();
        Imgproc.cvtColor(yuv, rgb, Imgproc.COLOR_YUV2RGB_NV21, 3);
        Core.rotate(rgb, rgb, Core.ROTATE_90_CLOCKWISE);
        return  rgb;
    }
}
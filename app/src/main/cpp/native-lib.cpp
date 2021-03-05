#include <jni.h>
#include <string>
#include <opencv2/opencv.hpp>

using namespace cv;




extern "C" JNIEXPORT void JNICALL
Java_com_nexysquare_opencv4test_MainActivity_ConvertRGBtoGray(JNIEnv *env, jobject thiz,
                                                             jlong mat_addr_input,
                                                             jlong mat_addr_result) {


    Mat &matInput = *(Mat *)mat_addr_input;
    Mat &matResult = *(Mat *)mat_addr_result;

    cvtColor(matInput, matResult, COLOR_RGBA2GRAY);


}
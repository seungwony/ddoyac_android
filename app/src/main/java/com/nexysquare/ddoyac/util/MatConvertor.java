package com.nexysquare.ddoyac.util;

import android.util.Base64;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.opencv.core.Mat;

public class MatConvertor {
    private final static String TAG = "MatConvertor";
    public static String matToJson(Mat mat){
        JsonObject obj = new JsonObject();

        if(mat.isContinuous()){
            int cols = mat.cols();
            int rows = mat.rows();
            int elemSize = (int) mat.elemSize();

            byte[] data = new byte[cols * rows * elemSize];

            mat.get(0, 0, data);

            obj.addProperty("rows", mat.rows());
            obj.addProperty("cols", mat.cols());
            obj.addProperty("type", mat.type());

            // We cannot set binary data to a json object, so:
            // Encoding data byte array to Base64.
            String dataString = new String(Base64.encode(data, Base64.DEFAULT));

            obj.addProperty("data", dataString);

            Gson gson = new Gson();
            String json = gson.toJson(obj);

            return json;
        } else {
            Log.e(TAG, "Mat not continuous.");
        }
        return "{}";
    }


    public static Mat matFromJson(String json){

        JsonObject JsonObject = JsonParser.parseString(json).getAsJsonObject();

        int rows = JsonObject.get("rows").getAsInt();
        int cols = JsonObject.get("cols").getAsInt();
        int type = JsonObject.get("type").getAsInt();

        String dataString = JsonObject.get("data").getAsString();
        byte[] data = Base64.decode(dataString.getBytes(), Base64.DEFAULT);

        Mat mat = new Mat(rows, cols, type);
        mat.put(0, 0, data);

        return mat;
    }
    //  Serialization/deserialization utility
    public static String SerializeFromMat(Mat mat)
    {
        SerializedMat serializedMat = new SerializedMat();
        serializedMat.setType(mat.type());
        serializedMat.setRows(mat.rows());
        serializedMat.setCols(mat.cols());

        if (serializedMat.getType()==0||
                serializedMat.getType()==8||
                serializedMat.getType()==16||
                serializedMat.getType()==24)
        {
            serializedMat.setBytes(new byte[(int)(mat.total()*mat.elemSize())]);
            mat.get(0,0,serializedMat.bytes);
        }
        else if (serializedMat.getType()==1||
                serializedMat.getType()==9||
                serializedMat.getType()==17||
                serializedMat.getType()==25)
        {
            serializedMat.setBytes(new byte[(int)(mat.total()*mat.elemSize())]);
            mat.get(0,0,serializedMat.bytes);
        }
        else if (serializedMat.getType()==2||
                serializedMat.getType()==10||
                serializedMat.getType()==18||
                serializedMat.getType()==26)
        {
            serializedMat.setShorts(new short[(int)(mat.total()*mat.elemSize())]);
            mat.get(0,0,serializedMat.shorts);
        }
        else if (serializedMat.getType()==3||
                serializedMat.getType()==11||
                serializedMat.getType()==19||
                serializedMat.getType()==27)
        {
            serializedMat.setShorts(new short[(int)(mat.total()*mat.elemSize())]);
            mat.get(0,0,serializedMat.shorts);
        }
        else if (serializedMat.getType()==4||
                serializedMat.getType()==12||
                serializedMat.getType()==20||
                serializedMat.getType()==28)
        {
            serializedMat.setInts(new int[(int)(mat.total()*mat.elemSize())]);
            mat.get(0,0,serializedMat.ints);
        }
        else if (serializedMat.getType()==5||
                serializedMat.getType()==13||
                serializedMat.getType()==21||
                serializedMat.getType()==29)
        {
            serializedMat.setFloats(new float[(int)(mat.total()*mat.elemSize())]);
            mat.get(0,0,serializedMat.floats);
        }
        else if (serializedMat.getType()==6||
                serializedMat.getType()==14||
                serializedMat.getType()==22||
                serializedMat.getType()==30)
        {
            serializedMat.setDoubles(new double[(int)(mat.total()*mat.elemSize())]);
            mat.get(0,0,serializedMat.doubles);
        }

        Gson gson = new Gson();
        return gson.toJson(serializedMat);
    }

    public static Mat DeserializeToMat(String json)
    {
        Gson gson = new Gson();
        SerializedMat serializedMat = gson.fromJson(json, SerializedMat.class);
        Mat mat = new Mat(serializedMat.getRows(),serializedMat.getCols(),serializedMat.getType());

        if (serializedMat.getType()==0||
                serializedMat.getType()==8||
                serializedMat.getType()==16||
                serializedMat.getType()==24)
        {
            mat.put(0,0,serializedMat.getBytes());
        }
        else if (serializedMat.getType()==1||
                serializedMat.getType()==9||
                serializedMat.getType()==17||
                serializedMat.getType()==25)
        {
            mat.put(0,0,serializedMat.getBytes());
        }
        else if (serializedMat.getType()==2||
                serializedMat.getType()==10||
                serializedMat.getType()==18||
                serializedMat.getType()==26)
        {
            mat.put(0,0,serializedMat.getShorts());
        }
        else if (serializedMat.getType()==3||
                serializedMat.getType()==11||
                serializedMat.getType()==19||
                serializedMat.getType()==27)
        {
            mat.put(0,0,serializedMat.getShorts());
        }
        else if (serializedMat.getType()==4||
                serializedMat.getType()==12||
                serializedMat.getType()==20||
                serializedMat.getType()==28)
        {
            mat.put(0,0,serializedMat.getInts());
        }
        else if (serializedMat.getType()==5||
                serializedMat.getType()==13||
                serializedMat.getType()==21||
                serializedMat.getType()==29)
        {
            mat.put(0,0,serializedMat.getFloats());
        }
        else if (serializedMat.getType()==6||
                serializedMat.getType()==14||
                serializedMat.getType()==22||
                serializedMat.getType()==30)
        {
            mat.put(0,0,serializedMat.getDoubles());
        }

        return mat;
    }

    private static class SerializedMat
    {
        byte[] bytes;
        short[] shorts;
        int[] ints;
        float[] floats;
        double[] doubles;

        int type;
        int rows;
        int cols;

        byte[] getBytes()
        {
            return bytes;
        }

        void setBytes(byte[] bytes)
        {
            this.bytes = bytes;
        }

        short[] getShorts()
        {
            return shorts;
        }

        void setShorts(short[] shorts)
        {
            this.shorts = shorts;
        }

        int[] getInts()
        {
            return ints;
        }

        void setInts(int[] ints)
        {
            this.ints = ints;
        }

        float[] getFloats()
        {
            return floats;
        }

        void setFloats(float[] floats)
        {
            this.floats = floats;
        }

        double[] getDoubles()
        {
            return doubles;
        }

        void setDoubles(double[] doubles)
        {
            this.doubles = doubles;
        }

        int getType()
        {
            return type;
        }

        void setType(int type)
        {
            this.type = type;
        }

        int getRows()
        {
            return rows;
        }

        void setRows(int rows)
        {
            this.rows = rows;
        }

        int getCols()
        {
            return cols;
        }

        void setCols(int cols)
        {
            this.cols = cols;
        }

        SerializedMat()
        {
        }
    }

}

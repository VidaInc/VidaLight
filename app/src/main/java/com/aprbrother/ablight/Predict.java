package com.aprbrother.ablight;

/**
 * Created by Staple on 6/4/2015.
 */
public class Predict {
    private int[] rssi;
    private static int size = 3;
    private RoomData[] rooms;


    public Predict (int[] a){
        rssi = a;
        //Room 0; invCovariance, determinant, mean vector initialization.
        double[][] rm0m =  {{1,2,3},{1,2,3},{1,2,3}};
        int rm0d = 1;
        double[] rm0v = {1,2,3};
        rooms[0] = new RoomData(0, new Matrix(size,rm0m),rm0d, new Vec(size,rm0v) );
        //Room 1; invCovariance, determinant, mean vector initialization.
        double[][] rm1m =  {{1,2,3},{1,2,3},{1,2,3}};
        int rm1d = 1;
        double[] rm1v = {1,2,3};
        rooms[1] = new RoomData(1, new Matrix(size,rm1m),rm1d, new Vec(size,rm1v) );
        //Room 2; invCovariance, determinant, mean vector initialization.
        double[][] rm2m =  {{1,2,3},{1,2,3},{1,2,3}};
        int rm2d = 1;
        double[] rm2v = {1,2,3};
        rooms[2] = new RoomData(0, new Matrix(size,rm2m),rm2d, new Vec(size,rm2v) );
    }

    public int RoomPrediction(RoomData[] rooms){
        int room = 0;
        double comparator = Gaussian(rooms[0], this.rssi);
        for(int i=1; i<rooms.length; i++){
            double result = Gaussian(rooms[i],this.rssi);
            if(result > comparator){
                room = i;
                comparator = result;
            }
        }
        return room;
    }

    private double Gaussian (RoomData rm, int[] rssi){
        return 0;
//apply function.
    }

}

class RoomData{

    private final int roomNum;
    private final Matrix invCov;
    private final int determinant;
    private final Vec mean;

    public RoomData(int RN, Matrix a, int det, Vec v){
        this.roomNum = RN;
        this.invCov = a;
        this.determinant = det;
        this.mean = v;
    }

    public int getRoomNum() {
        return roomNum;
    }
    public Matrix getInvCov() {
        return invCov;
    }

    public int getDeterminant() {
        return determinant;
    }

    public Vec getMean() {
        return mean;
    }
}


class Matrix {
    private double[][] m;
    private int size;

//setter getter constructor
    public Matrix(int s) {
        this.m=new double[s][s];
        this.size = s;
    }

    public Matrix(int s, double[][] data) {
        this.m=new double[s][s];
        this.size = s;
        this.m = data;
    }

    public void setMat(int s, double[][] data){
        this.m = data;
    }

    public double getEntry(int ind1, int ind2){
        return m[ind1][ind2];
    }


    //operation required:  vec*mat*vec

    public double VbyMbyV(int size, Vec v1, Matrix m, Vec v2){
        double result = 0;
        double[] interm = new double[size];   //store interm value
        for(int i = 0; i<size;i++){
            for(int j=0;j<size;j++){
                interm[i] += (m.getEntry(j,i)*v1.getEntry(j));
            }
       }
        for (int i = 0;i<size;i++){
            result += (interm[i]*v2.getEntry(i));
        }
        return result;
    }
}


class Vec {
    private double[] v;
    private int size;


//setter getter constructor

    public Vec (int s){
        this.v= new double[s];
        this.size = s;
    }
    public Vec (int s, double[] data ){
        this.v = new double[s];
        this.setVec(s,data);
    }

    public void setVec(int size, double[] data){
        this.v = data;
    }

    public double getEntry(int index){
        return this.v[index];
    }


//operation required: substraction
    public Vec substraction(int size, Vec subBy){
        Vec result = new Vec(size );
        double[] data = new double[size];
        for (int i=0; i<size;i++){
            data[i]=this.v[i] - subBy.v[i];
        }
        result.setVec(size, data);
        return result;
    }



}
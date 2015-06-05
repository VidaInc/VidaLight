package com.aprbrother.ablight;


        public class Predict {
            private double[] rssi;
            private int size = 3;
            private RoomData[] rooms = new RoomData[this.size];


            public Predict (double[] a){
                rssi = a;
                //Room 0; invCovariance, determinant, mean vector initialization.
                double[][] rm0m =  {{1,2,3},{1,2,3},{1,2,3}};
                int rm0d = 1;
                double[] rm0v = {1,2,3};
                this.rooms[0] = new RoomData(0, new Matrix(size,rm0m),rm0d, new Vec(size,rm0v) );
                //Room 1; invCovariance, determinant, mean vector initialization.
                double[][] rm1m =  {{1,2,3},{1,2,3},{1,2,3}};
                int rm1d = 1;
                double[] rm1v = {1,2,3};
                this.rooms[1] = new RoomData(1, new Matrix(size,rm1m),rm1d, new Vec(size,rm1v) );
                //Room 2; invCovariance, determinant, mean vector initialization.
                double[][] rm2m =  {{1,2,3},{1,2,3},{1,2,3}};
                int rm2d = 1;
                double[] rm2v = {1,2,3};
                this.rooms[2] = new RoomData(0, new Matrix(size,rm2m),rm2d, new Vec(size,rm2v) );
            }

            public int RoomPrediction(){
                int room = 0;
                Vec rs = new Vec(this.size, this.rssi);
                double comparator = Gaussian(this.rooms[0], rs);
                for(int i=1; i<this.rooms.length; i++){
                    double result = Gaussian(this.rooms[i],rs);
                    if(result > comparator){
                        room = this.rooms[i].getRoomNum();
                        comparator = result;
                    }
                }
                return room;
            }
            //apply function.
            private double Gaussian (RoomData rm, Vec rssi) {
                double result;
                double constant1 = Math.pow(0.111, (this.size * -0.5));
                Vec x_u = rssi.subtraction(rm.getMean());
                double exp_scalar = -0.5 * this.VbyMbyV(this.size, x_u, rm.getInvCov(), x_u);
                result = Math.exp(exp_scalar) * constant1 * rm.getDeterminant();
                return result;
            }

                //operation required:  vec*mat*vec

            public double VbyMbyV(int size, Vec v1, Matrix m, Vec v2){
                double result = 0;
                double[] inter_value = new double[size];   //store interm value
                for(int i = 0; i<size;i++){
                    for(int j=0;j<size;j++){
                        inter_value[i] += (m.getEntry(j,i)*v1.getEntry(j));
                    }
                }
                for (int i = 0;i<size;i++){
                    result += (inter_value[i]*v2.getEntry(i));
                }
                return result;
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
//            private int size;

    //setter getter constructor
//            public Matrix(int s) {
//                this.m=new double[s][s];
//                this.size = s;
//            }

    public Matrix(int s, double[][] data) {
        this.m=new double[s][s];
//                this.size = s;
        this.m = data;
    }

//            public void setMat(int s, double[][] data){
//                this.m = data;
//            }

    public double getEntry(int ind1, int ind2){
        return m[ind1][ind2];
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
        this.setVec(data);
    }

    public void setVec(double[] data){
        this.v = data;
    }

    public double getEntry(int index){
        return this.v[index];
    }


    //operation required: subtraction
    public Vec subtraction( Vec subBy){
        Vec result = new Vec(this.size );
        double[] data = new double[size];
        for (int i=0; i<size;i++){
            data[i]=this.v[i] - subBy.v[i];
        }
        result.setVec(data);
        return result;
    }



}


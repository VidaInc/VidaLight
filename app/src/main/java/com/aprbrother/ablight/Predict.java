package com.aprbrother.ablight;


    public class Predict {
        /*
    //Testing function
        public static void main(String[] args) {
            double[][] rs_input = { { -76, -74, -82 }, { -76, -76, -98 },
                    { -84, -68, -91 }, { -81, -74, -93 }, { -60, -67, -88 },
                    { -69, -61, -75 }, { -73, -55, -79 }, { -79, -76, -84 },
                    { -81, -81, -81 }, { -79, -73, -77 }, { -85, -79, -84 },
                    { -70, -75, -72 }, { -72, -75, -71 }, { -68, -73, -72 },
                    { -62, -70, -83 }, { -64, -74, -82 }, { -63, -80, -79 },
                    { -69, -70, -87 }, { -56, -82, -83 }, { -77, -83, -88 },
                    { -67, -81, -78 } };
            double[] results = {1.63428662e-04, 8.68039098e-06, 9.16788650e-05, 8.92326031e-05
                    , 5.79290679e-07, 2.98266324e-05, 2.65943326e-05, 1.64803858e-04
                    , 3.95264821e-05, 5.28570143e-05, 5.48630049e-05, 9.52266087e-06
                    , 6.36092687e-06, 1.07121038e-05, 7.68134346e-06, 1.84079716e-05
                    , 6.92677770e-06, 4.41128023e-05, 2.30005724e-07, 7.81264450e-05
                    , 1.40080506e-05};
            double sum = 0; int i =0;
            for(double[] rssi : rs_input){
                //how to predict by pass in double[] rssi
                Predict p = new Predict(rssi);
                RoomData rm = p.RoomPrediction();
                System.out.println("Predicted minorID"+rm.getMinor());
            }
        }
        //end of testing code
*/

        private double[] rssi;
        private int size = 3;
        private int numofroom = 3;
        private RoomData[] rooms = new RoomData[this.numofroom];
        private static RoomData current_room;

        public RoomData getCurrent_room() {
            return current_room;
        }

        //Need to change constructor into objects with minor id and rssi.
        public Predict(double[] a) {
            rssi = a;
            //Room 0; invCovariance, determinant, mean vector initialization.
            double[][] rm0m =
                    {{ 0.01813697,0.00659309, -0.00403546},
                            { 0.00659309,0.0230475, 0.00425336},
                            {-0.00403546,0.00425336,0.0395935 }};
            double rm0d = 268.223868385;
            double[] rm0v = {-70.03508772, -80.43859649, -78.29824561};
            this.rooms[0] = new RoomData(0, "B4:99:4C:89:72:9E", new Matrix(size, rm0m), rm0d, new Vec(size, rm0v));
//Room 1; invCovariance, determinant, mean vector initialization.
            double[][] rm1m = {{ 0.03012743,0.00190745, -0.00516787},
                    { 0.00190745,0.01646801,0.00071672},
                    {-0.00516787,0.00071672,0.02978893}};
            double rm1d = 265.357063883;
            double[] rm1v =
                    {-80.43478261, -67.65217391, -82.19565217};
            this.rooms[1] = new RoomData(0, "B4:99:4C:89:72:9E", new Matrix(size, rm1m), rm1d, new Vec(size, rm1v));
//Room 2; invCovariance, determinant, mean vector initialization.
            double[][] rm2m =
                    {{1.77290576e-02, 7.12135305e-04, 2.96985539e-03},
                            {7.12135305e-04, 4.73639410e-02, 3.91607048e-05},
                            {2.96985539e-03, 3.91607048e-05, 1.99390916e-02}};
            double rm2d = 247.57109853;
            double[] rm2v ={-76.02325581, -87.11627907, -66.65116279};
            this.rooms[2] = new RoomData(1, "B4:99:4C:89:72:9E", new Matrix(size, rm2m), rm2d, new Vec(size, rm2v));
        }


        public RoomData RoomPrediction() {
            RoomData room = this.rooms[0];
            Vec rs = new Vec(this.size, this.rssi);
            double comparator = Gaussian(this.rooms[0], rs);;

	        for (int i = 0; i < this.rooms.length; i++) {
	            double result = Gaussian(this.rooms[i], rs);
	            if (result > comparator) {
	                room = this.rooms[i];
	                comparator = result;
                    this.current_room = room;
                }
	        }
            return room;
        }

        //apply function.
        private double Gaussian(RoomData rm, Vec rssi) {
            double result;
            double constant1 = Math.pow(6.2831853, (this.size * -0.5));
            Vec x_u = rssi.subtraction(rm.getMean());
            double exp_scalar = -0.5 * this.VbyMbyV(this.size, x_u, rm.getInvCov(), x_u);
            result = Math.exp(exp_scalar) * constant1 / rm.getDeterminant();
//	        System.out.print("/exp:"+Math.exp(exp_scalar));
//	        System.out.print("/const:"+constant1);
//	        System.out.println("/det:"+rm.getDeterminant());

            return result;
        }

        //operation required:  vec*mat*vec

        public double VbyMbyV(int size, Vec v1, Matrix m, Vec v2) {
            double result = 0;
            double[] inter_value = new double[size];   //store interm value
            for (int i = 0; i < size; i++) {
                for (int j = 0; j < size; j++) {
                    inter_value[i] += (m.getEntry(j, i) * v1.getEntry(j));
                }
            }
            for (int i = 0; i < size; i++) {
                result += (inter_value[i] * v2.getEntry(i));
            }
            return result;
        }
    }

    class RoomData {

        private final int majorID;
        private final String minorID;
        private final Matrix invCov;
        private final double determinant;
        private final Vec mean;

        public RoomData(int maj, String min, Matrix a, double rm0d, Vec v) {
            this.majorID = maj;
            this.minorID = min;
            this.invCov = a;
            this.determinant = rm0d;
            this.mean = v;
        }

        public int getMajor() {
            return majorID;
        }

        public String getMinor(){
            return minorID;
        }

        public Matrix getInvCov() {
            return invCov;
        }

        public double getDeterminant() {
            return determinant;
        }

        public Vec getMean() {
            return mean;
        }
    }


    class Matrix {
        private double[][] m;
//	            private int size;

        //setter getter constructor
//	            public Matrix(int s) {
//	                this.m=new double[s][s];
//	                this.size = s;
//	            }

        public Matrix(int s, double[][] data) {
            this.m = new double[s][s];
//	                this.size = s;
            this.m = data;
        }

//	            public void setMat(int s, double[][] data){
//	                this.m = data;
//	            }

        public double getEntry(int ind1, int ind2) {
            return m[ind1][ind2];
        }


    }


    class Vec {
        private double[] v;
        private int size;


        //setter getter constructor

        public Vec(int s) {
            this.v = new double[s];
            this.size = s;
        }

        public Vec(int s, double[] data) {
            this.v = new double[s];
            this.size = s;
            this.setVec(data);
        }

        public void setVec(double[] data) {
            this.v = data;
        }

        public double getEntry(int index) {
            return this.v[index];
        }


        //operation required: subtraction
        public Vec subtraction(Vec subBy) {
            Vec result = new Vec(this.size);
            double[] data = new double[size];
            for (int i = 0; i < size; i++) {
                data[i] = this.v[i] - subBy.v[i];
            }
            result.setVec(data);
            return result;
        }


    }





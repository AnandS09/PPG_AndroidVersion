package com.myapps.myopencv_try1;

/**
 * Created by asamajda on 09-Feb-16.
 */
import java.util.Arrays;

public class fftLib{

    public static double[][] butterfly(double a_r, double a_i, double b_r, double b_i){
        double[][] arr_out= {{0,0},{0,0}};

        arr_out[0][0] = a_r + b_r;
        arr_out[0][1] = a_i + b_i;
        arr_out[1][0] = a_r - b_r;
        arr_out[1][1] = a_i - b_i;
        return arr_out;
    }

    public static double[][] butterfly_4pt(double a_in[][]){
        double [][] arr_out= {{0,0},{0,0},{0,0},{0,0}};

        int i = 0;
        while (i<2)
        {
            double tmp[][] = butterfly(a_in[i][0], a_in[i][1], a_in[i+2][0], a_in[i+2][1]);

            arr_out[i][0] = tmp[0][0];
            arr_out[i][1] = tmp[0][1];
            arr_out[i+2][0] = tmp[1][0];
            arr_out[i+2][1] = tmp[1][1];

            i++;
        }
        return arr_out;
    }

    public static double[][] butterfly_Npt(double a_in[][], int n){
        double [][] arr_out= new double[n][2];

        if((n%2) != 0){
            System.err.println("ERROR : n should be in powers of 2");
            double ret_arr[][] = {{0.0,0.0}};
            return ret_arr;
        }

        int i = 0;
        int k = n/2;
        while (i<k)
        {
            double tmp[][] = butterfly(a_in[i][0], a_in[i][1], a_in[i+k][0], a_in[i+k][1]);

            arr_out[i][0] = tmp[0][0];
            arr_out[i][1] = tmp[0][1];
            arr_out[i+k][0] = tmp[1][0];
            arr_out[i+k][1] = tmp[1][1];

            i++;
        }
        return arr_out;
    }

    public static double[][] spatial_decimation(double a_in[][], int n_points){
        if((n_points%2) != 0){
            System.err.println("ERROR : bits should be in powers of 2");
            double ret_arr[][] = {{0.0,0.0}};
            return ret_arr;
        }

        double a_out[][] = new double[n_points][2];
        int bits =(int) ( Math.log((double) n_points) / Math.log(2.0));

        for(int i = 0 ; i < n_points ; i++){
            int j = getBitReversed(i, bits);

            a_out[i][0] = a_in[j][0];
            a_out[i][1] = a_in[j][1];
        }

        return a_out;
    }

    public static int getBitReversed(int num, int bits){
        int exp = bits - 1;
        int val = 0;
        while (exp >= 0){
            val += num%2 * Math.pow(2.0, (double) exp);
            num = (int)num/2;
            exp--;
        }

        return val;
    }

    public static double[] complex_mult(double re_a, double re_b, double im_a, double im_b){
        double [] res = {0.0, 0.0};

        res[0] = (re_a * re_b) - (im_a * im_b);
        res[1] = (re_a * im_b) + (re_b * im_a);

        return res;
    }

    public static double[] nth_root_unity(int n, int N){
        double [] res = {0.0, 0.0};
        double theta = 6.28 / N;
        double omega = n * theta;

        res[0] = Math.cos(omega);
        res[1] = Math.sin(omega);

        return res;
    }

    public static double[][] fft(double[][] x_samples, int points){
        double [][] s_points = new double[points][2];
        double [][] x_samples_internal = new double[points][2];
        x_samples_internal = spatial_decimation(x_samples, points);

        int stages = (int) ( Math.log((double) points) / Math.log(2.0));
        int ctr = 1;

        while (ctr <= stages){
            int fact = (int) Math.pow(2, ctr-1);

            for(int i = 0; i < points/2 ; i ++){
                double a_r, a_i, b_r, b_i;
                double [][] temp = new double[1][2];
                double [][] temp_out = new double[2][2];

                int j = 2*i - i % (fact);

                a_r = x_samples_internal[j][0];
                a_i = x_samples_internal[j][1];

                b_r = x_samples_internal[j + fact][0];
                b_i = x_samples_internal[j + fact][1];

                //Check the validity of this function
                int n = j % (fact);
                temp[0] = nth_root_unity(n, fact*2);

                temp[0] = complex_mult(b_r, temp[0][0], b_i, temp[0][1]);

                b_r = temp[0][0];
                b_i = temp[0][1];

                temp_out = butterfly(a_r, a_i, b_r, b_i);

                x_samples_internal[j] 	  = temp_out[0];
                x_samples_internal[j+fact] = temp_out[1];

            }
            ctr++;

        }
        s_points = x_samples_internal;
        return s_points;
    }

    public static double[] fft_energy_squared(double[][] x_samples, int points){
        double [] energy = new double[points];
        double [][] s_points = new double[points][2];
        double [][] x_samples_internal = new double[points][2];
        x_samples_internal = spatial_decimation(x_samples, points);

        int stages = (int) ( Math.log((double) points) / Math.log(2.0));
        int ctr = 1;

        while (ctr <= stages){
            int fact = (int) Math.pow(2, ctr-1);

            for(int i = 0; i < points/2 ; i ++){
                double a_r, a_i, b_r, b_i;
                double [][] temp = new double[1][2];
                double [][] temp_out = new double[2][2];

                int j = 2*i - i % (fact);

                a_r = x_samples_internal[j][0];
                a_i = x_samples_internal[j][1];

                b_r = x_samples_internal[j + fact][0];
                b_i = x_samples_internal[j + fact][1];

                //Check the validity of this function
                int n = j % (fact);
                temp[0] = nth_root_unity(n, fact*2);

                temp[0] = complex_mult(b_r, temp[0][0], b_i, temp[0][1]);

                b_r = temp[0][0];
                b_i = temp[0][1];

                temp_out = butterfly(a_r, a_i, b_r, b_i);

                x_samples_internal[j] 	  = temp_out[0];
                x_samples_internal[j+fact] = temp_out[1];

                if(stages == ctr){
                    energy[j] = Math.pow(x_samples_internal[j][0], 2) + Math.pow(x_samples_internal[j][1], 2);
                    energy[j+fact] = Math.pow(x_samples_internal[j+fact][0], 2) + Math.pow(x_samples_internal[j+fact][1], 2);
                }
            }
            ctr++;

        }
        s_points = x_samples_internal;
        return energy;
    }


}

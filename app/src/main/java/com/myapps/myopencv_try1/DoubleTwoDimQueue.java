package com.myapps.myopencv_try1;

import java.util.Stack;

/**
 * Created by asamajda on 09-Feb-16.
 */
public class DoubleTwoDimQueue {
    private Stack<double [][]> queue;
    private int myQsize;

    public DoubleTwoDimQueue(){
        queue = new Stack<double [][]>();
    }

    public void Qpush(double [][]elem){
        //The local_elem variable is very important here.
        //If directly elem is used, Java links it to the original variable used in the function call
        //Thus changing that variable in the program elsewhere changes the contents in the queue.
        //I need to search web for more info about why this happens.
        double [][] local_elem = new double[1][2];

        local_elem[0][0] = elem[0][0];
        local_elem[0][1] = elem[0][1];

        queue.push(local_elem);
        myQsize = queue.size();
    }

    public double [][] Qtake(){
        double [][] pop = new double[1][2];
        pop = queue.get(0);
        queue.remove(0);
        myQsize = queue.size();

        return pop;
    }

    public double [][] Qpeek(int index){

        if(index > queue.size()){
            System.err.println("ERROR : Invalid index, Out of Bounds");
            return null;
        }

        double [][] pop = new double[1][2];
        pop = queue.get(index);

        return pop;
    }

    public double [][] toArray(){
        return  toArray(0,myQsize - 1);
    }

    public double [][] toArray(int start, int end){
        int size = end - start;
        double [][] arr =new double[size][2];

        for(int i = start; i < size; i++){
            double [][] peek = new double[1][2];
            peek = queue.get(i);

            arr[i][0] = peek[0][0];
            arr[i][1] = peek[0][1];
        }

        return arr;
    }

    public double [] toArray(int start, int end, int index){
        int size = end - start;
        double [] arr =new double[size];

        for(int i = start; i < size; i++){
            double [][] peek = new double[1][2];
            peek = queue.get(i);

            arr[i] = peek[0][index];

        }

        return arr;
    }

    public int getQSize(){
        return myQsize;
    }
}

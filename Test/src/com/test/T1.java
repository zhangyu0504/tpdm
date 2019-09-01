package com.test;

import java.util.Scanner;

public class T1 {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		int a =10,b=15;
		System.out.println("a:"+a+",b:"+b);
		
//		a=a-b;
//		b=a+b;
//		a=b-a;
//		a=a+b;
//		b=a-b;
//		a=a-b;
		a=a^b; 
		b=a^b;
		a=a^b; 
		System.out.println("a:"+a+",b:"+b);
		float q=10.2f/2f;
//		Scanner in =new Scanner(System.in);
//		in.next();//接收String输入
//		in.nextInt();//接收int
int score =85;
System.out.println(score>=90?"A":score>84?"B":"D");
switch(a){
	case(10): System.out.println("122112121");
	break;
	case(11): System.out.println("eeeeee");
	break;
	default:
		 System.out.println("22222");
}
	
	}

}

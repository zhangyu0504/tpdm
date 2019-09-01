package com.test;

import java.util.Arrays;

public class T2 {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		// 求1-10之前偶数和
		int sum = 0;
		int i = 1;
		while (i <= 10) {
			if (i % 2 == 0) {
				sum = sum + i;
			}
			i++;
		}
		System.out.println(sum);

		int arry[] = new int[]{ 10, 3, 9, 7, 15 };
		for (int b : arry) {
			System.out.print(b+ " ");
		}
		System.out.println();
		int arr[] = { 2, 5, 3 };
		System.out.println("-----排序前的一维数组：");
		for (int x : arr) {
			System.out.print(x + " ");// foreach循环逐个输入数组元素的值
		}
		System.out.println();

		// 数组冒泡排序
		Arrays.sort(arr);
		// foreach输出排序之后的数组元素
		System.out.println("-----排序后的一维数组：");
		for (int x : arr) {
			System.out.print(x + " ");
		}
		System.out.println();
	}
}

package com.test;

import java.util.Arrays;

public class T2 {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		// ��1-10֮ǰż����
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
		System.out.println("-----����ǰ��һά���飺");
		for (int x : arr) {
			System.out.print(x + " ");// foreachѭ�������������Ԫ�ص�ֵ
		}
		System.out.println();

		// ����ð������
		Arrays.sort(arr);
		// foreach�������֮�������Ԫ��
		System.out.println("-----������һά���飺");
		for (int x : arr) {
			System.out.print(x + " ");
		}
		System.out.println();
	}
}

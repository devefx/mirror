package org.devefx.mirror.test;

public class Test6 {
	public static void main(String[] args) {
		// 你当前的积分
		int currentScore = 122;
		int currentLv = 0;
		int nextScore = 0;	// 到下一级所需积分
		
		// 积分等级列表，从1级开始
		int[] scoreLv = {50, 100, 200, 350, 550, 800};
		int maxLv = scoreLv.length;
		
		// 计算积分等级
		for (int i = maxLv; i > 0; i--) {
			if (currentScore >= scoreLv[i-1]) {
				currentLv = i;
				// 没有满级的情况下，计算下一级所需积分
				if (i != maxLv) {
					nextScore = scoreLv[i] - currentScore;
				}
				break;
			}
		}
		
		System.out.println("当前积分：" + currentScore);
		System.out.println("当前等级：" + currentLv);
		System.out.println("升级所需积分：" + nextScore);
	}
}

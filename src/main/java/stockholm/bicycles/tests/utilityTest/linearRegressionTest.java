package stockholm.bicycles.tests.utilityTest;

import stockholm.bicycles.utility.LinearRegrerssion;

public class linearRegressionTest {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		double[] x= {-1,-2,-3,-4,-5};
		double[] y= {1,2,3,4,5};
		
		LinearRegrerssion lmTest= new LinearRegrerssion(x,y);
		System.out.println(lmTest.toString());
		
		double test=Math.atan(0);
		System.out.println("before: "+test/Math.PI*180);
		test=test+Math.PI;
		
		
		System.out.println("after: "+test/Math.PI*180);
		
		double test2=Math.atan(-1);
		double test3=test2+Math.PI*2;
		System.out.println("before: "+test3/Math.PI*180);
		test3=test2+Math.PI;
		
		
		System.out.println("after: "+test3/Math.PI*180);
	}

}

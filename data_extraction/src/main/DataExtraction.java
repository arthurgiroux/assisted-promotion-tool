package main;

import java.util.Arrays;

import common.DBHelper;
import echonest.EchoNest;

public class DataExtraction {

	public static void main(String args[]) {
		if (args.length > 0 && args[0] != null && args[0].equals("empty")){
			System.out.println("Emptying database");
			DBHelper dbHelper = DBHelper.getInstance();
			dbHelper.emptyAll();
		}
		// EchoNest + Freebase phase:
		EchoNest echonest = new EchoNest();
		
		echonest.run();
	}
}

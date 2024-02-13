package dbfileorga;

public class StartMitgliederDB {

	public static void main(String[] args) {
			MitgliederDB db = new MitgliederDB(false);
			System.out.println(db);
			
			//  test implementation with the following use cases
			
			// read the a record number e.g. 32 (86;3;13;Brutt;Jasmin;12.12.04;01.01.16;;7,5)
			Record rec;
			//db.read(32);
//			System.out.println(rec);
			
			//find and read a record with a given Mitgliedesnummer e.g 95
			//rec = db.read(db.findPos("121"));
			//int recNum = db.findPos("121");

			//System.out.println(recNum);
			//System.out.println(rec);

//			//insert Hans Meier
			//int newRecNum = db.insert(new Record("122;2;44;Meier;Hans;07.05.01;01.03.10;120;15"));
			//System.out.println(db.read(newRecNum));
		    //System.out.println(newRecNum);
			//System.out.println(db);

			//modify (ID95 Steffi Brahms wird zu ID 95 Steffi Bach)
			db.modify(db.findPos("95"), new Record("95;3;13;Bach;Steffi;17.08.08;01.01.17;;2,5"));
			db.modify(db.findPos("125"), new Record("125;3;13;Mahnisch;Wilfried;17.08.08;01.01.17;;2,5"));

			db.delete(db.findPos("57"));
			//db.insert(new Record("125;2;44;Ficker;Hans;07.05.01;01.03.10;120;15"), false);
			//db.insert(new Record("125;2;44;Ficker;Hans;07.05.01;01.03.10;120;15"), false);

		System.out.println(db);
//
//			//delete the record with Mitgliedsnummer 95
			//db.delete(db.findPos("95"));
			//int newRecNum = db.insert(new Record("122;2;44;Meier;Hans;07.05.01;01.03.10;120;15"), false);
			//System.out.println(db);

//			int newRecNum2 = db.insert(new Record("187;2;44;Frenz;Hans;07.05.01;01.03.10;120;15"));
//			System.out.println(db);


	}

}

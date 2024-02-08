package dbfileorga;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class MitgliederDB implements Iterable<Record>
{
	
	protected DBBlock db[] = new DBBlock[8];
	
	
	public MitgliederDB(boolean ordered){
		this();
		insertMitgliederIntoDB(ordered);
		
	}
	public MitgliederDB(){

		initDB();
	}
	
	private void initDB() {
		for (int i = 0; i<db.length; ++i){
			db[i]= new DBBlock();
		}
		
	}
	private void insertMitgliederIntoDB(boolean ordered) {
		MitgliederTableAsArray mitglieder = new MitgliederTableAsArray();
		String mitgliederDatasets[];
		if (ordered){
			mitgliederDatasets = mitglieder.recordsOrdered;
		}else{
			mitgliederDatasets = mitglieder.records;
		}
		for (String currRecord : mitgliederDatasets ){
			appendRecord(new Record(currRecord));
		}	
	}

		
	protected int appendRecord(Record record){
		//search for block where the record should be appended
		int currBlock = getBlockNumOfRecord(getNumberOfRecords());
		int result = db[currBlock].insertRecordAtTheEnd(record);
		if (result != -1 ){ //insert was successful
			return result;
		}else if (currBlock < db.length) { // overflow => insert the record into the next block
			return db[currBlock+1].insertRecordAtTheEnd(record);
		}
		return -1;
	}
	

	@Override
	public String toString(){
		String result = new String();
		for (int i = 0; i< db.length ; ++i){
			result += "Block "+i+"\n";
			result += db[i].toString();
			result += "-------------------------------------------------------------------------------------\n";
		}
		return result;
	}
	
	/**
	 * Returns the number of Records in the Database
	 * @return number of records stored in the database
	 */
	public int getNumberOfRecords(){
		int result = 0;
		for (DBBlock currBlock: db){
			result += currBlock.getNumberOfRecords();
		}
		return result;
	}
	
	/**
	 * Returns the block number of the given record number 
	 * @param recNum the record number to search for
	 * @return the block number or -1 if record is not found
	 */
	public int getBlockNumOfRecord(int recNum){
		int recCounter = 0;
		for (int i = 0; i< db.length; ++i){
			if (recNum <= (recCounter+db[i].getNumberOfRecords())){
				return i ;
			}else{
				recCounter += db[i].getNumberOfRecords();
			}
		}
		return -1;
	}
		
	public DBBlock getBlock(int i){

		return db[i];
	}
	
	
	/**
	 * Returns the record matching the record number
	 * @param recNum the term to search for
	 * @return the record matching the search term
	 */
	public Record read(int recNum){

		int recordCounter = 0;

		for (int i = 0; i < db.length; i++) { //gehe die ganzen Blöcke der DB durch

			if (recNum <= (recordCounter + db[i].getNumberOfRecords())){
				//Vergleich der recordNumber mit dem Zähler der Records

				return db[i].getRecord(recNum - recordCounter);
				//Return des Records verringert durch unseren Counter um den genaun Record aus dem Block zu bekommen,
				// weil Records innerhalb eines Blocks mit 1-5 nummeriert sind durch Methode getRecord()
			}
			else {
				//record nicht gefunden, also erhöhen wir unseren RecordCounter
				recordCounter += db[i].getNumberOfRecords();
			}
		}

		return null;
	}
	
	/**
	 * Returns the number of the first record that matches the search term
	 * @param searchTerm the term to search for
	 * @return the number of the record in the DB -1 if not found
	 */

	//searchTerm ist die Mitgliedsnummer, das 1.Attribut in einem Record
	public int findPos(String searchTerm){

		int recordCounter = 1;

		for (int i = 0; i < db.length; i++) { //gehe die ganzen Blöcke der DB durch

			DBBlock currBlock = db[i];

			Iterator<Record> recordIterator = currBlock.iterator();

			while(recordIterator.hasNext()){

				if(searchTerm.equals(recordIterator.next().getAttribute(1))) {
					System.out.println("ich bin hier rein gekommen");
					return recordCounter ;
				}
				else{

					recordCounter++;
				}

			}

		}
			return -1;
	}
	
	/**
	 * Inserts the record into the file and returns the record number
	 * @param record
	 * @return the record number of the inserted record
	 */
	public int insert(Record record){

		int recordCounter = 1;

		for (int i = 0; i < db.length; i++) { // gehe die ganzen Blöcke der DB durch

			DBBlock currBlock = db[i];

			int insertSuccess = currBlock.insertRecordAtTheEnd(record);

			if (insertSuccess == -1) {

				//System.out.println("Block ist voll");
				recordCounter += currBlock.getNumberOfRecords();
			} else {

				return recordCounter;
			}
		} return -1;
	}
	
	/**
	 * Deletes the record specified 
	 * @param numRecord number of the record to be deleted
	 */
	//numRecord ist Mitgliedsnummer
	public void delete(int numRecord){

		int recordCounter = 1;

		for (int i = 0; i < db.length; i++) { // gehe die ganzen Blöcke der DB durch

				DBBlock currBlock = db[i];

			if(numRecord <= (recordCounter + db[i].getNumberOfRecords())){

				currBlock.deleteRecord(numRecord-recordCounter);
				System.out.println("Habs gelöscht bro");
				break;
			}
			else{
				recordCounter += db[i].getNumberOfRecords();
			}

		}
	}
	
	/**
	 * Replaces the record at the specified position with the given one.
	 * @param numRecord the position of the old record in the db
	 * @param record the new record
	 * 
	 */
	//numRecord = Datensatznummer; record = geänderter Datensatz
	public void modify(int numRecord, Record record){
		//Datensatz suchen
		//Datensatz nicht vorhanden
		if(numRecord == -1){
			System.out.println("Record not modifiable");
		}
		int recordCounter = 1;
		for (int i = 0; i < db.length; i++) { // gehe die ganzen Blöcke der DB durch
			DBBlock currBlock = db[i];

			if(numRecord <= (recordCounter + db[i].getNumberOfRecords())){
				delete(numRecord);
				System.out.println("yo");
				currBlock.insertRecordAtTheEnd(record);
				//insert(record);
				System.out.println("hab inserted bre");
			}
			else{
				recordCounter += db[i].getNumberOfRecords();
			}
		}
	}

	
	@Override
	public Iterator<Record> iterator() {
		return new DBIterator();
	}
 
	private class DBIterator implements Iterator<Record> {

		    private int currBlock = 0;
		    private Iterator<Record> currBlockIter= db[currBlock].iterator();
	 
	        public boolean hasNext() {
	            if (currBlockIter.hasNext()){
	                return true; 
	            } else if (currBlock < (db.length-1)) { //continue search in the next block
	                return db[currBlock+1].iterator().hasNext();
	            }else{ 
	                return false;
	            }
	        }
	 
	        public Record next() {	        	
	        	if (currBlockIter.hasNext()){
	        		return currBlockIter.next();
	        	}else if (currBlock < db.length){ //continue search in the next block
	        		currBlockIter= db[++currBlock].iterator();
	        		return currBlockIter.next();
	        	}else{
	        		throw new NoSuchElementException();
	        	}
	        }
	 
	        @Override
	        public void remove() {

				throw new UnsupportedOperationException();
	        }
	    } 
	 

}

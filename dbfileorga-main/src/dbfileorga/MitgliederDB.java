package dbfileorga;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.*;

public class MitgliederDB implements Iterable<Record>
{

	protected DBBlock db[] = new DBBlock[8];
	private boolean sorted;

	public MitgliederDB(boolean sorted){
		this();
		this.sorted = sorted;
		insertMitgliederIntoDB(sorted);

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
			//iteriere durch den Block und schaue jedem Records Mitgliedsnummer an (Attribut 1)
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
		if(checkDuplicate(record)) {
			if (sorted) {

				List<Record> recordList = new ArrayList<>();


				for (DBBlock dbBlock : db) {
					int numberOfRecords = dbBlock.getNumberOfRecords();
					for (int i = 1; i <= numberOfRecords; i++) {
						//füge alle Records in die recordList hinzu
						recordList.add(dbBlock.getRecord(i));
					}
				}
				recordList.add(record);


				Collections.sort(recordList, new Comparator<Record>() {
					@Override
					public int compare(Record r1, Record r2) {
						int membNumR1 = Integer.parseInt(r1.getAttribute(1));
						int membNumR2 = Integer.parseInt(r2.getAttribute(1));
						return Integer.compare(membNumR1, membNumR2);
					}
				});

				int insertPosition = -1;
				for (int i = 0; i < recordList.size(); i++) {
					if (record.equals(recordList.get(i))) {
						insertPosition = i + 1;
						break;
					}
				}
				deleteDB();

				int currBlock = 0;
				for (Record sortedRecord : recordList) {
					int result = db[currBlock].insertRecordAtTheEnd(sortedRecord);
					if (result == -1) {
						currBlock++;
						if (currBlock < db.length) {
							db[currBlock].insertRecordAtTheEnd(sortedRecord);
						} else {
							System.out.println("Einfügen gefailt - DB voll");
							return -1;
						}
					}
				}

				return insertPosition;

			} else {
				int recNum = 0;
				for (DBBlock dbBlock : db) {
					recNum += dbBlock.getNumberOfRecords();
					int result = dbBlock.insertRecordAtTheEnd(record);
					if (result != -1) {
						return recNum + 1;
					}
				}
				return -1;
			}
		}
		System.out.println("Mitgliedsnummer schon vergeben");
		return -1;
	}


	/**
	 * Deletes the record specified
	 * @param numRecord number of the record to be deleted
	 */
	//numRecord ist Mitgliedsnummer
	public void delete(int numRecord){

		int recordCounter = 1;
		boolean found = false;

		for (int i = 0; i < db.length; i++) {
			//Gehe durch alle Blöcke durch

			DBBlock currBlock = db[i];
			int recordsInBlock = currBlock.getNumberOfRecords();
			if (numRecord <= (recordCounter + recordsInBlock)) {
				int recordIndex = numRecord - recordCounter;
				currBlock.deleteRecord(recordIndex);
				found = true;
				//Delete, wenn die Db unsortiert vorliegt
				if (!sorted) {
					for (int j = i + 1; j < db.length; j++) {
						DBBlock nextBlock = db[j];
						Record firstRecordOfNextBlock = nextBlock.getRecord(1);
						if (firstRecordOfNextBlock != null) {
							boolean recordAlreadyExists = false;
							// Check, ob erster Record des nextBlock schon im currBlock vorhanden ist
							for (int k = 1; k <= recordsInBlock; k++) {
								Record currentRecord = currBlock.getRecord(k);
								if (currentRecord != null && currentRecord.equals(firstRecordOfNextBlock)) {
									recordAlreadyExists = true;
									break;
								}
							}
							if (!recordAlreadyExists) {

								currBlock.insertRecordAtTheEnd(firstRecordOfNextBlock);
								int lastRec = currBlock.getNumberOfRecords();
								nextBlock.deleteRecord(lastRec);

								// Update numRecords und recordIndex für den nächsten Block
								recordsInBlock = currBlock.getNumberOfRecords();
								recordIndex++;
							}
						}
					}

					if (i == db.length - 1 && !found) {
						db[i].deleteRecord(db[i].getNumberOfRecords());
					}
				}
				break;
			} else {
				recordCounter += recordsInBlock;
			}
		}
		//Delete, wenn die DB sortiert vorliegt
		if (sorted) {
			List<Record> recordList = new ArrayList<>();
			for (DBBlock dbBlock : db) {
				int recCount = dbBlock.getNumberOfRecords();
				for (int i = 1; i <= recCount; i++) {
					Record currentRecord = dbBlock.getRecord(i);
					if (currentRecord.getNumOfAttributes() > 1) {
						recordList.add(currentRecord);
					}
				}
			}

			//Sortierung der Records in der DB mit der Rangfolge nach Mitgliedsnummern
			Collections.sort(recordList, new Comparator<Record>() {
				@Override
				public int compare(Record r1, Record r2) {
					String membNumR1 = r1.getAttribute(1);
					String membNumR2 = r2.getAttribute(1);
					//memberNumber, short membNum
					//Vergleich der Mitgliedernummer, wenn eine Mitgliedsnummer null oder leer sein sollte
					if (membNumR1 == null || membNumR1.isEmpty()  || membNumR2 == null || membNumR2.isEmpty() ) {
						return 0;
					}
					int membNumR1AsInt = Integer.parseInt(membNumR1);
					int membNumR2AsInt = Integer.parseInt(membNumR2);
					return Integer.compare(membNumR1AsInt, membNumR2AsInt);
				}
			});
			deleteDB();

			int currBlock = 0;
			for (Record sortedRecord : recordList) {
				int result = db[currBlock].insertRecordAtTheEnd(sortedRecord);
				if (result == -1) {
					currBlock++;
					if (currBlock < db.length) {
						 db[currBlock].insertRecordAtTheEnd(sortedRecord);
					} else {
						System.out.println("Delete failed");
					}
				}
			}
		}
	}

	/**
	 *
	 * @param recIndex, die Recordnumber eines Records
	 * @param blockNum, die Blocknummer
	 * @return recIndex, berechnet innerhalb des Blocks
	 */
	private int getPositionOfBlockRecord(int recIndex, int blockNum) {
		for (int i = 0; i < blockNum; ++i) {
			DBBlock dbBlock = this.getBlock(i);
			recIndex = recIndex - dbBlock.getNumberOfRecords();
		}
		return recIndex;
	}

	/**
	 *
	 * @param dbBlock, der Block der gerestockt werden soll
	 * @param records, Listen an Records, welche den Inhalt des Block dann befüllen
	 * löscht den Inhalt des Blocks und fügt die Records aus der List records wieder in den Block ein
	 */
	private void restockBlock(DBBlock dbBlock, List<Record> records) {
		dbBlock.delete();
		Iterator iterator = records.iterator();

		while (iterator.hasNext()) {
			Record record = (Record) iterator.next();
			for(Record r: records){

			}
			dbBlock.insertRecordAtTheEnd(record);
		}
	}

	/**
	 * löscht die ganze DB inhatlich,
	 * mit durchgehen der einzelnen Blöcke
	 */
	private void deleteDB(){
		for (DBBlock dbBlock : db) {
			dbBlock.delete();
		}
	}

	/**
	 * Replaces the record at the specified position with the given/updated one.
	 * @param numRecord the position of the old record in the db
	 * @param record the new/updated record
	 *
	 */
	//numRecord = Datensatznummer; record = geänderter Datensatz
	public void modify(int numRecord, Record record){

			int blockIndex = this.getBlockNumOfRecord(numRecord);
			DBBlock dbBlock = this.getBlock(blockIndex);
			int positionOfRecord = this.getPositionOfBlockRecord(numRecord, blockIndex);
			List<Record> blockRecords = new LinkedList();

			for (int i = 1; i <= dbBlock.getNumberOfRecords(); ++i) {
				if (i == positionOfRecord) {
					blockRecords.add(record);
				} else {
					blockRecords.add(dbBlock.getRecord(i));
				}
			}
			this.restockBlock(dbBlock, blockRecords);

	}

    public boolean checkDuplicate(Record record){
		boolean duplicate = false;

		for (int i = 0; i < db.length; i++) { //gehe die ganzen Blöcke der DB durch
			DBBlock currBlock = db[i];
			Iterator<Record> iterateBlock = new DBIterator();

			while(iterateBlock.hasNext()){
				Record currRec = iterateBlock.next();
				if(currRec.getAttribute(1) == record.getAttribute(1)){
					duplicate = true;
				}
			}
		}
			return duplicate;
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

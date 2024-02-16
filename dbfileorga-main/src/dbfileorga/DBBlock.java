package dbfileorga;

import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class DBBlock implements Iterable<Record> {
	public final static int BLOCKSIZE = 256;
	public final static char RECDEL = '|';
	public final static char DEFCHAR =  '\u0000';
	
	private char block[] = new char[BLOCKSIZE];
			
	/**
	 * Searches the record with number recNum in the DBBlock. 
	 * @param  recNum is the number of Record 1 = first record
	 * @return record with the specified number or null if record was not found 
	 */
	//first commit
	public Record getRecord(int recNum){
		int currRecNum = 1; //first Record starts at 0
		for (int i = 0; i <block.length;++i){
			if (currRecNum == recNum){
				return getRecordFromBlock(i);
			}
			if (block[i] == RECDEL){
				currRecNum++;
			}
		}
		return null;
	}
	
	
	private Record getRecordFromBlock(int startPos) {
		int endPos = getEndPosOfRecord(startPos);
		if (endPos != -1){ 
			return new Record (Arrays.copyOfRange(block, startPos, endPos));
		}else{
			return null;
		}
	}

	public int getEndPosOfRecord(int startPos){
		int currPos = startPos;
		while( currPos < block.length ){
			if (block[currPos]==RECDEL){
				return currPos;
			}else{
				currPos++;
			}
		}
		return -1;
	}

	
	/**
	 * Returns the number of records that are in the block
	 * @return number of records stored in this DBBlock
	 */	
	public int getNumberOfRecords(){
		int count = 0;
		for (int i = 0; i <block.length;++i){
			if (block[i] == RECDEL){
				count++;
			}
		}
		return count;
	}

	public int getCharsBeforeFoundRecord(Record foundRecord){
		int count = 0;
		Iterator<Record> iterator = new BlockIterator();

		while(iterator.hasNext()){
			Record currRecord = iterator.next();
			if(currRecord.getAttribute(1).compareTo(foundRecord.getAttribute(1)) == 0){
				System.out.println("Zähle die Chars vor dem modified Record");
				break;
			}
			count += currRecord.length() + 1;
		}
		return count;

	}

	
	/**
	 * Inserts an record at the end of the block
	 * @param record the record to insert
	 * @return returns the last position (the position of the RECDEL char) of the inserted record 
	 * 		   returns -1 if the insert fails
	 */
	public int insertRecordAtTheEnd(Record record){
		int startPos = findEmptySpace();
		return insertRecordAtPos(startPos, record);
	}
	
	/**
	 * deletes the content of the block
	 */
	public void delete(){

		block = new char[BLOCKSIZE];
	}

	public void deleteRecord(int recNum) {
		int currRecNum = 1;
		int startPos = -1;
		for (int i = 0; i < block.length; i++) {
			if (block[i] == RECDEL) {
				if (currRecNum == recNum) {
					startPos = i + 1;
					break;
				}
				currRecNum++;
			}
		}
		if (startPos != -1) {
			int endPos = getEndPosOfRecord(startPos);
			if (endPos != -1) {
				// Shift the remaining records to cover the deleted record
				System.arraycopy(block, endPos, block, startPos - 1, block.length - endPos);
				// Set the last character to RECDEL to mark the end of the record
				block[block.length - (endPos - startPos + 1)] = RECDEL;
			}
		}
	}


	

	/**
	 * Inserts an record beginning at position startPos
	 * @param startPos the postition to start inserting the record
	 * @param record the record to insert
	 * @return returns the last position (the position of the RECDEL char) of the inserted record 
	 * 		   returns -1 if the insert fails
	 */	
	public int insertRecordAtPos(int startPos, Record record) {
		//insert record + Trennzeichen RECDEL

		int n = record.length();

		if (startPos+n+1 > block.length){
			return -1; // record does not fit into the block;
		}
		for (int i = 0; i < n; ++i) {
		    block[i+startPos] = record.charAt(i);
		}
		block[n+startPos]= RECDEL;
		return n+startPos;
	}

	private int findEmptySpace(){
		for (int i = 0; i < block.length;++i){
			if (block[i] == DEFCHAR){
				return i;
			}
		}
		return block.length;		
	}
	
	@Override
	public String toString(){
		String result = new String();
		String tempRecord = new String();
		for (int i = 0; i < block.length; ++i) {
			if (block[i] == RECDEL) {
				if (!tempRecord.trim().isEmpty()) {
					result += tempRecord + "\n";
				}
				tempRecord = "";
			} else if (block[i] != DEFCHAR) {
				tempRecord += block[i];
			}
		}
		return result;
	}



	@Override
	public Iterator<Record> iterator() {

		return new BlockIterator();
	}

	public Record getFirstRecord() {
		String copy = new String();

		for (int i = 0; i < block.length; i++){

			if (block[i] == RECDEL){
				break;
			}
			copy += block[i];

		}
		Record firstRec = new Record(copy);
		return firstRec;
	}


	private class BlockIterator implements Iterator<Record> {
	    private int currRec=0;
 
	    public  BlockIterator() {

			this.currRec = 0;
        }
	    
        public boolean hasNext() {
            if ( getRecord(currRec+1) != null)
                return true;
            else
                return false;
        }
 
        public Record next() {
        	Record result = getRecord(++currRec);
            if (result == null){
            	throw new NoSuchElementException();
            }else{
            	return result;
            }
        }
 
        @Override
        public void remove() {

			throw new UnsupportedOperationException();
        }
    } 
	

}

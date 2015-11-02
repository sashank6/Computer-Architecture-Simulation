/*
 * Your Bimodal Branch Predictor goes here.
 * 
 */

public class BimodalPredictor extends BranchPredictor {

    public int nEntries = 1;
    public TwoBitSatCtr[] branchHistoryTable = null;

    /* 
     * nBits tells you how many entries there are in the table.
     * 2^n bits = nEntries
     */
    public BimodalPredictor(int tableSizeInBits) {
	nEntries = (int)Math.pow(2.0, tableSizeInBits);
	branchHistoryTable = new TwoBitSatCtr[nEntries];

	for (int i = 0; i < nEntries; i++) {
	    branchHistoryTable[i] = new TwoBitSatCtr();
	    branchHistoryTable[i].setChar(0, 'N');
	    branchHistoryTable[i].setChar(1, 'n');
	    branchHistoryTable[i].setChar(2, 't');
	    branchHistoryTable[i].setChar(3, 'T');
	}

    }
    
    /* 
     * Given a PC, look up the BHT to see whether you should predict taken or not taken
     */
    boolean predictIfTaken(long PC) {

	// COMPLETE THIS FUNCTION
      int index=(int)(PC%(long)nEntries);
      
	return branchHistoryTable[index].isTrue();
    }

    /*
     * Now you use the PC and whether the branch was taken or not to 
     * update your table.
     */
    void update(long PC, boolean wasTaken) {

	// COMPLETE THIS FUNCTION
      int index=(int)(PC%(long)nEntries);
      if(wasTaken)branchHistoryTable[index].inc();
	else 
	branchHistoryTable[index].dec();	

    }

    void printHeader() {

    	System.out.format("PC\tTableState\tPred\tOutcome\t  Result\tnIncorrect\n");

    }

    /* This is why we set up a char mapping in the constructor.
     */ 
    void printState() {

	System.out.format("\t");
	for (int i = 0; i < nEntries; i++) {
	    System.out.format("%c", branchHistoryTable[i].getChar());
	}
    }


}

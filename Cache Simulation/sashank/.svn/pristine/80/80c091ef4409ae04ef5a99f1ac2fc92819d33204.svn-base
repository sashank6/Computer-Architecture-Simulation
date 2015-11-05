/*
 * Your GShare Branch Predictor goes here.
 * 
 * 
 */

public class GSharePredictor extends BranchPredictor {

        public NBitHistoryRegister reg;
        public BimodalPredictor bhr; 
    public GSharePredictor(int tableSizeInBits, int historyLengthInBits) 
    {
              reg=new NBitHistoryRegister(historyLengthInBits);
		bhr=new BimodalPredictor(tableSizeInBits);
	 
    }
    
    /* Given a PC, return true if this branch predictor predicts that the branch is taken.
     * Return false if this branch predictor predicts that the branch is not taken.
     */
    boolean predictIfTaken(long PC) {

	return bhr.predictIfTaken(reg.register^PC);
    }

    /* Now you are given the PC and told whether the branch was taken or not.
     * Use this information to update your predictor.
     */
    void update(long PC, boolean wasTaken) 
 	{
                bhr.update(reg.register^PC,wasTaken);		
                reg.update(wasTaken); 
    }

    void printHeader() {
    	System.out.format("TableState\t\tHist\tPC\tOutcome\tPred\tResult\tnIncorrect\n");
    }

    /* 
     * Technically, you don't have to implement this. It's here in case you want to
     * create the debugging outputs.
     */
    void printState() {

    }


}

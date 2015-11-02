/*
 * Your Tournament Branch Predictor goes here.
 * 
 * 
 */

public class TournamentPredictor extends BranchPredictor {
   public TwoBitSatCtr[] chooserTable=null;
	public int entries=1;
      public BimodalPredictor bm;
      public GSharePredictor gm;
      public boolean gm_pred;
      public boolean bm_pred; 
    public TournamentPredictor(int bm_tableSizeInBits, 
			       int gs_tableSizeInBits, int gs_historyLengthInBits, 
				  int chooserTableSizeInBits) {
	entries=(int)Math.pow(2.0,chooserTableSizeInBits);
chooserTable=new TwoBitSatCtr[entries];

	
	for (int i = 0; i < entries; i++) {
	    chooserTable[i] = new TwoBitSatCtr();
	    chooserTable[i].setChar(0, 'B');
	    chooserTable[i].setChar(1, 'b');
	    chooserTable[i].setChar(2, 'g');
	    chooserTable[i].setChar(3, 'G');


    }
          bm=new BimodalPredictor(bm_tableSizeInBits);
          gm=new GSharePredictor(gs_tableSizeInBits,gs_historyLengthInBits);

}
    
    /* Given a PC, return true if this branch predictor predicts that the branch is taken.
     * Return false if this branch predictor predicts that the branch is not taken.
     */
    boolean predictIfTaken(long PC) {
        int index=(int)(PC%(long)entries);
        gm_pred=gm.predictIfTaken(PC);
        bm_pred=bm.predictIfTaken(PC);
        if(chooserTable[index].isTrue())
        return gm_pred;
	return bm_pred;
    }

    /* Now you are given the PC and told whether the branch was taken or not.
     * Use this information to update your predictor.
     */
    void update(long PC, boolean wasTaken) {
      
        int index=(int)(PC%(long)entries);
        if((wasTaken==gm_pred&&wasTaken==bm_pred)||(wasTaken!=gm_pred&&wasTaken!=bm_pred)){}
        else
        if(wasTaken==gm_pred)
        chooserTable[index].inc();
        else
        chooserTable[index].dec();
        
        bm.update(PC,wasTaken);
        gm.update(PC,wasTaken);
 
                   

    }

    void printHeader() {
    	System.out.format("PC\tChooseTable\tBimodTable\tGshareTable\t\tHist\tPred\tOutcome\tResult\t\tnIncorrect\n");
    }

    /* 
     * Technically, you don't have to implement this. It's here in case you want to
     * create the debugging outputs.
     */
    void printState() {

    }


}

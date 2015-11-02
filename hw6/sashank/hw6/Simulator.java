import java.lang.String;
import java.util.zip.GZIPInputStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayDeque;
import java.util.Iterator;

public class Simulator {

	public String testTrace = "sjeng-1K"; // default

	// print options, by default off
	public boolean verbose_f = false;
	public boolean debug_f = false;
	public boolean renameOnly_f = false;

	// optionally limit the simulation to the first N insns
	// do not break this functionality when you complete the code!
	public boolean insnLimit_f = false;
	public int uopLimit = 0;
	public boolean doneFetching_f = false;

	// don't touch! you'll be sorry....
	public BufferedReader traceReader = null;

	// Processor Configuration
	public int machineWidth = 1;
	public final int NUM_ARCH_REGISTERS = 50;
	public int nPhysicalRegisters = NUM_ARCH_REGISTERS; // must have *at least*
														// this many

	// Processor Structures
	public int MapTable[] = null;
	public ArrayDeque<Integer> freeList = null;
	public ReorderBuffer ROB = null;
	public int[] scoreboard = null;

	// Program Counters & Stats
	public double IPC = 0.0;
	public long nFetched = 0;
	public long nCommitted = 0;
	public long nROBstalls = 0;
	public long cycleCount = 0;
	public long lastCommittedCycle = 0;

	// Simulating Memory Dependences
	public boolean modelMemoryDependences_f = false;
	// At first: ignore memory dependences, set to false
	// if the above flag is true then we actually check the flag below
	// to determine whether we model the dependences conservatively or perfectly
	// (false = conservative = Exp #2, true = perfect = Exp #3)
	public boolean perfectMemSched_f = false;

	/*
	 * Simple constructor for the simulator.
	 */
	public Simulator() {

		MapTable = new int[NUM_ARCH_REGISTERS];
		// TODO: initialize your MapTable: for all n,
		// map architectural register n to physical register n

		for (int i = 0; i < NUM_ARCH_REGISTERS; i++) {
			MapTable[i] = i;
		}
		freeList = new ArrayDeque<Integer>();

		ROB = new ReorderBuffer();
	}

	/*
	 * Once the simulator is constructed, the number of physical registers can
	 * be specified by setting the variable nPhysicalRegisters. Once that number
	 * is set, you can set up the register file and associated structures
	 */
	public void initRegStructures() {

		scoreboard = new int[nPhysicalRegisters];

		// initialize to all zeroes
		for (int i = 0; i < this.nPhysicalRegisters; i++) {
			scoreboard[i] = 0;
		}

		// now that you know the size of the p regfile, you must
		// initialize your freeList: anyone not mapped is free.
		// add them such that the very first register you take OFF
		// The freeList has the lowest unused register number.
		// Example: to add 3, you must convert it into an Integer:
		// freeList.addLast(new Integer(3));
		if (this.nPhysicalRegisters > NUM_ARCH_REGISTERS) {
			for (int j = NUM_ARCH_REGISTERS; j < this.nPhysicalRegisters; j++) {
				freeList.addLast(new Integer(j));
			}
		}
	}

	/*
	 * set up the trace as usual
	 */
	public void initTrace() throws IOException {

		String tracePath = "../traces/" + testTrace + ".trace.gz";
		//String tracePath="/wustl/cse560/hw6/sashank/traces/"+testTrace+".trace.gz";
		//String tracePath = "E:\\Eclipse Workspace\\Computer Architecture -Assignment 01\\traces\\"
	//			+ testTrace + ".trace.gz";
		// ugly, but JAVA IO is not the point
		traceReader = new BufferedReader(new InputStreamReader(
				new GZIPInputStream(new FileInputStream(tracePath))));
	}

	/*
	 * Commit instructions in order starting at the head of the ROB. on an
	 * n-wide machine, you can commit n instructions per cycle
	 */
	public void commit() {

		for (int i = 0; i < machineWidth; i++) {
			if (ROB.isEmpty()) // nothing to commit. bye!
				break;

			ROBentry head = ROB.head(); // this doesn't remove the entry from
										// the ROB

			// TODO: commit head of the ROB if you can, update nCommitted, print
			// debug line

			if (head.doneCycle != -1) {
				if (head.doneCycle <= cycleCount) {
					ROB.dequeue();
					nCommitted++; // increment this when you dequeue
					lastCommittedCycle = cycleCount;
				}
				// this is NOT correct, just showing you how to use the Reorder
				// Buffer
			}
		}
	}

	/*
	 * issue instructions that have not already been issued & whose inputs are
	 * ready. on an n-wide machine, you can issue n instructions per cycle
	 */
	public void issue() {

		int count = 0;
		// This is how you can iterate through the ROB without actually removing
		// anything from it
		// (which is what you want to do for this method)
		for (Iterator<ROBentry> itr = ROB.entries.iterator(); itr.hasNext();) {
			ROBentry currEntry = itr.next();
			Uop currUop = currEntry.theUop;

			if (!currEntry.hasIssued_f && isReady(currEntry)) {
				currEntry.hasIssued_f = true;
				currEntry.issueCycle = cycleCount;
				currEntry.doneCycle = cycleCount + currUop.execLatency;
			}

			for (int j = 0; j < currEntry.physicalOutputRegs.length; j++) {
				if (currEntry.physicalOutputRegs[j] >= 0) {
					scoreboard[currEntry.physicalOutputRegs[j]] = currUop.execLatency;
				}
			}

			count++;
			if (count == machineWidth) {
				break;
			}
		}
	}

	/*
	 * 
	 */
	public boolean isReady(ROBentry currEntry) {
		boolean isReady = false;
		for (int i = 0; i < currEntry.physicalInputRegs.length; i++) {
			if (currEntry.physicalInputRegs[i] >= 0) {
				if (scoreboard[currEntry.physicalInputRegs[i]] != 0) {
					return isReady;
				}
			}
		}
		isReady = true;
		return isReady;
	}

	/*
	 * Fetches instructions, renames them, adds them to the ROB
	 */
	public void fetchRename() throws IOException {
		Uop currUop = null;
		String line = "";

		for (int i = 0; i < machineWidth; i++) {

			if (ROB.isFull()) {
				nROBstalls++;
				break;
			}

			line = traceReader.readLine();
			if (line == null) {
				doneFetching_f = true;
				return;
			}
			nFetched++;

			currUop = new Uop(line, NUM_ARCH_REGISTERS - 1);
			ROBentry entry = new ROBentry(currUop, nFetched, cycleCount);

			// complete this method.

			for (int q1 = 0; q1 < currUop.inputRegs.length; q1++) {
				if (currUop.inputRegs[q1] >= 0) {
					entry.physicalInputRegs[q1] = MapTable[currUop.inputRegs[q1]];
				} else {
					entry.physicalInputRegs[i] = -1;
				}
			}
			
			for (int j = 0; j < currUop.outputRegs.length; j++) {
				if (currUop.outputRegs[j] >= 0) {
					entry.overwrittenRegs[j] = MapTable[currUop.outputRegs[j]];
					int new_p_reg = freeList.removeFirst();
					MapTable[currUop.outputRegs[j]] = new_p_reg;
					entry.physicalOutputRegs[j] = new_p_reg;
				} else {
					entry.physicalOutputRegs[j] = -1;
				}
			}

			for (int q = 0; q < currUop.outputRegs.length; q++) {
				if (currUop.outputRegs[q] >= 0) {
					freeList.addLast(entry.overwrittenRegs[q]);
				}
			}

			for (int r = 0; r < entry.physicalOutputRegs.length; r++) {
				if (entry.physicalOutputRegs[r] >= 0) {
					scoreboard[entry.physicalOutputRegs[r]] = -1;
				}
			}
			// here is an example of how to add an insn to the ROB
			ROB.enqueue(entry);

			if (verbose_f)
				System.out.format("%s\n", line);
		}
	}

	/*
	 * HW 6 has two parts. Here is where you decide which one to simulate.
	 */
	public void processTrace() throws IOException {

		initTrace();

		if (renameOnly_f)
			simulateRenameOnly(); // PART 1
		else
			simulateAllStages(); // PART 2

		IPC = (double) nCommitted / cycleCount;
	}

	/*
	 * Goes through the trace line by line (i.e., instruction by instruction)
	 * and simulates the program being executed on a processor.
	 * 
	 * You may modify this function, but you shouldn't have to. Simply
	 * completing the methods that this method calls should suffice.
	 */
	public void simulateAllStages() throws IOException {

		if (debug_f)
			printDebugHeader();

		while (true) {

			// notice how we simulate parallel hw pipeline stages
			// on a serial sw simulator
			commit();

			issue();

			fetchRename();

			cycleCount++;

			for (int i = 0; i < scoreboard.length; i++) {
				if (scoreboard[i] > 0) {
					scoreboard[i]--;
				}
			}

			if (insnLimit_f && nCommitted >= uopLimit) {
				System.out
						.format("Reached insn limit of %d @ cycle %d. Ending Simulation...\n",
								uopLimit, cycleCount);
				break;
			}
			if (doneFetching_f && ROB.isEmpty()) {
				System.out.format(
						"Completed Trace @ cycle %d. Ending Simulation...\n",
						cycleCount);
				break;
			}
			// this is to help you debug
			if (cycleCount > (lastCommittedCycle + 100)) {
				System.out
						.format("You haven't committed an instruction in 100 cyces... Time to debug. Ending Simulation...\n");
				break;
			}
		}
	}

	/*
	 * To complete PART 1, complete this method.
	 */
	public void simulateRenameOnly() throws IOException {

		Uop currUop = null;
		String line = "";

		if (debug_f)
			printRenameDebugHeader();

		while (true) {

			line = traceReader.readLine();
			if (line == null) {
				break;
			}

			currUop = new Uop(line, NUM_ARCH_REGISTERS - 1);
			nFetched++;
			nCommitted++;
			ROBentry entry = new ROBentry(currUop, nCommitted, nCommitted); // arguments
																			// 2
																			// &
																			// 3
																			// don't
																			// matter
																			// for
																			// Part
																			// 1

			// perform part 1 of renaming algorithm here
			// for each valid input register...
			for (int i = 0; i < currUop.inputRegs.length; i++) {
				if (currUop.inputRegs[i] >= 0) {
					entry.physicalInputRegs[i] = MapTable[currUop.inputRegs[i]];
				} else {
					entry.physicalInputRegs[i] = -1;
				}
			}
			
			for (int j = 0; j < currUop.outputRegs.length; j++) {
				if (currUop.outputRegs[j] >= 0) {
					entry.overwrittenRegs[j] = MapTable[currUop.outputRegs[j]];
					int new_p_reg = freeList.removeFirst();
					MapTable[currUop.outputRegs[j]] = new_p_reg;
					entry.physicalOutputRegs[j] = new_p_reg;
				} else {
					entry.physicalOutputRegs[j] = -1;
				}
			}

			// print the mapping of the input registers
			if (debug_f) {
				System.out.format("%d", nCommitted);
				entry.printInputRegisters();
			}

			// perform part 2 of renaming algorithm here
			// for each valid logical output register...

			// now "commit"
			// free the overwritten register...

			for (int q = 0; q < currUop.outputRegs.length; q++) {
				if (currUop.outputRegs[q] >= 0) {
					freeList.addLast(entry.overwrittenRegs[q]);
				}
			}
			// print the mapping of the output registers
			if (debug_f) {
				entry.printOutputRegisters();
				currUop.printMacroMicroNames();
			}

			// prints the insn
			if (verbose_f)
				System.out.format("%s\n", line);

			if (insnLimit_f && nCommitted >= uopLimit) {
				System.out
						.format("Reached insn limit of %d @ cycle %d. Ending Simulation...\n",
								uopLimit, cycleCount);
				break;
			}
			cycleCount++;
		}
	}

	// -------PRINT METHODS BEGIN HERE --------

	/*
	 * Prints out basic stats + the hit/miss rates of the cache.
	 */
	public void printTraceStats() {

		System.out.format("Processed %d trace lines.\n", nCommitted);
		System.out.format("Instructions:\n");
		System.out.format("  N Fetched =    \t\t%d\n", nFetched);
		System.out.format("  N Committed =  \t\t%d\n", nCommitted);
		System.out.format("Stalls:\n");
		System.out.format("  Fetch stalled (ROB full) =    %d\n", nROBstalls);
		System.out.format("IPC: \t\t\t\t%.3f\n", IPC);

	}

	/*
	 * for Debug mode, you'll want to print headers to know what each column
	 * means
	 */
	public void printRenameDebugHeader() {
		System.out.format("uop# \t Reg Mappings [freeMe] | Mop Uop\n");
	}

	/*
	 * for Debug mode, you'll want to print headers to know what each column
	 * means
	 */
	public void printDebugHeader() {
		System.out.format("#: F I D C \t Reg Mappings [freeMe] | Mop Uop\n");
	}

	public void printHeader() {
		if (!debug_f) {
			System.out
					.format("HW 6 Printout for i-forgot-to-replace-this-string-with-my-wustlkey\n");
			System.out
					.format("-----------------------------------------------------\n");
		}
		System.out.format("Trace name  \t\t\t\t%s\n", testTrace);
		System.out.format("Instruction Limit\t\t\t%s\n",
				insnLimit_f ? Integer.toString(uopLimit) : "none");
		System.out.format("Processor Configuration:\n");
		System.out.format("  Machine Width \t\t\t%d\n", machineWidth);
		System.out.format("  Number of Logical Registers \t\t%d\n",
				NUM_ARCH_REGISTERS);
		System.out.format("  Number of Physical Registers \t\t%d\n",
				nPhysicalRegisters);
		System.out.format("  ROB Size \t\t\t\t%d\n", ROB.maxEntries);
		if (!modelMemoryDependences_f)
			System.out.format("  NOT modeling memory dependences\n");
		else {
			if (perfectMemSched_f)
				System.out
						.format("  modeling memory dependences: Perfect Memory Scheduling\n");
			else
				System.out
						.format("  modeling memory dependences: Conservative Memory Scheduling\n");
		}
	}

}

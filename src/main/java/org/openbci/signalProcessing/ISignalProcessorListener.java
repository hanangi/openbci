package org.openbci.signalProcessing;

public interface ISignalProcessorListener {
	
	public void dataPortionProcessed(int[] eegData);
}

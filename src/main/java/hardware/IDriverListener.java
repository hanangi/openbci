package hardware;

import org.openbci.general.EegData;

public interface IDriverListener {
	
	public void dataArrived(EegData eeg) ; 
}

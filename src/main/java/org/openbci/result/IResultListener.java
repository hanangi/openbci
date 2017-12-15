package org.openbci.result;


public interface IResultListener {
	public void resultArrived(String txt) ;
	public void stopRecording() ;
}

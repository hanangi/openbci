package org.openbci.collect;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.zip.GZIPOutputStream;

import org.apache.log4j.Logger;
import org.openbci.configuration.Configuration;
import org.openbci.configuration.IConfigurationListener;
import org.openbci.general.EegData;
import org.openbci.training.EegDataWithDescr;

import hardware.IDriverListener;

public class HardDriveCollector implements Runnable , IDriverListener, IConfigurationListener {
	
	private final static Logger log = Logger.getLogger(HardDriveCollector.class);

	private File file;
	private File fileHumanReadable ;
	private Configuration configuration;

	private String separator = System.getProperty("file.separator"); // "\\"
	// CONF - HEADERS... - showedSymbols - CONF - DATA... - HEADERS ...... 
	public HardDriveCollector(String subjectName, Configuration conf){
		this.configuration = conf ;
		String path = System.getProperty("user.dir")+separator+
		subjectName+separator+
		configuration.getValueInt("sessionId") ;
		
		boolean success = (new File(path)).mkdirs();
	    if (!success) {
	        log.info("[ERROR:HardDriveCollector] can't make dir for writing PATH:"+path);
	    }
	    // next available file name (from zero -> infinity )
	    int i = 0 ;
	    boolean exists = (new File(path+separator+i+".jp300")).exists();
	    
	    while(exists){
	    	i++ ;
	    	exists = (new File(path+separator+i+".jp300")).exists();
	    }
	    
		file = new File(path+separator+i+".jp300");
		fileHumanReadable = new File(path+separator+i+"_human.jp300");
	}

	private ObjectOutputStream obj = null ;
	private GZIPOutputStream out = null;
	private boolean runState = false ;
	private FileOutputStream outHuman;
	private OutputStreamWriter printHuman;

	private void writeStart(){
		try {
			out = new GZIPOutputStream(new FileOutputStream(file)) ;
			obj = new ObjectOutputStream(out);
		} catch(FileNotFoundException e) {
			System.err.println("Could not create file: " + file.getName());
			System.exit(1);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
		write(new DataObject(configuration)) ;
	}
	private void write(Object o){
		try {
			obj.writeObject(o);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void writeEnd(){
		write(new DataObject(DataObject.HEADER_ENDFILE)) ;
		try {
			obj.close();
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
	
	private void writeStartHuman(){
		try {
			outHuman = new FileOutputStream(fileHumanReadable) ;
			printHuman = new OutputStreamWriter(outHuman);
		} catch(FileNotFoundException e) {
			System.err.println("Could not create file: " + fileHumanReadable.getName());
			System.exit(1);
		}
	}
	private void writeHuman(Object o){
		try {
			DataObject object = (DataObject) o;
			if(object.getDataType()==DataObject.TYPE_EEGDATA)
				for(int val : ((EegData)object.getData()).getValues())
					printHuman.write(val+" ");
			else if(object.getDataType()==DataObject.HEADER_INITIALIZATION_NEXT_HIGHLIGHT){
				if(((Integer)((DataObject)object).getData())==EegDataWithDescr.POSITIVE)
					printHuman.write("1");
				else
					printHuman.write("0");
			}
				
			printHuman.write("\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void writeEndHuman(){
		try {
			printHuman.close();
			outHuman.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void setRunState(boolean runState){
		this.runState = runState ;
	}
	
	private volatile ConcurrentLinkedQueue<DataObject> fifo = new ConcurrentLinkedQueue<DataObject>();
	private volatile ConcurrentLinkedQueue<DataObject> fifoHeaders = new ConcurrentLinkedQueue<DataObject>();
	private IShowedSymbols showedSymbols;
	
	public void run() {
		log.info("[HddCollector] - starting"+this);
		runState = true ;
		writeStart() ;
		writeStartHuman() ;
		log.info("[HddCollector] - started");
		while(runState){
			synchronized(this){
				try {
					this.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			// write sequence of symbols that was presented
			log.info("[HddCollector] - writing headers ! "+this);
			for(DataObject object : fifoHeaders){
				write(object) ;
				writeHuman(object) ;
				fifoHeaders.remove() ;
			}
			write(new DataObject(showedSymbols.getHighlightedPositions(),DataObject.TYPE_HIGHLIGHTED_POSITIONS)) ;
			write(new DataObject(configuration));
			for(DataObject object : fifo){
				write(object) ;
				writeHuman(object) ;
			}
			fifo.clear() ;
		}
		writeEnd() ;
		writeEndHuman() ;
		log.info("[HddCollector] - stopped..");
	}

	public void dataArrived(EegData eeg) {
		fifo.add(new DataObject(eeg)) ;
	}
	
	public void addHeader(DataObject head){
		log.info("[HddCollector] - adding new header "+this);
		fifoHeaders.add(head) ;
	}
	
	public void addData(DataObject object){
		fifo.add(object) ;
	}
	
	public void configurationChanged() {
		log.info("[HddCollector] Configuration changed - need update") ;
		
	}
	public void setDisplayedObjects(IShowedSymbols showedSymbols) {
		this.showedSymbols = showedSymbols ; 		
	}
	public boolean getRunState() {
		return runState;
	}
}

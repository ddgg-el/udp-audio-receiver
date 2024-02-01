import java.lang.reflect.Method;

import com.cycling74.max.*;
import com.cycling74.msp.*;


public class Pipe extends MSPObject implements MSPPerformable{
	private RawUdpReceiver udp_client;
	// private int port = 8888;
	private int buf_size;

	private int blockSize = 512;
	private Method _p1;
	private final Object lock = new Object();

	// Constructor Bail
	public Pipe(){
        bail("(mxj~ Pipe) you must provide a valid udp port e.g. [mxj~ Pipe 8888].");
	}
	// Constructor
	public Pipe(int udp_port){
		declareInlets(new int[]{SIGNAL});
		declareOutlets(new int[]{SIGNAL, DataTypes.FLOAT});
		// port = udp_port;
		// declareAttribute("port",null,"setPort");
		this.buf_size = 1024;
		initListener(udp_port, this.buf_size);
		_p1 = getPerformMethod("p1");
	}

	public Pipe(int udp_port, int buf_size){
		declareInlets(new int[]{SIGNAL});
		declareOutlets(new int[]{SIGNAL, DataTypes.FLOAT});
		// port = udp_port;
		// declareAttribute("port",null,"setPort");
		this.buf_size = buf_size;
		initListener(udp_port, this.buf_size);
		_p1 = getPerformMethod("p1");
	}
	// init UDP 
	private void initListener(int udp_port, int buf_size){
		udp_client = new RawUdpReceiver(udp_port, buf_size); // the port argument also sets the UDPReceiver automatically Active
		// setPort(udp_port);
		udp_client.setDebugString("udp_client");
		udp_client.setCallback(this, "processUdpData");
	}

	public void sboom(){
		post("SBOOOM");
	}
	
	// private void setPort(int p){
	// 	udp_client.setPort(p);
	// }

	// Udp Data callback function
	private void processUdpData(float[] udpData){
		outlet(1,udpData);
		outlet(getInfoIdx(), udpData.length);
	}
		
	protected void notifyDeleted(){
		udp_client.close();
	}

	///////////////// MSPObject interface /////////////////
	// initialization function (as `dspsetup` in MSPPerformer class )
	public Method dsp(MSPSignal[] in, MSPSignal[] out){
		// post("once");
		return _p1;
	}

	public void p1(MSPSignal[] in, MSPSignal[] out){
		float[] o = out[0].vec;
		blockSize = out[0].n;
		synchronized(lock){
			for(int i = 0; i < blockSize; i++){
				o[i] = 0; //samples[i];
			}
		}
	}

	///////////////// MSP performable interface /////////////////
	// dspsetup (calls dsp in MSPObject) 
	public void dspsetup(MSPSignal[] in, MSPSignal[] out){
		dsp(in,out);
	}
	// the sample block process
	public void perform(MSPSignal[] in, MSPSignal[] out){
		p1(in,out);
	}
}
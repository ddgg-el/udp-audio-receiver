import java.net.BindException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import com.cycling74.max.Callback;
import com.cycling74.max.MaxSystem;

import java.io.IOException;

public class RawUdpReceiver implements Runnable {
   
   private int port;
   private int buf_size;
	private DatagramSocket socket;
   private Thread listener = null;
   private String debugString = "RawUdpReceiver";
   private Callback callback;

   public RawUdpReceiver(int udp_port, int buf_size){
      this.port = udp_port;
      this.buf_size = buf_size;
      this.setActive(true);
   }

   public void setPort(int port){
      MaxSystem.post("Calling setPort!");
      if (port != this.port) {
         this.port = port;
         this.resetListener();
      }
   }
   public int getPort(int port){
      MaxSystem.post("Calling getPort");
      return this.port;
   }

   private void resetListener() {
      MaxSystem.post("Resetting the Listener");
      (new Thread(){
         public void run(){
            RawUdpReceiver.this.setListener(false);
            RawUdpReceiver.this.setListener(true);
         }
      }).start();
   }

   public void setDebugString(String s) {
      MaxSystem.post("Setting Debug String");
      this.debugString = s;
   }

   public void setActive(boolean b){
      MaxSystem.post("SetActive Called " +  String.valueOf(b));
      if(b){
         (new Thread(){
            public void run(){
               RawUdpReceiver.this.setListener(true);
            }
         }).start();
      } else {
         (new Thread(){
            public void run(){
               RawUdpReceiver.this.setListener(false);
            }
         }).start();
      }
   }

   private void setListener(boolean b){
      MaxSystem.post("Setting the Listener");
      if(b){
         if(this.listener == null && this.initRecvSocket()){
            this.listener = new Thread(this);
            this.listener.start();
         }
      } else {
         if(this.socket != null){
            this.socket.close();
         }
         this.listener = null;
      }
   }

   private boolean initRecvSocket(){
      MaxSystem.post("Initializing socket");
      try {
         this.socket = new DatagramSocket(this.port);
         return true;
      } catch (BindException err2) {
         MaxSystem.error(this.debugString + ": there is already an object bound to port " + this.port);
      } catch (SocketException err3) {
         MaxSystem.error(this.debugString + ": socket exception: " + err3);
      }
      return false;
   }

   
   @Override
	public void run() {
      MaxSystem.post("Run Started");
      while(true) {
         byte[] buf = new byte[this.buf_size];
         DatagramPacket packet = new DatagramPacket(buf, buf.length);
         try {
            this.socket.receive(packet);
            float[] float32_sample = decodeBytes_toFloat32(packet.getData(), packet.getLength());
            this.callback.setArgs(new Object[]{float32_sample});
            this.callback.execute();
            // short[] int16_samples = decodeBytes_toInt16(packet.getData(),packet.getLength());
            // int i = 0;
            // for (float f : float32_sample) {
            //    System.out.println("Sample " + i + " " + f);
            //    i++;
            // }
         } catch (IOException e) {
            // MaxSystem.error(this.debugString + ": there was an error reading the socket. Am I done?");
            return;
         }
      }
   }
   
   public void setCallback(Object toCallIn, String methodName){
      MaxSystem.post("Setting the callback");
      this.callback = new Callback(toCallIn, methodName,new Object[]{new float[0]});
   }

   public void close(){
      MaxSystem.post("Closing the Receiver");
      this.setActive(false);
   }

   // private short[] decodeBytes_toInt16(byte[] data, int length){
   //    short[] samples = new short[length/2];
   //    ByteBuffer buffer = ByteBuffer.wrap(data,0,length);
   //    buffer.order(ByteOrder.LITTLE_ENDIAN);

   //    for (int i = 0; i < samples.length; i++) {
   //       samples[i] = buffer.getShort();
   //    }
   //    return samples;
   // }

   private float[] decodeBytes_toFloat32(byte[] data, int length){
      short[] samples = new short[length/2];
      float[] samples32 = new float[samples.length];
      ByteBuffer buffer = ByteBuffer.wrap(data,0,length);
      buffer.order(ByteOrder.LITTLE_ENDIAN);

      for (int i = 0; i < samples.length; i++) {
         short sample = buffer.getShort();
         double bits = Math.pow(2,15);
         float sample32 = (float)(sample/bits);
         samples32[i] = sample32;
      }
      return samples32;
   }

   // public static void main(String[] args) {
   //    RawUdpReceiver udpReceiver = new RawUdpReceiver(9999, 1024);
   //    // udpReceiver.start();
   //    try {
   //       Thread.sleep(5000);
   //    } catch (InterruptedException e){
   //       e.printStackTrace();
   //    }

   //    // udpReceiver.stopReceiver();
   // }
}

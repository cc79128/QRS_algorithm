import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
 
public class QRS {
 
	public static final int M = 5;

	public static void main(String[] args) throws IOException {
		
		//CSV檔
		ArrayList<String> data_list = readCSVToArrayList("/Users/mac/Desktop/samples.csv"); //檔案位置
		
		int nsamp = data_list.size()-2;
		float[] sig0 = new float[nsamp];
		for(int i=2;i<nsamp;i++){
			sig0[i-2] = Float.parseFloat(data_list.get(i));
		}
 
        float[] highPass = highPass(sig0, nsamp);
        
        //將highpass寫入csv檔
        try {
        	FileWriter fw = new FileWriter( "/Users/mac/Desktop/highpass.csv",true);
        	BufferedWriter bw=new BufferedWriter(fw);
        	
        	for (int i = 0; i < highPass.length; i++) {
	        	bw.write(String.valueOf(highPass[i]) );
				bw.newLine();//換行
        	}
        	bw.close();
        	
        } catch (IOException e) {
			e.printStackTrace();
			}

        float[] lowPass = lowPass(highPass, nsamp);
        
        //將bandpass寫入csv檔
        try {
        	FileWriter fw = new FileWriter( "/Users/mac/Desktop/band_pass.csv",true);
        	BufferedWriter bw=new BufferedWriter(fw);
        	
        	for (int i = 0; i < lowPass.length; i++) {
	        	bw.write(String.valueOf(lowPass[i]) );
				bw.newLine();//換行
        	}
        	bw.close();
        	
        } catch (IOException e) {
			e.printStackTrace();
			}
        
        int[] QRS = QRS(lowPass, nsamp);
        //將QRS_detection寫入csv檔
        try {
        	FileWriter fw = new FileWriter( "/Users/mac/Desktop/QRS.csv",true);
        	BufferedWriter bw=new BufferedWriter(fw);
        	
        	for (int i = 0; i < QRS.length; i++) {
	        	bw.write(String.valueOf(QRS[i]) );
				bw.newLine();//換行
        	}
        	bw.close();
        	
        } catch (IOException e) {
			e.printStackTrace();
			}
 
    }

		//讀取CSV檔的值存到ArrayList
	 public static ArrayList<String> readCSVToArrayList(String csvpath) {
	        
		 	/**
		     * 讀取csv檔,回傳ArrayList,裡面的每一個元素都是另一個ArrayList,其中存放每個單列的資料
		     * @param csvpath csv路徑
		     * @return ArrayList,裡面的每一個元素都是一個ArrayList,其中存放每個單列的資料
		     */
	        //存放所有檔案內容
	        ArrayList<String> dataAL = new ArrayList<String>();

	        //讀取檔案
	        BufferedReader reader;	        
	        try {            
	            reader = new BufferedReader(new FileReader(csvpath));
	            //reader.readLine();// 是否讀取第一行 (加上註解代表會讀取,註解拿掉不會讀取)
	            String line = null;// 暫存用(測試是否已讀完檔)
                
                int line_num = 0;
	            // 讀取資料
	            while ((line = reader.readLine()) != null) {
	                
	                String item[] = line.split(",");//csv文件為依據逗號切割
	                
	                //讀檔(單列資料)
	                dataAL.add(item[1]);
             
	                //dataAL.add(ticketStr);     
	                System.out.println(dataAL.get(line_num));
	                line_num++;
	            }
	            System.out.println(dataAL.size());
	            //System.out.print(ticketStr.toString());
	            
	        } catch (FileNotFoundException e) {
	            // TODO Auto-generated catch block
	            e.printStackTrace();
	        } catch (IOException e) {
	            // TODO Auto-generated catch block
	            e.printStackTrace();
	        }
	        
	        return dataAL;
	    }
	 
	 //===============High Pass Filter================================
	 // M是Window size， 5 or 7是較好的選擇（取奇數） 
	 // y1:前M個值(包括自己)加總除以M
	 // y2:Group delay (M+1/2)
	 
	 public static float[] highPass(float[] sig0, int nsamp) { //nsamp: data數量
	        float[] highPass = new float[nsamp];
	        float constant = (float) 1/M;
	 
	        for(int i=0; i<sig0.length; i++) { //sig0: input data的array
	            float y1 = 0;
	            float y2 = 0;
	 
	            int y2_index = i-((M+1)/2);
	            if(y2_index < 0) { //若小於0則array最後開始往回推
	                y2_index = nsamp + y2_index;
	            }
	            y2 = sig0[y2_index];
	 
	            float y1_sum = 0;
	            for(int j=i; j>i-M; j--) {
	                int x_index = i - (i-j);
	                if(x_index < 0) {
	                    x_index = nsamp + x_index;
	                }
	                y1_sum += sig0[x_index];
	            }
	 
	            y1 = constant * y1_sum; //constant = 1/M
	            highPass[i] = y2 - y1; 
	        }        
	 
	        return highPass;
	    }
	
	 //============Low pass filter==================
	 //Non Linear
	 //平方->加總
	 public static float[] lowPass(float[] sig0, int nsamp) {
	        float[] lowPass = new float[nsamp];
	        for(int i=0; i<sig0.length; i++) {
	            float sum = 0;
	            if(i+30 < sig0.length) {
	                for(int j=i; j<i+30; j++) {
	                    float current = sig0[j] * sig0[j]; //算平方
	                    sum += current; //加總
	                }
	            }
	            else if(i+30 >= sig0.length) { //超過array
	                int over = i+30 - sig0.length; 
	                for(int j=i; j<sig0.length; j++) {
	                    float current = sig0[j] * sig0[j];
	                    sum += current;
	                }
	                //怪怪的?? over應該<0
	                for(int j=0; j<over; j++) {
	                    float current = sig0[j] * sig0[j];
	                    sum += current;
	                }
	            }
	 
	            lowPass[i] = sum;
	        }
	 
	        return lowPass;
	 
	    }
	 
	 //=================QRS Detection================
	 //beat seeker
	 //alpha: forgetting factor 從0.001到0.1之間隨機取值
	 //Garma: weighting factor 從0.15和0.2之間選一個 
	 
	 public static int[] QRS(float[] lowPass, int nsamp) {
	        int[] QRS = new int[nsamp];
	 
	        double treshold = 0;
	 
	        //先從所有值中找出最大值當Threshold
	        for(int i=0; i<200; i++) {
	            if(lowPass[i] > treshold) {
	                treshold = lowPass[i];
	            }
	        }
	 
	        int frame = 250; //window size 取前250個中最大的值當PEAK
	        
	        for(int i=0; i<lowPass.length; i+=frame) { //每250筆data算一次
	            float max = 0;
	            int index = 0;
	            if(i + frame > lowPass.length) { //如果超過則為最後一個
	                index = lowPass.length;
	            }
	            else {
	                index = i + frame;
	            }
	            for(int j=i; j<index; j++) {
	                if(lowPass[j] > max) max = lowPass[j]; //250個data中的最大值
	            }
	            boolean added = false;
	            for(int j=i; j<index; j++) {
	                if(lowPass[j] > treshold && !added) {
	                    QRS[j] = 1; //找到R點，250個裡面就不再繼續找 (約0.5秒)
	                    			//若之後改成real time則frame可以改為1
	                    added = true;
	                }
	                else {
	                    QRS[j] = 0;
	                }
	            }	 
	            double gama = (Math.random() > 0.5) ? 0.15 : 0.20;
	            double alpha = 0.01 + (Math.random() * ((0.1 - 0.01)));
	 
	            treshold = alpha * gama * max + (1 - alpha) * treshold;	 
	        }
	 
	        return QRS;
	    }

}
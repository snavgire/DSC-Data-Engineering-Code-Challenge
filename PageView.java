/**
 * 
 * @author sagarnavgire
 *	sagar.navgire@asu.edu
 */

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.FileNotFoundException;
import java.util.Date;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Collections;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

public class PageView{
	
	String eventId;
	Date collectorTstamp; 
	String domainUserId;
	String pageUrlPath;
	String nextEventId;
	static final String  CVS_SPLIT_BY = ",";
	HashMap <String, ArrayList<PageView>> usersLists; 

	/**
	 * Parameterized constructor - Initializes the PageView object with the passed arguments.
	 * @param eventid
	 * @param collector_tstamp
	 * @param domain_userid
	 * @param page_urlpath
	 * @param next_event_id
	 */
	public PageView(String eventId, Date collectorTstamp, String domainUserId, String pageUrlPath,
			String nextEventId) {
		this.eventId = eventId;
		this.collectorTstamp = collectorTstamp;
		this.domainUserId = domainUserId;
		this.pageUrlPath = pageUrlPath;
		this.nextEventId = nextEventId;	
	}
	
	/**
	 *  Default constructor.
	 */
	public PageView() {
		usersLists  = new HashMap<String, ArrayList<PageView>>();
	}

	public String getEventId() {
		return eventId;
	}

	public void setNextEventId(String nextEventId) {
		this.nextEventId = nextEventId;
	}

	public Date getCollectorTstamp() {
		return collectorTstamp;
	}

	@Override
	public String toString() {
		return eventId + "," + collectorTstamp + "," + domainUserId + "," + pageUrlPath + "," + nextEventId;
	}
	
	/**
	 * Reads the input file and fills the HashMap with domainUserId (Key) and PageViews (Value).
	 * @param inputFile
	 */
	void readRecords(String inputFile){
		BufferedReader br = null;
		String line = "";
		
		try{
			br = new BufferedReader(new FileReader(inputFile));
			br.readLine();
			while ((line = br.readLine()) != null) {
				String[] tokens = line.split(CVS_SPLIT_BY);
				String currentUserId = tokens[2];
				
				String dateString = tokens[1].replace(' ','T');
				DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.S");
				Date date = formatter.parse(dateString);
				
				PageView temp = new PageView(tokens[0],date,tokens[2],tokens[3],null);

				//Adding a PageView record to an already existing domainUserId (Key).
				if(usersLists.containsKey(currentUserId)){
					usersLists.get(currentUserId).add(temp);
				}
				//Creating a new ArrayList and adding the PageView record to the newly read domainUserId.
				else{
					ArrayList<PageView> newArrayList = new ArrayList<>();
					newArrayList.add(temp);
					usersLists.put(currentUserId, newArrayList);
				}
			}
		}
		catch(FileNotFoundException e){
			e.printStackTrace();
		}
		catch(IOException e){
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		}
		finally{
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace(); 
				}
			}
		}
	}
	
	/**
	 * Sorts the ArrayLists according to the timeStamps and 
	 * adds the NextEventId for each PageView with the successors EventId(if any).
	 */
	void sortAndLink(){
		for(String s: usersLists.keySet()){
			ArrayList<PageView> tmpList=usersLists.get(s);
			
			Collections.sort(tmpList,new Comparator<PageView>() {

				@Override
				public int compare(PageView pv1, PageView pv2) {
					return pv1.getCollectorTstamp().compareTo(pv2.getCollectorTstamp());
				}
			});

			for(int i=0;i<tmpList.size()-1;i++)
			{
				PageView temp = tmpList.get(i);
				temp.setNextEventId(tmpList.get(i+1).getEventId());
			}
		}
	}
	
	/**
	 * Writes the records in the outputFile passed.
	 * The outputFile is sorted based on timeStamps.
	 * @param file
	 */
	boolean writeRecords(File file) {
		
		FileOutputStream fileOutputStream = null;
	
		try {
			
			fileOutputStream = new FileOutputStream(file, false);
			
			if (!file.exists()) {
				file.createNewFile();
			}

			fileOutputStream.write("event_id,event_id,domain_userid,page_urlpath,next_event_id\n".getBytes());
			
			for(String s: usersLists.keySet())
			{
				StringBuilder stringBuilder = new StringBuilder();
				ArrayList<PageView> tmpList=usersLists.get(s);

				for(int i=0;i<tmpList.size();i++)
				{
					PageView temp = tmpList.get(i);
					stringBuilder.append(temp.toString());
					stringBuilder.append("\n");
				}	
				fileOutputStream.write(stringBuilder.toString().getBytes());
			}
			fileOutputStream.flush();
			fileOutputStream.close();
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}

	//Main function
	public static void main(String[] args) {
		
		//Error handling if the filename is not received as an argument.
		if(args.length < 1)
		{
			System.out.println("Please pass file name.");
			System.exit(1);
		}
		
		String inputFile = args[0];
		String outputFileString = "output.csv";
		
		PageView pv = new PageView( );
		pv.readRecords(inputFile);
		pv.sortAndLink();
		
		//Create outputFile and calls the writeRecords() method.
		File outputFile = new File(outputFileString);
		if(pv.writeRecords(outputFile)){
			System.out.println("SUCCESS!\nOutput file has been created "+outputFile.getAbsolutePath());
		}
		else{
			System.out.println("Error.");
		}
	}
}
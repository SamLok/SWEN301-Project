package file;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import service.Mail;
import service.Parcel;

public class XMLWorker {


	/**
	 * Method which given a file name will seek out all tags with the given name
	 * and build an arraylist containing the data
	 * 
	 * @param filename XML file to read
	 * @param tagname Tag to get data of
	 * @return
	 * @throws Exception
	 */
	public static ArrayList<String> readTag(String filename, String tagname) throws Exception{

		//get the root element
		Element docEle = getRootElement(filename);
		//get a nodelist of elements
		ArrayList<String> dat = new ArrayList<String>();
		NodeList nl = docEle.getElementsByTagName(tagname);
		// The list of nodes in indent
		if(nl != null && nl.getLength() > 0) {
			for(int i = 0 ; i < nl.getLength();i++) {
				Element e = (Element)nl.item(i);
				dat.add(e.getTextContent());
			}
		}


		return dat;
	}
	

	/**
	 * Method which reads in the data of multiple tags in the given file.
	 * Each arraylist will be in the corresponding order as tagname
	 * 
	 * An example will illustrate how the data is returned.
	 * 
	 * tagname = new String[]{"mailevents", "day", "to", "from"}
	 * day data arrayList = returnedArrayList.get(0);
	 * to data arrayList = returnedArrayList.get(1);
	 * from data arrayList = returnedArrayList.get(2);
	 * 
	 * @param filename XML file to read
	 * @param tagnames Tags to be read
	 * @return ArrayList of ArrayLists<String> of desired data. Will be returned empty if <br>
	 * 		   no data exists.
	 * @throws Exception
	 */
	public static ArrayList<ArrayList<String>> readTags(String filename, String ... tagnames) throws Exception{

		//get the root element
		Element root = getRootElement(filename);
		
		ArrayList<ArrayList<String>> tagData = new ArrayList<ArrayList<String>>();
		for (int k = 0; k < tagnames.length; k++){
			// This gets EVERY tag with given name within root 
			NodeList nl = root.getElementsByTagName(tagnames[k]);
			ArrayList<String> dat = new ArrayList<String>();
			// The list of nodes in indent
			if(nl != null && nl.getLength() > 0) {
				for(int i = 0 ; i < nl.getLength();i++) {
					Element e = (Element)nl.item(i);
					dat.add(e.getTextContent());
				}
				tagData.add(dat);
			}
		}

		return tagData;
	}
		
	
	
	/**
	 * Method which loads the given xml file, and returns the root of the xml's tree.
	 * (This is the tag which contains ALL the data).
	 * @param filename XML File to read
	 * @return root node
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 */
	private static Element getRootElement(String filename) throws ParserConfigurationException, SAXException, IOException{
		File file = new File(filename + ".xml");

		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();		
		DocumentBuilder db = dbf.newDocumentBuilder();
		Document doc = db.parse(file);
		doc.getDocumentElement().normalize();
		
		//get the root element
		return doc.getDocumentElement();		
	}
	
	/**
	 * Method which reads in the list of countries. 
	 * @return List of countries
	 */
	public static ArrayList<String> loadCountries(){
		ArrayList<String> data = new ArrayList<String>();
		
		try { data = readTag("countries", "country"); } 
		catch (Exception e) { e.printStackTrace(); }
		
		return data;
	}
	
	
	/**
	 * Method which digs out all mail events from the xml file EXCLUDING packages. Packages<br>
	 * can be sought with getPackages(), and allMail().
	 * @return List of all non parcel mail
	 */
	public static ArrayList<Mail> getMail(){
		ArrayList<Mail> mail = new ArrayList<Mail>();
		String[] tagnames = new String[]{"day", "to", "from", "priority"};
		
		try {
			// Get all mail
			ArrayList<ArrayList<String>> maildata = readTags("mailevents", tagnames);
			
			for (int i = 0; i < maildata.get(0).size(); i++){
				String[] data = new String[tagnames.length];
				for (ArrayList<String> array: maildata)
					data[maildata.indexOf(array)] = array.get(i);
				
				mail.add(new Mail(data[0], data[1], data[2], Integer.parseInt(data[3])));
			}
			
			Collections.sort(mail, Mail.mailDateAscending());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return mail;
	}
	
/*	public static ArrayList<Parcel> getParcels() throws ParserConfigurationException, SAXException, IOException{
		//get the root element
		Element root = getRootElement("mailevents");
		ArrayList<Parcel> parcels = new ArrayList<Parcel>();
		String[] tagnames = new String[]{"day", "to", "from", "weight", "volume", "priority"};
		
		NodeList rootList = root.getElementsByTagName("parcel");
		for (int i = 0; i < rootList.getLength(); i++){
			
			for (int k = 0; k < tagnames.length; k++){
				// This gets EVERY tag with given name within root
				Element e = (Element)rootList.item(i);
				NodeList nl = e.getElementsByTagName(tagnames[k]);
				
				ArrayList<String> dat = new ArrayList<String>();
				// The list of nodes in indent
				if(nl != null && nl.getLength() > 0) {
					for(int j = 0 ; j < nl.getLength();j++) {
						Element el = (Element)nl.item(j);
						dat.add(el.getTextContent());
					}
					tagData.add(dat);
				}
			}	
		}
		
		
		return parcels;
	}*/
	/**
	 * Method which takes a list of Mail and returns a new List of Mail where the dates are <br>
	 * between the range specified
	 * @param mail List to filter
	 * @param date1 
	 * @param date2
	 * @return ArrayList<Mail> mail between given date range.
	 */
	public static ArrayList<Mail> getMailBetweenDates(ArrayList<Mail> mail, String date1, String date2){
		ArrayList<Mail> newList = new ArrayList<Mail>();
		Date d1 = XMLWorker.parseDate(date1);
		Date d2 = XMLWorker.parseDate(date2);
		
		// Make sure d1 is the older date
		if (d1.compareTo(d2) > 0){
			Date temp = d1;
			d1 = d2;
			d2 = temp;
		}
		
		for (Mail m: mail){
			Date day = XMLWorker.parseDate(m.getDay());
			// If mail day is between date range, add to new list
			if (d1.compareTo(day) <= 0 && d2.compareTo(day) >= 0){
				newList.add(m);
			}
		}
		
		return newList;
	}
	
	
	public static Date parseDate(String input){
	      SimpleDateFormat ft = new SimpleDateFormat ("dd/MM/yyyy");
	      Date t; 

	      try { 
	          t = ft.parse(input); 
	          return t;
	      } catch (ParseException e) { 
	          System.out.println("Unparseable using " + ft);
	          return null;
	      }
	   
	}
	
	
	
	
	
	public static void main(String[] args){
	
		loadCountries();
		ArrayList<Mail> am = getMail();
		for (Mail m: am){
			System.out.println(m.toString());
		}
		
		Collections.sort(am, Mail.mailDateAscending());
		System.out.println("\n\n");
		for (Mail m: am){
			System.out.println(m.toString());
		}
		
		Collections.sort(am, Mail.mailDateDescending());
		System.out.println("\n\n");
		for (Mail m: am){
			System.out.println(m.toString());
		}		
		
		
		Collections.sort(am, Mail.mailPriorityAscending());
		System.out.println("\n\n");
		for (Mail m: am){
			System.out.println(m.toString());
		}		
		
		Collections.sort(am, Mail.mailPriorityDescending());
		System.out.println("\n\n");
		for (Mail m: am){
			System.out.println(m.toString());
		}		
		
		ArrayList<Mail> filtered = XMLWorker.getMailBetweenDates(am, "10/05/2013", "09/06/2013");
		
		System.out.println("\n\n");
		for (Mail m: filtered){
			System.out.println(m.toString());
		}	
	}

}
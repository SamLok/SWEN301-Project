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
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
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
	 * Method which loads the given xml file, and returns the root of the xml's tree.
	 * (This is the tag which contains ALL the data).
	 * @param filename XML File to read
	 * @return root node
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 */
	private static Document getDocElement(String filename) throws ParserConfigurationException, SAXException, IOException{
		File file = new File(filename + ".xml");

		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();		
		DocumentBuilder db = dbf.newDocumentBuilder();
		Document doc = db.parse(file);
		doc.getDocumentElement().normalize();

		//get the root element
		return doc;		
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
	 * 
	 * @param fileName
	 * @param keyTag
	 * @param tags
	 * @param match
	 * @return
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 */
	public static ArrayList<ArrayList<String>> readTagsConditional(String fileName, String keyTag, String[] tags, String[] match) throws ParserConfigurationException, SAXException, IOException{
		Element root = getRootElement(fileName);
		ArrayList<ArrayList<String>> retArr = new ArrayList<ArrayList<String>>();

		NodeList rootList = root.getElementsByTagName(keyTag);
		for (int i = 0; i < rootList.getLength(); i++){
			ArrayList<String> dat = new ArrayList<String>();
			Element e = (Element)rootList.item(i);
			System.out.println(e.getNodeName());
			boolean isOk = true;
			for (int k = 0; k < tags.length; k++){
				NodeList nl = e.getElementsByTagName(tags[k]);
				// The list of nodes in indent
				if(nl != null && nl.getLength() > 0) {
					Element el = (Element)nl.item(0);
					
					if (match[k] == null || (match[k]!= null && el.getTextContent().equals(match[k])))
						dat.add(el.getTextContent());
					else isOk = false;
				}
			}	
			if(isOk)
				retArr.add(dat);
		}

		return retArr;
	}


	/**
	 * Method which digs out all parcel events from the xml file excluding mail <br>
	 * with the given conditions. <br> 
	 * -The match length must be 6, and in the order {"day", "to", "from", "weight", "volume", "priority"} 
	 * -If you do not want a condition on the corresponding match index <br>
	 *  make the match string null;<br>
	 *  -Date should be in format dd/mm/yyyy.<br><br>
	 *  i.e.<br>
	 *  match = new String[]{"13/01/2013", null, null, null, null, null} <br><br>
	 * 
	 * 	This would return all parcel events on the 13/01/2013
	 * @param match Data must match at the corresponding tag to be included in list.
	 * @return List of parcels with given parameter
	 */
	public static ArrayList<Parcel> getParcels(String[] match){
		ArrayList<Parcel> parcels = new ArrayList<Parcel>();
		try {
			ArrayList<ArrayList<String>> pdata = readTagsConditional("mailevents", "parcel", new String[]{"day", "to", "from", "weight", "volume", "priority"}, match);

			for (ArrayList<String> object: pdata)
				parcels.add(new Parcel(object.get(0), object.get(1), object.get(2), object.get(3), object.get(4), object.get(5)));

		} 
		catch (ParserConfigurationException e) {e.printStackTrace();} 
		catch (SAXException e) {e.printStackTrace();} 
		catch (IOException e) {e.printStackTrace();}

		return parcels;
	}

	/**
	 * Method which digs out all parcel events from the xml file excluding mail <br>
	 * with the given conditions. <br> 
	 * -The match length must be 4, and in the order {"day", "to", "from", "priority"} 
	 * -If you do not want a condition on the corresponding match index <br>
	 *  make the match string null;<br>
	 *  -Date should be in format dd/mm/yyyy.<br><br>
	 *  i.e.<br>
	 *  match = new String[]{"13/01/2013", null, null, null} <br><br>
	 * 
	 * 	This would return all parcel events on the 13/01/2013
	 * @param match Data must match at the corresponding tag to be included in list.
	 * @return List of parcels with given parameter
	 */
	public static ArrayList<Mail> getMail(String[] match){
		ArrayList<Mail> mail = new ArrayList<Mail>();
		try {
			ArrayList<ArrayList<String>> pdata = readTagsConditional("mailevents", "mail", new String[]{"day", "to", "from", "priority"}, match);

			for (ArrayList<String> object: pdata)
				mail.add(new Mail(object.get(0), object.get(1), object.get(2), object.get(3)));

		} 
		catch (ParserConfigurationException e) {e.printStackTrace();} 
		catch (SAXException e) {e.printStackTrace();} 
		catch (IOException e) {e.printStackTrace();}

		return mail;
	}
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

	/**
	 * Adds a new mail and or parcel event to the mailevents database. Can be as a Mail <br>
	 * object or Parcel.
	 * 
	 * @param mail Object to save data of
	 */
	public static void addMail(Mail mail) {
		Document doc;
		try {
			doc = getDocElement("mailevents");
		Element root = doc.getDocumentElement();
		
		Element newMail = doc.createElement("mail");
		String[] tags = new String[]{"day", "to", "from", "priority"};
		String[] data = mail.getData();
		
		if (mail instanceof Parcel){
			newMail = doc.createElement("parcel");
			tags = new String[]{"day", "to", "from", "weight", "volume", "priority"};
			data = ((Parcel)mail).getData();
		}
		
		root.appendChild(newMail);
		for (int i = 0; i < tags.length; i++){
			Element e = doc.createElement(tags[i]);
			e.appendChild(doc.createTextNode(data[i]));
			newMail.appendChild(e);
		}
		
		finishWritingXML(doc);
		}catch(Exception e){e.printStackTrace();}
	}

	private static void finishWritingXML(Document doc) throws TransformerException{
		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		Transformer transformer = transformerFactory.newTransformer();
		transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		DOMSource source = new DOMSource(doc);
		StreamResult result = new StreamResult(new File("mailevents.xml"));
 
		// Output to console for testing
		// StreamResult result = new StreamResult(System.out);
 
		transformer.transform(source, result);
	}


	public static void main(String[] args){


		ArrayList<Parcel> parcels = XMLWorker.getParcels(new String[]{null, null, null, null, null, null});
		for (Parcel p: parcels){
			System.out.println("Parcel: " + p.toString());
		}

		ArrayList<Mail> mail = XMLWorker.getMail(new String[]{null, null, null, null});
		for (Mail m: mail){
			System.out.println("Mail: " + m.toString());
		}		

		XMLWorker.addMail(new Mail("02/02/2012", "Madrid", "Palmerston North", 1));
		XMLWorker.addMail(new Parcel("02/04/2012", "Ho-Chi Min", "Clive", "2","5", 2));
		
	
		
		/*
		try {
			ArrayList<ArrayList<String>> arr = 	XMLWorker.readTagsConditional("mailevents", "parcel", 
					new String[]{"day", "to", "from", "priority", "weight", "volume"}, 
						new String[]{null, null, null, null, "3", null});
			for (ArrayList<String> array: arr){
				System.out.println("##### new Object #####");
				System.out.println(array + "\n");
			}
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
		/*		loadCountries();
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
		}	*/
	}

}

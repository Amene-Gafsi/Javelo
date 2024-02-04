package ch.epfl.javelo.routing;

import ch.epfl.javelo.gui.Waypoint;
import ch.epfl.javelo.projection.PointCh;
import javafx.collections.ObservableList;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


/**
 * This class represents a route generator in GPX format.
 *
 * @author Morgane Magnin (347041)
 * @author Amene Gafsi (345583)
 */
public class GpxGenerator {

	private GpxGenerator() {
	}

	/**
	 * Creates a document in GPX format describing the given route.
	 *
	 * @param route        the route.
	 * @param routeProfile the profile of the given route.
	 * @param waypoints    the list of waypoints.
	 * @return the GPX document corresponding to the itinerary.
	 */
	public static Document createGpx(Route route, ElevationProfile routeProfile, ObservableList<Waypoint> waypoints) {
		Document doc = newDocument();

		Element root = doc
				.createElementNS("http://www.topografix.com/GPX/1/1",
						"gpx");
		doc.appendChild(root);

		root.setAttributeNS(
				"http://www.w3.org/2001/XMLSchema-instance",
				"xsi:schemaLocation",
				"http://www.topografix.com/GPX/1/1 "
						+ "http://www.topografix.com/GPX/1/1/gpx.xsd");
		root.setAttribute("version", "1.1");
		root.setAttribute("creator", "JaVelo");

		Element metadata = doc.createElement("metadata");
		root.appendChild(metadata);

		Element name = doc.createElement("name");
		metadata.appendChild(name);
		name.setTextContent("Route JaVelo");

		Element rte = doc.createElement("rte");
		root.appendChild(rte);

		// We calculate the position for each point along the route by taking the length of the edges.
		// The first point has then a position = 0 and the last point has a position corresponding to the length of the route.
		List<Edge> allEdges = route.edges();
		double[] positions = new double[allEdges.size() + 1];
		positions[0] = 0.0;
		for (int i = 1; i < positions.length; i++) {
			positions[i] = positions[i - 1] + allEdges.get(i - 1).length();
		}
		assert route.points().size() == positions.length;

		int index = 0;
		for (PointCh p : route.points()) {
			Element rtept = doc.createElement("rtept");
			rte.appendChild(rtept);
			rtept.setAttribute("lat", Double.toString(Math.toDegrees(p.lat())));
			rtept.setAttribute("lon", Double.toString(Math.toDegrees(p.lon())));

			Element ele = doc.createElement("ele");
			rtept.appendChild(ele);
			ele.setTextContent(Double.toString(routeProfile.elevationAt(positions[index])));
			index++;
		}

		// the list of waypoints is added to the document
		for (Waypoint p : waypoints) {
			Element wayPoint = doc.createElement("waypoint");
			rte.appendChild(wayPoint);
			wayPoint.setAttribute("e", Double.toString(p.swissCoord().e()));
			wayPoint.setAttribute("n", Double.toString(p.swissCoord().n()));
			wayPoint.setAttribute("closestNodeId", Integer.toString(p.closestNodeId()));
		}
		return doc;
	}

	/**
	 * Writes the GPX document corresponding to the given route in the file chosen by the user.
	 *
	 * @param route        the route.
	 * @param routeProfile the profile of the route.
	 * @param waypoints    the list of waypoints.
	 * @throws IOException in case of an input/output error.
	 */
	public static void writeGpx(Route route, ElevationProfile routeProfile, ObservableList<Waypoint> waypoints) throws IOException {
		Document doc = createGpx(route, routeProfile, waypoints);
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setCurrentDirectory(new File("."));
		if (fileChooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
			File file = fileChooser.getSelectedFile();
			String path = file.getPath() + ".gpx";
			file = new File(path);
			FileWriter w = new FileWriter(file.getPath(), true);
			try {
				Transformer transformer = TransformerFactory
						.newDefaultInstance()
						.newTransformer();
				transformer.setOutputProperty(OutputKeys.INDENT, "yes");
				transformer.transform(new DOMSource(doc),
						new StreamResult(w));
			} catch (TransformerException e) {
				throw new Error(e);
			}
		}
	}

	/**
	 * Reads the GPX document corresponding to the given route in the file chosen by the user and gives it's list of waypoints.
	 *
	 * @return the list of waypoints.
	 */
	public static List<Waypoint> readGpx() throws ParserConfigurationException, SAXException {
		List<Waypoint> routePoints = new ArrayList<>();

		try {
			JFileChooser fileChooser = new JFileChooser();
			fileChooser.setCurrentDirectory(new File("."));
			FileNameExtensionFilter filter = new FileNameExtensionFilter("gpx", "gpx");
			fileChooser.setFileFilter(filter);

			int returnVal = fileChooser.showOpenDialog(null);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				File file = fileChooser.getSelectedFile();
				DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
				DocumentBuilder db = dbf.newDocumentBuilder();
				Document document = db.parse(file);
				document.getDocumentElement().normalize();
				NodeList nList = document.getElementsByTagName("waypoint");
				for (int temp = 0; temp < nList.getLength(); temp++) {
					Node nNode = nList.item(temp);
					if (nNode.getNodeType() == Node.ELEMENT_NODE) {
						Element eElement = (Element) nNode;
						PointCh swissCoords = new PointCh(Double.parseDouble(eElement.getAttribute("e")), Double.parseDouble(eElement.getAttribute("n")));
						routePoints.add(new Waypoint(swissCoords, Integer.parseInt(eElement.getAttribute("closestNodeId"))));
					}
				}
			}
		} catch (IOException e) {
			System.out.println(e);
		}
		return routePoints;
	}


	/**
	 * Creates a new document.
	 * This method is used above in the method createGPX.
	 *
	 * @return a new document.
	 */
	private static Document newDocument() {
		try {
			return DocumentBuilderFactory
					.newDefaultInstance()
					.newDocumentBuilder()
					.newDocument();
		} catch (ParserConfigurationException e) {
			throw new Error(e); // Should never happen
		}
	}
}


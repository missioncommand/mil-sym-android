package sec.geo.kml;

import armyc2.c2sd.graphics2d.PathIterator;
import sec.sun.awt.geom.AreaIterator;
import java.util.ArrayList;

import sec.geo.shape.*;

public class KmlRenderer {

    private static final String EXCEPTION = "EXCEPTION:";

    private static final String colorDefault = "ff003fff";
    private static final String descriptionField = "#DESCRIPTION#";
    private static final String extendedData = "#EXTENDEDDATA#";
    private static final String colorField = "#COLOR#";
    private static final String nameField = "#NAME#";
    private static final String idField = "#ID#";
    private static final String placemarkidField = "#PLACEMARKID#";

    private static final String KML_START = "<Folder id=\"" + idField + "\">\n";
    private static final String KML_END = "</Folder>\n";

    private static final String PLACEMARK_START = ""
            + "		<Placemark id=\"" + placemarkidField + "\">\n"
            + "                      <Style>\n"
            + "                          <PolyStyle>\n"
            + "                              <color>" + colorField + "</color>\n"
            + "                          </PolyStyle>\n"
            + "                          <LineStyle>\n"
            + "                              <color>" + colorField + "</color>\n"
            + "                          </LineStyle>\n"
            + "                       </Style>\n"
            + "			<name>" + nameField + "</name>\n"
            + "			<description>" + descriptionField + "</description>\n"
            + "			<ExtendedData>" + extendedData + "</ExtendedData>\n"
            + "			<MultiGeometry>\n";

    private static final String PLACEMARK_END = ""
            + "			</MultiGeometry>\n"
            + "		</Placemark>\n";

	//private static final String DEFAULT_EXDAT = "<Data name='sid'><value>#ID#</value></Data><Data name='shapeType'><value>#SHAPETYPE#</value></Data><Data name='lat'><value>#LAT#</value></Data><Data name='lon'><value>#LON#</value></Data><Data name='alt'><value>#ALT#</value></Data>";
    //private static final String DEFAULT_BLSTY = "<!" + "[CDAT" + "A[" + "$[sid]" +"]]"+">";
    public ArrayList<KmlPolygon> renderPolygons(AExtObject ext) {
        ArrayList<KmlPolygon> polys = new ArrayList<KmlPolygon>();

        ext.setMaxDistance(200000);
        //ext.setFlatness(1);
        //ext.setLimit(3);
        ext.setFlatness(2);
        ext.setLimit(8);

        // Render perimeter polys
        ArrayList<Point> perimeterPoints = new ArrayList<Point>();
        PathIterator it = null;
        AreaIterator ait = null;
        //PathIterator it = ext.getShape().getPathIterator(null);
        Object oit = ext.getPathIterator(null);
        if (oit instanceof PathIterator) {
            it = (PathIterator) oit;
        } else if (oit instanceof AreaIterator) {
            ait = (AreaIterator) oit;
        }

        Point pre = null;
        if (it != null) {
            while (!it.isDone()) {

                double[] strokePoints = new double[6];
                int type = it.currentSegment(strokePoints);

                double longitudeDegrees = strokePoints[0];
                double latitudeDegrees = strokePoints[1];
                if (longitudeDegrees < -180) {
                    longitudeDegrees += 360;
                }
                switch (type) {
                    case PathIterator.SEG_MOVETO:
                    case PathIterator.SEG_LINETO:
                        if (pre != null) {
                            ArrayList<Point> ps = new ArrayList<Point>();
                            ps.add(new Point(pre.getLongitude(), pre.getLatitude(), ext.getMinAltitude()));
                            ps.add(new Point(pre.getLongitude(), pre.getLatitude(), ext.getMaxAltitude()));
                            ps.add(new Point(longitudeDegrees, latitudeDegrees, ext.getMaxAltitude()));
                            ps.add(new Point(longitudeDegrees, latitudeDegrees, ext.getMinAltitude()));
                            ps.add(new Point(pre.getLongitude(), pre.getLatitude(), ext.getMinAltitude()));
                            polys.add(new KmlPolygon(ps, ext.getAltitudeMode()));
                        }
                        pre = new Point(longitudeDegrees, latitudeDegrees);
                        perimeterPoints.add(pre);
                }
                it.next();
            }
        } else if (ait != null) {
            while (!ait.isDone()) {

                double[] strokePoints = new double[6];
                int type = ait.currentSegment(strokePoints);

                double longitudeDegrees = strokePoints[0];
                double latitudeDegrees = strokePoints[1];
                if (longitudeDegrees < -180) {
                    longitudeDegrees += 360;
                }
                switch (type) {
                    case PathIterator.SEG_MOVETO:
                    case PathIterator.SEG_LINETO:
                        if (pre != null) {
                            ArrayList<Point> ps = new ArrayList<Point>();
                            ps.add(new Point(pre.getLongitude(), pre.getLatitude(), ext.getMinAltitude()));
                            ps.add(new Point(pre.getLongitude(), pre.getLatitude(), ext.getMaxAltitude()));
                            ps.add(new Point(longitudeDegrees, latitudeDegrees, ext.getMaxAltitude()));
                            ps.add(new Point(longitudeDegrees, latitudeDegrees, ext.getMinAltitude()));
                            ps.add(new Point(pre.getLongitude(), pre.getLatitude(), ext.getMinAltitude()));
                            polys.add(new KmlPolygon(ps, ext.getAltitudeMode()));
                        }
                        pre = new Point(longitudeDegrees, latitudeDegrees);
                        perimeterPoints.add(pre);
                }
                ait.next();
            }
        }
        // Render top and bottom poly if the perimeter is complete
        if (perimeterPoints.size() > 0) {
                    // In some weird cases, for routes, when it builds an area, it will drop the closing
            // point in the shape.   Route uses an area.  This causes this condition to not execute.
            // adding the first point to the perimeterPoints fixes the issue, not sure if it causes any
            // side effects.
            if (perimeterPoints.get(0).equals(perimeterPoints.get(perimeterPoints.size() - 1))) {

                polys.add(new KmlPolygon(transformPoints(perimeterPoints, ext.getMinAltitude()), ext.getAltitudeMode()));
                polys.add(new KmlPolygon(transformPoints(perimeterPoints, ext.getMaxAltitude()), ext.getAltitudeMode()));
            } else {
                perimeterPoints.add(perimeterPoints.get(0));

                polys.add(new KmlPolygon(transformPoints(perimeterPoints, ext.getMinAltitude()), ext.getAltitudeMode()));
                polys.add(new KmlPolygon(transformPoints(perimeterPoints, ext.getMaxAltitude()), ext.getAltitudeMode()));
            }
        }

        return polys;
    }

    //public String getPlacemarkKml(AExtrusion ext, String id, String name, String description, String lineColor, String fillColor) {
    public String getPlacemarkKml(AExtObject ext, String id, String name, String description, String lineColor, String fillColor) {

        try {
            StringBuilder sb = new StringBuilder();
            ArrayList<KmlPolygon> polys = renderPolygons(ext);
            sb.append(PLACEMARK_START);
            int placemarkIdIndex = sb.indexOf(placemarkidField);
            int placemarkIdLength = placemarkidField.length();
            if (id != null) {
                sb.replace(placemarkIdIndex, placemarkIdIndex + placemarkIdLength, id + "_mg");
            }

            int descriptionIndex = sb.indexOf(descriptionField);
            int descriptionLength = descriptionField.length();
            if (description != null) {
                sb.replace(descriptionIndex, descriptionIndex + descriptionLength, description);
            }
            int colorIndex = sb.indexOf(colorField);
            int colorLength = colorField.length();
            if (fillColor != null) {
                sb.replace(colorIndex, colorIndex + colorLength, fillColor);
            } else {
                sb.replace(colorIndex, colorIndex + colorLength, colorDefault);
            }

            int lineColorIndex = sb.indexOf(colorField, colorIndex + colorLength);
            if (lineColor != null) {
                sb.replace(lineColorIndex, lineColorIndex + colorLength, lineColor);
            } else {
                sb.replace(lineColorIndex, lineColorIndex + colorLength, colorDefault);
            }

            int nameIndex = sb.indexOf(nameField);
            int nameLength = nameField.length();
            if (name != null) {
                sb.replace(nameIndex, nameIndex + nameLength, name);
            }

            for (KmlPolygon poly : polys) {

                sb.append(poly.toString());
            }

            sb.append(PLACEMARK_END);
            return sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return EXCEPTION + e.getMessage();
        }

    }

    //public String getKml(AExtrusion ext, String id, String name, String description, String lineColor, String fillColor) {

    public String getKml(Object ext, String id, String name, String description, String lineColor, String fillColor) {
        try {
            AExtObject aext = getAExtObject(ext);
            if (aext.getElements() != null) {
                return (getTrackKml(ext, id, name, description, lineColor, fillColor));
            }
            StringBuilder sb = new StringBuilder();
            sb.append(KML_START);
            int idIndex = sb.indexOf(idField);
            int idLength = idField.length();
            sb.replace(idIndex, idIndex + idLength, id);
            sb.append(getPlacemarkKml(aext, id, name, description, lineColor, fillColor));
            sb.append(KML_END);
            return sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return EXCEPTION + e.getMessage();
        }
    }

    public String getTrackKml(Object ext, String id, String name, String description, String lineColor, String fillColor) {
        try {
            AExtObject aext = getAExtObject(ext);
            StringBuilder sb = new StringBuilder();
            sb.append(this.KML_START);
            int idIndex = sb.indexOf(this.idField);
            int idLength = idField.length();
            sb.replace(idIndex, idIndex + idLength, id);

            ArrayList elements = aext.getElements();
            int j = 0;
            int n = elements.size();
            //for (j = 0; j < elements.size(); j++)
            for (j = 0; j < n; j++) {
                Route route = (Route) elements.get(j);
                AExtObject aext2 = new sec.geo.shape.AExtObject(route);
                sb.append(this.getPlacemarkKml(aext2, id, name, description, lineColor, fillColor));
            }

            sb.append(this.KML_END);
            return sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return EXCEPTION + e.getMessage();
        }
    }

    public String getCakeKml(Cake com, String id, String name, String description, String lineColor, String fillColor) {
        StringBuilder sb = new StringBuilder();
        //Set<KmlPolygon> polys;
        sb.append(KML_START);
        int idIndex = sb.indexOf(idField);
        int idLength = idField.length();
        sb.replace(idIndex, idIndex + idLength, id);

        for (Object ext : com.getElements()) {
            AExtObject aext = new AExtObject(ext);
            String extStr = getPlacemarkKml(aext, id, name, description, lineColor, fillColor);

            if (!extStr.startsWith(EXCEPTION)) {
                sb.append(extStr);
            }
        }

        sb.append(KML_END);
        return sb.toString();
    }

    private AExtObject getAExtObject(Object obj) {
        Line line = null;
        Route route = null;
        Circle circle = null;
        Polyarc polyarc = null;
        Polygon polygon = null;
        Orbit orbit = null;
        Radarc radarc = null;
        Track track = null;
        Cake cake = null;
        Point point = null;
        AExtObject ext = null;
        if (obj instanceof Line) {
            line = (Line) obj;
            ext = new AExtObject(line);
        } else if (obj instanceof Route) {
            route = (Route) obj;
            ext = new AExtObject(route);
        } else if (obj instanceof Polyarc) {
            polyarc = (Polyarc) obj;
            ext = new AExtObject(polyarc);
        } else if (obj instanceof Orbit) {
            orbit = (Orbit) obj;
            ext = new AExtObject(orbit);
        } else if (obj instanceof Polygon) {
            polygon = (Polygon) obj;
            ext = new AExtObject(polygon);
        } else if (obj instanceof Circle) {
            circle = (Circle) obj;
            ext = new AExtObject(circle);
        } else if (obj instanceof Radarc) {
            radarc = (Radarc) obj;
            ext = new AExtObject(radarc);
        } else if (obj instanceof Track) {
            track = (Track) obj;
            ext = new AExtObject(track);
        } else if (obj instanceof Cake) {
            cake = (Cake) obj;
            ext = new AExtObject(cake);
        } else if (obj instanceof Point) {
            point = (Point) obj;
            ext = new AExtObject(point);
        }
        return ext;
    }

    public String[] getCoords(Object obj) {
        AExtObject ext = getAExtObject(obj);
        ArrayList<KmlPolygon> polys = renderPolygons(ext);

        // Iterate through the polygons and produce an array of KML coordinates
        String[] coords = new String[polys.size()];
        int i = 0;
        for (KmlPolygon poly : polys) {
            coords[i] = poly.toCoordString();
            i++;
        }
        return coords;
    }

    private ArrayList<Point> transformPoints(ArrayList<Point> points, double altitudeMeters) {
        ArrayList<Point> returnPoints = new ArrayList<Point>();
        for (Point p : points) {
            returnPoints.add(new Point(p.getLongitude(), p.getLatitude(), altitudeMeters));
        }
        return returnPoints;
    }
}

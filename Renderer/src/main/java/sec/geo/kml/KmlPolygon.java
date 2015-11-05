package sec.geo.kml;

import java.util.ArrayList;
import org.gavaghan.geodesy.Ellipsoid;
import sec.geo.kml.KmlOptions.AltitudeMode;
import sec.geo.shape.Point;

public class KmlPolygon {

    private final ArrayList<Point> points;

    private AltitudeMode altitudeMode = AltitudeMode.ABSOLUTE;

    protected static final Ellipsoid REFERENCE_ELLIPSOID = Ellipsoid.WGS84;
    private static final String altitudeModeField = "#ALTITUDEMODE#";

    private static final String PREFIX = ""
            + "				<Polygon>\n"
            + "					<tessellate>1</tessellate>\n"
            + "					<altitudeMode>" + altitudeModeField + "</altitudeMode>\n"
            + "					<outerBoundaryIs><LinearRing><coordinates>";
    private static final String SUFFIX = ""
            + "					</coordinates></LinearRing></outerBoundaryIs>\n"
            + "				</Polygon>\n";

    public KmlPolygon() {
        points = new ArrayList<Point>();
    }

    public KmlPolygon(ArrayList<Point> points, AltitudeMode altitudeMode) {
        this();
        this.points.addAll(points);
        this.altitudeMode = altitudeMode;
    }

    public void addPoint(Point point) {
        points.add(point);
    }

    public void addPoints(ArrayList<Point> points) {
        this.points.addAll(points);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append(PREFIX);
        sb.append(toCoordString());
        sb.append(SUFFIX);

        int altitudeModeIndex = sb.indexOf(altitudeModeField);
        int altitudeModeLength = altitudeModeField.length();
        if (altitudeMode != null) {
            sb.replace(altitudeModeIndex, altitudeModeIndex + altitudeModeLength, altitudeMode.toString());
        }

        return sb.toString();
    }

    public String toCoordString() {
        StringBuilder sb = new StringBuilder();

        ArrayList<Point> orderedPoints = getPointsCounterClockwise();
        if (orderedPoints == null) {
            return "";
        }

        for (Point point : orderedPoints) {
            sb.append(point.getLongitude());
            sb.append(",");
            sb.append(point.getLatitude());
            sb.append(",");
            sb.append(point.getAltitude());
            sb.append(" ");
        }

        // Close off the list of coordinates if necessary
        Point point = orderedPoints.get(0);
        if (!point.equals(orderedPoints.get(orderedPoints.size() - 1))) {
            sb.append(point.getLongitude());
            sb.append(",");
            sb.append(point.getLatitude());
            sb.append(",");
            sb.append(point.getAltitude());
            sb.append(" ");
        }

        return sb.toString();
    }

    public AltitudeMode getAltitudeMode() {
        return altitudeMode;
    }

    public void setAltitudeMode(AltitudeMode altitudeMode) {
        this.altitudeMode = altitudeMode;
    }

    public ArrayList<Point> getPointsClockwise() {  //did return List
        if (points == null || points.size() < 3) {
            return null;
        }

        //List<Point> result = points.subList(0, points.size()-1);
        ArrayList<Point> result = subList(points, 0, points.size() - 1);
        int order = getPointOrder();
        if (order < 0) {
            //Collections.reverse(result);
            result = reverse(result);
            return result;
        } else {
            return result;
        }
    }

    public ArrayList<Point> getPointsCounterClockwise() {   //did return List
        if (points == null || points.size() < 3) {
            return null;
        }

        //List<Point> result = points.subList(0, points.size()-1);
        ArrayList<Point> result = subList(points, 0, points.size() - 1);
        int order = getPointOrder();
        if (order > 0) {
            //Collections.reverse(result);
            result = reverse(result);
            return result;
        } else {
            return result;
        }
    }

    public int getPointOrder() {
        if (points == null || points.size() < 3) {
            return 0;
        }

        int n = points.size();
        int j, k, count = 0;
        double z;
        for (int i = 0; i < n; i++) {
            j = (i + 1) % n;
            k = (i + 2) % n;
            z = (points.get(j).getLongitude() - points.get(i).getLongitude()) * (points.get(k).getLatitude() - points.get(i).getLatitude());
            z -= (points.get(j).getLatitude() - points.get(i).getLatitude()) * (points.get(k).getLongitude() - points.get(i).getLongitude());
            if (z < 0) {
                count--;
            } else if (z > 0) {
                count++;
            }
        }
        if (count > 0) {
            return -1;	//counterclockwise
        } else if (count < 0) {
            return 1;	//clockwise
        } else {
            return 0;	//invalid
        }
    }
    /*
     * ArrayList from fromIndex, inclusive to toIndex, exclusive
     * 
     */

    private ArrayList subList(ArrayList al, int fromIndex, int toIndex) {
        ArrayList result = new ArrayList();
        int j = 0;
        for (j = fromIndex; j < toIndex; j++) {
            result.add(al.get(j));
        }

        return result;
    }
    /*
     * reverses the order of an arraylist
     */

    private ArrayList reverse(ArrayList al) {
        int j = 0;
        ArrayList result = new ArrayList();
        int n = al.size();
        //for(j=al.size()-1;j>=0;j--)            
        for (j = n - 1; j >= 0; j--) {
            result.add(al.get(j));
        }
        return result;
    }

}

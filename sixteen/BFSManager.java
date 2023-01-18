package sixteen;

import battlecode.common.Clock;
import battlecode.common.Direction;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;

public class BFSManager {

    static int MAX_BROADCASTS = 15;

    static final int MAX_BYTECODE = 500;

    RobotController rc;

    BFS[] bfsList = new BFS[MAX_BROADCASTS];
    int index = 0;

    BFSManager(){
        this.rc = Robot.rc;
    }

    int requestBFS(MapLocation location){
        bfsList[index++] = new BFS(location);
        return index-1;
    }

    void run(){
        for (int i = 0; i < index; ++i){
            if (Clock.getBytecodesLeft() < MAX_BYTECODE) return;
            bfsList[i].run();
        }
    }

    Direction getBestDirection(int bfsIndex){
        if (!bfsList[bfsIndex].isReady()) return null;
        MapLocation myLoc = rc.getLocation();
        Direction ans = Direction.CENTER;
        int[][] ds = bfsList[bfsIndex].dists;
        int dist = ds[myLoc.x][myLoc.y];
        if (dist == 0) return null;
        Constants.indicatorString += "BFS " + dist + " ";
        MapLocation loc;
        int newDist;
        if (rc.canMove(Direction.EAST)) {
            loc = myLoc.add(Direction.EAST);
            newDist = ds[loc.x][loc.y];
            Constants.indicatorString += "E" + newDist + " ";
            if (newDist != 0 && newDist < dist) {
                dist = newDist;
                ans = Direction.EAST;
            }
        }
        if (rc.canMove(Direction.NORTHEAST)) {
            loc = myLoc.add(Direction.NORTHEAST);
            newDist = ds[loc.x][loc.y];
            Constants.indicatorString += "NE" + newDist + " ";
            if (newDist != 0 && newDist < dist) {
                dist = newDist;
                ans = Direction.NORTHEAST;
            }
        }
        if (rc.canMove(Direction.NORTH)) {
            loc = myLoc.add(Direction.NORTH);
            newDist = ds[loc.x][loc.y];
            Constants.indicatorString += "N" + newDist + " ";
            if (newDist != 0 && newDist < dist) {
                dist = newDist;
                ans = Direction.NORTH;
            }
        }
        if (rc.canMove(Direction.NORTHWEST)) {
            loc = myLoc.add(Direction.NORTHWEST);
            newDist = ds[loc.x][loc.y];
            Constants.indicatorString += "NW" + newDist + " ";
            if (newDist != 0 && newDist < dist) {
                dist = newDist;
                ans = Direction.NORTHWEST;
            }
        }
        if (rc.canMove(Direction.WEST)) {
            loc = myLoc.add(Direction.WEST);
            newDist = ds[loc.x][loc.y];
            Constants.indicatorString += "W" + newDist + " ";
            if (newDist != 0 && newDist < dist) {
                dist = newDist;
                ans = Direction.WEST;
            }
        }
        if (rc.canMove(Direction.SOUTHWEST)) {
            loc = myLoc.add(Direction.SOUTHWEST);
            newDist = ds[loc.x][loc.y];
            Constants.indicatorString += "SW" + newDist + " ";
            if (newDist != 0 && newDist < dist) {
                dist = newDist;
                ans = Direction.SOUTHWEST;
            }
        }
        if (rc.canMove(Direction.SOUTH)) {
            loc = myLoc.add(Direction.SOUTH);
            newDist = ds[loc.x][loc.y];
            Constants.indicatorString += "S" + newDist + " ";
            if (newDist != 0 && newDist < dist) {
                dist = newDist;
                ans = Direction.SOUTH;
            }
        }
        if (rc.canMove(Direction.SOUTHEAST)) {
            loc = myLoc.add(Direction.SOUTHEAST);
            newDist = ds[loc.x][loc.y];
            Constants.indicatorString += "SE" + newDist + " ";
            if (newDist != 0 && newDist < dist) {
                dist = newDist;
                ans = Direction.SOUTHEAST;
            }
        }
        return ans;
    }



}

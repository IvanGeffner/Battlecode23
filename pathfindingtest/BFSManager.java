package pathfindingtest;

import battlecode.common.*;


public class BFSManager {

    static int MAX_BROADCASTS = 15;

    static final int MAX_BYTECODE = 500;

    RobotController rc;

    BFS[] bfsList = new BFS[MAX_BROADCASTS];

    MapLocation[] buffer = new MapLocation[MAX_BROADCASTS];

    int bufferIndex = 0;

    int index = 0;

    BFSManager(){
        this.rc = Robot.rc;
    }

    int requestBFS(MapLocation location){
        buffer[bufferIndex++] = new MapLocation(location.x, location.y);
        return bufferIndex-1;
    }

    void runBuffer(){
        if (index >= bufferIndex) return;
        if (rc.getRoundNum() <= Robot.roundBirth) return;
        bfsList[index] = new BFS(buffer[index]);
        index++;
    }

    void run(){
        if (rc.getRoundNum()%2 == 0) {
            for (int i = 0; i < index; ++i) {
                if (Clock.getBytecodesLeft() < MAX_BYTECODE) return;
                bfsList[i].runBFS();
            }
        } else {
            for (int i = index; i-- > 0;) {
                if (Clock.getBytecodesLeft() < MAX_BYTECODE) return;
                bfsList[i].runBFS();
            }
        }
    }

    Direction getBestDirection(int bfsIndex) throws GameActionException {
        if (bfsIndex >= index) return null;
        MapLocation myLoc = rc.getLocation();
        Direction ans = Direction.CENTER;
        MapInfo mInfo = rc.senseMapInfo(myLoc);
        myLoc = myLoc.add(mInfo.getCurrentDirection());
        int dist = bfsList[bfsIndex].getDistance(myLoc);

        if (dist == 0) return null;

        myLoc = rc.getLocation();


        Constants.indicatorString += "BFS on index " + bfsIndex + " " + dist + " ";
        MapLocation loc;
        int newDist;



        if (rc.canMove(Direction.EAST)) {
            loc = myLoc.add(Direction.EAST);
            loc = loc.add(rc.senseMapInfo(loc).getCurrentDirection());
            if (rc.canSenseLocation(loc)){
                loc = loc.add(rc.senseMapInfo(loc).getCurrentDirection());
            }
            newDist = bfsList[bfsIndex].getDistance(loc);
            //Constants.indicatorString += "E" + newDist + " ";
            if (newDist != 0 && newDist <= dist) {
                dist = newDist;
                ans = Direction.EAST;
            }
        }
        if (rc.canMove(Direction.NORTHEAST)) {
            loc = myLoc.add(Direction.NORTHEAST);
            loc = loc.add(rc.senseMapInfo(loc).getCurrentDirection());
            if (rc.canSenseLocation(loc)){
                loc = loc.add(rc.senseMapInfo(loc).getCurrentDirection());
            }
            newDist = bfsList[bfsIndex].getDistance(loc);
            //Constants.indicatorString += "NE" + newDist + " ";
            if (newDist != 0 && newDist <= dist) {
                dist = newDist;
                ans = Direction.NORTHEAST;
            }
        }
        if (rc.canMove(Direction.NORTH)) {
            loc = myLoc.add(Direction.NORTH);
            loc = loc.add(rc.senseMapInfo(loc).getCurrentDirection());
            if (rc.canSenseLocation(loc)){
                loc = loc.add(rc.senseMapInfo(loc).getCurrentDirection());
            }
            newDist = bfsList[bfsIndex].getDistance(loc);
            //Constants.indicatorString += "N" + newDist + " ";
            if (newDist != 0 && newDist <= dist) {
                dist = newDist;
                ans = Direction.NORTH;
            }
        }
        if (rc.canMove(Direction.NORTHWEST)) {
            loc = myLoc.add(Direction.NORTHWEST);
            loc = loc.add(rc.senseMapInfo(loc).getCurrentDirection());
            if (rc.canSenseLocation(loc)){
                loc = loc.add(rc.senseMapInfo(loc).getCurrentDirection());
            }
            newDist = bfsList[bfsIndex].getDistance(loc);
            //Constants.indicatorString += "NW" + newDist + " ";
            if (newDist != 0 && newDist <= dist) {
                dist = newDist;
                ans = Direction.NORTHWEST;
            }
        }
        if (rc.canMove(Direction.WEST)) {
            loc = myLoc.add(Direction.WEST);
            loc = loc.add(rc.senseMapInfo(loc).getCurrentDirection());
            if (rc.canSenseLocation(loc)){
                loc = loc.add(rc.senseMapInfo(loc).getCurrentDirection());
            }
            newDist = bfsList[bfsIndex].getDistance(loc);
            //Constants.indicatorString += "W" + newDist + " ";
            if (newDist != 0 && newDist <= dist) {
                dist = newDist;
                ans = Direction.WEST;
            }
        }
        if (rc.canMove(Direction.SOUTHWEST)) {
            loc = myLoc.add(Direction.SOUTHWEST);
            loc = loc.add(rc.senseMapInfo(loc).getCurrentDirection());
            if (rc.canSenseLocation(loc)){
                loc = loc.add(rc.senseMapInfo(loc).getCurrentDirection());
            }
            newDist = bfsList[bfsIndex].getDistance(loc);
            //Constants.indicatorString += "SW" + newDist + " ";
            if (newDist != 0 && newDist <= dist) {
                dist = newDist;
                ans = Direction.SOUTHWEST;
            }
        }
        if (rc.canMove(Direction.SOUTH)) {
            loc = myLoc.add(Direction.SOUTH);
            loc = loc.add(rc.senseMapInfo(loc).getCurrentDirection());
            if (rc.canSenseLocation(loc)){
                loc = loc.add(rc.senseMapInfo(loc).getCurrentDirection());
            }
            newDist = bfsList[bfsIndex].getDistance(loc);
            //Constants.indicatorString += "S" + newDist + " ";
            if (newDist != 0 && newDist <= dist) {
                dist = newDist;
                ans = Direction.SOUTH;
            }
        }
        if (rc.canMove(Direction.SOUTHEAST)) {
            loc = myLoc.add(Direction.SOUTHEAST);
            loc = loc.add(rc.senseMapInfo(loc).getCurrentDirection());
            if (rc.canSenseLocation(loc)){
                loc = loc.add(rc.senseMapInfo(loc).getCurrentDirection());
            }
            newDist = bfsList[bfsIndex].getDistance(loc);
            //Constants.indicatorString += "SE" + newDist + " ";
            if (newDist != 0 && newDist <= dist) {
                dist = newDist;
                ans = Direction.SOUTHEAST;
            }
        }
        return ans;
    }



}

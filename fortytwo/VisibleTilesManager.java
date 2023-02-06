package fortytwo;

import battlecode.common.Clock;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;

class VisibleTilesManager {


    static final int EXPLORE_LIMIT = 1500;

    RobotController rc;

    int[][] exploredTimes;
    int index = 0;

    VisibleTilesManager(){
        rc = Robot.rc;
        exploredTimes = new int[rc.getMapWidth()][];
    }

    void fill(){
        while (index < exploredTimes.length){
            if (Clock.getBytecodesLeft() < EXPLORE_LIMIT) return;
            exploredTimes[index++] = new int[rc.getMapHeight()];
        }
    }

    void run() throws GameActionException {
        fill();
        if (isReady()){
            update();
        }
    }

    void checkCurrentRandom(){
        if (currentRandom == null) return;
        if (rc.canSenseLocation(currentRandom)) currentRandom = null;
    }

    boolean isReady(){
        return index >= exploredTimes.length;
    }

    void reset(){
        index = 0;
    }

    void update() throws GameActionException {
        if (!isReady()) return;
        if (rc.getAnchor() == null) return;
        //checkCurrentRandom();
        //if (currentRandom != null) return;
        int r = rc.getRoundNum();
        MapLocation[] tiles = rc.getAllLocationsWithinRadiusSquared(rc.getLocation(), rc.getType().visionRadiusSquared);
        for (MapLocation m : tiles){
            if (Clock.getBytecodesLeft() < EXPLORE_LIMIT) return;
            exploredTimes[m.x][m.y] = r;
        }
    }

    MapLocation currentRandom;

    MapLocation getBestRandom(int tries){
        if (!isReady()) return null;
        int maxX = rc.getMapWidth();
        int maxY = rc.getMapHeight();
        while (tries-- > 0){
            MapLocation newLoc = new MapLocation((int)(Math.random()*maxX), (int)(Math.random()*maxY));
            if (currentRandom == null || exploredTimes[newLoc.x][newLoc.y] < exploredTimes[currentRandom.x][currentRandom.y]){
                currentRandom = newLoc;
            }
        }
        if (currentRandom != null) rc.setIndicatorDot(currentRandom, 0, 0, 0);
        else {
            System.out.println("NULL RANDOM");
        }
        return currentRandom;
    }

}

package seventh;

import battlecode.common.*;

class VisibleTilesManager {


    static final int EXPLORE_LIMIT = 500;

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

    boolean isReady(){
        return index >= exploredTimes.length;
    }

    void update() throws GameActionException {
        if (!isReady()) return;
        if (rc.getAnchor() == null) return;
        int r = rc.getRoundNum();
        MapInfo[] tiles = rc.senseNearbyMapInfos();
        for (MapInfo m : tiles){
            if (Clock.getBytecodesLeft() < EXPLORE_LIMIT) return;
            exploredTimes[m.getMapLocation().x][m.getMapLocation().y] = r;
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
        return currentRandom;
    }

}

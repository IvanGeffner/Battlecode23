package fortytwo;

import battlecode.common.MapLocation;
import battlecode.common.RobotController;

import java.util.Random;

public class Explore {

    RobotController rc;
    MapLocation exploreLoc = null;
    //MapLocation[] checkLocs = new MapLocation[5];
    MapData mapdata;

    final Random rng;

    Explore(){
        this.rc = Robot.rc;
        mapdata = new MapData();
        int id = rc.getID();
        rng = new Random(id);
    }


    void getEmergencyTarget(int tries) {
        MapLocation myLoc = rc.getLocation();
        int maxX = Robot.W;
        int maxY = Robot.H;
        while (tries-- > 0){
            if (exploreLoc != null) return;
            MapLocation newLoc = new MapLocation((int)(rng.nextFloat()*maxX), (int)(rng.nextFloat()*maxY));
            //if (checkDanger && Robot.comm.isEnemyTerritoryRadial(newLoc)) continue;
            if (!rc.canSenseLocation(newLoc)){
                if (!mapdata.explored(newLoc)) {
                    exploreLoc = newLoc;
                }
            }
        }
    }

    static final int CLOSE_DIST = 200;

    void getEmergencyTargetClose(int tries){
        int maxX = 12;
        int maxY = 12;
        MapLocation baseLoc = Carrier.adamantiumMine;
        if (baseLoc == null) baseLoc = Robot.hComm.myBaseLoc;
        if (baseLoc == null) baseLoc = rc.getLocation();
        while (tries-- > 0){
            if (exploreLoc != null) return;
            int newX = baseLoc.x + (int)(2.0f*rng.nextFloat()*maxX - maxX);
            int newY = baseLoc.y + (int)(2.0f*rng.nextFloat()*maxY - maxY);
            MapLocation newLoc = new MapLocation(newX, newY);
            if (!rc.onTheMap(newLoc)) continue;
            if (!rc.canSenseLocation(newLoc)){
                if (!mapdata.explored(newLoc)) {
                    exploreLoc = newLoc;
                }
            }
        }
    }
    MapLocation getExploreTarget() {
        if (exploreLoc != null && rc.canSenseLocation(exploreLoc)) exploreLoc = null;
        checkExploreLoc();
        if (exploreLoc == null){
            if (rc.getRoundNum() < 100) getEmergencyTargetClose(15);
            else getEmergencyTarget(15);
        }
        return exploreLoc;
    }

    void checkExploreLoc(){
        if (mapdata.explored(exploreLoc)) {
            exploreLoc = null;
        }
    }

    MapLocation exploreTarget2 = null;

    void checkExploreTarget2(){
        if (exploreTarget2 == null || rc.canSenseLocation(exploreTarget2)) exploreTarget2 = null;
    }

    MapLocation getExploreTarget2(int tries){
        checkExploreTarget2();
        if (exploreTarget2 != null) return exploreTarget2;
        int maxX = rc.getMapWidth();
        int maxY = rc.getMapHeight();
        while (tries-- > 0){
            MapLocation newLoc = new MapLocation((int)(rng.nextFloat()*maxX), (int)(rng.nextFloat()*maxY));
            if (!rc.canSenseLocation(newLoc)){
                exploreTarget2 = newLoc;
                return exploreTarget2;
            }
        }
        return exploreTarget2;
    }

}
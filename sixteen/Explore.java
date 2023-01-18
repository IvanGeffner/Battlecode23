package sixteen;

import battlecode.common.MapLocation;
import battlecode.common.RobotController;

import java.util.Random;

public class Explore {

    RobotController rc;
    int myVisionRange;
    MapLocation exploreLoc = null;
    MapLocation[] checkLocs = new MapLocation[5];
    boolean checker = false;
    MapData mapdata;

    final Random rng;

    Explore(){
        this.rc = Robot.rc;
        mapdata = new MapData();
        myVisionRange = rc.getType().visionRadiusSquared;
        int id = rc.getID();
        generateLocs();
        exploreLoc = checkLocs[id%checkLocs.length];
        if (id%2 == 0) checker = true;
        rng = new Random(id);
    }

    void generateLocs(){
        int w = rc.getMapWidth();
        int h = rc.getMapHeight();
        checkLocs[0] = new MapLocation(w/2,h/2);
        checkLocs[1] = new MapLocation(0,0);
        checkLocs[2] = new MapLocation(w-1,0);
        checkLocs[3] = new MapLocation(0,h-1);
        checkLocs[4] = new MapLocation(w-1,h-1);
    }

    /*void setChecker(int init){
        exploreLoc = checkLocs[init%checkLocs.length];
        checker = true;
    }*/


    void getEmergencyTarget(int tries) {
        MapLocation myLoc = rc.getLocation();
        int maxX = rc.getMapWidth();
        int maxY = rc.getMapHeight();
        while (tries-- > 0){
            if (exploreLoc != null) return;
            MapLocation newLoc = new MapLocation((int)(rng.nextFloat()*maxX), (int)(rng.nextFloat()*maxY));
            //if (checkDanger && Robot.comm.isEnemyTerritoryRadial(newLoc)) continue;
            if (myLoc.distanceSquaredTo(newLoc) > myVisionRange){
                if (!mapdata.explored(newLoc)) {
                    exploreLoc = newLoc;
                }
            }
        }
    }

    void getCheckerTarget(int tries){
        MapLocation myLoc = rc.getLocation();
        while (tries-- > 0){
            int checkerIndex = (int)(Math.random()* checkLocs.length);
            MapLocation newLoc = checkLocs[checkerIndex];
            if (myLoc.distanceSquaredTo(newLoc) > myVisionRange){
                exploreLoc = newLoc;
            }
        }
    }

    MapLocation getExploreTarget() {
        if (exploreLoc != null && rc.getLocation().distanceSquaredTo(exploreLoc) <= myVisionRange) exploreLoc = null;
        checkExploreLoc();
        if (exploreLoc == null){
            //if (checker) getCheckerTarget(15);
            //else getEmergencyTarget(15);
            //rc.setIndicatorString("EXPLORELOC NULL");
            getEmergencyTarget(15);
        } else{
            //rc.setIndicatorDot(exploreLoc, 255, 0, 0);
        }
        return exploreLoc;
    }

    void checkExploreLoc(){
        if (mapdata.explored(exploreLoc)) {
            exploreLoc = null;
        }
    }

}
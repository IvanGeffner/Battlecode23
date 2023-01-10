package first;

import battlecode.common.*;

public class Headquarters extends Robot {

    Spawner spawner;



    Headquarters(RobotController rc) throws GameActionException {
        super(rc);
        spawner = new Spawner();
    }

    void play() throws GameActionException {
        hComm.sendBuffer();
        MapLocation carrierTarget = getCarrierTarget();
        if (carrierTarget != null) spawnToTarget(RobotType.CARRIER, carrierTarget);
    }

    MapLocation getCarrierTarget(){
        WellInfo[] wells = rc.senseNearbyWells();
        MapLocation closestWell = null;
        int minDist = 0;
        MapLocation myLoc = rc.getLocation();
        for (WellInfo w : wells){
            int d = w.getMapLocation().distanceSquaredTo(myLoc);
            if (closestWell == null || minDist > d){
                minDist = d;
                closestWell = w.getMapLocation();
            }
        }
        return closestWell;
    }

    void spawnToTarget(RobotType t, MapLocation target) throws GameActionException {
        if (target == null) target = rc.getLocation();
        int id = spawner.constructRobotGreedy(t, target);
        if (id < 0) return;
        hComm.broadcastTarget(id, target);
    }

}

package first;

import battlecode.common.*;

public class HeadquarterComm {

    static int hqIndex = 1; //hq index
    RobotController rc;

    static MapLocation myBaseLoc = null;

    static MapLocation targetOnSpawn = null;

    int bufferCode = -1;

    HeadquarterComm() throws GameActionException {
        this.rc = Robot.rc;
        if (rc.getType() == RobotType.HEADQUARTERS) getMyIndex();
        else getTargetOnSpawn();
    }

    void getMyIndex() throws GameActionException {
        myBaseLoc = rc.getLocation();
        while(occupied()){
            ++hqIndex;
        }
        setIndex();
    }

    boolean occupied() throws GameActionException {
        int channel = GameConstants.SHARED_ARRAY_LENGTH - 2* hqIndex + 1;
        return rc.readSharedArray(channel) != 0;
    }

    void setIndex() throws GameActionException {
        MapLocation myLoc = rc.getLocation();
        int code = (myLoc.x << 10) | (myLoc.y << 4) | hqIndex;
        rc.writeSharedArray(GameConstants.SHARED_ARRAY_LENGTH - 2 * hqIndex + 1, code);
    }

    void broadcastTarget (int id, MapLocation target) throws GameActionException {
        bufferCode = (target.x << 10) | (target.y << 4) | (id & 0xF);
    }

    void sendBuffer() throws GameActionException {
        if (bufferCode < 0) return;
        rc.writeSharedArray(GameConstants.SHARED_ARRAY_LENGTH - 2 * hqIndex, bufferCode);
        bufferCode = -1;
    }

    void getTargetOnSpawn() throws GameActionException {
        for (; hqIndex <= 4; ++hqIndex){

            //target info
            int code = rc.readSharedArray(GameConstants.SHARED_ARRAY_LENGTH - 2 * hqIndex);
            int x = (code >>> 10) & 0x3F;
            int y = (code >>> 4) & 0x3F;
            int id = (code & 0xF);

            //base info
            int code2 = rc.readSharedArray(GameConstants.SHARED_ARRAY_LENGTH - 2 * hqIndex + 1);
            int x2 = (code2 >>> 10) & 0x3F;
            int y2 = (code2 >>> 4) & 0x3F;

            if (code2 == 0) return;

            //check id
            if ((rc.getID() & 0xF) != id) continue;
            MapLocation loc = new MapLocation(x2,y2);
            if (loc.distanceSquaredTo(rc.getLocation()) > RobotType.HEADQUARTERS.actionRadiusSquared) continue;

            myBaseLoc = new MapLocation(x2, y2);

            if (x == x2 && y == y2) return; //no target
            targetOnSpawn =  new MapLocation(x,y);
            return;
        }
    }

    void findBase() throws GameActionException {
        if (myBaseLoc != null) return;
        RobotInfo[] robots = rc.senseNearbyRobots();
        for (RobotInfo r : robots){
            if (r.getTeam() != rc.getTeam()) continue;
            if (r.getType() == RobotType.HEADQUARTERS){
                myBaseLoc = r.getLocation();
                return;
            }
        }
    }




}

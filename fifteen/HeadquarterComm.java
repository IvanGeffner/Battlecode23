package fifteen;

import battlecode.common.*;

public class HeadquarterComm {

    static int hqIndex = 1; //hq index
    static RobotController rc;

    static MapLocation myBaseLoc = null;

    static MapLocation targetOnSpawn = null;

    int bufferCode = 0xFFFF;

    static int numBases = 0;

    HeadquarterComm() throws GameActionException {
        this.rc = Robot.rc;
        if (rc.getType() == RobotType.HEADQUARTERS) getMyIndex();
        else getTargetOnSpawn();
        if (myBaseLoc == null){
            searchBase();
        }
        computeNumBases();
    }

    void computeNumBases() throws GameActionException{
        numBases = 0;
        for(;numBases < GameConstants.MAX_STARTING_HEADQUARTERS; ++numBases){
            int channel = GameConstants.SHARED_ARRAY_LENGTH - 2*numBases - 1;
            if(rc.readSharedArray(channel) == 0) break;
        }
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
        rc.writeSharedArray(GameConstants.SHARED_ARRAY_LENGTH - 2 * hqIndex, bufferCode);
        bufferCode = 0xFFFF;
    }

    void getTargetOnSpawn() throws GameActionException {
        for (; hqIndex <= 4; ++hqIndex){

            //target info
            int code = rc.readSharedArray(GameConstants.SHARED_ARRAY_LENGTH - 2 * hqIndex);
            if (code == 0xFFFF) continue;
            int x = (code >>> 10) & 0x3F;
            int y = (code >>> 4) & 0x3F;
            int id = (code & 0xF);

            //base info
            int code2 = rc.readSharedArray(GameConstants.SHARED_ARRAY_LENGTH - 2 * hqIndex + 1);
            if (code2 == 0) return;

            int x2 = (code2 >>> 10) & 0x3F;
            int y2 = (code2 >>> 4) & 0x3F;

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

    void searchBase() throws GameActionException {
        if (myBaseLoc != null){
            searchIndex();
            return;
        }
        RobotInfo[] robots = rc.senseNearbyRobots();
        for (RobotInfo r : robots){
            if (r.getTeam() != rc.getTeam()) continue;
            if (r.getType() == RobotType.HEADQUARTERS){
                myBaseLoc = r.getLocation();
                searchIndex();
                return;
            }
        }
    }

    void searchIndex() throws GameActionException {
        if (myBaseLoc == null) return;
        for (hqIndex = 1; hqIndex <= 4; ++hqIndex){
            MapLocation loc = getHQLocation(hqIndex);
            if (loc == null) break;
            if (myBaseLoc.distanceSquaredTo(loc) == 0) return;
        }
        hqIndex = 1;
    }

    static boolean isMyHQ(MapLocation loc) throws GameActionException {
        for (int i = 1; i <= 4; ++i){

            //base info
            int code2 = rc.readSharedArray(GameConstants.SHARED_ARRAY_LENGTH - 2 * i + 1);
            if (code2 == 0) return false;

            int x2 = (code2 >>> 10) & 0x3F;
            int y2 = (code2 >>> 4) & 0x3F;

            if (loc.x == x2 && loc.y == y2) return true; //no target
        }
        return false;
    }

    //TODO: maybe manhattan?
    MapLocation getClosestHQ(MapLocation loc) throws GameActionException {
        MapLocation ans = null;

        for (int x = 1; x <= 4; ++x){
            int code2 = rc.readSharedArray(GameConstants.SHARED_ARRAY_LENGTH - 2 * x + 1);
            if (code2 == 0) break;

            int x2 = (code2 >>> 10) & 0x3F;
            int y2 = (code2 >>> 4) & 0x3F;

            MapLocation hqLoc = new MapLocation(x2, y2);
            if (ans == null || hqLoc.distanceSquaredTo(loc) < ans.distanceSquaredTo(loc)) ans = hqLoc;
        }
        return ans;
    }

    MapLocation getHQLocation(int index) throws GameActionException {
        int x = (index)%4;
        if (x == 0) x = 4;
        int code2 = rc.readSharedArray(GameConstants.SHARED_ARRAY_LENGTH - 2 * x + 1);
        if (code2 == 0) return null;

        int x2 = (code2 >>> 10) & 0x3F;
        int y2 = (code2 >>> 4) & 0x3F;

        return new MapLocation(x2, y2);
    }




}

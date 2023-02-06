package fortytwo;

import battlecode.common.*;

public class HeadquarterComm {

    static int hqIndex = 1; //hq index
    static RobotController rc;

    static MapLocation myBaseLoc = null;

    static MapLocation targetOnSpawn = null;

    int bufferCode = 0xFFFF;

    static int numBases = 0;
    static int targetCode = 1;

    HeadquarterComm() throws GameActionException {
        this.rc = Robot.rc;
        if (rc.getType() == RobotType.HEADQUARTERS) getMyIndex();
        /*else getTargetOnSpawn();
        if (myBaseLoc == null){
            searchBase();
        }*/
        else{
            searchBase();
            getTargetOnSpawn();
        }
        //if (hqIndex > 4) System.out.println("MEGAFAIL");
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
        Robot.write(GameConstants.SHARED_ARRAY_LENGTH - 2 * hqIndex + 1, code);
    }

    void broadcastTarget (int code, MapLocation target) throws GameActionException {
        //System.out.println("Broadcasting: " + target.toString() + " " + hqIndex);
        bufferCode = (target.x << 10) | (target.y << 4) | (code & 0xF);
        //if (!rc.onTheMap(target)) System.out.println("EPIC FAIL!!!!!");
    }

    void sendBuffer() throws GameActionException {
        Robot.write(GameConstants.SHARED_ARRAY_LENGTH - 2 * hqIndex, bufferCode);
        bufferCode = 0xFFFF;
    }

    void getTargetOnSpawn() throws GameActionException {
        if (myBaseLoc == null) return;
        //rc.setIndicatorDot(myBaseLoc, 255, 0, 0);
        //System.out.println(" ->" + hqIndex + " ");
        //for (; hqIndex <= 4; ++hqIndex){
        int code = rc.readSharedArray(GameConstants.SHARED_ARRAY_LENGTH - 2 * hqIndex);
        if (code == 0xFFFF) return;
        int x = (code >>> 10) & 0x3F;
        int y = (code >>> 4) & 0x3F;

        targetOnSpawn =  new MapLocation(x,y);
        //rc.setIndicatorDot(targetOnSpawn, 0, 0, 0);
        //System.out.println(targetOnSpawn.toString() + " ");
        if (myBaseLoc.distanceSquaredTo(targetOnSpawn) == 0) targetOnSpawn = null;
        targetCode = (code&0xF);

        //rc.setIndicatorDot(targetOnSpawn, 200, 200, 200);
        //}
    }

    MapLocation[] getHQArray() throws GameActionException {
        MapLocation[] ans = new MapLocation[4];
        for(int i = 0; i < 4; ++i){
            int code2 = rc.readSharedArray(GameConstants.SHARED_ARRAY_LENGTH - 2 * i - 1);
            if (code2 == 0) return ans;

            int x2 = (code2 >>> 10) & 0x3F;
            int y2 = (code2 >>> 4) & 0x3F;

            ans[i] = new MapLocation(x2,y2);
        }
        return ans;
    }

    void searchBase() throws GameActionException {
        if (myBaseLoc != null) return;
        for (; hqIndex <= 4; ++hqIndex){
            //base info
            int code2 = rc.readSharedArray(GameConstants.SHARED_ARRAY_LENGTH - 2 * hqIndex + 1);
            if (code2 == 0) return;

            int x2 = (code2 >>> 10) & 0x3F;
            int y2 = (code2 >>> 4) & 0x3F;

            MapLocation loc = new MapLocation(x2,y2);

            //rc.setIndicatorDot(loc, 150, 150, 150);

            if (loc.distanceSquaredTo(rc.getLocation()) > RobotType.HEADQUARTERS.actionRadiusSquared) continue;

            myBaseLoc = new MapLocation(x2, y2);
            //searchIndex();
            return;
        }
        if (myBaseLoc == null){
            searchBase2();
        }
    }

    void searchBase2() throws GameActionException {
        if (myBaseLoc != null){
            searchIndex();
            return;
        }
        RobotInfo[] robots = rc.senseNearbyRobots(RobotType.HEADQUARTERS.actionRadiusSquared, rc.getTeam());
        for (RobotInfo r : robots){
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
            if (ans == null || hqLoc.distanceSquaredTo(loc) < ans.distanceSquaredTo(loc)){
                ans = hqLoc;
                //hqIndex = x;
            }
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

    void computeSymmetries() throws GameActionException {
        for (int i = 0; i < 4; ++i){
            MapLocation loc = getHQLocation(i+1);
            if (loc == null) return;
            MapLocation locH = new MapLocation(Robot.W - loc.x - 1, loc.y);
            if (rc.canSenseLocation(locH)){
                RobotInfo r = rc.senseRobotAtLocation(locH);
                if (r == null || r.getType() != RobotType.HEADQUARTERS || r.getTeam() != rc.getTeam().opponent()){
                    Robot.otherComm.eliminateHorizontal();
                    System.out.println("Not symmetric!");
                }
            }
            MapLocation locV = new MapLocation(loc.x, Robot.H - loc.y - 1);
            if (rc.canSenseLocation(locV)){
                RobotInfo r = rc.senseRobotAtLocation(locV);
                if (r == null || r.getType() != RobotType.HEADQUARTERS || r.getTeam() != rc.getTeam().opponent()){
                    Robot.otherComm.eliminateVertical();
                    System.out.println("Not Vertical!");
                }
            }
            MapLocation locR = new MapLocation(Robot.W - loc.x - 1, Robot.H - loc.y - 1);
            if (rc.canSenseLocation(locR)){
                RobotInfo r = rc.senseRobotAtLocation(locR);
                if (r == null || r.getType() != RobotType.HEADQUARTERS || r.getTeam() != rc.getTeam().opponent()){
                    Robot.otherComm.eliminateRotational();
                    System.out.println("Not rotational!");
                }
            }
        }
    }

    void checkMapType() throws GameActionException {
        int minDist = Constants.INF;
        MapLocation myLoc = rc.getLocation();
        for (int i = 0; i < 4; ++i){
            MapLocation loc = getHQLocation(i+1);
            if (loc == null) return;
            if (Robot.otherComm.horizontal) {
                MapLocation locH = new MapLocation(Robot.W - loc.x - 1, loc.y);
                int d = Math.max(Math.abs(myLoc.x - locH.x), Math.abs(myLoc.y - locH.y));
                if (d < minDist) minDist = d;
            }
            if (Robot.otherComm.horizontal) {
                MapLocation locV = new MapLocation(loc.x, Robot.H - loc.y - 1);
                int d = Math.max(Math.abs(myLoc.x - locV.x), Math.abs(myLoc.y - locV.y));
                if (d < minDist) minDist = d;
            }
            if (Robot.otherComm.horizontal) {
                MapLocation locR = new MapLocation(Robot.W - loc.x - 1, Robot.H - loc.y - 1);
                int d = Math.max(Math.abs(myLoc.x - locR.x), Math.abs(myLoc.y - locR.y));
                if (d < minDist) minDist = d;
            }
        }
        if (minDist < 15) Robot.MAP_TYPE = Robot.SMALL;
        else if (minDist < 25) Robot.MAP_TYPE = Robot.MEDIUM;
        else Robot.MAP_TYPE = Robot.LARGE;
    }
}

package fortytwo;

import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;

public class HQTracker {

    RobotController rc;
    HeadquarterComm hComm;

    OtherComm otherComm;

    Explore explore;

    HQTracker() throws GameActionException {
        rc = Robot.rc;
        hComm = Robot.hComm;
        otherComm = Robot.otherComm;
        explore = Robot.explore;
        generateHQArray();
    }

    MapLocation HQTarget = null;
    //PossibleHQLocation[] hqArray;
    int[] hqArray; //12 bits dist | 2 bits confirmation | 2 bits sym | 6 bits x | 6 bits y

    int HQIndex = 0;
    int lastRoundAway = -100;

    int currentIndex;

    MapLocation getLocation(){
        int code = hqArray[currentIndex];
        return new MapLocation((code >>> 6) & 0x3F, code&0x3F);
    }

    int getSym(int code){
        return ((code >>> 12)&3);
    }

    int getConfirmation() throws GameActionException {
        int code = hqArray[currentIndex];
        int confirmation = ((code >>> 14)&3);
        if (confirmation != 0) return confirmation;
        if (otherComm.sym != null){
            int sym = getSym(code);
            confirmation = otherComm.sym == sym ?  1 : 2;
            hqArray[currentIndex] |= ((confirmation) << 14);
            return confirmation;
        } else {
            int sym = getSym(code);
            switch(sym){
                case HOR:
                    if (!otherComm.horizontal){
                        confirmation = 2;
                        hqArray[currentIndex] |= ((confirmation) << 14);
                        return confirmation;
                    }
                    break;
                case VER:
                    if (!otherComm.vertical) {
                        confirmation = 2;
                        hqArray[currentIndex] |= ((confirmation) << 14);
                        return confirmation;
                    }
                    break;
                case ROT:
                    if (!otherComm.rotational){
                        confirmation = 2;
                        hqArray[currentIndex] |= ((confirmation) << 14);
                        return confirmation;
                    }
                    break;
            }
        }
        confirmation = explore.mapdata.isEnemyHQ(getLocation());
        if (confirmation == 2) {
            int sym = getSym(code);
            switch (sym) {
                case HOR:
                    Robot.otherComm.eliminateHorizontal();
                    break;
                case VER:
                    Robot.otherComm.eliminateVertical();
                    break;
                case ROT:
                    Robot.otherComm.eliminateRotational();
                    break;
            }
        }
        hqArray[currentIndex] |= ((confirmation) << 14);
        return confirmation;
    }

    //int getDistance(int code){
        //return ((code >>> 12) & 0xFFF);
   //}

    int getCode(MapLocation myHQLoc, int sym){
        MapLocation loc;
        switch(sym){
            case HOR:
                loc = new MapLocation(rc.getMapWidth() - myHQLoc.x - 1, myHQLoc.y);
                break;
            case VER:
                loc = new MapLocation(myHQLoc.x, rc.getMapHeight() - myHQLoc.y - 1);
                break;
            default:
                loc = new MapLocation(rc.getMapWidth() - myHQLoc.x - 1, rc.getMapHeight() - myHQLoc.y - 1);
                break;
        }
        int d;
        if (HeadquarterComm.myBaseLoc != null) d = HeadquarterComm.myBaseLoc.distanceSquaredTo(loc);
        else d = myHQLoc.distanceSquaredTo(loc);
        int code = (d << 16) | (sym << 12) | (loc.x << 6) | loc.y;
        return code;
    }

    void set(){
        this.HQIndex = currentIndex;
        HQTarget = getLocation();
    }

    void update(){
        if (HQTarget == null) return;
        int d = HQTarget.distanceSquaredTo(rc.getLocation());
        if (d > 50) lastRoundAway = rc.getRoundNum();
        if (lastRoundAway + 20 < rc.getRoundNum()) advance();
    }

    void advance(){
        HQIndex = (HQIndex + 1)%hqArray.length;
        HQTarget = null;
        lastRoundAway = -100;
    }

    void generateHQArray() throws GameActionException {
        int hqArrayIndex = 0;
        hqArray = new int[12];
        //boolean horFirst = true;
        //if (Math.abs(rc.getMapWidth() - 2*rc.getLocation().x) > Math.abs(rc.getMapHeight() - 2*rc.getLocation().y)) horFirst = false;
        for (int i = 0; i < 4; ++i){
            MapLocation loc = hComm.getHQLocation((hComm.hqIndex + i)%4);
            if (loc == null) continue;
            hqArray[hqArrayIndex++] = getCode(loc, ROT);
            hqArray[hqArrayIndex++] = getCode(loc, HOR);
            hqArray[hqArrayIndex++] = getCode(loc, VER);
        }
        //IfSorting.sort(hqArray);
    }


    MapLocation getBestEnemyBase() throws GameActionException {
        //if (rc.getRoundNum() < 150) return getClosestBase();
        if (HQTarget != null) return HQTarget;
        for (int x = 0; x < hqArray.length; ++x){
            currentIndex = (HQIndex + x)%hqArray.length;
            if (hqArray[currentIndex] == 0) continue;
            int conf = getConfirmation();
            switch(conf){
                case 1:
                    set();
                case 0: return getLocation();
                default: continue;
            }
        }
        return null;
    }

    static final int HOR = 1;
    static final int VER = 2;
    static final int ROT = 3;

    class PossibleHQLocation{

        final MapLocation loc;
        int confirmation = 0;

        int sym;
        PossibleHQLocation (MapLocation myHQLoc, int sym){
            this.sym = sym;
            switch(sym){
                case HOR:
                    loc = new MapLocation(rc.getMapWidth() - myHQLoc.x - 1, myHQLoc.y);
                    break;
                case VER:
                    loc = new MapLocation(myHQLoc.x, rc.getMapHeight() - myHQLoc.y - 1);
                    break;
                default:
                    loc = new MapLocation(rc.getMapWidth() - myHQLoc.x - 1, rc.getMapHeight() - myHQLoc.y - 1);
                    break;
            }
        }

        void checkConfirmation() throws GameActionException {
            if (confirmation != 0) return;
            if (otherComm.sym != null){
                confirmation = otherComm.sym == sym ?  1 : -1;
                return;
            } else {
                switch(sym){
                    case HOR:
                        if (!otherComm.horizontal){
                            confirmation = -1;
                            return;
                        }
                        break;
                    case VER:
                        if (!otherComm.vertical) {
                            confirmation = -1;
                            return;
                        }
                        break;
                    case ROT:
                        if (!otherComm.rotational){
                            confirmation = -1;
                            return;
                        }
                        break;
                }
            }
            confirmation = explore.mapdata.isEnemyHQ(loc);
        }
    }

}

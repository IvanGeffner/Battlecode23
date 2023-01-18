package twelve;

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

    PossibleHQLocation[] hqArray;

    MapLocation HQTarget = null;

    void generateHQArray() throws GameActionException {
        int hqArrayIndex = 0;
        hqArray = new PossibleHQLocation[12];
        for (int i = 0; i < 4; ++i){
            MapLocation loc = hComm.getHQLocation((hComm.hqIndex + i)%4);
            if (loc == null) continue;
            hqArray[hqArrayIndex++] = new PossibleHQLocation(loc, ROT);
            hqArray[hqArrayIndex++] = new PossibleHQLocation(loc, HOR);
            hqArray[hqArrayIndex++] = new PossibleHQLocation(loc, VER);
        }
    }


    MapLocation getBestEnemyBase() throws GameActionException {
        if (HQTarget != null) return HQTarget;
        for (int i = 0; i < hqArray.length; ++i){
            if (hqArray[i] == null) return null;
            hqArray[i].checkConfirmation();
            switch(hqArray[i].confirmation){
                case 1: HQTarget = hqArray[i].loc;
                case 0: return hqArray[i].loc;
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

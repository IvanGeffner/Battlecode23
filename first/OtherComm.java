package first;

import battlecode.common.GameActionException;
import battlecode.common.RobotController;

public class OtherComm {

    static final int SOLDIER_CHANNEL = 51;
    static final int SYM_CHANNEL = 50;

    static final int HOR_SYM = 1;
    static final int VER_SYM = 2;
    static final int ROT_SYM = 3;
    static final int HSYMBIT = (1 << 2);
    static final int VSYMBIT = (1 << 3);
    static final int RSYMBIT = (1 << 4);

    RobotController rc;

    OtherComm(){
        this.rc = Robot.rc;
    }

    void reportSoldier() throws GameActionException {
        int x = getSoldiers();
        rc.writeSharedArray(SOLDIER_CHANNEL, x+1);
    }

    int getSoldiers() throws GameActionException {
        return rc.readSharedArray(SOLDIER_CHANNEL);
    }


}

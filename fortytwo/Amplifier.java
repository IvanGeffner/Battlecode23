package fortytwo;

import battlecode.common.GameActionException;
import battlecode.common.RobotController;


public class Amplifier extends Attacker {

    Amplifier(RobotController rc)  throws GameActionException {
        super(rc);
    }

    void play() throws GameActionException {
        updateBase();
        checkMana();
        if (!microAttacker.doMicro()) moveToTargetAttacker();
    }

}
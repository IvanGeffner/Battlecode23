package first;

import battlecode.common.GameActionException;
import battlecode.common.RobotController;

public class Carrier extends Robot {

    Carrier(RobotController rc) throws GameActionException {
        super(rc);
    }

    void play() throws GameActionException{
        pathfinder.moveTo(hComm.targetOnSpawn);
    }

}
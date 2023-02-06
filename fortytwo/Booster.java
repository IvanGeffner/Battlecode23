package fortytwo;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.RobotController;


public class Booster extends Attacker {

    Booster(RobotController rc)  throws GameActionException {
        super(rc);
    }

    void play() throws GameActionException {
        updateBase();
        checkMana();
        if (!microAttacker.doMicro()) moveToTarget();
        attack();
    }

    void attack() throws GameActionException{
        if(rc.canBoost()) rc.boost();
    }

    void moveToTarget() throws GameActionException {
        if (roundSeen + TURNS_STATIC >= rc.getRoundNum()){
            Direction dir = getDirClosestAllies();
            if (dir != Direction.CENTER && dir != null && rc.canMove(dir) && !adjacentTiles.dirDanger[dir.ordinal()].closeToEnemyHQ) Robot.move(dir);
            return;
        }
        moveToTargetAttacker();
    }

}
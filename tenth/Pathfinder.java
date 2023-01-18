package tenth;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;

public class Pathfinder {

    BugPath bugPath;
    RobotController rc;


    Pathfinder(){
        rc = Robot.rc;
        bugPath = new BugPath();
    }

    void moveTo(int bfsIndex) throws GameActionException {
        int oldCooldown = rc.getMovementCooldownTurns();
        MapLocation target = Robot.bfsManager.bfsList[bfsIndex].start;
        if (rc.getLocation().distanceSquaredTo(target) == 0) return;
        rc.setIndicatorLine(rc.getLocation(), Robot.bfsManager.bfsList[bfsIndex].start, 255, 0, 255);
        while (rc.isMovementReady()){
            Direction dir = Robot.bfsManager.getBestDirection(bfsIndex);
            if (dir == null || dir == Direction.CENTER){
                moveTo(Robot.bfsManager.bfsList[bfsIndex].start);
                return;
            }
            if (rc.canMove(dir)){
                rc.move(dir);
                bugPath.resetPathfinding();
            }
            int newCooldown = rc.getMovementCooldownTurns();
            if (oldCooldown == newCooldown) return;
            oldCooldown = newCooldown;
        }
    }

    void moveTo(MapLocation target){
        int oldCooldown = rc.getMovementCooldownTurns();
        while (rc.isMovementReady()){
            tryMove(target);
            int newCooldown = rc.getMovementCooldownTurns();
            if (oldCooldown == newCooldown) return;
            oldCooldown = newCooldown;
        }
    }

    void tryMove(MapLocation target){
        bugPath.moveTo(target);
    }

}

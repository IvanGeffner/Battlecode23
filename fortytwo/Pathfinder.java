package fortytwo;

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
        MapLocation target = Robot.bfsManager.buffer[bfsIndex];
        if (rc.getLocation().distanceSquaredTo(target) == 0) return;
        Direction dir = Robot.bfsManager.getBestDirection(bfsIndex);
        if (dir == null || dir == Direction.CENTER){
            moveTo(Robot.bfsManager.buffer[bfsIndex]);
            return;
        }
        if (rc.canMove(dir)){
            Robot.move(dir);
            bugPath.resetPathfinding();
        }
    }

    void moveTo(MapLocation target){
        tryMove(target);
    }

    void tryMove(MapLocation target){
        bugPath.moveTo(target);
    }

}

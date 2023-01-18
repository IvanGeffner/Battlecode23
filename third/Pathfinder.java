package third;

import battlecode.common.MapLocation;
import battlecode.common.RobotController;

public class Pathfinder {

    BugPath bugPath;
    RobotController rc;


    Pathfinder(){
        rc = Robot.rc;
        bugPath = new BugPath();
    }

    void moveTo(MapLocation target){
        int oldCooldown = rc.getMovementCooldownTurns();
        while (rc.isMovementReady()){
            bugPath.moveTo(target);
            int newCooldown = rc.getMovementCooldownTurns();
            if (oldCooldown == newCooldown) return;
            oldCooldown = newCooldown;
        }
        //bugPath.moveTo(target);
    }

}

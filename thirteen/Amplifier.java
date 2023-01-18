package thirteen;

import battlecode.common.*;


public class Amplifier extends Robot {

    HQTracker hqTracker;

    MicroAttacker microAttacker;

    Amplifier(RobotController rc)  throws GameActionException {
        super(rc);
        microAttacker = new MicroAttacker();
        hqTracker = new HQTracker();
    }

    void play() throws GameActionException {
        if (!microAttacker.doMicro()) moveToTarget();
    }

    MapLocation getTarget() throws GameActionException {
        
        MapLocation target;

        //target = getClosestLauncher(rc.getTeam());
        //if (target != null) return target;

        target = getClosestLauncher(rc.getTeam().opponent());
        if (target != null) return target;

        target = otherComm.closestEnemyLauncher;
        if (target != null) return target;

        target = hqTracker.getBestEnemyBase();
        return target;
    }

    void moveToTarget() throws GameActionException {
        MapLocation target = getTarget();
        pathfinder.moveTo(target);
    }

    MapLocation getClosestLauncher(Team t) throws GameActionException {
        MapLocation ans = null;
        int bestDist = 0;
        MapLocation myLoc = rc.getLocation();
        RobotInfo[] enemies = rc.senseNearbyRobots(rc.getType().visionRadiusSquared, t);
        for (RobotInfo r : enemies){
            if (r.getType() != RobotType.LAUNCHER) continue;
            int d = myLoc.distanceSquaredTo(r.getLocation());
            if (ans == null || d < bestDist){
                bestDist = d;
                ans = r.getLocation();
            }
        }
        return ans;
    }

}
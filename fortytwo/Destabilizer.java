package fortytwo;

import battlecode.common.*;


public class Destabilizer extends Attacker {

    Destabilizer(RobotController rc)  throws GameActionException {
        super(rc);
    }

    void play() throws GameActionException {
        updateBase();
        checkMana();
        attack();
        if (!microAttacker.doMicro()) moveToTarget();
        attack();
    }

    void attack() throws GameActionException {
        attackLaunchers();
        //attackRandom();
    }

    void attackLaunchers() throws GameActionException {
        if (!rc.isActionReady()) return;
        RobotInfo[] enemies = Robot.enemies;
        for (RobotInfo r : enemies){
            if (r.getType() != RobotType.LAUNCHER) continue;
            MapLocation loc = r.getLocation();
            MapLocation bestLoc = null;
            int bestDist = 0;
            MapLocation newLoc = loc;
            if (rc.canDestabilize(newLoc)) {
                if (bestLoc == null || bestDist < newLoc.distanceSquaredTo(rc.getLocation())){
                    bestLoc = newLoc;
                    bestDist = newLoc.distanceSquaredTo(rc.getLocation());
                }
            }
            newLoc = loc.add(Direction.NORTH);
            if (rc.canDestabilize(newLoc)) {
                if (bestLoc == null || bestDist < newLoc.distanceSquaredTo(rc.getLocation())){
                    bestLoc = newLoc;
                    bestDist = newLoc.distanceSquaredTo(rc.getLocation());
                }
            }
            newLoc = loc.add(Direction.SOUTH);
            if (rc.canDestabilize(newLoc)) {
                if (bestLoc == null || bestDist < newLoc.distanceSquaredTo(rc.getLocation())){
                    bestLoc = newLoc;
                    bestDist = newLoc.distanceSquaredTo(rc.getLocation());
                }
            }
            newLoc = loc.add(Direction.EAST);
            if (rc.canDestabilize(newLoc)) {
                if (bestLoc == null || bestDist < newLoc.distanceSquaredTo(rc.getLocation())){
                    bestLoc = newLoc;
                    bestDist = newLoc.distanceSquaredTo(rc.getLocation());
                }
            }
            newLoc = loc.add(Direction.WEST);
            if (rc.canDestabilize(newLoc)) {
                if (bestLoc == null || bestDist < newLoc.distanceSquaredTo(rc.getLocation())){
                    bestLoc = newLoc;
                    bestDist = newLoc.distanceSquaredTo(rc.getLocation());
                }
            }
            newLoc = loc.add(Direction.NORTHEAST);
            if (rc.canDestabilize(newLoc)) {
                if (bestLoc == null || bestDist < newLoc.distanceSquaredTo(rc.getLocation())){
                    bestLoc = newLoc;
                    bestDist = newLoc.distanceSquaredTo(rc.getLocation());
                }
            }
            newLoc = loc.add(Direction.SOUTHWEST);
            if (rc.canDestabilize(newLoc)) {
                if (bestLoc == null || bestDist < newLoc.distanceSquaredTo(rc.getLocation())){
                    bestLoc = newLoc;
                    bestDist = newLoc.distanceSquaredTo(rc.getLocation());
                }
            }
            newLoc = loc.add(Direction.NORTHWEST);
            if (rc.canDestabilize(newLoc)) {
                if (bestLoc == null || bestDist < newLoc.distanceSquaredTo(rc.getLocation())){
                    bestLoc = newLoc;
                    bestDist = newLoc.distanceSquaredTo(rc.getLocation());
                }
            }
            newLoc = loc.add(Direction.SOUTHEAST);
            if (rc.canDestabilize(newLoc)) {
                if (bestLoc == null || bestDist < newLoc.distanceSquaredTo(rc.getLocation())){
                    bestLoc = newLoc;
                    bestDist = newLoc.distanceSquaredTo(rc.getLocation());
                }
            }
            if (bestLoc != null && rc.canDestabilize(bestLoc)){
                rc.destabilize(bestLoc);
                return;
            }
        }
    }

    void attackRandom() throws GameActionException {
        if (!rc.isActionReady()) return;
        for (RobotInfo r : enemies){
            //if (r.getType() != RobotType.LAUNCHER) continue;
            MapLocation loc = r.getLocation();
            MapLocation newLoc = loc;
            if (rc.canDestabilize(newLoc)) {
                rc.destabilize(newLoc);
                return;
            }
            newLoc = loc.add(Direction.NORTH);
            if (rc.canDestabilize(newLoc)) {
                rc.destabilize(newLoc);
                return;
            }
            newLoc = loc.add(Direction.SOUTH);
            if (rc.canDestabilize(newLoc)) {
                rc.destabilize(newLoc);
                return;
            }
            newLoc = loc.add(Direction.EAST);
            if (rc.canDestabilize(newLoc)) {
                rc.destabilize(newLoc);
                return;
            }
            newLoc = loc.add(Direction.WEST);
            if (rc.canDestabilize(newLoc)) {
                rc.destabilize(newLoc);
                return;
            }
            newLoc = loc.add(Direction.NORTHEAST);
            if (rc.canDestabilize(newLoc)) {
                rc.destabilize(newLoc);
                return;
            }
            newLoc = loc.add(Direction.SOUTHWEST);
            if (rc.canDestabilize(newLoc)) {
                rc.destabilize(newLoc);
                return;
            }
            newLoc = loc.add(Direction.NORTHWEST);
            if (rc.canDestabilize(newLoc)) {
                rc.destabilize(newLoc);
                return;
            }
            newLoc = loc.add(Direction.SOUTHEAST);
            if (rc.canDestabilize(newLoc)) {
                rc.destabilize(newLoc);
                return;
            }
        }
    }

    void moveToTarget() throws GameActionException {
        if (rc.getActionCooldownTurns() >= 10){
            return;
        }
        moveToTargetAttacker();
    }

}
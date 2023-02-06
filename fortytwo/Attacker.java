package fortytwo;

import battlecode.common.*;

public abstract class Attacker extends Robot {

    abstract void play() throws GameActionException;

    Attacker(RobotController rc) throws GameActionException {
        super(rc);
        microAttacker = new MicroAttacker();
        hqTracker = new HQTracker();
    }

    boolean manaChecked = false;

    HQTracker hqTracker;

    MicroAttacker microAttacker;

    MapLocation enemyBase = null;
    int baseBFSIndex = -1;

    static final int START_MOVING_TURN = 3;

    //int lastTurnEnemySeen = -1000;
    static final int TURNS_STATIC = 6;
    static final int TURNS_MEMORY_TARGET = 10;


    void updateBase() throws GameActionException {
        hqTracker.update();
        MapLocation loc = hqTracker.getBestEnemyBase();
        if (enemyBase == null || loc != null && loc.distanceSquaredTo(enemyBase) > 0){
            enemyBase = loc;
            if (baseBFSIndex >= 0) bfsManager.reset(baseBFSIndex, enemyBase);
            else baseBFSIndex = bfsManager.requestBFS(enemyBase);
        }
    }

    void checkMana() throws GameActionException {
        if (manaChecked) return;
        MapLocation target = explore.mapdata.getBestEnemyWell();
        if (target != null){
            if (rc.getLocation().distanceSquaredTo(target) <= 4) manaChecked = true;
        }
    }

    MapLocation getTarget() throws GameActionException {
        /*if (rc.getRoundNum() <= START_MOVING_TURN){
            return rc.getLocation();
        }*/

        MapLocation target;

        /*target = getClosestEnemy();
        if (target != null) return target;*/
        if (!manaChecked){
            target = explore.mapdata.getBestEnemyWell();
            if (target != null) return target;
        }

        target = closestEnemyLauncher;
        if (roundSeen + TURNS_MEMORY_TARGET >= rc.getRoundNum() && target != null) return target;

        target = otherComm.closestEnemyLauncher;
        if (target != null && target.distanceSquaredTo(rc.getLocation()) <= 100) return target;

        if (rc.getType() == RobotType.LAUNCHER){
            target = getClosestEnemyCarrier();
            if (target != null) return target;
        }

        target = getClosestEnemyIsland();
        if (target != null) return target;

        target = enemyBase;
        if (target != null) return target;

        target = explore.getExploreTarget();
        return target;
    }

    MapLocation getClosestEnemyIsland() throws GameActionException {
        int[] islands = rc.senseNearbyIslands();
        MapLocation ans = null;
        int d = rc.getType().visionRadiusSquared;
        MapLocation myLoc = rc.getLocation();
        for (int id : islands){
            if (rc.senseTeamOccupyingIsland(id) != opponent) continue;
            MapLocation[] iLocs = rc.senseNearbyIslandLocations(d, id);
            for (MapLocation m : iLocs){
                if (ans == null || m.distanceSquaredTo(myLoc) < d){
                    d = m.distanceSquaredTo(myLoc);
                    ans = m;
                }
            }
        }
        return ans;
    }

    MapLocation getClosestEnemyCarrier(){
        RobotInfo bestCarrier = null;

        for (RobotInfo r : Robot.enemies){
            if (r.getType() != RobotType.CARRIER) continue;
            if (bestCarrier == null){
                bestCarrier = r;
                continue;
            }
            if (r.getHealth() > bestCarrier.getHealth()) continue;
            if (r.getHealth() < bestCarrier.getHealth() || r.getHealth() == bestCarrier.getHealth() && r.getLocation().distanceSquaredTo(rc.getLocation()) < bestCarrier.getLocation().distanceSquaredTo(rc.getLocation())){
                bestCarrier = r;
            }
        }
        if (bestCarrier == null) return null;
        return bestCarrier.getLocation();
    }

    void moveToTargetAttacker() throws GameActionException {
        MapLocation target = getTarget();
        if (target != null){
            //if (baseBFSIndex >= 0) pathfinder.moveTo(baseBFSIndex);
            //else pathfinder.moveTo(target);
            pathfinder.moveTo(target);
            rc.setIndicatorLine(target, rc.getLocation(), 0, 0, 0);
        }
    }

    Direction getDirClosestAllies() throws GameActionException {
        RobotInfo[] allies = rc.senseNearbyRobots(rc.getType().visionRadiusSquared, rc.getTeam());
        MapLocation myLoc = rc.getLocation();
        MapLocation myLocN = rc.getLocation().add(Direction.NORTH);
        MapLocation myLocNE = rc.getLocation().add(Direction.NORTHEAST);
        MapLocation myLocE = rc.getLocation().add(Direction.EAST);
        MapLocation myLocSE = rc.getLocation().add(Direction.SOUTHEAST);
        MapLocation myLocS = rc.getLocation().add(Direction.SOUTH);
        MapLocation myLocSW = rc.getLocation().add(Direction.SOUTHWEST);
        MapLocation myLocW = rc.getLocation().add(Direction.WEST);
        MapLocation myLocNW = rc.getLocation().add(Direction.NORTHWEST);

        Direction ans = Direction.CENTER;
        int minDist = Constants.INF;

        int d;
        for (RobotInfo r : allies){
            if (Clock.getBytecodeNum() > 8700) break;
            if (r.getType() != RobotType.LAUNCHER) continue;
            d = myLoc.distanceSquaredTo(r.getLocation());
            if (d < minDist){
                minDist = d;
                ans = Direction.CENTER;
            }
            if (rc.canMove(Direction.NORTH)) {
                d = myLocN.distanceSquaredTo(r.getLocation());
                if (d < minDist) {
                    minDist = d;
                    ans = Direction.NORTH;
                }
            }
            if (rc.canMove(Direction.NORTHEAST)) {
                d = myLocNE.distanceSquaredTo(r.getLocation());
                if (d < minDist) {
                    minDist = d;
                    ans = Direction.NORTHEAST;
                }
            }
            if (rc.canMove(Direction.EAST)) {
                d = myLocE.distanceSquaredTo(r.getLocation());
                if (d < minDist) {
                    minDist = d;
                    ans = Direction.EAST;
                }
            }
            if (rc.canMove(Direction.SOUTHEAST)) {
                d = myLocSE.distanceSquaredTo(r.getLocation());
                if (d < minDist) {
                    minDist = d;
                    ans = Direction.SOUTHEAST;
                }
            }
            if (rc.canMove(Direction.SOUTH)) {
                d = myLocS.distanceSquaredTo(r.getLocation());
                if (d < minDist) {
                    minDist = d;
                    ans = Direction.SOUTH;
                }
            }
            if (rc.canMove(Direction.SOUTHWEST)) {
                d = myLocSW.distanceSquaredTo(r.getLocation());
                if (d < minDist) {
                    minDist = d;
                    ans = Direction.SOUTHWEST;
                }
            }
            if (rc.canMove(Direction.WEST)) {
                d = myLocW.distanceSquaredTo(r.getLocation());
                if (d < minDist) {
                    minDist = d;
                    ans = Direction.WEST;
                }
            }
            if (rc.canMove(Direction.NORTHWEST)) {
                d = myLocNW.distanceSquaredTo(r.getLocation());
                if (d < minDist) {
                    minDist = d;
                    ans = Direction.NORTHWEST;
                }
            }
        }
        rc.setIndicatorDot(rc.getLocation().add(ans), 0, 0, 0);
        return ans;
    }

}

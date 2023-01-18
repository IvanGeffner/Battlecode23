package thirteen;

import battlecode.common.*;

public class Launcher extends Robot {

    MicroAttacker microAttacker;
    static Team opponent;

    int lastTurnEnemySeen = -1000;
    static final int TURNS_STATIC = 4;

    HQTracker hqTracker;

    static final int START_MOVING_TURN = 8;

    Launcher(RobotController rc) throws GameActionException {
        super(rc);
        opponent = rc.getTeam().opponent();
        microAttacker = new MicroAttacker();
        hqTracker = new HQTracker();
    }

    void play() throws GameActionException {
        if (nearbyLauncher()) lastTurnEnemySeen = rc.getRoundNum();
        while(attack()){}
        while(desperateAttack()){}
        if (!microAttacker.doMicro()){
            moveToTarget();
        }
        while(attack()){}
        while(desperateAttack()){}
    }

    boolean nearbyLauncher() throws GameActionException {
        RobotInfo[] robots = rc.senseNearbyRobots(rc.getType().visionRadiusSquared, rc.getTeam().opponent());
        for (RobotInfo r : robots){
            if (r.getType() == RobotType.LAUNCHER) return true;
        }
        return false;
    }

    MapLocation getTarget() throws GameActionException {
        if (rc.getRoundNum() <= START_MOVING_TURN){
            return rc.getLocation();
        }
        if (lastTurnEnemySeen + TURNS_STATIC >= rc.getRoundNum()){
            Direction dir = getDirClosestAllies();
            if (dir != Direction.CENTER && rc.canMove(dir)) rc.move(dir);
            return rc.getLocation();
        }
        MapLocation target = getClosestEnemy();
        if (target != null) return target;

        target = otherComm.closestEnemyLauncher;
        if (target != null) return target;

        target = getClosestEnemyIsland();
        if (target != null) return target;

        target = hqTracker.getBestEnemyBase();
        if (target != null) return target;

        target = explore.getExploreTarget();
        return target;
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
            if (r.getType() != RobotType.LAUNCHER) continue;
            d = myLoc.distanceSquaredTo(r.getLocation());
            if (d < minDist || d == minDist){
                minDist = d;
                ans = Direction.CENTER;
            }
            if (rc.canMove(Direction.NORTH)) {
                d = myLocN.distanceSquaredTo(r.getLocation());
                if (d < minDist || d == minDist) {
                    minDist = d;
                    ans = Direction.NORTH;
                }
            }
            if (rc.canMove(Direction.NORTHEAST)) {
                d = myLocNE.distanceSquaredTo(r.getLocation());
                if (d < minDist || d == minDist) {
                    minDist = d;
                    ans = Direction.NORTHEAST;
                }
            }
            if (rc.canMove(Direction.EAST)) {
                d = myLocE.distanceSquaredTo(r.getLocation());
                if (d < minDist || d == minDist) {
                    minDist = d;
                    ans = Direction.EAST;
                }
            }
            if (rc.canMove(Direction.SOUTHEAST)) {
                d = myLocSE.distanceSquaredTo(r.getLocation());
                if (d < minDist || d == minDist) {
                    minDist = d;
                    ans = Direction.SOUTHEAST;
                }
            }
            if (rc.canMove(Direction.SOUTH)) {
                d = myLocS.distanceSquaredTo(r.getLocation());
                if (d < minDist || d == minDist) {
                    minDist = d;
                    ans = Direction.SOUTH;
                }
            }
            if (rc.canMove(Direction.SOUTHWEST)) {
                d = myLocSW.distanceSquaredTo(r.getLocation());
                if (d < minDist || d == minDist) {
                    minDist = d;
                    ans = Direction.SOUTHWEST;
                }
            }
            if (rc.canMove(Direction.WEST)) {
                d = myLocW.distanceSquaredTo(r.getLocation());
                if (d < minDist || d == minDist) {
                    minDist = d;
                    ans = Direction.WEST;
                }
            }
            if (rc.canMove(Direction.NORTHWEST)) {
                d = myLocNW.distanceSquaredTo(r.getLocation());
                if (d < minDist || d == minDist) {
                    minDist = d;
                    ans = Direction.NORTHWEST;
                }
            }
        }
        return ans;
    }

    void moveToTarget() throws GameActionException {
        MapLocation target = getTarget();
        pathfinder.moveTo(target);
    }

    MapLocation getClosestEnemy() throws GameActionException {
        MoveTarget bestMove = null;
        RobotInfo[] enemies = rc.senseNearbyRobots(rc.getType().visionRadiusSquared, rc.getTeam().opponent());
        for (RobotInfo r : enemies){
            if (r.getType() == RobotType.HEADQUARTERS) continue;
            MoveTarget t = new MoveTarget(r);
            if(t.isBetterThan(bestMove)) bestMove = t;
        }
        if (bestMove == null) return null;
        return bestMove.mloc;
    }

    boolean attack() throws GameActionException {
        if (!rc.isActionReady()) return false;
        AttackTarget bestAttacker = null;
        RobotInfo[] enemies = rc.senseNearbyRobots(rc.getType().actionRadiusSquared, rc.getTeam().opponent());
        for (RobotInfo r : enemies){
            if (r.getType() == RobotType.HEADQUARTERS) continue;
            if (!rc.canAttack(r.getLocation())) continue;
            AttackTarget t = new AttackTarget(r);
            if(t.isBetterThan(bestAttacker)) bestAttacker = t;
        }
        if (bestAttacker != null){
            if (rc.canAttack(bestAttacker.mloc)) rc.attack(bestAttacker.mloc);
            return true;
        }
        return false;
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

    class AttackTarget{
        RobotType type;
        int health;
        boolean attacker = false;
        MapLocation mloc;

        boolean isBetterThan(AttackTarget t){
            if (t == null) return true;
            if (attacker & !t.attacker) return true;
            if (!attacker & t.attacker) return false;
            return health <= t.health;
        }

        AttackTarget(RobotInfo r){
            type = r.getType();
            health = r.getHealth();
            mloc = r.getLocation();
            switch(type){
                case LAUNCHER:
                case BOOSTER:
                case DESTABILIZER:
                    attacker = true;
                default:
                    break;
            }
        }
    }

    class MoveTarget{
        RobotType type;
        int health;
        int priority;
        MapLocation mloc;

        boolean isBetterThan(MoveTarget t){
            if (t == null) return true;
            if (priority > t.priority) return true;
            if (priority < t.priority) return true;
            return health <= t.health;
        }

        MoveTarget(RobotInfo r){
            this.type = r.getType();
            this.health = r.getHealth();
            this.mloc = r.getLocation();
            switch (r.getType()){
                case LAUNCHER:
                    priority = 5;
                    break;
                case BOOSTER:
                    priority = 6;
                    break;
                case DESTABILIZER:
                    priority = 4;
                    break;
                case CARRIER:
                    priority = 2;
                    break;
                case AMPLIFIER:
                    priority = 1;
                    break;
            }
        }
    }

    //final static Direction[] dAttacks = new Direction[]{Direction.NORTHWEST, Direction.WEST, Direction.NORTHWEST, Direction.NORTH, Direction.NORTHWEST, Direction.NORTHEAST, Direction.NORTH, Direction.NORTHEAST, Direction.EAST, Direction.NORTHEAST, Direction.SOUTHEAST, Direction.EAST, Direction.SOUTHEAST, Direction.SOUTH, Direction.SOUTHEAST, Direction.SOUTHWEST, Direction.SOUTH, Direction.SOUTHWEST, Direction.WEST, Direction.WEST, Direction.NORTHWEST, Direction.WEST, Direction.NORTH, Direction.NORTHWEST, Direction.NORTHEAST, Direction.NORTH, Direction.EAST, Direction.NORTHEAST, Direction.SOUTHEAST, Direction.EAST, Direction.SOUTH, Direction.SOUTHEAST, Direction.SOUTHWEST, Direction.SOUTH, Direction.WEST, Direction.WEST, Direction.NORTHWEST, Direction.NORTHWEST, Direction.NORTHEAST, Direction.NORTHEAST, Direction.SOUTHEAST, Direction.SOUTHEAST, Direction.SOUTHWEST, Direction.WEST, Direction.NORTHWEST, Direction.NORTHEAST, Direction.SOUTHEAST, Direction.WEST};

    boolean desperateAttack() throws GameActionException {
        if (!rc.isActionReady()) return false;
        if (rc.senseMapInfo(rc.getLocation()).hasCloud()) {
            return desperateAttack2();
        }
        MapLocation[] clouds = rc.senseNearbyCloudLocations();
        for (MapLocation m : clouds){
            if (rc.canAttack(m)){
                rc.attack(m);
                return true;
            }
        }
        return false;
    }

    final static Direction[] dAttacks = new Direction[]{Direction.NORTHWEST, Direction.WEST, Direction.NORTHWEST, Direction.NORTH, Direction.NORTHWEST, Direction.NORTHEAST, Direction.NORTH, Direction.NORTHEAST, Direction.EAST, Direction.NORTHEAST, Direction.SOUTHEAST, Direction.EAST, Direction.SOUTHEAST, Direction.SOUTH, Direction.SOUTHEAST, Direction.SOUTHWEST, Direction.SOUTH, Direction.SOUTHWEST, Direction.WEST, Direction.WEST, Direction.NORTHWEST, Direction.WEST, Direction.NORTH, Direction.NORTHWEST, Direction.NORTHEAST, Direction.NORTH, Direction.EAST, Direction.NORTHEAST, Direction.SOUTHEAST, Direction.EAST, Direction.SOUTH, Direction.SOUTHEAST, Direction.SOUTHWEST, Direction.SOUTH, Direction.WEST, Direction.WEST, Direction.NORTHWEST, Direction.NORTHWEST, Direction.NORTHEAST, Direction.NORTHEAST, Direction.SOUTHEAST, Direction.SOUTHEAST, Direction.SOUTHWEST, Direction.WEST, Direction.NORTHWEST, Direction.NORTHEAST, Direction.SOUTHEAST, Direction.WEST};

    boolean desperateAttack2() throws GameActionException {
        if (!rc.isActionReady()) return false;
        MapLocation loc = rc.getLocation();
        for (int i = dAttacks.length; i-- > 0; ){
            loc = loc.add(dAttacks[i]);
            if (rc.canAttack(loc)){
                rc.attack(loc);
                return true;
            }
        }
        return false;
    }


}
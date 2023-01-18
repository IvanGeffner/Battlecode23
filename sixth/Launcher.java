package sixth;

import battlecode.common.*;

public class Launcher extends Robot {

    MicroAttacker microAttacker;
    static Team opponent;

    HQTracker hqTracker;

    Launcher(RobotController rc) throws GameActionException {
        super(rc);
        opponent = rc.getTeam().opponent();
        microAttacker = new MicroAttacker();
        hqTracker = new HQTracker();
    }

    void play() throws GameActionException {
        while(attack()){}
        int b = Clock.getBytecodeNum();
        while(desperateAttack()){}
        rc.setIndicatorString("" + (Clock.getBytecodeNum() - b));
        if (!microAttacker.doMicro()) moveToTarget();
        while(attack()){}
        while(desperateAttack()){}
    }

    void moveToTarget() throws GameActionException {
        MapLocation target = getClosestEnemy();
        if (target == null) target = getClosestEnemyIsland();
        if (target == null) target = hqTracker.getBestEnemyBase();
        if (target == null) target = explore.getExploreTarget();
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
        MapLocation[] clouds = rc.senseNearbyCloudLocations();
        for (MapLocation m : clouds){
            if (rc.canAttack(m)){
                rc.attack(m);
                return true;
            }
        }
        return false;
    }


}
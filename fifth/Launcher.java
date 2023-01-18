package fifth;

import battlecode.common.*;

public class Launcher extends Robot {

    MicroAttacker microAttacker;
    static Team opponent;

    HQTracker hqTracker;

    int lastTurnCombat = -1000;

    static int MAX_TURNS_AWARE = 5;

    Launcher(RobotController rc) throws GameActionException {
        super(rc);
        opponent = rc.getTeam().opponent();
        microAttacker = new MicroAttacker();
        hqTracker = new HQTracker();
    }

    void play() throws GameActionException {
        while(attack()){}
        while(desperateAttack()){}
        if (microAttacker.doMicro()) lastTurnCombat= rc.getRoundNum();
        else moveToTarget();
        while(attack()){}
        while(desperateAttack()){}
    }

    MapLocation getTarget() throws GameActionException {
        /*if (rc.getRoundNum() <= lastTurnCombat + MAX_TURNS_AWARE){
            MapLocation target = getClosestLauncher();
            if (target != null) return target;
            //return rc.getLocation();
        }*/
        MapLocation target = getClosestEnemy();
        if (target != null) return target;

        target = getClosestEnemyIsland();
        if (target != null) return target;

        if (target == null) target = hqTracker.getBestEnemyBase();
        if (target != null) return target;

        if (target == null) target = explore.getExploreTarget();
        return target;
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

    MapLocation getClosestLauncher() throws GameActionException {
        MapLocation mLoc = null;
        MapLocation myLoc = rc.getLocation();
        RobotInfo[] allies = rc.senseNearbyRobots(rc.getType().visionRadiusSquared, rc.getTeam());
        for (RobotInfo r : allies){
            if (r.getType() != RobotType.LAUNCHER) continue;
            if (mLoc == null || myLoc.distanceSquaredTo(r.getLocation()) < myLoc.distanceSquaredTo(mLoc)){
                mLoc = r.getLocation();
            }

        }
        return mLoc;
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
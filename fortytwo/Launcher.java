package fortytwo;

import battlecode.common.*;

public class Launcher extends Attacker {


    Launcher(RobotController rc) throws GameActionException {
        super(rc);
        generateGreedy();
    }

    void play() throws GameActionException {
        updateBase();
        checkMana();
        while(attack()){}
        if (!microAttacker.doMicro()){
            moveToTarget();
        }
        while(attack()){}
        desperateAttack();
    }

    void moveToTarget() throws GameActionException {
        greedyTarget = getTarget();
        if (roundSeen + TURNS_MEMORY_TARGET >= rc.getRoundNum() && rc.getRoundNum()%2 == 1){
            greedyTarget = null;
            return;
        }

        if (roundSeen + TURNS_STATIC >= rc.getRoundNum()){
            return;
        }
        moveToTargetAttacker();
    }

    boolean attack() throws GameActionException {
        if (!rc.isActionReady()) return false;
        AttackTarget bestAttacker = null;
        RobotInfo[] enemies = rc.senseNearbyRobots(rc.getType().actionRadiusSquared, rc.getTeam().opponent());
        //if (enemies.length == 0) Constants.indicatorString += "Sensing at radius " + rc.getType().actionRadiusSquared + " " + "NO ENEMIES ";
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

    void desperateAttack() throws GameActionException {
        tryAttackBroadcast();
        tryAttackLastSeen();
        if (rc.senseMapInfo(rc.getLocation()).hasCloud()) attackOnCloud();
        else attackToClouds();
    }

    void tryAttackBroadcast() throws GameActionException {
        if (!rc.isActionReady()) return;
        for (int i = 0; i < Robot.hComm.numBases; ++i){
            MapLocation loc = Robot.otherComm.getLauncherLoc(i);
            if (loc != null && rc.getLocation().distanceSquaredTo(loc) <= RobotType.LAUNCHER.actionRadiusSquared) {
                if (!rc.canSenseLocation(loc)) {
                    while(rc.canAttack(loc)) rc.attack(loc);
                }
            }
        }
    }

    void tryAttackLastSeen() throws GameActionException {
        if (closestEnemyLauncher != null && roundSeen + 6 >= rc.getRoundNum()){
            if (rc.canAttack(closestEnemyLauncher) && !rc.canSenseLocation(closestEnemyLauncher)){
                rc.attack(closestEnemyLauncher);
            }
        }
    }

    void attackToClouds() throws GameActionException {
        if (!rc.isActionReady()) return;
        MapLocation[] locs = rc.senseNearbyCloudLocations(RobotType.LAUNCHER.actionRadiusSquared);
        int x = (int)(explore.rng.nextFloat()*locs.length);
        for (int i = locs.length; i-- >0; ){
            if (!rc.isActionReady()) return;
            MapLocation loc = locs[(x+i)%locs.length];
            if (rc.canSenseLocation(loc)) continue;
            while(rc.canAttack(loc)) rc.attack(loc);
        }
    }

    final static Direction[] dAttacks = new Direction[]{Direction.NORTHWEST, Direction.WEST, Direction.NORTHWEST, Direction.NORTH, Direction.NORTHWEST, Direction.NORTHEAST, Direction.NORTH, Direction.NORTHEAST, Direction.EAST, Direction.NORTHEAST, Direction.SOUTHEAST, Direction.EAST, Direction.SOUTHEAST, Direction.SOUTH, Direction.SOUTHEAST, Direction.SOUTHWEST, Direction.SOUTH, Direction.SOUTHWEST, Direction.WEST, Direction.WEST, Direction.NORTHWEST, Direction.WEST, Direction.NORTH, Direction.NORTHWEST, Direction.NORTHEAST, Direction.NORTH, Direction.EAST, Direction.NORTHEAST, Direction.SOUTHEAST, Direction.EAST, Direction.SOUTH, Direction.SOUTHEAST, Direction.SOUTHWEST, Direction.SOUTH, Direction.WEST, Direction.WEST, Direction.NORTHWEST, Direction.NORTHWEST, Direction.NORTHEAST, Direction.NORTHEAST, Direction.SOUTHEAST, Direction.SOUTHEAST, Direction.SOUTHWEST, Direction.WEST, Direction.NORTHWEST, Direction.NORTHEAST, Direction.SOUTHEAST, Direction.WEST};

    boolean attackOnCloud() throws GameActionException {
        if (!rc.isActionReady()) return false;
        MapLocation loc = rc.getLocation();
        for (int i = dAttacks.length; i-- > 0; ){
            if (!rc.isActionReady()) return false;
            loc = loc.add(dAttacks[i]);
            if (!rc.onTheMap(loc) || rc.canSenseLocation(loc)) continue;
            while (rc.canAttack(loc)){
                rc.attack(loc);
            }
        }
        return false;
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

    RobotInfo leader = null;

    void findLeader() throws GameActionException{
        RobotInfo[] allies = rc.senseNearbyRobots(Robot.visionRadiusSquared, rc.getTeam());
        leader = null;
        for (RobotInfo r : allies){
            if (r.getType() != RobotType.LAUNCHER) continue;
            if (r.getID() < rc.getID() && (leader == null || r.getID() < leader.getID())){
                leader = r;
            }
        }
    }

    GreedyStep[] greedyArray;

    MapLocation launcherLoc;
    MapLocation greedyTarget;
    int neutralDistToTarget;

    void performGreedyStep() throws GameActionException {
        neutralDistToTarget = Constants.INF;
        if (greedyTarget != null) neutralDistToTarget = rc.getLocation().distanceSquaredTo(greedyTarget);
        greedyArray[0].updateTarget();
        greedyArray[1].updateTarget();
        greedyArray[2].updateTarget();
        greedyArray[3].updateTarget();
        greedyArray[4].updateTarget();
        greedyArray[5].updateTarget();
        greedyArray[6].updateTarget();
        greedyArray[7].updateTarget();
        greedyArray[8].updateTarget();

        RobotInfo[] allies = rc.senseNearbyRobots(Robot.visionRadiusSquared, rc.getTeam());
        for (RobotInfo r : allies){
            if (Clock.getBytecodeNum() > 7000) break;
            if (r.getType() != RobotType.LAUNCHER) continue;
            launcherLoc = r.getLocation();
            greedyArray[0].updateLauncher();
            greedyArray[1].updateLauncher();
            greedyArray[2].updateLauncher();
            greedyArray[3].updateLauncher();
            greedyArray[4].updateLauncher();
            greedyArray[5].updateLauncher();
            greedyArray[6].updateLauncher();
            greedyArray[7].updateLauncher();
            greedyArray[8].updateLauncher();
        }

        GreedyStep bestGreedy = greedyArray[Direction.CENTER.ordinal()];
        if (greedyArray[0].isBetterThan(bestGreedy)) bestGreedy = greedyArray[0];
        if (greedyArray[1].isBetterThan(bestGreedy)) bestGreedy = greedyArray[1];
        if (greedyArray[2].isBetterThan(bestGreedy)) bestGreedy = greedyArray[2];
        if (greedyArray[3].isBetterThan(bestGreedy)) bestGreedy = greedyArray[3];
        if (greedyArray[4].isBetterThan(bestGreedy)) bestGreedy = greedyArray[4];
        if (greedyArray[5].isBetterThan(bestGreedy)) bestGreedy = greedyArray[5];
        if (greedyArray[6].isBetterThan(bestGreedy)) bestGreedy = greedyArray[6];
        if (greedyArray[7].isBetterThan(bestGreedy)) bestGreedy = greedyArray[7];
        if (greedyArray[8].isBetterThan(bestGreedy)) bestGreedy = greedyArray[8];

        if (rc.canMove(bestGreedy.dir)){
            rc.setIndicatorDot(bestGreedy.mloc, 0, 0, 0);
            Robot.move(bestGreedy.dir);
        }

    }

    void generateGreedy(){
        greedyArray = new GreedyStep[9];
        greedyArray[Direction.CENTER.ordinal()] = new GreedyStep(Direction.CENTER);
        greedyArray[Direction.EAST.ordinal()] = new GreedyStep(Direction.EAST);
        greedyArray[Direction.NORTHEAST.ordinal()] = new GreedyStep(Direction.NORTHEAST);
        greedyArray[Direction.NORTH.ordinal()] = new GreedyStep(Direction.NORTH);
        greedyArray[Direction.NORTHWEST.ordinal()] = new GreedyStep(Direction.NORTHWEST);
        greedyArray[Direction.WEST.ordinal()] = new GreedyStep(Direction.WEST);
        greedyArray[Direction.SOUTHWEST.ordinal()] = new GreedyStep(Direction.SOUTHWEST);
        greedyArray[Direction.SOUTH.ordinal()] = new GreedyStep(Direction.SOUTH);
        greedyArray[Direction.SOUTHEAST.ordinal()] = new GreedyStep(Direction.SOUTHEAST);
    }

    class GreedyStep {

        Direction dir;
        int minDistLauncher;
        int targetDist;
        MapLocation mloc;

        GreedyStep(Direction dir){
            this.dir = dir;
        }

        void updateLauncher(){
            int d = mloc.distanceSquaredTo(launcherLoc);
            if (d < minDistLauncher) minDistLauncher = d;
        }

        void updateTarget(){
            mloc = AdjacentTiles.dirDanger[dir.ordinal()].endLoc;
            if (greedyTarget != null) targetDist = mloc.distanceSquaredTo(greedyTarget);
            else targetDist = 0;
            minDistLauncher = Constants.INF;
        }

        boolean isBetterThan(GreedyStep gs){
            if (!rc.canMove(dir)) return false;
            if (!rc.canMove(gs.dir)) return true;

            if (AdjacentTiles.dirDanger[dir.ordinal()].closeToEnemyHQ) return false;
            if (AdjacentTiles.dirDanger[gs.dir.ordinal()].closeToEnemyHQ) return true;

            if(targetDist >= neutralDistToTarget){
                if (gs.targetDist < neutralDistToTarget) return false;
                return targetDist < gs.targetDist;
            }

            if (gs.targetDist >= neutralDistToTarget) return true;

            if (minDistLauncher < gs.minDistLauncher) return true;
            if (minDistLauncher > gs.minDistLauncher) return false;

            return targetDist < gs.targetDist;
        }

    }


}